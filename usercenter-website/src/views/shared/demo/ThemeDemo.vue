<template>
  <div class="auth-page theme-demo-page">
    <!-- 科技网格覆盖层 -->
    <div class="auth-grid-overlay"></div>
    <!-- 噪点纹理覆盖层 -->
    <div class="auth-noise-overlay"></div>
    
    <!-- 粒子背景 -->
    <ParticleBackground theme="teal" density="low" />
    
    <header class="demo-header">
      <div class="logo">
        <div class="logo-icon"></div>
        <span>主题切换演示</span>
      </div>
      <div class="theme-controls">
        <ThemeToggle mode="button" :show-label="true" />
        <ThemeToggle mode="dropdown" :show-label="true" />
        <ThemeToggle mode="compact" :show-label="false" />
      </div>
    </header>

    <main class="demo-content">
      <div class="demo-card auth-card">
        <h1 class="demo-title">🌓 主题切换系统演示</h1>
        
        <div class="theme-info">
          <div class="info-item">
            <label>当前主题设置：</label>
            <code>{{ currentTheme }}</code>
            <span class="theme-display">({{ getThemeDisplayName() }})</span>
          </div>
          
          <div class="info-item">
            <label>系统主题：</label>
            <code>{{ systemTheme }}</code>
            <span class="theme-display">({{ getThemeDisplayName(systemTheme) }})</span>
          </div>
          
          <div class="info-item">
            <label>实际生效主题：</label>
            <code>{{ resolvedTheme }}</code>
            <span class="theme-icon">{{ getThemeIcon() }}</span>
          </div>
        </div>

        <div class="demo-features">
          <h3>主题功能特性</h3>
          <ul class="feature-list">
            <li>✨ 支持亮色/暗色/自动三种模式</li>
            <li>🎨 统一的设计变量系统</li>
            <li>🔄 响应系统主题偏好变化</li>
            <li>💾 本地存储用户偏好</li>
            <li>🚀 平滑的过渡动画</li>
            <li>📱 支持多种切换组件样式</li>
            <li>♿ 完整的无障碍支持</li>
          </ul>
        </div>

        <div class="demo-colors">
          <h3>主题色彩展示</h3>
          <div class="color-palette">
            <div class="color-item" style="background: var(--color-primary)">
              <span>primary</span>
            </div>
            <div class="color-item" style="background: var(--color-secondary)">
              <span>secondary</span>
            </div>
            <div class="color-item" style="background: var(--color-accent)">
              <span>accent</span>
            </div>
            <div class="color-item" style="background: var(--color-surface)">
              <span>surface</span>
            </div>
          </div>
        </div>

        <div class="demo-actions">
          <button class="demo-btn primary" @click="setTheme('light')">
            ☀️ 切换到亮色
          </button>
          <button class="demo-btn secondary" @click="setTheme('dark')">
            🌙 切换到暗色
          </button>
          <button class="demo-btn accent" @click="setTheme('auto')">
            🌓 跟随系统
          </button>
        </div>

        <div class="demo-tips">
          <h4>💡 使用提示</h4>
          <p>尝试在操作系统设置中更改主题偏好（亮色/暗色），本页面会自动响应变化（当设置为"跟随系统"时）。</p>
        </div>
      </div>
    </main>

    <footer class="demo-footer">
      <p>主题系统演示 - 支持亮色/暗色主题切换</p>
    </footer>
  </div>
</template>

<script setup>
import { useTheme } from '@/composables/useTheme'
import ThemeToggle from '@/components/ThemeToggle.vue'
import ParticleBackground from '@/components/ParticleBackground.vue'

const {
  currentTheme,
  systemTheme,
  resolvedTheme,
  isDark,
  setTheme,
  getThemeDisplayName,
  getThemeIcon
} = useTheme()
</script>

<style scoped>
.theme-demo-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.demo-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 3rem;
  background: linear-gradient(135deg, var(--color-surface) 0%, rgba(255, 255, 255, 0.12) 100%);
  backdrop-filter: blur(var(--blur-strong)) saturate(200%);
  border-bottom: 1px solid var(--color-border-hover);
  position: relative;
  z-index: 10;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  font-size: 1.6rem;
  font-weight: 700;
  color: var(--color-text-primary);
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
  border-radius: var(--radius-sm);
  position: relative;
}

.logo-icon::before {
  content: '🌓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 16px;
}

.theme-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.demo-content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  position: relative;
  z-index: 1;
}

.demo-card {
  width: 100%;
  max-width: 800px;
  padding: 3rem;
  text-align: center;
}

.demo-title {
  font-size: 2.5rem;
  font-weight: 800;
  margin-bottom: 2rem;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.theme-info {
  display: grid;
  gap: 1rem;
  margin-bottom: 2rem;
  text-align: left;
  background: var(--color-surface);
  padding: 1.5rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
}

.info-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.info-item label {
  font-weight: 600;
  color: var(--color-text-secondary);
  min-width: 120px;
}

.info-item code {
  background: rgba(0,0,0,0.1);
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 0.8rem;
}

.theme-display {
  color: var(--color-text-muted);
  font-style: italic;
}

.theme-icon {
  font-size: 1.2em;
}

.demo-features {
  margin-bottom: 2rem;
  text-align: left;
}

.demo-features h3 {
  color: var(--color-text-primary);
  margin-bottom: 1rem;
}

.feature-list {
  list-style: none;
  padding: 0;
  display: grid;
  gap: 0.5rem;
}

.feature-list li {
  color: var(--color-text-secondary);
  padding: 0.5rem;
  background: var(--color-surface);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--color-primary);
}

.demo-colors {
  margin-bottom: 2rem;
}

.demo-colors h3 {
  color: var(--color-text-primary);
  margin-bottom: 1rem;
}

.color-palette {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 1rem;
}

.color-item {
  height: 80px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0,0,0,0.5);
  border: 2px solid var(--color-border);
}

.demo-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.demo-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  font-size: 0.9rem;
}

.demo-btn.primary {
  background: var(--color-primary);
  color: white;
}

.demo-btn.secondary {
  background: var(--color-secondary);
  color: white;
}

.demo-btn.accent {
  background: var(--color-accent);
  color: var(--color-text-primary);
}

.demo-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.2);
}

.demo-tips {
  text-align: left;
  background: linear-gradient(135deg, var(--color-surface) 0%, rgba(255,255,255,0.1) 100%);
  padding: 1.5rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
}

.demo-tips h4 {
  color: var(--color-text-primary);
  margin-bottom: 0.5rem;
}

.demo-tips p {
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin: 0;
}

.demo-footer {
  background: var(--color-surface);
  padding: 1.5rem;
  text-align: center;
  color: var(--color-text-muted);
  border-top: 1px solid var(--color-border);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .demo-header {
    padding: 1rem 1.5rem;
    flex-direction: column;
    gap: 1rem;
  }
  
  .theme-controls {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .demo-card {
    padding: 2rem 1.5rem;
  }
  
  .demo-title {
    font-size: 2rem;
  }
  
  .demo-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .demo-btn {
    width: 100%;
    max-width: 300px;
  }
}
</style>