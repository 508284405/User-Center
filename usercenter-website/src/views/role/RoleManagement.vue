<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roleApi } from '@/api/role'

interface Role {
  id: number
  roleName: string
  roleCode: string
  systemId: number
  level: number
}

interface PageQuery {
  pageNum: number
  pageSize: number
  roleName?: string
  roleCode?: string
  systemId?: number
}

const roles = ref<Role[]>([])
const total = ref(0)
const loading = ref(false)
const searchQuery = ref({
  roleName: '',
  roleCode: '',
  systemId: undefined as number | undefined
})

const pageQuery = ref<PageQuery>({
  pageNum: 1,
  pageSize: 10
})

// 编辑对话框相关
const editDialogVisible = ref(false)
const editForm = ref<Role>({
  id: 0,
  roleName: '',
  roleCode: '',
  systemId: 0,
  level: 1
})
const editFormLoading = ref(false)

// 创建对话框相关
const createDialogVisible = ref(false)
const createForm = ref<Role>({
  id: 0,
  roleName: '',
  roleCode: '',
  systemId: 0,
  level: 1
})
const createFormLoading = ref(false)

// 获取角色列表
async function fetchRoles() {
  loading.value = true
  try {
    const response = await roleApi.page({
      ...pageQuery.value,
      ...searchQuery.value
    })
    roles.value = response.data
    total.value = response.total
  } catch (error) {
    console.error('获取角色列表错误:', error)
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索角色
async function handleSearch() {
  pageQuery.value.pageNum = 1
  await fetchRoles()
}

// 处理分页变化
async function handlePageChange(page: number) {
  pageQuery.value.pageNum = page
  await fetchRoles()
}

// 处理每页数量变化
async function handleSizeChange(size: number) {
  pageQuery.value.pageSize = size
  pageQuery.value.pageNum = 1
  await fetchRoles()
}

// 编辑角色
async function handleEdit(role: Role) {
  editForm.value = { ...role }
  editDialogVisible.value = true
}

// 保存编辑
async function handleSaveEdit() {
  editFormLoading.value = true
  try {
    await roleApi.update(editForm.value.id, {
      roleName: editForm.value.roleName,
      level: editForm.value.level
    })
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    await fetchRoles()
  } catch (error) {
    console.error('更新角色错误:', error)
    ElMessage.error('更新角色失败')
  } finally {
    editFormLoading.value = false
  }
}

// 删除角色
async function handleDelete(role: Role) {
  try {
    await ElMessageBox.confirm('确认删除该角色吗？', '提示', {
      type: 'warning'
    })

    await roleApi.delete(role.id)
    ElMessage.success('删除成功')
    await fetchRoles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除角色错误:', error)
      ElMessage.error('删除角色失败')
    }
  }
}

// 创建角色
async function handleCreate() {
  createFormLoading.value = true
  try {
    // 验证必填字段
    if (!createForm.value.roleName || !createForm.value.roleCode || !createForm.value.systemId) {
      throw new Error('角色名称、角色编码和系统ID为必填项')
    }

    await roleApi.create({
      roleName: createForm.value.roleName,
      roleCode: createForm.value.roleCode,
      systemId: createForm.value.systemId,
      level: createForm.value.level
    })

    ElMessage.success('创建成功')
    createDialogVisible.value = false
    await fetchRoles()
    // 重置表单
    createForm.value = {
      id: 0,
      roleName: '',
      roleCode: '',
      systemId: 0,
      level: 1
    }
  } catch (error) {
    console.error('创建角色错误:', error)
    ElMessage.error(error instanceof Error ? error.message : '创建角色失败')
  } finally {
    createFormLoading.value = false
  }
}

onMounted(() => {
  fetchRoles()
})
</script>

<template>
  <div class="role-management">
    <div class="page-header">
      <h2>角色管理</h2>
      <div class="search-bar">
        <button class="create-btn" @click="createDialogVisible = true">新建角色</button>
        <el-input
          v-model="searchQuery.roleName"
          placeholder="角色名称"
          class="search-input"
        />
        <el-input
          v-model="searchQuery.roleCode"
          placeholder="角色编码"
          class="search-input"
        />
        <el-input-number
          v-model="searchQuery.systemId"
          placeholder="系统ID"
          :min="1"
          class="search-input"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </div>
    </div>

    <div class="table-container">
      <el-table v-loading="loading" :data="roles" class="role-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="roleCode" label="角色编码" />
        <el-table-column prop="systemId" label="系统ID" />
        <el-table-column prop="level" label="角色级别" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <button class="edit-btn" @click="handleEdit(row)">编辑</button>
            <button class="delete-btn" @click="handleDelete(row)">删除</button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageQuery.pageNum"
          v-model:page-size="pageQuery.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑角色"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="角色名称" required>
          <el-input v-model="editForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" required>
          <el-input v-model="editForm.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="系统ID" required>
          <el-input-number v-model="editForm.systemId" :min="1" />
        </el-form-item>
        <el-form-item label="角色级别" required>
          <el-input-number v-model="editForm.level" :min="1" :max="99" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editFormLoading" @click="handleSaveEdit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 创建对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="新建角色"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="角色名称" required>
          <el-input v-model="createForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" required>
          <el-input v-model="createForm.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="系统ID" required>
          <el-input-number v-model="createForm.systemId" :min="1" />
        </el-form-item>
        <el-form-item label="角色级别" required>
          <el-input-number v-model="createForm.level" :min="1" :max="99" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="createFormLoading" @click="handleCreate">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.role-management {
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

.search-btn,
.create-btn {
  padding: 8px 16px;
  background-color: #0055cc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.search-btn:hover,
.create-btn:hover {
  background-color: #0044aa;
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

.edit-btn,
.delete-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-right: 8px;
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>