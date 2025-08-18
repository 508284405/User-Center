<template>
  <el-dialog
    :model-value="visible"
    title="索引详情"
    width="600px"
    @close="handleClose"
    v-loading="loading"
    element-loading-text="加载中..."
  >
    <div v-if="indexInfo">
      <h4 style="margin-top: 20px;">字段定义 (Schema):</h4>
      <el-table :data="indexInfo.attributes" style="width: 100%">
        <el-table-column prop="attribute" label="字段名"></el-table-column>
        <el-table-column prop="identifier" label="标识符"></el-table-column>
        <el-table-column prop="type" label="类型"></el-table-column>
        <el-table-column prop="WEIGHT" label="权重"></el-table-column>
        <el-table-column label="详细配置">
           <template #default="scope">
             <el-tooltip placement="top">
               <template #content>
                 <div v-if="scope.row.type === 'VECTOR'">
                   算法: {{ scope.row.algorithm ?? 'N/A' }}<br/>
                   数据类型: {{ scope.row.data_type ?? 'N/A' }}<br/>
                   维度: {{ scope.row.dim ?? 'N/A' }}<br/>
                   距离指标: {{ scope.row.distance_metric ?? 'N/A' }}<br/>
                   M: {{ scope.row.M ?? 'N/A' }}<br/>
                   ef_construction: {{ scope.row.ef_construction ?? 'N/A' }}
                 </div>
                 <div v-else>
                    <!-- 可以根据其他类型添加显示逻辑 -->
                    N/A
                 </div>
               </template>
               <span>查看配置</span>
             </el-tooltip>
           </template>
        </el-table-column>
        <!-- 可能需要根据 attribute 中的其他字段添加更多列 -->
        <!-- <el-table-column prop="schema" label="配置"></el-table-column> -->
      </el-table>

      <h4 style="margin-top: 20px;">定义 (Definition):</h4>
      <el-descriptions :column="1" border v-if="indexInfo.definition">
        <el-descriptions-item label="Key类型">{{ indexInfo.definition.key_type ?? 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="Prefixes">{{ JSON.stringify(indexInfo.definition.prefixes) ?? 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="默认分数">{{ indexInfo.definition.default_score ?? 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="索引所有">{{ indexInfo.definition.indexes_all ?? 'N/A' }}</el-descriptions-item>
      </el-descriptions>

      <h4 style="margin-top: 20px;">基本信息:</h4>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="索引名称">{{ indexInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="文档数量">{{ indexInfo.docs ?? 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="最大文档ID">{{ indexInfo.maxDocId ?? 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="术语数量">{{ indexInfo.terms ?? 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="记录数量">{{ indexInfo.records ?? 'N/A' }}</el-descriptions-item>
        <template v-if="showMoreBasicInfo">
          <el-descriptions-item label="倒排索引大小">{{ indexInfo.invertedSize ?? 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="向量索引大小">{{ indexInfo.vectorIndexSize ?? 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="总倒排索引块">{{ indexInfo.totalInvertedIndexBlocks ?? 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="Offset Vectors大小">{{ indexInfo.offsetVectorsSize ?? 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="文档表大小">{{ indexInfo.docTableSize ?? 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="可排序值大小">{{ indexInfo.sortableValuesSize ?? 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="Key表大小">{{ indexInfo.keyTableSize ?? 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="平均每文档记录数">{{ indexInfo.recordsPerDocAverage ?? 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="平均每记录字节数">{{ indexInfo.bytesPerRecordAverage ?? 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="平均每术语偏移量">{{ indexInfo.offsetsPerTermAverage ?? 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="平均每记录偏移量位">{{ indexInfo.offsetBitsPerRecordAverage ?? 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="哈希索引失败次数">{{ indexInfo.hashIndexingFailures ?? 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="总索引时间">{{ indexInfo.totalIndexingTime ?? 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="索引状态">{{ indexInfo.indexing ?? 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="已索引百分比">{{ indexInfo.percentIndexed ?? 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="使用次数">{{ indexInfo.numberOfUses ?? 'N/A' }}</el-descriptions-item>
        </template>
      </el-descriptions>
      <el-button type="text" @click="showMoreBasicInfo = !showMoreBasicInfo">{{ showMoreBasicInfo ? '收起' : '查看更多' }}</el-button>

      <h4 style="margin-top: 20px;">GC 统计 (GC Stats) <el-button type="text" @click="showGcStats = !showGcStats">{{ showGcStats ? '隐藏' : '详情' }}</el-button></h4>
      <el-descriptions :column="1" border v-if="indexInfo.gcStats && showGcStats">
        <el-descriptions-item label="收集字节数">{{ indexInfo.gcStats.bytes_collected ?? 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="总运行时长 (ms)">{{ indexInfo.gcStats.total_ms_run ?? 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="总循环次数">{{ indexInfo.gcStats.total_cycles ?? 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="平均循环时长 (ms)">{{ indexInfo.gcStats.average_cycle_time_ms ?? 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="上次运行时长 (ms)">{{ indexInfo.gcStats.last_run_time_ms ?? 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="GC 数字树缺失">{{ indexInfo.gcStats.gc_numeric_trees_missed ?? 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="GC 块拒绝">{{ indexInfo.gcStats.gc_blocks_denied ?? 'N/A' }}</el-descriptions-item>
      </el-descriptions>

      <h4 style="margin-top: 20px;">Cursor 统计 (Cursor Stats) <el-button type="text" @click="showCursorStats = !showCursorStats">{{ showCursorStats ? '隐藏' : '详情' }}</el-button></h4>
      <el-descriptions :column="1" border v-if="indexInfo.cursorStats && showCursorStats">
        <el-descriptions-item label="全局空闲">{{ indexInfo.cursorStats.global_idle ?? 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="全局总计">{{ indexInfo.cursorStats.global_total ?? 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="索引容量">{{ indexInfo.cursorStats.index_capacity ?? 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="索引总计">{{ indexInfo.cursorStats.index_total ?? 'N/A' }}</el-descriptions-item>
      </el-descriptions>

       <h4 style="margin-top: 20px;">Dialect 统计 (Dialect Stats) <el-button type="text" @click="showDialectStats = !showDialectStats">{{ showDialectStats ? '隐藏' : '详情' }}</el-button></h4>
      <el-descriptions :column="1" border v-if="indexInfo.dialectStats && showDialectStats">
        <el-descriptions-item label="Dialect 1">{{ indexInfo.dialectStats.dialect_1 ?? 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="Dialect 2">{{ indexInfo.dialectStats.dialect_2 ?? 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="Dialect 3">{{ indexInfo.dialectStats.dialect_3 ?? 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="Dialect 4">{{ indexInfo.dialectStats.dialect_4 ?? 'N/A' }}</el-descriptions-item>
      </el-descriptions>

    </div>
    <div v-else>
      <el-empty description="未能加载索引详情"></el-empty>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, defineProps, defineEmits, watch } from 'vue';
import { ElMessage, ElDialog, ElDescriptions, ElDescriptionsItem, ElTable, ElTableColumn, ElEmpty, ElButton, ElTooltip } from 'element-plus';
import { getIndexInfo } from '@/api/smartcs/knowledgeIndex';

interface IndexInfoDTO {
  name: string;
  options: any; // 根据实际结构细化，如果总是空对象可以定义为 Record<string, never>
  definition: {
    key_type: string;
    prefixes: any; // 根据实际结构细化，这里可能是一个空对象或者其他结构 Record<string, any>
    default_score: string;
    indexes_all: string;
  };
  attributes: Array<{
    identifier: string;
    attribute: string;
    type: string;
    WEIGHT?: string; // 可选
    algorithm?: string; // VECTOR specific
    data_type?: string; // VECTOR specific
    dim?: string; // VECTOR specific
    distance_metric?: string; // VECTOR specific
    M?: string; // VECTOR specific
    ef_construction?: string; // VECTOR specific
    // Add other potential fields from attributes here if any
    schema?: string; // Although we are replacing its display, it's in the original data
  }>;
  gcStats: {
    bytes_collected: string;
    total_ms_run: string;
    total_cycles: string;
    average_cycle_time_ms: string;
    last_run_time_ms: string;
    gc_numeric_trees_missed: string;
    gc_blocks_denied: string;
  };
  cursorStats: {
    global_idle: string;
    global_total: string;
    index_capacity: string;
    index_total: string;
  };
  dialectStats: {
    dialect_1: string;
    dialect_2: string;
    dialect_3: string;
    dialect_4: string;
  };
  docs: number;
  maxDocId: number;
  terms: number;
  records: number;
  invertedSize: number;
  vectorIndexSize: number;
  totalInvertedIndexBlocks: number;
  offsetVectorsSize: number;
  docTableSize: number;
  sortableValuesSize: number;
  keyTableSize: number;
  recordsPerDocAverage: number;
  bytesPerRecordAverage: number;
  offsetsPerTermAverage: number;
  offsetBitsPerRecordAverage: number;
  hashIndexingFailures: string;
  totalIndexingTime: number;
  indexing: string;
  percentIndexed: number;
  numberOfUses: string;
}

interface Props {
  visible: boolean;
  indexName: string; // 接收索引名称
}

const props = defineProps<Props>();
const emit = defineEmits(['update:visible']);

const loading = ref(false);
const indexInfo = ref<IndexInfoDTO | null>(null);

const showMoreBasicInfo = ref(false);
const showGcStats = ref(false);
const showCursorStats = ref(false);
const showDialectStats = ref(false);

// 获取索引详情
const fetchIndexDetail = async (indexName: string) => {
  loading.value = true;
  indexInfo.value = null; // 清空旧数据
  try {
    const res = await getIndexInfo({ indexName: indexName });
    if (res.success && res.data) {
      indexInfo.value = res.data;
    } else {
      ElMessage.error('获取索引详情失败: ' + res.errMessage);
    }
  } catch (error: any) {
    ElMessage.error('获取索引详情异常: ' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

// 处理模态框关闭
const handleClose = () => {
  emit('update:visible', false);
};

// 监听 visible 变化，在打开时获取详情
watch(() => props.visible, (newValue) => {
  if (newValue && props.indexName) {
    fetchIndexDetail(props.indexName);
  }
});

// 初始加载（如果 visible 默认为 true 且 indexName 已有值）
watch(() => props.indexName, (newValue, oldValue) => {
  if (props.visible && newValue && newValue !== oldValue) {
    fetchIndexDetail(newValue);
  }
}, { immediate: true });

</script>

<style scoped lang="scss">
@use './index-detail.scss' as *;
</style>
