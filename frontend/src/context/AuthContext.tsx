import { createContext, useCallback, useContext, useState, type ReactNode } from 'react'
import { api } from '../lib/api'
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

  const register = useCallback(
    async (data: CreateUserRequest) => {
      const res = await api.createUser(data)
      // rejestracja zwraca tylko { email, uuid } — resztę profilu znamy z formularza
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
      const uuid = await api.login(email, password)
      // login zwraca sam UUID — brak GET /user/me na backendzie, więc profil zostaje pusty
      persist({ userId: uuid, email })
    },
    [persist],
  )

  const logout = useCallback(() => persist(null), [persist])

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
