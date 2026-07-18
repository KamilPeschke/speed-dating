import { NavLink } from 'react-router-dom'

function navClass({ isActive }: { isActive: boolean }) {
  return `flex flex-col items-center justify-center gap-0.5 text-[11px] font-semibold transition-colors ${
    isActive ? 'text-navy' : 'text-gray-400 hover:text-gray-600'
  }`
}

const S = { fill: 'none', stroke: 'currentColor', strokeWidth: 1.8 } as const

/** 5 zakładek w kolejności ze starego FE: Messages, Matches, Map, Announcements, Profile. */
export function BottomNav() {
  return (
    <nav className="z-[1500] grid h-16 shrink-0 grid-cols-5 border-t border-line bg-white/95 backdrop-blur">
      <NavLink to="/messages" className={navClass}>
        <svg viewBox="0 0 24 24" {...S} className="h-6 w-6">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M21 12c0 4.556-4.03 8.25-9 8.25a9.76 9.76 0 0 1-2.555-.337A5.97 5.97 0 0 1 5.41 20.97a6 6 0 0 1-.474-.065 4.48 4.48 0 0 0 .978-2.025c.09-.457-.133-.901-.467-1.226C3.93 16.178 3 14.189 3 12c0-4.556 4.03-8.25 9-8.25s9 3.694 9 8.25Z"
          />
        </svg>
        Messages
      </NavLink>

      <NavLink to="/matches" className={navClass}>
        <svg viewBox="0 0 24 24" {...S} className="h-6 w-6">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12Z"
          />
        </svg>
        Matches
      </NavLink>

      <NavLink to="/map" className={navClass}>
        <svg viewBox="0 0 24 24" {...S} className="h-6 w-6">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M6 12 3.269 3.125A59.77 59.77 0 0 1 21.485 12 59.77 59.77 0 0 1 3.27 20.875L5.999 12Zm0 0h7.5"
          />
        </svg>
        Map
      </NavLink>

      <NavLink to="/announcements" className={navClass}>
        <svg viewBox="0 0 24 24" {...S} className="h-6 w-6">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M10.34 15.84c-.688-.06-1.386-.09-2.09-.09H7.5a4.5 4.5 0 1 1 0-9h.75c.704 0 1.402-.03 2.09-.09m0 9.18c.253.962.584 1.892.985 2.783.247.55.06 1.21-.463 1.511l-.657.38c-.551.318-1.26.117-1.527-.461a20.85 20.85 0 0 1-1.44-4.282m4.102.317c-.267-1.02-.407-2.09-.407-3.198 0-1.107.14-2.177.407-3.198m0 6.396a23.9 23.9 0 0 1 0-6.396m0 6.396c2.446.21 4.816.7 7.066 1.429A23.98 23.98 0 0 0 18 12c0-2.108-.966-4.318-1.594-5.828-2.25.73-4.62 1.219-7.066 1.43"
          />
        </svg>
        Announcements
      </NavLink>

      <NavLink to="/profile" className={navClass}>
        <svg viewBox="0 0 24 24" {...S} className="h-6 w-6">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.5 20.118a7.5 7.5 0 0 1 15 0A17.93 17.93 0 0 1 12 21.75c-2.676 0-5.216-.584-7.5-1.632Z"
          />
        </svg>
        Profile
      </NavLink>
    </nav>
  )
}
