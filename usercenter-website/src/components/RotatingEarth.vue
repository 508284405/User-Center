<template>
  <div 
    ref="earthContainer"
    class="earth-container" 
    @mouseenter="handleMouseEnter" 
    @mouseleave="handleMouseLeave"
    @mousedown="handleMouseDown"
    @wheel="handleWheel"
    @touchstart.passive="handleTouchStart"
    @touchmove.passive="handleTouchMove"
    @touchend.passive="handleTouchEnd"
  >
    <!-- 星空背景 -->
    <div class="stars-background" v-if="showStars">
      <div 
        v-for="star in stars" 
        :key="star.id"
        class="star"
        :style="{
          left: star.x + '%',
          top: star.y + '%',
          animationDelay: star.delay + 's',
          animationDuration: star.duration + 's',
          opacity: star.opacity
        }"
      ></div>
    </div>

    <!-- 3D地球主体 -->
    <div class="earth-scene">
      <div 
        ref="earthSphere"
        class="earth-sphere" 
        :class="{ 'hovered': isHovered, 'dragging': isDragging }"
        :style="earthTransform"
      >
        <!-- 3D球体切片 -->
        <div 
          v-for="(slice, index) in sphereSlices" 
          :key="`slice-${index}`"
          class="sphere-slice"
          :style="getSliceStyle(slice, index)"
        >
          <!-- 地球表面纹理 -->
          <div class="slice-surface" :style="getSliceTexture(slice, index)">
            <!-- 大陆层 -->
            <div class="slice-continents" :style="getContinentTexture(slice, index)"></div>
            <!-- 海洋层 -->
            <div class="slice-ocean" :style="getOceanTexture(slice, index)"></div>
            <!-- 城市灯光 -->
            <div class="slice-lights" :class="{ 'visible': showCityLights }" :style="getCityLightsTexture(slice, index)"></div>
          </div>
          
          <!-- 大气层效果 -->
          <div class="slice-atmosphere" :style="getAtmosphereStyle(slice, index)"></div>
        </div>

        <!-- 外层光晕 -->
        <div class="outer-glow"></div>
      </div>

      <!-- 地球阴影 -->
      <div class="earth-shadow" :style="shadowTransform"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  showStars: {
    type: Boolean,
    default: true
  },
  animationSpeed: {
    type: Number,
    default: 25
  },
  autoRotate: {
    type: Boolean,
    default: true
  },
  enableCityLights: {
    type: Boolean,
    default: true
  },
  pauseOnInteraction: {
    type: Boolean,
    default: true
  }
})

const isHovered = ref(false)
const stars = ref([])
const sphereSlices = ref([])

// 交互控制状态
const isDragging = ref(false)
const rotationX = ref(-10)
const rotationY = ref(0)
const autoRotationY = ref(0)
const scale = ref(1)
const lastMouseX = ref(0)
const lastMouseY = ref(0)
const velocityX = ref(0)
const velocityY = ref(0)
const inertiaAnimation = ref(null)
const autoRotationAnimation = ref(null)
const lastInteractionTime = ref(0)
const interactionTimeout = ref(null)

// 光照系统
const lightAngle = ref(0)
const lightDirection = ref({ x: 0.5, y: 0.3, z: 0.8 })
const showCityLights = ref(false)

// 地球容器引用
const earthContainer = ref(null)
const earthSphere = ref(null)

// 创建3D球体切片几何
const createSphere3D = () => {
  const slices = []
  const latSegments = 16 // 纬度分段
  const lonSegments = 32 // 经度分段
  const radius = 225 // 半径

  for (let lat = 0; lat < latSegments; lat++) {
    for (let lon = 0; lon < lonSegments; lon++) {
      // 使用更精确的球面坐标计算
      const latAngle = (lat / (latSegments - 1)) * Math.PI
      const lonAngle = (lon / lonSegments) * Math.PI * 2
      
      const x = Math.sin(latAngle) * Math.cos(lonAngle)
      const y = Math.cos(latAngle)
      const z = Math.sin(latAngle) * Math.sin(lonAngle)
      
      slices.push({
        id: lat * lonSegments + lon,
        lat: lat,
        lon: lon,
        latAngle: latAngle,
        lonAngle: lonAngle,
        x: x * radius,
        y: y * radius,
        z: z * radius,
        normalX: x,
        normalY: y,
        normalZ: z,
        u: lon / lonSegments,
        v: lat / (latSegments - 1)
      })
    }
  }
  
  sphereSlices.value = slices
}

// 计算切片样式
const getSliceStyle = (slice, index) => {
  const rotateX = Math.asin(slice.normalY) * (180 / Math.PI)
  const rotateY = Math.atan2(slice.normalX, slice.normalZ) * (180 / Math.PI)
  
  // 根据球体半径动态计算切片大小，使球体更圆润
  const sliceSize = Math.max(15, 225 / 16) // 最小15px，根据球体半径和分割数计算大小
  
  return {
    transform: `
      translate3d(${slice.x}px, ${slice.y}px, ${slice.z}px)
      rotateY(${rotateY}deg)
      rotateX(${rotateX}deg)
    `,
    width: `${sliceSize}px`,
    height: `${sliceSize}px`
  }
}

// 计算光照强度
const calculateLighting = (slice) => {
  const currentRotationY = (autoRotationY.value + rotationY.value) * Math.PI / 180
  
  // 旋转法向量
  const rotatedNormalX = slice.normalX * Math.cos(currentRotationY) - slice.normalZ * Math.sin(currentRotationY)
  const rotatedNormalZ = slice.normalX * Math.sin(currentRotationY) + slice.normalZ * Math.cos(currentRotationY)
  
  // 计算光照角度
  const dotProduct = rotatedNormalX * lightDirection.value.x + 
                   slice.normalY * lightDirection.value.y + 
                   rotatedNormalZ * lightDirection.value.z
  
  const lightIntensity = Math.max(0, dotProduct)
  const ambientLight = 0.2
  
  return Math.min(1, lightIntensity + ambientLight)
}

// 获取切片纹理
const getSliceTexture = (slice, index) => {
  const lighting = calculateLighting(slice)
  
  return {
    background: `
      radial-gradient(circle at 50% 50%, 
        rgba(44, 110, 142, ${0.3 + lighting * 0.7}) 0%, 
        rgba(26, 95, 122, ${0.4 + lighting * 0.6}) 50%, 
        rgba(13, 79, 108, ${0.5 + lighting * 0.5}) 100%
      )
    `,
    opacity: 0.8 + lighting * 0.2
  }
}

// 获取大陆纹理
const getContinentTexture = (slice, index) => {
  const lighting = calculateLighting(slice)
  const u = slice.u
  const v = slice.v
  
  // 简化的大陆分布
  let isLand = false
  
  // 北美洲
  if (u > 0.15 && u < 0.35 && v > 0.25 && v < 0.55) isLand = true
  // 南美洲  
  if (u > 0.2 && u < 0.32 && v > 0.55 && v < 0.85) isLand = true
  // 非洲
  if (u > 0.45 && u < 0.6 && v > 0.3 && v < 0.8) isLand = true
  // 欧亚大陆
  if (u > 0.5 && u < 0.9 && v > 0.15 && v < 0.5) isLand = true
  // 澳洲
  if (u > 0.7 && u < 0.82 && v > 0.65 && v < 0.8) isLand = true
  
  if (!isLand) return { opacity: 0 }
  
  const landLighting = lighting * 0.8 + 0.2
  
  return {
    background: `
      radial-gradient(circle at 50% 50%, 
        rgba(74, 117, 88, ${landLighting}) 0%, 
        rgba(58, 101, 72, ${landLighting * 0.9}) 60%, 
        rgba(42, 85, 56, ${landLighting * 0.8}) 100%
      )
    `,
    opacity: landLighting
  }
}

// 获取海洋纹理
const getOceanTexture = (slice, index) => {
  const lighting = calculateLighting(slice)
  const oceanLighting = lighting * 0.6 + 0.3
  
  return {
    background: `
      radial-gradient(circle at 30% 30%, 
        rgba(100, 200, 255, ${oceanLighting * 0.4}) 0%, 
        rgba(0, 150, 220, ${oceanLighting * 0.3}) 50%, 
        transparent 80%
      )
    `,
    opacity: oceanLighting * 0.5
  }
}

// 获取城市灯光纹理
const getCityLightsTexture = (slice, index) => {
  const lighting = calculateLighting(slice)
  const u = slice.u
  const v = slice.v
  
  // 只在背光面显示城市灯光
  if (lighting > 0.3) return { opacity: 0 }
  
  // 简化的城市分布
  let hasCity = false
  if (u > 0.2 && u < 0.3 && v > 0.35 && v < 0.45) hasCity = true // 北美东海岸
  if (u > 0.48 && u < 0.52 && v > 0.28 && v < 0.32) hasCity = true // 欧洲
  if (u > 0.6 && u < 0.7 && v > 0.3 && v < 0.4) hasCity = true // 亚洲
  
  if (!hasCity) return { opacity: 0 }
  
  return {
    background: `
      radial-gradient(circle at 50% 50%, 
        rgba(255, 221, 68, 0.8) 0%, 
        rgba(255, 170, 34, 0.6) 50%, 
        transparent 80%
      )
    `,
    opacity: (0.3 - lighting) * 2
  }
}

// 获取大气层样式
const getAtmosphereStyle = (slice, index) => {
  const lighting = calculateLighting(slice)
  
  return {
    background: `
      radial-gradient(circle at 50% 50%, 
        rgba(135, 206, 250, ${lighting * 0.2}) 0%, 
        rgba(100, 150, 255, ${lighting * 0.1}) 70%, 
        transparent 100%
      )
    `,
    opacity: lighting * 0.3
  }
}

// 计算属性
const earthTransform = computed(() => {
  const totalRotationY = autoRotationY.value + rotationY.value
  return {
    transform: `rotateX(${rotationX.value}deg) rotateY(${totalRotationY}deg) scale(${scale.value})`,
    transition: isDragging.value ? 'none' : 'transform 0.3s cubic-bezier(0.4, 0, 0.2, 1)'
  }
})

const shadowTransform = computed(() => {
  const shadowScale = 1 + (scale.value - 1) * 0.5
  return {
    transform: `translateX(-50%) scale(${shadowScale})`,
    opacity: Math.max(0.3, 0.8 - (scale.value - 1) * 0.3)
  }
})

// 事件处理函数
const handleMouseEnter = () => {
  isHovered.value = true
}

const handleMouseLeave = () => {
  isHovered.value = false
  if (isDragging.value) {
    handleMouseUp()
  }
}

// 自动旋转控制
const startAutoRotation = () => {
  if (!props.autoRotate) return
  
  const animate = () => {
    if (!isDragging.value && props.autoRotate) {
      const now = Date.now()
      if (now - lastInteractionTime.value > 2000) {
        autoRotationY.value += 0.2
        lightAngle.value = (autoRotationY.value + rotationY.value) * Math.PI / 180
        
        // 更新光照方向
        lightDirection.value = {
          x: Math.cos(lightAngle.value) * 0.8,
          y: 0.3,
          z: Math.sin(lightAngle.value) * 0.8
        }
        
        if (props.enableCityLights) {
          const lightSide = Math.cos(lightAngle.value)
          showCityLights.value = lightSide < -0.2
        }
      }
    }
    autoRotationAnimation.value = requestAnimationFrame(animate)
  }
  
  autoRotationAnimation.value = requestAnimationFrame(animate)
}

const stopAutoRotation = () => {
  if (autoRotationAnimation.value) {
    cancelAnimationFrame(autoRotationAnimation.value)
    autoRotationAnimation.value = null
  }
}

const recordInteraction = () => {
  lastInteractionTime.value = Date.now()
  
  if (interactionTimeout.value) {
    clearTimeout(interactionTimeout.value)
  }
  
  interactionTimeout.value = setTimeout(() => {
    if (!isDragging.value && props.pauseOnInteraction) {
      const targetRotationX = -10
      animateToRotation(targetRotationX, rotationX.value)
    }
  }, 3000)
}

const animateToRotation = (targetX, currentX) => {
  const startX = currentX
  const diffX = targetX - startX
  const startTime = Date.now()
  const duration = 2000
  
  const animate = () => {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    
    if (progress < 1 && !isDragging.value) {
      rotationX.value = startX + diffX * eased
      requestAnimationFrame(animate)
    } else if (progress >= 1) {
      rotationX.value = targetX
    }
  }
  
  animate()
}

// 鼠标拖拽事件处理
const handleMouseDown = (event) => {
  event.preventDefault()
  isDragging.value = true
  lastMouseX.value = event.clientX
  lastMouseY.value = event.clientY
  velocityX.value = 0
  velocityY.value = 0
  
  recordInteraction()
  
  if (inertiaAnimation.value) {
    cancelAnimationFrame(inertiaAnimation.value)
    inertiaAnimation.value = null
  }
  
  document.addEventListener('mousemove', handleMouseMove, { passive: false })
  document.addEventListener('mouseup', handleMouseUp)
}

const handleMouseMove = (event) => {
  if (!isDragging.value) return
  
  event.preventDefault()
  const deltaX = event.clientX - lastMouseX.value
  const deltaY = event.clientY - lastMouseY.value
  
  const sensitivity = 0.8
  rotationY.value += deltaX * sensitivity
  rotationX.value -= deltaY * sensitivity
  
  rotationX.value = Math.max(-85, Math.min(85, rotationX.value))
  
  velocityX.value = deltaX * sensitivity * 0.8
  velocityY.value = -deltaY * sensitivity * 0.8
  
  lastMouseX.value = event.clientX
  lastMouseY.value = event.clientY
  
  recordInteraction()
}

const handleMouseUp = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
  
  startInertia()
}

// 鼠标滚轮缩放
const handleWheel = (event) => {
  event.preventDefault()
  
  const delta = event.deltaY > 0 ? -0.15 : 0.15
  const newScale = Math.max(0.6, Math.min(3.0, scale.value + delta))
  
  animateScale(scale.value, newScale)
  recordInteraction()
}

const animateScale = (fromScale, toScale) => {
  const startTime = Date.now()
  const duration = 200
  const startScale = fromScale
  const scaleChange = toScale - fromScale
  
  const animate = () => {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 2)
    
    scale.value = startScale + scaleChange * eased
    
    if (progress < 1) {
      requestAnimationFrame(animate)
    }
  }
  
  animate()
}

// 惯性滑动效果
const startInertia = () => {
  const friction = 0.96
  const threshold = 0.05
  
  const animate = () => {
    if (Math.abs(velocityX.value) < threshold && Math.abs(velocityY.value) < threshold) {
      inertiaAnimation.value = null
      return
    }
    
    rotationY.value += velocityX.value
    rotationX.value += velocityY.value
    
    rotationX.value = Math.max(-85, Math.min(85, rotationX.value))
    
    velocityX.value *= friction
    velocityY.value *= friction
    
    inertiaAnimation.value = requestAnimationFrame(animate)
  }
  
  if (Math.abs(velocityX.value) > threshold || Math.abs(velocityY.value) > threshold) {
    animate()
  }
}

// 触摸事件支持
const handleTouchStart = (event) => {
  if (event.touches.length === 1) {
    const touch = event.touches[0]
    isDragging.value = true
    lastMouseX.value = touch.clientX
    lastMouseY.value = touch.clientY
    velocityX.value = 0
    velocityY.value = 0
    
    recordInteraction()
    
    if (inertiaAnimation.value) {
      cancelAnimationFrame(inertiaAnimation.value)
      inertiaAnimation.value = null
    }
  }
}

const handleTouchMove = (event) => {
  if (!isDragging.value || event.touches.length !== 1) return
  
  const touch = event.touches[0]
  const deltaX = touch.clientX - lastMouseX.value
  const deltaY = touch.clientY - lastMouseY.value
  
  const sensitivity = 0.6
  rotationY.value += deltaX * sensitivity
  rotationX.value -= deltaY * sensitivity
  
  rotationX.value = Math.max(-85, Math.min(85, rotationX.value))
  
  velocityX.value = deltaX * sensitivity * 0.7
  velocityY.value = -deltaY * sensitivity * 0.7
  
  lastMouseX.value = touch.clientX
  lastMouseY.value = touch.clientY
  
  recordInteraction()
}

const handleTouchEnd = () => {
  isDragging.value = false
  startInertia()
}

// 生成随机星星
const generateStars = () => {
  const starCount = 200
  const starArray = []
  
  for (let i = 0; i < starCount; i++) {
    starArray.push({
      id: i,
      x: Math.random() * 100,
      y: Math.random() * 100,
      delay: Math.random() * 5,
      duration: 3 + Math.random() * 4,
      opacity: 0.3 + Math.random() * 0.7
    })
  }
  
  stars.value = starArray
}

onMounted(() => {
  createSphere3D()
  generateStars()
  startAutoRotation()
  
  lastInteractionTime.value = Date.now() - 3000
})

onUnmounted(() => {
  if (inertiaAnimation.value) {
    cancelAnimationFrame(inertiaAnimation.value)
  }
  
  stopAutoRotation()
  
  if (interactionTimeout.value) {
    clearTimeout(interactionTimeout.value)
  }
  
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
})
</script>

<style scoped>
/* 容器和场景设置 */
.earth-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  perspective: 1200px;
  overflow: hidden;
  /* 添加3D变换样式 */
  transform-style: preserve-3d;
}

.earth-scene {
  position: relative;
  transform-style: preserve-3d;
  /* 添加透视效果 */
  perspective: 1000px;
}

/* 星空背景 */
.stars-background {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: -1;
}

.star {
  position: absolute;
  width: 1px;
  height: 1px;
  background: #ffffff;
  border-radius: 50%;
  animation: starTwinkle linear infinite;
  box-shadow: 0 0 2px rgba(255, 255, 255, 0.6);
}

.star:nth-child(3n) {
  width: 2px;
  height: 2px;
  background: #e6f3ff;
  box-shadow: 0 0 4px rgba(230, 243, 255, 0.8);
}

.star:nth-child(5n) {
  width: 1.5px;
  height: 1.5px;
  background: #fff2e6;
  box-shadow: 0 0 3px rgba(255, 242, 230, 0.7);
}

/* 3D地球主体 */
.earth-sphere {
  position: relative;
  width: 450px;
  height: 450px;
  transform-style: preserve-3d;
  will-change: transform;
  filter: 
    drop-shadow(0 0 80px rgba(0, 120, 220, 0.5))
    drop-shadow(0 30px 60px rgba(0, 0, 0, 0.4));
  cursor: grab;
  /* 添加3D透视效果 */
  perspective: 1000px;
}

.earth-sphere.hovered {
  filter: 
    drop-shadow(0 0 100px rgba(0, 150, 255, 0.6))
    drop-shadow(0 30px 60px rgba(0, 0, 0, 0.4));
}

.earth-sphere.dragging {
  cursor: grabbing;
}

/* 3D球体切片 */
.sphere-slice {
  position: absolute;
  left: 50%;
  top: 50%;
  transform-style: preserve-3d;
  border-radius: 50%;
  will-change: transform;
  backface-visibility: hidden;
  /* 根据动态计算的切片大小设置负边距 */
  margin-left: calc(v-bind('getSliceStyle(sphereSlices[0], 0).width') / -2);
  margin-top: calc(v-bind('getSliceStyle(sphereSlices[0], 0).height') / -2);
}

/* 切片表面 */
.slice-surface {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  overflow: hidden;
  /* 添加3D变换 */
  transform-style: preserve-3d;
}

.slice-continents,
.slice-ocean,
.slice-lights {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.slice-lights {
  opacity: 0;
  transition: opacity 1s ease-in-out;
}

.slice-lights.visible {
  opacity: 1;
}

/* 切片大气层 */
.slice-atmosphere {
  position: absolute;
  width: 120%;
  height: 120%;
  top: -10%;
  left: -10%;
  border-radius: 50%;
  pointer-events: none;
}

/* 外层光晕 */
.outer-glow {
  position: absolute;
  width: 130%;
  height: 130%;
  top: -15%;
  left: -15%;
  border-radius: 50%;
  background: 
    radial-gradient(circle at center, 
      transparent 60%, 
      rgba(0, 150, 255, 0.2) 72%, 
      rgba(0, 120, 220, 0.12) 82%, 
      rgba(0, 100, 200, 0.06) 90%, 
      transparent 100%
    );
  animation: outerGlowPulse 8s ease-in-out infinite alternate;
  filter: blur(1px);
  pointer-events: none;
}

/* 地球阴影 */
.earth-shadow {
  position: absolute;
  bottom: -25px;
  left: 50%;
  transform: translateX(-50%);
  width: 350px;
  height: 80px;
  background: 
    radial-gradient(ellipse 350px 80px at center, 
      rgba(0, 30, 60, 0.4) 0%, 
      rgba(0, 20, 40, 0.25) 30%, 
      rgba(0, 10, 25, 0.1) 60%, 
      transparent 80%
    );
  border-radius: 50%;
  animation: shadowPulse 8s ease-in-out infinite alternate;
  filter: blur(25px);
}

/* 动画定义 */
@keyframes starTwinkle {
  0%, 100% { opacity: 0.4; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.3); }
}

@keyframes outerGlowPulse {
  0% { opacity: 0.5; transform: scale(1); }
  100% { opacity: 0.8; transform: scale(1.05); }
}

@keyframes shadowPulse {
  0% { opacity: 0.6; transform: translateX(-50%) scale(1); }
  100% { opacity: 0.9; transform: translateX(-50%) scale(1.1); }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .earth-sphere {
    width: 380px;
    height: 380px;
  }
}

@media (max-width: 768px) {
  .earth-sphere {
    width: 300px;
    height: 300px;
  }
  
  .stars-background {
    display: none;
  }
  
  .earth-shadow {
    width: 200px;
    height: 40px;
  }
}

@media (max-width: 480px) {
  .earth-sphere {
    width: 240px;
    height: 240px;
  }
  
  .earth-shadow {
    width: 160px;
    height: 30px;
  }
}

/* 性能优化 */
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
  
  .earth-sphere.hovered {
    transform: scale(1.02);
    transition: transform 0.1s ease;
  }
}

/* 硬件加速优化 */
.earth-sphere,
.sphere-slice,
.slice-surface,
.slice-continents,
.slice-ocean,
.slice-lights,
.slice-atmosphere,
.outer-glow {
  will-change: transform;
  backface-visibility: hidden;
  transform: translateZ(0);
}
</style>