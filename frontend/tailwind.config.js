/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#08111f',
        panel: '#101b2d',
        line: '#263247',
        mint: '#39d98a',
        danger: '#ff5c75',
        gold: '#f5c451'
      },
      boxShadow: {
        glow: '0 20px 70px rgba(57, 217, 138, 0.12)'
      }
    }
  },
  plugins: []
};
