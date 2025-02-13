<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
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

  await loginFormRef.value.validate((valid, fields) => {
    if (valid) {
      console.log('Form submitted', loginForm)
      ElMessage.success('登录成功')
      router.push('/')
      // TODO: 实现实际的登录逻辑
    } else {
      console.log('验证失败', fields)
    }
  })
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
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              :prefix-icon="User"
              placeholder="请输入用户名/邮箱/手机号"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              :prefix-icon="Lock"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
            <a href="#" class="forgot-password">忘记密码？</a>
          </div>

          <el-button type="primary" class="submit-btn" @click="onSubmit">登录</el-button>

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
  display: -webkit-flex;
  display: -ms-flexbox;
  display: flex;
  -webkit-flex-direction: column;
  -ms-flex-direction: column;
  flex-direction: column;
  background: -webkit-linear-gradient(315deg, #f5f7fa 0%, #c3cfe2 100%);
  background: -ms-linear-gradient(315deg, #f5f7fa 0%, #c3cfe2 100%);
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.header {
  display: -webkit-flex;
  display: -ms-flexbox;
  display: flex;
  -webkit-justify-content: space-between;
  -ms-flex-pack: justify;
  justify-content: space-between;
  -webkit-align-items: center;
  -ms-flex-align: center;
  align-items: center;
  padding: 1rem 2rem;
  background: rgba(255, 255, 255, 0.9);
  -webkit-box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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
  -webkit-flex: 1;
  -ms-flex: 1;
  flex: 1;
  display: -webkit-flex;
  display: -ms-flexbox;
  display: flex;
  -webkit-justify-content: center;
  -ms-flex-pack: center;
  justify-content: center;
  -webkit-align-items: center;
  -ms-flex-align: center;
  align-items: center;
  padding: 2rem;
}

.form-container {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

h1 {
  text-align: center;
  color: #303133;
  margin-bottom: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: border-color 0.3s;
  font-size: 1rem;
}

.form-input:focus {
  border-color: #409EFF;
  outline: none;
}

.error-message {
  color: #f56c6c;
  font-size: 0.875rem;
  margin-top: 0.5rem;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #606266;
}

.forgot-password {
  color: #409EFF;
  text-decoration: none;
}

.submit-btn {
  width: 100%;
  padding: 0.75rem;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.submit-btn:hover {
  background-color: #66b1ff;
}

.register-link {
  text-align: center;
  margin-top: 1.5rem;
  color: #606266;
}

.register-btn {
  color: #409EFF;
  text-decoration: none;
  margin-left: 0.5rem;
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