import { useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { imageApi, errorMessage } from '../lib/api'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import type { Gender } from '../lib/types'

// Profil przeniesiony z ProfileScreen starego FE: karta na szarym tle,
// kółko na zdjęcie (dashed "+ Add Photo"), "Welcome, X!", wiersze danych, Logout.
// Upload zdjęcia woła realne API — backend uploadu jeszcze nie istnieje,
// więc użytkownik dostaje uczciwy komunikat. Zero mocków.

const GENDER_LABEL: Record<Gender, string> = { MALE: 'Male', FEMALE: 'Female' }

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between border-b border-[#eee] py-3">
      <span className="text-mut">{label}</span>
      <span className="font-semibold text-ink">{value}</span>
    </div>
  )
}

export function ProfilePage() {
  const { session, logout } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [photoUrl, setPhotoUrl] = useState<string | null>(null)

  if (!session) return null

  async function onFilePicked(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file || !session) return
    try {
      const { url } = await imageApi.uploadProfilePhoto(session.userId, file)
      setPhotoUrl(url)
      toast('Profile photo updated!', 'ok')
    } catch (err) {
      toast(errorMessage(err))
    }
  }

  function handleLogout() {
    logout()
    navigate('/auth')
  }

  return (
    <div className="flex h-full flex-col justify-center overflow-y-auto bg-paper p-5">
      <div className="rounded-xl bg-white p-6 shadow-md">
        {/* zdjęcie profilowe */}
        <div className="mb-5 flex justify-center">
          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            className="h-[120px] w-[120px] overflow-hidden rounded-full transition active:scale-95"
          >
            {photoUrl ? (
              <img src={photoUrl} alt="profile" className="h-full w-full object-cover" />
            ) : (
              <span className="flex h-full w-full flex-col items-center justify-center border-2 border-dashed border-navy bg-[#e1e4e8]">
                <span className="text-5xl font-light text-navy">+</span>
                <span className="mt-1 text-xs text-mut">Add Photo</span>
              </span>
            )}
          </button>
          <input ref={fileInputRef} type="file" accept="image/*" onChange={onFilePicked} className="hidden" />
        </div>

        <h1 className="text-center text-[26px] font-bold text-ink">Welcome, {session.name ?? session.email}!</h1>
        <p className="mb-7 text-center text-mut">Your Dating Profile</p>

        <Row label="Full Name:" value={session.name ? `${session.name} ${session.surname ?? ''}`.trim() : '—'} />
        <Row label="Email:" value={session.email} />
        <Row label="Age:" value={session.age != null ? String(session.age) : '—'} />
        <Row label="Gender:" value={session.gender ? GENDER_LABEL[session.gender] : '—'} />
        <Row label="Interested In:" value={session.interestedIn ? GENDER_LABEL[session.interestedIn] : '—'} />

        {session.age == null && (
          <p className="mt-4 rounded-lg bg-amber-50 px-3 py-2.5 text-xs font-medium leading-relaxed text-amber-700">
            Some fields are empty because the Java backend has no GET /api/user/me endpoint yet — after a plain login
            only your UUID is known. Register a new account to see the full profile, or add the endpoint 😉
          </p>
        )}

        <p className="mt-2 select-all break-all text-center font-mono text-[11px] text-gray-400">{session.userId}</p>

        <button
          type="button"
          onClick={handleLogout}
          className="mt-7 w-full rounded-lg border border-[#ddd] py-3 font-semibold text-mut transition hover:bg-paper active:scale-[0.98]"
        >
          Logout
        </button>
      </div>
    </div>
  )
}
