<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import ParticleOrbit from './ParticleOrbit.vue'

const props = defineProps({
  status: {
    type: String,
    default: 'idle',
    validator: (value) => ['idle', 'active', 'success', 'error'].includes(value)
  },
  codeType: {
    type: String,
    default: 'login',
    validator: (value) => ['login', 'register'].includes(value)
  }
})

const ringRef = ref(null)
let animationId = null
let rotation = 0
let scale = 1
let pulseDirection = 1

// 计算状态相关的样式
const ringStyles = computed(() => {
  const baseColor = props.codeType === 'login' ? '#8B5CF6' : '#EC4899'
  
  switch (props.status) {
    case 'active':
      return {
        borderColor: baseColor,
        boxShadow: `0 0 30px ${baseColor}, 0 0 60px ${baseColor}, 0 0 90px ${baseColor}`,
        transform: `scale(${scale}) rotate(${rotation}deg)`
      }
    case 'success':
      return {
        borderColor: '#10B981',
        boxShadow: '0 0 30px #10B981, 0 0 60px #10B981, 0 0 90px #10B981',
        transform: `scale(1.1) rotate(${rotation}deg)`
      }
    case 'error':
      return {
        borderColor: '#EF4444',
        boxShadow: '0 0 30px #EF4444, 0 0 60px #EF4444, 0 0 90px #EF4444',
        transform: `scale(0.95) rotate(${rotation}deg)`
      }
    default:
      return {
        borderColor: baseColor,
        boxShadow: `0 0 20px ${baseColor}80, 0 0 40px ${baseColor}40`,
        transform: `scale(${scale}) rotate(${rotation}deg)`
      }
  }
})

// 圆环动画
const animateRing = () => {
  rotation += 0.3
  
  // 呼吸效果
  if (props.status === 'idle' || props.status === 'active') {
    scale += 0.001 * pulseDirection
    if (scale >= 1.03) {
      pulseDirection = -1
    } else if (scale <= 0.97) {
      pulseDirection = 1
    }
  }
  
  animationId = requestAnimationFrame(animateRing)
}

// 监听状态变化
watch(() => props.status, (newStatus) => {
  if (newStatus === 'error') {
    // 错误状态的震动效果
    if (ringRef.value) {
      ringRef.value.style.animation = 'shake 0.5s ease-in-out'
      setTimeout(() => {
        if (ringRef.value) {
          ringRef.value.style.animation = ''
        }
      }, 500)
    }
  }
})

onMounted(() => {
  animateRing()
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
    animationId = null
  }
})
</script>

<template>
  <div class="glowing-ring-container">
    <div 
      ref="ringRef"
      class="glowing-ring"
      :style="ringStyles"
    >
      <!-- 粒子轨道效果 -->
      <ParticleOrbit :status="status" />
      
      <div class="form-content">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<style scoped>
.glowing-ring-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  min-height: 600px;
}

.glowing-ring {
  width: 600px;
  height: 600px;
  border: 3px solid #8B5CF6;
  border-radius: 50%;
  position: relative;
  transition: all 0.3s ease;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: blur(20px);
}

.form-content {
  width: 80%;
  height: 80%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  box-sizing: border-box;
  position: relative;
  z-index: 2;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-3px); }
  20%, 40%, 60%, 80% { transform: translateX(3px); }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .glowing-ring-container {
    min-height: 500px;
  }
  
  .glowing-ring {
    width: 480px;
    height: 480px;
  }
  
  .form-content {
    padding: 1.5rem;
  }
}

@media (max-width: 480px) {
  .glowing-ring-container {
    min-height: 450px;
  }
  
  .glowing-ring {
    width: 420px;
    height: 420px;
  }
  
  .form-content {
    padding: 1rem;
  }
}
</style> 