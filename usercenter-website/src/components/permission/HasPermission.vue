<template>
  <div v-if="hasAccess">
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { hasPermission, userPermissionState } from '@/utils/permission'

const props = defineProps({
  // 单个权限或权限数组
  permission: {
    type: [String, Array],
    required: true
  },
  // 是否需要满足所有权限 (AND逻辑, 默认为OR逻辑)
  all: {
    type: Boolean,
    default: false
  }
})

// 计算是否拥有所需权限
const hasAccess = computed(() => {
  // 管理员拥有所有权限
  if (userPermissionState.isAdmin) {
    return true
  }
  
  // 如果权限未初始化，默认不显示
  if (!userPermissionState.initialized) {
    return false
  }
  
  // 处理单个权限
  if (typeof props.permission === 'string') {
    return hasPermission(props.permission)
  }
  
  // 处理权限数组
  if (Array.isArray(props.permission) && props.permission.length > 0) {
    if (props.all) {
      // 需要满足所有权限 (AND逻辑)
      return props.permission.every(perm => hasPermission(perm))
    } else {
      // 满足任一权限即可 (OR逻辑)
      return props.permission.some(perm => hasPermission(perm))
    }
  }
  
  return false
})
</script>
