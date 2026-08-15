import { createContext, useCallback, useContext, useEffect, useRef, useState, type ReactNode } from 'react'
import { api, setAccessToken } from '../lib/api'
import type { CreateUserRequest, Gender } from '../lib/types'

export interface Session {
  userId: string
  email: string
  name?: string
  surname?: string
  age?: number
  gender?: Gender
  interestedIn?: Gender
}

interface AuthContextValue {
  session: Session | null
  register: (data: CreateUserRequest) => Promise<void>
  login: (email: string, password: string) => Promise<void>
  logout: () => void
}

const KEY = 'pairs.session'

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(() => {
    try {
      const raw = localStorage.getItem(KEY)
      return raw ? (JSON.parse(raw) as Session) : null
    } catch {
      return null
    }
  })

  const persist = useCallback((s: Session | null) => {
    setSession(s)
    if (s) localStorage.setItem(KEY, JSON.stringify(s))
    else localStorage.removeItem(KEY)
  }, [])

  // Access token żyje tylko w pamięci — po odświeżeniu strony trzeba go odzyskać
  // z httpOnly cookie refresh_token, zanim jakiekolwiek chronione żądanie zadziała.
  //
  // bootstrapped chroni przed podwójnym wywołaniem /user/refresh pod StrictMode
  // (dev odpala efekty dwukrotnie) — backend rotuje refresh token jednorazowo
  // i przy wykryciu powtórnego użycia unieważnia WSZYSTKIE tokeny usera, więc
  // drugie, "widmowe" wywołanie potrafiło ubić sesję zaraz po jej odzyskaniu.
  const bootstrapped = useRef(false)
  useEffect(() => {
    if (!session || bootstrapped.current) return
    bootstrapped.current = true
    api
      .refresh()
      .then((res) => setAccessToken(res.token))
      .catch(() => persist(null))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const register = useCallback(
    async (data: CreateUserRequest) => {
      const res = await api.createUser(data)
      setAccessToken(res.token)
      // rejestracja zwraca tylko { email, uuid, token, ... } — resztę profilu znamy z formularza
      persist({
        userId: res.uuid,
        email: res.email,
        name: data.name,
        surname: data.surname,
        age: data.age,
        gender: data.gender,
        interestedIn: data.interestedIn,
      })
    },
    [persist],
  )

  const login = useCallback(
    async (email: string, password: string) => {
      const res = await api.login(email, password)
      setAccessToken(res.token)
      persist({ userId: res.uuid, email: res.email })
    },
    [persist],
  )

  const logout = useCallback(() => {
    void api.logout().catch(() => {})
    setAccessToken(null)
    persist(null)
  }, [persist])

  return (
    <AuthContext.Provider value={{ session, register, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth musi być użyty wewnątrz <AuthProvider>')
  return ctx
}
