import { format } from 'date-fns';

/**
 * 格式化日期时间
 * @param date 日期字符串或Date对象
 * @param pattern 格式模式，默认为 'yyyy-MM-dd HH:mm:ss'
 * @returns 格式化后的日期字符串
 */
export function formatDateTime(date: string | Date | null | undefined, pattern: string = 'yyyy-MM-dd HH:mm:ss'): string {
  if (!date) return '';
  
  try {
    const dateObj = typeof date === 'string' ? new Date(date) : date;
    return format(dateObj, pattern);
  } catch (error) {
    console.error('Date formatting error:', error);
    return '';
  }
}

/**
 * 格式化日期
 * @param date 日期字符串或Date对象
 * @returns 格式化后的日期字符串 (yyyy-MM-dd)
 */
export function formatDate(date: string | Date | null | undefined): string {
  return formatDateTime(date, 'yyyy-MM-dd');
}

/**
 * 格式化时间
 * @param date 日期字符串或Date对象
 * @returns 格式化后的时间字符串 (HH:mm:ss)
 */
export function formatTime(date: string | Date | null | undefined): string {
  return formatDateTime(date, 'HH:mm:ss');
}

/**
 * 获取相对时间描述
 * @param date 日期字符串或Date对象
 * @returns 相对时间描述，如 "2分钟前", "1小时前", "昨天", "3天前"
 */
export function getRelativeTime(date: string | Date | null | undefined): string {
  if (!date) return '';
  
  try {
    const dateObj = typeof date === 'string' ? new Date(date) : date;
    const now = new Date();
    const diffMs = now.getTime() - dateObj.getTime();
    const diffSeconds = Math.floor(diffMs / 1000);
    const diffMinutes = Math.floor(diffSeconds / 60);
    const diffHours = Math.floor(diffMinutes / 60);
    const diffDays = Math.floor(diffHours / 24);
    
    if (diffSeconds < 60) {
      return '刚刚';
    } else if (diffMinutes < 60) {
      return `${diffMinutes}分钟前`;
    } else if (diffHours < 24) {
      return `${diffHours}小时前`;
    } else if (diffDays === 1) {
      return '昨天';
    } else if (diffDays < 7) {
      return `${diffDays}天前`;
    } else {
      return formatDate(dateObj);
    }
  } catch (error) {
    console.error('Relative time calculation error:', error);
    return '';
  }
}