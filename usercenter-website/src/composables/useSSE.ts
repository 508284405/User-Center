import { ref, onUnmounted, readonly } from 'vue';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { BotChatSSERequest, SSEEvent } from '@/types/chat';

export function useSSE() {
  const isConnecting = ref(false);
  const isConnected = ref(false);
  const error = ref<string | null>(null);
  
  let abortController: AbortController | null = null;
  
  // 建立SSE连接并发送聊天消息
  const sendChatMessage = async (
    request: BotChatSSERequest,
    onMessage: (data: any) => void,
    onError?: (error: any) => void,
    onDone?: () => void,
    onClose?: () => void
  ) => {
    console.log('[sendChatMessage] 调用，isConnecting=', isConnecting.value, 'isConnected=', isConnected.value);
    // 如果正在连接或已连接，先断开现有连接
    if (isConnecting.value || isConnected.value) {
      return;
    }
    
    try {
      isConnecting.value = true;
      error.value = null;
      
      // 创建新的 AbortController
      abortController = new AbortController();
      
      const token = localStorage.getItem('token');
      
      await fetchEventSource('http://localhost:8082/api/sse/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': token ? `Bearer ${token}` : '',
        },
        body: JSON.stringify(request),
        signal: abortController.signal,
        
        async onopen(response) {
          if (response.ok) {
            isConnected.value = true;
            isConnecting.value = false;
            console.log('SSE 连接已建立');
          } else {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
          }
        },
        
        onmessage(event) {
          console.log('[SSE onmessage] raw:', event.data)
          try {
            if (event.data === 'complete') {
              console.log('SSE 消息流结束');
              onDone?.();
              disconnect();
              return;
            }
            
            // 解析SSE消息
            const data = JSON.parse(event.data);
            console.log('收到SSE消息:', data);
            onMessage(data);
            
          } catch (parseError) {
            console.error('解析SSE消息失败:', parseError);
            error.value = '解析消息失败';
            onError?.(parseError);
          }
        },
        
        onerror(err) {
          console.error('SSE 连接错误:', err);
          isConnected.value = false;
          isConnecting.value = false;
          error.value = err.message || 'SSE 连接失败';
          onError?.(err);
          
          // 如果是主动断开连接，不重连
          if (abortController?.signal.aborted) {
            return;
          }
          
          // 自动重连逻辑可以在这里实现
          throw err; // 停止重连
        },
        
        onclose() {
          console.log('SSE 连接已关闭');
          isConnected.value = false;
          isConnecting.value = false;
          onClose?.();
        }
      });
      
    } catch (err: any) {
      console.error('发送SSE聊天消息失败:', err);
      isConnected.value = false;
      isConnecting.value = false;
      error.value = err.message || '发送消息失败';
      onError?.(err);
    }
  };
  
  // 断开SSE连接
  const disconnect = () => {
    if (abortController) {
      abortController.abort();
      abortController = null;
    }
    isConnected.value = false;
    isConnecting.value = false;
    console.log('SSE 连接已断开');
  };
  
  // 重连
  const reconnect = (
    request: BotChatSSERequest,
    onMessage: (data: any) => void,
    onError?: (error: any) => void,
    onDone?: () => void,
    onClose?: () => void
  ) => {
    disconnect();
    setTimeout(() => {
      sendChatMessage(request, onMessage, onError, onDone, onClose);
    }, 1000);
  };
  
  // 组件卸载时断开连接
  onUnmounted(() => {
    disconnect();
  });
  
  return {
    isConnecting: readonly(isConnecting),
    isConnected: readonly(isConnected),
    error: readonly(error),
    sendChatMessage,
    disconnect,
    reconnect
  };
} 