<template>
  <div class="file-upload-component">
    <!-- 文件上传区域 -->
    <el-upload
      ref="uploadRef"
      class="file-upload"
      :action="uploadUrl"
      :headers="uploadHeaders"
      :data="uploadData"
      :multiple="multiple"
      :accept="accept"
      :auto-upload="false"
      :show-file-list="false"
      :on-change="handleFileChange"
      :before-upload="beforeUpload"
      :http-request="customUpload"
      drag
      v-bind="$attrs"
    >
      <div class="upload-area">
        <el-icon class="upload-icon" :size="48">
          <UploadFilled />
        </el-icon>
        <div class="upload-text">
          <p class="upload-title">点击上传文件或将文件拖拽到此处</p>
          <p class="upload-subtitle">
            支持 {{ getAllowedTypesText() }}
            <span v-if="maxSize">，单个文件不超过 {{ formatFileSize(maxSize) }}</span>
          </p>
        </div>
      </div>
    </el-upload>

    <!-- 上传进度列表 -->
    <div v-if="uploadingFiles.length > 0" class="upload-progress-list">
      <div class="progress-title">上传进度</div>
      <div
        v-for="file in uploadingFiles"
        :key="file.uid"
        class="progress-item"
      >
        <div class="file-info">
          <div class="file-icon">
            <el-icon>
              <Document v-if="isDocumentFile(file.name)" />
              <Picture v-else-if="isImageFile(file.name)" />
              <Headphone v-else-if="isAudioFile(file.name)" />
              <VideoPlay v-else-if="isVideoFile(file.name)" />
              <Files v-else />
            </el-icon>
          </div>
          <div class="file-details">
            <div class="file-name" :title="file.name">{{ file.name }}</div>
            <div class="file-meta">
              {{ formatFileSize(file.size) }} • {{ getFileStatusText(file) }}
            </div>
          </div>
        </div>
        
        <div class="progress-controls">
          <el-progress
            v-if="file.status === 'uploading'"
            :percentage="file.percentage || 0"
            :stroke-width="4"
            :show-text="false"
            class="upload-progress"
          />
          
          <div class="control-buttons">
            <el-button
              v-if="file.status === 'ready' || file.status === 'error'"
              type="primary"
              size="small"
              @click="retryUpload(file)"
              :loading="file.status === 'uploading'"
            >
              {{ file.status === 'error' ? '重试' : '上传' }}
            </el-button>
            
            <el-button
              size="small"
              type="text"
              @click="removeFile(file)"
              class="remove-btn"
            >
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="showPreview"
      :title="previewFile?.name"
      width="80%"
      :append-to-body="true"
      class="file-preview-dialog"
    >
      <div v-if="previewFile" class="preview-content">
        <!-- 图片预览 -->
        <div v-if="isImageFile(previewFile.name)" class="image-preview">
          <img :src="previewFile.url" :alt="previewFile.name" />
        </div>
        
        <!-- 视频预览 -->
        <div v-else-if="isVideoFile(previewFile.name)" class="video-preview">
          <video :src="previewFile.url" controls width="100%" />
        </div>
        
        <!-- 音频预览 -->
        <div v-else-if="isAudioFile(previewFile.name)" class="audio-preview">
          <audio :src="previewFile.url" controls />
        </div>
        
        <!-- 文件信息 -->
        <div v-else class="file-info-preview">
          <el-icon size="64"><Document /></el-icon>
          <h3>{{ previewFile.name }}</h3>
          <p>大小: {{ formatFileSize(previewFile.size) }}</p>
          <p>类型: {{ previewFile.type }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue';
import { ElMessage, ElMessageBox, type UploadFile, type UploadRequestOptions } from 'element-plus';
import { 
  UploadFilled, Document, Picture, Headphone, VideoPlay, Files, Close 
} from '@element-plus/icons-vue';
import { uploadFile, getUploadUrl } from '@/api/smartcs/file';

// Props
interface Props {
  // 上传URL
  uploadUrl?: string;
  // 是否支持多文件上传
  multiple?: boolean;
  // 接受的文件类型
  accept?: string;
  // 最大文件大小（字节）
  maxSize?: number;
  // 最大文件数量
  maxCount?: number;
  // 允许的文件类型
  allowedTypes?: string[];
  // 是否显示上传列表
  showUploadList?: boolean;
  // 用户ID
  userId?: string;
  // 会话ID
  sessionId?: number;
}

const props = withDefaults(defineProps<Props>(), {
  uploadUrl: '/api/file/upload',
  multiple: true,
  accept: '*',
  maxSize: 50 * 1024 * 1024, // 50MB
  maxCount: 10,
  allowedTypes: () => ['image/*', 'audio/*', 'video/*', 'application/*'],
  showUploadList: true
});

// Emits
const emit = defineEmits<{
  'upload-success': [file: any];
  'upload-error': [error: string, file: any];
  'upload-progress': [percentage: number, file: any];
  'files-change': [files: any[]];
}>();

// 响应式数据
const uploadRef = ref();
const uploadingFiles = ref<any[]>([]);
const showPreview = ref(false);
const previewFile = ref<any>(null);

// 计算属性
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
}));

const uploadData = computed(() => ({
  userId: props.userId,
  sessionId: props.sessionId
}));

// 方法
const getAllowedTypesText = () => {
  const typeMap: Record<string, string> = {
    'image/*': '图片',
    'audio/*': '音频',
    'video/*': '视频',
    'application/*': '文档',
    'text/*': '文本'
  };
  
  return props.allowedTypes
    .map(type => typeMap[type] || type)
    .join('、');
};

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
};

const isImageFile = (fileName: string): boolean => {
  const imageExts = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'];
  return imageExts.some(ext => fileName.toLowerCase().endsWith(ext));
};

const isAudioFile = (fileName: string): boolean => {
  const audioExts = ['.mp3', '.wav', '.ogg', '.m4a', '.aac'];
  return audioExts.some(ext => fileName.toLowerCase().endsWith(ext));
};

const isVideoFile = (fileName: string): boolean => {
  const videoExts = ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.webm'];
  return videoExts.some(ext => fileName.toLowerCase().endsWith(ext));
};

const isDocumentFile = (fileName: string): boolean => {
  const docExts = ['.pdf', '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx', '.txt'];
  return docExts.some(ext => fileName.toLowerCase().endsWith(ext));
};

const getFileStatusText = (file: any): string => {
  switch (file.status) {
    case 'ready': return '准备上传';
    case 'uploading': return '上传中';
    case 'success': return '上传成功';
    case 'error': return '上传失败';
    default: return '未知状态';
  }
};

const beforeUpload = (file: File): boolean => {
  // 检查文件大小
  if (file.size > props.maxSize) {
    ElMessage.error(`文件大小不能超过 ${formatFileSize(props.maxSize)}`);
    return false;
  }
  
  // 检查文件数量
  if (uploadingFiles.value.length >= props.maxCount) {
    ElMessage.error(`最多只能上传 ${props.maxCount} 个文件`);
    return false;
  }
  
  // 检查文件类型
  if (props.allowedTypes.length > 0) {
    const isAllowed = props.allowedTypes.some(type => {
      if (type.endsWith('/*')) {
        return file.type.startsWith(type.slice(0, -1));
      }
      return file.type === type;
    });
    
    if (!isAllowed) {
      ElMessage.error(`不支持的文件类型: ${file.type}`);
      return false;
    }
  }
  
  return true;
};

const handleFileChange = (file: UploadFile, fileList: UploadFile[]) => {
  if (file.status === 'ready') {
    const fileData = {
      uid: file.uid,
      name: file.name,
      size: file.size || 0,
      type: file.raw?.type || '',
      file: file.raw,
      status: 'ready',
      percentage: 0,
      url: '',
      uploadId: ''
    };
    
    uploadingFiles.value.push(fileData);
    emit('files-change', uploadingFiles.value);
  }
};

const customUpload = async (options: UploadRequestOptions) => {
  const { file, onProgress, onSuccess, onError } = options;
  
  try {
    // 找到对应的文件记录
    const fileRecord = uploadingFiles.value.find(f => f.file === file);
    if (!fileRecord) {
      onError(new Error('文件记录未找到'));
      return;
    }
    
    fileRecord.status = 'uploading';
    
    // 调用上传API
    const response = await uploadFile({
      file: file,
      userId: props.userId,
      sessionId: props.sessionId,
      onProgress: (percentage: number) => {
        fileRecord.percentage = percentage;
        onProgress({ percent: percentage });
        emit('upload-progress', percentage, fileRecord);
      }
    });
    
    if (response.success && response.data) {
      fileRecord.status = 'success';
      fileRecord.url = response.data.fileUrl;
      fileRecord.uploadId = response.data.uploadId;
      fileRecord.percentage = 100;
      
      onSuccess(response.data);
      emit('upload-success', fileRecord);
      ElMessage.success(`文件 "${file.name}" 上传成功`);
    } else {
      throw new Error(response.errMessage || '上传失败');
    }
  } catch (error: any) {
    const fileRecord = uploadingFiles.value.find(f => f.file === file);
    if (fileRecord) {
      fileRecord.status = 'error';
      fileRecord.percentage = 0;
    }
    
    onError(error);
    emit('upload-error', error.message || '上传失败', fileRecord);
    ElMessage.error(`文件 "${file.name}" 上传失败: ${error.message}`);
  }
};

const retryUpload = async (file: any) => {
  if (!file.file) return;
  
  file.status = 'uploading';
  file.percentage = 0;
  
  try {
    const response = await uploadFile({
      file: file.file,
      userId: props.userId,
      sessionId: props.sessionId,
      onProgress: (percentage: number) => {
        file.percentage = percentage;
        emit('upload-progress', percentage, file);
      }
    });
    
    if (response.success && response.data) {
      file.status = 'success';
      file.url = response.data.fileUrl;
      file.uploadId = response.data.uploadId;
      file.percentage = 100;
      
      emit('upload-success', file);
      ElMessage.success(`文件 "${file.name}" 上传成功`);
    } else {
      throw new Error(response.errMessage || '上传失败');
    }
  } catch (error: any) {
    file.status = 'error';
    file.percentage = 0;
    emit('upload-error', error.message || '上传失败', file);
    ElMessage.error(`文件 "${file.name}" 上传失败: ${error.message}`);
  }
};

const removeFile = (file: any) => {
  const index = uploadingFiles.value.findIndex(f => f.uid === file.uid);
  if (index > -1) {
    uploadingFiles.value.splice(index, 1);
    emit('files-change', uploadingFiles.value);
  }
};

const previewFileContent = (file: any) => {
  if (file.status === 'success' && file.url) {
    previewFile.value = file;
    showPreview.value = true;
  }
};

// 清空上传列表
const clearFiles = () => {
  uploadingFiles.value = [];
  emit('files-change', uploadingFiles.value);
};

// 获取成功上传的文件
const getSuccessFiles = () => {
  return uploadingFiles.value.filter(f => f.status === 'success');
};

// 暴露方法给父组件
defineExpose({
  clearFiles,
  getSuccessFiles,
  uploadingFiles
});
</script>

<style scoped lang="scss">
.file-upload-component {
  .file-upload {
    .upload-area {
      padding: 40px 20px;
      text-align: center;
      border: 2px dashed #d9d9d9;
      border-radius: 8px;
      background-color: #fafafa;
      transition: all 0.3s;
      cursor: pointer;
      
      &:hover {
        border-color: #409eff;
        background-color: #f0f9ff;
      }
      
      .upload-icon {
        color: #c0c4cc;
        margin-bottom: 16px;
      }
      
      .upload-text {
        .upload-title {
          margin: 0 0 8px 0;
          font-size: 16px;
          color: #606266;
        }
        
        .upload-subtitle {
          margin: 0;
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }

  .upload-progress-list {
    margin-top: 24px;
    
    .progress-title {
      font-size: 16px;
      font-weight: 500;
      color: #303133;
      margin-bottom: 16px;
    }
    
    .progress-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 16px;
      border: 1px solid #ebeef5;
      border-radius: 6px;
      margin-bottom: 8px;
      background-color: #fff;
      
      .file-info {
        display: flex;
        align-items: center;
        flex: 1;
        min-width: 0;
        
        .file-icon {
          margin-right: 12px;
          color: #606266;
          
          .el-icon {
            font-size: 24px;
          }
        }
        
        .file-details {
          min-width: 0;
          flex: 1;
          
          .file-name {
            font-size: 14px;
            font-weight: 500;
            color: #303133;
            margin-bottom: 4px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          
          .file-meta {
            font-size: 12px;
            color: #909399;
          }
        }
      }
      
      .progress-controls {
        display: flex;
        align-items: center;
        gap: 12px;
        
        .upload-progress {
          width: 120px;
        }
        
        .control-buttons {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .remove-btn {
            color: #f56c6c;
            
            &:hover {
              color: #f78989;
            }
          }
        }
      }
    }
  }
}

.file-preview-dialog {
  .preview-content {
    text-align: center;
    
    .image-preview img {
      max-width: 100%;
      max-height: 60vh;
      border-radius: 8px;
    }
    
    .video-preview video {
      max-width: 100%;
      max-height: 60vh;
      border-radius: 8px;
    }
    
    .audio-preview audio {
      width: 100%;
      max-width: 400px;
    }
    
    .file-info-preview {
      padding: 40px 20px;
      
      .el-icon {
        color: #c0c4cc;
        margin-bottom: 16px;
      }
      
      h3 {
        margin: 16px 0 8px 0;
        color: #303133;
      }
      
      p {
        margin: 4px 0;
        color: #606266;
      }
    }
  }
}

:deep(.el-upload-dragger) {
  border: none !important;
  background: none !important;
}
</style>