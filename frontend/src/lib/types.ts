// ─────────────────────────────────────────────────────────────────────────────
// CZĘŚĆ 1: typy 1:1 z rekordami OBECNEGO backendu Java (Spring Boot).
// Nazwy pól = klucze JSON.
// ─────────────────────────────────────────────────────────────────────────────

export type Gender = 'MALE' | 'FEMALE'
export type UserStatus = 'AVAILABLE' | 'UNAVAILABLE' | 'HIDDEN'

/** LocalizationDto — backend waliduje radiusKm w zakresie 0–3 km */
export interface Localization {
  lat: number
  lon: number
  radiusKm: number
}

/** FiltersDto — wiek 18–80 (stary FE używał zakresu 18–70 w UI) */
export interface Filters {
  ageFrom: number
  ageTo: number
  gender: Gender
}

/** discovery/UserProfile */
export interface UserProfile {
  userId: string
  name: string
  age: number | null
  gender: Gender
  /** dystans w km (GeoUnit.KM po stronie backendu) */
  distance: number | null
  profilePhotoLink: string | null
  photos: string[] | null
}

/** user/dto/CreateUserDto */
export interface CreateUserRequest {
  email: string
  password: string
  name: string
  surname: string
  age: number
  gender: Gender
  interestedIn: Gender
}

/** user/response/CreateUserResponse — pole nazywa się `uuid`, nie `userId`! */
export interface CreateUserResponse {
  email: string
  uuid: string
}

/** user/UpdateUserStatus */
export interface UpdateUserStatus {
  userId: string
  userStatus: UserStatus
}

// ─────────────────────────────────────────────────────────────────────────────
// CZĘŚĆ 2: typy funkcji, których backend Java JESZCZE NIE MA.
// Odtworzone z kontraktu starego backendu NodeJS (źródło: D:/Pulpit/frontend).
// UI jest gotowe i czeka — po dodaniu endpointów w Javie wystarczy podpiąć
// implementacje w lib/api.ts (sekcja "stuby").
// ─────────────────────────────────────────────────────────────────────────────

/** Skrócone dane rozmówcy w liście konwersacji (stary BE: conversations_list) */
export interface ConversationPeer {
  id: string
  name: string
  age: number | null
  profilePhoto: string | null
  status?: UserStatus
}

export interface LastMessage {
  content: string
  createdAt: string
  isRead: boolean
  senderId: string
}

/** Stary BE: element listy konwersacji */
export interface Conversation {
  id: string
  otherUser: ConversationPeer
  lastMessage: LastMessage | null
  unreadCount: number
  /** serduszko przy awatarze, gdy rozmowa jest po matchu */
  isMatched?: boolean
}

/** Stary BE: pojedyncza wiadomość (messages_history / new_message) */
export interface ChatMessage {
  id: string
  senderId: string
  content: string
  createdAt: string
  isRead: boolean
  readAt?: string | null
}

/** Stary BE: statusy 5-minutowej "randki" */
export type MatchStatus = 'STARTED' | 'DECIDING' | 'MATCHED' | 'REJECTED'

/** Stary BE: GET /api/matches/conversation?conversationId= */
export interface Match {
  id: string
  chatId: string
  matchStatus: MatchStatus
  createdAt: string
  initiatorId: string
  initiatorDecision: boolean | null
  receiverDecision: boolean | null
}

/** Stary BE: GET /api/matches?userId= — kafelek na liście matchy */
export interface MatchListItem {
  id: string
  chatId: string
  otherUser: ConversationPeer
}
