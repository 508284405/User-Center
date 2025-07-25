import request from '../config';

interface FaqAddCmd {
  id?: number;
  question: string;
  answer: string;
}

interface KnowledgeSearchQry {
  keyword?: string;
  modelType?: string;
  pageIndex?: number;
  pageSize?: number;
  k?: number;
}

// 新增文档命令对象
export interface DocAddCmd {
  title: string;
  ossUrl: string;
  fileType?: string;
  fileSize?: number;
}

// 触发文档向量生成命令对象
interface TriggerDocEmbeddingCmd {
  docId: number;
  strategyName: string;
}

// 向量数据分页查询对象
interface EmbeddingListQry {
  docId: number;
  strategyName: string;
  pageIndex?: number;
  pageSize?: number;
}

interface ApiResponse<T> {
  success: boolean;
  errorCode?: string;
  errorMessage?: string;
  data?: T;
  total?: number;
}

export const knowledgeApi = {
  /**
   * 创建/更新FAQ
   */
  addFaq(data: FaqAddCmd): Promise<ApiResponse<any>> {
    return request({
      url: '/smartcs/api/admin/knowledge/faq',
      method: 'post',
      data
    });
  },
  
  /**
   * 删除FAQ
   */
  deleteFaq(id: number): Promise<ApiResponse<any>> {
    return request({
      url: '/smartcs/api/admin/knowledge/faq/' + id,
      method: 'delete'
    });
  },
  
  /**
   * 查询FAQ列表
   */
  listFaqs(keyword?: string, page: number = 1, size: number = 10): Promise<ApiResponse<any>> {
    return request({
      url: '/smartcs/api/admin/knowledge/faq',
      method: 'get',
      params: {
        keyword,
        page,
        size
      }
    });
  },
  
  /**
   * 上传文档
   */
  addDoc(data: DocAddCmd): Promise<ApiResponse<any>> {
    return request({
      url: '/smartcs/api/admin/knowledge/doc',
      method: 'post',
      data,
      headers: {
        'Content-Type': 'application/json'
      }
    });
  },
  
  /**
   * 删除文档 (后端没有直接提供该接口，但前端实现可能需要)
   */
  deleteDoc(id: number): Promise<ApiResponse<any>> {
    return request({
      url: '/smartcs/api/admin/knowledge/doc/' + id,
      method: 'delete'
    });
  },
  
  /**
   * 触发文档向量生成
   */
  triggerDocEmbedding(data: TriggerDocEmbeddingCmd): Promise<ApiResponse<any>> {
    return request({
      url: '/smartcs/api/admin/knowledge/doc/trigger-embedding',
      method: 'post',
      data
    });
  },
  
  /**
   * 查询文档列表
   */
  listDocs(keyword?: string, page: number = 1, size: number = 10): Promise<ApiResponse<any>> {
    return request({
      url: '/smartcs/api/admin/knowledge/doc',
      method: 'get',
      params: {
        keyword,
        page,
        size
      }
    });
  },
  
  /**
   * 查询向量数据列表
   */
  listEmbeddings(qry: EmbeddingListQry): Promise<ApiResponse<any>> {
    return request({
      url: '/smartcs/api/admin/knowledge/embedding',
      method: 'get',
      params: qry
    });
  },
  
  /**
   * 批量添加向量
   */
  addEmbeddings(data: any): Promise<ApiResponse<any>> {
    return request({
      url: '/smartcs/api/admin/knowledge/embeddings/batch',
      method: 'post',
      data
    });
  },
  
  /**
   * 向量检索
   */
  searchByVector(data: KnowledgeSearchQry): Promise<ApiResponse<any>> {
    return request({
      url: '/smartcs/api/admin/knowledge/search/vector',
      method: 'post',
      data
    });
  },
  
  /**
   * 文本检索
   */
  searchByText(data: KnowledgeSearchQry): Promise<ApiResponse<any>> {
    return request({
      url: '/smartcs/api/admin/knowledge/search/text',
      method: 'post',
      data
    });
  }
}; 