<template>
  <div class="member-pricing-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>会员价格配置</h3>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增配置
          </el-button>
        </div>
      </template>

      <!-- 查询条件 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="商品ID">
          <el-input v-model="searchForm.productId" placeholder="请输入商品ID" clearable />
        </el-form-item>
        <el-form-item label="会员等级">
          <el-select v-model="searchForm.memberLevel" placeholder="选择会员等级" clearable>
            <el-option label="银卡会员" value="SILVER" />
            <el-option label="金卡会员" value="GOLD" />
            <el-option label="白金会员" value="PLATINUM" />
            <el-option label="钻石会员" value="DIAMOND" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.isActive" placeholder="选择状态" clearable>
            <el-option label="激活" :value="true" />
            <el-option label="停用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productId" label="商品ID" width="100" />
        <el-table-column prop="memberLevel" label="会员等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getMemberLevelType(row.memberLevel)">
              {{ getMemberLevelText(row.memberLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="discountType" label="折扣类型" width="120">
          <template #default="{ row }">
            <span>{{ row.discountTypeDesc }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="discountValue" label="折扣值" width="100">
          <template #default="{ row }">
            <span>{{ formatDiscountValue(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="minPurchaseQuantity" label="最小购买量" width="100" />
        <el-table-column prop="maxPurchaseQuantity" label="最大购买量" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.isActive"
              @change="handleStatusChange(row)"
              :disabled="statusChanging"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" size="small" @click="handleCalculate(row)">测算</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :small="false"
        :disabled="false"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <MemberPricingDialog
      v-model="dialogVisible"
      :form-data="editForm"
      :is-edit="isEdit"
      @success="handleDialogSuccess"
    />

    <!-- 价格测算对话框 -->
    <MemberPricingCalculator
      v-model="calculatorVisible"
      :pricing-config="selectedPricing"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { MemberPricingDTO } from '@/types/member-pricing'
import { memberPricingApi } from '@/api/member-pricing'
import MemberPricingDialog from './components/MemberPricingDialog.vue'
import MemberPricingCalculator from './components/MemberPricingCalculator.vue'

// 响应式数据
const tableData = ref<MemberPricingDTO[]>([])
const dialogVisible = ref(false)
const calculatorVisible = ref(false)
const isEdit = ref(false)
const statusChanging = ref(false)
const selectedPricing = ref<MemberPricingDTO | null>(null)

const searchForm = reactive({
  productId: '',
  memberLevel: '',
  isActive: undefined as boolean | undefined
})

const editForm = ref<Partial<MemberPricingDTO>>({})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 获取数据
const fetchData = async () => {
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.size,
      productId: searchForm.productId || undefined,
      memberLevel: searchForm.memberLevel || undefined,
      isActive: searchForm.isActive
    }
    
    const response = await memberPricingApi.getPage(params)
    if (response.success) {
      tableData.value = response.data || []
      pagination.total = response.totalCount || 0
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    productId: '',
    memberLevel: '',
    isActive: undefined
  })
  pagination.page = 1
  fetchData()
}

// 新增
const handleCreate = () => {
  isEdit.value = false
  editForm.value = {
    discountType: 'PERCENTAGE',
    minPurchaseQuantity: 1,
    isActive: true
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: MemberPricingDTO) => {
  isEdit.value = true
  editForm.value = { ...row }
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: MemberPricingDTO) => {
  try {
    await ElMessageBox.confirm('确定要删除这个配置吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await memberPricingApi.delete(row.id!)
    if (response.success) {
      ElMessage.success('删除成功')
      fetchData()
    }
  } catch (error) {
    // 用户取消删除或删除失败
  }
}

// 状态切换
const handleStatusChange = async (row: MemberPricingDTO) => {
  statusChanging.value = true
  try {
    const response = row.isActive
      ? await memberPricingApi.activate(row.id!)
      : await memberPricingApi.deactivate(row.id!)
    
    if (response.success) {
      ElMessage.success(`${row.isActive ? '激活' : '停用'}成功`)
    } else {
      row.isActive = !row.isActive // 回滚状态
      ElMessage.error(response.errMessage || '操作失败')
    }
  } catch (error) {
    row.isActive = !row.isActive // 回滚状态
    ElMessage.error('操作失败')
  } finally {
    statusChanging.value = false
  }
}

// 价格测算
const handleCalculate = (row: MemberPricingDTO) => {
  selectedPricing.value = row
  calculatorVisible.value = true
}

// 分页
const handleSizeChange = (val: number) => {
  pagination.size = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  pagination.page = val
  fetchData()
}

// 对话框成功回调
const handleDialogSuccess = () => {
  dialogVisible.value = false
  fetchData()
}

// 格式化方法
const getMemberLevelType = (level: string) => {
  const typeMap: Record<string, string> = {
    SILVER: '',
    GOLD: 'warning',
    PLATINUM: 'success',
    DIAMOND: 'danger'
  }
  return typeMap[level] || ''
}

const getMemberLevelText = (level: string) => {
  const textMap: Record<string, string> = {
    SILVER: '银卡',
    GOLD: '金卡',
    PLATINUM: '白金',
    DIAMOND: '钻石'
  }
  return textMap[level] || level
}

const formatDiscountValue = (row: MemberPricingDTO) => {
  switch (row.discountType) {
    case 'PERCENTAGE':
      return `${row.discountValue}%`
    case 'FIXED_AMOUNT':
      return `¥${(row.discountValue / 100).toFixed(2)}`
    case 'FIXED_PRICE':
      return `¥${(row.discountValue / 100).toFixed(2)}`
    default:
      return row.discountValue
  }
}

const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.member-pricing-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
}

.search-form {
  margin-bottom: 20px;
}
</style>