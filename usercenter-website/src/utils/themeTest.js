// Theme Toggle Test Utility
// This utility can be used to test if the theme toggle is working correctly

import { useTheme } from '@/composables/useTheme'

export function runThemeTest() {
  console.log('🧪 Running theme toggle tests...')
  
  const { 
    currentTheme, 
    resolvedTheme, 
    isDark, 
    setTheme, 
    toggleTheme, 
    THEMES 
  } = useTheme()
  
  const tests = []
  
  // Test 1: Check if theme system is initialized
  tests.push({
    name: 'Theme System Initialization',
    test: () => {
      const hasCurrentTheme = currentTheme.value !== null
      const hasResolvedTheme = resolvedTheme.value !== null
      const hasDarkState = typeof isDark.value === 'boolean'
      return hasCurrentTheme && hasResolvedTheme && hasDarkState
    }
  })
  
  // Test 2: Check if DOM has theme classes
  tests.push({
    name: 'DOM Theme Classes',
    test: () => {
      const root = document.documentElement
      return root.classList.contains('light-theme') || root.classList.contains('dark-theme')
    }
  })
  
  // Test 3: Check if localStorage persistence works
  tests.push({
    name: 'LocalStorage Persistence',
    test: () => {
      const originalTheme = currentTheme.value
      setTheme(THEMES.DARK)
      const stored = localStorage.getItem('user-center-theme')
      setTheme(originalTheme) // restore
      return stored === THEMES.DARK
    }
  })
  
  // Test 4: Check if toggle function works
  tests.push({
    name: 'Theme Toggle Function',
    test: () => {
      const originalTheme = currentTheme.value
      toggleTheme()
      const changedTheme = currentTheme.value
      toggleTheme() // restore
      return originalTheme !== changedTheme
    }
  })
  
  // Test 5: Check if CSS variables are applied
  tests.push({
    name: 'CSS Variables Application',
    test: () => {
      const root = document.documentElement
      const primaryColor = getComputedStyle(root).getPropertyValue('--color-primary')
      return primaryColor && primaryColor.trim() !== ''
    }
  })
  
  // Run all tests
  const results = tests.map(test => {
    try {
      const passed = test.test()
      return { name: test.name, passed, error: null }
    } catch (error) {
      return { name: test.name, passed: false, error: error.message }
    }
  })
  
  // Log results
  console.log('🧪 Theme Test Results:')
  results.forEach(result => {
    const status = result.passed ? '✅' : '❌'
    const error = result.error ? ` (Error: ${result.error})` : ''
    console.log(`${status} ${result.name}${error}`)
  })
  
  const passedCount = results.filter(r => r.passed).length
  const totalCount = results.length
  console.log(`🧪 Test Summary: ${passedCount}/${totalCount} tests passed`)
  
  // Log current theme state
  console.log('🎨 Current Theme State:')
  console.log(`  Current Theme: ${currentTheme.value}`)
  console.log(`  Resolved Theme: ${resolvedTheme.value}`)
  console.log(`  Is Dark: ${isDark.value}`)
  console.log(`  DOM Classes: ${document.documentElement.className}`)
  console.log(`  Data Theme: ${document.documentElement.getAttribute('data-theme')}`)
  
  return results
}

// Auto-run test in development mode
if (process.env.NODE_ENV === 'development') {
  // Run test after DOM is ready
  document.addEventListener('DOMContentLoaded', () => {
    setTimeout(() => {
      runThemeTest()
    }, 1000) // Wait 1 second to ensure theme system is initialized
  })
}