# SmartCS 管理控制台

## 概述

SmartCS 管理控制台是从原始的 HTML/JavaScript 实现迁移到 Vue 3 + Element Plus 的现代化管理界面。提供意图管理、快照管理和分类测试等核心功能。

## 功能模块

### 1. 仪表板 (Dashboard)
- 系统运行状态概览
- 关键指标统计
- 快速操作入口
- 最近活动记录

### 2. 意图管理 (Intent Management)
- 意图的 CRUD 操作
- 标签和边界管理
- 意图分类配置
- 版本历史查看

### 3. 快照管理 (Snapshot Management)
- 快照创建和发布
- 配置导出功能
- 运行时配置预览
- 快照状态管理

### 4. 分类测试 (Classification Test)
- 实时意图分类测试
- 测试历史记录
- 快速样例选择
- 结果详细分析

## 技术架构

### 前端技术栈
- **Vue 3**: 使用 Composition API
- **Element Plus**: UI 组件库
- **Pinia**: 状态管理
- **Vue Router**: 路由管理
- **SCSS**: 样式预处理器

### 目录结构
```
src/views/admin/
├── AdminLayout.vue           # 管理控制台布局组件
├── Dashboard.vue             # 仪表板页面
├── IntentManagement.vue      # 意图管理页面
├── SnapshotManagement.vue    # 快照管理页面
├── ClassificationTest.vue    # 分类测试页面
├── admin-styles.scss         # 管理控制台样式
├── README.md                 # 本文档
└── components/
    ├── IntentModal.vue       # 意图编辑模态框
    └── SnapshotModal.vue     # 快照详情模态框

src/api/admin/
└── admin.js                  # API 接口定义

src/stores/admin/
└── admin.js                  # Pinia 状态管理
```

## API 接口

### 仪表板统计
- `GET /admin/intent/stats` - 意图统计
- `GET /admin/intent/snapshot/stats` - 快照统计
- `GET /admin/knowledge/knowledgeBase/stats` - 知识库统计
- `GET /admin/intent/classification/stats` - 分类统计

### 意图管理
- `GET /admin/intent/page` - 分页查询意图
- `GET /admin/intent/:id` - 获取意图详情
- `POST /admin/intent` - 创建意图
- `PUT /admin/intent/:id` - 更新意图
- `DELETE /admin/intent/:id` - 删除意图
- `GET /admin/intent/catalog/list` - 获取分类目录

### 快照管理
- `GET /admin/intent/snapshot/page` - 分页查询快照
- `GET /admin/intent/snapshot/:id` - 获取快照详情
- `POST /admin/intent/snapshot/create` - 创建快照
- `POST /admin/intent/snapshot/:id/publish` - 发布快照
- `GET /admin/intent/snapshot/:id/export` - 导出快照配置

### 分类测试
- `POST /api/intent/classify` - 测试意图分类
- `GET /api/intent/runtime/config` - 获取运行时配置

## 状态管理

使用 Pinia 管理以下状态：
- 仪表板统计数据
- 意图列表和分页信息
- 快照列表和分页信息
- 当前编辑的意图/快照
- 分类目录列表
- 加载状态

## 样式设计

### 设计原则
- 遵循项目设计 Token 系统
- 现代化的渐变和阴影效果
- 响应式设计适配移动端
- 平滑的过渡动画

### 主要特性
- 毛玻璃效果 (Glassmorphism)
- 渐变背景和按钮
- 微妙的悬停动画
- 一致的圆角和间距
- 暗色主题支持

## 路由配置

```javascript
{
  path: 'smartcs-admin',
  component: () => import('../views/admin/AdminLayout.vue'),
  children: [
    {
      path: 'dashboard',
      name: 'SmartCSDashboard',
      component: () => import('../views/admin/Dashboard.vue')
    },
    {
      path: 'intent',
      name: 'IntentManagement', 
      component: () => import('../views/admin/IntentManagement.vue')
    },
    {
      path: 'snapshot',
      name: 'SnapshotManagement',
      component: () => import('../views/admin/SnapshotManagement.vue')
    },
    {
      path: 'classification',
      name: 'ClassificationTest',
      component: () => import('../views/admin/ClassificationTest.vue')
    }
  ]
}
```

## 使用说明

### 开发环境启动
```bash
npm run dev
```

### 访问管理控制台
访问路径：`/dashboard/smartcs-admin/dashboard`

### 权限要求
- `smartcs:admin:view` - 访问管理控制台
- `smartcs:intent:view` - 意图管理权限
- `smartcs:snapshot:view` - 快照管理权限
- `smartcs:classification:test` - 分类测试权限

## 迁移完成清单

### ✅ 已完成的功能
1. **布局组件**: AdminLayout 侧边栏导航
2. **仪表板**: 统计卡片、快速操作、系统状态
3. **意图管理**: CRUD 操作、搜索筛选、标签管理
4. **快照管理**: 列表展示、详情查看、发布导出
5. **分类测试**: 实时测试、历史记录、样例选择
6. **API 集成**: 完整的 RESTful API 封装
7. **状态管理**: Pinia store 管理全局状态
8. **样式优化**: 基于设计 Token 的现代样式
9. **响应式设计**: 移动端适配
10. **路由配置**: 嵌套路由和权限控制

### 🔄 待优化的功能
1. 意图版本管理详细功能
2. 批量操作功能
3. 更多图表和可视化
4. 实时数据更新 (WebSocket)
5. 国际化支持

## 注意事项

1. **API 兼容性**: 确保后端 API 接口与前端调用保持一致
2. **权限控制**: 需要配置相应的权限角色
3. **错误处理**: 已添加完善的错误提示和处理
4. **性能优化**: 使用了懒加载和按需加载
5. **浏览器兼容**: 支持现代浏览器 (Chrome 90+, Firefox 88+, Safari 14+)

## 维护和扩展

### 添加新功能页面
1. 在 `src/views/admin/` 创建新的 Vue 组件
2. 在 `AdminLayout.vue` 添加侧边栏菜单项
3. 在路由配置中添加新路由
4. 如需要，在 API 和 Store 中添加相关逻辑

### 样式定制
- 修改 `admin-styles.scss` 中的样式变量
- 使用项目设计 Token 系统保持一致性
- 遵循 BEM 命名规范

### 状态管理扩展
- 在 `stores/admin/admin.js` 中添加新的状态和方法
- 保持状态的响应式特性
- 合理组织状态结构