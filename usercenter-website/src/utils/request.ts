import axios from 'axios';
import { ElMessage } from 'element-plus';
import { setupIdempotencyInterceptor } from './idempotencyInterceptor';

// 声明Vite环境变量的类型
interface ImportMetaEnv {
  VITE_API_BASE_URL: string;
  // 其他环境变量
}

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // 默认以/api为前缀
  timeout: 15000 // 请求超时时间
});

// 安装幂等性拦截器
setupIdempotencyInterceptor(service);

// request拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

// response拦截器
service.interceptors.response.use(
  response => {
    const res = response.data;
    
    // 如果是下载文件的请求，直接返回
    if (response.config.responseType === 'blob') {
      return response;
    }
    
    // 如果响应成功，直接返回数据
    if (res.success === true) {
      return res;
    }
    
    // 处理特定错误码
    if (res.errorCode) {
      switch (res.errorCode) {
        case 'AUTH_EXPIRED':
          // 认证过期，重定向到登录页
          ElMessage.error('登录已过期，请重新登录');
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          setTimeout(() => {
            window.location.href = '/login';
          }, 1500);
          break;
        case 'ACCESS_DENIED':
          ElMessage.error('没有访问权限');
          break;
        default:
          ElMessage.error(res.errorMessage || '未知错误');
      }
    } else {
      ElMessage.error(res.errorMessage || '未知错误');
    }
    
    return Promise.reject(new Error(res.errorMessage || '未知错误'));
  },
  error => {
    console.error('响应拦截器错误:', error);
    
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录');
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          setTimeout(() => {
            window.location.href = '/login';
          }, 1500);
          break;
        case 403:
          ElMessage.error('没有访问权限');
          break;
        case 500:
          ElMessage.error('服务器错误');
          break;
        default:
          ElMessage.error(`请求失败: ${error.message}`);
      }
    } else {
      ElMessage.error(`网络错误: ${error.message}`);
    }
    
    return Promise.reject(error);
  }
);

export default service; 