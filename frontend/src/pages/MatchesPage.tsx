import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { matchesApi, errorMessage, isNotImplemented } from '../lib/api'
import { useAuth } from '../context/AuthContext'
import type { MatchListItem } from '../lib/types'

// Siatka matchy przeniesiona z MatchesScreen starego FE (kafelki 2 kolumny,
// zdjęcie z gradientem, serce). Backend matchy nie istnieje jeszcze w Javie —
// strona łapie ApiNotImplementedError i mówi to wprost. Zero mocków.

type PageState =
  | { kind: 'loading' }
  | { kind: 'not-implemented' }
  | { kind: 'error'; message: string }
  | { kind: 'ready'; matches: MatchListItem[] }

export function MatchesPage() {
  const { session } = useAuth()
  const navigate = useNavigate()
  const [state, setState] = useState<PageState>({ kind: 'loading' })

  useEffect(() => {
    if (!session) return
    let cancelled = false
    matchesApi
      .list(session.userId)
      .then((matches) => {
        if (!cancelled) setState({ kind: 'ready', matches })
      })
      .catch((e: unknown) => {
        if (cancelled) return
        setState(isNotImplemented(e) ? { kind: 'not-implemented' } : { kind: 'error', message: errorMessage(e) })
      })
    return () => {
      cancelled = true
    }
  }, [session])

  return (
    <div className="flex h-full flex-col bg-paper">
      <header className="shrink-0 border-b border-line bg-white px-4 py-3">
        <h1 className="text-lg font-bold text-ink">Matches</h1>
      </header>

      {state.kind === 'loading' && (
        <div className="flex flex-1 items-center justify-center">
          <p className="text-sm text-mut">Loading matches...</p>
        </div>
      )}

      {state.kind === 'not-implemented' && (
        <div className="flex flex-1 flex-col items-center justify-center px-10 text-center">
          <span className="mb-4 text-6xl">💘</span>
          <h2 className="mb-2 text-xl font-bold text-ink">Matches are coming</h2>
          <p className="mb-1 text-sm font-semibold text-navy">Backend in progress 🚧</p>
          <p className="text-sm leading-relaxed text-mut">
            The 5-minute date flow is being rewritten from NodeJS to Java: chat with someone, and when the timer runs
            out and you both say yes — you'll see each other here.
          </p>
        </div>
      )}

      {state.kind === 'error' && (
        <div className="flex flex-1 items-center justify-center px-10 text-center">
          <p className="text-sm text-red-600">{state.message}</p>
        </div>
      )}

      {state.kind === 'ready' &&
        (state.matches.length === 0 ? (
          /* stary FE: empty state */
          <div className="flex flex-1 flex-col items-center justify-center px-10 text-center">
            <span className="mb-4 text-6xl text-gray-300">💔</span>
            <h2 className="mb-3 text-2xl font-bold text-ink">No matches yet</h2>
            <p className="max-w-[280px] text-[15px] leading-relaxed text-mut">
              Chat with other users, and when the time runs out and you both decide to connect, you'll appear here!
            </p>
          </div>
        ) : (
          <div className="flex-1 overflow-y-auto p-4">
            <h2 className="mb-4 text-xl font-bold text-ink">Your Matches ({state.matches.length})</h2>
            <div className="grid grid-cols-2 gap-3">
              {state.matches.map((m) => (
                <button
                  key={m.id}
                  type="button"
                  onClick={() =>
                    navigate(`/chat/${m.otherUser.id}`, {
                      state: {
                        conversationId: m.chatId,
                        name: m.otherUser.name,
                        age: m.otherUser.age,
                        photo: m.otherUser.profilePhoto,
                      },
                    })
                  }
                  className="relative aspect-[1/1.3] overflow-hidden rounded-2xl bg-line shadow-md transition active:scale-[0.97]"
                >
                  {m.otherUser.profilePhoto ? (
                    <img src={m.otherUser.profilePhoto} alt={m.otherUser.name} className="h-full w-full object-cover" />
                  ) : (
                    <span className="grid h-full w-full place-items-center bg-navy text-5xl font-bold text-white">
                      {m.otherUser.name?.charAt(0).toUpperCase() || '?'}
                    </span>
                  )}
                  <span className="absolute inset-x-0 bottom-0 h-[45%] bg-gradient-to-t from-black/70 to-transparent" />
                  <span className="absolute bottom-3 left-3 right-3 truncate text-left font-bold text-white drop-shadow">
                    {m.otherUser.name}
                    {m.otherUser.age != null ? `, ${m.otherUser.age}` : ''}
                  </span>
                  <span className="absolute right-2.5 top-2.5 grid h-7 w-7 place-items-center rounded-full bg-white/90">
                    <span className="text-sm text-blush">♥</span>
                  </span>
                </button>
              ))}
            </div>
          </div>
        ))}
    </div>
  )
}
