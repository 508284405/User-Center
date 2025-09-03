<script setup lang="ts">
import { computed } from 'vue';
import { Loading } from '@element-plus/icons-vue';

// Props
interface Props {
  typingText: string;
  visible: boolean;
}

const props = defineProps<Props>();

// 计算属性
const showIndicator = computed(() => props.visible && props.typingText.length > 0);
</script>

<template>
  <Transition name="typing-fade">
    <div v-if="showIndicator" class="typing-indicator">
      <div class="typing-content">
        <div class="typing-dots">
          <el-icon class="typing-icon">
            <Loading />
          </el-icon>
        </div>
        <span class="typing-text">{{ typingText }}</span>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.typing-indicator {
  padding: 8px 16px;
  background: rgba(64, 158, 255, 0.05);
  border-left: 3px solid #409eff;
  margin: 8px 0;
  border-radius: 4px;
}

.typing-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
}

.typing-icon {
  color: #409eff;
  font-size: 14px;
  animation: typing-spin 1s linear infinite;
}

.typing-text {
  font-style: italic;
}

/* 旋转动画 */
@keyframes typing-spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 渐入渐出动画 */
.typing-fade-enter-active,
.typing-fade-leave-active {
  transition: all 0.3s ease;
}

.typing-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.typing-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .typing-indicator {
    padding: 6px 12px;
    margin: 6px 0;
  }
  
  .typing-content {
    font-size: 12px;
  }
}</style>