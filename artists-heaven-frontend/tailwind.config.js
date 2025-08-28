module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      screens: {
        '3xl': '1921px', 
      },
      dropShadow: {
        'strong': '0 6px 8px rgba(0, 0, 0, 0.4)',
      },
    },
  },
  plugins: [],
}