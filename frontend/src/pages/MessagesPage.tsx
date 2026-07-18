import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { messagingApi, errorMessage, isNotImplemented } from '../lib/api'
import { formatTimestamp } from '../lib/format'
import { useAuth } from '../context/AuthContext'
import { Avatar } from '../components/Avatar'
import type { Conversation } from '../lib/types'

// Lista konwersacji przeniesiona z MessagesScreen starego FE.
// Backend wiadomości jeszcze nie istnieje w Javie — strona woła realne API,
// łapie ApiNotImplementedError i pokazuje uczciwą informację. Zero mocków.

type PageState =
  | { kind: 'loading' }
  | { kind: 'not-implemented' }
  | { kind: 'error'; message: string }
  | { kind: 'ready'; conversations: Conversation[] }

export function MessagesPage() {
  const { session } = useAuth()
  const navigate = useNavigate()
  const [state, setState] = useState<PageState>({ kind: 'loading' })

  useEffect(() => {
    if (!session) return
    let cancelled = false
    messagingApi
      .fetchConversations(session.userId)
      .then((conversations) => {
        if (!cancelled) setState({ kind: 'ready', conversations })
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
    <div className="flex h-full flex-col bg-white">
      <header className="shrink-0 border-b border-line px-4 py-3">
        <h1 className="text-lg font-bold text-ink">Messages</h1>
      </header>

      {state.kind === 'loading' && (
        <div className="flex flex-1 items-center justify-center">
          <p className="text-sm text-mut">Loading conversations...</p>
        </div>
      )}

      {state.kind === 'not-implemented' && (
        <div className="flex flex-1 flex-col items-center justify-center px-10 text-center">
          <span className="mb-4 text-6xl">💬</span>
          <h2 className="mb-2 text-xl font-bold text-ink">Messages are on the way</h2>
          <p className="mb-1 text-sm font-semibold text-navy">Backend in progress 🚧</p>
          <p className="text-sm leading-relaxed text-mut">
            The messaging backend is being rewritten from NodeJS to Java. Conversations, unread badges and read
            receipts will appear here once it's ready.
          </p>
        </div>
      )}

      {state.kind === 'error' && (
        <div className="flex flex-1 items-center justify-center px-10 text-center">
          <p className="text-sm text-red-600">{state.message}</p>
        </div>
      )}

      {state.kind === 'ready' &&
        (state.conversations.length === 0 ? (
          /* stary FE: empty state */
          <div className="flex flex-1 flex-col items-center justify-center px-10 text-center">
            <span className="mb-4 text-6xl">💬</span>
            <h2 className="mb-2 text-xl font-bold text-ink">No Conversations</h2>
            <p className="text-sm text-mut">Find someone on the map and start a conversation!</p>
          </div>
        ) : (
          <ul className="flex-1 divide-y divide-line overflow-y-auto">
            {state.conversations.map((c) => (
              <li key={c.id}>
                <button
                  type="button"
                  onClick={() =>
                    navigate(`/chat/${c.otherUser.id}`, {
                      state: {
                        conversationId: c.id,
                        name: c.otherUser.name,
                        age: c.otherUser.age,
                        photo: c.otherUser.profilePhoto,
                      },
                    })
                  }
                  className="flex w-full items-center gap-3 px-4 py-4 text-left transition hover:bg-paper"
                >
                  <span className="relative">
                    <Avatar name={c.otherUser.name} photo={c.otherUser.profilePhoto} size="lg" />
                    {c.isMatched && (
                      <span className="absolute -bottom-0.5 -left-0.5 grid h-5 w-5 place-items-center rounded-full bg-white text-xs shadow">
                        <span className="text-blush">♥</span>
                      </span>
                    )}
                  </span>
                  <span className="min-w-0 flex-1">
                    <span className="flex items-center justify-between gap-2">
                      <span className="flex items-center gap-2 truncate font-semibold text-ink">
                        {c.otherUser.name}
                        {c.otherUser.age != null ? `, ${c.otherUser.age}` : ''}
                        {c.otherUser.status === 'AVAILABLE' && <span className="h-2.5 w-2.5 rounded-full bg-avail" />}
                      </span>
                      {c.lastMessage && <span className="shrink-0 text-xs text-mut">{formatTimestamp(c.lastMessage.createdAt)}</span>}
                    </span>
                    <span className="mt-0.5 flex items-center justify-between gap-2">
                      <span className={`truncate text-sm ${c.unreadCount > 0 ? 'font-semibold text-ink' : 'text-mut'}`}>
                        {c.lastMessage?.content ?? 'Start conversation'}
                      </span>
                      {c.unreadCount > 0 && (
                        <span className="grid h-6 min-w-6 shrink-0 place-items-center rounded-full bg-navy px-1.5 text-xs font-bold text-white">
                          {c.unreadCount}
                        </span>
                      )}
                    </span>
                  </span>
                </button>
              </li>
            ))}
          </ul>
        ))}
    </div>
  )
}
