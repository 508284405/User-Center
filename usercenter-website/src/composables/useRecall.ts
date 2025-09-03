import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { sendRecallMessage, registerMessageHandler, RecallMessage, RecallNotification } from '@/utils/chatWebSocket';
import { Message } from '@/types/chat';
import { updateMessageRecallStatus } from '@/utils/chat';

/**
 * 撤回消息的组合式函数
 */
export function useRecall() {
  const isRecalling = ref(false);
  
  // 撤回消息
  const recallMessage = async (messageId: string, sessionId?: string) => {
    if (isRecalling.value) {
      ElMessage.warning('撤回操作进行中...');
      return false;
    }
    
    try {
      isRecalling.value = true;
      
      const recallMsg: RecallMessage = {
        msgId: messageId,
        sessionId: sessionId,
        userId: localStorage.getItem('userId') || '',
        reason: '用户撤回',
        recallTime: Date.now(),
        type: 'RECALL'
      };
      
      const success = sendRecallMessage(recallMsg);
      if (success) {
        ElMessage.success('撤回请求已发送');
        return true;
      } else {
        ElMessage.error('撤回失败，请检查网络连接');
        return false;
      }
    } catch (error) {
      console.error('撤回消息失败:', error);
      ElMessage.error('撤回消息失败');
      return false;
    } finally {
      isRecalling.value = false;
    }
  };
  
  // 处理撤回通知
  const handleRecallNotification = (messages: Message[], notification: RecallNotification) => {
    const messageIndex = messages.findIndex(msg => msg.id === notification.msgId);
    if (messageIndex !== -1) {
      // 更新消息的撤回状态
      const updatedMessage = updateMessageRecallStatus(messages[messageIndex], {
        isRecalled: true,
        recalledAt: notification.recallTime,
        recalledBy: notification.userId,
        recallReason: notification.reason
      });
      
      messages.splice(messageIndex, 1, updatedMessage);
      console.log(`消息 ${notification.msgId} 已被撤回`);
    }
  };
  
  // 注册撤回消息处理器
  const registerRecallHandler = (messages: Message[]) => {
    return registerMessageHandler('recall', (notification: RecallNotification) => {
      handleRecallNotification(messages, notification);
    });
  };
  
  return {
    isRecalling,
    recallMessage,
    handleRecallNotification,
    registerRecallHandler
  };
}