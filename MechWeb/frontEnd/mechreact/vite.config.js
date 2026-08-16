// vite.config.js
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api/payments': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      },
      '/api/subscriptions': {
        target: 'http://localhost:8084',
        changeOrigin: true,
      },
    },
  },
});