<script setup lang="ts">
import { ref, defineProps, defineEmits, computed } from 'vue'
import { ElTable, ElTableColumn, ElPagination, ElButton, ElTooltip } from 'element-plus'

// 定义列配置类型
interface TableColumn {
  prop: string;
  label: string;
  width?: number | string;
  formatter?: (row: any, column: any, cellValue: any) => any;
  align?: 'left' | 'center' | 'right';
  fixed?: 'left' | 'right' | boolean;
}

// 定义分页配置类型
interface PaginationConfig {
  total: number;
  pageSize: number;
  currentPage: number;
}

// 定义组件属性
const props = defineProps<{
  data: any[];
  columns: TableColumn[];
  loading?: boolean;
  pagination?: PaginationConfig;
  showSelection?: boolean;
  rowKey?: string;
  border?: boolean;
  stripe?: boolean;
}>()

// 定义事件
const emit = defineEmits(['page-change', 'size-change', 'selection-change', 'row-click'])

// 选中行数据
const selectedRows = ref<any[]>([])

// 处理分页变化
const handleCurrentChange = (page: number) => {
  emit('page-change', page)
}

// 处理每页条数变化
const handleSizeChange = (size: number) => {
  emit('size-change', size)
}

// 处理行选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
  emit('selection-change', selection)
}

// 处理行点击
const handleRowClick = (row: any, column: any, event: Event) => {
  emit('row-click', row, column, event)
}

// 是否显示分页
const showPagination = computed(() => 
  props.pagination && props.pagination.total > 0
)
</script>

<template>
  <div class="data-table">
    <el-table
      :data="data"
      :border="border !== false"
      :stripe="stripe !== false"
      :row-key="rowKey"
      v-loading="loading"
      @selection-change="handleSelectionChange"
      @row-click="handleRowClick"
    >
      <!-- 可选择列 -->
      <el-table-column
        v-if="showSelection"
        type="selection"
        width="55"
        align="center"
      />
      
      <!-- 动态列 -->
      <el-table-column
        v-for="(column, index) in columns"
        :key="index"
        :prop="column.prop"
        :label="column.label"
        :width="column.width"
        :formatter="column.formatter"
        :align="column.align || 'left'"
        :fixed="column.fixed"
      />
      
      <!-- 插槽操作列 -->
      <slot name="operations"></slot>
    </el-table>
    
    <!-- 分页 -->
    <el-pagination
      v-if="showPagination"
      v-model:current-page="pagination.currentPage"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<style scoped>
.data-table {
  background-color: #fff;
  border-radius: 4px;
  overflow: hidden;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style> 