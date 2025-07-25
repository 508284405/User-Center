<script setup lang="ts">
import { ref, onMounted, defineProps, defineEmits } from 'vue'
import { ElMessage } from 'element-plus'
import { menuApi } from '@/api/usercenter/menu'
import { permissionApi } from '@/api/usercenter/permission'

interface MenuItem {
  id: number
  menuName: string
  menuCode: string
  parentId: number
  systemId: number
  path: string
  component: string
  sort: number
  type: number
  menuType: number
  children?: MenuItem[]
}

const props = defineProps({
  roleId: {
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
const menuList = ref<MenuItem[]>([])
const checkedMenuIds = ref<number[]>([])
const defaultExpandedKeys = ref<number[]>([])

// 树形菜单配置
const defaultProps = {
  children: 'children',
  label: 'menuName'
}

// 递归构建树形菜单
function buildMenuTree(menus: MenuItem[], parentId: number = 0): MenuItem[] {
  if (!Array.isArray(menus)) {
    console.warn('buildMenuTree: menus参数不是数组类型')
    return []
  }
  const result: MenuItem[] = []
  menus.forEach(menu => {
    if (menu.parentId === parentId) {
      const children = buildMenuTree(menus, menu.id)
      if (children.length) {
        menu.children = children
      }
      result.push(menu)
    }
  })
  return result
}

// 加载菜单数据
async function loadMenuData() {
  loading.value = true
  try {
    // 获取所有菜单
    const {data:allMenus } = await menuApi.getAll()
    // 获取角色已分配的菜单ID
    const {data:roleMenuIds} = await permissionApi.getRoleMenus(props.roleId)
    // 确保allMenus是数组类型
    console.log('roleMenuIds:', roleMenuIds)
    if (!Array.isArray(allMenus)) {
      throw new Error('获取的菜单数据格式不正确')
    }
    // 构建树形菜单结构
    menuList.value = buildMenuTree(allMenus)
    checkedMenuIds.value = roleMenuIds
    
    // 设置默认展开的节点（目录和一级菜单）
    defaultExpandedKeys.value = menuList.value
      .filter(menu => menu.type === 1 || menu.parentId === 0)
      .map(menu => menu.id)
  } catch (error) {
    console.error('获取菜单权限数据错误:', error)
    ElMessage.error('获取菜单权限数据失败')
  } finally {
    loading.value = false
  }
}

// 保存菜单权限设置
async function saveMenuPermission() {
  loading.value = true
  try {
    await permissionApi.assignRoleMenus(props.roleId, checkedMenuIds.value)
    ElMessage.success('保存权限设置成功')
    emit('saved')
    closeDialog()
  } catch (error) {
    console.error('保存菜单权限错误:', error)
    ElMessage.error('保存菜单权限失败')
  } finally {
    loading.value = false
  }
}

// 关闭对话框
function closeDialog() {
  emit('update:visible', false)
}

// 角色ID变化时重新加载菜单数据
import { watch } from 'vue'

watch(() => props.roleId, (newId) => {
  if (newId && props.visible) {
    loadMenuData()
  }
})

// 对话框显示状态变化时处理
watch(() => props.visible, (newVisible) => {
  if (newVisible && props.roleId) {
    loadMenuData()
  }
})

// 处理树节点选中状态变化
function handleCheckChange(node: any, { checkedKeys }: { checkedKeys: any[] }) {
  checkedMenuIds.value = checkedKeys.map(key => Number(key))
}

</script>

<template>
  <el-dialog
    v-model="props.visible"
    title="菜单权限设置"
    width="600px"
    :before-close="closeDialog"
    @open="loadMenuData"
  >
    <div v-loading="loading">
      <el-tree
        ref="menuTree"
        :data="menuList"
        show-checkbox
        node-key="id"
        :props="defaultProps"
        :default-expanded-keys="defaultExpandedKeys"
        :default-checked-keys="checkedMenuIds"
        @check="handleCheckChange"
      >
        <template #default="{ node, data }">
          <span>
            {{ data.menuName }}
            <el-tag size="small" v-if="data.type === 1">目录</el-tag>
            <el-tag size="small" type="success" v-else-if="data.type === 2">菜单</el-tag>
            <el-tag size="small" type="warning" v-else-if="data.type === 3">按钮</el-tag>
            <el-tag size="small" type="info" v-if="data.menuType === 0">客户端</el-tag>
            <el-tag size="small" type="danger" v-else-if="data.menuType === 1">运营端</el-tag>
          </span>
        </template>
      </el-tree>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button 
          type="primary" 
          :loading="loading" 
          @click="saveMenuPermission"
        >
          保存
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
