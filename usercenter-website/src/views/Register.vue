<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Form as VeeForm, Field, ErrorMessage } from 'vee-validate'
import * as yup from 'yup'

const router = useRouter()

const schema = yup.object({
  username: yup.string().required('用户名不能为空').min(3, '用户名至少3个字符'),
  email: yup.string().required('邮箱不能为空').email('请输入有效的邮箱地址'),
  password: yup.string().required('密码不能为空').min(6, '密码至少6个字符')
    .matches(/[A-Z]/, '密码必须包含至少一个大写字母')
    .matches(/[0-9]/, '密码必须包含至少一个数字')
    .matches(/[!@#$%^&*]/, '密码必须包含至少一个特殊字符'),
  confirmPassword: yup.string().required('请确认密码')
    .oneOf([yup.ref('password')], '两次输入的密码不一致')
})

const onSubmit = (values) => {
  console.log('Form submitted', values)
  ElMessage.success('注册成功')
  router.push('/login')
  // TODO: 实现实际的注册逻辑
}
</script>

<template>
  <div class="register-container">
    <header class="header">
      <div class="logo">用户中心</div>
      <nav>
        <a href="#">首页</a>
        <a href="#">帮助</a>
        <a href="#">联系我们</a>
      </nav>
    </header>

    <main class="main-content">
      <div class="form-container">
        <h1>创建账号</h1>
        <VeeForm :validation-schema="schema" @submit="onSubmit" class="register-form">
          <div class="form-group">
            <Field name="username" type="text" class="form-input" placeholder="请输入用户名" />
            <ErrorMessage name="username" class="error-message" />
          </div>

          <div class="form-group">
            <Field name="email" type="email" class="form-input" placeholder="请输入邮箱地址" />
            <ErrorMessage name="email" class="error-message" />
          </div>

          <div class="form-group">
            <Field name="password" type="password" class="form-input" placeholder="请输入密码" />
            <ErrorMessage name="password" class="error-message" />
          </div>

          <div class="form-group">
            <Field name="confirmPassword" type="password" class="form-input" placeholder="请确认密码" />
            <ErrorMessage name="confirmPassword" class="error-message" />
          </div>

          <div class="terms">
            <label class="terms-label">
              <input type="checkbox" required> 我已阅读并同意
              <a href="#" class="terms-link">服务条款</a>和
              <a href="#" class="terms-link">隐私政策</a>
            </label>
          </div>

          <button type="submit" class="submit-btn">注册</button>

          <div class="login-link">
            已有账号？
            <router-link to="/login" class="login-btn">立即登录</router-link>
          </div>
        </VeeForm>
      </div>
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

.terms {
  margin-bottom: 1.5rem;
}

.terms-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #606266;
  font-size: 0.875rem;
}

.terms-link {
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

.login-link {
  text-align: center;
  margin-top: 1.5rem;
  color: #606266;
}

.login-btn {
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

@media (max-width: 768px) {
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