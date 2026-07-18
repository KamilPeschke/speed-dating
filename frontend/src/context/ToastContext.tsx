import { createContext, useCallback, useContext, useState, type ReactNode } from 'react'

type ToastKind = 'error' | 'ok'

interface Toast {
  id: number
  msg: string
  kind: ToastKind
}

type ShowToast = (msg: string, kind?: ToastKind) => void

const ToastContext = createContext<ShowToast>(() => {})

export const useToast = () => useContext(ToastContext)

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])

  const show = useCallback<ShowToast>((msg, kind = 'error') => {
    const id = Date.now() + Math.random()
    setToasts((t) => [...t, { id, msg, kind }])
    setTimeout(() => setToasts((t) => t.filter((x) => x.id !== id)), 3500)
  }, [])

  return (
    <ToastContext.Provider value={show}>
      {children}
      <div className="pointer-events-none fixed inset-x-0 bottom-24 z-[3000] flex flex-col items-center gap-2 px-6">
        {toasts.map((t) => (
          <div
            key={t.id}
            className={`max-w-full rounded-full px-4 py-2.5 text-center text-sm font-medium text-white shadow-xl ${
              t.kind === 'error' ? 'bg-neutral-900/95' : 'bg-emerald-600/95'
            }`}
          >
            {t.msg}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}
