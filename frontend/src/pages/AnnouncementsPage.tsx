// Wierna kopia AnnouncementsScreen ze starego FE — tam również był to placeholder
// (jedyny ekran po polsku w tamtej apce). Zero mocków: sekcja po prostu jeszcze nie istnieje.

export function AnnouncementsPage() {
  return (
    <div className="flex h-full flex-col bg-paper">
      <header className="shrink-0 border-b border-line bg-white px-4 py-3">
        <h1 className="text-lg font-bold text-ink">Announcements</h1>
      </header>
      <div className="flex flex-1 flex-col items-center justify-center px-10 text-center">
        <span className="mb-5 text-6xl">📢</span>
        <h2 className="mb-2.5 text-2xl font-bold text-ink">Ogłoszenia</h2>
        <p className="mb-2.5 font-semibold text-navy">Ta sekcja będzie wkrótce dostępna</p>
        <p className="max-w-[300px] text-sm leading-relaxed text-mut">
          Tutaj będziesz mógł przeglądać i dodawać ogłoszenia
        </p>
      </div>
    </div>
  )
}
