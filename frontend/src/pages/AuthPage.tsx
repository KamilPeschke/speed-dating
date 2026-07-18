import { useState, type InputHTMLAttributes } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { errorMessage } from '../lib/api'
import { Chips } from '../components/Chips'
import type { Gender } from '../lib/types'

// Ekran logowania przeniesiony 1:1 z LoginScreen starego FE:
// granatowe tło #051f57, logo, białe pill-inputy, przełącznik Login/Sign up,
// przyciski płci, suwak wieku 18–100, biały button z granatowym napisem.

function Field(props: InputHTMLAttributes<HTMLInputElement>) {
  return (
    <input
      className="h-[55px] w-full rounded-[30px] border-[1.5px] border-white/30 bg-white/20 px-6 text-[16px] font-medium text-white outline-none transition placeholder:text-white/60 focus:border-white/60"
      {...props}
    />
  )
}

const GENDER_OPTIONS: { value: Gender; label: string }[] = [
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
]

export function AuthPage() {
  const { session, login, register } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()

  const [isSignup, setIsSignup] = useState(false)
  const [pending, setPending] = useState(false)

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [name, setName] = useState('')
  const [surname, setSurname] = useState('')
  const [age, setAge] = useState(18)
  const [gender, setGender] = useState<Gender>('MALE')
  const [interestedIn, setInterestedIn] = useState<Gender>('FEMALE')
  const [error, setError] = useState('')

  if (session) return <Navigate to="/messages" replace />

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    setPending(true)
    try {
      if (isSignup) {
        await register({ email, password, name, surname, age, gender, interestedIn })
        toast('Registration successful! Welcome 🎉', 'ok')
        navigate('/messages')
      } else {
        await login(email, password)
        navigate('/messages')
      }
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="flex h-full flex-col overflow-y-auto bg-navydark px-10 py-8">
      {/* logo ze starego FE */}
      <div className="flex shrink-0 items-center justify-center py-6">
        <img src="/logo.png" alt="logo" className="max-h-44 w-auto max-w-full object-contain" />
      </div>

      <form onSubmit={submit} className="flex flex-col gap-4 pb-8">
        {isSignup && (
          <>
            <Field placeholder="First Name" value={name} onChange={(e) => setName(e.target.value)} required minLength={3} maxLength={20} />
            <Field placeholder="Last Name" value={surname} onChange={(e) => setSurname(e.target.value)} required />
          </>
        )}

        <Field placeholder="Email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoComplete="email" />
        <Field
          placeholder={isSignup ? 'Password (min. 5 characters)' : 'Password'}
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          minLength={5}
          maxLength={20}
          autoComplete={isSignup ? 'new-password' : 'current-password'}
        />

        {isSignup && (
          <>
            <div>
              <p className="mb-2 ml-2.5 text-sm font-semibold text-white">Gender</p>
              <Chips value={gender} onChange={setGender} options={GENDER_OPTIONS} variant="dark" />
            </div>
            <div>
              <p className="mb-2 ml-2.5 text-sm font-semibold text-white">Interested In</p>
              <Chips value={interestedIn} onChange={setInterestedIn} options={GENDER_OPTIONS} variant="dark" />
            </div>
            <div>
              <div className="mb-1 flex items-center justify-between px-2.5">
                <span className="text-sm font-semibold text-white">Age</span>
                <span className="text-2xl font-bold text-white">{age}</span>
              </div>
              <input
                type="range"
                min={18}
                max={100}
                step={1}
                value={age}
                onChange={(e) => setAge(Number(e.target.value))}
                className="w-full accent-white"
              />
              <div className="flex justify-between px-2.5 text-xs text-white/60">
                <span>18</span>
                <span>100</span>
              </div>
            </div>
          </>
        )}

        {error && <p className="text-center text-sm font-semibold text-[#FFE5E5]">{error}</p>}

        <button
          type="submit"
          disabled={pending}
          className="mt-3 rounded-[30px] bg-white py-4 text-lg font-extrabold uppercase tracking-wider text-navy shadow-xl transition active:scale-[0.98] disabled:opacity-60"
        >
          {pending ? '...' : isSignup ? 'Sign Up' : 'Login'}
        </button>

        <button
          type="button"
          onClick={() => {
            setIsSignup(!isSignup)
            setError('')
          }}
          className="mt-2 py-2 text-center text-sm font-semibold text-white underline"
        >
          {isSignup ? 'Already have an account? Login' : "Don't have an account? Sign up"}
        </button>
      </form>
    </div>
  )
}
