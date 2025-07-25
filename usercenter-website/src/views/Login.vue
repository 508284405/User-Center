<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/usercenter/auth'
import AnimatedBackground from '../components/AnimatedBackground.vue'
import GlowingRing from '../components/GlowingRing.vue'
import TechForm from '../components/TechForm.vue'

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
</script>

<template>
  <div class="login-container">
    <!-- 动画背景 -->
    <AnimatedBackground />
    
    <header class="header">
      <div class="logo">用户中心</div>
      <nav>
        <a href="#">首页</a>
        <a href="#">帮助</a>
        <a href="#">联系我们</a>
      </nav>
    </header>

    <main class="main-content">
      <GlowingRing 
        :status="formStatus"
        code-type="login"
      >
        <TechForm 
          type="login" 
          @submit="onSubmit"
          @google-login="handleGoogleLogin"
        />
      </GlowingRing>
    </main>

    <footer class="footer">
      <div class="footer-content">
        <p>&copy; 2024 用户中心. All rights reserved.</p>
        <div class="footer-links">
          <a href="#">隐私政策</a>
          <a href="#">使用条款</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 10;
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: #FFFFFF;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

nav a {
  margin-left: 1.5rem;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: all 0.3s ease;
  font-weight: 300;
}

nav a:hover {
  color: #8B5CF6;
  text-shadow: 0 0 5px rgba(139, 92, 246, 0.8);
}

.main-content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  position: relative;
  z-index: 10;
}

.footer {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 1rem 2rem;
  position: relative;
  z-index: 10;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  color: rgba(255, 255, 255, 0.6);
}

.footer-links a {
  margin-left: 1rem;
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  transition: all 0.3s ease;
}

.footer-links a:hover {
  color: #8B5CF6;
  text-shadow: 0 0 5px rgba(139, 92, 246, 0.8);
}

@media screen and (max-width: 768px) {
  .header {
    padding: 1rem;
  }

  nav a {
    margin-left: 1rem;
  }

  .main-content {
    padding: 1rem;
  }

  .footer-content {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }

  .footer-links a {
    margin: 0 0.5rem;
  }
}
</style>