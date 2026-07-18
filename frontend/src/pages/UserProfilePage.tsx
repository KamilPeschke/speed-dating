import { Navigate, useLocation, useNavigate, useParams } from 'react-router-dom'
import { formatDistance } from '../lib/format'
import type { UserProfile } from '../lib/types'

// Pełnoekranowy profil usera przeniesiony z UserProfileScreen starego FE:
// zdjęcie na 3/4 ekranu, gradient, imię+wiek, dystans, FAB wiadomości.
// Dane przychodzą z realnych wyników discovery (nawigacja z mapy).

export function UserProfilePage() {
  const { userId } = useParams<{ userId: string }>()
  const { state } = useLocation() as { state: { profile: UserProfile } | null }
  const navigate = useNavigate()

  const profile = state?.profile
  if (!userId || !profile) return <Navigate to="/map" replace />

  return (
    <div className="relative flex h-full flex-col bg-black">
      {/* zdjęcie (75% wysokości jak w starym FE) */}
      <div className="relative h-[75%] w-full overflow-hidden">
        {profile.profilePhotoLink ? (
          <img src={profile.profilePhotoLink} alt={profile.name} className="h-full w-full object-cover" />
        ) : (
          <div className="grid h-full w-full place-items-center bg-navy">
            <span className="text-[120px] font-bold text-white">{profile.name?.charAt(0).toUpperCase() || '?'}</span>
          </div>
        )}
      </div>

      <div className="pointer-events-none absolute inset-x-0 bottom-0 h-[55%] bg-gradient-to-t from-black/80 to-transparent" />

      {/* przycisk wstecz */}
      <button
        type="button"
        onClick={() => navigate(-1)}
        aria-label="Back"
        className="absolute left-5 top-5 z-10 grid h-11 w-11 place-items-center rounded-full bg-black/30 text-white transition active:scale-90"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-6 w-6">
          <path strokeLinecap="round" strokeLinejoin="round" d="M10.5 19.5 3 12m0 0 7.5-7.5M3 12h18" />
        </svg>
      </button>

      {/* dane + FAB */}
      <div className="absolute inset-x-0 bottom-10 flex items-end justify-between px-5">
        <div className="min-w-0 flex-1 pr-4">
          <h1 className="mb-2 text-3xl font-bold text-white">
            {profile.name}
            {profile.age != null ? `, ${profile.age}` : ''}
          </h1>
          {profile.distance != null && (
            <p className="flex items-center gap-1.5 text-white">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="h-4 w-4">
                <path strokeLinecap="round" strokeLinejoin="round" d="M15 10.5a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" />
                <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 10.5c0 7.142-7.5 11.25-7.5 11.25S4.5 17.642 4.5 10.5a7.5 7.5 0 1 1 15 0Z" />
              </svg>
              {formatDistance(profile.distance)}
            </p>
          )}
          {/* bio pojawi się, gdy backend Java doda pole bio do profilu */}
        </div>

        <button
          type="button"
          onClick={() =>
            navigate(`/chat/${profile.userId}`, {
              state: { name: profile.name, age: profile.age, photo: profile.profilePhotoLink, distance: profile.distance },
            })
          }
          aria-label="Send message"
          className="grid h-[60px] w-[60px] shrink-0 place-items-center rounded-full bg-navy text-white shadow-2xl transition active:scale-90"
        >
          <svg viewBox="0 0 24 24" fill="currentColor" className="h-7 w-7">
            <path d="M4.913 2.658c2.075-.27 4.19-.408 6.337-.408 2.147 0 4.262.139 6.337.408 1.922.25 3.291 1.861 3.405 3.727a4.4 4.4 0 0 0-1.032-.211 51 51 0 0 0-8.71 0c-2.365.207-4.099 2.145-4.099 4.486v4.286a4.47 4.47 0 0 0 2.433 3.984L7.28 21.53A.75.75 0 0 1 6 21v-4.03a48 48 0 0 1-1.087-.128C2.905 16.58 1.5 14.833 1.5 12.862V6.638c0-1.97 1.405-3.718 3.413-3.979Z" />
            <path d="M15.75 7.5c-1.376 0-2.739.057-4.086.169C10.124 7.797 9 9.103 9 10.609v4.285c0 1.507 1.128 2.814 2.67 2.94q1.774.147 3.58.173l2.22 2.22a.75.75 0 0 0 1.28-.53v-2.03q.618-.041 1.227-.1c1.54-.127 2.67-1.433 2.67-2.94v-4.28c0-1.506-1.128-2.812-2.67-2.94A49 49 0 0 0 15.75 7.5" />
          </svg>
        </button>
      </div>
    </div>
  )
}
