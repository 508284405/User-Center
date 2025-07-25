<script setup lang="ts">
import { ref, watch, onMounted, defineProps, defineEmits, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { roleApi } from '@/api/usercenter/role'
import { permissionApi } from '@/api/usercenter/permission'
import { Search } from '@element-plus/icons-vue'

interface Role {
  id: number
  roleName: string
  roleCode: string
  systemId: number
  level: number
}

const props = defineProps({
  userId: {
    type: Number,
    required: true
  },
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'saved'])

const loading = ref(false)
const allRoles = ref<Role[]>([])
const selectedRoleIds = ref<number[]>([])
const searchQuery = ref('')

// 过滤后的角色列表
const filteredRoles = computed(() => {
  if (!searchQuery.value) {
    return allRoles.value
  }
  const query = searchQuery.value.toLowerCase()
  return allRoles.value.filter(role => 
    role.roleName.toLowerCase().includes(query) || 
    role.roleCode.toLowerCase().includes(query)
  )
})

// 检查角色是否被选中
const isRoleSelected = (roleId: number) => {
  return selectedRoleIds.value.includes(roleId)
}

// 加载角色数据
async function loadRoleData() {
  loading.value = true
  try {
    // 获取所有角色
    const {data:roles} = await roleApi.getAll()
    console.log('角色列表数据:', roles)
    allRoles.value = Array.isArray(roles) ? roles : []
    
    // 获取用户已分配的角色ID
    const {data:userRoleIds} = await permissionApi.getUserRoles(props.userId)
    console.log('用户当前角色ID列表:', userRoleIds)
    
    // 确保角色ID是数字数组
    selectedRoleIds.value = Array.isArray(userRoleIds) 
      ? userRoleIds.map(x => x.id) : []

    console.log('初始化后的selectedRoleIds:', selectedRoleIds.value)
  } catch (error) {
    console.error('获取角色数据错误:', error)
    ElMessage.error('获取角色数据失败')
    allRoles.value = []
    selectedRoleIds.value = []
  } finally {
    loading.value = false
  }
}

// 保存用户角色分配
async function saveUserRoles() {
  loading.value = true
  try {
    console.log('保存前的 selectedRoleIds:', selectedRoleIds.value)
    
    if (!Array.isArray(selectedRoleIds.value)) {
      console.warn('selectedRoleIds 不是数组，正在创建空数组')
      selectedRoleIds.value = []
    }
    
    // 确保是数字数组
    const roleIdsToSave = [...selectedRoleIds.value]
    console.log('最终提交到后端的角色ID:', roleIdsToSave)
    
    // 发送请求到后端
    await permissionApi.assignUserRoles(props.userId, roleIdsToSave)
    ElMessage.success('保存用户角色成功')
    emit('saved')
    closeDialog()
  } catch (error) {
    console.error('保存用户角色错误:', error)
    ElMessage.error('保存用户角色失败')
  } finally {
    loading.value = false
  }
}

// 关闭对话框
function closeDialog() {
  emit('update:visible', false)
}

// 切换角色选择状态
function toggleRoleSelection(roleId: number) {
  const index = selectedRoleIds.value.indexOf(roleId)
  if (index === -1) {
    // 如果没有选中，则添加
    selectedRoleIds.value.push(roleId)
  } else {
    // 如果已选中，则移除
    selectedRoleIds.value.splice(index, 1)
  }
  console.log('更新后的 selectedRoleIds:', selectedRoleIds.value)
}

// 全选/取消全选当前可见角色
function toggleSelectAll() {
  const visibleRoleIds = filteredRoles.value.map(role => role.id)
  
  // 检查当前可见角色是否全部已选中
  const allSelected = visibleRoleIds.every(id => selectedRoleIds.value.includes(id))
  
  if (allSelected) {
    // 如果全部已选中，则取消所有可见角色的选中状态
    selectedRoleIds.value = selectedRoleIds.value.filter(id => !visibleRoleIds.includes(id))
  } else {
    // 如果不是全部选中，则选中所有可见角色（避免重复）
    const newSelectedRoleIds = [...selectedRoleIds.value]
    
    visibleRoleIds.forEach(id => {
      if (!newSelectedRoleIds.includes(id)) {
        newSelectedRoleIds.push(id)
      }
    })
    
    selectedRoleIds.value = newSelectedRoleIds
  }
}

// 计算当前可见角色是否全部选中
const allVisibleSelected = computed(() => {
  return filteredRoles.value.length > 0 && 
         filteredRoles.value.every(role => selectedRoleIds.value.includes(role.id))
})

// 计算当前可见角色是否部分选中
const someVisibleSelected = computed(() => {
  return !allVisibleSelected.value && 
         filteredRoles.value.some(role => selectedRoleIds.value.includes(role.id))
})

// 对话框显示状态变化时处理
watch(() => props.visible, (newVisible) => {
  if (newVisible && props.userId) {
    searchQuery.value = '' // 重置搜索
    loadRoleData()
  }
})

</script>

<template>
  <el-dialog
    v-model="props.visible"
    title="分配用户角色"
    width="650px"
    :before-close="closeDialog"
  >
    <div v-loading="loading" class="role-assignment-container">
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索角色名称或代码"
          clearable
          :prefix-icon="Search"
        />
      </div>
      
      <div class="role-count-info">
        <div class="count-badge">
          已选角色: <el-tag type="success">{{ selectedRoleIds.length }}</el-tag>
        </div>
        <div class="count-badge">
          总角色数: <el-tag type="info">{{ allRoles.length }}</el-tag>
        </div>
      </div>
      
      <el-card shadow="never" class="role-table-container">
        <el-table :data="filteredRoles" style="width: 100%" border stripe>
          <el-table-column type="selection" width="55" align="center">
            <template #header>
              <el-checkbox
                :model-value="allVisibleSelected"
                :indeterminate="someVisibleSelected"
                @change="toggleSelectAll"
              />
            </template>
            <template #default="{ row }">
              <el-checkbox
                :model-value="isRoleSelected(row.id)"
                @change="() => toggleRoleSelection(row.id)"
              />
            </template>
          </el-table-column>
          
          <el-table-column label="角色名称" prop="roleName" min-width="140">
            <template #default="{ row }">
              <el-tooltip :content="row.roleName" placement="top" :disabled="row.roleName.length < 15">
                <span class="role-name">{{ row.roleName }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          
          <el-table-column label="角色代码" prop="roleCode" min-width="120">
            <template #default="{ row }">
              <el-tag size="small">{{ row.roleCode }}</el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button
                :type="isRoleSelected(row.id) ? 'danger' : 'primary'"
                size="small"
                link
                @click="toggleRoleSelection(row.id)"
              >
                {{ isRoleSelected(row.id) ? '移除' : '添加' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="filteredRoles.length === 0" class="no-data">
          <el-empty description="未找到匹配的角色" />
        </div>
      </el-card>
    </div>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button 
          type="primary" 
          :loading="loading" 
          @click="saveUserRoles"
        >
          保存
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped>
.role-assignment-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-bar {
  margin-bottom: 8px;
}

.role-count-info {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.count-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.role-table-container {
  max-height: 350px;
  overflow-y: auto;
}

.role-name {
  display: inline-block;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-data {
  padding: 30px 0;
  text-align: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
