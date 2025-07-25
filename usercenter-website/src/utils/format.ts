/**
 * 日期格式化工具函数
 * 支持字符串、时间戳、Date 对象输入，默认格式 yyyy-MM-dd HH:mm:ss
 * 支持本地化，默认 locale 为 zh-CN
 * @param date 原始日期
 * @param format 格式字符串（可选）
 * @param locale 区域（可选，默认 zh-CN）
 * @returns 格式化后的字符串
 */
export function formatDate(
  date: string | number | Date | undefined | null,
  format: string = 'yyyy-MM-dd HH:mm:ss',
  locale: string = 'zh-CN'
): string {
  if (!date) return '--';
  let d: Date;
  if (date instanceof Date) {
    d = date;
  } else if (typeof date === 'number') {
    d = new Date(date);
  } else if (typeof date === 'string') {
    // 兼容 ISO 字符串或 yyyy-MM-dd HH:mm:ss
    d = new Date(date.replace(/-/g, '/'));
    if (isNaN(d.getTime())) {
      d = new Date(date);
    }
  } else {
    return '--';
  }
  if (isNaN(d.getTime())) return '--';

  // 格式化实现
  const pad = (n: number) => n.toString().padStart(2, '0');
  const map: Record<string, string> = {
    yyyy: d.getFullYear().toString(),
    MM: pad(d.getMonth() + 1),
    dd: pad(d.getDate()),
    HH: pad(d.getHours()),
    mm: pad(d.getMinutes()),
    ss: pad(d.getSeconds()),
  };
  let result = format;
  Object.keys(map).forEach((key) => {
    result = result.replace(key, map[key]);
  });
  // 本地化支持
  if (locale.startsWith('zh')) {
    if (format === 'yyyy-MM-dd HH:mm:ss') {
      result = `${map['yyyy']}年${map['MM']}月${map['dd']}日 ${map['HH']}:${map['mm']}:${map['ss']}`;
    }
  }
  return result;
}
