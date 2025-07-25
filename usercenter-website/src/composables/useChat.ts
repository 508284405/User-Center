import { ref, reactive, nextTick, readonly } from 'vue';
import { ElMessage } from 'element-plus';
import { Message, MessageType, MessageStatus, BotChatSSERequest } from '@/types/chat';
import { createUserMessage, createAssistantMessage, saveMessagesToStorage, loadMessagesFromStorage, scrollToBottom } from '@/utils/chat';
import { useSSE } from './useSSE';
import { chatMessageApi } from '@/api/smartcs/chatMessage';

export function useChat(sessionId?: string) {
  const messages = ref<Message[]>([]);
  const isLoading = ref(false);
  const currentSessionId = ref(sessionId);
  
  const { sendChatMessage, isConnecting, isConnected, error: sseError } = useSSE();
  
  // 加载会话消息历史
  const loadMessages = async (targetSessionId: string) => {
    if (!targetSessionId) return;
    
    try {
      isLoading.value = true;
      
      // 先从本地存储加载
      const localMessages = loadMessagesFromStorage(targetSessionId);
      if (localMessages.length > 0) {
        messages.value = localMessages;
      }
      
      // 从服务器获取最新消息
      const serverMessages = await chatMessageApi.getSessionMessages(targetSessionId);
      if (serverMessages && serverMessages.length > 0) {
        // 转换服务器消息格式为本地Message格式
        const convertedMessages: Message[] = serverMessages.map(msg => ({
          id: msg.msgId || String(Date.now()),
          sessionId: targetSessionId,
          content: msg.content || '',
          type: msg.chatType as MessageType,
          status: MessageStatus.RECEIVED,
          timestamp: msg.createdAt ? parseInt(String(msg.createdAt)) : Date.now(),
          metadata: {
            originalMessage: msg
          }
        }));
        
        // 按照createdAt字段进行倒序排序（最新消息在前）
        convertedMessages.sort((a, b) => a.timestamp - b.timestamp);
        
        messages.value = convertedMessages;
        // 保存到本地存储
        saveMessagesToStorage(targetSessionId, convertedMessages);
      }
      
    } catch (error) {
      console.error('加载消息历史失败:', error);
      ElMessage.error('加载消息历史失败');
    } finally {
      isLoading.value = false;
      // 滚动到底部
      await nextTick();
      scrollToBottom();
    }
  };
  
  // 发送消息
  const sendMessage = async (
    content: string,
    botId?: number,
    knowledgeBaseId?: number,
    contentId?: number
  ) => {
    if (!content.trim() || !currentSessionId.value || isLoading.value) {
      return;
    }
    
    // 设置加载状态
    isLoading.value = true;
    
    // 创建用户消息
    const userMessage = createUserMessage(content.trim(), currentSessionId.value);
    messages.value.push(userMessage);
    
    // 保存到本地存储
    saveMessagesToStorage(currentSessionId.value, messages.value);
    
    // 滚动到底部
    await nextTick();
    scrollToBottom();
    
    // 创建助手消息占位符，确保thinkingContent正确初始化
    const assistantMessage = createAssistantMessage('', currentSessionId.value);
    assistantMessage.status = MessageStatus.SENDING;
    assistantMessage.thinkingContent = ''; // 确保初始化为空字符串
    messages.value.push(assistantMessage);
    
    console.log('发送消息开始 - sessionId:', currentSessionId.value, 'botId:', botId, 'knowledgeBaseId:', knowledgeBaseId, 'contentId:', contentId);
    
    try {
      // 构建SSE请求
      const request: BotChatSSERequest = {
        sessionId: currentSessionId.value,
        question: content.trim(),
        targetBotId: botId,
        knowledgeBaseId,
        contentId,
        includeHistory: true
      };
      
      // 发送SSE请求
      await sendChatMessage(
        request,
        
        // 处理接收到的消息数据
        (data) => {
          console.log('收到SSE数据:', data);
          if (data && typeof data === 'object') {
            // 找到助手消息在数组中的索引
            const assistantMessageIndex = messages.value.findIndex(msg => msg.id === assistantMessage.id);
            
            // 根据消息类型处理
            if (data.type === 'progress') {
              // data结构 {data: '开始处理您的问题...', id: 'progress_53144604610019328_1748831710934', timestamp: 1748831710934, type: 'progress'}
              // 思考过程消息
              if (data.data && assistantMessageIndex !== -1) {
                console.log('处理progress消息:', data.data);
                
                // 创建新的消息对象，确保Vue响应式更新
                const updatedMessage = { 
                  ...assistantMessage,
                  thinkingContent: (assistantMessage.thinkingContent || '') + data.data,
                  status: MessageStatus.SENDING,
                  timestamp: Date.now()
                };
                
                // 替换数组中的消息对象，触发响应式更新
                messages.value.splice(assistantMessageIndex, 1, updatedMessage);
                
                // 更新本地引用
                Object.assign(assistantMessage, updatedMessage);
                
                console.log('thinkingContent已更新:', assistantMessage.thinkingContent);
              }
            } else if (data.type === 'data') {
              // data结构 {data: {answer: '回答内容', finished: false}, id: 'data_53144604610019328_1748831719514', timestamp: 1748831719514, type: 'data'}
              // 实际回复内容
              if (data.data && data.data.answer && assistantMessageIndex !== -1) {
                console.log('处理data消息:', data.data.answer);
                
                // 创建新的消息对象，确保Vue响应式更新
                const updatedMessage = { 
                  ...assistantMessage,
                  content: assistantMessage.content + data.data.answer,
                  status: MessageStatus.RECEIVED,
                  timestamp: Date.now()
                };
                
                // 替换数组中的消息对象，触发响应式更新
                messages.value.splice(assistantMessageIndex, 1, updatedMessage);
                
                // 更新本地引用
                Object.assign(assistantMessage, updatedMessage);
              }
            } else if (data.type === 'complete') {
              // data结构 {data: {answer: '回答内容', finished: true}, id: 'complete_53144604610019328_1748831723100', timestamp: 1748831723100, type: 'complete'}
              // 完成标识
              console.log('收到complete消息:', data);
              
              if (assistantMessageIndex !== -1) {
                // 创建新的消息对象，确保Vue响应式更新
                const updatedMessage = { 
                  ...assistantMessage,
                  status: MessageStatus.RECEIVED,
                  timestamp: Date.now()
                };
                
                if (data.data && data.data.answer) {
                  updatedMessage.content = data.data.answer;
                  console.log('助手消息最终内容:', updatedMessage.content);
                }
                
                // 替换数组中的消息对象，触发响应式更新
                messages.value.splice(assistantMessageIndex, 1, updatedMessage);
                
                // 更新本地引用
                Object.assign(assistantMessage, updatedMessage);
              }
              
              // 更新用户消息状态为已发送
              const userMessageIndex = messages.value.findIndex(msg => msg.id === userMessage.id);
              if (userMessageIndex !== -1) {
                const updatedUserMessage = { ...userMessage, status: MessageStatus.SENT };
                messages.value.splice(userMessageIndex, 1, updatedUserMessage);
                Object.assign(userMessage, updatedUserMessage);
              }
              
              // 保存到本地存储
              saveMessagesToStorage(currentSessionId.value!, messages.value);
              
              // 清除加载状态
              isLoading.value = false;
              
              console.log('消息发送完成 - 助手消息状态:', assistantMessage.status, '内容长度:', assistantMessage.content?.length);
              
              // 强制触发DOM更新
              nextTick(() => scrollToBottom());
              
              return; // 直接返回，避免后续重复处理
            }
            
            // 保存到本地存储
            saveMessagesToStorage(currentSessionId.value!, messages.value);
            
            // 强制触发DOM更新
            nextTick(() => scrollToBottom());
          }
        },
        
        // 处理错误
        (error) => {
          console.error('SSE消息接收错误:', error);
          
          // 找到助手消息在数组中的索引并更新
          const assistantMessageIndex = messages.value.findIndex(msg => msg.id === assistantMessage.id);
          if (assistantMessageIndex !== -1) {
            const updatedMessage = { 
              ...assistantMessage,
              status: MessageStatus.ERROR,
              content: assistantMessage.content || '抱歉，消息发送失败，请重试。'
            };
            messages.value.splice(assistantMessageIndex, 1, updatedMessage);
            Object.assign(assistantMessage, updatedMessage);
          }
          
          ElMessage.error('消息发送失败');
          isLoading.value = false;
        },
        
        // 处理完成
        () => {
          console.log('SSE消息流完成，当前助手消息状态:', assistantMessage.status);
          
          // 更新助手消息状态
          const assistantMessageIndex = messages.value.findIndex(msg => msg.id === assistantMessage.id);
          if (assistantMessage.status === MessageStatus.SENDING && assistantMessageIndex !== -1) {
            console.log('强制设置助手消息状态为已接收');
            const updatedMessage = { 
              ...assistantMessage,
              status: MessageStatus.RECEIVED
            };
            messages.value.splice(assistantMessageIndex, 1, updatedMessage);
            Object.assign(assistantMessage, updatedMessage);
          }
          
          // 更新用户消息状态为已发送
          const userMessageIndex = messages.value.findIndex(msg => msg.id === userMessage.id);
          if (userMessageIndex !== -1) {
            const updatedUserMessage = { ...userMessage, status: MessageStatus.SENT };
            messages.value.splice(userMessageIndex, 1, updatedUserMessage);
            Object.assign(userMessage, updatedUserMessage);
          }
          
          // 保存到本地存储
          saveMessagesToStorage(currentSessionId.value!, messages.value);
          
          // 清除加载状态
          isLoading.value = false;
          
          console.log('消息发送完成');
        },
        
        // 处理连接关闭
        () => {
          console.log('SSE连接关闭，当前助手消息状态:', assistantMessage.status);
          
          // 如果消息还在发送状态，标记为已接收
          const assistantMessageIndex = messages.value.findIndex(msg => msg.id === assistantMessage.id);
          if (assistantMessage.status === MessageStatus.SENDING && assistantMessageIndex !== -1) {
            console.log('连接关闭时强制设置助手消息状态为已接收');
            const updatedMessage = { 
              ...assistantMessage,
              status: MessageStatus.RECEIVED
            };
            messages.value.splice(assistantMessageIndex, 1, updatedMessage);
            Object.assign(assistantMessage, updatedMessage);
          }
          
          // 清除加载状态
          isLoading.value = false;
          
          console.log('SSE连接已关闭');
        }
      );
      
    } catch (error) {
      console.error('发送消息失败:', error);
      
      // 更新助手消息状态
      const assistantMessageIndex = messages.value.findIndex(msg => msg.id === assistantMessage.id);
      if (assistantMessageIndex !== -1) {
        const updatedAssistantMessage = { 
          ...assistantMessage,
          status: MessageStatus.ERROR,
          content: assistantMessage.content || '抱歉，消息发送失败，请重试。'
        };
        messages.value.splice(assistantMessageIndex, 1, updatedAssistantMessage);
        Object.assign(assistantMessage, updatedAssistantMessage);
      }
      
      // 更新用户消息状态
      const userMessageIndex = messages.value.findIndex(msg => msg.id === userMessage.id);
      if (userMessageIndex !== -1) {
        const updatedUserMessage = { ...userMessage, status: MessageStatus.ERROR };
        messages.value.splice(userMessageIndex, 1, updatedUserMessage);
        Object.assign(userMessage, updatedUserMessage);
      }
      
      ElMessage.error('消息发送失败');
      isLoading.value = false;
    }
  };
  
  // 重试发送消息
  const retryMessage = async (messageId: string) => {
    if (isLoading.value) {
      return;
    }
    
    const messageIndex = messages.value.findIndex(msg => msg.id === messageId);
    if (messageIndex === -1) return;
    
    const message = messages.value[messageIndex];
    if (message.type !== MessageType.USER) return;
    
    // 重置消息状态
    message.status = MessageStatus.SENDING;
    
    // 查找并移除对应的助手回复（如果存在且是错误状态）
    const nextMessage = messages.value[messageIndex + 1];
    if (nextMessage && 
        nextMessage.type === MessageType.ASSISTANT && 
        nextMessage.status === MessageStatus.ERROR) {
      messages.value.splice(messageIndex + 1, 1);
    }
    
    // 重新发送
    await sendMessage(message.content);
  };
  
    // 切换会话
  const switchSession = async (sessionId: string) => {
    currentSessionId.value = sessionId;
    await loadMessages(sessionId);
  };

  // 设置当前会话ID（不加载消息）
  const setCurrentSessionId = (sessionId: string) => {
    currentSessionId.value = sessionId;
  };

  // 清空消息
  const clearMessages = () => {
    messages.value = [];
    if (currentSessionId.value) {
      saveMessagesToStorage(currentSessionId.value, []);
    }
  };
  
  // 添加消息到列表
  const addMessage = (message: Message) => {
    messages.value.push(message);
    if (currentSessionId.value) {
      saveMessagesToStorage(currentSessionId.value, messages.value);
    }
  };
  
  return {
    messages: readonly(messages),
    isLoading: readonly(isLoading),
    currentSessionId: readonly(currentSessionId),
    isConnecting,
    isConnected,
    sseError,
    loadMessages,
    sendMessage,
    retryMessage,
    switchSession,
    setCurrentSessionId,
    clearMessages,
    addMessage
  };
} 