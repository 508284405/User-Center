<script setup>
import { ref, computed } from 'vue'
import { RouterLink } from 'vue-router'

const props = defineProps({
  type: {
    type: String,
    default: 'login',
    validator: (value) => ['login', 'register'].includes(value)
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

// 按钮样式计算
const buttonTheme = computed(() => {
  return props.type === 'login' ? {
    primary: '#8B5CF6',
    secondary: '#A78BFA',
    glow: 'rgba(139, 92, 246, 0.4)'
  } : {
    primary: '#EC4899',
    secondary: '#F472B6', 
    glow: 'rgba(236, 72, 153, 0.4)'
  }
})

// 输入框样式计算
const getInputStyle = (field) => {
  const theme = buttonTheme.value
  return focusStates.value[field] ? {
    borderColor: theme.primary,
    boxShadow: `0 0 10px ${theme.glow}`,
    backgroundColor: `${theme.primary}08`
  } : {}
}

const onSubmit = () => {
  emit('submit', formData.value)
}

const onGoogleLogin = () => {
  emit('google-login')
}
</script>

<template>
  <div class="tech-form">
    <h1 class="form-title">
      {{ type === 'login' ? '欢迎回来' : '创建账号' }}
    </h1>

    <div class="input-group">
      <div class="tech-input-wrapper">
        <input
          v-model="formData.username"
          type="text"
          placeholder="用户名/邮箱/手机号"
          class="tech-input"
          :style="getInputStyle('username')"
          @focus="handleFocus('username')"
          @blur="handleBlur('username')"
        >
        <div class="input-glow" :class="{ active: focusStates.username }"></div>
      </div>

      <div v-if="type === 'register'" class="tech-input-wrapper">
        <input
          v-model="formData.email"
          type="email"
          placeholder="邮箱地址"
          class="tech-input"
          :style="getInputStyle('email')"
          @focus="handleFocus('email')"
          @blur="handleBlur('email')"
        >
        <div class="input-glow" :class="{ active: focusStates.email }"></div>
      </div>

      <div class="tech-input-wrapper">
        <input
          v-model="formData.password"
          type="password"
          placeholder="密码"
          class="tech-input"
          :style="getInputStyle('password')"
          @focus="handleFocus('password')"
          @blur="handleBlur('password')"
        >
        <div class="input-glow" :class="{ active: focusStates.password }"></div>
      </div>

      <div v-if="type === 'register'" class="tech-input-wrapper">
        <input
          v-model="formData.confirmPassword"
          type="password"
          placeholder="确认密码"
          class="tech-input"
          :style="getInputStyle('confirmPassword')"
          @focus="handleFocus('confirmPassword')"
          @blur="handleBlur('confirmPassword')"
        >
        <div class="input-glow" :class="{ active: focusStates.confirmPassword }"></div>
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
      @click="onSubmit"
    >
      <span class="btn-content">
        {{ type === 'login' ? '登录' : '注册' }}
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
        :style="{ color: buttonTheme.primary }"
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
  color: #FFFFFF;
  font-size: 1.5rem;
  font-weight: 300;
  margin-bottom: 1rem;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
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
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  color: #FFFFFF;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  outline: none;
  position: relative;
  z-index: 2;
  box-sizing: border-box;
}

.tech-input::placeholder {
  color: rgba(255, 255, 255, 0.6);
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
  color: #FFFFFF;
}

.cyber-checkbox input {
  display: none;
}

.checkmark {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.5);
  border-radius: 3px;
  margin-right: 0.3rem;
  position: relative;
  transition: all 0.3s ease;
}

.cyber-checkbox input:checked + .checkmark {
  background: var(--primary-color, #8B5CF6);
  border-color: var(--primary-color, #8B5CF6);
  box-shadow: 0 0 8px rgba(139, 92, 246, 0.5);
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
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  transition: all 0.3s ease;
}

.forgot-link:hover {
  color: #FFFFFF;
  text-shadow: 0 0 5px rgba(255, 255, 255, 0.8);
}

.energy-button {
  position: relative;
  width: 100%;
  padding: 0.8rem 1.5rem;
  background: transparent;
  border: 2px solid var(--primary-color, #8B5CF6);
  border-radius: 8px;
  color: #FFFFFF;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s ease;
  margin-bottom: 0.8rem;
  z-index: 10;
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
    var(--primary-color, #8B5CF6), 
    transparent);
  transition: left 0.4s ease;
  z-index: 1;
  opacity: 0.3;
}

.energy-button:hover {
  box-shadow: 0 0 15px var(--glow-color, rgba(139, 92, 246, 0.4));
  transform: translateY(-1px);
}

.energy-button:hover .energy-fill {
  left: 100%;
}

.google-btn {
  --primary-color: #4285f4;
  --glow-color: rgba(66, 133, 244, 0.4);
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
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.8rem;
}

.divider::before,
.divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 35%;
  height: 1px;
  background: linear-gradient(to right, transparent, rgba(255, 255, 255, 0.3), transparent);
}

.divider::before {
  left: 0;
}

.divider::after {
  right: 0;
}

.switch-form {
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.8rem;
  margin-top: 0.5rem;
}

.switch-link {
  text-decoration: none;
  font-weight: 500;
  margin-left: 0.3rem;
  transition: all 0.3s ease;
}

.switch-link:hover {
  text-shadow: 0 0 5px currentColor;
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