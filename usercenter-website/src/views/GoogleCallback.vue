<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/usercenter/auth'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const statusMessage = ref('正在处理Google登录...')

// 清空token的函数
const clearToken = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
}

// 处理Google登录回调
const handleGoogleCallback = async () => {
  // 从URL中获取授权码
  const authCode = route.query.code

  if (!authCode) {
    loading.value = false
    statusMessage.value = '授权失败：未获取到授权码'
    return
  }

  try {
    statusMessage.value = '正在验证Google账号...'
    // 登录前清空token
    clearToken()
    
    // 调用Google登录API
    const response = await authApi.googleLogin({ authCode: authCode.toString() })

    if (response && response.success) {
      // 存储token信息
      localStorage.setItem('token', response.data.token.accessToken)
      localStorage.setItem('refreshToken', response.data.token.refreshToken)
      
      statusMessage.value = '登录成功，正在跳转...'
      ElMessage.success('Google账号登录成功')
      
      // 延迟跳转，让用户看到成功消息
      setTimeout(() => {
        router.push('/dashboard')
      }, 1500)
    } else {
      throw new Error('登录失败：服务器响应异常')
    }
  } catch (error) {
    console.error('Google登录错误:', error)
    // 登录失败时清空token
    clearToken()
    statusMessage.value = error.message || 'Google账号登录失败，请重试'
    ElMessage.error(statusMessage.value)
    
    // 登录失败后3秒返回登录页
    setTimeout(() => {
      router.push('/login')
    }, 3000)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 组件挂载后立即处理回调
  handleGoogleCallback()
})
</script>

<template>
  <div class="callback-container">
    <div class="callback-card">
      <div class="spinner-container" v-if="loading">
        <div class="spinner"></div>
      </div>
      <div class="icon-container" v-else>
        <div class="icon" :class="{ 'success': !statusMessage.includes('失败'), 'error': statusMessage.includes('失败') }">
          <i v-if="!statusMessage.includes('失败')" class="check-icon">✓</i>
          <i v-else class="error-icon">✗</i>
        </div>
      </div>
      <h2 class="status-title">{{ statusMessage }}</h2>
      <p v-if="statusMessage.includes('失败')" class="redirect-message">即将返回登录页...</p>
    </div>
  </div>
</template>

<style scoped>
.callback-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.callback-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 40px;
  text-align: center;
  width: 400px;
  max-width: 90%;
}

.spinner-container {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top-color: #409eff;
  animation: spin 1s ease-in-out infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.icon-container {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: white;
}

.success {
  background-color: #67c23a;
}

.error {
  background-color: #f56c6c;
}

.check-icon, .error-icon {
  font-style: normal;
}

.status-title {
  font-size: 24px;
  margin-bottom: 16px;
  color: #303133;
}

.redirect-message {
  font-size: 16px;
  color: #909399;
}
</style>
