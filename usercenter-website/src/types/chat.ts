// 消息类型枚举
export enum MessageType {
  USER = 'USER',
  ASSISTANT = 'ASSISTANT',
  SYSTEM = 'SYSTEM',
  CLARIFICATION = 'CLARIFICATION'
}

// 消息状态枚举
export enum MessageStatus {
  SENDING = 'sending',
  SENT = 'sent',
  RECEIVED = 'received',
  ERROR = 'error'
}

// 消息发送状态枚举
export enum MessageSendStatus {
  SENDING = 0,    // 发送中
  DELIVERED = 1,  // 已送达
  SEND_FAILED = 2, // 发送失败
  READ = 3        // 已读
}

// 消息内容类型枚举
export enum MessageContentType {
  TEXT = 0,
  IMAGE = 1,
  ORDER_CARD = 2,
  SYSTEM = 3
}

// 消息删除类型
export enum MessageDeleteType {
  SELF_ONLY = 0,  // 仅自己可见删除
  BOTH_SIDES = 1  // 双方删除
}

// 消息接口
export interface Message {
  id?: string; // 兼容旧版本
  msgId: string;
  sessionId: string;
  content: string;
  type: MessageType;
  status?: MessageStatus; // 兼容旧版本
  timestamp: number;
  metadata?: Record<string, any>;
  thinkingContent?: string; // 思考过程内容
  isThinkingExpanded?: boolean; // 思考内容是否展开
  
  // IM消息扩展字段
  msgType?: number; // 消息内容类型
  chatType?: string;
  fromUserId?: string; // 发送者用户ID
  fromUserType?: string; // 发送者用户类型
  createdAt?: number; // 创建时间
  
  // 消息撤回相关字段
  isRecalled?: boolean; // 是否已撤回
  recalledAt?: number; // 撤回时间戳
  recalledBy?: string; // 撤回操作者ID
  recallReason?: string; // 撤回原因
  
  // 删除相关字段
  isDeletedBySender?: boolean;
  isDeletedByReceiver?: boolean;
  deletedBySenderAt?: number;
  deletedByReceiverAt?: number;
  deleteType?: number;
  deletedReason?: string;
  
  // 编辑相关字段
  isEdited?: boolean;
  editedAt?: number;
  originalContent?: string;
  editCount?: number;
  
  // 已读回执相关字段
  isRead?: boolean;
  readAt?: number;
  readBy?: string;
  
  // 消息状态相关字段
  sendStatus?: number;
  sendFailReason?: string;
  retryCount?: number;
  
  // 回复/引用相关字段
  replyToMsgId?: string;
  quotedContent?: string;
  quotedFromUser?: string;
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
  'recall-message': [messageId: string];
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

// ==================== 槽位填充相关类型定义 ====================

// 槽位类型枚举
export enum SlotType {
  STRING = 'STRING',
  NUMBER = 'NUMBER', 
  DATE = 'DATE',
  TIME = 'TIME',
  EMAIL = 'EMAIL',
  PHONE = 'PHONE',
  URL = 'URL',
  BOOLEAN = 'BOOLEAN',
  ENUM = 'ENUM',
  JSON = 'JSON',
  LIST = 'LIST',
  ENTITY = 'ENTITY',
  CUSTOM = 'CUSTOM'
}

// 槽位定义DTO
export interface SlotDefinitionDTO {
  name: string;
  label: string;
  type: SlotType;
  required?: boolean;
  validation?: Record<string, any>;
  hint?: string;
  examples?: string[];
  defaultValue?: string;
  dependencies?: string[];
  order?: number;
  multiple?: boolean;
  minValue?: number;
  maxValue?: number;
  minLength?: number;
  maxLength?: number;
  enumOptions?: string[];
  pattern?: string;
  unit?: string;
  extensions?: Record<string, any>;
}

// 槽位模板DTO
export interface SlotTemplateDTO {
  templateId?: string;
  templateName: string;
  description?: string;
  intentCode: string;
  slotDefinitions: SlotDefinitionDTO[];
  promptTemplate?: string;
  clarificationTemplates?: Record<string, string>;
  slotFillingEnabled?: boolean;
  maxClarificationAttempts?: number;
  blockRetrievalOnMissing?: boolean;
  completenessThreshold?: number;
  language?: string;
  version?: string;
  extensions?: Record<string, any>;
}

// 槽位填充元数据
export interface SlotFillingMetadata {
  slotTemplateId?: string;
  missingSlots?: string[];
  filledSlots?: Record<string, any>;
  clarificationQuestions?: string[];
  completenessScore?: number;
  nextRequiredSlot?: string;
}

// 澄清问题界面数据
export interface ClarificationData {
  questions: string[];
  suggestedResponses?: string[];
  contextHint?: string;
  slotName?: string;
  slotLabel?: string;
  examples?: string[];
}

// 消息表情反应接口
export interface MessageReaction {
  emoji: string;
  name: string;
  count: number;
  userIds: string[];
}

// 扩展消息接口以支持槽位填充
export interface MessageWithSlotFilling extends Message {
  slotFillingMetadata?: SlotFillingMetadata;
  clarificationData?: ClarificationData;
} 