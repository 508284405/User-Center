<template>
  <div class="test-order-create-container">
    <div class="page-header">
      <el-page-header @back="goBack" content="测试订单创建">
      </el-page-header>
    </div>

    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>订单信息</span>
          <el-button type="primary" @click="submitOrder" :loading="submitting">创建订单</el-button>
        </div>
      </template>

      <el-form :model="orderForm" :rules="orderRules" ref="orderFormRef" label-width="120px">
        <!-- 用户信息 -->
        <el-card class="section-card">
          <template #header>
            <span>用户信息</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="搜索用户" prop="userSearch">
                <el-input 
                  v-model="userSearchText" 
                  placeholder="输入用户名、邮箱或手机号搜索" 
                  @keyup.enter="searchUser"
                  style="width: 100%;"
                >
                  <template #append>
                    <el-button @click="searchUser" :loading="userSearchLoading">
                      <el-icon><Search /></el-icon>
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="16">
              <el-form-item label="选中用户" prop="userId" v-if="selectedUser">
                <el-descriptions :column="3" border>
                  <el-descriptions-item label="用户ID">{{ selectedUser.id }}</el-descriptions-item>
                  <el-descriptions-item label="用户名">{{ selectedUser.username }}</el-descriptions-item>
                  <el-descriptions-item label="邮箱">{{ selectedUser.email }}</el-descriptions-item>
                  <el-descriptions-item label="手机号">{{ selectedUser.phone }}</el-descriptions-item>
                </el-descriptions>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="clearSelectedUser" 
                  style="margin-top: 10px;"
                >
                  重新选择用户
                </el-button>
              </el-form-item>
              <el-form-item v-else>
                <el-alert 
                  title="请先搜索并选择用户" 
                  type="warning" 
                  :closable="false"
                  show-icon
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>
        
        <!-- 用户搜索结果对话框 -->
        <el-dialog title="选择用户" v-model="userSearchDialogVisible" width="800px">
          <div v-if="userSearchResults.length > 0">
            <el-table :data="userSearchResults" @row-click="selectUser" style="cursor: pointer;">
              <el-table-column prop="id" label="用户ID" width="100"></el-table-column>
              <el-table-column prop="username" label="用户名"></el-table-column>
              <el-table-column prop="email" label="邮箱"></el-table-column>
              <el-table-column prop="phone" label="手机号"></el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="selectUser(scope.row)">选择</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-empty v-else description="未找到匹配的用户" />
          <template #footer>
            <el-button @click="userSearchDialogVisible = false">取消</el-button>
          </template>
        </el-dialog>

        <!-- 收货地址 -->
        <el-card class="section-card">
          <template #header>
            <span>收货地址</span>
          </template>
          <el-form-item label="收货人姓名" prop="receiverName">
            <el-input v-model="orderForm.receiverName" placeholder="请输入收货人姓名" style="width: 300px;" />
          </el-form-item>
          <el-form-item label="收货人电话" prop="receiverPhone">
            <el-input v-model="orderForm.receiverPhone" placeholder="请输入收货人电话" style="width: 300px;" />
          </el-form-item>
          <el-form-item label="收货地址" prop="receiverAddress">
            <el-input 
              v-model="orderForm.receiverAddress" 
              type="textarea" 
              :rows="2"
              placeholder="请输入详细收货地址" 
              style="width: 500px;" 
            />
          </el-form-item>
        </el-card>

        <!-- 订单商品 -->
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span>订单商品</span>
              <el-button type="primary" size="small" @click="addOrderItem">添加商品</el-button>
            </div>
          </template>
          
          <div v-if="orderForm.items.length === 0" class="empty-items">
            <el-empty description="暂无商品，请添加订单商品" />
          </div>
          
          <div v-else>
            <div 
              v-for="(item, index) in orderForm.items" 
              :key="index" 
              class="order-item-form"
            >
              <div class="item-header">
                <span>商品 {{ index + 1 }}</span>
                <el-button type="danger" size="small" @click="removeOrderItem(index)">删除</el-button>
              </div>
              
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item :label="`商品ID`" :prop="`items.${index}.productId`" :rules="itemRules.productId">
                    <el-input-number v-model="item.productId" :min="1" style="width: 100%;" placeholder="商品ID" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item :label="`SKU ID`" :prop="`items.${index}.skuId`" :rules="itemRules.skuId">
                    <el-input-number v-model="item.skuId" :min="1" style="width: 100%;" placeholder="SKU ID" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item :label="`商品名称`" :prop="`items.${index}.productName`" :rules="itemRules.productName">
                    <el-input v-model="item.productName" placeholder="商品名称" />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item :label="`数量`" :prop="`items.${index}.quantity`" :rules="itemRules.quantity">
                    <el-input-number v-model="item.quantity" :min="1" style="width: 100%;" @change="calculateTotalPrice(index)" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item :label="`单价`" :prop="`items.${index}.unitPrice`" :rules="itemRules.unitPrice">
                    <el-input-number 
                      v-model="item.unitPrice" 
                      :min="0.01" 
                      :precision="2" 
                      style="width: 100%;" 
                      @change="calculateTotalPrice(index)"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item :label="`总价`">
                    <el-input-number v-model="item.totalPrice" :precision="2" disabled style="width: 100%;" />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item :label="`SKU属性`">
                <div class="sku-properties">
                  <div v-for="(prop, propIndex) in item.skuPropertiesList" :key="propIndex" class="property-item">
                    <el-input v-model="prop.key" placeholder="属性名" style="width: 120px;" />
                    <span style="margin: 0 10px;">:</span>
                    <el-input v-model="prop.value" placeholder="属性值" style="width: 120px;" />
                    <el-button type="danger" size="small" @click="removeSkuProperty(index, propIndex)" style="margin-left: 10px;">删除</el-button>
                  </div>
                  <el-button type="primary" size="small" @click="addSkuProperty(index)">添加属性</el-button>
                </div>
              </el-form-item>
            </div>
          </div>
        </el-card>

        <!-- 订单汇总 -->
        <el-card class="section-card">
          <template #header>
            <span>订单汇总</span>
          </template>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="商品数量">{{ totalQuantity }}</el-descriptions-item>
            <el-descriptions-item label="订单总金额">¥{{ totalAmount.toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="预计用户">
              <span v-if="selectedUser">{{ selectedUser.username }} (ID: {{ selectedUser.id }})</span>
              <span v-else class="text-warning">未选择用户</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-form>
    </el-card>

    <!-- 创建结果对话框 -->
    <el-dialog title="订单创建结果" v-model="resultDialogVisible" width="500px">
      <div v-if="createResult.success" class="success-result">
        <el-result icon="success" title="订单创建成功">
          <template #sub-title>
            <p>订单号: {{ createResult.orderNumber }}</p>
            <p>您可以在订单管理中查看此订单详情</p>
          </template>
          <template #extra>
            <el-button type="primary" @click="viewOrderDetail">查看订单详情</el-button>
            <el-button @click="createAnother">再创建一个</el-button>
          </template>
        </el-result>
      </div>
      <div v-else class="error-result">
        <el-result icon="error" title="订单创建失败">
          <template #sub-title>
            <p>{{ createResult.errorMessage }}</p>
          </template>
          <template #extra>
            <el-button type="primary" @click="resultDialogVisible = false">重新填写</el-button>
          </template>
        </el-result>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, FormInstance } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import { createOrder, CreateOrderRequest, OrderItemRequest } from '@/api/client-web/order';
import { userApi } from '@/api/usercenter/user';

interface SkuProperty {
  key: string;
  value: string;
}

interface OrderItemForm extends Omit<OrderItemRequest, 'skuProperties'> {
  skuPropertiesList: SkuProperty[];
}

interface OrderForm extends Omit<CreateOrderRequest, 'items'> {
  items: OrderItemForm[];
}

interface User {
  id: number;
  username: string;
  email: string;
  phone: string;
}

const router = useRouter();
const submitting = ref(false);
const resultDialogVisible = ref(false);
const orderFormRef = ref<FormInstance | null>(null);

// 用户搜索相关
const userSearchText = ref('');
const userSearchLoading = ref(false);
const userSearchDialogVisible = ref(false);
const userSearchResults = ref<User[]>([]);
const selectedUser = ref<User | null>(null);

const orderForm = reactive<OrderForm>({
  userId: 0,
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  items: []
});

const createResult = reactive({
  success: false,
  orderNumber: '',
  errorMessage: ''
});

const orderRules = {
  userId: [
    { 
      required: true, 
      validator: (rule: any, value: number, callback: Function) => {
        if (!selectedUser.value) {
          callback(new Error('请先搜索并选择用户'));
        } else {
          callback();
        }
      },
      trigger: 'blur' 
    }
  ],
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入收货人电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  receiverAddress: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
};

const itemRules = {
  productId: [{ required: true, message: '请输入商品ID', trigger: 'blur' }],
  skuId: [{ required: true, message: '请输入SKU ID', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }]
};

const totalQuantity = computed(() => {
  return orderForm.items.reduce((sum, item) => sum + (item.quantity || 0), 0);
});

const totalAmount = computed(() => {
  return orderForm.items.reduce((sum, item) => sum + (item.totalPrice || 0), 0);
});

// 用户搜索功能
const searchUser = async () => {
  if (!userSearchText.value.trim()) {
    ElMessage.warning('请输入搜索关键词');
    return;
  }

  userSearchLoading.value = true;
  userSearchResults.value = [];

  try {
    let searchResult = null;
    const keyword = userSearchText.value.trim();

    // 根据输入内容判断搜索类型
    if (/^1[3-9]\d{9}$/.test(keyword)) {
      // 手机号格式
      searchResult = await userApi.getByPhone(keyword);
      if (searchResult) {
        userSearchResults.value = [searchResult];
      }
    } else if (/\S+@\S+\.\S+/.test(keyword)) {
      // 邮箱格式
      searchResult = await userApi.getByEmail(keyword);
      if (searchResult) {
        userSearchResults.value = [searchResult];
      }
    } else {
      // 用户名格式，同时也搜索分页结果
      try {
        searchResult = await userApi.getByUsername(keyword);
        if (searchResult) {
          userSearchResults.value = [searchResult];
        }
      } catch (error) {
        // 如果用户名精确搜索失败，尝试模糊搜索
        const pageResult = await userApi.page({
          pageNum: 1,
          pageSize: 20,
          username: keyword
        });
        if (pageResult && pageResult.data) {
          userSearchResults.value = pageResult.data;
        }
      }
    }

    if (userSearchResults.value.length === 0) {
      ElMessage.warning('未找到匹配的用户');
    } else {
      userSearchDialogVisible.value = true;
    }
  } catch (error: any) {
    console.error('搜索用户失败:', error);
    ElMessage.error(`搜索用户失败: ${error.message || '网络错误'}`);
  } finally {
    userSearchLoading.value = false;
  }
};

// 选择用户
const selectUser = (user: User) => {
  selectedUser.value = user;
  orderForm.userId = user.id;
  userSearchDialogVisible.value = false;
  userSearchText.value = '';
  userSearchResults.value = [];
  ElMessage.success(`已选择用户: ${user.username}`);
};

// 清除选中的用户
const clearSelectedUser = () => {
  selectedUser.value = null;
  orderForm.userId = 0;
  userSearchText.value = '';
  userSearchResults.value = [];
};

const addOrderItem = () => {
  orderForm.items.push({
    productId: 0,
    skuId: 0,
    productName: '',
    unitPrice: 0,
    quantity: 1,
    totalPrice: 0,
    skuPropertiesList: []
  });
};

const removeOrderItem = (index: number) => {
  orderForm.items.splice(index, 1);
};

const calculateTotalPrice = (index: number) => {
  const item = orderForm.items[index];
  if (item.quantity && item.unitPrice) {
    item.totalPrice = item.quantity * item.unitPrice;
  }
};

const addSkuProperty = (itemIndex: number) => {
  orderForm.items[itemIndex].skuPropertiesList.push({ key: '', value: '' });
};

const removeSkuProperty = (itemIndex: number, propIndex: number) => {
  orderForm.items[itemIndex].skuPropertiesList.splice(propIndex, 1);
};

const convertSkuProperties = (skuPropertiesList: SkuProperty[]): Record<string, string> => {
  const properties: Record<string, string> = {};
  skuPropertiesList.forEach(prop => {
    if (prop.key && prop.value) {
      properties[prop.key] = prop.value;
    }
  });
  return properties;
};

const submitOrder = async () => {
  if (!orderFormRef.value) return;

  // 验证至少有一个订单项
  if (orderForm.items.length === 0) {
    ElMessage.error('请至少添加一个订单商品');
    return;
  }

  await orderFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        // 转换数据格式
        const orderData: CreateOrderRequest = {
          userId: orderForm.userId,
          receiverName: orderForm.receiverName,
          receiverPhone: orderForm.receiverPhone,
          receiverAddress: orderForm.receiverAddress,
          items: orderForm.items.map(item => ({
            productId: item.productId,
            skuId: item.skuId,
            productName: item.productName,
            skuProperties: convertSkuProperties(item.skuPropertiesList),
            unitPrice: item.unitPrice,
            quantity: item.quantity,
            totalPrice: item.totalPrice
          }))
        };

        const response = await createOrder(orderData);
        if (response.success && response.data) {
          createResult.success = true;
          createResult.orderNumber = response.data;
          createResult.errorMessage = '';
          ElMessage.success('订单创建成功！');
        } else {
          createResult.success = false;
          createResult.orderNumber = '';
          createResult.errorMessage = response.errMessage || '创建失败';
          ElMessage.error(createResult.errorMessage);
        }
        resultDialogVisible.value = true;
      } catch (error: any) {
        console.error('创建订单失败:', error);
        createResult.success = false;
        createResult.orderNumber = '';
        createResult.errorMessage = error.message || '网络错误';
        ElMessage.error(`创建订单失败: ${createResult.errorMessage}`);
        resultDialogVisible.value = true;
      } finally {
        submitting.value = false;
      }
    }
  });
};

const goBack = () => {
  router.push({ name: 'OrderList' });
};

const viewOrderDetail = () => {
  resultDialogVisible.value = false;
  router.push({ 
    name: 'OrderDetail', 
    params: { orderNumber: createResult.orderNumber } 
  });
};

const createAnother = () => {
  resultDialogVisible.value = false;
  // 重置表单
  clearSelectedUser();
  orderForm.receiverName = '';
  orderForm.receiverPhone = '';
  orderForm.receiverAddress = '';
  orderForm.items = [];
  orderFormRef.value?.resetFields();
};

onMounted(() => {
  // 添加一个默认订单项
  addOrderItem();
});
</script>

<style scoped>
.test-order-create-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.form-card {
  margin-top: 20px;
}

.section-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-items {
  padding: 20px 0;
  text-align: center;
}

.order-item-form {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
  background-color: #fafafa;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-weight: bold;
}

.sku-properties {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 15px;
  background-color: #f9f9f9;
}

.property-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.success-result .el-result,
.error-result .el-result {
  padding: 20px 0;
}

.text-warning {
  color: #E6A23C;
  font-style: italic;
}
</style>