<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/usercenter/auth'
import ModernForm from '@/components/ModernForm.vue'
import ThemeToggle from '@/components/ThemeToggle.vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const googleLoading = ref(false)
const formStatus = ref('idle') // idle, active, success, error
const loadingMessage = ref('')

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
    loadingMessage.value = '正在验证用户信息...'
    
    // 登录前清空token
    clearToken()
    
    const response = await authApi.login({
      username: formData.username,
      password: formData.password
    })

    console.log(response)
    if (response && response.success) {
      loadingMessage.value = '登录成功，正在跳转...'
      
      // 存储token信息
      localStorage.setItem('token', response.data.token.accessToken)
      localStorage.setItem('refreshToken', response.data.token.refreshToken)
      localStorage.setItem('userInfo', JSON.stringify(response.data.user))
      
      formStatus.value = 'success'
      ElMessage.success('登录成功！欢迎回来')
      
      setTimeout(() => {
        router.push('/dashboard')
      }, 1500)
    } else {
      throw new Error(response?.errMessage || '登录失败：服务器响应异常')
    }
  } catch (error) {
    console.error('登录错误:', error)
    formStatus.value = 'error'
    loadingMessage.value = ''
    
    // 登录失败时清空token
    clearToken()
    
    // 提供更友好的错误信息
    let errorMessage = '登录失败，请重试'
    if (error.message) {
      if (error.message.includes('用户名') || error.message.includes('密码')) {
        errorMessage = '用户名或密码错误，请检查后重试'
      } else if (error.message.includes('网络') || error.message.includes('timeout')) {
        errorMessage = '网络连接超时，请检查网络连接'
      } else {
        errorMessage = error.message
      }
    }
    
    ElMessage.error(errorMessage)
    
    setTimeout(() => {
      formStatus.value = 'idle'
    }, 2500)
  } finally {
    loading.value = false
  }
}

// 处理Google登录
const handleGoogleLogin = async () => {
  try {
    googleLoading.value = true
    loadingMessage.value = '正在获取Google登录链接...'
    
    // 获取当前页面URL作为成功后的重定向URL
    const redirectUri = window.location.origin + '/google-callback'
    
    const response = await authApi.getGoogleLoginUrl(redirectUri)
    if (response && response.success && response.data) {
      // 假设接口返回的数据中第一个键是Google登录URL
      const googleLoginUrl = response.data.mapKey1
      console.log('Google登录URL:', googleLoginUrl)
      
      loadingMessage.value = '正在跳转到Google登录页面...'
      
      // 重定向到Google登录页面
      window.location.href = googleLoginUrl
    } else {
      throw new Error(response?.errMessage || '获取Google登录链接失败')
    }
  } catch (error) {
    console.error('获取Google登录链接错误:', error)
    loadingMessage.value = ''
    ElMessage.error(error.message || 'Google登录暂时不可用，请稍后重试')
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
  <div class="auth-page login-container" :class="{ 'page-loaded': pageLoadAnimation }" role="main" aria-label="用户登录页面">
    <!-- 科技网格覆盖层 -->
    <div class="auth-grid-overlay"></div>
    <!-- 噪点纹理覆盖层 -->
    <div class="auth-noise-overlay"></div>
    
    <!-- 简化的背景装饰 -->
    
    
    <header class="header" role="banner">
      <!-- 无障碍跳转链接 -->
      <a href="#main-form" class="skip-link">跳转到主内容</a>
      <div class="logo" role="img" aria-label="用户中心Logo">
        <div class="logo-icon"></div>
        <span>用户中心</span>
      </div>
      <nav role="navigation" aria-label="主导航">
        <a href="#" class="nav-link" aria-label="返回首页">首页</a>
        <a href="#" class="nav-link" aria-label="查看帮助文档">帮助</a>
        <a href="#" class="nav-link" aria-label="联系客服">联系我们</a>
        <ThemeToggle mode="button" :show-label="false" aria-label="切换主题" />
      </nav>
    </header>

    <main class="main-content">
      <!-- 左侧品牌展示区域 - 简化版本 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="brand-header">
            <div class="brand-logo">
              <div class="logo-icon">🚀</div>
              <h1 class="brand-title">用户中心</h1>
            </div>
            <p class="brand-subtitle">安全、智能、高效的用户管理平台</p>
          </div>
          
          <div class="features-list">
            <div class="feature-item">
              <div class="feature-icon">🛡️</div>
              <div class="feature-content">
                <h3 class="feature-title">安全认证</h3>
                <p class="feature-desc">多层安全防护，保障账户安全</p>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">⚡</div>
              <div class="feature-content">
                <h3 class="feature-title">快速响应</h3>
                <p class="feature-desc">高性能架构，毫秒级响应</p>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">🌐</div>
              <div class="feature-content">
                <h3 class="feature-title">全球服务</h3>
                <p class="feature-desc">覆盖全球的CDN网络</p>
              </div>
            </div>
          </div>

          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-value">10K+</div>
              <div class="stat-label">活跃用户</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">99.9%</div>
              <div class="stat-label">服务可用性</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧登录表单区域 -->
      <div class="form-section">
        <div class="form-container auth-card" id="main-form" role="region" aria-label="登录表单">
          <ModernForm 
            type="login"
            :loading="loading"
            :disabled="formStatus === 'active'"
            @submit="onSubmit"
            @google-login="handleGoogleLogin"
            aria-describedby="login-instructions"
          />
          <div id="login-instructions" class="sr-only">
            请输入您的用户名和密码来登录系统
          </div>
          
          <!-- 全局加载状态指示器 -->
          <div v-if="loadingMessage" class="global-loading" role="status" aria-live="polite">
            <div class="loading-spinner"></div>
            <span class="loading-text">{{ loadingMessage }}</span>
          </div>
        </div>
      </div>
    </main>

    <footer class="footer" role="contentinfo">
      <div class="footer-content">
        <p>&copy; 2024 用户中心. All rights reserved.</p>
        <div class="footer-links" role="navigation" aria-label="法律信息链接">
          <a href="#" class="footer-link" aria-label="查看隐私政策">隐私政策</a>
          <a href="#" class="footer-link" aria-label="查看使用条款">使用条款</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* 主容器 - 简化设计 */
.login-container {
  position: relative;
  overflow: hidden;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
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
  opacity: 0.3;
  animation: mouseGlow 0.3s ease-out;
}

/* 头部导航 - 简化设计 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-4) var(--spacing-6);
  background: var(--auth-card-bg);
  backdrop-filter: var(--backdrop-blur-md);
  border-bottom: 1px solid var(--auth-border);
  position: relative;
  z-index: var(--z-index-sticky);
  box-shadow: var(--auth-shadow);
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  font-size: var(--text-xl);
  font-weight: var(--font-weight-bold);
  color: var(--auth-text-primary);
  transition: var(--transition-colors);
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: var(--auth-primary);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: var(--color-text-inverted);
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
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);
  transform: translateY(-2px) scale(1.03);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.12) 0%, rgba(255, 255, 255, 0.16) 100%);
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
  z-index: var(--z-index-base);
  min-height: 0;
  align-items: stretch;
}

/* 品牌展示区域 */
.brand-section {
  flex: 0 0 60%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-8);
  background: var(--auth-gradient-1);
  position: relative;
}

.brand-content {
  max-width: 500px;
  width: 100%;
}

.brand-header {
  text-align: center;
  margin-bottom: var(--spacing-8);
}

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-4);
  margin-bottom: var(--spacing-4);
}

.brand-logo .logo-icon {
  font-size: var(--text-4xl);
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, var(--auth-primary), var(--auth-secondary));
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--auth-shadow-glow);
}

.brand-title {
  font-size: var(--text-4xl);
  font-weight: var(--font-weight-extrabold);
  color: var(--auth-text-primary);
  margin: 0;
  background: linear-gradient(135deg, var(--auth-primary), var(--auth-secondary));
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  line-height: var(--line-height-tight);
}

.brand-subtitle {
  font-size: var(--text-lg);
  color: var(--auth-text-secondary);
  font-weight: var(--font-weight-normal);
  line-height: var(--line-height-relaxed);
  margin: 0;
}

.features-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-6);
  margin-bottom: var(--spacing-8);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-4);
  padding: var(--spacing-4);
  background: var(--color-glass-surface);
  border-radius: var(--radius-lg);
  backdrop-filter: var(--backdrop-blur-sm);
  border: 1px solid var(--auth-border);
  transition: var(--transition-all);
}

.feature-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md), var(--auth-shadow-glow);
  border-color: var(--auth-border-hover);
}

.feature-icon {
  font-size: var(--text-2xl);
  width: 48px;
  height: 48px;
  background: var(--auth-primary);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.feature-content {
  flex: 1;
}

.feature-title {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--auth-text-primary);
  margin: 0 0 var(--spacing-1) 0;
  line-height: var(--line-height-snug);
}

.feature-desc {
  font-size: var(--text-sm);
  color: var(--auth-text-secondary);
  margin: 0;
  line-height: var(--line-height-normal);
  font-weight: var(--font-weight-normal);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-4);
}

.stat-card {
  text-align: center;
  padding: var(--spacing-4);
  background: var(--color-glass-surface);
  border-radius: var(--radius-lg);
  backdrop-filter: var(--backdrop-blur-sm);
  border: 1px solid var(--auth-border);
}

.stat-value {
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-extrabold);
  color: var(--auth-primary);
  margin-bottom: var(--spacing-1);
  line-height: var(--line-height-none);
}

.stat-label {
  font-size: var(--text-xs);
  color: var(--auth-text-secondary);
  font-weight: var(--font-weight-medium);
  text-transform: uppercase;
  letter-spacing: var(--letter-spacing-wide);
  margin: 0;
}








































/* 表单区域 - 简化设计 */
.form-section {
  flex: 0 0 40%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: var(--spacing-8);
  background: var(--auth-gradient-2);
  border-left: 1px solid var(--auth-border);
  position: relative;
  z-index: var(--z-index-base);
}



.form-container {
  position: relative;
  z-index: var(--z-index-base);
  width: 100%;
  max-width: 400px;
  padding: var(--spacing-8);
  background: var(--auth-card-bg);
  border: var(--auth-card-border);
  border-radius: var(--radius-2xl);
  backdrop-filter: var(--backdrop-blur-md);
  box-shadow: var(--auth-shadow), var(--auth-shadow-glow);
  transition: var(--transition-all);
}


.form-container:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-xl), var(--auth-shadow-glow);
  border-color: var(--auth-border-hover);
}


/* 全局加载状态指示器 */
.global-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-4);
  background: var(--color-surface-overlay);
  backdrop-filter: var(--backdrop-blur-lg);
  border-radius: var(--radius-xl);
  padding: var(--spacing-6);
  box-shadow: var(--shadow-xl);
  border: 1px solid var(--auth-border);
  z-index: var(--z-index-modal);
  min-width: 200px;
  text-align: center;
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid var(--auth-border);
  border-top: 2px solid var(--auth-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  color: var(--auth-text-primary);
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 页脚 - 简化设计 */
.footer {
  background: var(--auth-card-bg);
  backdrop-filter: var(--backdrop-blur-md);
  border-top: 1px solid var(--auth-border);
  padding: var(--spacing-4) var(--spacing-6);
  position: relative;
  z-index: var(--z-index-sticky);
  box-shadow: var(--auth-shadow);
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  color: var(--auth-text-secondary);
  font-size: var(--text-sm);
}

.footer-links {
  display: flex;
  gap: var(--spacing-6);
}

.footer-link {
  color: var(--auth-text-secondary);
  text-decoration: none;
  transition: var(--transition-colors);
  padding: var(--spacing-1) var(--spacing-2);
  border-radius: var(--radius-md);
  font-weight: var(--font-weight-medium);
}

.footer-link:hover {
  color: var(--auth-text-primary);
}

/* 简化的动画定义 */

/* 页面加载动画 - 简化 */
.login-container {
  opacity: 0;
  transition: opacity var(--duration-normal) var(--ease-out);
}

.login-container.page-loaded {
  opacity: 1;
}

/* 响应式设计 */
@media screen and (max-width: 1024px) {
  .brand-section {
    flex: 0 0 55%;
  }
  
  .form-section {
    flex: 0 0 45%;
  }
  
  .brand-logo .logo-icon {
    width: 48px;
    height: 48px;
    font-size: var(--text-2xl);
  }
  
  .brand-title {
    font-size: var(--text-3xl);
  }
  
  .brand-subtitle {
    font-size: var(--text-base);
  }
  
  .form-container {
    padding: var(--spacing-6);
  }
}

@media screen and (max-width: 768px) {
  .header {
    padding: var(--spacing-3) var(--spacing-4);
    flex-wrap: wrap;
    gap: var(--spacing-3);
  }
  
  .logo {
    font-size: var(--text-lg);
  }
  
  .logo .logo-icon {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }
  
  nav {
    gap: var(--spacing-4);
    flex-wrap: wrap;
  }
  
  .nav-link {
    padding: var(--spacing-1) var(--spacing-2);
    font-size: var(--text-sm);
  }

  .main-content {
    flex-direction: column;
  }

  .brand-section {
    flex: none;
    min-height: 40vh;
    padding: var(--spacing-6);
  }

  .form-section {
    flex: 1;
    border-left: none;
    border-top: 1px solid var(--auth-border);
    padding: var(--spacing-6);
  }

  .brand-content {
    max-width: none;
  }

  .brand-header {
    margin-bottom: var(--spacing-6);
  }

  .brand-logo .logo-icon {
    width: 48px;
    height: 48px;
    font-size: var(--text-2xl);
  }
  
  .brand-title {
    font-size: var(--text-3xl);
  }

  .brand-subtitle {
    font-size: var(--text-base);
  }

  .features-list {
    gap: var(--spacing-4);
    margin-bottom: var(--spacing-6);
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-3);
  }

  .footer {
    padding: var(--spacing-3) var(--spacing-4);
  }

  .footer-content {
    flex-direction: column;
    text-align: center;
    gap: var(--spacing-3);
  }

  .footer-links {
    gap: var(--spacing-4);
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media screen and (max-width: 480px) {
  .header {
    padding: var(--spacing-3);
    flex-direction: column;
    gap: var(--spacing-2);
  }
  
  .logo {
    font-size: var(--text-base);
    order: 1;
  }
  
  nav {
    order: 2;
    justify-content: center;
    gap: var(--spacing-3);
  }
  
  .nav-link {
    padding: var(--spacing-1);
    font-size: var(--text-xs);
  }

  .brand-section {
    min-height: 35vh;
    padding: var(--spacing-4);
  }

  .form-section {
    padding: var(--spacing-4);
  }

  .form-container {
    padding: var(--spacing-5);
    border-radius: var(--radius-xl);
  }

  .brand-title {
    font-size: var(--text-2xl);
  }

  .brand-subtitle {
    font-size: var(--text-sm);
  }

  .features-list {
    gap: var(--spacing-3);
    margin-bottom: var(--spacing-4);
  }
  
  .feature-item {
    padding: var(--spacing-3);
  }
  
  .feature-icon {
    width: 36px;
    height: 36px;
    font-size: var(--text-lg);
  }
  
  .feature-title {
    font-size: var(--text-base);
  }
  
  .feature-desc {
    font-size: var(--text-xs);
  }

  .stats-grid {
    gap: var(--spacing-2);
  }
  
  .stat-card {
    padding: var(--spacing-3);
  }
  
  .stat-value {
    font-size: var(--text-xl);
  }

  .footer {
    padding: var(--spacing-2);
  }

  .footer-content {
    font-size: var(--text-xs);
    gap: var(--spacing-2);
  }

  .footer-links {
    gap: var(--spacing-3);
  }
}

/* 性能优化和硬件加速 */
.brand-section,
.form-section,
.header,
.footer {
  backface-visibility: hidden;
  transform: translateZ(0);
}
</style>