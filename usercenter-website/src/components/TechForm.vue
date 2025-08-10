<script setup>
import { ref, computed } from 'vue'
import { RouterLink } from 'vue-router'

const props = defineProps({
  type: {
    type: String,
    default: 'login',
    validator: (value) => ['login', 'register'].includes(value)
  },
  theme: {
    type: String,
    default: 'teal',
    validator: (value) => ['blue', 'purple', 'pink', 'teal', 'cyber-blue'].includes(value)
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit', 'google-login'])

// 表单数据
const formData = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  rememberMe: false,
  agreeTerms: false
})

// 表单焦点状态
const focusStates = ref({
  username: false,
  email: false,
  password: false,
  confirmPassword: false
})

// 输入框交互效果
const handleFocus = (field) => {
  focusStates.value[field] = true
}

const handleBlur = (field) => {
  focusStates.value[field] = false
}

// 主题色表
const builtInThemes = {
  blue: { primary: '#3B82F6', secondary: '#1D4ED8', glow: 'rgba(59,130,246,0.4)', accent: '#60A5FA' },
  purple: { primary: '#8B5CF6', secondary: '#7C3AED', glow: 'rgba(139,92,246,0.4)', accent: '#A78BFA' },
  pink: { primary: '#EC4899', secondary: '#DB2777', glow: 'rgba(236,72,153,0.4)', accent: '#F472B6' },
  teal: { primary: '#22D3EE', secondary: '#0EA5E9', glow: 'rgba(34,211,238,0.4)', accent: '#67E8F9' },
  'cyber-blue': { primary: '#06B6D4', secondary: '#0A4D8C', glow: 'rgba(56,189,248,0.35)', accent: '#38BDF8' }
}

// 优先读取 .auth-page 的 CSS 变量，否则回退到内置主题
const resolveCssVar = (varName, fallback) => {
  try {
    const el = document.querySelector('.auth-page') || document.documentElement
    const value = getComputedStyle(el).getPropertyValue(varName).trim()
    return value || fallback
  } catch (_) {
    return fallback
  }
}

// 按钮样式计算 - 优先使用CSS变量
const buttonTheme = computed(() => {
  const builtIn = builtInThemes[props.theme] || builtInThemes.teal
  return {
    primary: resolveCssVar('--color-primary', builtIn.primary),
    secondary: resolveCssVar('--color-secondary', builtIn.secondary),
    accent: resolveCssVar('--color-accent', builtIn.accent),
    glow: resolveCssVar('--color-glow', builtIn.glow),
    glowStrong: resolveCssVar('--color-glow-strong', builtIn.glow),
    surface: resolveCssVar('--color-surface', 'rgba(255,255,255,0.06)'),
    border: resolveCssVar('--color-border', 'rgba(255,255,255,0.18)'),
    borderHover: resolveCssVar('--color-border-hover', 'rgba(255,255,255,0.3)')
  }
})

// 输入框样式计算
const getInputStyle = (field) => {
  const theme = buttonTheme.value
  return focusStates.value[field] ? {
    borderColor: theme.primary,
    boxShadow: `0 0 12px ${theme.glow}, 0 0 4px ${theme.glowStrong}`,
    backgroundColor: theme.surface
  } : {
    borderColor: theme.border,
    backgroundColor: 'transparent'
  }
}

// 简单前端校验与错误状态
const errors = ref({ username: '', email: '', password: '', confirmPassword: '' })

const validate = () => {
  errors.value = { username: '', email: '', password: '', confirmPassword: '' }
  let ok = true
  if (!formData.value.username || formData.value.username.trim().length < 3) {
    errors.value.username = '用户名至少 3 个字符'
    ok = false
  }
  if (props.type === 'register') {
    const email = formData.value.email
    const emailOk = /.+@+.\..+/.test(email || '')
    if (!emailOk) {
      errors.value.email = '请输入有效的邮箱地址'
      ok = false
    }
  }
  const pwd = formData.value.password || ''
  if (pwd.length < 6) {
    errors.value.password = '密码至少 6 个字符'
    ok = false
  }
  if (props.type === 'register') {
    if (!/[A-Z]/.test(pwd) || !/[0-9]/.test(pwd) || !/[!@#$%^&*]/.test(pwd)) {
      errors.value.password = '需包含大写字母、数字和特殊字符'
      ok = false
    }
    if (formData.value.confirmPassword !== pwd) {
      errors.value.confirmPassword = '两次输入的密码不一致'
      ok = false
    }
  }
  return ok
}

const onSubmit = () => {
  if (!validate()) return
  emit('submit', formData.value)
}

const onGoogleLogin = () => {
  emit('google-login')
}
// 为可访问性生成 id 前缀
const idPrefix = computed(() => (props.type === 'register' ? 'register' : 'login'))

</script>

<template>
  <div class="tech-form">
    <h1 class="form-title">
      {{ type === 'login' ? '欢迎回来' : '创建账号' }}
    </h1>

    <div class="input-group">
      <div class="tech-input-wrapper">
        <label class="sr-only" :for="`${idPrefix}-username`">用户名</label>
        <input
          :id="`${idPrefix}-username`"
          v-model="formData.username"
          type="text"
          placeholder="用户名/邮箱/手机号"
          class="tech-input"
          :style="getInputStyle('username')"
          aria-label="用户名"
          autocomplete="username"
          @focus="handleFocus('username')"
          @blur="handleBlur('username')"
        >
        <div class="input-glow" :class="{ active: focusStates.username }"></div>
        <p v-if="errors.username" class="field-error" role="alert">{{ errors.username }}</p>
      </div>

      <div v-if="type === 'register'" class="tech-input-wrapper">
        <label class="sr-only" :for="`${idPrefix}-email`">邮箱地址</label>
        <input
          :id="`${idPrefix}-email`"
          v-model="formData.email"
          type="email"
          placeholder="邮箱地址"
          class="tech-input"
          :style="getInputStyle('email')"
          aria-label="邮箱地址"
          autocomplete="email"
          @focus="handleFocus('email')"
          @blur="handleBlur('email')"
        >
        <div class="input-glow" :class="{ active: focusStates.email }"></div>
        <p v-if="errors.email" class="field-error" role="alert">{{ errors.email }}</p>
      </div>

      <div class="tech-input-wrapper">
        <label class="sr-only" :for="`${idPrefix}-password`">密码</label>
        <input
          :id="`${idPrefix}-password`"
          v-model="formData.password"
          type="password"
          placeholder="密码"
          class="tech-input"
          :style="getInputStyle('password')"
          aria-label="密码"
          :autocomplete="type === 'register' ? 'new-password' : 'current-password'"
          @focus="handleFocus('password')"
          @blur="handleBlur('password')"
        >
        <div class="input-glow" :class="{ active: focusStates.password }"></div>
        <p v-if="errors.password" class="field-error" role="alert">{{ errors.password }}</p>
      </div>

      <div v-if="type === 'register'" class="tech-input-wrapper">
        <label class="sr-only" :for="`${idPrefix}-confirm`">确认密码</label>
        <input
          :id="`${idPrefix}-confirm`"
          v-model="formData.confirmPassword"
          type="password"
          placeholder="确认密码"
          class="tech-input"
          :style="getInputStyle('confirmPassword')"
          aria-label="确认密码"
          autocomplete="new-password"
          @focus="handleFocus('confirmPassword')"
          @blur="handleBlur('confirmPassword')"
        >
        <div class="input-glow" :class="{ active: focusStates.confirmPassword }"></div>
        <p v-if="errors.confirmPassword" class="field-error" role="alert">{{ errors.confirmPassword }}</p>
      </div>
    </div>

    <div class="form-options" v-if="type === 'login'">
      <label class="cyber-checkbox">
        <input v-model="formData.rememberMe" type="checkbox">
        <span class="checkmark"></span>
        <span class="label-text">记住我</span>
      </label>
      <a href="#" class="forgot-link">忘记密码？</a>
    </div>

    <div class="form-options" v-if="type === 'register'">
      <label class="cyber-checkbox">
        <input v-model="formData.agreeTerms" type="checkbox" required>
        <span class="checkmark"></span>
        <span class="label-text">我同意服务条款</span>
      </label>
    </div>

    <button 
      class="energy-button primary-btn"
      :style="{ '--primary-color': buttonTheme.primary, '--glow-color': buttonTheme.glow }"
      :disabled="props.loading"
      :aria-busy="props.loading ? 'true' : 'false'"
      @click="onSubmit"
    >
      <span class="btn-content">
        {{ props.loading ? '处理中…' : (type === 'login' ? '登录' : '注册') }}
      </span>
      <div class="energy-fill"></div>
    </button>

    <div v-if="type === 'login'" class="social-section">
      <div class="divider">
        <span>或使用其他方式</span>
      </div>
      <button 
        class="energy-button google-btn"
        @click="onGoogleLogin"
      >
        <span class="btn-content">
          <img src="https://developers.google.com/identity/images/g-logo.png" alt="Google" class="google-icon">
          Google 登录
        </span>
        <div class="energy-fill"></div>
      </button>
    </div>

    <div class="switch-form">
      <span v-if="type === 'login'">还没有账号？</span>
      <span v-else>已有账号？</span>
      <RouterLink 
        :to="type === 'login' ? '/register' : '/login'" 
        class="switch-link"
        :style="{ color: buttonTheme.accent || buttonTheme.primary }"
      >
        {{ type === 'login' ? '立即注册' : '立即登录' }}
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
.tech-form {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 1rem;
}

.form-title {
  text-align: center;
  color: #1a1a1a;
  font-size: 1.5rem;
  font-weight: 800;
  margin-bottom: 1.5rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  background: none;
  -webkit-text-fill-color: initial;
}

.input-group {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  margin-bottom: 1rem;
}

.tech-input-wrapper {
  position: relative;
  width: 100%;
}

.tech-input {
  width: 100%;
  padding: 0.8rem 1rem;
  background: linear-gradient(135deg, rgba(255,255,255,0.25) 0%, rgba(255,255,255,0.15) 100%);
  border: 2px solid rgba(255,255,255,0.4);
  border-radius: var(--radius-sm, 8px);
  color: #1a1a1a;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(var(--blur-medium, 15px)) saturate(180%);
  outline: none;
  position: relative;
  z-index: 2;
  box-sizing: border-box;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1), inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.tech-input:hover {
  border-color: rgba(255,255,255,0.6);
  background: linear-gradient(135deg, rgba(255,255,255,0.35) 0%, rgba(255,255,255,0.25) 100%);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15), inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.tech-input:focus {
  transform: translateY(-2px);
  background: linear-gradient(135deg, rgba(255,255,255,0.4) 0%, rgba(255,255,255,0.3) 100%);
  border-color: var(--color-primary, #22D3EE);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2), 0 0 0 3px rgba(34, 211, 238, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.tech-input::placeholder {
  color: rgba(26, 26, 26, 0.8);
  font-weight: 600;
  transition: color 0.3s ease;
}

.tech-input:focus::placeholder {
  color: rgba(26, 26, 26, 0.6);
  transform: translateY(-2px);
}

.input-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 8px;
  opacity: 0;
  background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: opacity 0.3s ease;
  z-index: 1;
}

.input-glow.active {
  opacity: 1;
  animation: pulse-glow 2s infinite;
}

@keyframes pulse-glow {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(1.02); opacity: 0.6; }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin: 0.5rem 0;
  flex-wrap: wrap;
  gap: 0.5rem;
  font-size: 0.8rem;
}

.cyber-checkbox {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #1a1a1a;
  font-weight: 600;
  transition: color 0.3s ease;
}

.cyber-checkbox:hover {
  color: #0d0d0d;
}

.cyber-checkbox input {
  display: none;
}

.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid var(--color-border, rgba(255, 255, 255, 0.5));
  border-radius: 4px;
  margin-right: 0.5rem;
  position: relative;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.05);
}

.checkmark:hover {
  border-color: var(--color-border-hover, rgba(255, 255, 255, 0.7));
  background: rgba(255, 255, 255, 0.08);
  transform: scale(1.1);
}

.cyber-checkbox input:checked + .checkmark {
  background: var(--color-primary, #22D3EE);
  border-color: var(--color-primary, #22D3EE);
  box-shadow: 0 0 8px var(--color-glow, rgba(34, 211, 238, 0.4));
}

.cyber-checkbox input:checked + .checkmark::after {
  content: '';
  position: absolute;
  left: 4px;
  top: 1px;
  width: 3px;
  height: 6px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.forgot-link {
  color: #4a5568;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
  padding: 0.2rem 0.4rem;
  border-radius: var(--radius-sm, 4px);
}

.forgot-link:hover {
  color: var(--color-primary, #22D3EE);
  text-shadow: 0 0 8px rgba(34, 211, 238, 0.6);
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
}

.energy-button {
  position: relative;
  width: 100%;
  padding: 0.8rem 1.5rem;
  background: linear-gradient(135deg, rgba(34, 211, 238, 0.1) 0%, rgba(34, 211, 238, 0.05) 100%);
  border: 2px solid var(--color-primary, #22D3EE);
  border-radius: var(--radius-sm, 8px);
  color: #1a1a1a;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 0.8rem;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.energy-button[disabled] {
  opacity: 0.6;
  cursor: not-allowed;
  filter: grayscale(0.2);
}

.field-error {
  margin: 6px 2px 0;
  color: #ef4444;
  font-size: 12px;
  font-weight: 600;
  text-shadow: 0 0 8px rgba(239, 68, 68, 0.3);
}

.btn-content {
  position: relative;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-weight: 600;
  text-shadow: 0 0 3px rgba(0, 0, 0, 0.5);
}

.energy-fill {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent, 
    var(--color-primary, #22D3EE), 
    transparent);
  transition: left 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;
  opacity: 0.3;
}

.energy-button:hover {
  box-shadow: var(--shadow-glow, 0 0 20px var(--color-glow, rgba(34, 211, 238, 0.4))), 0 8px 25px rgba(0, 0, 0, 0.15);
  transform: translateY(-3px) scale(1.02);
  border-color: var(--color-accent, #67E8F9);
  background: linear-gradient(135deg, var(--color-surface, rgba(255,255,255,0.08)) 0%, rgba(255,255,255,0.12) 100%);
}

.energy-button:active {
  transform: translateY(-1px) scale(1.01);
  box-shadow: var(--shadow-glow, 0 0 15px var(--color-glow, rgba(34, 211, 238, 0.4))), 0 4px 12px rgba(0, 0, 0, 0.1);
}

.energy-button:hover .energy-fill {
  left: 100%;
}

.google-btn {
  --primary-color: #4285f4;
  --glow-color: color-mix(in srgb, var(--color-glow, rgba(66,133,244,0.35)) 60%, rgba(66,133,244,0.35));
}

.google-icon {
  width: 16px;
  height: 16px;
}

.social-section {
  width: 100%;
  margin: 1rem 0;
}

.divider {
  text-align: center;
  margin: 1rem 0;
  position: relative;
  color: #4a5568;
  font-size: 0.8rem;
  font-weight: 600;
}

.divider::before,
.divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 35%;
  height: 1px;
  background: linear-gradient(to right, transparent, rgba(26, 26, 26, 0.2), transparent);
}

.divider::before {
  left: 0;
}

.divider::after {
  right: 0;
}

.switch-form {
  text-align: center;
  color: #4a5568;
  font-size: 0.9rem;
  font-weight: 600;
  margin-top: 0.5rem;
}

.switch-link {
  text-decoration: none;
  font-weight: 500;
  margin-left: 0.3rem;
  transition: all 0.3s ease;
}

.switch-link:hover {
  text-shadow: 0 0 8px currentColor;
  transform: translateY(-1px);
  filter: brightness(1.2);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .form-title {
    font-size: 1.3rem;
  }
  
  .form-options {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.3rem;
  }
  
  .tech-input {
    padding: 0.7rem 0.8rem;
    font-size: 0.85rem;
  }
  
  .energy-button {
    padding: 0.7rem 1.2rem;
    font-size: 0.85rem;
  }
}

@media (max-width: 480px) {
  .form-title {
    font-size: 1.2rem;
  }
  
  .tech-input {
    padding: 0.6rem 0.7rem;
  }
  
  .energy-button {
    padding: 0.6rem 1rem;
  }
}
</style> 
