// 需要先安装 axios 及其类型声明文件
// npm install axios @types/axios
import axios, { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';

// 创建axios实例
const instance: AxiosInstance = axios.create({
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: false  // 允许跨域请求携带cookie
});

// 请求拦截器
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 根据请求路径设置不同的baseURL
    config.baseURL = 'http://115.190.36.226:8080/';
    if(config.url?.includes('smartcs')) {
      // 去掉smartcs
      config.baseURL = 'http://localhost:8082';
      config.url = config.baseURL + config.url?.replace('/smartcs', '');
      console.log(config.url)
    }

    const token = localStorage.getItem('token');
    if (token) {
      config.headers = config.headers || {};
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
instance.interceptors.response.use(
  (response: AxiosResponse) => {
    const { success, errCode, errMessage, data } = response.data;
    if (success === true) {
      return response.data;
    } else {
      // 统一处理错误
      ElMessage.warning(errMessage);
      return Promise.reject(errMessage);
    }
  },
  (error) => {
    console.log(error)
    if (error.response?.status === 401 || error.response?.status === 403) {
      // token过期或无权限，清除登录信息并跳转到登录页
      // console.error("无权限");
      // localStorage.removeItem('token');
      // localStorage.removeItem('refreshToken');
      // window.location.href = '/login';
    } else {
      // 弹窗错误信息
      const errorMsg = error.response?.data?.errMessage || error.message || '服务器错误，请稍后再试';
      
      // 使用Element Plus的Message组件
      ElMessage.error(errorMsg);
    }
    return Promise.reject(error);
  }
);

export default instance;