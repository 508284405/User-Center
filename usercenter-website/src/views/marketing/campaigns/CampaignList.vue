<template>
  <div class="campaign-list">
    <div class="page-header">
      <h2>营销活动管理</h2>
      <div class="actions">
        <el-button type="primary" @click="handleAdd">新建活动</el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px" inline>
        <el-form-item label="活动名称">
          <el-input v-model="searchForm.name" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable>
            <el-option label="折扣活动" value="DISCOUNT" />
            <el-option label="积分翻倍" value="POINTS_MULTIPLY" />
            <el-option label="优惠券雨" value="COUPON_RAIN" />
            <el-option label="抽奖活动" value="LUCKY_DRAW" />
            <el-option label="限时优惠" value="LIMITED_TIME_OFFER" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="暂停" value="PAUSED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="name" label="活动名称" min-width="150" />
        <el-table-column prop="type" label="活动类型" min-width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="currentParticipants" label="参与人数" min-width="100">
          <template #default="{ row }">
            {{ row.currentParticipants }}
            <span v-if="row.participantLimit">/ {{ row.participantLimit }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="budget" label="预算使用" min-width="120">
          <template #default="{ row }">
            <div v-if="row.budget">
              ¥{{ (row.usedBudget / 100).toFixed(2) }} / ¥{{ (row.budget / 100).toFixed(2) }}
              <el-progress 
                :percentage="Math.round((row.usedBudget / row.budget) * 100)"
                :stroke-width="4"
                style="margin-top: 4px;"
              />
            </div>
            <span v-else>无限制</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="300">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              v-if="row.status === 'DRAFT'" 
              size="small" 
              @click="handleEdit(row)">编辑</el-button>
            <el-button 
              v-if="row.status === 'DRAFT'" 
              size="small" 
              type="success" 
              @click="handleActivate(row)">激活</el-button>
            <el-button 
              v-if="row.status === 'ACTIVE'" 
              size="small" 
              type="warning" 
              @click="handlePause(row)">暂停</el-button>
            <el-button 
              v-if="row.status === 'PAUSED'" 
              size="small" 
              type="success" 
              @click="handleResume(row)">恢复</el-button>
            <el-button 
              v-if="['ACTIVE', 'PAUSED'].includes(row.status)" 
              size="small" 
              type="info" 
              @click="handleComplete(row)">完成</el-button>
            <el-button 
              size="small" 
              @click="handleParticipants(row)">参与记录</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="活动名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入活动名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择活动类型">
                <el-option label="折扣活动" value="DISCOUNT" />
                <el-option label="积分翻倍" value="POINTS_MULTIPLY" />
                <el-option label="优惠券雨" value="COUPON_RAIN" />
                <el-option label="抽奖活动" value="LUCKY_DRAW" />
                <el-option label="限时优惠" value="LIMITED_TIME_OFFER" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="活动描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入活动描述"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="请选择开始时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="请选择结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预算限制">
              <el-input-number
                v-model="form.budgetYuan"
                :min="0"
                :precision="2"
                controls-position="right"
                placeholder="请输入预算(元)"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="参与人数限制">
              <el-input-number
                v-model="form.participantLimit"
                :min="1"
                controls-position="right"
                placeholder="请输入参与人数限制"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="目标用户群体">
          <el-input
            v-model="form.targetAudience"
            type="textarea"
            :rows="3"
            placeholder="请输入目标用户群体配置(JSON格式)"
          />
        </el-form-item>
        
        <el-form-item label="活动规则">
          <el-input
            v-model="form.rules"
            type="textarea"
            :rows="4"
            placeholder="请输入活动规则配置(JSON格式)"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import { formatDateTime } from '@/utils/dateUtils';

const router = useRouter();

// Mock API functions - replace with actual API calls
const campaignApi = {
  getCampaignPage: async (params: any) => {
    // Mock data
    return {
      data: [
        {
          id: 1,
          name: '春节积分翻倍活动',
          type: 'POINTS_MULTIPLY',
          status: 'ACTIVE',
          description: '春节期间所有订单积分翻倍奖励',
          startTime: '2024-02-08 00:00:00',
          endTime: '2024-02-18 23:59:59',
          budget: 1000000,
          usedBudget: 250000,
          participantLimit: 10000,
          currentParticipants: 2500,
          createdBy: 'admin',
          createdAt: '2024-01-15 10:00:00'
        },
        {
          id: 2,
          name: '新用户优惠券雨',
          type: 'COUPON_RAIN',
          status: 'DRAFT',
          description: '新注册用户7天内随机获得优惠券',
          startTime: '2024-01-01 00:00:00',
          endTime: '2024-03-31 23:59:59',
          budget: 500000,
          usedBudget: 0,
          participantLimit: 5000,
          currentParticipants: 0,
          createdBy: 'admin',
          createdAt: '2024-01-10 14:30:00'
        }
      ],
      total: 2
    };
  },
  createCampaign: async (data: any) => {
    console.log('Creating campaign:', data);
    return { id: Date.now() };
  },
  updateCampaign: async (id: number, data: any) => {
    console.log('Updating campaign:', id, data);
    return { id };
  },
  activateCampaign: async (id: number) => {
    console.log('Activating campaign:', id);
  },
  pauseCampaign: async (id: number) => {
    console.log('Pausing campaign:', id);
  },
  resumeCampaign: async (id: number) => {
    console.log('Resuming campaign:', id);
  },
  completeCampaign: async (id: number) => {
    console.log('Completing campaign:', id);
  }
};

// 表格数据
const tableData = ref<any[]>([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 搜索表单
const searchForm = reactive({
  name: '',
  type: undefined as string | undefined,
  status: undefined as string | undefined
});

// 对话框相关
const dialogVisible = ref(false);
const dialogTitle = ref('');
const isEdit = ref(false);
const submitLoading = ref(false);
const formRef = ref();

// 表单数据
const form = reactive({
  id: undefined as number | undefined,
  name: '',
  description: '',
  type: '',
  startTime: '',
  endTime: '',
  targetAudience: '',
  rules: '',
  budgetYuan: undefined as number | undefined,
  participantLimit: undefined as number | undefined
});

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
};

// 获取数据
const fetchData = async () => {
  loading.value = true;
  try {
    const params = {
      pageIndex: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    };
    
    const res = await campaignApi.getCampaignPage(params);
    tableData.value = res.data;
    total.value = res.total;
  } catch (error) {
    ElMessage.error('获取数据失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 工具函数
const getTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    DISCOUNT: '折扣活动',
    POINTS_MULTIPLY: '积分翻倍',
    COUPON_RAIN: '优惠券雨',
    LUCKY_DRAW: '抽奖活动',
    LIMITED_TIME_OFFER: '限时优惠'
  };
  return labels[type] || type;
};

const getTypeTagType = (type: string) => {
  const types: Record<string, string> = {
    DISCOUNT: 'success',
    POINTS_MULTIPLY: 'warning',
    COUPON_RAIN: 'info',
    LUCKY_DRAW: 'danger',
    LIMITED_TIME_OFFER: 'primary'
  };
  return types[type] || '';
};

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    DRAFT: '草稿',
    ACTIVE: '活跃',
    PAUSED: '暂停',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  };
  return labels[status] || status;
};

const getStatusTagType = (status: string) => {
  const types: Record<string, string> = {
    DRAFT: 'info',
    ACTIVE: 'success',
    PAUSED: 'warning',
    COMPLETED: 'info',
    CANCELLED: 'danger'
  };
  return types[status] || '';
};

// 事件处理
const handleSearch = () => {
  currentPage.value = 1;
  fetchData();
};

const handleReset = () => {
  searchForm.name = '';
  searchForm.type = undefined;
  searchForm.status = undefined;
  currentPage.value = 1;
  fetchData();
};

const handleAdd = () => {
  dialogTitle.value = '新建营销活动';
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
};

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑营销活动';
  isEdit.value = true;
  
  Object.assign(form, {
    id: row.id,
    name: row.name,
    description: row.description,
    type: row.type,
    startTime: row.startTime,
    endTime: row.endTime,
    targetAudience: row.targetAudience,
    rules: row.rules,
    budgetYuan: row.budget ? row.budget / 100 : undefined,
    participantLimit: row.participantLimit
  });
  
  dialogVisible.value = true;
};

const handleView = (row: any) => {
  router.push(`/marketing/campaigns/${row.id}`);
};

const handleActivate = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认激活该营销活动吗？', '提示', {
      type: 'warning'
    });
    
    await campaignApi.activateCampaign(row.id);
    ElMessage.success('激活成功');
    fetchData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('激活失败');
    }
  }
};

const handlePause = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认暂停该营销活动吗？', '提示', {
      type: 'warning'
    });
    
    await campaignApi.pauseCampaign(row.id);
    ElMessage.success('暂停成功');
    fetchData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('暂停失败');
    }
  }
};

const handleResume = async (row: any) => {
  try {
    await campaignApi.resumeCampaign(row.id);
    ElMessage.success('恢复成功');
    fetchData();
  } catch (error) {
    ElMessage.error('恢复失败');
  }
};

const handleComplete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认完成该营销活动吗？完成后将无法再次激活。', '提示', {
      type: 'warning'
    });
    
    await campaignApi.completeCampaign(row.id);
    ElMessage.success('完成成功');
    fetchData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('完成失败');
    }
  }
};

const handleParticipants = (row: any) => {
  router.push(`/marketing/campaigns/${row.id}/participants`);
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    submitLoading.value = true;
    
    const submitData = {
      ...form,
      budget: form.budgetYuan ? Math.round(form.budgetYuan * 100) : null
    };
    delete submitData.budgetYuan;
    
    if (isEdit.value && form.id) {
      await campaignApi.updateCampaign(form.id, submitData);
      ElMessage.success('更新成功');
    } else {
      await campaignApi.createCampaign(submitData);
      ElMessage.success('创建成功');
    }
    
    dialogVisible.value = false;
    fetchData();
  } catch (error) {
    console.error(error);
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败');
  } finally {
    submitLoading.value = false;
  }
};

const resetForm = () => {
  Object.assign(form, {
    id: undefined,
    name: '',
    description: '',
    type: '',
    startTime: '',
    endTime: '',
    targetAudience: '',
    rules: '',
    budgetYuan: undefined,
    participantLimit: undefined
  });
};

const handleSizeChange = (val: number) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchData();
};

const handleCurrentChange = (val: number) => {
  currentPage.value = val;
  fetchData();
};

// 初始化数据
onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.campaign-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.actions {
  display: flex;
  gap: 10px;
}

.search-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>