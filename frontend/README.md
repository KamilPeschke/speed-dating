# pairs — frontend (web)

Mobile-first webowy klient aplikacji speed-dating (React 19 + Vite + TypeScript + Tailwind 4 + Leaflet).
Na desktopie renderuje się w „ramce telefonu", na komórce — pełny ekran.

## Uruchomienie (dev)

```powershell
# 1. Infrastruktura + backend (z katalogu głównego repo)
docker compose up -d postgres redis
.\gradlew.bat bootRun          # backend na :3010

# 2. Frontend (z katalogu frontend/)
npm install
npm run dev                    # http://localhost:8082
```

Dev-server Vite proxuje `/api` i `/ws` na `localhost:3010` — zero problemów z CORS.

## Testowanie dwóch użytkowników naraz

Otwórz http://localhost:8082 w normalnym oknie i w oknie incognito.
Zarejestruj dwa konta, na obu przeciągnij pinezkę w to samo miejsce (mapa — marker jest przeciągalny,
nie potrzebujesz prawdziwego GPS), ustaw dopasowane filtry i włącz dostępność.

## Co jest mockiem (do zaimplementowania na backendzie)

- **Czat** — `src/lib/chat.ts`: rozmowy trzymane w localStorage + symulowane odpowiedzi.
  Docelowo: REST (historia) + STOMP (`/app/chat.send` → `/status/queue/messages`).
- **Profil** — brak endpointu `GET /api/user/me`; po zwykłym loginie znamy tylko UUID.

## Mapa

Leaflet + kafelki OpenStreetMap — darmowe, bez klucza API.
Zasięg wyszukiwania ograniczony do **3 km** (walidacja backendu w `LocalizationDto`).
