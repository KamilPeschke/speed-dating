import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// Port 8082 — zgodny z CORS w SpringConfig backendu.
// Proxy /api oraz /ws -> backend :3010, dzięki czemu w dev nie ma w ogóle tematu CORS.
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 8082,
    proxy: {
      '/api': { target: 'http://localhost:3010', changeOrigin: true },
      '/ws': { target: 'http://localhost:3010', ws: true },
    },
  },
})
