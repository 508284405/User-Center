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

// RAG组件配置接口
export interface ContentAggregatorConfig {
  maxResults: number; // 最大结果数 (1-50)
  minScore: number; // 最小分数阈值 (0.0-1.0)
  scoringModelId?: number | string; // 评分模型ID，未指定时回退到会话级 modelId
}

export interface QueryTransformerConfig {
  n: number; // 查询扩展数量 (1-10)
  promptTemplate?: string; // 提示模板
  modelId?: number | string; // 模型ID，未指定时回退到会话级 modelId
}

export interface QueryRouterConfig {
  webSearchEnabled: boolean; // 是否启用网络搜索
  knowledgeSearchEnabled: boolean; // 是否启用知识库搜索
  enableSqlQuery?: boolean; // 是否启用SQL查询检索
  promptTemplate?: string; // 提示模板
  retrieverToDescription?: Record<string, string>; // 检索器描述映射
  modelId?: number | string; // 模型ID，未指定时回退到会话级 modelId
}

export interface WebSearchConfig {
  maxResults: number; // 最大搜索结果数 (1-20)
  timeout: number; // 搜索超时时间（秒）(1-60)
}

export interface KnowledgeSearchConfig {
  topK: number; // 返回的最相关结果数 (1-100)
  scoreThreshold: number; // 分数阈值 (0.0-1.0)
  modelId?: number | string; // 模型ID，用于知识库检索
}

// 内容注入器配置
export interface ContentInjectorConfig {
  promptTemplate?: string; // 提示模板
  metadataKeysToInclude?: string[]; // 要包含的元数据键列表
}

// 嵌入存储配置
export interface EmbeddingStoreConfig {
  maxResults: number; // 最大结果数量 (1-100)
  minScore: number; // 最小分数 (0.0-1.0)
  metadataKeysToInclude?: string[]; // 要包含的元数据键列表
}

// SQL查询安全设置
export interface SecuritySettings {
  allowedTables?: string[]; // 允许的表名列表
  forbiddenKeywords?: string[]; // 禁止的关键字列表
  enableQueryValidation: boolean; // 是否启用查询验证
  selectOnly: boolean; // 是否只允许SELECT语句
}

// SQL查询配置
export interface SqlQueryConfig {
  maxResults: number; // 最大结果数量 (1-1000)
  timeout: number; // 超时时间（秒）(1-300)
  securitySettings?: SecuritySettings; // 安全设置
}

// 记忆配置
export interface MemoryConfig {
  maxMessages: number; // 最大消息数量 (1-1000)
  memoryType: string; // 记忆类型
}



// RAG组件完整配置
export interface RagComponentConfig {
  contentAggregator: ContentAggregatorConfig;
  queryTransformer: QueryTransformerConfig;
  queryRouter: QueryRouterConfig;
  webSearch: WebSearchConfig;
  knowledgeSearch: KnowledgeSearchConfig;
  contentInjector: ContentInjectorConfig;
  embeddingStore: EmbeddingStoreConfig;
  sqlQuery: SqlQueryConfig;
  memory: MemoryConfig;
}

// RAG配置预设模式
export enum RagConfigPreset {
  HIGH_PRECISION = 'high_precision',
  HIGH_RECALL = 'high_recall',
  BALANCED = 'balanced',
  DEBUG = 'debug'
}

// RAG配置预设定义
export interface RagConfigPresetDefinition {
  id: RagConfigPreset;
  name: string;
  description: string;
  config: RagComponentConfig;
}

// RAG调试信息
export interface RagDebugInfo {
  configUsed: RagComponentConfig; // 实际使用的配置
  retrievalStats: {
    totalDocuments: number;
    webSearchResults: number;
    knowledgeSearchResults: number;
    finalResults: number;
  };
  performanceMetrics: {
    totalResponseTime: number; // 毫秒
    retrievalTime: number; // 毫秒
    aggregationTime: number; // 毫秒
  };
  timestamp: number;
}

// RAG配置验证错误
export interface RagConfigValidationError {
  field: string;
  message: string;
  suggestedValue?: number | boolean;
}

// RAG配置验证结果
export interface RagConfigValidationResult {
  isValid: boolean;
  errors: RagConfigValidationError[];
  correctedConfig?: RagComponentConfig;
}

// 扩展聊天请求以包含RAG配置
export interface BotChatSSERequestWithRag extends BotChatSSERequest {
  ragConfig?: RagComponentConfig;
} 