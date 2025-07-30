<template>
  <div class="particle-background">
    <!-- 动态粒子层 -->
    <div class="particle-layer">
      <div 
        v-for="particle in particles" 
        :key="particle.id"
        class="particle"
        :class="particle.type"
        :style="{
          left: particle.x + '%',
          top: particle.y + '%',
          animationDelay: particle.delay + 's',
          animationDuration: particle.duration + 's',
          opacity: particle.opacity
        }"
      ></div>
    </div>

    <!-- 连接线效果 -->
    <div class="connection-layer">
      <svg class="connection-svg" width="100%" height="100%">
        <defs>
          <linearGradient id="connectionGradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" :style="{ stopColor: theme.primary, stopOpacity: 0 }" />
            <stop offset="50%" :style="{ stopColor: theme.primary, stopOpacity: 0.3 }" />
            <stop offset="100%" :style="{ stopColor: theme.primary, stopOpacity: 0 }" />
          </linearGradient>
        </defs>
        <g v-for="connection in connections" :key="connection.id">
          <line 
            :x1="connection.x1 + '%'" 
            :y1="connection.y1 + '%'" 
            :x2="connection.x2 + '%'" 
            :y2="connection.y2 + '%'"
            stroke="url(#connectionGradient)"
            :stroke-width="connection.width"
            class="connection-line"
            :style="{
              animationDelay: connection.delay + 's',
              animationDuration: connection.duration + 's'
            }"
          />
        </g>
      </svg>
    </div>

    <!-- 光晕效果 -->
    <div class="glow-layer">
      <div 
        v-for="glow in glows" 
        :key="glow.id"
        class="glow-orb"
        :style="{
          left: glow.x + '%',
          top: glow.y + '%',
          width: glow.size + 'px',
          height: glow.size + 'px',
          background: `radial-gradient(circle, ${glow.color} 0%, transparent 70%)`,
          animationDelay: glow.delay + 's',
          animationDuration: glow.duration + 's'
        }"
      ></div>
    </div>

    <!-- 脉冲波效果 -->
    <div class="pulse-layer">
      <div 
        v-for="pulse in pulses" 
        :key="pulse.id"
        class="pulse-ring"
        :style="{
          left: pulse.x + '%',
          top: pulse.y + '%',
          animationDelay: pulse.delay + 's',
          animationDuration: pulse.duration + 's',
          borderColor: pulse.color
        }"
      ></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'

const props = defineProps({
  theme: {
    type: String,
    default: 'blue',
    validator: (value) => ['blue', 'purple', 'pink'].includes(value)
  },
  density: {
    type: String,
    default: 'medium',
    validator: (value) => ['low', 'medium', 'high'].includes(value)
  },
  animated: {
    type: Boolean,
    default: true
  }
})

const particles = ref([])
const connections = ref([])
const glows = ref([])
const pulses = ref([])

// 主题配置
const themeConfig = computed(() => {
  const themes = {
    blue: {
      primary: '#3B82F6',
      secondary: '#1D4ED8',
      accent: '#60A5FA',
      glow: 'rgba(59, 130, 246, 0.4)'
    },
    purple: {
      primary: '#8B5CF6',
      secondary: '#7C3AED',
      accent: '#A78BFA',
      glow: 'rgba(139, 92, 246, 0.4)'
    },
    pink: {
      primary: '#EC4899',
      secondary: '#DB2777',
      accent: '#F472B6',
      glow: 'rgba(236, 72, 153, 0.4)'
    }
  }
  return themes[props.theme]
})

// 密度配置
const densityConfig = computed(() => {
  const configs = {
    low: { particles: 30, connections: 8, glows: 5, pulses: 3 },
    medium: { particles: 50, connections: 12, glows: 8, pulses: 5 },
    high: { particles: 80, connections: 18, glows: 12, pulses: 8 }
  }
  return configs[props.density]
})

// 生成粒子
const generateParticles = () => {
  const particleArray = []
  const count = densityConfig.value.particles
  
  for (let i = 0; i < count; i++) {
    particleArray.push({
      id: `particle-${i}`,
      x: Math.random() * 100,
      y: Math.random() * 100,
      delay: Math.random() * 5,
      duration: 8 + Math.random() * 12,
      opacity: 0.3 + Math.random() * 0.7,
      type: Math.random() > 0.7 ? 'large' : (Math.random() > 0.5 ? 'medium' : 'small')
    })
  }
  
  particles.value = particleArray
}

// 生成连接线
const generateConnections = () => {
  const connectionArray = []
  const count = densityConfig.value.connections
  
  for (let i = 0; i < count; i++) {
    connectionArray.push({
      id: `connection-${i}`,
      x1: Math.random() * 100,
      y1: Math.random() * 100,
      x2: Math.random() * 100,
      y2: Math.random() * 100,
      width: 1 + Math.random() * 2,
      delay: Math.random() * 3,
      duration: 6 + Math.random() * 8
    })
  }
  
  connections.value = connectionArray
}

// 生成光晕
const generateGlows = () => {
  const glowArray = []
  const count = densityConfig.value.glows
  const theme = themeConfig.value
  
  for (let i = 0; i < count; i++) {
    glowArray.push({
      id: `glow-${i}`,
      x: Math.random() * 100,
      y: Math.random() * 100,
      size: 100 + Math.random() * 200,
      color: Math.random() > 0.5 ? theme.primary : theme.secondary,
      delay: Math.random() * 4,
      duration: 10 + Math.random() * 15
    })
  }
  
  glows.value = glowArray
}

// 生成脉冲
const generatePulses = () => {
  const pulseArray = []
  const count = densityConfig.value.pulses
  const theme = themeConfig.value
  
  for (let i = 0; i < count; i++) {
    pulseArray.push({
      id: `pulse-${i}`,
      x: Math.random() * 100,
      y: Math.random() * 100,
      color: Math.random() > 0.5 ? theme.primary : theme.accent,
      delay: Math.random() * 6,
      duration: 4 + Math.random() * 6
    })
  }
  
  pulses.value = pulseArray
}

// 初始化所有效果
const initializeEffects = () => {
  generateParticles()
  generateConnections()
  generateGlows()
  generatePulses()
}

onMounted(() => {
  initializeEffects()
})
</script>

<style scoped>
.particle-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -1;
  overflow: hidden;
}

.particle-layer,
.connection-layer,
.glow-layer,
.pulse-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

/* 粒子样式 */
.particle {
  position: absolute;
  background: v-bind('themeConfig.primary');
  border-radius: 50%;
  animation: particleFloat linear infinite;
  box-shadow: 0 0 4px v-bind('themeConfig.glow');
}

.particle.small {
  width: 2px;
  height: 2px;
}

.particle.medium {
  width: 3px;
  height: 3px;
  background: v-bind('themeConfig.accent');
}

.particle.large {
  width: 4px;
  height: 4px;
  background: v-bind('themeConfig.secondary');
  box-shadow: 0 0 8px v-bind('themeConfig.glow');
}

/* 连接线样式 */
.connection-svg {
  position: absolute;
  top: 0;
  left: 0;
}

.connection-line {
  animation: connectionPulse linear infinite;
}

/* 光晕样式 */
.glow-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(30px);
  opacity: 0.3;
  animation: glowFloat ease-in-out infinite alternate;
}

/* 脉冲样式 */
.pulse-ring {
  position: absolute;
  width: 20px;
  height: 20px;
  border: 2px solid;
  border-radius: 50%;
  animation: pulseExpand ease-out infinite;
}

/* 动画定义 */
@keyframes particleFloat {
  0% {
    transform: translateY(100vh) translateX(0px) scale(0);
    opacity: 0;
  }
  10% {
    opacity: 1;
    transform: translateY(90vh) translateX(10px) scale(1);
  }
  90% {
    opacity: 1;
    transform: translateY(10vh) translateX(-10px) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateY(-10vh) translateX(0px) scale(0);
  }
}

@keyframes connectionPulse {
  0%, 100% {
    opacity: 0.2;
    stroke-dasharray: 0, 100;
  }
  50% {
    opacity: 0.8;
    stroke-dasharray: 50, 50;
  }
}

@keyframes glowFloat {
  0% {
    transform: translateY(0px) scale(1);
    opacity: 0.2;
  }
  100% {
    transform: translateY(-30px) scale(1.1);
    opacity: 0.4;
  }
}

@keyframes pulseExpand {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(4);
    opacity: 0;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .particle.large {
    width: 3px;
    height: 3px;
  }
  
  .particle.medium {
    width: 2px;
    height: 2px;
  }
  
  .particle.small {
    width: 1px;
    height: 1px;
  }
  
  .glow-orb {
    filter: blur(20px);
    opacity: 0.2;
  }
  
  .pulse-ring {
    width: 15px;
    height: 15px;
  }
}

/* 性能优化 */
@media (prefers-reduced-motion: reduce) {
  .particle,
  .connection-line,
  .glow-orb,
  .pulse-ring {
    animation: none;
  }
  
  .particle {
    opacity: 0.5;
  }
  
  .connection-line {
    opacity: 0.3;
  }
  
  .glow-orb {
    opacity: 0.2;
  }
  
  .pulse-ring {
    display: none;
  }
}

/* 硬件加速 */
.particle,
.glow-orb,
.pulse-ring {
  will-change: transform, opacity;
  backface-visibility: hidden;
  transform: translateZ(0);
}
</style>