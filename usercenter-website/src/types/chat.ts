// 消息类型枚举
export enum MessageType {
  USER = 'USER',
  ASSISTANT = 'ASSISTANT',
  SYSTEM = 'SYSTEM'
}

// 消息状态枚举
export enum MessageStatus {
  SENDING = 'sending',
  SENT = 'sent',
  RECEIVED = 'received',
  ERROR = 'error'
}

// 消息接口
export interface Message {
  id: string;
  sessionId: string;
  content: string;
  type: MessageType;
  status: MessageStatus;
  timestamp: number;
  metadata?: Record<string, any>;
  thinkingContent?: string; // 思考过程内容
  isThinkingExpanded?: boolean; // 思考内容是否展开
}

// 会话接口
export interface Session {
  sessionId: string;
  name: string;
  sessionName?: string;
  customerId: number;
  botId?: number;
  botName?: string;
  knowledgeBaseId?: number;
  knowledgeBaseName?: string;
  contentId?: number;
  contentName?: string;
  status: string;
  createdAt: number;
  updatedAt: number;
  lastMessage?: string;
  lastMsgTime?: number;
}

// 机器人配置接口
export interface BotConfig {
  botId: number;
  botName: string;
  modelName: string;
  vendor: string;
  modelType: string;
  enabled: boolean;
}

// 知识库接口
export interface KnowledgeBase {
  id: number;
  name: string;
  description?: string;
  enabled: boolean;
  docCount?: number;
}

// 内容接口
export interface Content {
  id: number;
  knowledgeBaseId: number;
  title: string;
  contentType: string;
  status: string;
  createdAt?: number;
  updatedAt?: number;
}

// 聊天配置接口
export interface ChatConfig {
  selectedBotId?: number;
  selectedKnowledgeBaseId?: number | null;
  selectedContentId?: number;
  customerId: number;
}

// SSE事件类型
export interface SSEEvent {
  type: 'message' | 'error' | 'done';
  data: any;
}

// 创建会话命令
export interface CreateSessionCmd {
  customerId: number;
  botId?: number;
  knowledgeBaseId?: number;
}

// SSE聊天请求
export interface BotChatSSERequest {
  sessionId: string;
  question: string;
  targetBotId?: number;
  includeHistory?: boolean;
  knowledgeBaseId?: number;
  contentId?: number;
}

// 组件Props类型
export interface ChatViewProps {}

export interface ConfigPanelProps {
  modelValue: ChatConfig;
}

export interface ChatAreaProps {
  sessionId?: string;
  messages: Message[];
  loading?: boolean;
}

export interface MessageItemProps {
  message: Message;
}

export interface SessionListProps {
  sessions: Session[];
  currentSessionId?: string;
}

// 组件Emits类型
export interface ConfigPanelEmits {
  'update:modelValue': [value: ChatConfig];
  'session-created': [session: Session];
  'session-selected': [sessionId: string];
}

export interface ChatAreaEmits {
  'send-message': [content: string];
  'retry-message': [messageId: string];
}

export interface MessageItemEmits {
  'copy-message': [content: string];
  'retry-message': [messageId: string];
}

export interface SessionListEmits {
  'session-selected': [sessionId: string];
  'session-deleted': [sessionId: string];
  'session-renamed': [sessionId: string, newName: string];
} 