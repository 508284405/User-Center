import { ref, computed, watchEffect, onMounted } from 'vue'

// 主题类型
export const THEMES = {
  LIGHT: 'light',
  DARK: 'dark',
  AUTO: 'auto'
}

// 主题状态
const currentTheme = ref(THEMES.AUTO)
const systemTheme = ref(THEMES.LIGHT)

// 从 localStorage 获取保存的主题
const getStoredTheme = () => {
  try {
    const stored = localStorage.getItem('user-center-theme')
    return stored && Object.values(THEMES).includes(stored) ? stored : THEMES.AUTO
  } catch {
    return THEMES.AUTO
  }
}

// 保存主题到 localStorage
const saveTheme = (theme) => {
  try {
    localStorage.setItem('user-center-theme', theme)
  } catch {
    // 静默失败
  }
}

// 检测系统主题偏好
const detectSystemTheme = () => {
  if (typeof window === 'undefined') return THEMES.LIGHT
  
  try {
    return window.matchMedia('(prefers-color-scheme: dark)').matches 
      ? THEMES.DARK 
      : THEMES.LIGHT
  } catch {
    return THEMES.LIGHT
  }
}

// 监听系统主题变化
const setupSystemThemeListener = () => {
  if (typeof window === 'undefined') return

  try {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    
    const handleChange = (e) => {
      systemTheme.value = e.matches ? THEMES.DARK : THEMES.LIGHT
    }
    
    // 现代浏览器使用 addEventListener
    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', handleChange)
    } else {
      // 旧版浏览器兼容
      mediaQuery.addListener(handleChange)
    }
    
    return () => {
      if (mediaQuery.removeEventListener) {
        mediaQuery.removeEventListener('change', handleChange)
      } else {
        mediaQuery.removeListener(handleChange)
      }
    }
  } catch {
    return () => {}
  }
}

// 实际生效的主题（解析 auto 模式）
const resolvedTheme = computed(() => {
  if (currentTheme.value === THEMES.AUTO) {
    return systemTheme.value
  }
  return currentTheme.value
})

// 是否为暗色主题
const isDark = computed(() => resolvedTheme.value === THEMES.DARK)

// 应用主题到 DOM
const applyTheme = (theme) => {
  if (typeof document === 'undefined') return
  
  const root = document.documentElement
  
  // 移除所有主题类
  root.classList.remove('light-theme', 'dark-theme')
  
  // 添加当前主题类
  if (theme === THEMES.DARK) {
    root.classList.add('dark-theme')
    root.setAttribute('data-theme', 'dark')
  } else {
    root.classList.add('light-theme')  
    root.setAttribute('data-theme', 'light')
  }
}

export function useTheme() {
  // 切换到指定主题
  const setTheme = (theme) => {
    if (!Object.values(THEMES).includes(theme)) {
      console.warn(`Invalid theme: ${theme}`)
      return
    }
    
    currentTheme.value = theme
    saveTheme(theme)
  }

  // 切换主题（在 light/dark 间循环，不包括 auto）
  const toggleTheme = () => {
    if (currentTheme.value === THEMES.AUTO) {
      // 从 auto 切换到与当前系统主题相反的主题
      setTheme(systemTheme.value === THEMES.DARK ? THEMES.LIGHT : THEMES.DARK)
    } else if (currentTheme.value === THEMES.LIGHT) {
      setTheme(THEMES.DARK)
    } else {
      setTheme(THEMES.LIGHT)
    }
  }

  // 重置为自动模式
  const resetToAuto = () => {
    setTheme(THEMES.AUTO)
  }

  // 获取主题显示名称
  const getThemeDisplayName = (theme = currentTheme.value) => {
    const names = {
      [THEMES.LIGHT]: '浅色',
      [THEMES.DARK]: '深色',
      [THEMES.AUTO]: '跟随系统'
    }
    return names[theme] || '未知'
  }

  // 获取主题图标
  const getThemeIcon = (theme = resolvedTheme.value) => {
    const icons = {
      [THEMES.LIGHT]: '☀️',
      [THEMES.DARK]: '🌙'
    }
    return icons[theme] || '🌓'
  }

  // 初始化主题系统
  const initializeTheme = () => {
    // 检测系统主题
    systemTheme.value = detectSystemTheme()
    
    // 加载保存的主题偏好
    currentTheme.value = getStoredTheme()
    
    // 立即应用主题
    applyTheme(resolvedTheme.value)
    
    // 监听系统主题变化
    const cleanup = setupSystemThemeListener()
    
    // 监听主题变化并应用
    const stopWatcher = watchEffect(() => {
      applyTheme(resolvedTheme.value)
    })
    
    // 返回清理函数
    return () => {
      cleanup()
      stopWatcher()
    }
  }

  return {
    // 状态
    currentTheme: computed(() => currentTheme.value),
    systemTheme: computed(() => systemTheme.value),
    resolvedTheme,
    isDark,
    
    // 方法
    setTheme,
    toggleTheme,
    resetToAuto,
    getThemeDisplayName,
    getThemeIcon,
    initializeTheme,
    
    // 常量
    THEMES
  }
}

// 全局主题状态（单例模式）
let globalThemeCleanup = null

export const initGlobalTheme = () => {
  if (globalThemeCleanup) {
    globalThemeCleanup()
  }
  
  const { initializeTheme } = useTheme()
  globalThemeCleanup = initializeTheme()
  
  return globalThemeCleanup
}

// 在应用卸载时清理
export const cleanupGlobalTheme = () => {
  if (globalThemeCleanup) {
    globalThemeCleanup()
    globalThemeCleanup = null
  }
}