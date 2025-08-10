/* eslint-disable no-undef */
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';
import nodePolyfills from 'rollup-plugin-node-polyfills';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    global: 'globalThis',
  },
  resolve: {
    alias: {
      process: 'process/browser',
      buffer: 'buffer',
      '@': path.resolve(__dirname, 'src'), // @ → src 폴더
    },
  },
  optimizeDeps: {
    include: ['@stomp/stompjs', 'sockjs-client'],
  },
});
