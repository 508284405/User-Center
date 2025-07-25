import request from '../config';

// 文件上传响应结果类型
export interface FileUploadResponse {
  code: number;
  msg: string;
  data: string;
}

/**
 * 上传图片文件
 * @param file 图片文件
 * @returns Promise<string> 返回图片URL
 */
export const uploadImage = async (file: File): Promise<string> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await request.post<FileUploadResponse>('/client-web/api/file/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
  if (response.success) {
    return response.data.fileUrl;
  }
  throw new Error(response.errMessage || '图片上传失败');
};

/**
 * 上传文档文件
 * @param file 文档文件 (支持TXT, MARKDOWN, MDX, PDF, HTML, XLSX, XLS, DOCX, CSV, MD, HTM等格式)
 * @returns Promise<string> 返回文档URL
 */
export const uploadDocument = async (file: File): Promise<string> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await request.post<FileUploadResponse>('/client-web/api/file/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
  if (response.success) {
    return response.data.fileUrl || response.data;
  }
  throw new Error(response.errMessage || '文档上传失败');
};