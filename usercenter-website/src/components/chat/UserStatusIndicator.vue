<script setup lang="ts">
import { computed } from 'vue';
import { UserStatus } from '@/utils/chatWebSocket';

// Props
interface Props {
  status: UserStatus;
  size?: 'small' | 'medium' | 'large';
  showText?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  size: 'medium',
  showText: false
});

// 状态文本映射
const statusTextMap = {
  [UserStatus.ONLINE]: '在线',
  [UserStatus.OFFLINE]: '离线',
  [UserStatus.BUSY]: '忙碌',
  [UserStatus.AWAY]: '离开',
  [UserStatus.INVISIBLE]: '隐身'
};

// 状态颜色映射
const statusColorMap = {
  [UserStatus.ONLINE]: '#67c23a',
  [UserStatus.OFFLINE]: '#909399',
  [UserStatus.BUSY]: '#f56c6c',
  [UserStatus.AWAY]: '#e6a23c',
  [UserStatus.INVISIBLE]: '#c0c4cc'
};

// 计算属性
const statusText = computed(() => statusTextMap[props.status] || '未知');
const statusColor = computed(() => statusColorMap[props.status] || '#909399');

const dotSize = computed(() => {
  switch (props.size) {
    case 'small': return '6px';
    case 'large': return '12px';
    default: return '8px';
  }
});

const fontSize = computed(() => {
  switch (props.size) {
    case 'small': return '11px';
    case 'large': return '14px';
    default: return '12px';
  }
});
</script>

<template>
  <div class="user-status-indicator" :class="{ 'with-text': showText }">
    <div 
      class="status-dot" 
      :style="{ 
        backgroundColor: statusColor,
        width: dotSize,
        height: dotSize
      }"
    ></div>
    <span v-if="showText" class="status-text" :style="{ fontSize }">
      {{ statusText }}
    </span>
  </div>
</template>

<style scoped>
.user-status-indicator {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.status-dot {
  border-radius: 50%;
  display: inline-block;
  position: relative;
  animation: status-pulse 2s infinite;
}

.status-text {
  color: #606266;
  white-space: nowrap;
}

/* 在线状态脉冲动画 */
@keyframes status-pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.4);
  }
  50% {
    box-shadow: 0 0 0 4px rgba(103, 194, 58, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0);
  }
}

/* 只对在线状态应用脉冲动画 */
.status-dot[style*="#67c23a"] {
  animation: status-pulse 2s infinite;
}

/* 其他状态不使用动画 */
.status-dot:not([style*="#67c23a"]) {
  animation: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-status-indicator.with-text {
    gap: 3px;
  }
  
  .status-text {
    font-size: 11px;
  }
}</style>