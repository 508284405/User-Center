<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api/usercenter/user'
import UserRoleAssignment from '@/components/permission/UserRoleAssignment.vue'

interface User {
  id: number
  username: string
  email: string
  phone: string
  status: number
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
const searchQuery = ref({
  username: '',
  email: '',
  phone: ''
})

const createDialogVisible = ref(false)
const createForm = ref({
  username: '',
  password: '',
  email: '',
  phone: ''
})
const createFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const pageQuery = ref<PageQuery>({
  pageNum: 1,
  pageSize: 10
})

// 角色分配相关
const roleAssignmentVisible = ref(false)
const currentUserId = ref(0)

// 获取用户列表
async function fetchUsers() {
  loading.value = true
  try {
    const { data, totalCount: totalCount } = await userApi.page({
      ...pageQuery.value,
      username: searchQuery.value.username || undefined,
      email: searchQuery.value.email || undefined,
      phone: searchQuery.value.phone || undefined
    })
    
    users.value = data
    total.value = totalCount
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

// 重置搜索条件
function handleReset() {
  searchQuery.value.username = ''
  searchQuery.value.email = ''
  searchQuery.value.phone = ''
  handleSearch()
}

const editDialogVisible = ref(false)
const editForm = ref({
  id: 0,
  username: '',
  email: '',
  phone: ''
})

// 编辑用户
async function handleEdit(user: User) {
  if (user.username === 'admin') {
    ElMessage.warning('管理员账号不可编辑')
    return
  }

  editForm.value = {
    id: user.id,
    username: user.username,
    email: user.email,
    phone: user.phone
  }
  editDialogVisible.value = true
}

// 提交编辑用户表单
async function handleEditSubmit(formEl: any) {
  if (!formEl) return
  
  await formEl.validate(async (valid: boolean) => {
    if (valid) {
      try {
        await userApi.update(editForm.value.id, {
          email: editForm.value.email || undefined,
          phone: editForm.value.phone || undefined
        })
        ElMessage.success('更新用户信息成功')
        editDialogVisible.value = false
        await fetchUsers()
      } catch (error) {
        console.error('更新用户信息错误:', error)
        ElMessage.error('更新用户信息失败')
      }
    }
  })
}

// 删除用户
async function handleDelete(user: User) {
  if (user.username === 'admin') {
    ElMessage.warning('管理员账号不可删除')
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await userApi.delete(user.id)
    ElMessage.success('用户已删除')
    await fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户错误:', error)
      ElMessage.error('删除用户失败')
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

// 打开新增用户对话框
function handleCreate() {
  createDialogVisible.value = true
}

// 提交新增用户表单
async function handleCreateSubmit(formEl: any) {
  if (!formEl) return
  
  await formEl.validate(async (valid: boolean) => {
    if (valid) {
      try {
        await userApi.create({
          username: createForm.value.username,
          password: createForm.value.password,
          email: createForm.value.email || undefined,
          phone: createForm.value.phone || undefined
        })
        ElMessage.success('创建用户成功')
        createDialogVisible.value = false
        createForm.value = {
          username: '',
          password: '',
          email: '',
          phone: ''
        }
        await fetchUsers()
      } catch (error) {
        console.error('创建用户错误:', error)
        ElMessage.error('创建用户失败')
      }
    }
  })
}

// 处理用户角色分配
function handleRoleAssignment(user: User) {
  if (user.username === 'admin') {
    ElMessage.warning('管理员账号角色不可修改')
    return
  }
  
  currentUserId.value = user.id
  roleAssignmentVisible.value = true
}

// 角色分配后的回调
function handleRoleAssignmentSaved() {
  ElMessage.success('用户角色分配成功')
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
        <button class="create-btn" @click="handleCreate">新增用户</button>
        <input
          type="text"
          v-model="searchQuery.username"
          placeholder="搜索用户名"
          class="search-input"
        />
        <input
          type="text"
          v-model="searchQuery.email"
          placeholder="搜索邮箱"
          class="search-input"
        />
        <input
          type="text"
          v-model="searchQuery.phone"
          placeholder="搜索手机号"
          class="search-input"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
        <button class="reset-btn" @click="handleReset">重置</button>
      </div>
    </div>

    <el-dialog
      v-model="createDialogVisible"
      title="新增用户"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createFormRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="createForm.email" placeholder="请输入邮箱（选填）" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="createForm.phone" placeholder="请输入手机号（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreateSubmit($refs.createFormRef)">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑用户"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="createFormRules"
        label-width="80px"
      >
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱（选填）" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleEditSubmit($refs.editFormRef)">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <div class="table-container">
      <el-table v-loading="loading" :data="users" class="user-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '有效' : '无效' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleEdit(row)"
              :disabled="row.username === 'admin'"
            >编辑</el-button>
            <el-button 
              type="success" 
              size="small" 
              @click="handleRoleAssignment(row)"
              :disabled="row.username === 'admin'"
            >分配角色</el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
              :disabled="row.username === 'admin'"
            >删除</el-button>
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

    <!-- 用户角色分配组件 -->
    <UserRoleAssignment
      v-model:visible="roleAssignmentVisible" 
      :userId="currentUserId"
      @saved="handleRoleAssignmentSaved"
    />
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
  align-items: center;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 200px;
}

.search-btn,
.reset-btn,
.create-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.search-btn {
  background-color: #0055cc;
  color: white;
}

.search-btn:hover {
  background-color: #0044aa;
}

.reset-btn {
  background-color: #909399;
  color: white;
}

.reset-btn:hover {
  background-color: #737579;
}

.create-btn {
  background-color: #67c23a;
  color: white;
}

.create-btn:hover {
  background-color: #529b2e;
}

.table-container {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.pagination {
  padding: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
