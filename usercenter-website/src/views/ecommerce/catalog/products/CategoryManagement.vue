<template>
  <div class="category-management">
    <div class="page-header">
      <h2>商品分类管理</h2>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索分类名称"
          :prefix-icon="Search"
          clearable
          @input="filterCategories"
          class="search-input"
        />
        <el-button type="primary" @click="handleRefresh">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
        <el-button type="success" @click="handleAddNew">
          <el-icon><Plus /></el-icon> 新增分类
        </el-button>
      </div>
    </div>
    
    <el-card class="main-content">
      <!-- 树形结构 -->
      <div class="tree-container-full">
        <div class="tree-header">
          <h3>分类结构</h3>
          <el-tag type="info">可拖拽调整顺序</el-tag>
        </div>
        <el-tree
          ref="categoryTreeRef"
          :data="filteredCategoryTree"
          node-key="categoryId"
          draggable
          :allow-drop="allowDrop"
          @node-drop="handleNodeDrop"
          :expand-on-click-node="false"
          highlight-current
          :default-expand-all="true"
          @node-click="handleNodeClick"
          class="category-tree"
          :key="JSON.stringify(filteredCategoryTree)"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <div class="node-label">
                <el-icon v-if="data.isLeaf"><Folder /></el-icon>
                <el-icon v-else><FolderOpened /></el-icon>
                <el-tooltip
                  :content="node.label"
                  placement="top"
                  :disabled="!node.label || node.label.length <= 15"
                  effect="light"
                  :enterable="false"
                >
                  <span class="node-text">{{ node.label }}</span>
                </el-tooltip>
                <!-- <el-tag size="small" type="info" class="level-tag">
                   {{ data.level }}级
                </el-tag> -->
                <el-tag size="small" type="info" class="level-tag">
                    {{ data.name }}
                </el-tag>
              </div>
              <div class="node-actions">
                <el-button
                  type="primary"
                  size="small"
                  text
                  @click.stop="handleEdit(data)"
                  class="action-btn"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  text
                  @click.stop="handleDelete(data)"
                  :disabled="data.children && data.children.length > 0"
                  class="action-btn"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </div>
    </el-card>

    <!-- 新增/编辑分类对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '新增分类'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form 
        :model="form" 
        :rules="rules" 
        ref="formRef" 
        label-width="100px"
        status-icon
        class="category-form"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="50" show-word-limit />
        </el-form-item>
        
        <el-form-item label="父级分类" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="categoryTree"
            node-key="categoryId"
            :props="treeProps"
            clearable
            placeholder="选择父级分类（顶级分类可不选）"
            :disabled="isEdit && form.level === 0"
            class="tree-select"
          />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" :step="1" step-strictly controls-position="right" />
          <div class="form-tip">数字越小排序越靠前</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">{{ isEdit ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Folder, FolderOpened, Edit, Delete, Search } from '@element-plus/icons-vue'
import {
  getCategoryTree,
  createCategory,
  updateCategory,
  deleteCategory,
  moveCategory,
} from '../../api/client-web/category'

// 树形数据
const categoryTree = ref([])
const categoryTreeRef = ref(null)
const treeProps = {
  label: 'name',
  children: 'children'
}

// 搜索过滤
const searchKeyword = ref('')
const filteredCategoryTree = computed(() => {
  if (!searchKeyword.value) return categoryTree.value
  
  const filterNode = (node, keyword) => {
    if (!node) return false
    
    // 如果当前节点匹配关键字，则返回true
    if (node.name.toLowerCase().includes(keyword.toLowerCase())) {
      return true
    }
    
    // 如果有子节点，递归检查子节点
    if (node.children && node.children.length > 0) {
      return node.children.some(child => filterNode(child, keyword))
    }
    
    return false
  }
  
  // 深拷贝原始树，避免修改原始数据
  const filterTree = (tree, keyword) => {
    return tree.filter(node => {
      const copyNode = { ...node }
      
      if (node.children && node.children.length > 0) {
        copyNode.children = filterTree(node.children, keyword)
        if (copyNode.children.length > 0) return true
      }
      
      return filterNode(node, keyword)
    })
  }
  
  return filterTree(JSON.parse(JSON.stringify(categoryTree.value)), searchKeyword.value)
})

// 表单数据
const formRef = ref(null)
const isEdit = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const form = ref({
  categoryId: null,
  name: '',
  parentId: null,
  sort: 0,
  level: 0
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在1到50个字符之间', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
}

// 获取分类树
const fetchTree = async () => {
  try {
    const response = await getCategoryTree()
    console.log('分类数据原始响应:', response)
    
    if (response.success && Array.isArray(response.data)) {
      categoryTree.value = response.data
      console.log('设置后的分类树数据:', categoryTree.value)
    } else {
      console.warn('分类数据格式不符合预期:', response)
      categoryTree.value = []
    }
  } catch (error) {
    console.error('获取分类数据失败:', error)
    ElMessage.error('获取分类数据失败')
    categoryTree.value = []
  }
}

// 刷新数据
const handleRefresh = () => {
  fetchTree()
  searchKeyword.value = ''
  handleReset()
}

// 过滤分类
const filterCategories = () => {
  // 搜索后展开所有节点以便查看结果
  nextTick(() => {
    if (categoryTreeRef.value && searchKeyword.value) {
      categoryTreeRef.value.expandAll()
    }
  })
}

// 节点拖拽处理
const allowDrop = (draggingNode, dropNode, type) => {
  // 不允许拖拽到节点内部，只允许拖拽到前后
  if (type === 'inner') return false
  
  // 不允许将节点拖拽到不同层级的节点之间
  if (draggingNode.data.level !== dropNode.data.level) return false
  
  return true
}

const handleNodeDrop = async ({ data }, targetNode, type) => {
  try {
    submitting.value = true
    await moveCategory(data.categoryId, {
      categoryId: data.categoryId,
      targetParentId: targetNode?.data?.parentId || null
    })
    ElMessage.success('移动成功')
    fetchTree()
  } catch (error) {
    ElMessage.error('移动失败')
    fetchTree()
  } finally {
    submitting.value = false
  }
}

// 点击节点
const handleNodeClick = (data) => {
  handleEdit(data)
}

// 新增分类
const handleAddNew = () => {
  isEdit.value = false
  handleReset()
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (data) => {
  isEdit.value = true
  form.value = {
    categoryId: data.categoryId,
    name: data.name,
    parentId: data.parentId,
    sort: data.sort,
    level: data.level
  }
  dialogVisible.value = true
}

// 取消编辑
const handleCancel = () => {
  isEdit.value = false
  handleReset()
  dialogVisible.value = false
}

// 删除分类
const handleDelete = (data) => {
  // 如果有子分类，不允许删除
  if (data.children && data.children.length > 0) {
    ElMessage.warning('该分类下有子分类，无法删除')
    return
  }
  
  ElMessageBox.confirm('确认删除该分类？删除后无法恢复！', '警告', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      submitting.value = true
      await deleteCategory(data.categoryId)
      ElMessage.success('删除成功')
      fetchTree()
      // 如果正在编辑被删除的分类，重置表单
      if (isEdit.value && form.value.categoryId === data.categoryId) {
        handleReset()
      }
    } catch (error) {
      ElMessage.error('删除失败')
    } finally {
      submitting.value = false
    }
  }).catch(() => {})
}

// 表单重置
const handleReset = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.value = {
    categoryId: null,
    name: '',
    parentId: null,
    sort: 0,
    level: 0
  }
  isEdit.value = false
}

// 表单提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      submitting.value = true
      
      if (isEdit.value) {
        // 编辑模式
        await updateCategory(form.value.categoryId, {
          categoryId: form.value.categoryId,
          name: form.value.name,
          sort: form.value.sort
        })
        ElMessage.success('更新成功')
      } else {
        // 新增模式
        await createCategory({
          name: form.value.name,
          parentId: form.value.parentId || null,
          sort: form.value.sort
        })
        ElMessage.success('创建成功')
      }
      
      handleReset()
      fetchTree()
      dialogVisible.value = false
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitting.value = false
    }
  })
}

// 初始化加载
onMounted(() => {
  console.log('组件挂载，开始获取分类数据')
  setTimeout(() => {
    fetchTree()
  }, 500) // 添加延时确保组件完全挂载
})
</script>

<style scoped>
.category-management {
  padding: 24px;
  height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 240px;
  margin-right: 4px;
}

.main-content {
  min-height: 550px;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.tree-container-full {
  width: 100%;
  overflow: auto;
  padding: 8px;
}

.category-tree {
  margin-top: 12px;
}

.tree-node {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.tree-node:hover {
  background-color: #f5f7fa;
}

.node-label {
  display: flex;
  align-items: center;
  gap: 10px;
}

.node-text {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: inline-block;
}

.level-tag {
  margin-left: 8px;
  font-size: 12px;
  padding: 2px 6px;
  background-color: #f4f4f5;
  color: #909399;
  border-radius: 4px;
}

.node-actions {
  display: flex;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.tree-node:hover .node-actions {
  opacity: 1;
}

.action-btn {
  padding: 4px 8px;
  border-radius: 4px;
}

.category-form {
  padding: 8px 0;
}

.category-form .el-form-item {
  margin-bottom: 22px;
}

.tree-select {
  width: 100%;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }
  
  .search-input {
    width: 100%;
    margin-bottom: 8px;
  }
}
</style>