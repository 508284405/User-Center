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
      class="form-card"
      :class="[`status-${status}`, `type-${codeType}`]"
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
  background: 
    linear-gradient(135deg, 
      rgba(255, 255, 255, 0.1) 0%, 
      rgba(255, 255, 255, 0.05) 50%, 
      rgba(255, 255, 255, 0.02) 100%
    );
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 24px;
  padding: 3rem 2.5rem;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 2px 16px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
  overflow: hidden;
  will-change: transform, box-shadow;
}

/* 状态样式 */
.status-idle {
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 2px 16px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.status-active.type-login {
  border-color: rgba(139, 92, 246, 0.4);
  box-shadow: 
    0 8px 32px rgba(139, 92, 246, 0.2),
    0 2px 16px rgba(139, 92, 246, 0.15),
    0 0 0 1px rgba(139, 92, 246, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.status-active.type-register {
  border-color: rgba(236, 72, 153, 0.4);
  box-shadow: 
    0 8px 32px rgba(236, 72, 153, 0.2),
    0 2px 16px rgba(236, 72, 153, 0.15),
    0 0 0 1px rgba(236, 72, 153, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.status-success {
  border-color: rgba(16, 185, 129, 0.4);
  box-shadow: 
    0 8px 32px rgba(16, 185, 129, 0.2),
    0 2px 16px rgba(16, 185, 129, 0.15),
    0 0 0 1px rgba(16, 185, 129, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
  transform: scale(1.02);
}

.status-error {
  border-color: rgba(239, 68, 68, 0.4);
  box-shadow: 
    0 8px 32px rgba(239, 68, 68, 0.2),
    0 2px 16px rgba(239, 68, 68, 0.15),
    0 0 0 1px rgba(239, 68, 68, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
  transform: scale(0.98);
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
  opacity: 0.3;
  animation: orbFloat 8s ease-in-out infinite alternate;
}

.type-login .orb-1 {
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.6) 0%, transparent 70%);
  top: -20%;
  right: -10%;
  animation-delay: 0s;
}

.type-login .orb-2 {
  width: 80px;
  height: 80px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.4) 0%, transparent 70%);
  bottom: -15%;
  left: -5%;
  animation-delay: 2s;
}

.type-login .orb-3 {
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.3) 0%, transparent 70%);
  top: 30%;
  left: -20%;
  animation-delay: 4s;
}

.type-register .orb-1 {
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(236, 72, 153, 0.6) 0%, transparent 70%);
  top: -20%;
  right: -10%;
  animation-delay: 0s;
}

.type-register .orb-2 {
  width: 80px;
  height: 80px;
  background: radial-gradient(circle, rgba(244, 114, 182, 0.4) 0%, transparent 70%);
  bottom: -15%;
  left: -5%;
  animation-delay: 2s;
}

.type-register .orb-3 {
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(251, 146, 60, 0.3) 0%, transparent 70%);
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
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
  animation: particleFloat linear infinite;
  box-shadow: 0 0 6px rgba(255, 255, 255, 0.4);
}

/* 边框光效 */
.border-glow {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 26px;
  background: 
    linear-gradient(45deg, 
      transparent 30%, 
      rgba(255, 255, 255, 0.1) 50%, 
      transparent 70%
    );
  opacity: 0;
  animation: borderSweep 3s ease-in-out infinite;
  z-index: -1;
}

.status-active .border-glow {
  opacity: 1;
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
  border-color: rgba(139, 92, 246, 0.8);
  box-shadow: 0 0 8px rgba(139, 92, 246, 0.6);
}

.status-active .status-pulse {
  opacity: 1;
  background: rgba(139, 92, 246, 1);
  animation: statusPulse 2s ease-in-out infinite;
}

.status-success .status-ring {
  border-color: rgba(16, 185, 129, 0.8);
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.6);
}

.status-success .status-pulse {
  opacity: 1;
  background: rgba(16, 185, 129, 1);
}

.status-error .status-ring {
  border-color: rgba(239, 68, 68, 0.8);
  box-shadow: 0 0 8px rgba(239, 68, 68, 0.6);
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
    background-position: -200% 0; 
    opacity: 0; 
  }
  50% { 
    opacity: 1; 
  }
  100% { 
    background-position: 200% 0; 
    opacity: 0; 
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

/* 硬件加速 */
.form-card,
.gradient-orb,
.decoration-particle,
.border-glow {
  will-change: transform;
  backface-visibility: hidden;
  transform: translateZ(0);
}
</style> 