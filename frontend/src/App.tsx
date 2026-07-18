import { Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import { BottomNav } from './components/BottomNav'
import { AuthPage } from './pages/AuthPage'
import { MapPage } from './pages/MapPage'
import { MessagesPage } from './pages/MessagesPage'
import { MatchesPage } from './pages/MatchesPage'
import { AnnouncementsPage } from './pages/AnnouncementsPage'
import { ChatPage } from './pages/ChatPage'
import { UserProfilePage } from './pages/UserProfilePage'
import { ProfilePage } from './pages/ProfilePage'

/** Ramka telefonu: na mobile pełny ekran, na desktopie wycentrowany „telefon". */
function Shell() {
  const { session } = useAuth()
  return (
    <div className="min-h-dvh bg-gradient-to-br from-neutral-200 via-neutral-100 to-blue-100 md:flex md:items-center md:justify-center md:p-6">
      <div className="relative flex h-dvh w-full flex-col overflow-hidden bg-white md:h-[52rem] md:max-h-[92vh] md:w-[26rem] md:rounded-[2.5rem] md:shadow-2xl md:ring-8 md:ring-neutral-900">
        <main className="relative min-h-0 flex-1">
          <Outlet />
        </main>
        {session && <BottomNav />}
      </div>
    </div>
  )
}

function RequireAuth() {
  const { session } = useAuth()
  return session ? <Outlet /> : <Navigate to="/auth" replace />
}

export default function App() {
  return (
    <Routes>
      <Route element={<Shell />}>
        <Route path="/auth" element={<AuthPage />} />
        <Route element={<RequireAuth />}>
          {/* kolejność zakładek jak w starym FE; domyślny ekran = Messages */}
          <Route index element={<Navigate to="/messages" replace />} />
          <Route path="/messages" element={<MessagesPage />} />
          <Route path="/matches" element={<MatchesPage />} />
          <Route path="/map" element={<MapPage />} />
          <Route path="/announcements" element={<AnnouncementsPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/chat/:peerId" element={<ChatPage />} />
          <Route path="/user/:userId" element={<UserProfilePage />} />
        </Route>
        <Route path="*" element={<Navigate to="/messages" replace />} />
      </Route>
    </Routes>
  )
}
