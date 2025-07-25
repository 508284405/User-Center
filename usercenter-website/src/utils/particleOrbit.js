// 粒子轨道系统工具
export class OrbitParticle {
  constructor(radius, angle, speed, size, color) {
    this.radius = radius
    this.angle = angle
    this.speed = speed
    this.size = size
    this.baseSize = size
    this.color = color
    this.opacity = Math.random() * 0.6 + 0.4
    this.pulsePhase = Math.random() * Math.PI * 2
    this.trail = []
    this.maxTrailLength = 8
  }

  update(centerX, centerY, statusMultiplier = 1) {
    // 更新角度
    this.angle += this.speed * statusMultiplier
    if (this.angle >= 360) this.angle -= 360
    if (this.angle < 0) this.angle += 360

    // 计算位置
    const radian = (this.angle * Math.PI) / 180
    const x = centerX + Math.cos(radian) * this.radius
    const y = centerY + Math.sin(radian) * this.radius

    // 更新拖尾
    this.trail.unshift({ x, y, opacity: this.opacity })
    if (this.trail.length > this.maxTrailLength) {
      this.trail.pop()
    }

    // 脉冲效果
    this.pulsePhase += 0.1
    const pulseSize = Math.sin(this.pulsePhase) * 0.3 + 1
    this.size = this.baseSize * pulseSize

    return { x, y }
  }

  draw(ctx, statusColor = null) {
    // 绘制拖尾
    this.trail.forEach((point, index) => {
      const trailOpacity = (this.opacity * (this.maxTrailLength - index)) / this.maxTrailLength * 0.3
      ctx.save()
      ctx.globalAlpha = trailOpacity
      ctx.fillStyle = statusColor || this.color
      ctx.beginPath()
      ctx.arc(point.x, point.y, this.size * 0.5, 0, Math.PI * 2)
      ctx.fill()
      ctx.restore()
    })

    // 绘制主粒子
    if (this.trail.length > 0) {
      const currentPos = this.trail[0]
      ctx.save()
      ctx.globalAlpha = this.opacity

      // 发光效果
      const gradient = ctx.createRadialGradient(
        currentPos.x, currentPos.y, 0,
        currentPos.x, currentPos.y, this.size * 2
      )
      gradient.addColorStop(0, statusColor || this.color)
      gradient.addColorStop(0.5, `${statusColor || this.color}80`)
      gradient.addColorStop(1, 'transparent')

      ctx.fillStyle = gradient
      ctx.beginPath()
      ctx.arc(currentPos.x, currentPos.y, this.size * 2, 0, Math.PI * 2)
      ctx.fill()

      // 核心粒子
      ctx.fillStyle = statusColor || this.color
      ctx.beginPath()
      ctx.arc(currentPos.x, currentPos.y, this.size, 0, Math.PI * 2)
      ctx.fill()
      ctx.restore()
    }
  }
}

export class ParticleOrbitSystem {
  constructor(centerX, centerY) {
    this.centerX = centerX
    this.centerY = centerY
    this.particles = []
    this.status = 'idle'
    this.initParticles()
  }

  initParticles() {
    this.particles = []

    // 内圈 - 12个粒子
    for (let i = 0; i < 12; i++) {
      const angle = (360 / 12) * i
      this.particles.push(new OrbitParticle(
        180, // 半径增大
        angle,
        0.8, // 速度
        2, // 大小
        '#8B5CF6' // 颜色
      ))
    }

    // 中圈 - 16个粒子
    for (let i = 0; i < 16; i++) {
      const angle = (360 / 16) * i
      this.particles.push(new OrbitParticle(
        220, // 半径增大
        angle,
        -0.6, // 反向旋转
        1.5,
        '#A78BFA'
      ))
    }

    // 外圈 - 20个粒子
    for (let i = 0; i < 20; i++) {
      const angle = (360 / 20) * i
      this.particles.push(new OrbitParticle(
        260, // 半径增大
        angle,
        0.4,
        1,
        '#C4B5FD'
      ))
    }
  }

  updateCenter(centerX, centerY) {
    this.centerX = centerX
    this.centerY = centerY
  }

  setStatus(status) {
    this.status = status
  }

  getStatusConfig() {
    switch (this.status) {
      case 'active':
        return {
          speedMultiplier: 1.5,
          color: '#8B5CF6',
          glowIntensity: 1.2
        }
      case 'success':
        return {
          speedMultiplier: 0.8,
          color: '#10B981',
          glowIntensity: 1.5
        }
      case 'error':
        return {
          speedMultiplier: 2,
          color: '#EF4444',
          glowIntensity: 1.3
        }
      default:
        return {
          speedMultiplier: 1,
          color: null,
          glowIntensity: 1
        }
    }
  }

  update() {
    const config = this.getStatusConfig()
    
    this.particles.forEach(particle => {
      particle.update(this.centerX, this.centerY, config.speedMultiplier)
    })
  }

  draw(ctx) {
    const config = this.getStatusConfig()
    
    this.particles.forEach(particle => {
      particle.draw(ctx, config.color)
    })
  }

  resize(newCenterX, newCenterY) {
    this.updateCenter(newCenterX, newCenterY)
  }
} 