<script setup>
import { ref, onMounted, watch } from 'vue'

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

const cardRef = ref(null)
const decorativeParticles = ref([])

// 生成装饰粒子
const generateDecorativeParticles = () => {
  const particles = []
  for (let i = 0; i < 15; i++) {
    particles.push({
      id: i,
      x: Math.random() * 100,
      y: Math.random() * 100,
      delay: Math.random() * 3,
      duration: 2 + Math.random() * 3
    })
  }
  decorativeParticles.value = particles
}

// 监听状态变化
watch(() => props.status, (newStatus) => {
  if (newStatus === 'error' && cardRef.value) {
    // 错误状态的震动效果
    cardRef.value.style.animation = 'shake 0.6s cubic-bezier(0.36, 0.07, 0.19, 0.97)'
    setTimeout(() => {
      if (cardRef.value) {
        cardRef.value.style.animation = ''
      }
    }, 600)
  }
})

onMounted(() => {
  generateDecorativeParticles()
})
</script>

<template>
  <div class="form-card-container">
    <div 
      ref="cardRef"
      class="form-card auth-card"
      :class="[`status-${status}`, `type-${codeType}`]"
      :style="{
        '--active-color': 'var(--color-primary, #22D3EE)',
        '--active-glow': 'var(--color-glow, rgba(34,211,238,0.4))',
        '--active-accent': 'var(--color-accent, #67E8F9)',
        '--surface-color': 'var(--color-surface, rgba(255,255,255,0.06))',
        '--border-color': 'var(--color-border, rgba(255,255,255,0.18))'
      }"
    >
      <!-- 背景装饰 -->
      <div class="card-background">
        <div class="gradient-orb orb-1"></div>
        <div class="gradient-orb orb-2"></div>
        <div class="gradient-orb orb-3"></div>
      </div>

      <!-- 粒子装饰 -->
      <div class="particle-decoration">
        <div 
          v-for="particle in decorativeParticles" 
          :key="particle.id"
          class="decoration-particle"
          :style="{
            left: particle.x + '%',
            top: particle.y + '%',
            animationDelay: particle.delay + 's',
            animationDuration: particle.duration + 's'
          }"
        ></div>
      </div>

      <!-- 边框光效 -->
      <div class="border-glow"></div>
      
      <!-- 表单内容 -->
      <div class="form-content">
        <slot></slot>
      </div>

      <!-- 状态指示器 -->
      <div class="status-indicator">
        <div class="status-ring"></div>
        <div class="status-pulse"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 主容器 */
.form-card-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  min-height: 500px;
  padding: 2rem;
}

/* 表单卡片 */
.form-card {
  position: relative;
  width: 100%;
  max-width: 480px;
  min-height: 520px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.85) 0%, rgba(255, 255, 255, 0.75) 100%);
  backdrop-filter: blur(var(--blur-medium, 20px)) saturate(150%);
  border: 2px solid rgba(255, 255, 255, 0.9);
  border-radius: var(--radius-xl, 24px);
  padding: 3rem 2.5rem;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.25), 0 8px 32px rgba(0, 0, 0, 0.15), inset 0 2px 0 rgba(255, 255, 255, 0.95);
  overflow: hidden;
  will-change: transform, box-shadow;
}

/* 状态样式 */
.status-idle {
  transform: translateY(0px);
  box-shadow: 
    0 16px 48px rgba(0, 0, 0, 0.25),
    0 8px 32px rgba(0, 0, 0, 0.15),
    inset 0 2px 0 rgba(255, 255, 255, 0.95);
  border-color: rgba(255, 255, 255, 0.9);
}

.status-active {
  transform: translateY(-3px) scale(1.01);
  border-color: var(--active-color, #22D3EE);
  box-shadow: 
    0 20px 60px var(--active-glow, rgba(34, 211, 238, 0.3)),
    0 8px 32px var(--active-glow, rgba(34, 211, 238, 0.2)),
    0 0 0 3px var(--active-color, rgba(34, 211, 238, 0.4)),
    inset 0 2px 0 rgba(255, 255, 255, 0.95);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 255, 255, 0.8) 100%);
}

.status-success {
  border-color: rgba(16, 185, 129, 0.6);
  box-shadow: 
    0 12px 40px rgba(16, 185, 129, 0.3),
    0 4px 20px rgba(16, 185, 129, 0.2),
    0 0 0 2px rgba(16, 185, 129, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  transform: translateY(-2px) scale(1.02);
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.08) 0%, rgba(255, 255, 255, 0.12) 100%);
}

.status-error {
  border-color: rgba(239, 68, 68, 0.6);
  box-shadow: 
    0 12px 40px rgba(239, 68, 68, 0.3),
    0 4px 20px rgba(239, 68, 68, 0.2),
    0 0 0 2px rgba(239, 68, 68, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.25);
  transform: translateY(1px) scale(0.98);
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.08) 0%, rgba(255, 255, 255, 0.12) 100%);
}

/* 背景装饰 */
.card-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 24px;
  overflow: hidden;
  z-index: 0;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.1;
  animation: orbFloat 8s ease-in-out infinite alternate;
}

/* 统一使用蓝青主题色 */
.orb-1 {
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(34, 211, 238, 0.15) 0%, transparent 70%);
  top: -20%;
  right: -10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 80px;
  height: 80px;
  background: radial-gradient(circle, rgba(14, 165, 233, 0.12) 0%, transparent 70%);
  bottom: -15%;
  left: -5%;
  animation-delay: 2s;
}

.orb-3 {
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(103, 232, 249, 0.1) 0%, transparent 70%);
  top: 30%;
  left: -20%;
  animation-delay: 4s;
}

/* 粒子装饰 */
.particle-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1;
}

.decoration-particle {
  position: absolute;
  width: 3px;
  height: 3px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  animation: particleFloat linear infinite;
  box-shadow: 0 0 4px rgba(255, 255, 255, 0.3);
  filter: blur(0.5px);
  opacity: 0.6;
}

/* 边框光效 */
.border-glow {
  position: absolute;
  top: -3px;
  left: -3px;
  right: -3px;
  bottom: -3px;
  border-radius: 27px;
  background: 
    linear-gradient(45deg, 
      transparent 20%, 
      rgba(255, 255, 255, 0.15) 30%,
      var(--color-accent, rgba(103, 232, 249, 0.2)) 50%, 
      rgba(255, 255, 255, 0.15) 70%,
      transparent 80%
    );
  opacity: 0;
  animation: borderSweep 4s ease-in-out infinite;
  z-index: -1;
  filter: blur(1px);
}

.status-active .border-glow {
  opacity: 0.8;
  animation: borderSweep 2s ease-in-out infinite;
}

.form-card:hover .border-glow {
  opacity: 0.6;
  animation: borderSweep 3s ease-in-out infinite;
}

/* 表单内容 */
.form-content {
  position: relative;
  z-index: 2;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

/* 状态指示器 */
.status-indicator {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 12px;
  height: 12px;
  z-index: 3;
}

.status-ring {
  width: 100%;
  height: 100%;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  transition: all 0.3s ease;
}

.status-pulse {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.8);
  opacity: 0;
  transition: all 0.3s ease;
}

.status-active .status-ring {
  border-color: color-mix(in srgb, var(--active-color) 80%, transparent);
  box-shadow: 0 0 8px var(--active-glow);
}

.status-active .status-pulse {
  opacity: 1;
  background: var(--active-color);
  animation: statusPulse 2s ease-in-out infinite;
}

.status-success .status-ring {
  border-color: rgba(16, 185, 129, 0.7);
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.45);
}

.status-success .status-pulse {
  opacity: 1;
  background: rgba(16, 185, 129, 0.9);
}

.status-error .status-ring {
  border-color: rgba(239, 68, 68, 0.7);
  box-shadow: 0 0 8px rgba(239, 68, 68, 0.45);
}

.status-error .status-pulse {
  opacity: 1;
  background: rgba(239, 68, 68, 1);
  animation: errorPulse 0.3s ease-in-out 3;
}

/* 动画定义 */
@keyframes orbFloat {
  0% { transform: translateY(0px) scale(1); }
  100% { transform: translateY(-20px) scale(1.1); }
}

@keyframes particleFloat {
  0% { 
    opacity: 0; 
    transform: translateY(10px) scale(0.5); 
  }
  50% { 
    opacity: 1; 
    transform: translateY(-5px) scale(1); 
  }
  100% { 
    opacity: 0; 
    transform: translateY(-20px) scale(0.5); 
  }
}

@keyframes borderSweep {
  0% { 
    background-position: -300% 0; 
    transform: rotate(0deg);
  }
  25% {
    opacity: 0.6;
  }
  50% { 
    opacity: 1;
    transform: rotate(90deg);
  }
  75% {
    opacity: 0.6;
  }
  100% { 
    background-position: 300% 0; 
    transform: rotate(180deg);
  }
}

@keyframes statusPulse {
  0%, 100% { 
    transform: translate(-50%, -50%) scale(1); 
    opacity: 1; 
  }
  50% { 
    transform: translate(-50%, -50%) scale(1.3); 
    opacity: 0.7; 
  }
}

@keyframes errorPulse {
  0%, 100% { 
    transform: translate(-50%, -50%) scale(1); 
  }
  50% { 
    transform: translate(-50%, -50%) scale(1.2); 
  }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-4px); }
  20%, 40%, 60%, 80% { transform: translateX(4px); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-card-container {
    padding: 1rem;
    min-height: 450px;
  }
  
  .form-card {
    min-height: 480px;
    padding: 2.5rem 2rem;
    border-radius: 20px;
  }
  
  .card-background {
    border-radius: 20px;
  }
  
  .border-glow {
    border-radius: 22px;
  }
  
  .gradient-orb {
    filter: blur(40px);
  }
  
  .particle-decoration {
    display: none;
  }
}

@media (max-width: 480px) {
  .form-card-container {
    padding: 0.5rem;
    min-height: 400px;
  }
  
  .form-card {
    min-height: 420px;
    padding: 2rem 1.5rem;
    border-radius: 16px;
  }
  
  .card-background {
    border-radius: 16px;
  }
  
  .border-glow {
    border-radius: 18px;
  }
  
  .status-indicator {
    top: 15px;
    right: 15px;
    width: 10px;
    height: 10px;
  }
  
  .status-pulse {
    width: 4px;
    height: 4px;
  }
}

/* 性能优化 */
@media (prefers-reduced-motion: reduce) {
  .form-card,
  .gradient-orb,
  .decoration-particle,
  .border-glow,
  .status-pulse {
    animation: none;
  }
  
  .form-card {
    transition: none;
  }
}

/* 硬件加速和性能优化 */
.form-card,
.gradient-orb,
.decoration-particle,
.border-glow {
  will-change: transform, opacity;
  backface-visibility: hidden;
  transform: translateZ(0);
}

.form-card:hover {
  transform: translateY(-2px) translateZ(0);
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.2),
    0 8px 32px rgba(0, 0, 0, 0.15),
    inset 0 2px 0 rgba(255, 255, 255, 1.0);
  border-color: rgba(255, 255, 255, 0.95);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 255, 255, 0.85) 100%);
}
</style> 