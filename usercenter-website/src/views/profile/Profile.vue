<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { onMounted } from 'vue'

interface UserProfile {
  username: string
  email: string
  avatar: string
  role: string
  lastLogin: string
  createdAt: string
}

const userProfile = ref<UserProfile>({
  username: '',
  email: '',
  avatar: '',
  role: '',
  lastLogin: '',
  createdAt: ''
})

const isEditing = ref(false)
const avatarFile = ref<File | null>(null)

// 获取用户信息
async function fetchUserProfile() {
  try {
    const response = await fetch('http://localhost:8080/api/admin/users/profile', {
      credentials: 'include'
    })

    const data = await response.json()
    if (data.code === 200) {
      userProfile.value = data.data
    } else {
      throw new Error(data.message || '获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息错误:', error)
    ElMessage.error('获取用户信息失败')
  }
}

function handleEdit() {
  isEditing.value = true
}

async function handleSave() {
  try {
    const response = await fetch('http://localhost:8080/api/users/profile', {
      method: 'PUT',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        email: userProfile.value.email
      })
    })

    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('保存成功')
      isEditing.value = false
      await fetchUserProfile()
    } else {
      throw new Error(data.message || '保存失败')
    }
  } catch (error) {
    console.error('保存用户信息错误:', error)
    ElMessage.error('保存失败')
  }
}

function handleCancel() {
  isEditing.value = false
  fetchUserProfile() // 重新获取用户信息，放弃修改
}

async function handleAvatarChange(event: Event) {
  const input = event.target as HTMLInputElement
  if (input.files && input.files[0]) {
    avatarFile.value = input.files[0]
    const formData = new FormData()
    formData.append('avatar', input.files[0])

    try {
      const response = await fetch('http://localhost:8080/api/users/avatar', {
        method: 'POST',
        credentials: 'include',
        body: formData
      })

      const data = await response.json()
      if (data.code === 200) {
        ElMessage.success('头像上传成功')
        await fetchUserProfile()
      } else {
        throw new Error(data.message || '头像上传失败')
      }
    } catch (error) {
      console.error('上传头像错误:', error)
      ElMessage.error('头像上传失败')
    }
  }
}

onMounted(() => {
  fetchUserProfile()
})
</script>

<template>
  <div class="profile">
    <div class="page-header">
      <h2>个人信息</h2>
      <div class="actions" v-if="!isEditing">
        <button class="edit-btn" @click="handleEdit">编辑资料</button>
      </div>
      <div class="actions" v-else>
        <button class="save-btn" @click="handleSave">保存</button>
        <button class="cancel-btn" @click="handleCancel">取消</button>
      </div>
    </div>

    <div class="profile-container">
      <div class="avatar-section">
        <img :src="userProfile.avatar || 'https://via.placeholder.com/150'" alt="用户头像" class="avatar" />
        <div v-if="isEditing" class="upload-container">
          <input
            type="file"
            accept="image/*"
            @change="handleAvatarChange"
            class="file-input"
            id="avatar-upload"
          />
          <label for="avatar-upload" class="upload-btn">更换头像</label>
        </div>
      </div>

      <div class="info-section">
        <div class="info-group">
          <label>用户名</label>
          <span class="info-text">{{ userProfile.username }}</span>
        </div>

        <div class="info-group">
          <label>邮箱</label>
          <input
            v-if="isEditing"
            v-model="userProfile.email"
            type="email"
            class="info-input"
          />
          <span v-else class="info-text">{{ userProfile.email }}</span>
        </div>

        <div class="info-group">
          <label>角色</label>
          <span class="info-text">{{ userProfile.role }}</span>
        </div>

        <div class="info-group">
          <label>最后登录</label>
          <span class="info-text">{{ userProfile.lastLogin }}</span>
        </div>

        <div class="info-group">
          <label>注册时间</label>
          <span class="info-text">{{ userProfile.createdAt }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

h2 {
  margin: 0;
  color: #333;
}

.actions {
  display: flex;
  gap: 10px;
}

.edit-btn,
.save-btn,
.cancel-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.edit-btn,
.save-btn {
  background-color: #0055cc;
  color: white;
}

.edit-btn:hover,
.save-btn:hover {
  background-color: #0044aa;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
}

.cancel-btn:hover {
  background-color: #e8e8e8;
}

.profile-container {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 24px;
  display: flex;
  gap: 40px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
}

.upload-container {
  position: relative;
}

.file-input {
  display: none;
}

.upload-btn {
  padding: 8px 16px;
  background-color: #f5f5f5;
  border: none;
  border-radius: 4px;
  color: #666;
  cursor: pointer;
  transition: background-color 0.3s;
  display: inline-block;
}

.upload-btn:hover {
  background-color: #e8e8e8;
}

.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-group label {
  color: #666;
  font-size: 0.875rem;
}

.info-text {
  color: #333;
  font-size: 1rem;
}

.info-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  width: 100%;
  max-width: 300px;
}

.info-input:focus {
  outline: none;
  border-color: #0055cc;
  box-shadow: 0 0 0 2px rgba(0, 85, 204, 0.1);
}
</style>