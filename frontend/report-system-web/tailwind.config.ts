import type { Config } from 'tailwindcss'

export default <Config>{
  content: [
    './app/components/**/*.{vue,ts}',
    './app/layouts/**/*.vue',
    './app/pages/**/*.vue',
    './app/composables/**/*.ts',
    './app/directives/**/*.ts',
    './app/plugins/**/*.{ts,js}',
    './app/app.vue',
    './app/error.vue'
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#1890ff',
          hover: '#40a9ff',
          active: '#096dd9',
          light: '#e6f7ff',
          border: '#91d5ff',
          text: '#1890ff'
        },
        success: {
          DEFAULT: '#52c41a',
          hover: '#73d13d',
          active: '#389e0d',
          light: '#f6ffed',
          border: '#b7eb8f'
        },
        warning: {
          DEFAULT: '#faad14',
          hover: '#ffc53d',
          active: '#d48806',
          light: '#fffbe6',
          border: '#ffe58f'
        },
        danger: {
          DEFAULT: '#ff4d4f',
          hover: '#ff7875',
          active: '#d4380d',
          light: '#fff1f0',
          border: '#ffa39e'
        },
        info: {
          DEFAULT: '#909399',
          hover: '#a6a9ad',
          active: '#73767a',
          light: '#f4f4f5',
          border: '#d3d4d6'
        },
        sidebar: {
          DEFAULT: '#001529',
          hover: '#000c17',
          active: '#1890ff',
          text: 'rgba(255, 255, 255, 0.65)',
          textActive: '#ffffff',
          textHover: '#ffffff',
          border: 'rgba(255, 255, 255, 0.1)'
        },
        body: '#f0f2f5',
        border: '#e8eaec',
        text: {
          primary: 'rgba(0, 0, 0, 0.85)',
          secondary: 'rgba(0, 0, 0, 0.65)',
          disabled: 'rgba(0, 0, 0, 0.45)',
          heading: 'rgba(0, 0, 0, 0.85)'
        }
      },
      fontFamily: {
        sans: [
          '-apple-system',
          'BlinkMacSystemFont',
          'Segoe UI',
          'PingFang SC',
          'Hiragino Sans GB',
          'Microsoft YaHei',
          'Helvetica Neue',
          'Helvetica',
          'Arial',
          'sans-serif'
        ]
      },
      fontSize: {
        xs: ['12px', '20px'],
        sm: ['13px', '22px'],
        base: ['14px', '22px'],
        md: ['14px', '22px'],
        lg: ['16px', '24px'],
        xl: ['18px', '26px'],
        '2xl': ['20px', '28px'],
        '3xl': ['24px', '32px']
      },
      spacing: {
        sidebar: '220px',
        sidebarCollapsed: '60px',
        header: '56px'
      },
      boxShadow: {
        card: '0 1px 2px 0 rgba(0, 0, 0, 0.03), 0 1px 6px -1px rgba(0, 0, 0, 0.02), 0 2px 4px 0 rgba(0, 0, 0, 0.02)',
        drawer: '-4px 0 12px 0 rgba(0, 0, 0, 0.08)',
        popover: '0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 3px 6px -4px rgba(0, 0, 0, 0.12), 0 9px 28px 8px rgba(0, 0, 0, 0.05)'
      },
      borderRadius: {
        sm: '2px',
        DEFAULT: '4px',
        md: '6px',
        lg: '8px'
      },
      transitionDuration: {
        DEFAULT: '200ms'
      }
    }
  }
}
