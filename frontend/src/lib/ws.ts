import { Client } from '@stomp/stompjs'
import type { UserProfile } from './types'

// STOMP po czystym WebSocket (backend: registry.addEndpoint("/ws") bez SockJS).
// Połączenie idzie przez proxy Vite: ws://localhost:8082/ws -> ws://localhost:3010/ws
//
// ── UWAGA (backend, do zrobienia po stronie BE) ─────────────────────────────
// convertAndSendToUser dostarcza wiadomość przez SimpUserRegistry, a ten jest
// zasilany Principalem sesji WS — bez auth na handshake'u/CONNECT rejestr jest
// pusty i wiadomości per-user są PO CICHU odrzucane (zweryfikowane na bajtkodzie
// spring-messaging 7.0.6: getUser(name)==null -> Collections.emptySet -> drop).
// Brak wiodącego "/" w destination ("queue/userStatusChange") NIE jest bugiem —
// Spring normalizuje prefix i destination sam. FE zadziała od razu, gdy backend
// doda Principala o nazwie równej userId (UUID.toString()).
// ─────────────────────────────────────────────────────────────────────────────
const STATUS_FEED_DESTINATION = '/status/queue/userStatusChange'

export function connectStatusFeed(onUsers: (users: UserProfile[]) => void): () => void {
  const proto = location.protocol === 'https:' ? 'wss' : 'ws'
  const client = new Client({
    brokerURL: `${proto}://${location.host}/ws`,
    reconnectDelay: 5000,
    debug: () => {},
    onConnect: () => {
      client.subscribe(STATUS_FEED_DESTINATION, (message) => {
        try {
          onUsers(JSON.parse(message.body) as UserProfile[])
        } catch (e) {
          console.warn('[ws] nieparsowalna wiadomość', e)
        }
      })
    },
    onStompError: (frame) => console.warn('[ws] STOMP error', frame.headers.message),
  })
  client.activate()
  return () => {
    void client.deactivate()
  }
}
