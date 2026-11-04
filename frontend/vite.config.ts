import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config'


export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_BACKEND_PROXY_TARGET || 'http://localhost:8080',
        changeOrigin: true,
      },
    },
    watch: {
      usePolling: true,
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    css: false,
    // `npm run test:coverage` writes coverage/ (HTML report + lcov.info for SonarCloud).
    coverage: {
      provider: 'v8',
      reporter: ['text-summary', 'html', 'lcov'],
      include: ['src/**/*.{ts,tsx}'],
      exclude: ['src/**/*.test.{ts,tsx}', 'src/test/**', 'src/main.tsx'],
    },
  },
})
