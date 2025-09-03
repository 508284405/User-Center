import { Message, MessageSendStatus, MessageContentType, MessageDeleteType } from '@/types/chat';

/**
 * 消息工具函数
 */
export class MessageUtils {
  
  /**
   * 获取消息状态图标
   */
  static getStatusIcon(status: number): string {
    switch (status) {
      case MessageSendStatus.SENDING:
        return '⏳';
      case MessageSendStatus.DELIVERED:
        return '✓';
      case MessageSendStatus.SEND_FAILED:
        return '❌';
      case MessageSendStatus.READ:
        return '✓✓';
      default:
        return '❓';
    }
  }

  /**
   * 获取消息状态文本
   */
  static getStatusText(status: number): string {
    switch (status) {
      case MessageSendStatus.SENDING:
        return '发送中...';
      case MessageSendStatus.DELIVERED:
        return '已送达';
      case MessageSendStatus.SEND_FAILED:
        return '发送失败';
      case MessageSendStatus.READ:
        return '已读';
      default:
        return '未知状态';
    }
  }

  /**
   * 获取消息类型文本
   */
  static getTypeText(type: number): string {
    switch (type) {
      case MessageContentType.TEXT:
        return '文本';
      case MessageContentType.IMAGE:
        return '图片';
      case MessageContentType.ORDER_CARD:
        return '订单卡片';
      case MessageContentType.SYSTEM:
        return '系统消息';
      default:
        return '未知类型';
    }
  }

  /**
   * 格式化时间显示
   */
  static formatTime(timestamp: number): string {
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    
    // 一分钟内显示"刚刚"
    if (diff < 60000) {
      return '刚刚';
    }
    
    // 一小时内显示"X分钟前"
    if (diff < 3600000) {
      return `${Math.floor(diff / 60000)}分钟前`;
    }
    
    // 今天显示时间
    if (date.toDateString() === now.toDateString()) {
      return date.toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      });
    }
    
    // 昨天显示"昨天 HH:mm"
    const yesterday = new Date(now.getTime() - 86400000);
    if (date.toDateString() === yesterday.toDateString()) {
      return `昨天 ${date.toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })}`;
    }
    
    // 其他显示完整日期时间
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * 检查消息是否可以撤回
   */
  static canRecall(message: Message, currentUserId: string): boolean {
    // 已撤回的消息不能再撤回
    if (message.isRecalled) {
      return false;
    }
    
    // 只能撤回自己的消息
    if (message.fromUserId !== currentUserId) {
      return false;
    }
    
    // 检查时间限制（2分钟）
    const now = Date.now();
    const messageTime = message.createdAt || message.timestamp;
    return (now - messageTime) <= 120000; // 2分钟
  }

  /**
   * 检查消息是否可以编辑
   */
  static canEdit(message: Message, currentUserId: string): boolean {
    // 只有文本消息可以编辑
    if (message.msgType !== MessageContentType.TEXT && message.msgType !== undefined) {
      return false;
    }
    
    // 已撤回的消息不能编辑
    if (message.isRecalled) {
      return false;
    }
    
    // 已删除的消息不能编辑
    if (message.isDeletedBySender || message.isDeletedByReceiver) {
      return false;
    }
    
    // 只能编辑自己的消息
    if (message.fromUserId !== currentUserId) {
      return false;
    }
    
    // 检查时间限制（30分钟）
    const now = Date.now();
    const messageTime = message.createdAt || message.timestamp;
    return (now - messageTime) <= 1800000; // 30分钟
  }

  /**
   * 检查消息是否可以删除
   */
  static canDelete(message: Message, currentUserId: string): boolean {
    // 已撤回的消息不能删除
    if (message.isRecalled) {
      return false;
    }
    
    // 已删除的消息不能再删除
    if (message.isDeletedBySender || message.isDeletedByReceiver) {
      return false;
    }
    
    return true;
  }

  /**
   * 检查消息是否可以回复
   */
  static canReply(message: Message): boolean {
    // 已撤回的消息不能回复
    if (message.isRecalled) {
      return false;
    }
    
    // 已删除的消息不能回复
    if (message.isDeletedBySender || message.isDeletedByReceiver) {
      return false;
    }
    
    return true;
  }

  /**
   * 检查消息是否可以重试
   */
  static canRetry(message: Message, currentUserId: string): boolean {
    // 只能重试自己发送失败的消息
    if (message.fromUserId !== currentUserId) {
      return false;
    }
    
    // 只有发送失败的消息可以重试
    if (message.sendStatus !== MessageSendStatus.SEND_FAILED) {
      return false;
    }
    
    // 检查重试次数限制
    const retryCount = message.retryCount || 0;
    return retryCount < 3;
  }

  /**
   * 截取引用内容摘要
   */
  static getQuotedSummary(content: string, maxLength: number = 50): string {
    if (!content) return '';
    
    if (content.length <= maxLength) {
      return content;
    }
    
    return content.substring(0, maxLength - 3) + '...';
  }

  /**
   * 生成消息ID
   */
  static generateMessageId(): string {
    return `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * 检查是否为自己的消息
   */
  static isOwnMessage(message: Message, currentUserId: string): boolean {
    return message.fromUserId === currentUserId;
  }

  /**
   * 获取消息发送者显示名称
   */
  static getSenderDisplayName(message: Message, userMap: Map<string, any> = new Map()): string {
    if (!message.fromUserId) {
      return '系统';
    }
    
    const user = userMap.get(message.fromUserId);
    return user?.nickname || user?.username || `用户${message.fromUserId}`;
  }

  /**
   * 检查消息是否需要显示时间
   */
  static shouldShowTime(currentMessage: Message, previousMessage?: Message): boolean {
    if (!previousMessage) {
      return true;
    }
    
    // 如果两条消息间隔超过5分钟，显示时间
    const currentTime = currentMessage.createdAt || currentMessage.timestamp;
    const previousTime = previousMessage.createdAt || previousMessage.timestamp;
    const timeDiff = currentTime - previousTime;
    return timeDiff > 300000; // 5分钟
  }

  /**
   * 检查消息是否需要显示发送者
   */
  static shouldShowSender(currentMessage: Message, previousMessage?: Message): boolean {
    if (!previousMessage) {
      return true;
    }
    
    // 如果发送者不同，显示发送者
    if (currentMessage.fromUserId !== previousMessage.fromUserId) {
      return true;
    }
    
    // 如果时间间隔超过5分钟，显示发送者
    const currentTime = currentMessage.createdAt || currentMessage.timestamp;
    const previousTime = previousMessage.createdAt || previousMessage.timestamp;
    const timeDiff = currentTime - previousTime;
    return timeDiff > 300000; // 5分钟
  }

  /**
   * 获取引用显示文本
   */
  static getQuoteDisplayText(message: Message): string | null {
    if (!message.replyToMsgId || !message.quotedContent) {
      return null;
    }
    
    const fromUser = message.quotedFromUser || '未知用户';
    const summary = this.getQuotedSummary(message.quotedContent);
    
    return `回复 ${fromUser}: ${summary}`;
  }

  /**
   * 获取编辑历史标识
   */
  static getEditHistoryText(message: Message): string | null {
    if (!message.isEdited) {
      return null;
    }
    
    const editCount = message.editCount || 1;
    return `已编辑 ${editCount} 次`;
  }

  /**
   * 检查消息是否为回复消息
   */
  static isReplyMessage(message: Message): boolean {
    return !!(message.replyToMsgId && message.replyToMsgId.trim());
  }

  /**
   * 检查消息是否已编辑
   */
  static isEditedMessage(message: Message): boolean {
    return Boolean(message.isEdited);
  }

  /**
   * 检查消息是否已删除
   */
  static isDeletedMessage(message: Message): boolean {
    return Boolean(message.isDeletedBySender || message.isDeletedByReceiver);
  }

  /**
   * 检查消息是否已撤回
   */
  static isRecalledMessage(message: Message): boolean {
    return Boolean(message.isRecalled);
  }
}