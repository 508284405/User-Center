<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { ParticleOrbitSystem } from '@/utils/particleOrbit'

const props = defineProps({
  status: {
    type: String,
    default: 'idle',
    validator: (value) => ['idle', 'active', 'success', 'error'].includes(value)
  },
  centerX: {
    type: Number,
    default: 0
  },
  centerY: {
    type: Number,
    default: 0
  }
})

const canvasRef = ref(null)
let animationId = null
let orbitSystem = null
let canvas = null
let ctx = null

// 初始化粒子轨道系统
const initOrbitSystem = () => {
  if (!canvas) return
  
  const rect = canvas.getBoundingClientRect()
  const centerX = canvas.width / 2
  const centerY = canvas.height / 2
  
  orbitSystem = new ParticleOrbitSystem(centerX, centerY)
}

// 动画循环
const animate = () => {
  if (!ctx || !canvas || !orbitSystem) return
  
  // 清除画布
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  
  // 更新和绘制粒子轨道
  orbitSystem.update()
  orbitSystem.draw(ctx)
  
  animationId = requestAnimationFrame(animate)
}

// 处理窗口大小变化
const handleResize = () => {
  if (!canvas || !orbitSystem) return
  
  // 获取父元素的尺寸
  const container = canvas.parentElement
  if (container) {
    canvas.width = container.offsetWidth
    canvas.height = container.offsetHeight
    
    const centerX = canvas.width / 2
    const centerY = canvas.height / 2
    orbitSystem.resize(centerX, centerY)
  }
}

// 监听状态变化
watch(() => props.status, (newStatus) => {
  if (orbitSystem) {
    orbitSystem.setStatus(newStatus)
  }
})

// 初始化画布
const initCanvas = () => {
  if (!canvasRef.value) return
  
  canvas = canvasRef.value
  ctx = canvas.getContext('2d')
  
  // 获取父元素的尺寸
  const container = canvas.parentElement
  if (container) {
    canvas.width = container.offsetWidth
    canvas.height = container.offsetHeight
  }
  
  initOrbitSystem()
  animate()
}

onMounted(() => {
  initCanvas()
  window.addEventListener('resize', handleResize)
  
  // 页面可见性检测
  document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
      if (animationId) {
        cancelAnimationFrame(animationId)
        animationId = null
      }
    } else {
      if (!animationId && orbitSystem) {
        animate()
      }
    }
  })
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <div class="particle-orbit">
    <canvas ref="canvasRef" class="orbit-canvas"></canvas>
  </div>
</template>

<style scoped>
.particle-orbit {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.orbit-canvas {
  width: 100%;
  height: 100%;
  display: block;
}
</style> 