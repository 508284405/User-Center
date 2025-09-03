<script setup lang="ts">
import { ref } from 'vue';
import { ArrowDown } from '@element-plus/icons-vue';
import { UserStatus } from '@/utils/chatWebSocket';
import UserStatusIndicator from './UserStatusIndicator.vue';

// Props
interface Props {
  currentStatus: UserStatus;
}

const props = defineProps<Props>();

// Emits
interface Emits {
  (e: 'status-change', status: UserStatus): void;
}

const emit = defineEmits<Emits>();

// 响应式数据
const dropdownVisible = ref(false);

// 状态选项
const statusOptions = [
  { value: UserStatus.ONLINE, label: '在线', description: '我当前在线并可以聊天' },
  { value: UserStatus.BUSY, label: '忙碌', description: '我很忙，请稍后联系' },
  { value: UserStatus.AWAY, label: '离开', description: '我暂时离开了' },
  { value: UserStatus.INVISIBLE, label: '隐身', description: '显示为离线状态' }
];

// 处理状态选择
const handleStatusSelect = (status: UserStatus) => {
  if (status !== props.currentStatus) {
    emit('status-change', status);
  }
  dropdownVisible.value = false;
};
</script>

<template>
  <el-dropdown 
    v-model:visible="dropdownVisible"
    trigger="click"
    placement="bottom-start"
    class="user-status-selector"
  >
    <div class="status-trigger">
      <UserStatusIndicator 
        :status="currentStatus" 
        size="small" 
        :show-text="true" 
      />
      <el-icon class="dropdown-arrow">
        <ArrowDown />
      </el-icon>
    </div>
    
    <template #dropdown>
      <el-dropdown-menu class="status-dropdown-menu">
        <el-dropdown-item
          v-for="option in statusOptions"
          :key="option.value"
          :class="{ active: option.value === currentStatus }"
          @click="handleStatusSelect(option.value)"
        >
          <div class="status-option">
            <div class="status-option-main">
              <UserStatusIndicator 
                :status="option.value" 
                size="small"
              />
              <span class="status-label">{{ option.label }}</span>
            </div>
            <div class="status-description">{{ option.description }}</div>
          </div>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style scoped>
.user-status-selector {
  cursor: pointer;
}

.status-trigger {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.status-trigger:hover {
  background-color: #f5f7fa;
}

.dropdown-arrow {
  font-size: 12px;
  color: #909399;
  transition: transform 0.2s;
}

.status-trigger:hover .dropdown-arrow {
  color: #606266;
}

:deep(.el-dropdown-menu) {
  min-width: 200px;
}

.status-dropdown-menu {
  padding: 8px 0;
}

:deep(.el-dropdown-menu__item) {
  padding: 0;
  line-height: normal;
}

:deep(.el-dropdown-menu__item.active) {
  background-color: #ecf5ff;
  color: #409eff;
}

.status-option {
  padding: 8px 16px;
  width: 100%;
}

.status-option-main {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}

.status-label {
  font-size: 14px;
  font-weight: 500;
}

.status-description {
  font-size: 11px;
  color: #909399;
  margin-left: 16px;
  line-height: 1.2;
}

:deep(.el-dropdown-menu__item.active) .status-description {
  color: #79bbff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  :deep(.el-dropdown-menu) {
    min-width: 180px;
  }
  
  .status-option {
    padding: 6px 12px;
  }
  
  .status-label {
    font-size: 13px;
  }
  
  .status-description {
    font-size: 10px;
    margin-left: 14px;
  }
}</style>