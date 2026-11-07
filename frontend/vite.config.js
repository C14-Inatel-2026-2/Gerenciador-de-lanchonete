import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config' // trocando de vite para vitest/config para que reconheça as configurações de teste do vitest também

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],

  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/setupTests.js',

    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'], // definição dos formatos do relatório de cobertura de teste
      include: ['src/**/*.{js,jsx}'], // definição dos arquivos que devem ser incluídos na cobertura de teste
      exclude: ['src/**/*.test.{js,jsx}', 'src/setupTests.js'], // definição dos arquivos que devem ser excluídos da cobertura de teste
    },
  },
})