import request from '../config';

// 文件上传相关接口类型定义
export interface FileUploadResponse {
  uploadId: string;
  fileName: string;
  fileSize: number;
  fileUrl: string;
  thumbnailUrl?: string;
  mimeType: string;
  width?: number;
  height?: number;
  duration?: number;
}

export interface UploadOptions {
  file: File;
  userId?: string;
  sessionId?: number;
  onProgress?: (percentage: number) => void;
}

export interface ChunkUploadOptions {
  file: File;
  uploadId: string;
  chunkIndex: number;
  totalChunks: number;
  onProgress?: (percentage: number) => void;
}

export interface MediaMessageData {
  msgId: string;
  mediaType: number;
  fileName?: string;
  fileSize?: number;
  fileUrl: string;
  thumbnailUrl?: string;
  width?: number;
  height?: number;
  duration?: number;
  latitude?: number;
  longitude?: number;
  address?: string;
  locationName?: string;
}

// API响应类型
export interface ApiResponse<T = any> {
  data?: T;
  success: boolean;
  errCode?: string;
  errMessage?: string;
}

/**
 * 获取上传URL和配置
 */
export const getUploadUrl = async (): Promise<ApiResponse<{ uploadUrl: string; maxSize: number }>> => {
  try {
    const response = await request.get('/api/file/upload/config');
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('获取上传配置失败:', error);
    return {
      success: false,
      errCode: error.code || 'GET_UPLOAD_CONFIG_ERROR',
      errMessage: error.message || '获取上传配置失败'
    };
  }
};

/**
 * 单文件上传
 */
export const uploadFile = async (options: UploadOptions): Promise<ApiResponse<FileUploadResponse>> => {
  try {
    const formData = new FormData();
    formData.append('file', options.file);
    
    if (options.userId) {
      formData.append('userId', options.userId);
    }
    
    if (options.sessionId) {
      formData.append('sessionId', options.sessionId.toString());
    }

    const response = await request.post('/api/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      timeout: 300000, // 5分钟超时
      onUploadProgress: (progressEvent) => {
        if (options.onProgress && progressEvent.total) {
          const percentage = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          options.onProgress(percentage);
        }
      },
    });

    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('文件上传失败:', error);
    return {
      success: false,
      errCode: error.code || 'UPLOAD_FILE_ERROR',
      errMessage: error.message || '文件上传失败'
    };
  }
};

/**
 * 分片上传 - 初始化
 */
export const initChunkUpload = async (fileName: string, fileSize: number, fileMd5: string): Promise<ApiResponse<{ uploadId: string; chunkSize: number }>> => {
  try {
    const response = await request.post('/api/file/upload/chunk/init', {
      fileName,
      fileSize,
      fileMd5
    });

    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('初始化分片上传失败:', error);
    return {
      success: false,
      errCode: error.code || 'INIT_CHUNK_UPLOAD_ERROR',
      errMessage: error.message || '初始化分片上传失败'
    };
  }
};

/**
 * 分片上传 - 上传分片
 */
export const uploadChunk = async (options: ChunkUploadOptions): Promise<ApiResponse> => {
  try {
    const formData = new FormData();
    formData.append('file', options.file);
    formData.append('uploadId', options.uploadId);
    formData.append('chunkIndex', options.chunkIndex.toString());
    formData.append('totalChunks', options.totalChunks.toString());

    const response = await request.post('/api/file/upload/chunk', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      timeout: 120000, // 2分钟超时
      onUploadProgress: (progressEvent) => {
        if (options.onProgress && progressEvent.total) {
          const percentage = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          options.onProgress(percentage);
        }
      },
    });

    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('分片上传失败:', error);
    return {
      success: false,
      errCode: error.code || 'UPLOAD_CHUNK_ERROR',
      errMessage: error.message || '分片上传失败'
    };
  }
};

/**
 * 分片上传 - 合并分片
 */
export const mergeChunks = async (uploadId: string): Promise<ApiResponse<FileUploadResponse>> => {
  try {
    const response = await request.post('/api/file/upload/chunk/merge', {
      uploadId
    });

    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('合并分片失败:', error);
    return {
      success: false,
      errCode: error.code || 'MERGE_CHUNKS_ERROR',
      errMessage: error.message || '合并分片失败'
    };
  }
};

/**
 * 图片压缩上传
 */
export const uploadCompressedImage = async (file: File, quality = 0.8, maxWidth = 1920): Promise<ApiResponse<FileUploadResponse>> => {
  try {
    // 创建canvas压缩图片
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    const img = new Image();

    return new Promise((resolve) => {
      img.onload = async () => {
        // 计算压缩后的尺寸
        let { width, height } = img;
        if (width > maxWidth) {
          height = (height * maxWidth) / width;
          width = maxWidth;
        }

        canvas.width = width;
        canvas.height = height;
        
        if (ctx) {
          ctx.drawImage(img, 0, 0, width, height);
          
          canvas.toBlob(async (blob) => {
            if (blob) {
              const compressedFile = new File([blob], file.name, {
                type: file.type,
                lastModified: Date.now(),
              });

              const result = await uploadFile({ file: compressedFile });
              resolve(result);
            } else {
              resolve({
                success: false,
                errCode: 'COMPRESS_ERROR',
                errMessage: '图片压缩失败'
              });
            }
          }, file.type, quality);
        }
      };

      img.onerror = () => {
        resolve({
          success: false,
          errCode: 'IMAGE_LOAD_ERROR',
          errMessage: '图片加载失败'
        });
      };

      img.src = URL.createObjectURL(file);
    });
  } catch (error: any) {
    console.error('压缩上传失败:', error);
    return {
      success: false,
      errCode: error.code || 'COMPRESS_UPLOAD_ERROR',
      errMessage: error.message || '压缩上传失败'
    };
  }
};

/**
 * 删除文件
 */
export const deleteFile = async (uploadId: string): Promise<ApiResponse> => {
  try {
    const response = await request.delete(`/api/file/upload/${uploadId}`);
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('删除文件失败:', error);
    return {
      success: false,
      errCode: error.code || 'DELETE_FILE_ERROR',
      errMessage: error.message || '删除文件失败'
    };
  }
};

/**
 * 获取文件信息
 */
export const getFileInfo = async (uploadId: string): Promise<ApiResponse<FileUploadResponse>> => {
  try {
    const response = await request.get(`/api/file/upload/${uploadId}`);
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('获取文件信息失败:', error);
    return {
      success: false,
      errCode: error.code || 'GET_FILE_INFO_ERROR',
      errMessage: error.message || '获取文件信息失败'
    };
  }
};

/**
 * 发送多媒体消息
 */
export const sendMediaMessage = async (data: MediaMessageData): Promise<ApiResponse> => {
  try {
    const response = await request.post('/api/chat/messages/media', data);
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('发送多媒体消息失败:', error);
    return {
      success: false,
      errCode: error.code || 'SEND_MEDIA_MESSAGE_ERROR',
      errMessage: error.message || '发送多媒体消息失败'
    };
  }
};

/**
 * 获取用户文件使用统计
 */
export const getFileUsageStats = async (userId: string): Promise<ApiResponse<{
  totalFiles: number;
  totalSize: number;
  quota: number;
  usage: number;
}>> => {
  try {
    const response = await request.get('/api/file/stats', {
      params: { userId }
    });
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('获取文件使用统计失败:', error);
    return {
      success: false,
      errCode: error.code || 'GET_FILE_STATS_ERROR',
      errMessage: error.message || '获取文件使用统计失败'
    };
  }
};