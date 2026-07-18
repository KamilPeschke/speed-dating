import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { AttributionControl, Circle, MapContainer, Marker, TileLayer, useMap } from 'react-leaflet'
import L from 'leaflet'
import { api, errorMessage } from '../lib/api'
import { connectStatusFeed } from '../lib/ws'
import { formatDistance } from '../lib/format'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { Avatar } from '../components/Avatar'
import { Chips } from '../components/Chips'
import type { Gender, UserProfile } from '../lib/types'

// Ekran mapy przeniesiony z MapScreen starego FE:
// - toggle "Start Searching" (prawy górny róg, szary → zielony)
// - modal filtrów PRZED włączeniem szukania (płeć, wiek 18–70, zasięg 1/2/3 km)
// - szara nakładka "Set your status to available", gdy offline
// - po włączeniu: lista "Nearby People (N)" z Refresh i zwijaniem
// Dodatki webowe (brak GPS telefonu): przeciągalna pinezka + przycisk GPS.

const DEFAULT_POS = { lat: 52.2297, lon: 21.0122 } // Warszawa — fallback
const PREFS_KEY = 'pairs.map'

const meIcon = L.divIcon({
  className: '',
  iconSize: [18, 18],
  iconAnchor: [9, 9],
  html: `<span class="relative flex h-[18px] w-[18px]"><span class="absolute inline-flex h-full w-full animate-ping rounded-full bg-[#4285f4] opacity-50"></span><span class="relative inline-flex h-[18px] w-[18px] rounded-full bg-[#4285f4] ring-[3px] ring-white shadow-md"></span></span>`,
})

interface MapPrefs {
  lat: number
  lon: number
  radiusKm: number
  gender: Gender
  ageFrom: number
  ageTo: number
}

function loadPrefs(): MapPrefs | null {
  try {
    const raw = localStorage.getItem(PREFS_KEY)
    return raw ? (JSON.parse(raw) as MapPrefs) : null
  } catch {
    return null
  }
}

function FlyTo({ lat, lon }: { lat: number; lon: number }) {
  const map = useMap()
  useEffect(() => {
    map.flyTo([lat, lon])
  }, [map, lat, lon])
  return null
}

const GENDER_OPTIONS: { value: Gender; label: string }[] = [
  { value: 'FEMALE', label: 'FEMALE' },
  { value: 'MALE', label: 'MALE' },
]

export function MapPage() {
  const { session } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()

  const prefs = useRef(loadPrefs()).current
  const [pos, setPos] = useState(() => (prefs ? { lat: prefs.lat, lon: prefs.lon } : DEFAULT_POS))
  const [radiusKm, setRadiusKm] = useState<number>(() => prefs?.radiusKm ?? 3)
  const [gender, setGender] = useState<Gender>(() => prefs?.gender ?? session?.interestedIn ?? 'FEMALE')
  const [ageFrom, setAgeFrom] = useState(() => prefs?.ageFrom ?? 18)
  const [ageTo, setAgeTo] = useState(() => prefs?.ageTo ?? 70)

  const [searching, setSearching] = useState(false)
  const [filterModalOpen, setFilterModalOpen] = useState(false)
  const [pending, setPending] = useState(false)
  const [refreshing, setRefreshing] = useState(false)
  const [listExpanded, setListExpanded] = useState(false)
  const [results, setResults] = useState<UserProfile[]>([])
  const wsDisconnect = useRef<(() => void) | null>(null)

  useEffect(() => {
    localStorage.setItem(PREFS_KEY, JSON.stringify({ ...pos, radiusKm, gender, ageFrom, ageTo } satisfies MapPrefs))
  }, [pos, radiusKm, gender, ageFrom, ageTo])

  useEffect(
    () => () => {
      wsDisconnect.current?.()
    },
    [],
  )

  const localization = { lat: pos.lat, lon: pos.lon, radiusKm }
  const filters = { ageFrom, ageTo, gender }

  async function refresh(silent = false) {
    if (!session) return
    setRefreshing(true)
    try {
      setResults(await api.refreshDiscovery(session.userId, localization, filters))
    } catch (e) {
      if (!silent) toast(errorMessage(e))
    } finally {
      setRefreshing(false)
    }
  }

  /** Stary FE: klik toggle → offline? otwórz filtry : przejdź offline */
  function handleToggle() {
    if (searching) void goOffline()
    else setFilterModalOpen(true)
  }

  async function goOnline() {
    if (!session) return
    setFilterModalOpen(false)
    setPending(true)
    try {
      await api.setAvailable(session.userId, localization, filters)
      wsDisconnect.current = connectStatusFeed(setResults)
      setSearching(true)
      await refresh(true)
    } catch (e) {
      toast(errorMessage(e))
    } finally {
      setPending(false)
    }
  }

  async function goOffline() {
    if (!session) return
    setPending(true)
    try {
      await api.setUnavailable(session.userId)
      wsDisconnect.current?.()
      wsDisconnect.current = null
      setSearching(false)
      setResults([])
    } catch (e) {
      toast(errorMessage(e))
    } finally {
      setPending(false)
    }
  }

  function useGps() {
    if (!navigator.geolocation) {
      toast('Geolocation unavailable — drag the pin instead')
      return
    }
    navigator.geolocation.getCurrentPosition(
      (p) => setPos({ lat: p.coords.latitude, lon: p.coords.longitude }),
      () => toast('Could not get GPS — drag the pin instead'),
      { enableHighAccuracy: true, timeout: 8000 },
    )
  }

  function openUserProfile(u: UserProfile) {
    navigate(`/user/${u.userId}`, { state: { profile: u } })
  }

  function openChat(u: UserProfile) {
    navigate(`/chat/${u.userId}`, {
      state: { name: u.name, age: u.age, photo: u.profilePhotoLink, distance: u.distance },
    })
  }

  const clampAge = (v: number) => Math.max(18, Math.min(70, v))

  return (
    <div className="flex h-full flex-col">
      {/* ── Mapa ── */}
      <div className={`relative transition-all duration-300 ${searching ? (listExpanded ? 'h-[42%]' : 'h-[62%]') : 'flex-1'}`}>
        <MapContainer
          center={[pos.lat, pos.lon]}
          zoom={13}
          zoomControl={false}
          attributionControl={false}
          className="absolute inset-0 z-0 h-full w-full"
        >
          <TileLayer
            url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          />
          <AttributionControl position="topleft" prefix={false} />
          <Marker
            position={[pos.lat, pos.lon]}
            icon={meIcon}
            draggable
            eventHandlers={{
              dragend: (e) => {
                const p = (e.target as L.Marker).getLatLng()
                setPos({ lat: p.lat, lon: p.lng })
              },
            }}
          />
          {searching && (
            <Circle
              center={[pos.lat, pos.lon]}
              radius={radiusKm * 1000}
              pathOptions={{ color: 'rgba(255,75,110,0.5)', weight: 1, fillColor: '#ff4b6e', fillOpacity: 0.1 }}
            />
          )}
          <FlyTo lat={pos.lat} lon={pos.lon} />
        </MapContainer>

        {/* szara nakładka gdy offline (stary FE: mapOverlay) */}
        {!searching && (
          <div className="absolute inset-0 z-[500] flex items-center justify-center bg-[rgba(200,200,200,0.8)]">
            <div className="flex flex-col items-center">
              <svg viewBox="0 0 24 24" fill="none" stroke="#aaa" strokeWidth="1.5" className="h-16 w-16">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M9 6.75V15m6-6v8.25m.503 3.498 4.875-2.437c.381-.19.622-.58.622-1.006V4.82c0-.836-.88-1.38-1.628-1.006l-3.869 1.934c-.317.159-.69.159-1.006 0L9.503 3.252a1.13 1.13 0 0 0-1.006 0L3.622 5.689C3.24 5.88 3 6.27 3 6.695V19.18c0 .836.88 1.38 1.628 1.006l3.869-1.934c.317-.159.69-.159 1.006 0l4.994 2.497c.317.158.69.158 1.006 0Z"
                />
              </svg>
              <p className="mt-5 text-lg font-semibold text-[#555]">Set your status to available</p>
              <p className="mt-1 text-sm text-gray-500">to start searching</p>
            </div>
          </div>
        )}

        {/* toggle "Start Searching" (stary FE: prawy górny róg) */}
        <div className="absolute right-4 top-4 z-[1000]">
          <button
            type="button"
            onClick={handleToggle}
            disabled={pending}
            className="relative h-11 w-[150px] rounded-full bg-white shadow-lg transition active:scale-95 disabled:opacity-60"
          >
            <span className="absolute inset-0 z-10 grid place-items-center text-[13px] font-semibold text-[#333]">
              {pending ? '...' : searching ? 'Searching' : 'Start Searching'}
            </span>
            <span
              className={`absolute top-1 h-9 w-9 rounded-full shadow transition-all duration-300 ${
                searching ? 'left-[110px] bg-avail' : 'left-1 bg-[#ccc]'
              }`}
            />
          </button>
        </div>

        {/* przycisk GPS (dodatek webowy) */}
        <button
          type="button"
          onClick={useGps}
          title="Use my GPS location"
          className="absolute bottom-4 right-4 z-[1000] grid h-11 w-11 place-items-center rounded-full bg-white text-navy shadow-lg transition active:scale-90"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="h-5 w-5">
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 8.25a3.75 3.75 0 1 0 0 7.5 3.75 3.75 0 0 0 0-7.5Z" />
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 2.25v3m0 13.5v3m9.75-9.75h-3M5.25 12h-3" />
          </svg>
        </button>

        {!searching && (
          <p className="absolute bottom-4 left-4 z-[600] rounded-full bg-white/90 px-3 py-1.5 text-xs font-medium text-mut shadow">
            📍 Drag the pin to set your position
          </p>
        )}
      </div>

      {/* ── Lista "Nearby People" (tylko gdy szukamy) ── */}
      {searching && (
        <div className="z-[600] -mt-5 flex min-h-0 flex-1 flex-col rounded-t-[20px] bg-white px-5 pt-5">
          <div className="mb-3 flex shrink-0 items-center justify-between">
            <h2 className="text-lg font-bold text-[#333]">Nearby People ({results.length})</h2>
            <div className="flex items-center gap-2.5">
              <button
                type="button"
                onClick={() => refresh()}
                disabled={refreshing}
                className="rounded-full bg-paper px-3 py-1.5 text-sm font-semibold text-navy transition active:scale-95"
              >
                {refreshing ? '...' : 'Refresh'}
              </button>
              <button
                type="button"
                onClick={() => setListExpanded(!listExpanded)}
                className="rounded-full bg-paper px-3 py-1.5 text-navy transition active:scale-95"
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className={`h-4 w-4 transition-transform ${listExpanded ? 'rotate-180' : ''}`}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="m4.5 15.75 7.5-7.5 7.5 7.5" />
                </svg>
              </button>
            </div>
          </div>

          {results.length === 0 ? (
            <p className="py-8 text-center text-sm text-mut">
              No one nearby yet. Try widening the range or open a second browser window to test 😉
            </p>
          ) : (
            <ul className="min-h-0 flex-1 divide-y divide-[#f0f0f0] overflow-y-auto pb-2">
              {results.map((u) => (
                <li key={u.userId} className="flex items-center gap-3 py-3">
                  <button type="button" onClick={() => openUserProfile(u)} className="transition active:scale-95">
                    <Avatar name={u.name} photo={u.profilePhotoLink} />
                  </button>
                  <div className="min-w-0 flex-1">
                    <p className="truncate font-semibold text-ink">
                      {u.name}
                      {u.age != null ? `, ${u.age}` : ''}
                    </p>
                    <p className="text-sm text-gray-400">{formatDistance(u.distance)}</p>
                  </div>
                  <button
                    type="button"
                    onClick={() => openChat(u)}
                    title="Send message"
                    className="grid h-9 w-9 place-items-center rounded-full bg-paper text-navy transition active:scale-90"
                  >
                    <svg viewBox="0 0 24 24" fill="currentColor" className="h-5 w-5">
                      <path d="M4.913 2.658c2.075-.27 4.19-.408 6.337-.408 2.147 0 4.262.139 6.337.408 1.922.25 3.291 1.861 3.405 3.727a4.4 4.4 0 0 0-1.032-.211 51 51 0 0 0-8.71 0c-2.365.207-4.099 2.145-4.099 4.486v4.286a4.47 4.47 0 0 0 2.433 3.984L7.28 21.53A.75.75 0 0 1 6 21v-4.03a48 48 0 0 1-1.087-.128C2.905 16.58 1.5 14.833 1.5 12.862V6.638c0-1.97 1.405-3.718 3.413-3.979Z" />
                      <path d="M15.75 7.5c-1.376 0-2.739.057-4.086.169C10.124 7.797 9 9.103 9 10.609v4.285c0 1.507 1.128 2.814 2.67 2.94q1.774.147 3.58.173l2.22 2.22a.75.75 0 0 0 1.28-.53v-2.03q.618-.041 1.227-.1c1.54-.127 2.67-1.433 2.67-2.94v-4.28c0-1.506-1.128-2.812-2.67-2.94A49 49 0 0 0 15.75 7.5" />
                    </svg>
                  </button>
                  <span className="h-2.5 w-2.5 shrink-0 rounded-full bg-avail" />
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      {/* ── Modal filtrów (stary FE: "Choose Filters" przed goOnline) ── */}
      {filterModalOpen && (
        <div className="absolute inset-0 z-[2000] flex items-center justify-center bg-black/50 p-5">
          <div className="w-full rounded-[20px] bg-white p-7 shadow-2xl">
            <h2 className="mb-5 text-center text-xl font-bold text-ink">Choose Filters</h2>

            <p className="mb-2.5 text-base font-semibold text-[#333]">Gender</p>
            <div className="mb-5">
              <Chips value={gender} onChange={setGender} options={GENDER_OPTIONS} />
            </div>

            <p className="mb-2.5 text-base font-semibold text-[#333]">Age Range</p>
            <div className="mb-4 rounded-[10px] bg-[#f7f8fa] py-3.5 text-center">
              <span className="text-lg font-semibold text-navy">
                {ageFrom} - {ageTo} years old
              </span>
            </div>

            <div className="mb-1.5 flex items-center justify-between">
              <span className="text-sm font-semibold text-[#555]">From</span>
              <span className="font-bold text-navy">{ageFrom}</span>
            </div>
            <input
              type="range"
              min={18}
              max={70}
              step={1}
              value={ageFrom}
              onChange={(e) => {
                const v = clampAge(Number(e.target.value))
                setAgeFrom(v)
                if (v > ageTo) setAgeTo(v)
              }}
              className="mb-4 w-full accent-navy"
            />

            <div className="mb-1.5 flex items-center justify-between">
              <span className="text-sm font-semibold text-[#555]">To</span>
              <span className="font-bold text-navy">{ageTo}</span>
            </div>
            <input
              type="range"
              min={18}
              max={70}
              step={1}
              value={ageTo}
              onChange={(e) => {
                const v = clampAge(Number(e.target.value))
                setAgeTo(v)
                if (v < ageFrom) setAgeFrom(v)
              }}
              className="mb-5 w-full accent-navy"
            />

            <p className="mb-2.5 text-base font-semibold text-[#333]">Range (km)</p>
            <div className="mb-6 flex gap-2.5">
              {[1, 2, 3].map((km) => (
                <button
                  key={km}
                  type="button"
                  onClick={() => setRadiusKm(km)}
                  className={`flex-1 rounded-[10px] border py-2.5 font-semibold transition active:scale-95 ${
                    radiusKm === km ? 'border-navy bg-navy text-white' : 'border-[#ddd] bg-white text-[#333]'
                  }`}
                >
                  {km} km
                </button>
              ))}
            </div>

            <button
              type="button"
              onClick={goOnline}
              className="w-full rounded-[25px] bg-navy py-4 font-bold text-white transition active:scale-[0.98]"
            >
              Search
            </button>
            <button
              type="button"
              onClick={() => setFilterModalOpen(false)}
              className="mt-3 w-full py-1 text-center text-gray-500"
            >
              Cancel
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
