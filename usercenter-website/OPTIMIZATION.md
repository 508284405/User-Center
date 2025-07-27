# StepWizardDialog 文件上传优化

## 优化背景
之前的实现中，每次点击"预览块"按钮都会重新上传文件到OSS，导致：
- 重复消耗带宽和存储资源
- 用户等待时间增加
- 服务器负载不必要增加

## 优化方案

### 1. 文件缓存机制
- 使用文件名、大小和修改时间生成唯一标识符
- 缓存已上传文件的URL，避免重复上传
- 实现智能缓存清理机制

### 2. 状态显示优化
- 在文件列表中显示文件缓存状态
- 提供清晰的视觉反馈：
  - 🟡 **上传中** - 文件正在上传到OSS
  - 🟢 **已缓存** - 文件已上传并可重复使用
  - 🔵 **处理中** - 正在执行分块处理

### 3. 用户体验提升
- 缓存命中时显示"使用缓存文件"和"缓存加速"标识
- 显著减少等待时间
- 提供更流畅的预览体验

## 技术实现

### 核心数据结构
```typescript
// 文件URL缓存映射
const uploadedFileUrls = ref<Map<string, string>>(new Map());

// 文件状态缓存
const cacheStatus = ref<Map<string, 'uploading' | 'cached' | 'processing'>>(new Map());
```

### 文件标识生成
```typescript
const getFileIdentifier = (file: File): string => {
  return `${file.name}_${file.size}_${file.lastModified}`;
};
```

### 缓存优先逻辑
```typescript
// 检查文件是否已缓存
if (uploadedFileUrls.value.has(fileId)) {
  fileUrl = uploadedFileUrls.value.get(fileId)!;
  uploadProgress.value = '使用缓存文件';
} else {
  // 上传并缓存
  fileUrl = await uploadDocument(file);
  uploadedFileUrls.value.set(fileId, fileUrl);
}
```

## 缓存管理

### 自动清理时机
1. **弹窗关闭时** - 清理所有缓存数据
2. **文件移除时** - 清理对应文件的缓存
3. **组件重置时** - 重置所有状态

### 手动清理
用户可以通过重新上传文件或关闭弹窗来清理缓存。

## 性能提升

### 优化效果
- ✅ **减少重复上传** - 同一文件只上传一次
- ✅ **节省带宽** - 避免重复网络传输
- ✅ **提升响应速度** - 缓存命中时几乎即时响应
- ✅ **减少服务器负载** - 降低OSS API调用频次

### 适用场景
- 用户频繁调整分段参数并预览
- 用户在不同分段模式间切换
- 用户需要多次确认预览效果

## 兼容性
- 保持原有API接口不变
- 向后兼容，不影响现有功能
- 无需后端配合修改

## 注意事项
1. 缓存仅在当前会话有效
2. 文件内容变更会生成新的缓存项
3. 缓存不会持久化到本地存储