<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const canvasRef = ref(null)
let animationId = null
let particles = []
let canvas = null
let ctx = null
let mouseX = 0
let mouseY = 0
let mouseRadius = 150

// 星尘粒子类 - 细腻的背景效果
class Particle {
  constructor(x, y) {
    this.x = x
    this.y = y
    this.vx = (Math.random() - 0.5) * 0.2
    this.vy = (Math.random() - 0.5) * 0.2
    this.size = Math.random() * 0.8 + 0.2
    this.opacity = Math.random() * 0.4 + 0.1
    this.twinklePhase = Math.random() * Math.PI * 2
  }
  
  update() {
    this.x += this.vx
    this.y += this.vy
    
    // 更新闪烁效果
    this.twinklePhase += 0.02
    
    // 边界检测 - 循环移动
    if (this.x < 0) this.x = canvas.width
    if (this.x > canvas.width) this.x = 0
    if (this.y < 0) this.y = canvas.height
    if (this.y > canvas.height) this.y = 0
  }
  
  draw() {
    ctx.save()
    // 闪烁效果
    const twinkle = Math.sin(this.twinklePhase) * 0.3 + 0.7
    ctx.globalAlpha = this.opacity * twinkle
    
    // 创建星光效果
    const gradient = ctx.createRadialGradient(
      this.x, this.y, 0,
      this.x, this.y, this.size * 3
    )
    gradient.addColorStop(0, '#FFFFFF')
    gradient.addColorStop(0.5, '#E0E7FF')
    gradient.addColorStop(1, 'transparent')
    
    ctx.fillStyle = gradient
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size * 3, 0, Math.PI * 2)
    ctx.fill()
    
    // 核心星点
    ctx.globalAlpha = this.opacity
    ctx.fillStyle = '#FFFFFF'
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.fill()
    ctx.restore()
  }
}

// 初始化星尘粒子系统 - 细腻的背景效果
const initParticles = () => {
  particles = []
  const particleCount = window.innerWidth > 768 ? 100 : 50
  
  for (let i = 0; i < particleCount; i++) {
    particles.push(new Particle(
      Math.random() * canvas.width,
      Math.random() * canvas.height
    ))
  }
}

// 绘制鼠标光效
const drawMouseGlow = () => {
  if (!ctx) return
  
  // 创建鼠标处的径向渐变
  const gradient = ctx.createRadialGradient(
    mouseX, mouseY, 0,
    mouseX, mouseY, mouseRadius
  )
  gradient.addColorStop(0, 'rgba(139, 92, 246, 0.3)')
  gradient.addColorStop(0.3, 'rgba(139, 92, 246, 0.15)')
  gradient.addColorStop(0.6, 'rgba(167, 139, 250, 0.08)')
  gradient.addColorStop(1, 'transparent')
  
  ctx.save()
  ctx.fillStyle = gradient
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  ctx.restore()
}

// 动画循环 - 绘制粒子和鼠标光效
const animate = () => {
  if (!ctx || !canvas) return
  
  // 清除画布
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  
  // 绘制鼠标光效
  drawMouseGlow()
  
  // 更新和绘制粒子
  particles.forEach(particle => {
    particle.update()
    particle.draw()
  })
  
  animationId = requestAnimationFrame(animate)
}

// 处理窗口大小变化
const handleResize = () => {
  if (!canvas) return
  
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
  initParticles()
}

// 初始化画布
const initCanvas = () => {
  if (!canvasRef.value) return
  
  canvas = canvasRef.value
  ctx = canvas.getContext('2d')
  
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
  
  initParticles()
  animate()
}

// 处理鼠标移动
const handleMouseMove = (event) => {
  if (!canvas) return
  
  mouseX = event.clientX
  mouseY = event.clientY
}

onMounted(() => {
  initCanvas()
  window.addEventListener('resize', handleResize)
  window.addEventListener('mousemove', handleMouseMove)
  
  // 页面可见性检测
  document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
      if (animationId) {
        cancelAnimationFrame(animationId)
        animationId = null
      }
    } else {
      if (!animationId) {
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
  window.removeEventListener('mousemove', handleMouseMove)
})
</script>

<template>
  <div class="animated-background">
    <canvas ref="canvasRef" class="particle-canvas"></canvas>
    <div class="gradient-overlay"></div>
  </div>
</template>

<style scoped>
.animated-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  overflow: hidden;
}

.particle-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, 
    #1a1a2e 0%, 
    #16213e 30%, 
    #0f3460 60%, 
    #1a1a2e 100%);
  opacity: 0.9;
  pointer-events: none;
  animation: gradientShift 20s ease-in-out infinite;
}

@keyframes gradientShift {
  0%, 100% {
    background: linear-gradient(180deg, 
      #1a1a2e 0%, 
      #16213e 30%, 
      #0f3460 60%, 
      #1a1a2e 100%);
  }
  50% {
    background: linear-gradient(180deg, 
      #0f3460 0%, 
      #1a1a2e 30%, 
      #16213e 60%, 
      #0f3460 100%);
  }
}

@media (max-width: 768px) {
  .gradient-overlay {
    background: linear-gradient(135deg, 
      #667eea 0%, 
      #764ba2 30%, 
      #f093fb 70%, 
      #4facfe 100%);
  }
}
</style> 