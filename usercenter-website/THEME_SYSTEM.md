# 🌓 主题切换系统使用说明

本项目已集成完整的亮色/暗色主题切换系统，支持自动跟随系统主题偏好。

## ✨ 功能特性

- **三种主题模式**
  - 🌞 **亮色主题**：适合白天使用的浅色界面
  - 🌙 **暗色主题**：适合夜晚或低光环境的深色界面  
  - 🌓 **自动模式**：根据系统设置自动切换亮/暗主题

- **智能响应**
  - 实时检测系统主题偏好变化
  - 自动应用对应的主题样式
  - 保存用户偏好到本地存储

- **完整的设计系统**
  - 统一的 CSS 变量系统
  - 所有组件自动适配主题
  - 平滑的过渡动画效果

## 🚀 快速开始

### 1. 访问演示页面
访问 `/theme-demo` 路由查看完整的主题切换演示。

### 2. 在登录/注册页面体验
- 登录页面：`http://localhost:5174/login`
- 注册页面：`http://localhost:5174/register`

页面右上角有主题切换按钮。

### 3. 在管理后台使用
登录后台后，顶部导航栏右侧有紧凑的主题切换按钮组。

## 🛠️ 开发者使用

### 在组件中使用主题系统

```vue
<script setup>
import { useTheme } from '@/composables/useTheme'

const {
  currentTheme,      // 当前设置的主题 (light/dark/auto)
  systemTheme,       // 系统主题偏好 (light/dark)
  resolvedTheme,     // 实际生效的主题 (light/dark)
  isDark,           // 是否为暗色主题
  setTheme,         // 设置主题函数
  toggleTheme,      // 切换主题函数
  getThemeDisplayName,  // 获取主题显示名称
  getThemeIcon      // 获取主题图标
} = useTheme()

// 设置为亮色主题
setTheme('light')

// 设置为暗色主题  
setTheme('dark')

// 设置为自动模式
setTheme('auto')
</script>
```

### 使用主题切换组件

```vue
<template>
  <!-- 按钮模式 -->
  <ThemeToggle mode="button" :show-label="true" />
  
  <!-- 下拉菜单模式 -->
  <ThemeToggle mode="dropdown" :show-label="true" />
  
  <!-- 紧凑按钮组模式 -->
  <ThemeToggle mode="compact" :show-label="false" />
</template>

<script setup>
import ThemeToggle from '@/components/ThemeToggle.vue'
</script>
```

### 在样式中使用主题变量

```scss
.my-component {
  background: var(--color-surface);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border);
  
  &:hover {
    background: var(--color-surface-hover);
    border-color: var(--color-border-hover);
  }
}

// 针对特定主题的样式
.dark-theme .my-component {
  // 暗色主题下的特殊样式
}

.light-theme .my-component {
  // 亮色主题下的特殊样式
}
```

## 🎨 主题变量说明

### 基础色彩变量

| 变量名 | 亮色主题 | 暗色主题 | 说明 |
|--------|----------|----------|------|
| `--color-primary` | `#22d3ee` | `#06b6d4` | 主要品牌色 |
| `--color-secondary` | `#0ea5e9` | `#0284c7` | 次要品牌色 |  
| `--color-accent` | `#67e8f9` | `#22d3ee` | 强调色 |
| `--color-text-primary` | `#1a1a1a` | `#f8fafc` | 主要文字色 |
| `--color-text-secondary` | `#2d3748` | `#e2e8f0` | 次要文字色 |
| `--color-text-muted` | `#4a5568` | `#94a3b8` | 弱化文字色 |

### 表面和边框变量

| 变量名 | 说明 |
|--------|------|
| `--color-surface` | 表面背景色 |
| `--color-border` | 边框色 |
| `--color-border-hover` | 鼠标悬停时边框色 |
| `--auth-page-bg` | 认证页面背景 |
| `--auth-card-bg` | 认证卡片背景 |

### 几何和效果变量

| 变量名 | 值 | 说明 |
|--------|------|------|
| `--radius-sm` | `8px` | 小圆角 |
| `--radius-md` | `16px` | 中圆角 |
| `--radius-lg` | `20px` | 大圆角 |
| `--radius-xl` | `24px` | 超大圆角 |
| `--blur-light` | `10px` | 轻微模糊 |
| `--blur-medium` | `15px` | 中度模糊 |
| `--blur-strong` | `25px` | 强烈模糊 |

## 🔧 系统架构

### 文件结构

```
src/
├── composables/
│   └── useTheme.js           # 主题管理 composable
├── components/
│   └── ThemeToggle.vue       # 主题切换组件
├── styles/
│   └── auth-theme.scss       # 主题样式变量
└── views/
    └── ThemeDemo.vue         # 主题演示页面
```

### 主题检测逻辑

1. **初始化**：检测系统主题偏好和用户保存的设置
2. **监听变化**：监听系统主题偏好变化事件
3. **应用主题**：将主题类名添加到 `<html>` 元素
4. **持久化**：将用户选择保存到 localStorage

### 无障碍支持

- 完整的键盘导航支持
- 适当的 ARIA 属性
- 高对比度模式适配
- 减少动画偏好支持
- 触摸设备优化

## 📱 响应式支持

主题系统在所有设备尺寸上都能正常工作：

- **桌面端**：完整的主题切换控件
- **平板端**：适配的布局和交互
- **移动端**：优化的触摸交互

## 🚨 注意事项

1. **CSS 变量兼容性**：需要现代浏览器支持
2. **localStorage 依赖**：用户偏好存储需要 localStorage 支持  
3. **媒体查询支持**：系统主题检测需要 `prefers-color-scheme` 支持
4. **动画性能**：在低性能设备上会自动减少动画

## 🎯 最佳实践

1. **使用 CSS 变量**：始终使用预定义的主题变量，避免硬编码颜色
2. **测试两个主题**：确保组件在两种主题下都能正常工作
3. **考虑对比度**：确保文字在两种主题下都有足够的对比度
4. **优雅降级**：为不支持的浏览器提供备用方案
5. **性能优化**：避免不必要的主题变量重复定义

## 🤝 贡献指南

如需扩展主题系统，请遵循以下原则：

1. 保持变量命名一致性
2. 确保两个主题的视觉平衡
3. 测试无障碍功能
4. 更新相关文档

---

🎉 享受完美的主题切换体验！如有问题请查看演示页面或联系开发团队。