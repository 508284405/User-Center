<template>
  <div class="theme-toggle">
    <!-- 简单切换按钮模式 -->
    <button
      v-if="mode === 'button'"
      @click="toggleTheme"
      class="theme-toggle-button"
      :title="`当前: ${getThemeDisplayName()}, 点击切换`"
      :aria-label="`切换主题，当前为${getThemeDisplayName()}`"
    >
      <span class="theme-icon" :class="{ 'is-dark': isDark }">
        {{ getThemeIcon() }}
      </span>
      <span v-if="showLabel" class="theme-label">
        {{ getThemeDisplayName() }}
      </span>
    </button>

    <!-- 下拉选择模式 -->
    <div v-else-if="mode === 'dropdown'" class="theme-dropdown" :class="{ 'is-open': isDropdownOpen }">
      <button 
        @click="toggleDropdown"
        class="theme-dropdown-trigger"
        :aria-expanded="isDropdownOpen"
        aria-haspopup="listbox"
        :aria-label="`主题设置，当前为${getThemeDisplayName()}`"
      >
        <span class="theme-icon" :class="{ 'is-dark': isDark }">
          {{ getThemeIcon() }}
        </span>
        <span v-if="showLabel" class="theme-label">
          {{ getThemeDisplayName() }}
        </span>
        <span class="dropdown-arrow" :class="{ 'is-open': isDropdownOpen }">▼</span>
      </button>
      
      <Transition name="dropdown">
        <ul 
          v-if="isDropdownOpen"
          class="theme-dropdown-menu"
          role="listbox"
          :aria-label="主题选择"
        >
          <li
            v-for="theme in Object.values(THEMES)"
            :key="theme"
            @click="selectTheme(theme)"
            class="theme-option"
            :class="{ 'is-active': currentTheme === theme }"
            role="option"
            :aria-selected="currentTheme === theme"
          >
            <span class="option-icon">{{ getThemeIcon(theme === THEMES.AUTO ? resolvedTheme : theme) }}</span>
            <span class="option-label">{{ getThemeDisplayName(theme) }}</span>
            <span v-if="currentTheme === theme" class="option-check">✓</span>
          </li>
        </ul>
      </Transition>
    </div>

    <!-- 紧凑按钮组模式 -->
    <div v-else-if="mode === 'compact'" class="theme-compact">
      <div class="theme-buttons" role="radiogroup" aria-label="主题选择">
        <button
          v-for="theme in Object.values(THEMES)"
          :key="theme"
          @click="setTheme(theme)"
          class="theme-compact-button"
          :class="{ 'is-active': currentTheme === theme }"
          :aria-checked="currentTheme === theme"
          role="radio"
          :title="getThemeDisplayName(theme)"
        >
          {{ getThemeIcon(theme === THEMES.AUTO ? resolvedTheme : theme) }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useTheme } from '@/composables/useTheme'

defineProps({
  mode: {
    type: String,
    default: 'button',
    validator: (value) => ['button', 'dropdown', 'compact'].includes(value)
  },
  showLabel: {
    type: Boolean,
    default: true
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  }
})

const {
  currentTheme,
  resolvedTheme,
  isDark,
  setTheme,
  toggleTheme,
  getThemeDisplayName,
  getThemeIcon,
  THEMES
} = useTheme()

// 下拉菜单状态
const isDropdownOpen = ref(false)

const toggleDropdown = () => {
  isDropdownOpen.value = !isDropdownOpen.value
}

const selectTheme = (theme) => {
  setTheme(theme)
  isDropdownOpen.value = false
}

// 点击外部关闭下拉菜单
const closeDropdown = (event) => {
  if (!event.target.closest('.theme-dropdown')) {
    isDropdownOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', closeDropdown)
})

onUnmounted(() => {
  document.removeEventListener('click', closeDropdown)
})
</script>

<style scoped>
.theme-toggle {
  position: relative;
  display: inline-block;
}

/* 增强的基础按钮样式 */
.theme-toggle-button,
.theme-dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 1rem;
  background: 
    linear-gradient(135deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0.08) 100%),
    radial-gradient(circle at 30% 30%, rgba(139, 92, 246, 0.1) 0%, transparent 50%);
  border: 2px solid rgba(255,255,255,0.25);
  border-radius: 12px;
  color: inherit;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(15px) saturate(150%);
  position: relative;
  overflow: hidden;
  user-select: none;
  box-shadow: 
    0 4px 16px rgba(0,0,0,0.08),
    0 2px 8px rgba(139, 92, 246, 0.05),
    inset 0 1px 0 rgba(255,255,255,0.3);
}

/* 按钮闪光效果 */
.theme-toggle-button::before,
.theme-dropdown-trigger::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -100%;
  width: 100%;
  height: calc(100% + 4px);
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(255,255,255,0.4) 50%, 
    transparent 100%
  );
  transition: left 0.6s ease;
  z-index: 1;
}

.theme-toggle-button:hover,
.theme-dropdown-trigger:hover {
  background: 
    linear-gradient(135deg, rgba(255,255,255,0.22) 0%, rgba(255,255,255,0.12) 100%),
    radial-gradient(circle at 30% 30%, rgba(139, 92, 246, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 70% 70%, rgba(34, 211, 238, 0.08) 0%, transparent 50%);
  border-color: rgba(255,255,255,0.4);
  transform: translateY(-2px) scale(1.02);
  box-shadow: 
    0 8px 24px rgba(0,0,0,0.12),
    0 4px 16px rgba(139, 92, 246, 0.1),
    0 2px 12px rgba(34, 211, 238, 0.08),
    inset 0 1px 0 rgba(255,255,255,0.4);
  text-shadow: 0 0 8px rgba(255,255,255,0.3);
}

.theme-toggle-button:hover::before,
.theme-dropdown-trigger:hover::before {
  left: 100%;
}

.theme-toggle-button:active,
.theme-dropdown-trigger:active {
  transform: translateY(-1px) scale(0.98);
  box-shadow: 
    0 4px 12px rgba(0,0,0,0.1),
    0 2px 8px rgba(139, 92, 246, 0.08),
    inset 0 2px 4px rgba(0,0,0,0.1);
}

/* 按钮焦点状态 */
.theme-toggle-button:focus-visible,
.theme-dropdown-trigger:focus-visible {
  outline: 2px solid rgba(139, 92, 246, 0.5);
  outline-offset: 2px;
  box-shadow: 
    0 8px 24px rgba(0,0,0,0.12),
    0 4px 16px rgba(139, 92, 246, 0.15),
    0 0 0 4px rgba(139, 92, 246, 0.1);
}

/* 增强的主题图标 */
.theme-icon {
  font-size: 1.2em;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 1.2em;
  position: relative;
  z-index: 2;
  filter: drop-shadow(0 0 4px rgba(255,255,255,0.3));
  animation: iconPulse 3s ease-in-out infinite;
}

.theme-icon.is-dark {
  transform: rotate(20deg) scale(1.05);
  filter: drop-shadow(0 0 8px rgba(139, 92, 246, 0.4));
  animation: iconPulseDark 3s ease-in-out infinite;
}

.theme-toggle-button:hover .theme-icon,
.theme-dropdown-trigger:hover .theme-icon {
  transform: rotate(15deg) scale(1.1);
  filter: drop-shadow(0 0 8px rgba(255,255,255,0.5));
}

.theme-toggle-button:hover .theme-icon.is-dark,
.theme-dropdown-trigger:hover .theme-icon.is-dark {
  transform: rotate(35deg) scale(1.15);
  filter: drop-shadow(0 0 12px rgba(139, 92, 246, 0.6));
}

/* 标签文字 */
.theme-label {
  font-weight: 600;
  white-space: nowrap;
}

/* 下拉箭头 */
.dropdown-arrow {
  font-size: 0.8em;
  transition: transform 0.25s ease;
  margin-left: 0.25rem;
}

.dropdown-arrow.is-open {
  transform: rotate(180deg);
}

/* 下拉菜单 */
.theme-dropdown {
  position: relative;
}

.theme-dropdown-menu {
  position: absolute;
  top: calc(100% + 0.5rem);
  right: 0;
  min-width: 180px;
  background: linear-gradient(135deg, rgba(255,255,255,0.95) 0%, rgba(255,255,255,0.9) 100%);
  border: 2px solid rgba(255,255,255,0.3);
  border-radius: 12px;
  backdrop-filter: blur(20px) saturate(180%);
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  padding: 0.5rem;
  margin: 0;
  list-style: none;
  z-index: 1000;
  overflow: hidden;
}

.theme-option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  margin-bottom: 0.25rem;
}

.theme-option:last-child {
  margin-bottom: 0;
}

.theme-option:hover {
  background: linear-gradient(135deg, rgba(255,255,255,0.4) 0%, rgba(255,255,255,0.2) 100%);
  transform: translateX(2px);
}

.theme-option.is-active {
  background: linear-gradient(135deg, var(--color-primary, #22D3EE) 0%, var(--color-secondary, #0EA5E9) 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(34, 211, 238, 0.3);
}

.option-icon {
  font-size: 1.1em;
  min-width: 1.2em;
  display: flex;
  align-items: center;
  justify-content: center;
}

.option-label {
  flex: 1;
  font-weight: 500;
}

.option-check {
  font-size: 0.9em;
  opacity: 0.9;
}

/* 增强的紧凑按钮组 */
.theme-compact {
  display: inline-block;
}

.theme-buttons {
  display: flex;
  background: 
    linear-gradient(135deg, rgba(255,255,255,0.12) 0%, rgba(255,255,255,0.06) 100%),
    radial-gradient(circle at center, rgba(139, 92, 246, 0.08) 0%, transparent 50%);
  border: 2px solid rgba(255,255,255,0.2);
  border-radius: 14px;
  padding: 0.35rem;
  gap: 0.3rem;
  backdrop-filter: blur(15px) saturate(150%);
  box-shadow: 
    0 4px 16px rgba(0,0,0,0.08),
    0 2px 8px rgba(139, 92, 246, 0.05),
    inset 0 1px 0 rgba(255,255,255,0.2);
}

.theme-compact-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.8rem;
  height: 2.8rem;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: inherit;
  font-size: 1.15em;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  filter: drop-shadow(0 0 4px rgba(255,255,255,0.2));
}

.theme-compact-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.theme-compact-button:hover {
  background: linear-gradient(135deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0.08) 100%);
  transform: scale(1.08);
  filter: drop-shadow(0 0 8px rgba(255,255,255,0.4));
  box-shadow: 
    0 4px 12px rgba(0,0,0,0.1),
    inset 0 1px 0 rgba(255,255,255,0.3);
}

.theme-compact-button:hover::before {
  opacity: 1;
}

.theme-compact-button.is-active {
  background: 
    linear-gradient(135deg, var(--color-primary, #22D3EE) 0%, var(--color-secondary, #0EA5E9) 100%),
    radial-gradient(circle at 30% 30%, rgba(255,255,255,0.2) 0%, transparent 50%);
  color: white;
  box-shadow: 
    0 6px 20px rgba(34, 211, 238, 0.4),
    0 3px 12px rgba(14, 165, 233, 0.3),
    inset 0 1px 0 rgba(255,255,255,0.4);
  transform: scale(1.1);
  filter: drop-shadow(0 0 12px rgba(34, 211, 238, 0.5));
}

.theme-compact-button.is-active::before {
  opacity: 1;
  background: radial-gradient(circle, rgba(255,255,255,0.2) 0%, transparent 70%);
}

/* 下拉动画 */
.dropdown-enter-active {
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.dropdown-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 1, 1);
}

.dropdown-enter-from {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-5px) scale(0.98);
}

/* 暗色主题适配 */
:global(.dark-theme) .theme-toggle-button,
:global(.dark-theme) .theme-dropdown-trigger {
  background: linear-gradient(135deg, rgba(255,255,255,0.05) 0%, rgba(255,255,255,0.02) 100%);
  border-color: rgba(255,255,255,0.1);
  color: rgba(255,255,255,0.9);
}

:global(.dark-theme) .theme-toggle-button:hover,
:global(.dark-theme) .theme-dropdown-trigger:hover {
  background: linear-gradient(135deg, rgba(255,255,255,0.08) 0%, rgba(255,255,255,0.04) 100%);
  border-color: rgba(255,255,255,0.2);
}

:global(.dark-theme) .theme-dropdown-menu {
  background: linear-gradient(135deg, rgba(15,15,15,0.95) 0%, rgba(10,10,10,0.9) 100%);
  border-color: rgba(255,255,255,0.1);
}

:global(.dark-theme) .theme-option:hover {
  background: linear-gradient(135deg, rgba(255,255,255,0.08) 0%, rgba(255,255,255,0.04) 100%);
}

:global(.dark-theme) .theme-buttons {
  background: linear-gradient(135deg, rgba(255,255,255,0.04) 0%, rgba(255,255,255,0.02) 100%);
  border-color: rgba(255,255,255,0.08);
}

:global(.dark-theme) .theme-compact-button:hover {
  background: rgba(255,255,255,0.06);
}

/* 尺寸变体 */
.theme-toggle[data-size="small"] .theme-toggle-button,
.theme-toggle[data-size="small"] .theme-dropdown-trigger {
  padding: 0.375rem 0.5rem;
  font-size: 0.8rem;
}

.theme-toggle[data-size="small"] .theme-icon {
  font-size: 1em;
}

.theme-toggle[data-size="large"] .theme-toggle-button,
.theme-toggle[data-size="large"] .theme-dropdown-trigger {
  padding: 0.75rem 1rem;
  font-size: 1rem;
}

.theme-toggle[data-size="large"] .theme-icon {
  font-size: 1.4em;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .theme-dropdown-menu {
    right: -0.5rem;
    left: -0.5rem;
    min-width: auto;
  }
  
  .theme-label {
    display: none;
  }
}

/* 无障碍优化 */
@media (prefers-reduced-motion: reduce) {
  .theme-toggle * {
    transition: none !important;
    animation: none !important;
  }
  
  .theme-icon.is-dark {
    transform: none;
  }
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .theme-toggle-button,
  .theme-dropdown-trigger {
    border-width: 3px;
    border-color: currentColor;
    background: transparent;
  }
  
  .theme-dropdown-menu {
    border-width: 3px;
    border-color: currentColor;
    background: var(--color-surface, #ffffff);
  }
}

/* 新增动画 */
@keyframes iconPulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.9;
  }
  50% {
    transform: scale(1.02);
    opacity: 1;
  }
}

@keyframes iconPulseDark {
  0%, 100% {
    transform: rotate(20deg) scale(1.05);
    opacity: 0.9;
    filter: drop-shadow(0 0 8px rgba(139, 92, 246, 0.4));
  }
  50% {
    transform: rotate(20deg) scale(1.08);
    opacity: 1;
    filter: drop-shadow(0 0 12px rgba(139, 92, 246, 0.6));
  }
}
</style>