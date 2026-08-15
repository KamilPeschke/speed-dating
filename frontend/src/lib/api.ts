import type {
  Conversation,
  ChatMessage,
  CreateUserRequest,
  CreateUserResponse,
  Filters,
  Localization,
  Match,
  MatchListItem,
  UpdateUserStatus,
  UserProfile,
} from './types'

// Wszystkie żądania idą na względne /api/... — dev-server Vite proxuje je na backend :3010.
const BASE = '/api'

export class ApiError extends Error {
  readonly status: number
  constructor(message: string, status: number) {
    super(message)
    this.status = status
  }
}

/**
 * Rzucany przez stuby funkcji, których backend Java jeszcze nie ma.
 * UI łapie ten typ i pokazuje uczciwy komunikat "w budowie" — ZERO mocków.
 */
export class ApiNotImplementedError extends ApiError {
  constructor(feature: string) {
    super(`${feature} — backend jeszcze nie istnieje (przepisywany z NodeJS na Javę)`, 501)
  }
}

// Access token żyje tylko w pamięci (nie w localStorage — ochrona przed kradzieżą przez XSS).
// Po odświeżeniu strony AuthContext odzyskuje go z httpOnly cookie refresh_token przez /user/refresh.
let accessToken: string | null = null

export function setAccessToken(token: string | null): void {
  accessToken = token
}

/** Wszystkie istniejące endpointy backendu Java są POST-ami. */
async function post<T>(path: string, body?: unknown): Promise<T> {
  let res: Response
  try {
    res = await fetch(BASE + path, {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      },
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch {
    throw new ApiError('No connection to backend (port 3010) — run: gradlew bootRun', 0)
  }

  if (!res.ok) {
    // GlobalExceptionHandler zwraca ErrorResponseHandler { message, statusCode }
    let message = `Server error (${res.status})`
    try {
      const data: unknown = await res.json()
      if (data && typeof data === 'object' && 'message' in data && typeof data.message === 'string') {
        message = data.message
      }
    } catch {
      /* odpowiedź bez JSON-a — zostaje komunikat domyślny */
    }
    throw new ApiError(message, res.status)
  }

  const text = await res.text()
  return (text ? JSON.parse(text) : undefined) as T
}

// ─────────────────────────────────────────────────────────────────────────────
// DZIAŁAJĄCE — podpięte pod istniejący backend Java
// ─────────────────────────────────────────────────────────────────────────────

export const api = {
  /** POST /api/user/create */
  createUser: (data: CreateUserRequest) => post<CreateUserResponse>('/user/create', data),

  /** POST /api/user/login — zwraca CreateUserResponse (email, uuid, token, tokenType, expiresInMs) */
  login: (email: string, password: string) => post<CreateUserResponse>('/user/login', { email, password }),

  /** POST /api/user/refresh — mintuje nowy access token na podstawie httpOnly cookie refresh_token */
  refresh: () => post<CreateUserResponse>('/user/refresh'),

  /** POST /api/user/logout — unieważnia refresh token po stronie backendu */
  logout: () => post<void>('/user/logout'),

  /** POST /api/user/status-available — userId bierze backend z JWT, nie z URL-a */
  setAvailable: (localization: Localization, filters: Filters) =>
    post<UpdateUserStatus>('/user/status-available', { localization, filters }),

  /** POST /api/user/status-unavailable — usuwa z puli */
  setUnavailable: () => post<UpdateUserStatus>('/user/status-unavailable'),

  /** POST /api/discovery/refresh/{userId} — userAge/userGender uzupełnia backend z bazy */
  refreshDiscovery: (userId: string, localization: Localization, filters: Filters) =>
    post<UserProfile[]>(`/discovery/refresh/${userId}`, { localization, filters }),
}

// ─────────────────────────────────────────────────────────────────────────────
// STUBY — funkcje ze starego FE, na które backend Java dopiero powstanie.
// Każdy stub dokumentuje ORYGINALNY kontrakt starego backendu NodeJS
// (namespace'y Socket.IO + REST), jako spec do implementacji w Javie.
// Po dodaniu endpointu: podmień `throw` na realne wywołanie i UI ożyje.
// ─────────────────────────────────────────────────────────────────────────────

export const messagingApi = {
  /**
   * Stary BE: GET /api/messaging/conversation/find?userOneId=&userTwoId=
   * → { conversationId: string | null }
   */
  findConversation: async (_userOneId: string, _userTwoId: string): Promise<{ conversationId: string | null }> => {
    throw new ApiNotImplementedError('Wiadomości (find conversation)')
  },

  /**
   * Stary BE: Socket.IO /messaging, emit "fetch_conversations" → event "conversations_list".
   * Docelowo w Javie: REST GET + STOMP na aktualizacje.
   */
  fetchConversations: async (_userId: string): Promise<Conversation[]> => {
    throw new ApiNotImplementedError('Wiadomości (lista konwersacji)')
  },

  /**
   * Stary BE: Socket.IO /messaging, emit "fetch_messages" {conversationId, limit, offset}
   * → event "messages_history" (paginacja po 50, od najnowszych).
   */
  fetchMessages: async (_conversationId: string, _limit: number, _offset: number): Promise<ChatMessage[]> => {
    throw new ApiNotImplementedError('Wiadomości (historia)')
  },

  /**
   * Stary BE: Socket.IO /messaging, emit "send_message" {recipientId, content, conversationId|null}
   * → eventy: "new_message" (obie strony), "message_delivered" (nadawca).
   * Dodatkowo: "mark_as_read" {conversationId} → "message_read" {conversationId, readAt}
   * oraz "delete_message" {messageId, conversationId} → "message_deleted" (long-press na własnej).
   */
  sendMessage: async (_recipientId: string, _content: string, _conversationId: string | null): Promise<void> => {
    throw new ApiNotImplementedError('Wiadomości (wysyłka)')
  },
}

export const matchesApi = {
  /** Stary BE: GET /api/matches?userId= → MatchListItem[] (kafelki "Your Matches") */
  list: async (_userId: string): Promise<MatchListItem[]> => {
    throw new ApiNotImplementedError('Matches (lista)')
  },

  /**
   * Stary BE: GET /api/matches/conversation?conversationId= → Match.
   * Logika 5-minutowej randki: status STARTED (timer 5:00 od createdAt)
   * → DECIDING (obie strony wybierają TAK/NIE)
   * → Socket.IO /matches: emit "pair_decided" {matchId, decision};
   *   eventy: "pair_started", "pair_matched" {match, initiator, receiver}, "pair_rejected".
   */
  getByConversation: async (_conversationId: string): Promise<Match | null> => {
    throw new ApiNotImplementedError('Matches (stan randki)')
  },

  /** Stary BE: Socket.IO /matches, emit "pair_decided" {matchId, decision: boolean} */
  sendDecision: async (_matchId: string, _decision: boolean): Promise<void> => {
    throw new ApiNotImplementedError('Matches (decyzja)')
  },
}

export const imageApi = {
  /**
   * Stary BE: POST /image/upload?userId= (multipart/form-data, pole "file")
   * → { url } (Cloudinary). W Javie do zrobienia np. przez spring-web multipart + Cloudinary SDK.
   */
  uploadProfilePhoto: async (_userId: string, _file: File): Promise<{ url: string }> => {
    throw new ApiNotImplementedError('Upload zdjęcia profilowego')
  },
}

export function errorMessage(e: unknown): string {
  return e instanceof Error ? e.message : 'Something went wrong'
}

export function isNotImplemented(e: unknown): e is ApiNotImplementedError {
  return e instanceof ApiNotImplementedError
}
