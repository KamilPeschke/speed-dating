interface ChipOption<T extends string> {
  value: T
  label: string
}

interface ChipsProps<T extends string> {
  value: T
  onChange: (v: T) => void
  options: ChipOption<T>[]
  /** 'light' — na białym tle (modal filtrów); 'dark' — na granatowym (ekran logowania) */
  variant?: 'light' | 'dark'
}

/** Przyciski wyboru jak w starym FE (gender selector). */
export function Chips<T extends string>({ value, onChange, options, variant = 'light' }: ChipsProps<T>) {
  return (
    <div className="flex w-full gap-2.5">
      {options.map((o) => {
        const active = value === o.value
        const cls =
          variant === 'dark'
            ? active
              ? 'bg-white text-navy border-white font-extrabold'
              : 'bg-white/20 text-white/80 border-white/30'
            : active
              ? 'bg-navy text-white border-navy'
              : 'bg-white text-ink border-line'
        return (
          <button
            key={o.value}
            type="button"
            onClick={() => onChange(o.value)}
            className={`flex-1 rounded-full border px-4 py-2.5 text-sm font-semibold transition active:scale-95 ${cls}`}
          >
            {o.label}
          </button>
        )
      })}
    </div>
  )
}
