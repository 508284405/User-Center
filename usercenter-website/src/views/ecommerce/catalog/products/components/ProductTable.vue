<template>
  <div class="product-table">
    <el-table :data="products" border style="width: 100%">
      <el-table-column prop="id" label="商品ID" width="80" />
      <el-table-column label="商品图片" width="100">
        <template #default="scope">
          <el-image 
            v-if="scope.row.images && scope.row.images.length > 0" 
            :src="scope.row.images[0]" 
            style="width: 60px; height: 60px; object-fit: cover;"
            :preview-src-list="scope.row.images"
          />
          <span v-else>无图片</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="scope">
          ¥{{ scope.row.price }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleDetail(scope.row)">详情</el-button>
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          <el-button
            size="small"
            :type="scope.row.status === 1 ? 'warning' : 'success'"
            @click="handleStatusChange(scope.row)"
            v-if="scope.row.status !== 0"
          >
            {{ scope.row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button
            size="small"
            type="success"
            @click="handleStatusChange(scope.row)"
            v-if="scope.row.status === 0"
          >
            上架
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineProps, defineEmits } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { deleteProduct, updateProductStatus, type Product } from '@/api/client-web/product';

interface Props {
  products: Product[];
  total: number;
  currentPage: number;
  pageSize: number;
}

const props = defineProps<Props>();

const emit = defineEmits([
  'detail',
  'edit',
  'refresh',
  'update:currentPage',
  'update:pageSize'
]);

const currentPage = ref(props.currentPage);
const pageSize = ref(props.pageSize);

// 监听分页变化
const handleSizeChange = (val: number) => {
  pageSize.value = val;
  emit('update:pageSize', val);
  emit('refresh');
};

const handleCurrentChange = (val: number) => {
  currentPage.value = val;
  emit('update:currentPage', val);
  emit('refresh');
};

// 获取状态类型
const getStatusType = (status: number): string => {
  return status === 1 ? 'success' : status === 0 ? 'info' : 'warning';
};

// 获取状态文本
const getStatusText = (status: number): string => {
  return status === 0 ? '草稿' : status === 1 ? '上架' : '下架';
};

// 详情
const handleDetail = (row: Product) => {
  emit('detail', row);
};

// 编辑
const handleEdit = (row: Product) => {
  emit('edit', row);
};

// 删除商品
const handleDelete = (row: Product) => {
  ElMessageBox.confirm('确认删除该商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteProduct(row.id!);
      ElMessage.success('删除成功');
      emit('refresh');
    } catch (error) {
      ElMessage.error('删除失败');
    }
  });
};

// 更新商品状态
const handleStatusChange = async (row: Product) => {
  try {
    const newStatus = row.status === 0 ? 1 : row.status === 1 ? 2 : 1;
    await updateProductStatus(row.id!, newStatus);
    ElMessage.success('状态更新成功');
    emit('refresh');
  } catch (error) {
    ElMessage.error('状态更新失败');
  }
};
</script>

<style scoped>
.product-table {
  width: 100%;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>