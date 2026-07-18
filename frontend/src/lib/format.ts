/** Format dystansu jak w starym FE: "750 m away" / "1.2 km away" */
export function formatDistance(km: number | null | undefined): string {
  if (km == null) return ''
  return km < 1 ? `${Math.round(km * 1000)} m away` : `${km.toFixed(1)} km away`
}

/** Format czasu wiadomości jak w starym MessagesScreen: dziś → HH:MM, wczoraj → "Yesterday", starsze → data */
export function formatTimestamp(iso: string | null | undefined): string {
  if (!iso) return ''
  const date = new Date(iso)
  const diffInHours = Math.floor((Date.now() - date.getTime()) / (1000 * 60 * 60))
  if (diffInHours < 24) {
    return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
  }
  if (diffInHours < 48) return 'Yesterday'
  return date.toLocaleDateString('en-US', { day: 'numeric', month: 'short' })
}

/** Format timera randki MM:SS (ChatScreen) */
export function formatCountdown(ms: number): string {
  if (ms <= 0) return '00:00'
  const totalSeconds = Math.floor(ms / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
}
