// Komponenty 5-minutowej randki, przeniesione z ChatScreen starego FE.
// Renderują się wyłącznie z realnych danych matcha (matchesApi) — dziś backend
// ich nie dostarcza, więc pozostają nieaktywne. Zero mocków.

import { Avatar } from './Avatar'

/** Modal decyzji: "Time's up! Do you want to match with X?" (stary FE: showMatchModal) */
export function MatchDecisionModal({
  otherName,
  onDecide,
}: {
  otherName: string
  onDecide: (decision: boolean) => void
}) {
  return (
    <div className="absolute inset-0 z-[2000] flex items-center justify-center bg-black/70 p-6">
      <div className="w-full max-w-[340px] rounded-3xl bg-white p-8 text-center shadow-2xl">
        <div className="anim-pulse-heart mb-4 text-6xl">💕</div>
        <h2 className="mb-2 text-2xl font-bold text-ink">Time's up!</h2>
        <p className="mb-6 text-mut">Do you want to match with {otherName}?</p>
        <div className="flex justify-center gap-4">
          <button
            type="button"
            onClick={() => onDecide(false)}
            className="flex items-center gap-2 rounded-full bg-blush px-7 py-3.5 font-semibold text-white transition active:scale-95"
          >
            ✕ No
          </button>
          <button
            type="button"
            onClick={() => onDecide(true)}
            className="flex items-center gap-2 rounded-full bg-avail px-7 py-3.5 font-semibold text-white transition active:scale-95"
          >
            ♥ Yes!
          </button>
        </div>
      </div>
    </div>
  )
}

/** Pełnoekranowa celebracja "It's a match!" (stary FE: showCelebration) */
export function MatchCelebration({
  myName,
  myPhoto,
  otherName,
  otherAge,
  otherPhoto,
  onClose,
}: {
  myName: string
  myPhoto: string | null
  otherName: string
  otherAge: number | null
  otherPhoto: string | null
  onClose: () => void
}) {
  return (
    <div className="absolute inset-0 z-[2100] flex flex-col items-center justify-center bg-gradient-to-b from-[#1a0533] via-[#2d1b69] to-navy px-6">
      {/* dekoracyjne serca w tle */}
      <span className="anim-float-heart absolute left-[10%] top-[12%] text-4xl text-blush">♥</span>
      <span className="anim-float-heart absolute right-[8%] top-[20%] text-2xl text-blushlight [animation-delay:0.7s]">♥</span>
      <span className="anim-float-heart absolute bottom-[18%] right-[15%] text-5xl text-blush [animation-delay:1.2s]">♥</span>
      <span className="anim-float-heart absolute bottom-[25%] left-[12%] text-xl text-blushlight [animation-delay:0.4s]">♥</span>

      {/* zdjęcia + serce */}
      <div className="mb-8 flex items-center justify-center">
        <div className="anim-slide-in-left rounded-full border-[3px] border-white bg-white p-[3px] shadow-2xl">
          <Avatar name={myName} photo={myPhoto} size="xl" />
        </div>
        <div className="anim-heart-pop z-10 -mx-3 grid h-14 w-14 place-items-center rounded-full bg-white shadow-lg">
          <span className="text-3xl text-blush">♥</span>
        </div>
        <div className="anim-slide-in-right rounded-full border-[3px] border-white bg-white p-[3px] shadow-2xl">
          <Avatar name={otherName} photo={otherPhoto} size="xl" />
        </div>
      </div>

      <h1 className="mb-2 text-4xl font-extrabold text-white drop-shadow-lg">It's a match!</h1>
      <p className="mb-3 text-xl font-semibold text-white/90">
        You and {otherName}
        {otherAge != null ? `, ${otherAge}` : ''}
      </p>
      <p className="mb-8 max-w-[300px] text-center text-[15px] leading-relaxed text-white/75">
        You both decided to connect! You can now continue the conversation as a match.
      </p>

      <button
        type="button"
        onClick={onClose}
        className="flex items-center gap-2.5 rounded-full bg-white px-8 py-4 text-[17px] font-bold text-navy shadow-xl transition active:scale-95"
      >
        💬 Continue chatting
      </button>
    </div>
  )
}
