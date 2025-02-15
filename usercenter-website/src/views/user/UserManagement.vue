<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

interface User {
  id: number
  username: string
  email: string
  phone: string
  status: string
  createdAt: string
}

interface PageQuery {
  pageNum: number
  pageSize: number
  username?: string
  email?: string
  phone?: string
}

const users = ref<User[]>([])
const total = ref(0)
const loading = ref(false)
const searchQuery = ref('')

const pageQuery = ref<PageQuery>({
  pageNum: 1,
  pageSize: 10
})

// 获取用户列表
async function fetchUsers() {
  loading.value = true
  try {
    const response = await fetch('http://localhost:8080/api/users/page', {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        ...pageQuery.value,
        username: searchQuery.value || undefined,
        email: searchQuery.value || undefined,
        phone: searchQuery.value || undefined
      })
    })

    const data = await response.json()
    if (data.code === 200) {
      users.value = data.data.list
      total.value = data.data.total
    } else {
      throw new Error(data.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表错误:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索用户
async function handleSearch() {
  pageQuery.value.pageNum = 1
  await fetchUsers()
}

// 编辑用户
async function handleEdit(user: User) {
  try {
    const response = await fetch(`http://localhost:8080/api/users/${user.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        email: user.email,
        phone: user.phone
      })
    })

    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('更新用户信息成功')
      await fetchUsers()
    } else {
      throw new Error(data.message || '更新用户信息失败')
    }
  } catch (error) {
    console.error('更新用户信息错误:', error)
    ElMessage.error('更新用户信息失败')
  }
}

// 删除用户
async function handleDelete(user: User) {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 这里使用锁定用户接口，因为API中没有提供删除用户的接口
    const response = await fetch(`http://localhost:8080/api/users/${user.id}/lock`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })

    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('用户已锁定')
      await fetchUsers()
    } else {
      throw new Error(data.message || '锁定用户失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('锁定用户错误:', error)
      ElMessage.error('锁定用户失败')
    }
  }
}

// 处理分页变化
async function handlePageChange(page: number) {
  pageQuery.value.pageNum = page
  await fetchUsers()
}

// 处理每页数量变化
async function handleSizeChange(size: number) {
  pageQuery.value.pageSize = size
  pageQuery.value.pageNum = 1
  await fetchUsers()
}

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <div class="user-management">
    <div class="page-header">
      <h2>用户管理</h2>
      <div class="search-bar">
        <input
          type="text"
          v-model="searchQuery"
          placeholder="搜索用户名或邮箱"
          class="search-input"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </div>
    </div>

    <div class="table-container">
      <el-table v-loading="loading" :data="users" class="user-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === '启用' ? 'success' : 'danger'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">锁定</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageQuery.pageNum"
          v-model:page-size="pageQuery.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.user-management {
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

.search-bar {
  display: flex;
  gap: 10px;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 250px;
}

.search-btn {
  padding: 8px 16px;
  background-color: #0055cc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.search-btn:hover {
  background-color: #0044aa;
}

.table-container {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
}

.user-table th,
.user-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.user-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.875rem;
}

.status-badge.active {
  background-color: #e6f4ea;
  color: #1e7e34;
}

.status-badge.inactive {
  background-color: #feeced;
  color: #dc3545;
}

.actions {
  display: flex;
  gap: 8px;
}

.edit-btn,
.delete-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.edit-btn {
  background-color: #0055cc;
  color: white;
}

.edit-btn:hover {
  background-color: #0044aa;
}

.delete-btn {
  background-color: #dc3545;
  color: white;
}

.delete-btn:hover {
  background-color: #c82333;
}
</style>