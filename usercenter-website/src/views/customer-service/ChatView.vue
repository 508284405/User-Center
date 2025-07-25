<template>
  <div class="chat-container">
    <div class="chat-layout">
      <!-- 左侧会话列表 -->
      <div class="sessions-panel">
        <div class="panel-header">
          <h3>客服会话列表</h3>
          <el-tag v-if="connectionStatus === 'connected'" type="success">已连接</el-tag>
          <el-tag v-else-if="connectionStatus === 'connecting'" type="warning">连接中</el-tag>
          <el-tag v-else type="danger">未连接</el-tag>
        </div>
        
        <div class="sessions-filter">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索会话"
            prefix-icon="Search"
            clearable
          />
        </div>
        
        <div class="sessions-list" v-loading="sessionsLoading">
          <div
            v-for="session in filteredSessions"
            :key="session.sessionId"
            class="session-item"
            :class="{ active: currentSession?.sessionId === session.sessionId }"
            @click="selectSession(session)"
          >
            <div class="session-info">
              <div class="session-title">
                <span>客户{{ session.customerId }}</span>
                <el-tag size="small" :type="getStatusType(session.status)">
                  {{ getStatusText(session.status) }}
                </el-tag>
              </div>
              <div class="session-message">{{ truncateMessage(session.lastMessage) }}</div>
            </div>
            <div class="session-time">{{ formatTime(session.lastMsgTime) }}</div>
          </div>
          
          <div v-if="filteredSessions.length === 0" class="empty-sessions">
            <p>暂无会话</p>
          </div>
        </div>
      </div>
      
      <!-- 右侧聊天区域 -->
      <div class="chat-panel">
        <template v-if="currentSession">
          <div class="chat-header">
            <div class="customer-info">
              <h3>客户{{ currentSession.customerId }}</h3>
              <el-tag size="small" :type="getStatusType(currentSession.status)">
                {{ getStatusText(currentSession.status) }}
              </el-tag>
            </div>
            <div class="header-actions">
              <el-button type="warning" size="small" @click="confirmCloseSession" :disabled="currentSession.status === 'CLOSED'">
                结束会话
              </el-button>
              <el-button type="primary" size="small" @click="refreshMessages">
                刷新消息
              </el-button>
            </div>
          </div>
          
          <div class="chat-messages" ref="messagesContainer" v-loading="messagesLoading">
            <div v-if="messages.length === 0" class="empty-messages">
              <p>暂无消息记录</p>
            </div>
            
            <div v-else>
              <div v-if="hasMoreMessages" class="load-more-container">
                <el-button type="primary" plain size="small" @click="loadMoreMessages" :loading="loadingMoreMessages">
                  加载更多历史消息
                </el-button>
              </div>
              
              <template v-for="(message, index) in processedMessages" :key="message.msgId || index">
                <!-- 时间分隔条 -->
                <div v-if="message.isTimeHeader" class="time-divider">
                  <span>{{ message.timeHeader }}</span>
                </div>
                
                <!-- 消息项 -->
                <div v-else class="message-item" :class="{ 
                  'sender-message': isSender(message), 
                  'receiver-message': !isSender(message) 
                }">
                  <!-- 接收方头像 -->
                  <div v-if="!isSender(message)" class="avatar-container">
                    <el-avatar :size="32" :src="message.senderAvatar">
                      {{ getAvatarFallback(message) }}
                    </el-avatar>
                  </div>
                  
                  <div class="message-content-wrapper">
                    <div class="message-sender">
                      {{ isSender(message) ? '我' : getSenderName(message) }}
                      <span class="message-time">{{ formatTime(message.createdAt) }}</span>
                    </div>
                    
                    <div class="message-content" @mouseenter="message.showCopy = true" @mouseleave="message.showCopy = false">
                      <div v-if="message.msgType === 0" class="text-message">
                        {{ message.content }}
                        <el-button 
                          v-if="message.showCopy && message.content" 
                          class="copy-button" 
                          size="small" 
                          circle 
                          icon="DocumentCopy"
                          @click="copyMessageContent(message.content)"
                        />
                      </div>
                      <div v-else-if="message.msgType === 1" class="image-message">
                        <el-image :src="message.content" :preview-src-list="[message.content]" fit="cover" />
                      </div>
                      <div v-else class="other-message">
                        {{ message.content }}
                      </div>
                    </div>
                  </div>
                  
                  <!-- 发送方头像 -->
                  <div v-if="isSender(message)" class="avatar-container">
                    <el-avatar :size="32" :src="message.senderAvatar">
                      {{ getAvatarFallback(message) }}
                    </el-avatar>
                  </div>
                </div>
              </template>
            </div>
          </div>
          
          <div class="chat-input" :class="{ disabled: currentSession.status === 'CLOSED' }">
            <div class="input-tools">
              <el-tooltip content="发送图片">
                <el-button type="primary" :icon="PictureRounded" circle size="small" @click="handleImageUpload" :disabled="currentSession.status === 'CLOSED'" />
              </el-tooltip>
              <input
                ref="fileInput"
                type="file"
                accept="image/*"
                style="display: none"
                @change="onFileSelected"
              />
            </div>
            
            <div class="input-area">
              <el-input
                v-model="messageInput"
                type="textarea"
                :rows="3"
                placeholder="输入消息..."
                resize="none"
                :disabled="currentSession.status === 'CLOSED'"
                @keydown.enter.prevent="sendMessage"
              />
            </div>
            
            <div class="input-actions">
              <el-button
                type="primary"
                @click="sendMessage"
                :disabled="!messageInput.trim() || currentSession.status === 'CLOSED'"
              >
                发送
              </el-button>
            </div>
          </div>
        </template>
        
        <div v-else class="no-session-selected">
          <el-empty description="请选择一个会话" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { PictureRounded, DocumentCopy } from '@element-plus/icons-vue';
import { chatSessionApi, SessionVO } from '@/api/smartcs/chatSession';
import { chatMessageApi, MessageVO, SendMessageRequest, GetMessagesParams } from '@/api/smartcs/chatMessage';
import { initWebSocketConnection, connectionStatus, sendChatMessage, registerMessageHandler, cleanup } from '@/utils/chatWebSocket';
import { formatDate } from '@/utils/format';

// 路由
const route = useRoute();
const router = useRouter();

// 加载状态
const sessionsLoading = ref(false);
const messagesLoading = ref(false);

// 会话列表
const allSessions = ref<SessionVO[]>([]);
const searchKeyword = ref('');
const currentSession = ref<SessionVO | null>(null);

// 消息列表 - 修改为any[]类型以适应自定义消息格式
const messages = ref<any[]>([]);
const messageInput = ref('');
const messagesContainer = ref<HTMLElement | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);
const loadingMoreMessages = ref(false);
const hasMoreMessages = ref(true); // 是否还有更多历史消息
const oldestMessageId = ref<string | null>(null); // 当前加载的最早消息ID

// 过滤后的会话列表
const filteredSessions = computed(() => {
  if (!searchKeyword.value) {
    return allSessions.value;
  }
  
  const keyword = searchKeyword.value.toLowerCase();
  return allSessions.value.filter(session => 
    String(session.sessionId).includes(keyword) || 
    String(session.customerId).includes(keyword) ||
    (session.lastMessage && session.lastMessage.toLowerCase().includes(keyword))
  );
});

// 获取状态类型
const getStatusType = (status: string | undefined) => {
  switch (status) {
    case 'PENDING':
      return 'warning';
    case 'ACTIVE':
      return 'success';
    case 'CLOSED':
      return 'info';
    default:
      return '';
  }
};

// 获取状态文本
const getStatusText = (status: string | undefined) => {
  switch (status) {
    case 'PENDING':
      return '待分配';
    case 'ACTIVE':
      return '活跃';
    case 'CLOSED':
      return '已关闭';
    default:
      return status || '';
  }
};

// 截断消息文本
const truncateMessage = (message: string | undefined) => {
  if (!message) return '';
  return message.length > 20 ? `${message.substring(0, 20)}...` : message;
};

// 判断消息是否为当前用户发送
const isSender = (message: any) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
  return message.senderId === userInfo.id?.toString();
};

// 获取发送者名称
const getSenderName = (message: any) => {
  return message.senderName || `用户${message.senderId}`;
};

// 获取头像占位符
const getAvatarFallback = (message: any) => {
  if (message.senderName && message.senderName.length > 0) {
    return message.senderName.charAt(0).toUpperCase();
  }
  return message.senderId.charAt(0).toUpperCase();
};

// 复制消息内容
const copyMessageContent = (content: string) => {
  navigator.clipboard.writeText(content)
    .then(() => {
      ElMessage.success('复制成功');
    })
    .catch(() => {
      ElMessage.error('复制失败');
    });
};

// 处理消息列表，添加时间分隔条
const processedMessages = computed(() => {
  const result: any[] = [];
  const TIME_INTERVAL = 5 * 60 * 1000; // 5分钟间隔
  
  messages.value.forEach((message, index) => {
    // 添加反应性属性
    if (!message.hasOwnProperty('showCopy')) {
      message = reactive({ ...message, showCopy: false });
    }
    
    // 判断是否需要添加时间分隔条
    if (index === 0 || isTimeGapSignificant(messages.value[index - 1], message)) {
      result.push({
        isTimeHeader: true,
        timeHeader: formatTime(message.createdAt, true),
      });
    }
    
    // 添加消息
    result.push(message);
  });
  
  return result;
});

// 判断两条消息的时间间隔是否足够大以添加时间分隔条
const isTimeGapSignificant = (prevMsg: any, currMsg: any) => {
  const TIME_INTERVAL = 5 * 60 * 1000; // 5分钟间隔
  
  if (!prevMsg || !prevMsg.createdAt || !currMsg || !currMsg.createdAt) {
    return false;
  }
  
  const prevTime = new Date(prevMsg.createdAt).getTime();
  const currTime = new Date(currMsg.createdAt).getTime();
  
  return Math.abs(currTime - prevTime) > TIME_INTERVAL;
};

// 格式化时间(增强版)
const formatTime = (date: any, isFullFormat = false) => {
  if (!date) return '';
  
  const messageDate = new Date(Number(date));
  const now = new Date();
  
  // 显示完整格式（用于时间分隔条）
  if (isFullFormat) {
    return messageDate.toLocaleString('zh-CN', { 
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
  
  // 如果是今天的消息，只显示时间
  if (messageDate.toDateString() === now.toDateString()) {
    return messageDate.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  }
  
  // 如果是昨天的消息，显示"昨天"和时间
  const yesterday = new Date(now);
  yesterday.setDate(now.getDate() - 1);
  if (messageDate.toDateString() === yesterday.toDateString()) {
    return `昨天 ${messageDate.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`;
  }
  
  // 其他情况，显示完整日期和时间
  return formatDate(date);
};

// 获取客服的会话列表
const fetchAgentSessions = async () => {
  sessionsLoading.value = true;
  try {
    // 获取当前登录用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    const agentId = userInfo.id;
    
    if (!agentId) {
      ElMessage.error('未获取到客服ID');
      return;
    }
    
    // 获取客服的活跃会话
    const sessions = await chatSessionApi.getAgentActiveSessions(agentId);
    allSessions.value = sessions;
  } catch (error) {
    console.error('获取客服会话列表失败:', error);
    ElMessage.error('获取客服会话列表失败');
  } finally {
    sessionsLoading.value = false;
  }
};

// 获取会话详情
const fetchSessionDetail = async (sessionId: string) => {
  try {
    const session = await chatSessionApi.getSessionDetail(sessionId);
    if (session) {
      currentSession.value = session;
    } else {
      ElMessage.error('获取会话详情失败');
    }
  } catch (error) {
    console.error('获取会话详情失败:', error);
    ElMessage.error('获取会话详情失败');
  }
};

// 工具函数：将createdAt统一转为时间戳number
function getTimestamp(val: any): number {
  if (!val) return 0;
  if (typeof val === 'number') return val;
  if (/^\d+$/.test(val)) return Number(val);
  return new Date(val).getTime();
}

// 获取会话消息历史
const fetchSessionMessages = async (sessionId: string) => {
  messagesLoading.value = true;
  try {
    // 获取会话消息历史
    const sessionMessages = await chatMessageApi.getSessionMessages(sessionId);
    
    // 重置消息列表状态
    hasMoreMessages.value = sessionMessages.length >= 20; // 如果返回20条，可能有更多
    oldestMessageId.value = sessionMessages.length > 0 ? 
      sessionMessages[sessionMessages.length - 1].msgId : null;
    
    // 将后端返回的消息转换为本地格式
    const formattedMessages = [];
    for (const msg of sessionMessages) {
      formattedMessages.push({
        msgId: msg.msgId || `temp-${Date.now()}`,
        sessionId: sessionId,
        senderId: (msg as any).senderId || '',
        senderName: (msg as any).senderName || '',
        senderRole: (msg as any).senderRole || 0,
        content: msg.content || '',
        msgType: (msg as any).msgType || 0,
        createdAt: msg.createdAt || new Date().toISOString(),
        timestamp: getTimestamp(msg.createdAt),
        showCopy: false
      });
    }
    // 按timestamp升序排序（旧→新，最新消息在下方）
    formattedMessages.sort((a, b) => a.timestamp - b.timestamp);
    messages.value = formattedMessages;
    // 滚动到底部
    await nextTick();
    scrollToBottom();
  } catch (error) {
    console.error('获取会话消息历史失败:', error);
    ElMessage.error('获取会话消息历史失败');
  } finally {
    messagesLoading.value = false;
  }
};

// 选择会话
const selectSession = async (session: SessionVO) => {
  console.log(session);
  currentSession.value = session;
  
  // 更新路由参数，但不重新加载页面
  router.replace({ 
    path: `/customer-service/chat/${session.sessionId}` 
  });
  
  // 获取会话消息历史
  await fetchSessionMessages(session.sessionId);
};

// 确认关闭会话
const confirmCloseSession = async () => {
  if (!currentSession.value) return;
  
  try {
    await ElMessageBox.confirm('确定要结束当前会话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const result = await chatSessionApi.closeSession(currentSession.value.sessionId);
    
    if (result) {
      ElMessage.success('会话已关闭');
      
      // 更新当前会话状态
      if (currentSession.value) {
        currentSession.value.status = 'CLOSED';
      }
      
      // 更新会话列表中的状态
      const index = allSessions.value.findIndex(s => s.sessionId === currentSession.value?.sessionId);
      if (index !== -1) {
        allSessions.value[index].status = 'CLOSED';
      }
    } else {
      ElMessage.error('关闭会话失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('关闭会话失败:', error);
      ElMessage.error('关闭会话失败');
    }
  }
};

// 刷新消息
const refreshMessages = async () => {
  if (!currentSession.value) return;
  await fetchSessionMessages(currentSession.value.sessionId);
};

// 处理收到的WebSocket消息
const handleIncomingMessage = (message: any) => {
  // 确保 sessionId 是字符串类型进行比较
  const msgSessionId = String(message.sessionId);
  
  if (currentSession.value && msgSessionId === currentSession.value.sessionId) {
    // 格式化为本地消息格式
    const formattedMessage = {
      msgId: message.msgId || Date.now().toString(),
      sessionId: msgSessionId,
      senderId: message.fromUserId || message.senderId,
      senderName: message.fromUserName || message.senderName || '',
      senderRole: message.fromUserType === 'CUSTOMER' ? 0 : 1,
      content: message.content,
      msgType: message.contentType === 'TEXT' ? 0 : 1,
      createdAt: message.createdAt || Date.now().toString(),
      showCopy: false
    };
    
    // 添加消息到列表
    messages.value.push(formattedMessage);
    
    // 滚动到最新消息
    nextTick().then(() => {
      scrollToBottom();
    });
  }
  
  // 更新会话列表中的最后消息
  const sessionIndex = allSessions.value.findIndex(s => s.sessionId === msgSessionId);
  if (sessionIndex !== -1) {
    allSessions.value[sessionIndex].lastMessage = message.content;
    allSessions.value[sessionIndex].lastMsgTime = message.createdAt;
  }
};

// 发送消息
const sendMessage = async () => {
  if (!currentSession.value || !messageInput.value.trim() || currentSession.value.status === 'CLOSED') {
    return;
  }
  
  try {
    // 获取当前登录用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    const userId = userInfo.id?.toString() || '1';
    
    // 创建消息请求
    const request: SendMessageRequest = {
      sessionId: currentSession.value.sessionId, 
      content: messageInput.value.trim(),
      messageType: 'TEXT',
      fromUserId: userId,
      fromUserType: 'AGENT'
    };
    
    // 清空输入框
    messageInput.value = '';
    
    // 发送消息
    if (connectionStatus.value === 'connected') {
      // 构造完整消息对象
      sendChatMessage({
        sessionId: request.sessionId,
        fromUserId: request.fromUserId,
        fromUserType: request.fromUserType,
        fromUserName: userInfo.name || userInfo.username || '',
        toUserId: currentSession.value?.customerId || '',
        content: request.content,
        contentType: request.messageType, // 保持与输入一致
        type: 'CHAT'
      } as any);
      
      // 添加消息到本地消息列表以实现即时显示
      const newMessage = {
        msgId: Date.now().toString(), // 临时ID
        sessionId: request.sessionId,
        senderId: userId,
        senderName: userInfo.name || userInfo.username || '',
        senderRole: 1, // 客服
        content: request.content,
        msgType: 0, // 文本
        createdAt: Date.now().toString(),
        showCopy: false
      };
      
      // 添加到消息列表
      messages.value.push(newMessage as any);
      
      // 更新当前会话的最后消息
      if (currentSession.value) {
        currentSession.value.lastMessage = request.content;
        currentSession.value.lastMsgTime = new Date();
      }
      
      // 滚动到最新消息
      nextTick().then(() => {
        scrollToBottom();
      });
    } else {
      // 通过HTTP API发送
      const result = await chatMessageApi.sendMessage(request);
      
      if (result) {
        // 转换为本地消息格式
        const formattedResult = {
          ...result,
          senderId: result.fromUserId,
          senderRole: result.fromUserType === 'CUSTOMER' ? 0 : 1,
          msgType: result.messageType === 'TEXT' ? 0 : 1,
          showCopy: false
        };
        
        // 添加消息到列表
        messages.value.push(formattedResult as any);
        
        // 更新当前会话的最后消息
        if (currentSession.value) {
          currentSession.value.lastMessage = result.content;
          currentSession.value.lastMsgTime = result.createdAt;
        }
        
        // 滚动到最新消息
        nextTick().then(() => {
          scrollToBottom();
        });
      }
    }
  } catch (error) {
    console.error('发送消息失败:', error);
    ElMessage.error('发送消息失败');
  }
};

// 处理图片上传按钮点击
const handleImageUpload = () => {
  if (fileInput.value) {
    fileInput.value.click();
  }
};

// 文件选择处理
const onFileSelected = async (event: Event) => {
  const input = event.target as HTMLInputElement;
  if (!input.files || !input.files.length || !currentSession.value) {
    return;
  }
  
  const file = input.files[0];
  
  // 简单检查文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件');
    return;
  }
  
  // 在实际项目中，这里应该调用文件上传API，获取文件URL后再发送消息
  // 这里简化处理，假设已经上传并获取了URL
  ElMessage.info('图片上传功能需要实现文件上传API');
  
  // 清除文件选择
  if (fileInput.value) {
    fileInput.value.value = '';
  }
};

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
};

// 初始化WebSocket连接
const initChat = () => {
  // console.log(currentSession.value);
  // if (!currentSession.value) return;
  // console.log(2);
  try {
    // 获取当前登录用户信息
    const userInfoStr = localStorage.getItem('userInfo');
    if (!userInfoStr) {
      ElMessage.error('未获取到用户信息，无法建立WebSocket连接');
      return;
    }
    const userInfo = JSON.parse(userInfoStr);
    if (!userInfo.id) {
      ElMessage.error('未获取到用户ID，无法建立WebSocket连接');
      return;
    }
    const userId = userInfo.id.toString();
    // 初始化WebSocket连接
    initWebSocketConnection(userId, 'AGENT');
    
    // 注册消息处理函数
    const unregisterMessageHandler = registerMessageHandler('message', handleIncomingMessage);
    
    // 组件卸载时清理
    onUnmounted(() => {
      if (unregisterMessageHandler) {
        unregisterMessageHandler();
      }
      cleanup();
    });
  } catch (error) {
    console.error('初始化WebSocket连接失败:', error);
    ElMessage.error('初始化WebSocket连接失败');
  }
};

// watch路由参数变化
watch(() => route.params.sessionId, (newSessionId) => {
  if (newSessionId && (!currentSession.value || currentSession.value.sessionId !== String(newSessionId))) {
    const sessionId = String(newSessionId);
    const session = allSessions.value.find(s => s.sessionId === sessionId);
    
    if (session) {
      // 如果在已加载的会话列表中找到了会话，直接选择
      selectSession(session);
    } else {
      // 否则通过API获取会话详情
      chatSessionApi.getSessionDetail(sessionId).then(sessionDetail => {
        if (sessionDetail) {
          allSessions.value.push(sessionDetail);
          currentSession.value = sessionDetail;
          fetchSessionMessages(sessionId);
        }
      });
    }
  }
}, { immediate: true });

// 组件挂载时初始化
onMounted(() => {
  // 获取客服活跃会话列表
  fetchAgentSessions();
  
  // 初始化WebSocket连接
  initChat();
});

// 组件卸载时清理
onUnmounted(() => {
  // 清理WebSocket连接和定时器
  cleanup();
});

// 消息提示音播放
const playMessageSound = () => {
  // 在实际项目中，这里可以实现消息提示音播放
  // 例如：new Audio('/message-sound.mp3').play();
  console.log('播放消息提示音');
};

// 处理接收到的消息
const handleReceivedMessage = (message: MessageVO) => {
  // 确保 sessionId 转为字符串进行比较
  const msgSessionId = String(message.sessionId);
  
  // 添加消息到对应的会话
  if (currentSession.value && msgSessionId === currentSession.value.sessionId) {
    messages.value.push(message);
    scrollToBottom();
    
    // 播放提示音
    playMessageSound();
  }
  
  // 更新会话列表中的最后消息
  const sessionIndex = allSessions.value.findIndex(s => s.sessionId === msgSessionId);
  if (sessionIndex !== -1) {
    allSessions.value[sessionIndex].lastMessage = message.content;
    allSessions.value[sessionIndex].lastMsgTime = message.createdAt;
  }
};

// 加载更多历史消息
const loadMoreMessages = async () => {
  console.log('加载更多历史消息');
  console.log(currentSession.value);
  console.log(oldestMessageId.value);
  console.log(loadingMoreMessages.value);
  if (!currentSession.value || !oldestMessageId.value || loadingMoreMessages.value) return;
  loadingMoreMessages.value = true;
  try {
    // 获取指定消息之前的历史消息
    const params = {
      sessionId: currentSession.value.sessionId,
      beforeMessageId: oldestMessageId.value,
      limit: 20
    };
    const olderMessages = await chatMessageApi.getSessionMessages(params);
    hasMoreMessages.value = olderMessages.length >= 20;
    if (olderMessages.length > 0) {
      oldestMessageId.value = olderMessages[olderMessages.length - 1].msgId;
      const formattedOlderMessages = [];
      for (const msg of olderMessages) {
        formattedOlderMessages.push({
          msgId: msg.msgId || `temp-${Date.now()}`,
          sessionId: currentSession.value.sessionId,
          senderId: (msg as any).senderId || '',
          senderName: (msg as any).senderName || '',
          senderRole: (msg as any).senderRole || 0,
          content: msg.content || '',
          msgType: (msg as any).msgType || 0,
          createdAt: msg.createdAt || new Date().toISOString(),
          timestamp: getTimestamp(msg.createdAt),
          showCopy: false
        });
      }
      // 按timestamp升序排序
      formattedOlderMessages.sort((a, b) => a.timestamp - b.timestamp);
      messages.value = [...formattedOlderMessages, ...messages.value];
    } else {
      hasMoreMessages.value = false;
    }
    // 记录当前滚动位置，加载后恢复
    const scrollContainer = messagesContainer.value;
    const oldScrollHeight = scrollContainer?.scrollHeight || 0;
    const oldScrollTop = scrollContainer?.scrollTop || 0;
    await nextTick();
    if (scrollContainer) {
      const newScrollHeight = scrollContainer.scrollHeight;
      scrollContainer.scrollTop = oldScrollTop + (newScrollHeight - oldScrollHeight);
    }
  } catch (error) {
    console.error('加载更多历史消息失败:', error);
    ElMessage.error('加载更多历史消息失败');
  } finally {
    loadingMoreMessages.value = false;
  }
};
</script>

<style scoped>
.chat-container {
  height: calc(100vh - 140px);
  padding: 20px;
}

.chat-layout {
  display: flex;
  height: 100%;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 左侧会话列表样式 */
.sessions-panel {
  width: 300px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e6e6e6;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #e6e6e6;
}

.panel-header h3 {
  margin: 0;
}

.sessions-filter {
  padding: 10px;
  border-bottom: 1px solid #e6e6e6;
}

.sessions-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  display: flex;
  padding: 12px 15px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
}

.session-item:hover {
  background-color: #f5f7fa;
}

.session-item.active {
  background-color: #ecf5ff;
}

.session-info {
  flex: 1;
  overflow: hidden;
}

.session-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  margin-bottom: 5px;
}

.session-message {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

.empty-sessions {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100px;
  color: #909399;
}

/* 右侧聊天区域样式 */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #e6e6e6;
}

.customer-info {
  display: flex;
  align-items: center;
}

.customer-info h3 {
  margin: 0;
  margin-right: 10px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.chat-messages {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  background-color: #f5f7fa;
}

.message-item {
  max-width: 70%;
  margin-bottom: 15px;
  clear: both;
  display: flex;
  align-items: flex-start;
}

.sender-message {
  float: right;
  flex-direction: row-reverse;
  margin-left: auto;
}

.receiver-message {
  float: left;
  flex-direction: row;
}

.avatar-container {
  margin: 0 8px;
  flex-shrink: 0;
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
  max-width: calc(100% - 56px);
}

.message-sender {
  font-size: 12px;
  margin-bottom: 4px;
}

.message-time {
  margin-left: 5px;
  color: #909399;
}

.message-content {
  padding: 10px;
  border-radius: 8px;
  position: relative;
}

.sender-message .message-content {
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  color: white;
}

.receiver-message .message-content {
  background: linear-gradient(135deg, #fafafa, #f0f2f5);
  color: #303133;
}

.text-message {
  word-break: break-word;
  white-space: pre-wrap;
  position: relative;
}

.copy-button {
  position: absolute;
  top: -18px;
  right: -18px;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.copy-button:hover {
  opacity: 1;
}

.image-message {
  max-width: 250px;
}

.empty-messages {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100px;
  color: #909399;
}

.chat-input {
  padding: 15px;
  border-top: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
}

.chat-input.disabled {
  background-color: #f5f7fa;
}

.input-tools {
  margin-bottom: 10px;
}

.input-area {
  margin-bottom: 10px;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
}

.no-session-selected {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 时间分隔条样式 */
.time-divider {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 10px 0;
  clear: both;
  width: 100%;
}

.time-divider span {
  background: #f0f2f5;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  color: #8c8c8c;
}

.load-more-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #e6e6e6;
}
</style>
