<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/usercenter/auth'
import GlowingRing from '../components/GlowingRing.vue'
import TechForm from '../components/TechForm.vue'
import ParticleBackground from '../components/ParticleBackground.vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const googleLoading = ref(false)
const formStatus = ref('idle') // idle, active, success, error

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// 清空token的函数
const clearToken = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
}

const rules = {
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 3, message: '用户名至少3个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ]
}

const loginFormRef = ref()

const onSubmit = async (formData) => {
  try {
    loading.value = true
    formStatus.value = 'active'
    
    // 登录前清空token
    clearToken()
    
    const response = await authApi.login({
      username: formData.username,
      password: formData.password
    })

    console.log(response)
    if (response && response.success) {
      // 存储token信息
      localStorage.setItem('token', response.data.token.accessToken)
      localStorage.setItem('refreshToken', response.data.token.refreshToken)
      localStorage.setItem('userInfo', JSON.stringify(response.data.user))
      
      formStatus.value = 'success'
      ElMessage.success('登录成功')
      
      setTimeout(() => {
        router.push('/dashboard')
      }, 1000)
    } else {
      throw new Error('登录失败：服务器响应异常')
    }
  } catch (error) {
    console.error('登录错误:', error)
    formStatus.value = 'error'
    // 登录失败时清空token
    clearToken()
    ElMessage.error(error.message || '登录失败，请重试')
    
    setTimeout(() => {
      formStatus.value = 'idle'
    }, 2000)
  } finally {
    loading.value = false
  }
}

// 处理Google登录
const handleGoogleLogin = async () => {
  try {
    googleLoading.value = true
    // 获取当前页面URL作为成功后的重定向URL
    const redirectUri = window.location.origin + '/google-callback'
    
    const response = await authApi.getGoogleLoginUrl(redirectUri)
    if (response && response.success && response.data) {
      // 假设接口返回的数据中第一个键是Google登录URL
      const googleLoginUrl = response.data.mapKey1
      console.log(googleLoginUrl)
      
      // 重定向到Google登录页面
      window.location.href = googleLoginUrl
    } else {
      throw new Error('获取Google登录链接失败')
    }
  } catch (error) {
    console.error('获取Google登录链接错误:', error)
    ElMessage.error(error.message || '获取Google登录链接失败，请重试')
  } finally {
    googleLoading.value = false
  }
}

// 鼠标跟随光效
const setupMouseFollowEffect = () => {
  const container = document.querySelector('.login-container')
  if (!container) return

  const handleMouseMove = (e) => {
    const rect = container.getBoundingClientRect()
    const x = e.clientX - rect.left
    const y = e.clientY - rect.top
    
    container.style.setProperty('--mouse-x', `${x}px`)
    container.style.setProperty('--mouse-y', `${y}px`)
    
    // 更新鼠标光效位置
    const afterElement = container
    if (afterElement) {
      afterElement.style.setProperty('--cursor-x', `${x}px`)
      afterElement.style.setProperty('--cursor-y', `${y}px`)
    }
  }

  const handleMouseEnter = () => {
    const afterElement = container
    if (afterElement) {
      afterElement.classList.add('mouse-active')
    }
  }

  const handleMouseLeave = () => {
    const afterElement = container
    if (afterElement) {
      afterElement.classList.remove('mouse-active')
    }
  }

  container.addEventListener('mousemove', handleMouseMove)
  container.addEventListener('mouseenter', handleMouseEnter)
  container.addEventListener('mouseleave', handleMouseLeave)

  return () => {
    container.removeEventListener('mousemove', handleMouseMove)
    container.removeEventListener('mouseenter', handleMouseEnter)
    container.removeEventListener('mouseleave', handleMouseLeave)
  }
}

// 页面加载动画
const pageLoadAnimation = ref(false)

onMounted(() => {
  // 设置鼠标跟随效果
  const cleanup = setupMouseFollowEffect()
  
  // 页面加载动画
  setTimeout(() => {
    pageLoadAnimation.value = true
  }, 100)
  
  // 清理函数
  onUnmounted(cleanup)
})
</script>

<template>
  <div class="auth-page login-container" :class="{ 'page-loaded': pageLoadAnimation }">
    <!-- 科技网格覆盖层 -->
    <div class="auth-grid-overlay"></div>
    <!-- 噪点纹理覆盖层 -->
    <div class="auth-noise-overlay"></div>
    
    <!-- 统一粒子背景：蓝青主题 -->
    <ParticleBackground theme="teal" density="medium" />
    
    <header class="header">
      <div class="logo">
        <div class="logo-icon"></div>
        <span>用户中心</span>
      </div>
      <nav>
        <a href="#" class="nav-link">首页</a>
        <a href="#" class="nav-link">帮助</a>
        <a href="#" class="nav-link">联系我们</a>
      </nav>
    </header>

    <main class="main-content">
      <!-- 左侧3D地球特效区域 -->
      <div class="earth-section auth-planet">
        <div class="earth-container">
          <div class="earth-3d">
            <div class="earth-sphere">
              <div class="earth-surface"></div>
              <div class="earth-atmosphere"></div>
              <div class="earth-glow"></div>
              <div class="earth-rings"></div>
              <div class="earth-particles"></div>
            </div>
            <div class="earth-orbit">
              <div class="satellite"></div>
            </div>
            <div class="earth-orbit-secondary">
              <div class="satellite-secondary"></div>
            </div>
            <div class="energy-field"></div>
            <div class="data-streams">
              <div class="data-stream stream-1"></div>
              <div class="data-stream stream-2"></div>
              <div class="data-stream stream-3"></div>
            </div>
          </div>
        </div>
        <div class="earth-overlay">
          <h1 class="welcome-title">欢迎来到用户中心</h1>
          <p class="welcome-subtitle">连接世界，开启未来科技体验</p>
          <div class="feature-tags">
            <span class="feature-tag">安全认证</span>
            <span class="feature-tag">智能管理</span>
            <span class="feature-tag">全球服务</span>
          </div>
        </div>
      </div>
      
      <!-- 右侧登录表单区域 -->
      <div class="form-section">
        <div class="form-wrapper auth-card">
          <GlowingRing 
            :status="formStatus"
            code-type="login"
          >
            <TechForm 
              type="login"
              theme="teal"
              @submit="onSubmit"
              @google-login="handleGoogleLogin"
            />
          </GlowingRing>
        </div>
      </div>
    </main>

    <footer class="footer">
      <div class="footer-content">
        <p>&copy; 2024 用户中心. All rights reserved.</p>
        <div class="footer-links">
          <a href="#" class="footer-link">隐私政策</a>
          <a href="#" class="footer-link">使用条款</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* 主容器 - 使用统一的蓝青科技主题 */
.login-container {
  /* 继承 .auth-page 的样式 */
  position: relative;
  overflow: hidden;
}

/* 增强背景遮罩层 */
.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--gradient-bg-1), var(--gradient-bg-2), var(--gradient-bg-3);
  z-index: 0;
  pointer-events: none;
}

/* 鼠标跟随光效 - 使用CSS变量 */
.login-container::after {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, var(--color-glow) 0%, var(--color-glow) 50%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  z-index: 1;
  opacity: 0;
  transition: all 0.3s ease;
  left: var(--cursor-x, 50%);
  top: var(--cursor-y, 50%);
  transform: translate(-50%, -50%);
}

.login-container.mouse-active::after {
  opacity: 0.6;
  animation: mouseGlow 0.3s ease-out;
}

/* 头部导航 - 使用CSS变量 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 3rem;
  background: linear-gradient(135deg, var(--surface-color) 0%, rgba(255, 255, 255, 0.12) 100%);
  backdrop-filter: blur(var(--blur-strong)) saturate(200%);
  border-bottom: 1px solid var(--color-border-hover);
  position: relative;
  z-index: 10;
  box-shadow: var(--shadow-medium), 0 0 40px var(--color-glow), inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  font-size: 1.6rem;
  font-weight: 700;
  color: var(--color-text-primary);
  text-shadow: 0 0 20px var(--color-glow), 0 0 10px rgba(255, 255, 255, 0.6);
  transition: all 0.3s ease;
}

.logo:hover {
  transform: scale(1.05);
  text-shadow: 0 0 25px var(--color-glow), 0 0 15px rgba(255, 255, 255, 0.8);
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
  border-radius: var(--radius-sm);
  position: relative;
  box-shadow: var(--shadow-glow);
}

.logo-icon::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 16px;
  height: 16px;
  background: var(--color-text-primary);
  border-radius: 4px;
  box-shadow: 0 0 10px var(--color-glow);
}

nav {
  display: flex;
  gap: 2rem;
}

.nav-link {
  color: var(--color-text-secondary);
  text-decoration: none;
  font-weight: 500;
  padding: 0.5rem 1rem;
  border-radius: var(--radius-sm);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.nav-link::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--color-surface);
  border-radius: var(--radius-sm);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.nav-link:hover {
  color: var(--color-text-primary);
  text-shadow: var(--shadow-glow), 0 0 10px rgba(255, 255, 255, 0.6);
  transform: translateY(-3px) scale(1.05);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.08) 0%, rgba(255, 255, 255, 0.12) 100%);
  border-color: var(--color-border-hover);
}

.nav-link:hover::before {
  opacity: 1;
}

/* 主内容区 */
.main-content {
  flex: 1;
  display: flex;
  position: relative;
  z-index: 1;
  min-height: 0;
}

/* 3D地球特效区域 */
.earth-section {
  flex: 0 0 58%;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  background: 
    radial-gradient(circle at center, rgba(139, 92, 246, 0.08) 0%, transparent 60%),
    radial-gradient(circle at 20% 80%, rgba(59, 130, 246, 0.06) 0%, transparent 50%);
  overflow: hidden;
}

.earth-container {
  position: relative;
  z-index: 1;
  perspective: 1000px;
}

.earth-container::before {
  content: '';
  position: absolute;
  width: 500px;
  height: 500px;
  top: -100px;
  left: -100px;
  border: 2px solid rgba(139, 92, 246, 0.1);
  border-radius: 50%;
  animation: pulseWave 4s ease-in-out infinite;
}

.earth-container::after {
  content: '';
  position: absolute;
  width: 600px;
  height: 600px;
  top: -150px;
  left: -150px;
  border: 1px solid rgba(59, 130, 246, 0.05);
  border-radius: 50%;
  animation: pulseWave 6s ease-in-out infinite;
  animation-delay: 2s;
}

.earth-3d {
  position: relative;
  width: 300px;
  height: 300px;
  transform-style: preserve-3d;
  animation: earthRotate 20s linear infinite;
  filter: drop-shadow(0 0 50px rgba(139, 92, 246, 0.3));
  will-change: transform;
  z-index: 2;
}

.earth-sphere {
  position: absolute;
  width: 100%;
  height: 100%;
  transform-style: preserve-3d;
  z-index: 1;
  will-change: transform;
  backface-visibility: hidden;
}

.earth-surface {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: 
    radial-gradient(circle at 30% 30%, var(--color-primary) 0%, var(--color-secondary) 25%, var(--color-dark) 50%, var(--color-accent) 75%, var(--color-light) 100%),
    radial-gradient(circle at 70% 70%, var(--color-cyber-secondary) 0%, var(--color-cyber-primary) 25%, var(--color-cyber-accent) 50%, var(--color-accent) 75%, var(--color-light) 100%),
    linear-gradient(45deg, var(--color-surface) 0%, transparent 50%),
    radial-gradient(circle at 20% 80%, var(--color-glow) 0%, transparent 60%);
  box-shadow: 
    inset 0 0 50px rgba(0, 0, 0, 0.3),
    var(--shadow-glow),
    var(--shadow-glow-strong),
    inset 0 0 100px var(--color-surface);
  animation: earthGlow 4s ease-in-out infinite alternate;
  position: relative;
  overflow: hidden;
  will-change: box-shadow;
  backface-visibility: hidden;
}

.earth-surface::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 40% 40%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 60% 60%, var(--color-glow) 0%, transparent 50%),
    linear-gradient(45deg, transparent 30%, var(--color-surface) 50%, transparent 70%);
  border-radius: 50%;
  animation: surfaceShimmer 6s ease-in-out infinite;
  will-change: opacity;
  backface-visibility: hidden;
}

.earth-surface::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 30% 70%, var(--color-cyber-glow) 0%, transparent 40%),
    radial-gradient(circle at 70% 30%, var(--color-glow) 0%, transparent 40%);
  border-radius: 50%;
  animation: surfaceShimmer 8s ease-in-out infinite reverse;
  will-change: opacity;
  backface-visibility: hidden;
}

.earth-atmosphere {
  position: absolute;
  width: 120%;
  height: 120%;
  top: -10%;
  left: -10%;
  border-radius: 50%;
  background: 
    radial-gradient(circle, var(--color-cyber-glow) 0%, var(--color-glow) 50%, transparent 70%);
  animation: atmospherePulse 3s ease-in-out infinite;
  will-change: opacity;
  backface-visibility: hidden;
}

.earth-glow {
  position: absolute;
  width: 140%;
  height: 140%;
  top: -20%;
  left: -20%;
  border-radius: 50%;
  background: 
    radial-gradient(circle, var(--color-glow-strong) 0%, var(--color-glow) 30%, transparent 60%);
  animation: glowPulse 5s ease-in-out infinite;
  will-change: opacity;
  backface-visibility: hidden;
}

.earth-orbit {
  position: absolute;
  width: 400px;
  height: 400px;
  top: -50px;
  left: -50px;
  border: 1px solid var(--color-border-hover);
  border-radius: 50%;
  animation: orbitRotate 15s linear infinite;
  will-change: transform;
  backface-visibility: hidden;
}

.satellite {
  position: absolute;
  width: 8px;
  height: 8px;
  background: #FFFFFF;
  border-radius: 50%;
  top: -4px;
  left: 50%;
  box-shadow: 0 0 20px rgba(255, 255, 255, 0.8);
  animation: satelliteGlow 2s ease-in-out infinite alternate;
}

.earth-orbit-secondary {
  position: absolute;
  width: 350px;
  height: 350px;
  top: -25px;
  left: -25px;
  border: 1px solid rgba(139, 92, 246, 0.2);
  border-radius: 50%;
  animation: orbitRotate 25s linear infinite reverse;
  will-change: transform;
  backface-visibility: hidden;
}

.satellite-secondary {
  position: absolute;
  width: 6px;
  height: 6px;
  background: #A78BFA;
  border-radius: 50%;
  top: -3px;
  left: 50%;
  box-shadow: 0 0 15px rgba(167, 139, 250, 0.8);
  animation: satelliteGlow 3s ease-in-out infinite alternate;
}

.earth-rings {
  position: absolute;
  width: 140%;
  height: 140%;
  top: -20%;
  left: -20%;
  border-radius: 50%;
  background: 
    conic-gradient(from 0deg, transparent, rgba(139, 92, 246, 0.1), transparent, rgba(59, 130, 246, 0.1), transparent);
  animation: ringsRotate 30s linear infinite;
  will-change: transform;
  backface-visibility: hidden;
}

.earth-particles {
  position: absolute;
  width: 200%;
  height: 200%;
  top: -50%;
  left: -50%;
  border-radius: 50%;
  background: 
    radial-gradient(circle at 20% 20%, rgba(139, 92, 246, 0.3) 0%, transparent 2px),
    radial-gradient(circle at 80% 40%, rgba(59, 130, 246, 0.2) 0%, transparent 2px),
    radial-gradient(circle at 40% 80%, rgba(167, 139, 250, 0.25) 0%, transparent 2px),
    radial-gradient(circle at 60% 60%, rgba(139, 92, 246, 0.15) 0%, transparent 1px);
  animation: particlesFloat 8s ease-in-out infinite;
  will-change: transform, opacity;
  backface-visibility: hidden;
}

.energy-field {
  position: absolute;
  width: 180%;
  height: 180%;
  top: -40%;
  left: -40%;
  border-radius: 50%;
  background: 
    conic-gradient(from 0deg, transparent, rgba(139, 92, 246, 0.05), transparent, rgba(59, 130, 246, 0.05), transparent);
  animation: energyPulse 4s ease-in-out infinite;
  will-change: opacity;
  backface-visibility: hidden;
}

.data-streams {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
}

.data-streams::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(circle at 20% 20%, rgba(139, 92, 246, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(59, 130, 246, 0.1) 0%, transparent 50%);
  border-radius: 50%;
  animation: energyField 5s ease-in-out infinite;
}

.data-stream {
  position: absolute;
  width: 2px;
  height: 100px;
  background: linear-gradient(to bottom, transparent, rgba(139, 92, 246, 0.8), transparent);
  border-radius: 1px;
  animation: dataFlow 3s linear infinite;
}

.stream-1 {
  top: -50px;
  left: 20%;
  animation-delay: 0s;
}

.stream-2 {
  top: -50px;
  left: 50%;
  animation-delay: 1s;
}

.stream-3 {
  top: -50px;
  left: 80%;
  animation-delay: 2s;
}

.earth-overlay {
  position: absolute;
  bottom: 15%;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
  z-index: 2;
  pointer-events: none;
  max-width: 80%;
}

.welcome-title {
  font-size: 3rem;
  font-weight: 800;
  color: #FFFFFF;
  text-shadow: 0 0 40px rgba(255, 255, 255, 0.9), 0 0 20px rgba(139, 92, 246, 0.6);
  margin-bottom: 1.5rem;
  background: 
    linear-gradient(135deg, 
      #FFFFFF 0%, 
      #A78BFA 30%, 
      #8B5CF6 70%,
      #FFFFFF 100%
    );
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.1;
  letter-spacing: -0.02em;
  animation: titleGlow 4s ease-in-out infinite alternate;
}

.welcome-subtitle {
  font-size: 1.3rem;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 0 15px rgba(255, 255, 255, 0.4);
  font-weight: 300;
  margin-bottom: 2rem;
  line-height: 1.4;
}

.feature-tags {
  display: flex;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.feature-tag {
  padding: 0.6rem 1.2rem;
  background: 
    linear-gradient(135deg, 
      rgba(255, 255, 255, 0.25) 0%, 
      rgba(255, 255, 255, 0.15) 50%,
      rgba(255, 255, 255, 0.12) 100%
    );
  backdrop-filter: blur(12px) saturate(120%);
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: 20px;
  color: rgba(255, 255, 255, 0.98);
  font-size: 0.85rem;
  font-weight: 500;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
  box-shadow: 
    0 4px 20px rgba(0, 0, 0, 0.15),
    0 0 30px rgba(139, 92, 246, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.feature-tag::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.5s;
}

.feature-tag:hover::before {
  left: 100%;
}

.feature-tag:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 
    0 12px 35px rgba(0, 0, 0, 0.25),
    0 0 50px rgba(139, 92, 246, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.5),
    0 0 20px rgba(139, 92, 246, 0.4);
  border-color: rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(15px) saturate(150%);
}

.feature-tag:nth-child(1) {
  animation: tagFloat 6s ease-in-out infinite;
  border-color: rgba(139, 92, 246, 0.3);
}

.feature-tag:nth-child(2) {
  animation: tagFloat 6s ease-in-out infinite;
  animation-delay: 2s;
  border-color: rgba(59, 130, 246, 0.3);
}

.feature-tag:nth-child(3) {
  animation: tagFloat 6s ease-in-out infinite;
  animation-delay: 4s;
  border-color: rgba(167, 139, 250, 0.3);
}

/* 表单区域 */
.form-section {
  flex: 0 0 42%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: 
    linear-gradient(135deg, 
      rgba(255, 255, 255, 0.4) 0%, 
      rgba(255, 255, 255, 0.35) 50%,
      rgba(255, 255, 255, 0.3) 100%
    );
  backdrop-filter: blur(15px) saturate(150%);
  border-left: 2px solid rgba(255, 255, 255, 0.6);
  position: relative;
  z-index: 2;
}

.form-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at center, rgba(139, 92, 246, 0.12) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(59, 130, 246, 0.08) 0%, transparent 40%),
    linear-gradient(45deg, rgba(139, 92, 246, 0.04) 0%, transparent 50%);
  z-index: 0;
}

.form-section::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    linear-gradient(90deg, 
      transparent 0%, 
      rgba(34, 211, 238, 0.08) 30%,
      rgba(139, 92, 246, 0.06) 50%,
      rgba(34, 211, 238, 0.08) 70%,
      transparent 100%
    );
  z-index: 1;
  animation: shimmer 4s ease-in-out infinite;
}

.form-wrapper {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 480px;
  padding: 3rem 2rem;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0.1) 100%);
  border-radius: var(--radius-xl, 24px);
  backdrop-filter: blur(10px);
}

/* 页脚 - 使用统一主题 */
.footer {
  background: linear-gradient(135deg, var(--surface-color) 0%, rgba(255, 255, 255, 0.08) 100%);
  backdrop-filter: blur(var(--blur-strong)) saturate(200%);
  border-top: 1px solid var(--color-border-hover);
  padding: 1.5rem 3rem;
  position: relative;
  z-index: 10;
  box-shadow: var(--shadow-medium), 0 0 40px var(--color-glow), inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  color: var(--color-text-muted);
  font-size: 0.9rem;
}

.footer-links {
  display: flex;
  gap: 2rem;
}

.footer-link {
  color: var(--color-text-muted);
  text-decoration: none;
  transition: all 0.3s ease;
  padding: 0.3rem 0.8rem;
  border-radius: var(--radius-sm);
}

.footer-link:hover {
  color: var(--color-text-primary);
  text-shadow: var(--shadow-glow);
  background: var(--color-surface);
}

/* 3D地球动画 */
@keyframes earthRotate {
  0% {
    transform: rotateY(0deg) rotateX(20deg);
  }
  50% {
    transform: rotateY(180deg) rotateX(20deg);
  }
  100% {
    transform: rotateY(360deg) rotateX(20deg);
  }
}

@keyframes earthGlow {
  0%, 100% {
    box-shadow: 
      inset 0 0 50px rgba(0, 0, 0, 0.3),
      0 0 50px rgba(139, 92, 246, 0.3),
      0 0 100px rgba(59, 130, 246, 0.2);
  }
  50% {
    box-shadow: 
      inset 0 0 50px rgba(0, 0, 0, 0.3),
      0 0 70px rgba(139, 92, 246, 0.4),
      0 0 120px rgba(59, 130, 246, 0.3);
  }
}

@keyframes atmospherePulse {
  0%, 100% {
    opacity: 0.3;
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes glowPulse {
  0%, 100% {
    opacity: 0.2;
  }
  50% {
    opacity: 0.4;
  }
}

@keyframes orbitRotate {
  0% {
    transform: rotateZ(0deg);
  }
  50% {
    transform: rotateZ(180deg);
  }
  100% {
    transform: rotateZ(360deg);
  }
}

@keyframes satelliteGlow {
  0%, 100% {
    box-shadow: 0 0 20px rgba(255, 255, 255, 0.8);
  }
  50% {
    box-shadow: 0 0 30px rgba(255, 255, 255, 1);
  }
}

@keyframes surfaceShimmer {
  0%, 100% {
    opacity: 0.3;
  }
  50% {
    opacity: 0.6;
  }
}

@keyframes ringsRotate {
  0% {
    transform: rotate(0deg);
  }
  50% {
    transform: rotate(180deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes particlesFloat {
  0%, 100% {
    transform: translateY(0px);
    opacity: 0.5;
  }
  50% {
    transform: translateY(-10px);
    opacity: 0.8;
  }
}

@keyframes energyPulse {
  0%, 100% {
    opacity: 0.2;
  }
  50% {
    opacity: 0.4;
  }
}

@keyframes dataFlow {
  0% {
    transform: translateY(-100px);
    opacity: 0;
  }
  20% {
    opacity: 1;
  }
  80% {
    opacity: 1;
  }
  100% {
    transform: translateY(100px);
    opacity: 0;
  }
}

@keyframes pulseWave {
  0% {
    transform: scale(0.8);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.3;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

@keyframes energyField {
  0%, 100% {
    opacity: 0.3;
  }
  50% {
    opacity: 0.6;
  }
}

/* 其他动画定义 */
@keyframes tagFloat {
  0%, 100% {
    transform: translateY(0px) scale(1);
    opacity: 0.8;
  }
  50% {
    transform: translateY(-8px) scale(1.02);
    opacity: 1;
    box-shadow: 
      0 8px 25px rgba(0, 0, 0, 0.2),
      0 0 35px rgba(139, 92, 246, 0.15);
  }
}

@keyframes shimmer {
  0%, 100% {
    opacity: 0;
    transform: translateX(-150%) scaleX(1);
  }
  25% {
    opacity: 0.4;
    transform: translateX(-50%) scaleX(1.5);
  }
  50% {
    opacity: 1;
    transform: translateX(50%) scaleX(1);
  }
  75% {
    opacity: 0.4;
    transform: translateX(150%) scaleX(1.5);
  }
}

@keyframes titleGlow {
  0%, 100% {
    text-shadow: 0 0 40px rgba(255, 255, 255, 0.9), 0 0 20px rgba(139, 92, 246, 0.6);
    filter: brightness(1);
  }
  50% {
    text-shadow: 0 0 50px rgba(255, 255, 255, 1), 0 0 30px rgba(139, 92, 246, 0.8), 0 0 15px rgba(167, 139, 250, 0.6);
    filter: brightness(1.1);
  }
}

@keyframes backgroundPulse {
  0%, 100% {
    opacity: 0.6;
  }
  50% {
    opacity: 0.8;
  }
}

@keyframes mouseGlow {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.5);
  }
  100% {
    opacity: 0.3;
    transform: translate(-50%, -50%) scale(1);
  }
}

@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 页面加载动画 */
.login-container {
  opacity: 0;
  transform: scale(0.95);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.login-container.page-loaded {
  opacity: 1;
  transform: scale(1);
}

.earth-section {
  animation: slideInLeft 1s ease-out;
  animation-delay: 0.2s;
  animation-fill-mode: both;
}

.form-section {
  animation: slideInRight 1s ease-out;
  animation-delay: 0.4s;
  animation-fill-mode: both;
}

.header {
  animation: slideInDown 0.8s ease-out;
  animation-delay: 0.1s;
  animation-fill-mode: both;
}

.footer {
  animation: slideInUp 0.8s ease-out;
  animation-delay: 0.6s;
  animation-fill-mode: both;
}

/* 响应式设计 */
@media screen and (max-width: 1200px) {
  .login-container {
    background-size: cover;
    background-position: center center;
  }
  
  .header {
    padding: 1.5rem 2rem;
  }
  
  .footer {
    padding: 1.5rem 2rem;
  }
  
  .welcome-title {
    font-size: 2.5rem;
  }
  
  .earth-3d {
    width: 250px;
    height: 250px;
  }
  
  .earth-orbit {
    width: 350px;
    height: 350px;
  }
  
  .earth-orbit-secondary {
    width: 300px;
    height: 300px;
  }
}

@media screen and (max-width: 1024px) {
  .login-container {
    background-size: cover;
    background-position: center top;
  }
  
  .earth-section {
    flex: 0 0 50%;
  }
  
  .form-section {
    flex: 0 0 50%;
  }
  
  .welcome-title {
    font-size: 2.2rem;
  }
  
  .welcome-subtitle {
    font-size: 1.1rem;
  }
  
  .form-wrapper {
    padding: 2.5rem 1.5rem;
  }
  
  .earth-3d {
    width: 200px;
    height: 200px;
  }
  
  .earth-orbit {
    width: 300px;
    height: 300px;
  }
  
  .earth-orbit-secondary {
    width: 250px;
    height: 250px;
  }
}

@media screen and (max-width: 768px) {
  .login-container {
    background-size: cover;
    background-position: center center;
  }
  
  .header {
    padding: 1rem 1.5rem;
  }
  
  .logo {
    font-size: 1.4rem;
  }
  
  .logo-icon {
    width: 28px;
    height: 28px;
  }
  
  .logo-icon::before {
    width: 14px;
    height: 14px;
  }
  
  nav {
    gap: 1rem;
  }
  
  .nav-link {
    padding: 0.4rem 0.8rem;
    font-size: 0.9rem;
  }

  .main-content {
    flex-direction: column;
  }

  .earth-section {
    flex: 0 0 45vh;
    min-height: 320px;
  }

  .form-section {
    flex: 1;
    border-left: none;
    border-top: 2px solid rgba(255, 255, 255, 0.5);
  }

  .form-wrapper {
    padding: 2rem 1.5rem;
    max-width: 100%;
  }

  .welcome-title {
    font-size: 2rem;
  }

  .welcome-subtitle {
    font-size: 1rem;
    margin-bottom: 1.5rem;
  }

  .earth-overlay {
    bottom: 10%;
    max-width: 90%;
  }
  
  .feature-tags {
    gap: 0.8rem;
  }
  
  .feature-tag {
    padding: 0.5rem 1rem;
    font-size: 0.8rem;
  }

  .footer {
    padding: 1rem 1.5rem;
  }

  .footer-content {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }

  .footer-links {
    gap: 1rem;
  }
  
  .earth-3d {
    width: 150px;
    height: 150px;
  }
  
  .earth-orbit {
    width: 250px;
    height: 250px;
  }
  
  .earth-orbit-secondary {
    width: 200px;
    height: 200px;
  }
}

@media screen and (max-width: 480px) {
  .header {
    padding: 1rem;
  }
  
  .logo {
    font-size: 1.2rem;
  }
  
  .nav-link {
    padding: 0.3rem 0.6rem;
    font-size: 0.85rem;
  }

  .earth-section {
    flex: 0 0 40vh;
    min-height: 280px;
  }

  .form-wrapper {
    padding: 1.5rem 1rem;
  }

  .welcome-title {
    font-size: 1.6rem;
  }

  .welcome-subtitle {
    font-size: 0.9rem;
  }
  
  .feature-tags {
    flex-direction: column;
    align-items: center;
    gap: 0.6rem;
  }
  
  .feature-tag {
    padding: 0.4rem 0.8rem;
    font-size: 0.75rem;
  }

  .footer-content {
    font-size: 0.8rem;
  }
  
  .earth-3d {
    width: 120px;
    height: 120px;
  }
  
  .earth-orbit {
    width: 200px;
    height: 200px;
  }
  
  .earth-orbit-secondary {
    width: 170px;
    height: 170px;
  }
}

/* 性能优化 */
@media (prefers-reduced-motion: reduce) {
  .earth-section,
  .form-section,
  .feature-tag,
  .earth-3d,
  .earth-orbit,
  .earth-orbit-secondary,
  .earth-rings,
  .earth-particles,
  .energy-field,
  .data-stream,
  .earth-container::before,
  .earth-container::after,
  .earth-surface::before,
  .earth-surface::after,
  .data-streams::before {
    animation: none;
  }
  
  .nav-link,
  .footer-link {
    transition: none;
  }
}

/* 硬件加速 */
.earth-section,
.form-section,
.header,
.footer,
.earth-3d {
  will-change: transform;
  backface-visibility: hidden;
  transform: translateZ(0);
}
</style>