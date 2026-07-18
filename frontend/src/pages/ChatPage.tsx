import { useEffect, useState } from 'react'
import { Navigate, useLocation, useNavigate, useParams } from 'react-router-dom'
import { matchesApi, messagingApi, errorMessage, isNotImplemented } from '../lib/api'
import { formatCountdown, formatDistance } from '../lib/format'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { Avatar } from '../components/Avatar'
import { MatchCelebration, MatchDecisionModal } from '../components/MatchFlow'
import type { ChatMessage, Match } from '../lib/types'

// Ekran czatu przeniesiony z ChatScreen starego FE: nagłówek z awatarem,
// dymki (moje granatowe / cudze białe), timer 5-minutowej randki, modal decyzji
// i celebracja "It's a match!". Backend wiadomości/matchy nie istnieje jeszcze
// w Javie — strona woła realne API, łapie ApiNotImplementedError i mówi to
// wprost. Zero mocków. Match-flow ożyje, gdy matchesApi dostanie implementację.

const MATCH_TIMER_DURATION_MS = 5 * 60 * 1000

interface ChatNavState {
  conversationId?: string | null
  name?: string
  age?: number | null
  photo?: string | null
  distance?: number | null
}

type MessagesState =
  | { kind: 'loading' }
  | { kind: 'not-implemented' }
  | { kind: 'error'; message: string }
  | { kind: 'ready'; messages: ChatMessage[] }

export function ChatPage() {
  const { peerId } = useParams<{ peerId: string }>()
  const { state } = useLocation() as { state: ChatNavState | null }
  const { session } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()

  const [messagesState, setMessagesState] = useState<MessagesState>({ kind: 'loading' })
  const [text, setText] = useState('')

  // ── 5-minutowa randka (stary FE: matchData/timeRemaining/showCelebration) ──
  const [match, setMatch] = useState<Match | null>(null)
  const [timeRemaining, setTimeRemaining] = useState<number | null>(null)
  const [showDecision, setShowDecision] = useState(false)
  const [celebration, setCelebration] = useState<{ name: string; age: number | null; photo: string | null } | null>(null)

  const conversationId = state?.conversationId ?? null

  useEffect(() => {
    let cancelled = false
    messagingApi
      .fetchMessages(conversationId ?? '', 50, 0)
      .then((messages) => {
        if (!cancelled) setMessagesState({ kind: 'ready', messages })
      })
      .catch((e: unknown) => {
        if (cancelled) return
        setMessagesState(isNotImplemented(e) ? { kind: 'not-implemented' } : { kind: 'error', message: errorMessage(e) })
      })

    // stan randki dla tej konwersacji (STARTED → timer, DECIDING → modal)
    if (conversationId) {
      matchesApi
        .getByConversation(conversationId)
        .then((m) => {
          if (cancelled || !m) return
          setMatch(m)
          if (m.matchStatus === 'STARTED') {
            const elapsed = Date.now() - new Date(m.createdAt).getTime()
            setTimeRemaining(Math.max(0, MATCH_TIMER_DURATION_MS - elapsed))
          } else if (m.matchStatus === 'DECIDING') {
            setShowDecision(true)
          }
        })
        .catch(() => {
          /* backend matchy jeszcze nie istnieje — timer po prostu się nie pokazuje */
        })
    }
    return () => {
      cancelled = true
    }
  }, [conversationId])

  // odliczanie timera
  useEffect(() => {
    if (timeRemaining === null || timeRemaining <= 0) return
    const id = setInterval(() => setTimeRemaining((prev) => (prev !== null && prev > 1000 ? prev - 1000 : 0)), 1000)
    return () => clearInterval(id)
  }, [timeRemaining])

  if (!peerId) return <Navigate to="/messages" replace />

  const peerName = state?.name ?? 'User'

  async function send(e: React.FormEvent) {
    e.preventDefault()
    if (!text.trim() || !peerId) return
    try {
      await messagingApi.sendMessage(peerId, text.trim(), conversationId)
      setText('')
    } catch (err) {
      toast(errorMessage(err))
    }
  }

  async function decide(decision: boolean) {
    setShowDecision(false)
    if (!match) return
    try {
      await matchesApi.sendDecision(match.id, decision)
      // stary FE: po TAK — modal oczekiwania; pair_matched przyjdzie z websocketu
    } catch (err) {
      toast(errorMessage(err))
    }
  }

  return (
    <div className="relative flex h-full flex-col bg-paper">
      {/* nagłówek (stary FE: headerTitle z awatarem) */}
      <header className="flex shrink-0 items-center gap-2.5 border-b border-line bg-white px-3 py-2.5">
        <button
          type="button"
          onClick={() => navigate(-1)}
          aria-label="Back"
          className="grid h-9 w-9 place-items-center rounded-full text-mut transition hover:bg-paper active:scale-90"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-5 w-5">
            <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 19.5 8.25 12l7.5-7.5" />
          </svg>
        </button>
        <Avatar name={peerName} photo={state?.photo} size="sm" />
        <div className="min-w-0">
          <p className="truncate font-semibold text-ink">
            {peerName}
            {state?.age != null ? `, ${state.age}` : ''}
          </p>
          {state?.distance != null && <p className="text-xs text-mut">{formatDistance(state.distance)}</p>}
        </div>
      </header>

      {/* timer randki (stary FE: "MM:SS to decide") */}
      {timeRemaining !== null && timeRemaining > 0 && (
        <div className="flex shrink-0 items-center justify-center gap-2 border-b border-line bg-white py-3">
          <span className="text-xl">⏱</span>
          <span className="text-2xl font-bold tabular-nums text-navy">{formatCountdown(timeRemaining)}</span>
          <span className="text-sm text-mut">to decide</span>
        </div>
      )}

      {/* wiadomości */}
      <div className="flex min-h-0 flex-1 flex-col overflow-y-auto px-3 py-4">
        {messagesState.kind === 'loading' && <p className="m-auto text-sm text-mut">Loading messages...</p>}

        {messagesState.kind === 'not-implemented' && (
          <div className="m-auto flex max-w-[300px] flex-col items-center text-center">
            <span className="mb-4 text-5xl">🚧</span>
            <h2 className="mb-2 text-lg font-bold text-ink">Chat is being rebuilt</h2>
            <p className="text-sm leading-relaxed text-mut">
              The messaging backend (NodeJS → Java) is under construction. Messages, read receipts, the 5-minute date
              timer and matching will work here once it's ready.
            </p>
          </div>
        )}

        {messagesState.kind === 'error' && <p className="m-auto text-sm text-red-600">{messagesState.message}</p>}

        {messagesState.kind === 'ready' &&
          (messagesState.messages.length === 0 ? (
            <div className="m-auto flex flex-col items-center text-center">
              <span className="mb-3 text-5xl">👋</span>
              <p className="text-mut">No messages. Say hi!</p>
            </div>
          ) : (
            <div className="flex flex-col gap-1.5">
              {messagesState.messages.map((m) => {
                const mine = m.senderId === session?.userId
                return (
                  <div
                    key={m.id}
                    className={`max-w-[75%] rounded-[18px] px-3.5 py-2.5 ${
                      mine ? 'self-end rounded-br-md bg-navy text-white' : 'self-start rounded-bl-md bg-white text-ink'
                    }`}
                  >
                    <p className="text-[15px] leading-snug">{m.content}</p>
                    <p className={`mt-1 text-right text-[11px] ${mine ? 'text-white/80' : 'text-mut'}`}>
                      {new Date(m.createdAt).toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' })}
                      {mine && <span className="ml-1">{m.isRead ? '✓✓' : '✓'}</span>}
                    </p>
                  </div>
                )
              })}
            </div>
          ))}
      </div>

      {/* pole wpisywania (stary FE: input + Send) */}
      <form onSubmit={send} className="flex shrink-0 items-end gap-2 border-t border-line bg-white p-3">
        <input
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Type a message..."
          maxLength={1000}
          disabled={messagesState.kind === 'not-implemented'}
          className="min-w-0 flex-1 rounded-[20px] bg-paper px-4 py-2.5 text-[15px] text-ink outline-none transition placeholder:text-gray-400 focus:ring-2 focus:ring-navy/20 disabled:opacity-60"
        />
        <button
          type="submit"
          disabled={!text.trim() || messagesState.kind === 'not-implemented'}
          className="rounded-[20px] bg-navy px-5 py-2.5 font-semibold text-white transition active:scale-95 disabled:bg-gray-300"
        >
          Send
        </button>
      </form>

      {/* match-flow — renderuje się wyłącznie z realnych danych matchesApi */}
      {showDecision && match && <MatchDecisionModal otherName={peerName} onDecide={decide} />}
      {celebration && session && (
        <MatchCelebration
          myName={session.name ?? session.email}
          myPhoto={null}
          otherName={celebration.name}
          otherAge={celebration.age}
          otherPhoto={celebration.photo}
          onClose={() => setCelebration(null)}
        />
      )}
    </div>
  )
}
