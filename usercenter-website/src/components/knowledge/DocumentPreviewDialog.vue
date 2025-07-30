<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Document, Loading, Warning } from '@element-plus/icons-vue';

// Props 定义
interface DocumentPreviewProps {
  visible: boolean;
  fileUrl: string;
  fileName: string;
  fileType?: string;
}

const props = defineProps<DocumentPreviewProps>();

// Emits 定义
const emit = defineEmits<{
  'update:visible': [value: boolean];
}>();

// 响应式数据
const documentContent = ref<string>('');
const documentLoading = ref(false);
const documentError = ref('');
const contentType = ref<string>('');

// 修复URL中的重复域名问题
const fixFileUrl = (url: string): string => {
  if (!url) return url;
  
  // 检查是否有重复的域名部分
  const domainPattern = /https?:\/\/[^\/]+/;
  const matches = url.match(new RegExp(domainPattern.source, 'g'));
  
  if (matches && matches.length > 1) {
    // 有重复的域名，只保留第一个
    const firstDomain = matches[0];
    
    // 提取文件名部分（最后一个斜杠后的内容）
    const lastSlashIndex = url.lastIndexOf('/');
    if (lastSlashIndex !== -1) {
      const fileName = url.substring(lastSlashIndex);
      return firstDomain + fileName;
    }
  }
  
  return url;
};

// 获取原始文档内容
const fetchOriginalDocument = async () => {
  if (!props.fileUrl) {
    documentError.value = '文件URL不能为空';
    return;
  }

  documentLoading.value = true;
  documentError.value = '';
  documentContent.value = '';

  try {
    // 修复URL
    const fixedUrl = fixFileUrl(props.fileUrl);
    console.log('原始URL:', props.fileUrl);
    console.log('修复后URL:', fixedUrl);
    
    // 直接尝试获取文件内容
    const response = await fetch(fixedUrl, {
      method: 'GET',
      mode: 'cors', // 尝试CORS
      headers: {
        'Accept': '*/*',
      }
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    // 获取内容类型
    contentType.value = response.headers.get('content-type') || '';
    
    // 根据内容类型处理
    if (contentType.value.includes('text/') || 
        contentType.value.includes('application/json') ||
        contentType.value.includes('application/xml')) {
      // 文本类型
      documentContent.value = await response.text();
    } else if (contentType.value.includes('image/')) {
      // 图片类型，转换为base64
      const blob = await response.blob();
      const reader = new FileReader();
      reader.onload = () => {
        documentContent.value = reader.result as string;
      };
      reader.readAsDataURL(blob);
    } else {
      // 其他类型，尝试作为文本处理
      try {
        documentContent.value = await response.text();
      } catch {
        throw new Error('不支持的文件格式');
      }
    }
    
  } catch (error: any) {
    console.error('获取原始文档失败:', error);
    documentError.value = error.message || '获取文档内容失败';
    ElMessage.error('获取文档内容失败: ' + documentError.value);
  } finally {
    documentLoading.value = false;
  }
};

// 关闭弹窗
const handleClose = () => {
  emit('update:visible', false);
  // 清理数据
  documentContent.value = '';
  documentError.value = '';
  contentType.value = '';
};

// 监听弹窗显示状态
watch(() => props.visible, (newVal) => {
  if (newVal && props.fileUrl) {
    fetchOriginalDocument();
  }
});

// 判断是否为图片类型
const isImageType = computed(() => {
  return contentType.value.startsWith('image/') || 
         props.fileType?.toLowerCase().match(/\.(jpg|jpeg|png|gif|bmp|webp)$/);
});

// 判断是否为HTML类型
const isHtmlType = computed(() => {
  return contentType.value.includes('text/html') || 
         props.fileType?.toLowerCase().endsWith('.html') ||
         props.fileType?.toLowerCase().endsWith('.htm');
});

// 获取文件大小显示
const getFileSizeDisplay = (content: string) => {
  const size = new Blob([content]).size;
  if (size < 1024) {
    return `${size} B`;
  } else if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`;
  } else {
    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
  }
};

// 复制内容到剪贴板
const copyToClipboard = () => {
  try {
    const textArea = document.createElement('textarea');
    textArea.value = documentContent.value;
    document.body.appendChild(textArea);
    textArea.select();
    document.execCommand('copy');
    document.body.removeChild(textArea);
    ElMessage.success('内容已复制到剪贴板');
  } catch (error) {
    ElMessage.error('复制失败');
  }
};
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="`预览原始文档 - ${fileName}`"
    width="70%"
    :close-on-click-modal="false"
    @close="handleClose"
    @update:model-value="(val: boolean) => emit('update:visible', val)"
  >
    <div class="document-preview-content">
      <!-- 加载状态 -->
      <div v-if="documentLoading" class="loading-container">
        <el-icon class="rotating"><Loading /></el-icon>
        <p>正在加载文档内容...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="documentError" class="error-container">
        <el-icon><Warning /></el-icon>
        <p>{{ documentError }}</p>
        <el-button type="primary" size="small" @click="fetchOriginalDocument">
          重试
        </el-button>
      </div>
      
      <!-- 文档内容 -->
      <div v-else-if="documentContent" class="content-container">
        <!-- 文件信息 -->
        <div class="file-info">
          <div class="info-item">
            <span class="label">文件类型:</span>
            <span class="value">{{ contentType || props.fileType || '未知' }}</span>
          </div>
          <div class="info-item">
            <span class="label">文件大小:</span>
            <span class="value">{{ getFileSizeDisplay(documentContent) }}</span>
          </div>
        </div>
        
        <!-- 图片预览 -->
        <div v-if="isImageType" class="image-preview">
          <el-image 
            :src="documentContent" 
            :preview-src-list="[documentContent]"
            fit="contain"
            style="max-width: 100%; max-height: 500px;"
          />
        </div>
        
        <!-- HTML预览 -->
        <div v-else-if="isHtmlType" class="html-preview">
          <div class="html-content" v-html="documentContent"></div>
        </div>
        
        <!-- 文本预览 -->
        <div v-else class="text-preview">
          <pre class="text-content">{{ documentContent }}</pre>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-else class="empty-container">
        <el-icon><Document /></el-icon>
        <p>暂无文档内容</p>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button 
          v-if="documentContent && !isImageType" 
          type="primary" 
          @click="copyToClipboard"
        >
          复制内容
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.document-preview-content {
  min-height: 400px;
  max-height: 600px;
  overflow-y: auto;
}

.loading-container,
.error-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #666;
}

.loading-container p,
.error-container p,
.empty-container p {
  margin: 16px 0 0 0;
  font-size: 14px;
}

.rotating {
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.error-container {
  color: #f56c6c;
}

.error-container .el-button {
  margin-top: 16px;
}

.file-info {
  display: flex;
  gap: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 6px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item .label {
  font-weight: 500;
  color: #333;
}

.info-item .value {
  color: #666;
}

.content-container {
  padding: 0;
}

.image-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.html-preview {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  overflow: hidden;
}

.html-content {
  padding: 16px;
  max-height: 500px;
  overflow-y: auto;
  background: #fff;
}

.text-preview {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  overflow: hidden;
}

.text-content {
  margin: 0;
  padding: 16px;
  max-height: 500px;
  overflow-y: auto;
  background: #fafafa;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style> 