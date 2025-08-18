<script setup>
import { ref, computed } from 'vue'
import { RouterLink } from 'vue-router'

const props = defineProps({
  type: {
    type: String,
    default: 'login',
    validator: (value) => ['login', 'register'].includes(value)
  },
  loading: {
    type: Boolean,
    default: false
  },
  disabled: {
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

// 表单焦点状态管理
const focusStates = ref({
  username: false,
  email: false,
  password: false,
  confirmPassword: false
})

// 错误状态管理
const errors = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  terms: ''
})

// 焦点处理
const handleFocus = (field) => {
  focusStates.value[field] = true
}

const handleBlur = (field) => {
  focusStates.value[field] = false
}

// 输入字段样式计算
const getFieldClasses = (field) => ({
  'form-field': true,
  'has-focus': focusStates.value[field],
  'has-error': !!errors.value[field],
  'has-value': !!formData.value[field]
})

// 表单验证
const validate = () => {
  errors.value = { username: '', email: '', password: '', confirmPassword: '', terms: '' }
  let isValid = true

  // 用户名验证
  if (!formData.value.username?.trim() || formData.value.username.trim().length < 3) {
    errors.value.username = '用户名至少需要 3 个字符'
    isValid = false
  }

  // 注册页面额外验证
  if (props.type === 'register') {
    // 邮箱验证
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!formData.value.email?.trim() || !emailPattern.test(formData.value.email.trim())) {
      errors.value.email = '请输入有效的邮箱地址'
      isValid = false
    }

    // 服务条款验证
    if (!formData.value.agreeTerms) {
      errors.value.terms = '请同意服务条款和隐私政策'
      isValid = false
    }
  }

  // 密码验证
  const password = formData.value.password || ''
  if (password.length < 6) {
    errors.value.password = '密码至少需要 6 个字符'
    isValid = false
  }

  // 注册页面密码强度验证
  if (props.type === 'register') {
    if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(password)) {
      errors.value.password = '密码需要包含大小写字母和数字'
      isValid = false
    }

    // 确认密码验证
    if (formData.value.confirmPassword !== password) {
      errors.value.confirmPassword = '两次输入的密码不一致'
      isValid = false
    }
  }

  return isValid
}

// 表单提交
const handleSubmit = () => {
  if (props.disabled || props.loading) return
  if (!validate()) return
  
  emit('submit', { ...formData.value })
}

// Google 登录
const handleGoogleLogin = () => {
  if (props.disabled || props.loading) return
  emit('google-login')
}

// 计算属性：表单标题
const formTitle = computed(() => {
  return props.type === 'login' ? '欢迎回来' : '创建账号'
})

const submitButtonText = computed(() => {
  if (props.loading) return props.type === 'login' ? '登录中...' : '注册中...'
  return props.type === 'login' ? '登录' : '注册'
})

// ID 前缀用于无障碍访问
const idPrefix = computed(() => props.type)
</script>

<template>
  <div class="modern-form">
    <div class="form-header">
      <h1 class="form-title">{{ formTitle }}</h1>
      <p class="form-subtitle">
        {{ type === 'login' ? '登录到您的账户' : '创建新的账户' }}
      </p>
    </div>

    <form class="form-body" @submit.prevent="handleSubmit" novalidate>
      <!-- 用户名字段 -->
      <div :class="getFieldClasses('username')">
        <label :for="`${idPrefix}-username`" class="field-label">
          用户名
          <span class="required" aria-label="必填">*</span>
        </label>
        <div class="input-wrapper">
          <input
            :id="`${idPrefix}-username`"
            v-model="formData.username"
            type="text"
            class="field-input"
            placeholder="请输入用户名或邮箱"
            autocomplete="username"
            :aria-invalid="!!errors.username"
            :aria-describedby="errors.username ? `${idPrefix}-username-error` : undefined"
            @focus="handleFocus('username')"
            @blur="handleBlur('username')"
          >
          <div class="input-focus-ring" aria-hidden="true"></div>
        </div>
        <div
          v-if="errors.username"
          :id="`${idPrefix}-username-error`"
          class="field-error"
          role="alert"
        >
          {{ errors.username }}
        </div>
      </div>

      <!-- 邮箱字段（仅注册页面） -->
      <div v-if="type === 'register'" :class="getFieldClasses('email')">
        <label :for="`${idPrefix}-email`" class="field-label">
          邮箱地址
          <span class="required" aria-label="必填">*</span>
        </label>
        <div class="input-wrapper">
          <input
            :id="`${idPrefix}-email`"
            v-model="formData.email"
            type="email"
            class="field-input"
            placeholder="请输入邮箱地址"
            autocomplete="email"
            :aria-invalid="!!errors.email"
            :aria-describedby="errors.email ? `${idPrefix}-email-error` : undefined"
            @focus="handleFocus('email')"
            @blur="handleBlur('email')"
          >
          <div class="input-focus-ring" aria-hidden="true"></div>
        </div>
        <div
          v-if="errors.email"
          :id="`${idPrefix}-email-error`"
          class="field-error"
          role="alert"
        >
          {{ errors.email }}
        </div>
      </div>

      <!-- 密码字段 -->
      <div :class="getFieldClasses('password')">
        <label :for="`${idPrefix}-password`" class="field-label">
          密码
          <span class="required" aria-label="必填">*</span>
        </label>
        <div class="input-wrapper">
          <input
            :id="`${idPrefix}-password`"
            v-model="formData.password"
            type="password"
            class="field-input"
            placeholder="请输入密码"
            :autocomplete="type === 'register' ? 'new-password' : 'current-password'"
            :aria-invalid="!!errors.password"
            :aria-describedby="errors.password ? `${idPrefix}-password-error` : undefined"
            @focus="handleFocus('password')"
            @blur="handleBlur('password')"
          >
          <div class="input-focus-ring" aria-hidden="true"></div>
        </div>
        <div
          v-if="errors.password"
          :id="`${idPrefix}-password-error`"
          class="field-error"
          role="alert"
        >
          {{ errors.password }}
        </div>
      </div>

      <!-- 确认密码字段（仅注册页面） -->
      <div v-if="type === 'register'" :class="getFieldClasses('confirmPassword')">
        <label :for="`${idPrefix}-confirm-password`" class="field-label">
          确认密码
          <span class="required" aria-label="必填">*</span>
        </label>
        <div class="input-wrapper">
          <input
            :id="`${idPrefix}-confirm-password`"
            v-model="formData.confirmPassword"
            type="password"
            class="field-input"
            placeholder="请再次输入密码"
            autocomplete="new-password"
            :aria-invalid="!!errors.confirmPassword"
            :aria-describedby="errors.confirmPassword ? `${idPrefix}-confirm-password-error` : undefined"
            @focus="handleFocus('confirmPassword')"
            @blur="handleBlur('confirmPassword')"
          >
          <div class="input-focus-ring" aria-hidden="true"></div>
        </div>
        <div
          v-if="errors.confirmPassword"
          :id="`${idPrefix}-confirm-password-error`"
          class="field-error"
          role="alert"
        >
          {{ errors.confirmPassword }}
        </div>
      </div>

      <!-- 表单选项 -->
      <div class="form-options">
        <!-- 登录页面选项 -->
        <div v-if="type === 'login'" class="options-row">
          <label class="checkbox-field">
            <input
              v-model="formData.rememberMe"
              type="checkbox"
              class="checkbox-input"
            >
            <span class="checkbox-custom"></span>
            <span class="checkbox-label">记住我</span>
          </label>
          <a href="#" class="forgot-link">忘记密码？</a>
        </div>

        <!-- 注册页面选项 -->
        <div v-if="type === 'register'" class="options-row">
          <label class="checkbox-field">
            <input
              v-model="formData.agreeTerms"
              type="checkbox"
              class="checkbox-input"
              required
              :aria-invalid="!!errors.terms"
              :aria-describedby="errors.terms ? `${idPrefix}-terms-error` : undefined"
            >
            <span class="checkbox-custom"></span>
            <span class="checkbox-label">
              我同意 <a href="#" class="terms-link">服务条款</a> 和 <a href="#" class="terms-link">隐私政策</a>
            </span>
          </label>
        </div>

        <!-- 注册条款错误信息 -->
        <div
          v-if="type === 'register' && errors.terms"
          :id="`${idPrefix}-terms-error`"
          class="field-error"
          role="alert"
        >
          {{ errors.terms }}
        </div>
      </div>

      <!-- 提交按钮 -->
      <button
        type="submit"
        class="submit-button"
        :disabled="disabled || loading"
        :aria-busy="loading"
      >
        <span class="button-text">{{ submitButtonText }}</span>
        <div v-if="loading" class="loading-spinner" aria-hidden="true"></div>
      </button>

      <!-- 第三方登录（仅登录页面） -->
      <div v-if="type === 'login'" class="social-login">
        <div class="divider">
          <span class="divider-text">或使用以下方式登录</span>
        </div>
        
        <button
          type="button"
          class="google-button"
          :disabled="disabled || loading"
          @click="handleGoogleLogin"
        >
          <img 
            src="https://developers.google.com/identity/images/g-logo.png" 
            alt=""
            class="google-icon"
            aria-hidden="true"
          >
          <span class="button-text">使用 Google 登录</span>
        </button>
      </div>
    </form>

    <!-- 页面切换 -->
    <div class="form-footer">
      <p class="switch-text">
        {{ type === 'login' ? '还没有账号？' : '已经有账号了？' }}
        <RouterLink 
          :to="type === 'login' ? '/register' : '/login'"
          class="switch-link"
        >
          {{ type === 'login' ? '立即注册' : '立即登录' }}
        </RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
/* 使用设计系统变量 */
.modern-form {
  width: 100%;
  max-width: 400px;
  font-family: var(--font-family-primary);
}

/* 表单头部 */
.form-header {
  text-align: center;
  margin-bottom: var(--spacing-8);
}

.form-title {
  font-size: var(--text-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--auth-text-primary);
  margin-bottom: var(--spacing-2);
  line-height: var(--line-height-tight);
}

.form-subtitle {
  font-size: var(--text-base);
  color: var(--auth-text-secondary);
  margin: 0;
  font-weight: var(--font-weight-normal);
}

/* 表单主体 */
.form-body {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-6);
}

/* 表单字段 */
.form-field {
  position: relative;
}

.field-label {
  display: block;
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--auth-text-primary);
  margin-bottom: var(--spacing-2);
  line-height: var(--line-height-normal);
}

.required {
  color: var(--color-error);
  margin-left: var(--spacing-1);
}

.input-wrapper {
  position: relative;
}

.field-input {
  width: 100%;
  height: var(--form-control-height);
  padding: var(--form-control-padding);
  font-family: var(--font-family-primary);
  font-size: var(--text-base);
  font-weight: var(--font-weight-normal);
  color: var(--auth-text-primary);
  background: var(--color-surface);
  border: var(--form-control-border-width) solid var(--auth-border);
  border-radius: var(--form-control-border-radius);
  transition: var(--transition-all);
  outline: none;
  box-sizing: border-box;
}

.field-input::placeholder {
  color: var(--color-text-muted);
  font-weight: var(--font-weight-normal);
}

.field-input:hover {
  border-color: var(--auth-border-hover);
}

.field-input:focus {
  border-color: var(--auth-primary);
  box-shadow: var(--auth-shadow-glow);
}

/* 焦点环 */
.input-focus-ring {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: calc(var(--form-control-border-radius) + 2px);
  pointer-events: none;
  transition: var(--transition-opacity);
  opacity: 0;
}

.has-focus .input-focus-ring {
  opacity: 1;
  box-shadow: 0 0 0 3px var(--color-glow-primary);
}

/* 错误状态 */
.has-error .field-input {
  border-color: var(--color-error);
}

.has-error .input-focus-ring {
  opacity: 1;
  box-shadow: 0 0 0 3px var(--color-glow-error);
}

.field-error {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
  margin-top: var(--spacing-2);
  font-size: var(--text-sm);
  color: var(--color-error);
  font-weight: var(--font-weight-medium);
  line-height: var(--line-height-normal);
}

/* 表单选项 */
.form-options {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-3);
}

.options-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-4);
  flex-wrap: wrap;
}

/* 复选框样式 */
.checkbox-field {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  cursor: pointer;
  font-size: var(--text-sm);
  color: var(--auth-text-secondary);
  line-height: var(--line-height-normal);
}

.checkbox-input {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.checkbox-custom {
  position: relative;
  width: 18px;
  height: 18px;
  border: 2px solid var(--auth-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  transition: var(--transition-all);
  flex-shrink: 0;
}

.checkbox-input:checked + .checkbox-custom {
  background: var(--auth-primary);
  border-color: var(--auth-primary);
}

.checkbox-input:checked + .checkbox-custom::after {
  content: '';
  position: absolute;
  left: 50%;
  top: 50%;
  width: 4px;
  height: 8px;
  border: solid var(--color-text-inverted);
  border-width: 0 2px 2px 0;
  transform: translate(-50%, -60%) rotate(45deg);
}

.checkbox-input:focus-visible + .checkbox-custom {
  outline: 2px solid var(--auth-accent);
  outline-offset: 2px;
}

.checkbox-label {
  font-weight: var(--font-weight-normal);
}

/* 链接样式 */
.forgot-link,
.terms-link {
  color: var(--auth-accent);
  text-decoration: none;
  font-weight: var(--font-weight-medium);
  transition: var(--transition-colors);
}

.forgot-link:hover,
.terms-link:hover {
  color: var(--auth-primary);
  text-decoration: underline;
}

.forgot-link:focus-visible,
.terms-link:focus-visible {
  outline: 2px solid var(--auth-accent);
  outline-offset: 2px;
  border-radius: var(--radius-sm);
}

/* 按钮样式 */
.submit-button {
  position: relative;
  width: 100%;
  height: var(--button-height-md);
  padding: var(--button-padding-md);
  font-family: var(--font-family-primary);
  font-size: var(--text-base);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-inverted);
  background: var(--auth-primary);
  border: none;
  border-radius: var(--form-control-border-radius);
  cursor: pointer;
  transition: var(--transition-all);
  outline: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-2);
  margin-top: var(--spacing-2);
}

.submit-button:hover:not(:disabled) {
  background: var(--color-primary-600);
  transform: translateY(-1px);
  box-shadow: var(--auth-shadow-glow);
}

.submit-button:active:not(:disabled) {
  transform: translateY(0);
}

.submit-button:focus-visible {
  outline: 2px solid var(--auth-accent);
  outline-offset: 2px;
}

.submit-button:disabled {
  background: var(--color-text-muted);
  cursor: not-allowed;
  opacity: 0.6;
}

.button-text {
  transition: var(--transition-opacity);
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 第三方登录 */
.social-login {
  margin-top: var(--spacing-6);
}

.divider {
  position: relative;
  text-align: center;
  margin: var(--spacing-6) 0;
}

.divider-text {
  display: inline-block;
  padding: 0 var(--spacing-4);
  background: var(--color-surface);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  font-weight: var(--font-weight-normal);
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--auth-border);
}

.google-button {
  width: 100%;
  height: var(--button-height-md);
  padding: var(--button-padding-md);
  font-family: var(--font-family-primary);
  font-size: var(--text-base);
  font-weight: var(--font-weight-medium);
  color: var(--auth-text-primary);
  background: var(--color-surface);
  border: 1px solid var(--auth-border);
  border-radius: var(--form-control-border-radius);
  cursor: pointer;
  transition: var(--transition-all);
  outline: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-3);
}

.google-button:hover:not(:disabled) {
  background: var(--color-surface-raised);
  border-color: var(--auth-border-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.google-button:focus-visible {
  outline: 2px solid var(--auth-accent);
  outline-offset: 2px;
}

.google-button:disabled {
  background: var(--color-bg-muted);
  cursor: not-allowed;
  opacity: 0.6;
}

.google-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

/* 表单底部 */
.form-footer {
  text-align: center;
  margin-top: var(--spacing-8);
}

.switch-text {
  font-size: var(--text-sm);
  color: var(--auth-text-secondary);
  margin: 0;
  line-height: var(--line-height-normal);
}

.switch-link {
  color: var(--auth-accent);
  text-decoration: none;
  font-weight: var(--font-weight-medium);
  margin-left: var(--spacing-1);
  transition: var(--transition-colors);
}

.switch-link:hover {
  color: var(--auth-primary);
  text-decoration: underline;
}

.switch-link:focus-visible {
  outline: 2px solid var(--auth-accent);
  outline-offset: 2px;
  border-radius: var(--radius-sm);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-title {
    font-size: var(--text-2xl);
  }
  
  .form-subtitle {
    font-size: var(--text-sm);
  }
  
  .options-row {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-2);
  }
  
  .field-input {
    font-size: var(--text-base);
  }
  
  .submit-button,
  .google-button {
    height: var(--button-height-lg);
    font-size: var(--text-base);
  }
}

@media (max-width: 480px) {
  .form-title {
    font-size: var(--text-xl);
  }
  
  .form-body {
    gap: var(--spacing-5);
  }
  
  .field-input,
  .submit-button,
  .google-button {
    height: calc(var(--button-height-md) - 4px);
    padding: var(--button-padding-sm);
    font-size: var(--text-sm);
  }
  
  .google-icon {
    width: 16px;
    height: 16px;
  }
}

/* 无障碍和交互优化 */
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: var(--duration-fast) !important;
  }
}

@media (hover: none) and (pointer: coarse) {
  .field-input,
  .submit-button,
  .google-button,
  .checkbox-field,
  .forgot-link,
  .terms-link,
  .switch-link {
    min-height: var(--form-control-height);
    min-width: var(--form-control-height);
  }
  
  .submit-button:active,
  .google-button:active {
    transform: scale(0.98);
  }
}

/* 高对比度模式支持 */
@media (prefers-contrast: high) {
  .field-input {
    border-width: 2px;
  }
  
  .checkbox-custom {
    border-width: 2px;
  }
  
  .field-error {
    font-weight: var(--font-weight-bold);
  }
}
</style>