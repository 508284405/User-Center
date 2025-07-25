<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AnimatedBackground from '../components/AnimatedBackground.vue'
import GlowingRing from '../components/GlowingRing.vue'
import TechForm from '../components/TechForm.vue'

const router = useRouter()
const formStatus = ref('idle') // idle, active, success, error

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreeTerms: false
})

const rules = {
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 3, message: '用户名至少3个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '邮箱不能为空', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' },
    { pattern: /[A-Z]/, message: '密码必须包含至少一个大写字母', trigger: 'blur' },
    { pattern: /[0-9]/, message: '密码必须包含至少一个数字', trigger: 'blur' },
    { pattern: /[!@#$%^&*]/, message: '密码必须包含至少一个特殊字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  agreeTerms: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请阅读并同意服务条款和隐私政策'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

const registerFormRef = ref()

const onSubmit = async (formData) => {
  try {
    formStatus.value = 'active'

    const response = await fetch('http://localhost:8080/api/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        username: formData.username,
        email: formData.email,
        password: formData.password
      })
    })

    const data = await response.json()

    if (!response.ok || data.code !== 200) {
      throw new Error(data.message || '注册失败')
    }

    formStatus.value = 'success'
    ElMessage.success('注册成功')
    
    setTimeout(() => {
      router.push('/login')
    }, 1000)
  } catch (error) {
    console.error('注册错误:', error)
    formStatus.value = 'error'
    ElMessage.error(error.message || '注册失败，请重试')
    
    setTimeout(() => {
      formStatus.value = 'idle'
    }, 2000)
  }
}
</script>

<template>
  <div class="register-container">
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
        code-type="register"
      >
        <TechForm 
          type="register" 
          @submit="onSubmit"
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
.register-container {
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
  color: #EC4899;
  text-shadow: 0 0 5px rgba(236, 72, 153, 0.8);
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
  color: #EC4899;
  text-shadow: 0 0 5px rgba(236, 72, 153, 0.8);
}

@media (max-width: 768px) {
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