// 粒子系统管理器
export class ParticleSystemManager {
  constructor(canvas, options = {}) {
    this.canvas = canvas
    this.ctx = canvas.getContext('2d')
    this.particles = []
    this.animationId = null
    
    // 默认配置
    this.config = {
      particleCount: window.innerWidth > 768 ? 150 : 80,
      maxDistance: window.innerWidth > 768 ? 120 : 80,
      particleSize: { min: 1, max: 3 },
      speed: { min: 0.2, max: 0.8 },
      opacity: { min: 0.2, max: 0.6 },
      colors: {
        particle: '#A78BFA',
        connection: '#8B5CF6'
      },
      connectionOpacity: 0.3,
      ...options
    }
    
    this.isRunning = false
    this.mousePosition = { x: 0, y: 0 }
    this.mouseInfluence = 50
    
    this.init()
  }
  
  // 粒子类
  createParticle(x, y) {
    return {
      x: x || Math.random() * this.canvas.width,
      y: y || Math.random() * this.canvas.height,
      vx: (Math.random() - 0.5) * (this.config.speed.max - this.config.speed.min) + this.config.speed.min,
      vy: (Math.random() - 0.5) * (this.config.speed.max - this.config.speed.min) + this.config.speed.min,
      size: Math.random() * (this.config.particleSize.max - this.config.particleSize.min) + this.config.particleSize.min,
      opacity: Math.random() * (this.config.opacity.max - this.config.opacity.min) + this.config.opacity.min,
      originalVx: 0,
      originalVy: 0
    }
  }
  
  // 初始化粒子系统
  init() {
    this.particles = []
    for (let i = 0; i < this.config.particleCount; i++) {
      const particle = this.createParticle()
      particle.originalVx = particle.vx
      particle.originalVy = particle.vy
      this.particles.push(particle)
    }
  }
  
  // 更新粒子位置
  updateParticle(particle) {
    // 鼠标影响效果
    const dx = this.mousePosition.x - particle.x
    const dy = this.mousePosition.y - particle.y
    const distance = Math.sqrt(dx * dx + dy * dy)
    
    if (distance < this.mouseInfluence) {
      const force = (this.mouseInfluence - distance) / this.mouseInfluence
      particle.vx += (dx / distance) * force * 0.1
      particle.vy += (dy / distance) * force * 0.1
    } else {
      // 恢复原始速度
      particle.vx += (particle.originalVx - particle.vx) * 0.05
      particle.vy += (particle.originalVy - particle.vy) * 0.05
    }
    
    // 更新位置
    particle.x += particle.vx
    particle.y += particle.vy
    
    // 边界检测和反弹
    if (particle.x <= 0 || particle.x >= this.canvas.width) {
      particle.vx *= -1
      particle.originalVx *= -1
      particle.x = Math.max(0, Math.min(this.canvas.width, particle.x))
    }
    
    if (particle.y <= 0 || particle.y >= this.canvas.height) {
      particle.vy *= -1
      particle.originalVy *= -1
      particle.y = Math.max(0, Math.min(this.canvas.height, particle.y))
    }
  }
  
  // 绘制单个粒子
  drawParticle(particle) {
    this.ctx.save()
    this.ctx.globalAlpha = particle.opacity
    this.ctx.fillStyle = this.config.colors.particle
    this.ctx.beginPath()
    this.ctx.arc(particle.x, particle.y, particle.size, 0, Math.PI * 2)
    this.ctx.fill()
    this.ctx.restore()
  }
  
  // 计算两点距离
  getDistance(p1, p2) {
    const dx = p1.x - p2.x
    const dy = p1.y - p2.y
    return Math.sqrt(dx * dx + dy * dy)
  }
  
  // 绘制粒子连线
  drawConnections() {
    for (let i = 0; i < this.particles.length; i++) {
      for (let j = i + 1; j < this.particles.length; j++) {
        const distance = this.getDistance(this.particles[i], this.particles[j])
        
        if (distance < this.config.maxDistance) {
          const opacity = (1 - distance / this.config.maxDistance) * this.config.connectionOpacity
          
          this.ctx.save()
          this.ctx.globalAlpha = opacity
          this.ctx.strokeStyle = this.config.colors.connection
          this.ctx.lineWidth = 1
          this.ctx.beginPath()
          this.ctx.moveTo(this.particles[i].x, this.particles[i].y)
          this.ctx.lineTo(this.particles[j].x, this.particles[j].y)
          this.ctx.stroke()
          this.ctx.restore()
        }
      }
    }
  }
  
  // 添加鼠标跟踪粒子
  addMouseParticle() {
    if (this.mousePosition.x && this.mousePosition.y) {
      const mouseParticle = {
        x: this.mousePosition.x,
        y: this.mousePosition.y,
        size: 3,
        opacity: 0.8
      }
      
      // 绘制鼠标粒子
      this.drawParticle(mouseParticle)
      
      // 连接到附近的粒子
      this.particles.forEach(particle => {
        const distance = this.getDistance(mouseParticle, particle)
        if (distance < this.config.maxDistance * 1.5) {
          const opacity = (1 - distance / (this.config.maxDistance * 1.5)) * 0.5
          
          this.ctx.save()
          this.ctx.globalAlpha = opacity
          this.ctx.strokeStyle = this.config.colors.connection
          this.ctx.lineWidth = 2
          this.ctx.beginPath()
          this.ctx.moveTo(mouseParticle.x, mouseParticle.y)
          this.ctx.lineTo(particle.x, particle.y)
          this.ctx.stroke()
          this.ctx.restore()
        }
      })
    }
  }
  
  // 主动画循环
  animate() {
    if (!this.isRunning) return
    
    // 清除画布
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height)
    
    // 更新和绘制所有粒子
    this.particles.forEach(particle => {
      this.updateParticle(particle)
      this.drawParticle(particle)
    })
    
    // 绘制连线
    this.drawConnections()
    
    // 添加鼠标交互效果
    this.addMouseParticle()
    
    this.animationId = requestAnimationFrame(() => this.animate())
  }
  
  // 启动动画
  start() {
    if (!this.isRunning) {
      this.isRunning = true
      this.animate()
    }
  }
  
  // 停止动画
  stop() {
    this.isRunning = false
    if (this.animationId) {
      cancelAnimationFrame(this.animationId)
      this.animationId = null
    }
  }
  
  // 更新鼠标位置
  updateMousePosition(x, y) {
    this.mousePosition.x = x
    this.mousePosition.y = y
  }
  
  // 调整画布大小
  resize(width, height) {
    this.canvas.width = width
    this.canvas.height = height
    
    // 重新计算配置参数
    this.config.particleCount = width > 768 ? 150 : 80
    this.config.maxDistance = width > 768 ? 120 : 80
    
    // 重新初始化粒子，但保持现有粒子在边界内
    this.particles = this.particles.slice(0, this.config.particleCount)
    
    // 确保现有粒子在新边界内
    this.particles.forEach(particle => {
      particle.x = Math.min(particle.x, width)
      particle.y = Math.min(particle.y, height)
    })
    
    // 如果需要更多粒子，添加新粒子
    while (this.particles.length < this.config.particleCount) {
      this.particles.push(this.createParticle())
    }
  }
  
  // 更新配置
  updateConfig(newConfig) {
    this.config = { ...this.config, ...newConfig }
    if (newConfig.particleCount && newConfig.particleCount !== this.particles.length) {
      this.init()
    }
  }
  
  // 清理资源
  destroy() {
    this.stop()
    this.particles = []
    this.ctx = null
    this.canvas = null
  }
}

// 碰撞检测工具函数
export const CollisionDetection = {
  // 圆形碰撞检测
  circleCollision(circle1, circle2) {
    const dx = circle1.x - circle2.x
    const dy = circle1.y - circle2.y
    const distance = Math.sqrt(dx * dx + dy * dy)
    return distance < (circle1.radius + circle2.radius)
  },
  
  // 点与圆形碰撞检测
  pointInCircle(point, circle) {
    const dx = point.x - circle.x
    const dy = point.y - circle.y
    const distance = Math.sqrt(dx * dx + dy * dy)
    return distance < circle.radius
  },
  
  // 矩形碰撞检测
  rectCollision(rect1, rect2) {
    return rect1.x < rect2.x + rect2.width &&
           rect1.x + rect1.width > rect2.x &&
           rect1.y < rect2.y + rect2.height &&
           rect1.y + rect1.height > rect2.y
  }
}

// 性能优化工具
export const PerformanceOptimizer = {
  // 获取设备性能等级
  getDevicePerformance() {
    const canvas = document.createElement('canvas')
    const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl')
    
    if (!gl) return 'low'
    
    const renderer = gl.getParameter(gl.RENDERER)
    const vendor = gl.getParameter(gl.VENDOR)
    
    // 简单的性能评估
    if (renderer.includes('Mali') || renderer.includes('Adreno 3')) {
      return 'low'
    } else if (renderer.includes('Adreno 5') || renderer.includes('PowerVR')) {
      return 'medium'
    } else {
      return 'high'
    }
  },
  
  // 根据性能调整配置
  getOptimizedConfig(baseConfig) {
    const performance = this.getDevicePerformance()
    const isMobile = window.innerWidth <= 768
    
    switch (performance) {
      case 'low':
        return {
          ...baseConfig,
          particleCount: isMobile ? 30 : 60,
          maxDistance: isMobile ? 50 : 80,
          connectionOpacity: 0.2
        }
      case 'medium':
        return {
          ...baseConfig,
          particleCount: isMobile ? 50 : 100,
          maxDistance: isMobile ? 70 : 100,
          connectionOpacity: 0.25
        }
      default:
        return baseConfig
    }
  }
} 