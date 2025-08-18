<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createLogisticsOrder } from '@/api/client-web/logistics'
import type { CreateLogisticsOrderParams } from '@/types/logisticsOrderCreate'

const router = useRouter()

const form = reactive<CreateLogisticsOrderParams>({
  vendorId: undefined,
  vendorName: '',
  refNo: '',
  providerCode: '',
  bizType: '',
  expectedPickupAt: '',
  senderName: '',
  senderMobile: '',
  senderProvince: '',
  senderCity: '',
  senderDistrict: '',
  senderAddress: '',
  receiverName: '',
  receiverMobile: '',
  receiverProvince: '',
  receiverCity: '',
  receiverDistrict: '',
  receiverAddress: '',
  packages: [
    {
      productName: '',
      qty: undefined,
      skuId: undefined,
      weight: undefined,
      length: undefined,
      width: undefined,
      height: undefined
    }
  ]
})

const loading = ref(false)

const addPackageRow = () => {
  form.packages?.push({
    productName: '',
    qty: undefined,
    skuId: undefined,
    weight: undefined,
    length: undefined,
    width: undefined,
    height: undefined
  })
}

const removePackageRow = (idx: number) => {
  if (form.packages && form.packages.length > 1) {
    form.packages.splice(idx, 1)
  }
}

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await createLogisticsOrder(form)
    if (response.success) {
      ElMessage.success('创建成功')
      router.push(`/dashboard/logistics/orders/${response.data.id}`)
    } else {
      ElMessage.error(response.errMessage || '创建失败')
    }
  } catch (e) {
    ElMessage.error('创建失败')
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  router.back()
}
</script>

<template>
  <div class="order-create-container">
    <h2 class="page-title">创建物流订单</h2>
    <el-form :model="form" label-width="120px" class="form-root">
      <el-divider>订单信息</el-divider>
      <el-row :gutter="20" class="form-section">
        <el-col :span="8">
          <el-form-item label="业务单号">
            <el-input v-model="form.refNo" placeholder="业务单号" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="物流商">
            <el-select v-model="form.providerCode" placeholder="请选择物流商">
              <el-option label="菜鸟裹裹" value="CAINIAO" />
              <el-option label="顺丰快递" value="SF" />
              <el-option label="京东物流" value="JD" />
              <el-option label="圆通快递" value="YTO" />
              <el-option label="中通快递" value="ZTO" />
              <el-option label="申通快递" value="STO" />
              <el-option label="韵达快递" value="YD" />
              <el-option label="百世快递" value="BEST" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="业务类型">
            <el-select v-model="form.bizType" placeholder="请选择业务类型">
              <el-option label="发货" value="DELIVERY" />
              <el-option label="补发" value="REDELIVERY" />
              <el-option label="退货" value="RETURN" />
              <el-option label="换货发出" value="EXCHANGE_SEND" />
              <el-option label="换货回收" value="EXCHANGE_BACK" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="预计揽收时间">
            <el-date-picker v-model="form.expectedPickupAt" type="datetime" placeholder="选择时间" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-divider>寄件人信息</el-divider>
      <el-row :gutter="20" class="form-section">
        <el-col :span="8">
          <el-form-item label="姓名">
            <el-input v-model="form.senderName" placeholder="姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="手机号">
            <el-input v-model="form.senderMobile" placeholder="手机号" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="省份">
            <el-input v-model="form.senderProvince" placeholder="省份" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="城市">
            <el-input v-model="form.senderCity" placeholder="城市" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="区县">
            <el-input v-model="form.senderDistrict" placeholder="区县" />
          </el-form-item>
        </el-col>
        <el-col :span="16">
          <el-form-item label="详细地址">
            <el-input v-model="form.senderAddress" placeholder="详细地址" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-divider>收件人信息</el-divider>
      <el-row :gutter="20" class="form-section">
        <el-col :span="8">
          <el-form-item label="姓名">
            <el-input v-model="form.receiverName" placeholder="姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="手机号">
            <el-input v-model="form.receiverMobile" placeholder="手机号" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="省份">
            <el-input v-model="form.receiverProvince" placeholder="省份" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="城市">
            <el-input v-model="form.receiverCity" placeholder="城市" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="区县">
            <el-input v-model="form.receiverDistrict" placeholder="区县" />
          </el-form-item>
        </el-col>
        <el-col :span="16">
          <el-form-item label="详细地址">
            <el-input v-model="form.receiverAddress" placeholder="详细地址" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-divider>包裹信息</el-divider>
      <div class="form-section">
        <el-table :data="form.packages" border style="width: 100%; margin-bottom: 16px;">
          <el-table-column prop="productName" label="商品名称">
            <template #default="{ row }">
              <el-input v-model="row.productName" placeholder="商品名称" />
            </template>
          </el-table-column>
          <el-table-column prop="qty" label="数量">
            <template #default="{ row }">
              <el-input-number v-model="row.qty" :min="1" placeholder="数量" />
            </template>
          </el-table-column>
          <el-table-column prop="skuId" label="SKU ID">
            <template #default="{ row }">
              <el-input v-model="row.skuId" placeholder="SKU ID" />
            </template>
          </el-table-column>
          <el-table-column prop="weight" label="重量(克)">
            <template #default="{ row }">
              <el-input-number v-model="row.weight" :min="0" placeholder="重量" />
            </template>
          </el-table-column>
          <el-table-column prop="length" label="长度(mm)">
            <template #default="{ row }">
              <el-input-number v-model="row.length" :min="0" placeholder="长度" />
            </template>
          </el-table-column>
          <el-table-column prop="width" label="宽度(mm)">
            <template #default="{ row }">
              <el-input-number v-model="row.width" :min="0" placeholder="宽度" />
            </template>
          </el-table-column>
          <el-table-column prop="height" label="高度(mm)">
            <template #default="{ row }">
              <el-input-number v-model="row.height" :min="0" placeholder="高度" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button type="danger" size="small" @click="removePackageRow($index)" :disabled="form.packages.length === 1">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" plain icon="el-icon-plus" @click="addPackageRow">新增包裹</el-button>
      </div>
      <div class="form-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">提交</el-button>
      </div>
    </el-form>
  </div>
</template>

<style scoped>
.order-create-container {
  padding: 24px 0;
}
.page-title {
  margin-bottom: 24px;
  font-size: 20px;
  font-weight: 500;
}
.form-section {
  margin-bottom: 16px;
}
.form-actions {
  margin-top: 32px;
  text-align: right;
}
</style> 