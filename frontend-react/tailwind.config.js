/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        municipal: {
          900: '#0B1E33', // Deep Navy
          800: '#0F2942', // Primary Brand Navy
          700: '#1A3E66',
          600: '#26548A',
          500: '#346DAE',
          100: '#E8F1FA',
          50: '#F0F6FC'
        },
        civicTeal: {
          700: '#0F766E',
          600: '#0D9488', // Accent Teal
          500: '#14B8A6',
          100: '#CCFBF1',
          50: '#F0FDFA'
        }
      }
    },
  },
  plugins: [],
}
