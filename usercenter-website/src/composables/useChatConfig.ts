import { ref, reactive, readonly } from 'vue';
import { ElMessage } from 'element-plus';
import { BotConfig, KnowledgeBase, Content, ChatConfig } from '@/types/chat';
import { knowledgeBaseApi } from '@/api/smartcs/knowledgeBase';
import { contentApi } from '@/api/smartcs/content';
import { chatSessionApi } from '@/api/smartcs/chatSession';

export function useChatConfig() {
  // 机器人列表
  const botList = ref<BotConfig[]>([]);
  const botLoading = ref(false);
  
  // 知识库列表
  const knowledgeBaseList = ref<KnowledgeBase[]>([]);
  const knowledgeLoading = ref(false);
  
  // 内容列表
  const contentList = ref<Content[]>([]);
  const contentLoading = ref(false);
  
  // 当前配置
  const config = reactive<ChatConfig>({
    selectedBotId: undefined,
    selectedKnowledgeBaseId: undefined,
    selectedContentId: undefined,
    customerId: 1 // 默认客户ID，实际应用中应该从用户信息获取
  });
  
  // 加载机器人列表 - 机器人管理模块已移除，返回空列表
  const loadBotList = async () => {
    try {
      botLoading.value = true;
      // 机器人管理模块已被移除，提供默认配置或空列表
      botList.value = [];
      ElMessage.info('机器人管理功能已移除，聊天功能可能受限');
    } catch (error) {
      console.error('加载机器人列表失败:', error);
      ElMessage.error('加载机器人列表失败');
    } finally {
      botLoading.value = false;
    }
  };
  
  // 加载知识库列表
  const loadKnowledgeBaseList = async () => {
    try {
      knowledgeLoading.value = true;
      const response = await knowledgeBaseApi.list({
        pageIndex: 1,
        pageSize: 100,
        name: ''
      });
      
      if (response.success && response.data) {
        knowledgeBaseList.value = response.data.map((doc: any) => ({
          id: doc.id,
          name: doc.title || doc.name,
          description: doc.description,
          enabled: true,
          docCount: doc.docCount
        }));
        
        // 如果还没有选择知识库，不选择知识库
        if (!config.selectedKnowledgeBaseId && knowledgeBaseList.value.length > 0) {
          config.selectedKnowledgeBaseId = null;
        }
      }
    } catch (error) {
      console.error('加载知识库列表失败:', error);
      ElMessage.error('加载知识库列表失败');
    } finally {
      knowledgeLoading.value = false;
    }
  };
  
  // 加载会话列表
  const loadSessionList = async () => {
    const response = await chatSessionApi.getSessionsPage({
      pageIndex: 1,
      pageSize: 100,
      customerId: config.customerId
    });
    console.log(response);
  };
  
  // 加载内容列表
  const loadContentList = async (knowledgeBaseId: number) => {
    console.log('useChatConfig: 开始加载内容列表，知识库ID:', knowledgeBaseId);
    try {
      contentLoading.value = true;
      const response = await contentApi.list({
        knowledgeBaseId,
        pageIndex: 1,
        pageSize: 100
      });
      
      console.log('useChatConfig: 内容API响应:', response);
      
      if (response.success && response.data) {
        contentList.value = response.data.map((item: any) => ({
          id: item.id,
          knowledgeBaseId: item.knowledgeBaseId,
          title: item.title,
          contentType: item.contentType,
          status: item.status,
          createdAt: item.createdAt,
          updatedAt: item.updatedAt
        }));
        console.log('useChatConfig: 内容列表加载完成，数量:', contentList.value.length);
      } else {
        console.log('useChatConfig: API响应失败或无数据');
        contentList.value = [];
      }
    } catch (error) {
      console.error('加载内容列表失败:', error);
      ElMessage.error('加载内容列表失败');
      contentList.value = [];
    } finally {
      contentLoading.value = false;
    }
  };

  // 选择机器人
  const selectBot = (botId: number) => {
    config.selectedBotId = botId;
  };
  
  // 选择知识库
  const selectKnowledgeBase = (knowledgeBaseId: number, forceLoad = false) => {
    console.log('useChatConfig: selectKnowledgeBase被调用，知识库ID:', knowledgeBaseId, '强制加载:', forceLoad);
    if (config.selectedKnowledgeBaseId !== knowledgeBaseId || forceLoad || contentList.value.length === 0) {
      console.log('useChatConfig: 开始加载内容，原因:', 
        config.selectedKnowledgeBaseId !== knowledgeBaseId ? '知识库ID变化' : 
        forceLoad ? '强制加载' : '内容列表为空');
      config.selectedKnowledgeBaseId = knowledgeBaseId;
      config.selectedContentId = undefined; // 清空内容选择
      loadContentList(knowledgeBaseId);
    } else {
      console.log('useChatConfig: 知识库ID未变化且内容已存在，跳过加载');
    }
  };
  
  // 选择内容
  const selectContent = (contentId: number) => {
    config.selectedContentId = contentId;
  };
  
  // 清空知识库选择
  const clearKnowledgeBaseSelection = () => {
    config.selectedKnowledgeBaseId = undefined;
    config.selectedContentId = undefined;
    contentList.value = [];
  };

  // 重置配置
  const resetConfig = () => {
    config.selectedBotId = botList.value.length > 0 ? botList.value[0].botId : undefined;
    clearKnowledgeBaseSelection();
  };
  
  // 获取选中的机器人信息
  const getSelectedBot = (): BotConfig | undefined => {
    return botList.value.find(bot => bot.botId === config.selectedBotId);
  };
  
  // 获取选中的知识库信息
  const getSelectedKnowledgeBase = (): KnowledgeBase | undefined => {
    return knowledgeBaseList.value.find(kb => kb.id === config.selectedKnowledgeBaseId);
  };
  
  // 获取选中的内容信息
  const getSelectedContent = (): Content | undefined => {
    return contentList.value.find(content => content.id === config.selectedContentId);
  };
  
  // 验证配置是否完整
  const validateConfig = (): boolean => {
    console.log('validateConfig', config);
    if (!config.selectedBotId) {
      ElMessage.warning('请选择一个机器人');
      return false;
    }
    return true;
  };
  
  // 初始化加载数据
  const initialize = async () => {
    await Promise.all([
      loadBotList(),
      loadKnowledgeBaseList(),
      loadSessionList()
    ]);
  };
  
      return {
    // 状态
    botList: readonly(botList),
    knowledgeBaseList: readonly(knowledgeBaseList),
    contentList: readonly(contentList),
    config,
    botLoading: readonly(botLoading),
    knowledgeLoading: readonly(knowledgeLoading),
    contentLoading: readonly(contentLoading),
    
    // 方法
    loadBotList,
    loadKnowledgeBaseList,
    loadContentList,
    selectBot,
    selectKnowledgeBase,
    selectContent,
    clearKnowledgeBaseSelection,
    resetConfig,
    getSelectedBot,
    getSelectedKnowledgeBase,
    getSelectedContent,
    validateConfig,
    initialize
  };
} 