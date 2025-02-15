<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

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

const onSubmit = async () => {
  if (!loginFormRef.value) return

  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return

    loading.value = true
    const formData = new FormData()
    formData.append('username', loginForm.username)
    formData.append('password', loginForm.password)
    formData.append('rememberMe', loginForm.rememberMe)

    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      body: formData,
      credentials: 'include'
    })

    const data = await response.json()
    
    if (!response.ok || data.code !== 200) {
      throw new Error(data.message || '登录失败')
    }

    // 存储token
    localStorage.setItem('token', data.token)
    
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    console.error('登录错误:', error)
    ElMessage.error(error.message || '登录失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <header class="header">
      <div class="logo">用户中心</div>
      <nav>
        <a href="#">首页</a>
        <a href="#">帮助</a>
        <a href="#">联系我们</a>
      </nav>
    </header>

    <main class="main-content">
      <el-card class="form-container">
        <h1>欢迎登录</h1>
        <el-form
          :model="loginForm"
          :rules="rules"
          ref="loginFormRef"
          class="login-form"
          @submit.prevent="onSubmit"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              :prefix-icon="User"
              placeholder="请输入用户名/邮箱/手机号"
              @keyup.enter="onSubmit"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              :prefix-icon="Lock"
              placeholder="请输入密码"
              show-password
              @keyup.enter="onSubmit"
            />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
            <a href="#" class="forgot-password">忘记密码？</a>
          </div>

          <el-button
            type="primary"
            class="submit-btn"
            :loading="loading"
            @click="onSubmit"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>

          <div class="register-link">
            还没有账号？
            <router-link to="/register" class="register-btn">立即注册</router-link>
          </div>
        </el-form>
      </el-card>
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
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: #409EFF;
}

nav a {
  margin-left: 1.5rem;
  color: #606266;
  text-decoration: none;
  transition: color 0.3s;
}

nav a:hover {
  color: #409EFF;
}

.main-content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
}

.form-container {
  width: 100%;
  max-width: 400px;
}

.login-form {
  margin-top: 2rem;
}

h1 {
  text-align: center;
  color: #303133;
  margin-bottom: 2rem;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 1rem 0;
}

.forgot-password {
  color: #409EFF;
  text-decoration: none;
  font-size: 0.875rem;
}

.submit-btn {
  width: 100%;
  margin-top: 1rem;
}

.register-link {
  text-align: center;
  margin-top: 1.5rem;
  color: #606266;
  font-size: 0.875rem;
}

.register-btn {
  color: #409EFF;
  text-decoration: none;
}

.footer {
  background: rgba(255, 255, 255, 0.9);
  padding: 1rem 2rem;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
}

.footer-links a {
  margin-left: 1rem;
  color: #606266;
  text-decoration: none;
}

@media screen and (max-width: 768px) {
  .header {
    padding: 1rem;
  }

  .nav a {
    margin-left: 1rem;
  }

  .form-container {
    padding: 1.5rem;
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