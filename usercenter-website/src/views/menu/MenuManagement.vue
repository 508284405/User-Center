<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { menuApi } from '@/api/menu'

interface MenuItem {
  id: number
  menuName: string
  menuCode: string
  systemId: number
  parentId: number
  path: string
  component: string
  sort: number
}

interface PageQuery {
  pageNum: number
  pageSize: number
  menuName?: string
  menuCode?: string
  systemId?: number
}

const menus = ref<MenuItem[]>([])
const total = ref(0)
const loading = ref(false)
const searchQuery = ref('')
const pageQuery = ref<PageQuery>({
  pageNum: 1,
  pageSize: 10
})
const editDialogVisible = ref(false)
const createDialogVisible = ref(false)
const editForm = ref<MenuItem>({
  id: 0,
  menuName: '',
  menuCode: '',
  systemId: 0,
  parentId: 0,
  path: '',
  component: '',
  sort: 0
})
const editFormLoading = ref(false)

// 创建菜单
async function handleCreate() {
  const newMenu: MenuItem = {
    id: 0,
    menuName: '',
    menuCode: '',
    systemId: 0,
    parentId: 0,
    path: '',
    component: '',
    sort: 0
  }
  editForm.value = newMenu
  createDialogVisible.value = true
}

// 保存创建
async function handleSaveCreate() {
  editFormLoading.value = true
  try {
    // 验证必填字段
    if (!editForm.value.menuName || !editForm.value.menuCode) {
      throw new Error('菜单名称和菜单编码为必填项')
    }

    await menuApi.create({
      menuName: editForm.value.menuName,
      menuCode: editForm.value.menuCode,
      systemId: editForm.value.systemId,
      parentId: editForm.value.parentId,
      path: editForm.value.path,
      component: editForm.value.component,
      sort: editForm.value.sort
    })

    ElMessage.success('创建成功')
    createDialogVisible.value = false
    await fetchMenus()
  } catch (error) {
    console.error('创建菜单错误:', error)
    ElMessage.error('创建菜单失败')
  } finally {
    editFormLoading.value = false
  }
}

// 编辑菜单
async function handleEdit(menu: MenuItem) {
  editForm.value = { ...menu }
  editDialogVisible.value = true
}

// 保存编辑
async function handleSaveEdit() {
  editFormLoading.value = true
  try {
    await menuApi.update(editForm.value.id, {
      menuName: editForm.value.menuName,
      path: editForm.value.path,
      component: editForm.value.component,
      sort: editForm.value.sort
    })

    ElMessage.success('更新成功')
    editDialogVisible.value = false
    await fetchMenus()
  } catch (error) {
    console.error('更新菜单错误:', error)
    ElMessage.error('更新菜单失败')
  } finally {
    editFormLoading.value = false
  }
}

// 获取菜单列表
async function fetchMenus() {
  loading.value = true
  try {
    const response = await menuApi.page({
      ...pageQuery.value,
      menuName: searchQuery.value || undefined
    })

    menus.value = response.data
    total.value = response.total || 0
    console.log(response)
  } catch (error) {
    console.error('获取菜单列表错误:', error)
    ElMessage.error('获取菜单列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索菜单
async function handleSearch() {
  pageQuery.value.pageNum = 1
  await fetchMenus()
}

// 处理分页变化
async function handlePageChange(page: number) {
  pageQuery.value.pageNum = page
  await fetchMenus()
}

// 处理每页数量变化
async function handleSizeChange(size: number) {
  pageQuery.value.pageSize = size
  pageQuery.value.pageNum = 1
  await fetchMenus()
}

// 删除菜单
async function handleDelete(menu: MenuItem) {
  try {
    await ElMessageBox.confirm('确认删除该菜单吗？', '提示', {
      type: 'warning'
    })

    await menuApi.delete(menu.id)
    ElMessage.success('删除成功')
    await fetchMenus()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除菜单错误:', error)
      ElMessage.error('删除菜单失败')
    }
  }
}

onMounted(() => {
  fetchMenus()
})
</script>

<template>
  <div class="menu-management">
    <div class="page-header">
      <h2>菜单管理</h2>
      <div class="search-bar">
        <button class="create-btn" @click="handleCreate()">新建菜单</button>
        <input
          type="text"
          v-model="searchQuery"
          placeholder="搜索菜单名称"
          class="search-input"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </div>
    </div>

    <div class="table-container">
      <el-table v-loading="loading" :data="menus" class="menu-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="menuName" label="菜单名称" />
        <el-table-column prop="menuCode" label="菜单编码" />
        <el-table-column prop="systemId" label="系统ID" width="100" />
        <el-table-column prop="parentId" label="父菜单ID" width="100" />
        <el-table-column prop="menuUrl" label="菜单URL" />
        <el-table-column prop="menuType" label="菜单类型" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="visible" label="是否可见" />
        <el-table-column prop="icon" label="图标" />
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

    <!-- 创建对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="新建菜单"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="菜单名称" required>
          <el-input v-model="editForm.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="菜单编码" required>
          <el-input v-model="editForm.menuCode" placeholder="请输入菜单编码" />
        </el-form-item>
        <el-form-item label="系统ID">
          <el-input-number v-model="editForm.systemId" :min="0" />
        </el-form-item>
        <el-form-item label="父菜单ID">
          <el-input-number v-model="editForm.parentId" :min="0" />
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="editForm.path" placeholder="请输入路径" />
        </el-form-item>
        <el-form-item label="组件">
          <el-input v-model="editForm.component" placeholder="请输入组件" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editForm.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="editFormLoading"
          @click="handleSaveCreate"
        >
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑菜单"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="菜单名称" required>
          <el-input v-model="editForm.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="editForm.path" placeholder="请输入路径" />
        </el-form-item>
        <el-form-item label="组件">
          <el-input v-model="editForm.component" placeholder="请输入组件" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editForm.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="editFormLoading"
          @click="handleSaveEdit"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.menu-management {
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

.create-btn {
  padding: 8px 16px;
  background-color: #67c23a;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-right: 10px;
}

.create-btn:hover {
  background-color: #529b2e;
}
</style>