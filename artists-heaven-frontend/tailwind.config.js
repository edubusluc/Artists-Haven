module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      screens: {
        '3xl': '1921px',
        // breakpoint personalizado para el rango 503px → 639px
        'custom-range': { 'min': '503px', 'max': '639px' },
      },
      dropShadow: {
        'strong': '0 6px 8px rgba(0, 0, 0, 0.4)',
      },
    },
  },
  plugins: [
    function ({ addUtilities }) {
      addUtilities({
        '.grayscale-50': {
          filter: 'grayscale(0.5)',
        },
        '.grayscale-75': {
          filter: 'grayscale(0.75)',
        },
        '.scrollbar-hide': {
          '&::-webkit-scrollbar': {
            display: 'none',
          },
          '-ms-overflow-style': 'none',
          'scrollbar-width': 'none',
        },
      })
    },
  ],
}
