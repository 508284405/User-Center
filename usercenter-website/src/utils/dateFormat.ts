/**
 * 时间格式化工具函数
 */

/**
 * 将时间戳转换为 YYYY-MM-DD HH:mm:ss 格式
 * @param timestamp 时间戳（毫秒）
 * @returns 格式化后的时间字符串
 */
export function formatTimestamp(timestamp: number | string): string {
  if (!timestamp) return '';
  
  const date = new Date(Number(timestamp));
  
  if (isNaN(date.getTime())) {
    return '';
  }
  
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

/**
 * 将时间戳转换为相对时间（如：刚刚、5分钟前、1小时前等）
 * @param timestamp 时间戳（毫秒）
 * @returns 相对时间字符串
 */
export function formatRelativeTime(timestamp: number | string): string {
  if (!timestamp) return '';
  
  const date = new Date(Number(timestamp));
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  
  if (isNaN(date.getTime()) || diff < 0) {
    return '';
  }
  
  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);
  
  if (seconds < 60) {
    return '刚刚';
  } else if (minutes < 60) {
    return `${minutes}分钟前`;
  } else if (hours < 24) {
    return `${hours}小时前`;
  } else if (days < 7) {
    return `${days}天前`;
  } else {
    return formatTimestamp(timestamp);
  }
}

/**
 * 将时间戳转换为简短格式（如：MM-DD HH:mm）
 * @param timestamp 时间戳（毫秒）
 * @returns 简短格式时间字符串
 */
export function formatShortTime(timestamp: number | string): string {
  if (!timestamp) return '';
  
  const date = new Date(Number(timestamp));
  
  if (isNaN(date.getTime())) {
    return '';
  }
  
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  
  return `${month}-${day} ${hours}:${minutes}`;
} 