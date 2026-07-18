const SIZES = {
  xs: 'h-[28px] w-[28px] text-xs',
  sm: 'h-[34px] w-[34px] text-sm',
  md: 'h-[50px] w-[50px] text-lg',
  lg: 'h-14 w-14 text-xl',
  xl: 'h-[110px] w-[110px] text-5xl',
} as const

interface AvatarProps {
  name: string
  photo?: string | null
  size?: keyof typeof SIZES
}

/** Awatar jak w starym FE: zdjęcie albo granatowe kółko z pierwszą literą imienia. */
export function Avatar({ name, photo, size = 'md' }: AvatarProps) {
  if (photo) {
    return <img src={photo} alt={name} className={`${SIZES[size]} shrink-0 rounded-full bg-line object-cover`} />
  }
  return (
    <div className={`${SIZES[size]} grid shrink-0 place-items-center rounded-full bg-navy font-bold text-white`}>
      {name?.charAt(0).toUpperCase() || '?'}
    </div>
  )
}
