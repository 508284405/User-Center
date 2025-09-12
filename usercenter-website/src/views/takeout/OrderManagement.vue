<template>
  <div class="order-management">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">订单管理</h1>
        <p class="page-subtitle">管理外卖订单状态、处理订单流程和客户服务</p>
      </div>
      <div class="header-stats">
        <div class="stat-item pending">
          <div class="stat-number">{{ pendingCount }}</div>
          <div class="stat-label">待处理</div>
        </div>
        <div class="stat-item processing">
          <div class="stat-number">{{ processingCount }}</div>
          <div class="stat-label">处理中</div>
        </div>
        <div class="stat-item completed">
          <div class="stat-number">{{ completedCount }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-filters">
      <div class="search-section">
        <div class="search-box">
          <i class="fas fa-search"></i>
          <input 
            v-model="searchKeyword" 
            placeholder="搜索订单号、门店名称或客户信息"
            @input="handleSearch"
            class="search-input"
          >
        </div>
      </div>
      
      <div class="filters-section">
        <div class="filter-group">
          <label>订单状态:</label>
          <select v-model="statusFilter" @change="handleFilter" class="filter-select">
            <option value="">全部状态</option>
            <option value="PENDING">待确认</option>
            <option value="CONFIRMED">已确认</option>
            <option value="PREPARING">制作中</option>
            <option value="DELIVERING">配送中</option>
            <option value="COMPLETED">已完成</option>
            <option value="CANCELLED">已取消</option>
          </select>
        </div>
        
        <div class="filter-group">
          <label>门店:</label>
          <select v-model="storeFilter" @change="handleFilter" class="filter-select">
            <option value="">全部门店</option>
            <option v-for="store in stores" :key="store.id" :value="store.id">
              {{ store.name }}
            </option>
          </select>
        </div>
        
        <div class="filter-group">
          <label>时间范围:</label>
          <select v-model="timeFilter" @change="handleFilter" class="filter-select">
            <option value="today">今天</option>
            <option value="yesterday">昨天</option>
            <option value="week">本周</option>
            <option value="month">本月</option>
            <option value="custom">自定义</option>
          </select>
        </div>

        <button @click="exportOrders" class="export-btn">
          <i class="fas fa-download"></i>
          导出订单
        </button>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="orders-container">
      <div class="orders-header">
        <div class="bulk-actions">
          <input 
            type="checkbox" 
            v-model="selectAll" 
            @change="handleSelectAll"
            class="bulk-checkbox"
          >
          <span>批量操作</span>
          <button 
            v-if="selectedOrders.length > 0" 
            @click="showBulkActionMenu = !showBulkActionMenu"
            class="bulk-action-btn"
          >
            操作选中项 ({{ selectedOrders.length }})
            <i class="fas fa-chevron-down"></i>
          </button>
          <div v-if="showBulkActionMenu" class="bulk-action-menu">
            <button @click="bulkUpdateStatus('CONFIRMED')" class="menu-item">
              <i class="fas fa-check"></i>
              批量确认
            </button>
            <button @click="bulkUpdateStatus('CANCELLED')" class="menu-item danger">
              <i class="fas fa-times"></i>
              批量取消
            </button>
            <button @click="bulkExport" class="menu-item">
              <i class="fas fa-download"></i>
              批量导出
            </button>
          </div>
        </div>
        
        <div class="view-options">
          <button 
            @click="viewMode = 'card'" 
            :class="{ active: viewMode === 'card' }"
            class="view-btn"
          >
            <i class="fas fa-th-large"></i>
          </button>
          <button 
            @click="viewMode = 'table'" 
            :class="{ active: viewMode === 'table' }"
            class="view-btn"
          >
            <i class="fas fa-list"></i>
          </button>
        </div>
      </div>

      <!-- 卡片视图 -->
      <div v-if="viewMode === 'card'" class="orders-grid">
        <div 
          v-for="order in filteredOrders" 
          :key="order.id"
          class="order-card"
          :class="{ 
            'order-urgent': isUrgent(order),
            'order-selected': selectedOrders.includes(order.id)
          }"
        >
          <div class="order-header">
            <div class="order-select">
              <input 
                type="checkbox" 
                :value="order.id" 
                v-model="selectedOrders"
                class="order-checkbox"
              >
            </div>
            <div class="order-number">
              <strong>{{ order.orderNumber }}</strong>
              <span class="order-time">{{ formatTime(order.createTime) }}</span>
            </div>
            <div class="order-status">
              <div class="status-badge" :class="order.status.toLowerCase()">
                {{ getStatusText(order.status) }}
              </div>
              <div v-if="isUrgent(order)" class="urgent-badge">
                <i class="fas fa-exclamation-triangle"></i>
                紧急
              </div>
            </div>
          </div>

          <div class="order-content">
            <div class="store-info">
              <div class="store-name">
                <i class="fas fa-store"></i>
                {{ order.storeName }}
              </div>
              <div class="customer-info">
                <i class="fas fa-user"></i>
                {{ order.customerName }} · {{ order.customerPhone }}
              </div>
            </div>

            <div class="delivery-info">
              <div class="delivery-address">
                <i class="fas fa-map-marker-alt"></i>
                {{ order.deliveryAddress }}
              </div>
              <div class="delivery-time">
                <i class="fas fa-clock"></i>
                预计送达: {{ formatDeliveryTime(order.estimatedDeliveryTime) }}
              </div>
            </div>

            <div class="order-items">
              <div class="items-summary">
                <span class="items-count">{{ order.items.length }}个商品</span>
                <div class="items-preview">
                  <span 
                    v-for="(item, index) in order.items.slice(0, 3)" 
                    :key="index"
                    class="item-name"
                  >
                    {{ item.itemName }}{{ index < Math.min(order.items.length, 3) - 1 ? '、' : '' }}
                  </span>
                  <span v-if="order.items.length > 3" class="more-items">
                    等{{ order.items.length }}样
                  </span>
                </div>
              </div>
            </div>

            <div class="order-amount">
              <div class="amount-breakdown">
                <div class="amount-item">
                  <span>商品金额: ¥{{ order.itemsAmount }}</span>
                </div>
                <div class="amount-item">
                  <span>配送费: ¥{{ order.deliveryFee }}</span>
                </div>
                <div v-if="order.discountAmount > 0" class="amount-item discount">
                  <span>优惠: -¥{{ order.discountAmount }}</span>
                </div>
              </div>
              <div class="total-amount">
                总计: ¥{{ order.totalAmount }}
              </div>
            </div>
          </div>

          <div class="order-actions">
            <button 
              v-if="order.status === 'PENDING'" 
              @click="confirmOrder(order)"
              class="action-btn primary"
            >
              <i class="fas fa-check"></i>
              确认订单
            </button>
            <button 
              v-if="order.status === 'CONFIRMED'" 
              @click="startPreparing(order)"
              class="action-btn warning"
            >
              <i class="fas fa-utensils"></i>
              开始制作
            </button>
            <button 
              v-if="order.status === 'PREPARING'" 
              @click="startDelivery(order)"
              class="action-btn info"
            >
              <i class="fas fa-shipping-fast"></i>
              开始配送
            </button>
            <button 
              v-if="order.status === 'DELIVERING'" 
              @click="completeOrder(order)"
              class="action-btn success"
            >
              <i class="fas fa-check-circle"></i>
              完成订单
            </button>
            <button 
              v-if="['PENDING', 'CONFIRMED'].includes(order.status)" 
              @click="cancelOrder(order)"
              class="action-btn danger"
            >
              <i class="fas fa-times"></i>
              取消订单
            </button>
            <button @click="viewOrderDetail(order)" class="action-btn secondary">
              <i class="fas fa-eye"></i>
              查看详情
            </button>
          </div>
        </div>
      </div>

      <!-- 表格视图 -->
      <div v-if="viewMode === 'table'" class="orders-table-container">
        <table class="orders-table">
          <thead>
            <tr>
              <th width="40">
                <input 
                  type="checkbox" 
                  v-model="selectAll" 
                  @change="handleSelectAll"
                >
              </th>
              <th>订单号</th>
              <th>门店</th>
              <th>客户</th>
              <th>商品</th>
              <th>金额</th>
              <th>状态</th>
              <th>下单时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="order in filteredOrders" 
              :key="order.id"
              :class="{ 
                'row-urgent': isUrgent(order),
                'row-selected': selectedOrders.includes(order.id)
              }"
            >
              <td>
                <input 
                  type="checkbox" 
                  :value="order.id" 
                  v-model="selectedOrders"
                >
              </td>
              <td>
                <div class="order-number-cell">
                  {{ order.orderNumber }}
                  <span v-if="isUrgent(order)" class="urgent-indicator">
                    <i class="fas fa-exclamation-triangle"></i>
                  </span>
                </div>
              </td>
              <td>{{ order.storeName }}</td>
              <td>
                <div class="customer-cell">
                  <div>{{ order.customerName }}</div>
                  <div class="phone">{{ order.customerPhone }}</div>
                </div>
              </td>
              <td>
                <div class="items-cell">
                  {{ order.items.length }}个商品
                  <div class="items-tooltip">
                    <div 
                      v-for="item in order.items" 
                      :key="item.id" 
                      class="item-detail"
                    >
                      {{ item.itemName }} x{{ item.quantity }}
                    </div>
                  </div>
                </div>
              </td>
              <td class="amount-cell">¥{{ order.totalAmount }}</td>
              <td>
                <div class="status-badge" :class="order.status.toLowerCase()">
                  {{ getStatusText(order.status) }}
                </div>
              </td>
              <td>{{ formatTime(order.createTime) }}</td>
              <td>
                <div class="table-actions">
                  <button 
                    v-if="order.status === 'PENDING'" 
                    @click="confirmOrder(order)"
                    class="table-btn primary"
                    title="确认订单"
                  >
                    <i class="fas fa-check"></i>
                  </button>
                  <button 
                    v-if="['PENDING', 'CONFIRMED'].includes(order.status)" 
                    @click="cancelOrder(order)"
                    class="table-btn danger"
                    title="取消订单"
                  >
                    <i class="fas fa-times"></i>
                  </button>
                  <button @click="viewOrderDetail(order)" class="table-btn secondary" title="查看详情">
                    <i class="fas fa-eye"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <div class="pagination-info">
          显示 {{ ((currentPage - 1) * pageSize + 1) }} - {{ Math.min(currentPage * pageSize, totalItems) }} 条，
          共 {{ totalItems }} 条记录
        </div>
        <div class="pagination-controls">
          <button 
            @click="currentPage = Math.max(1, currentPage - 1)"
            :disabled="currentPage === 1"
            class="page-btn"
          >
            <i class="fas fa-chevron-left"></i>
          </button>
          <span class="page-numbers">
            <button 
              v-for="page in visiblePages" 
              :key="page"
              @click="currentPage = page"
              :class="{ active: currentPage === page }"
              class="page-number"
            >
              {{ page }}
            </button>
          </span>
          <button 
            @click="currentPage = Math.min(totalPages, currentPage + 1)"
            :disabled="currentPage === totalPages"
            class="page-btn"
          >
            <i class="fas fa-chevron-right"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- 订单详情对话框 -->
    <div v-if="viewingOrder" class="modal-overlay" @click="closeOrderDetail">
      <div class="modal-content large" @click.stop>
        <div class="modal-header">
          <h3>订单详情 - {{ viewingOrder.orderNumber }}</h3>
          <button @click="closeOrderDetail" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <div class="modal-body">
          <div class="order-detail-content">
            <!-- 订单状态时间轴 -->
            <div class="detail-section">
              <h4>订单状态</h4>
              <div class="status-timeline">
                <div 
                  v-for="(statusItem, index) in orderStatusHistory" 
                  :key="index"
                  class="timeline-item"
                  :class="{ active: statusItem.completed, current: statusItem.current }"
                >
                  <div class="timeline-dot">
                    <i :class="statusItem.icon"></i>
                  </div>
                  <div class="timeline-content">
                    <div class="timeline-title">{{ statusItem.title }}</div>
                    <div class="timeline-time">{{ statusItem.time }}</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 基本信息 -->
            <div class="detail-section">
              <h4>基本信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>订单号:</label>
                  <span>{{ viewingOrder.orderNumber }}</span>
                </div>
                <div class="detail-item">
                  <label>下单时间:</label>
                  <span>{{ formatTime(viewingOrder.createTime) }}</span>
                </div>
                <div class="detail-item">
                  <label>门店:</label>
                  <span>{{ viewingOrder.storeName }}</span>
                </div>
                <div class="detail-item">
                  <label>配送方式:</label>
                  <span>{{ viewingOrder.deliveryType === 'DELIVERY' ? '外卖配送' : '到店自取' }}</span>
                </div>
              </div>
            </div>

            <!-- 客户信息 -->
            <div class="detail-section">
              <h4>客户信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>客户姓名:</label>
                  <span>{{ viewingOrder.customerName }}</span>
                </div>
                <div class="detail-item">
                  <label>联系电话:</label>
                  <span>{{ viewingOrder.customerPhone }}</span>
                </div>
                <div class="detail-item">
                  <label>配送地址:</label>
                  <span>{{ viewingOrder.deliveryAddress }}</span>
                </div>
                <div class="detail-item">
                  <label>预计送达:</label>
                  <span>{{ formatDeliveryTime(viewingOrder.estimatedDeliveryTime) }}</span>
                </div>
              </div>
            </div>

            <!-- 商品清单 -->
            <div class="detail-section">
              <h4>商品清单</h4>
              <div class="items-list">
                <div 
                  v-for="item in viewingOrder.items" 
                  :key="item.id"
                  class="item-detail-card"
                >
                  <div class="item-image">
                    <img :src="item.image || '/images/default-food.jpg'" :alt="item.itemName">
                  </div>
                  <div class="item-info">
                    <div class="item-name">{{ item.itemName }}</div>
                    <div class="item-specs" v-if="item.selectedOptions && item.selectedOptions.length">
                      {{ item.selectedOptions.map(opt => opt.optionName).join('、') }}
                    </div>
                    <div class="item-remark" v-if="item.remark">
                      备注: {{ item.remark }}
                    </div>
                  </div>
                  <div class="item-pricing">
                    <div class="unit-price">¥{{ item.unitPrice }}</div>
                    <div class="quantity">x{{ item.quantity }}</div>
                    <div class="subtotal">¥{{ (item.unitPrice * item.quantity).toFixed(2) }}</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 费用明细 -->
            <div class="detail-section">
              <h4>费用明细</h4>
              <div class="amount-details">
                <div class="amount-row">
                  <span>商品小计</span>
                  <span>¥{{ viewingOrder.itemsAmount }}</span>
                </div>
                <div class="amount-row">
                  <span>包装费</span>
                  <span>¥{{ viewingOrder.packagingFee || 0 }}</span>
                </div>
                <div class="amount-row">
                  <span>配送费</span>
                  <span>¥{{ viewingOrder.deliveryFee }}</span>
                </div>
                <div v-if="viewingOrder.discountAmount > 0" class="amount-row discount">
                  <span>优惠抵扣</span>
                  <span>-¥{{ viewingOrder.discountAmount }}</span>
                </div>
                <div class="amount-row total">
                  <span>实付金额</span>
                  <span>¥{{ viewingOrder.totalAmount }}</span>
                </div>
              </div>
            </div>

            <!-- 备注信息 -->
            <div v-if="viewingOrder.orderNote" class="detail-section">
              <h4>订单备注</h4>
              <div class="order-note">
                {{ viewingOrder.orderNote }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 取消订单对话框 -->
    <div v-if="cancellingOrder" class="modal-overlay" @click="closeCancelDialog">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>取消订单</h3>
          <button @click="closeCancelDialog" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <div class="modal-body">
          <div class="cancel-form">
            <p>确定要取消订单 <strong>{{ cancellingOrder.orderNumber }}</strong> 吗？</p>
            <div class="form-group">
              <label>取消原因:</label>
              <select v-model="cancelReason" class="form-select">
                <option value="">请选择取消原因</option>
                <option value="CUSTOMER_REQUEST">客户要求取消</option>
                <option value="STORE_UNAVAILABLE">门店无法制作</option>
                <option value="DELIVERY_ISSUE">配送问题</option>
                <option value="SYSTEM_ERROR">系统错误</option>
                <option value="OTHER">其他原因</option>
              </select>
            </div>
            <div class="form-group">
              <label>详细说明:</label>
              <textarea 
                v-model="cancelNote" 
                rows="3"
                placeholder="请详细说明取消原因..."
                class="form-textarea"
              ></textarea>
            </div>
            <div class="form-actions">
              <button @click="closeCancelDialog" class="btn btn-secondary">
                取消
              </button>
              <button @click="confirmCancel" class="btn btn-danger" :disabled="!cancelReason">
                确认取消订单
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OrderManagement',
  data() {
    return {
      searchKeyword: '',
      statusFilter: '',
      storeFilter: '',
      timeFilter: 'today',
      viewMode: 'card',
      currentPage: 1,
      pageSize: 20,
      selectAll: false,
      selectedOrders: [],
      showBulkActionMenu: false,
      viewingOrder: null,
      cancellingOrder: null,
      cancelReason: '',
      cancelNote: '',
      
      stores: [
        { id: 1, name: '川香麻辣烫' },
        { id: 2, name: '老北京炸酱面' },
        { id: 3, name: '兰州拉面' },
        { id: 4, name: '黄焖鸡米饭' }
      ],

      orders: [
        {
          id: 1,
          orderNumber: 'TO202501090001',
          storeId: 1,
          storeName: '川香麻辣烫',
          customerName: '张三',
          customerPhone: '138****5678',
          deliveryAddress: '北京市朝阳区建国门外大街1号国贸大厦A座2501',
          deliveryType: 'DELIVERY',
          status: 'PENDING',
          createTime: new Date(Date.now() - 600000), // 10分钟前
          estimatedDeliveryTime: new Date(Date.now() + 2400000), // 40分钟后
          items: [
            { id: 1, itemName: '麻辣香锅', unitPrice: 28, quantity: 1, selectedOptions: [{ optionName: '中辣' }], remark: '少放辣椒' },
            { id: 2, itemName: '酸辣粉', unitPrice: 15, quantity: 2, selectedOptions: [], remark: '' }
          ],
          itemsAmount: 58,
          packagingFee: 2,
          deliveryFee: 3,
          discountAmount: 5,
          totalAmount: 58,
          orderNote: '尽快送达，谢谢！'
        },
        {
          id: 2,
          orderNumber: 'TO202501090002',
          storeId: 2,
          storeName: '老北京炸酱面',
          customerName: '李四',
          customerPhone: '139****8765',
          deliveryAddress: '北京市海淀区中关村大街15号科技大厦B座1203',
          deliveryType: 'DELIVERY',
          status: 'CONFIRMED',
          createTime: new Date(Date.now() - 900000), // 15分钟前
          estimatedDeliveryTime: new Date(Date.now() + 1800000), // 30分钟后
          items: [
            { id: 3, itemName: '炸酱面', unitPrice: 22, quantity: 2, selectedOptions: [], remark: '多放黄瓜丝' },
            { id: 4, itemName: '小菜拼盘', unitPrice: 12, quantity: 1, selectedOptions: [], remark: '' }
          ],
          itemsAmount: 56,
          packagingFee: 2,
          deliveryFee: 2,
          discountAmount: 0,
          totalAmount: 60,
          orderNote: ''
        },
        {
          id: 3,
          orderNumber: 'TO202501090003',
          storeId: 3,
          storeName: '兰州拉面',
          customerName: '王五',
          customerPhone: '137****4321',
          deliveryAddress: '北京市西城区西单北大街88号购物中心3层',
          deliveryType: 'DELIVERY',
          status: 'DELIVERING',
          createTime: new Date(Date.now() - 1800000), // 30分钟前
          estimatedDeliveryTime: new Date(Date.now() + 600000), // 10分钟后
          items: [
            { id: 5, itemName: '兰州牛肉拉面', unitPrice: 26, quantity: 1, selectedOptions: [{ optionName: '细面条' }], remark: '汤要热一点' }
          ],
          itemsAmount: 26,
          packagingFee: 1,
          deliveryFee: 4,
          discountAmount: 0,
          totalAmount: 31,
          orderNote: '请从北门进入'
        }
      ]
    }
  },
  computed: {
    filteredOrders() {
      let result = this.orders

      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        result = result.filter(order => 
          order.orderNumber.toLowerCase().includes(keyword) ||
          order.storeName.toLowerCase().includes(keyword) ||
          order.customerName.toLowerCase().includes(keyword) ||
          order.customerPhone.includes(keyword)
        )
      }

      if (this.statusFilter) {
        result = result.filter(order => order.status === this.statusFilter)
      }

      if (this.storeFilter) {
        result = result.filter(order => order.storeId === parseInt(this.storeFilter))
      }

      return result
    },

    totalItems() {
      return this.filteredOrders.length
    },

    totalPages() {
      return Math.ceil(this.totalItems / this.pageSize)
    },

    visiblePages() {
      const pages = []
      const start = Math.max(1, this.currentPage - 2)
      const end = Math.min(this.totalPages, this.currentPage + 2)
      
      for (let i = start; i <= end; i++) {
        pages.push(i)
      }
      return pages
    },

    pendingCount() {
      return this.orders.filter(order => order.status === 'PENDING').length
    },

    processingCount() {
      return this.orders.filter(order => ['CONFIRMED', 'PREPARING', 'DELIVERING'].includes(order.status)).length
    },

    completedCount() {
      return this.orders.filter(order => order.status === 'COMPLETED').length
    },

    orderStatusHistory() {
      if (!this.viewingOrder) return []

      const statusFlow = [
        { key: 'PENDING', title: '待确认', icon: 'fas fa-clock' },
        { key: 'CONFIRMED', title: '已确认', icon: 'fas fa-check' },
        { key: 'PREPARING', title: '制作中', icon: 'fas fa-utensils' },
        { key: 'DELIVERING', title: '配送中', icon: 'fas fa-shipping-fast' },
        { key: 'COMPLETED', title: '已完成', icon: 'fas fa-check-circle' }
      ]

      const currentIndex = statusFlow.findIndex(s => s.key === this.viewingOrder.status)
      
      return statusFlow.map((status, index) => ({
        ...status,
        completed: index < currentIndex,
        current: index === currentIndex,
        time: index <= currentIndex ? this.formatTime(this.viewingOrder.createTime) : ''
      }))
    }
  },
  methods: {
    handleSearch() {
      this.currentPage = 1
    },

    handleFilter() {
      this.currentPage = 1
    },

    handleSelectAll() {
      if (this.selectAll) {
        this.selectedOrders = this.filteredOrders.map(order => order.id)
      } else {
        this.selectedOrders = []
      }
    },

    isUrgent(order) {
      // 超过预计送达时间或者下单超过1小时未处理
      const now = new Date()
      const orderTime = new Date(order.createTime)
      const estimatedTime = new Date(order.estimatedDeliveryTime)
      
      return (now > estimatedTime && order.status !== 'COMPLETED') ||
             (now - orderTime > 3600000 && order.status === 'PENDING')
    },

    getStatusText(status) {
      const statusMap = {
        'PENDING': '待确认',
        'CONFIRMED': '已确认',
        'PREPARING': '制作中',
        'DELIVERING': '配送中',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    },

    formatTime(time) {
      return new Date(time).toLocaleString()
    },

    formatDeliveryTime(time) {
      return new Date(time).toLocaleTimeString()
    },

    confirmOrder(order) {
      order.status = 'CONFIRMED'
      this.$message.success(`订单 ${order.orderNumber} 已确认`)
    },

    startPreparing(order) {
      order.status = 'PREPARING'
      this.$message.success(`订单 ${order.orderNumber} 开始制作`)
    },

    startDelivery(order) {
      order.status = 'DELIVERING'
      this.$message.success(`订单 ${order.orderNumber} 开始配送`)
    },

    completeOrder(order) {
      order.status = 'COMPLETED'
      this.$message.success(`订单 ${order.orderNumber} 已完成`)
    },

    cancelOrder(order) {
      this.cancellingOrder = order
      this.cancelReason = ''
      this.cancelNote = ''
    },

    confirmCancel() {
      this.cancellingOrder.status = 'CANCELLED'
      this.$message.success(`订单 ${this.cancellingOrder.orderNumber} 已取消`)
      this.closeCancelDialog()
    },

    closeCancelDialog() {
      this.cancellingOrder = null
      this.cancelReason = ''
      this.cancelNote = ''
    },

    viewOrderDetail(order) {
      this.viewingOrder = order
    },

    closeOrderDetail() {
      this.viewingOrder = null
    },

    bulkUpdateStatus(status) {
      this.selectedOrders.forEach(orderId => {
        const order = this.orders.find(o => o.id === orderId)
        if (order) {
          order.status = status
        }
      })
      this.$message.success(`已批量更新 ${this.selectedOrders.length} 个订单状态`)
      this.selectedOrders = []
      this.selectAll = false
      this.showBulkActionMenu = false
    },

    bulkExport() {
      this.$message.info('批量导出功能开发中')
      this.showBulkActionMenu = false
    },

    exportOrders() {
      this.$message.info('订单导出功能开发中')
    }
  }
}
</script>

<style scoped>
.order-management {
  padding: 24px;
  background: #f8f9fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 16px;
  color: #7f8c8d;
  margin: 0;
}

.header-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  border-radius: 8px;
  min-width: 80px;
}

.stat-item.pending { background: linear-gradient(135deg, #ffeaa7, #fdcb6e); }
.stat-item.processing { background: linear-gradient(135deg, #74b9ff, #0984e3); }
.stat-item.completed { background: linear-gradient(135deg, #55efc4, #00b894); }

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: white;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: white;
  font-weight: 600;
}

.search-filters {
  background: white;
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.search-section {
  margin-bottom: 20px;
}

.search-box {
  position: relative;
  max-width: 400px;
}

.search-box i {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #7f8c8d;
}

.search-input {
  width: 100%;
  padding: 12px 16px 12px 44px;
  border: 2px solid #e1e8ed;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
}

.filters-section {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group label {
  font-size: 14px;
  color: #2c3e50;
  font-weight: 600;
  white-space: nowrap;
}

.filter-select {
  padding: 10px 12px;
  border: 1px solid #e1e8ed;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  min-width: 120px;
}

.export-btn {
  padding: 10px 16px;
  background: linear-gradient(135deg, #27ae60, #2ecc71);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.export-btn:hover {
  transform: translateY(-1px);
}

.orders-container {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.orders-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.bulk-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
}

.bulk-checkbox {
  margin-right: 8px;
}

.bulk-action-btn {
  padding: 8px 12px;
  background: #f8f9fa;
  border: 1px solid #e1e8ed;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.bulk-action-menu {
  position: absolute;
  top: 100%;
  left: 0;
  background: white;
  border: 1px solid #e1e8ed;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  z-index: 100;
  min-width: 160px;
}

.menu-item {
  width: 100%;
  padding: 12px 16px;
  border: none;
  background: none;
  text-align: left;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: background-color 0.2s;
}

.menu-item:hover {
  background: #f8f9fa;
}

.menu-item.danger {
  color: #e74c3c;
}

.view-options {
  display: flex;
  gap: 8px;
}

.view-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #e1e8ed;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.view-btn.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.orders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
  gap: 20px;
}

.order-card {
  border: 2px solid #e1e8ed;
  border-radius: 12px;
  padding: 20px;
  background: white;
  transition: all 0.2s ease;
}

.order-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
}

.order-card.order-urgent {
  border-color: #e74c3c;
  background: #fef5f5;
}

.order-card.order-selected {
  border-color: #667eea;
  background: #f8f9ff;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.order-select {
  margin-right: 12px;
}

.order-number {
  flex: 1;
}

.order-number strong {
  color: #2c3e50;
  font-size: 16px;
}

.order-time {
  display: block;
  font-size: 12px;
  color: #7f8c8d;
  margin-top: 2px;
}

.order-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
}

.status-badge.pending { background: #fff3cd; color: #856404; }
.status-badge.confirmed { background: #d4edda; color: #155724; }
.status-badge.preparing { background: #cce5ff; color: #004085; }
.status-badge.delivering { background: #e2e3e5; color: #383d41; }
.status-badge.completed { background: #d1ecf1; color: #0c5460; }
.status-badge.cancelled { background: #f8d7da; color: #721c24; }

.urgent-badge {
  background: #e74c3c;
  color: white;
  padding: 2px 6px;
  border-radius: 8px;
  font-size: 10px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.order-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.store-info, .delivery-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.store-name, .customer-info, .delivery-address, .delivery-time {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #2c3e50;
}

.store-name i, .customer-info i, .delivery-address i, .delivery-time i {
  width: 16px;
  color: #7f8c8d;
}

.order-items {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 8px;
}

.items-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.items-count {
  font-size: 12px;
  color: #7f8c8d;
  font-weight: 600;
}

.items-preview {
  flex: 1;
  margin-left: 12px;
}

.item-name {
  font-size: 13px;
  color: #2c3e50;
}

.more-items {
  font-size: 12px;
  color: #7f8c8d;
}

.order-amount {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 8px;
}

.amount-breakdown {
  margin-bottom: 8px;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #7f8c8d;
  margin-bottom: 2px;
}

.amount-row.discount {
  color: #e74c3c;
}

.total-amount {
  display: flex;
  justify-content: space-between;
  font-weight: bold;
  color: #2c3e50;
  padding-top: 8px;
  border-top: 1px solid #e1e8ed;
}

.order-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s ease;
}

.action-btn.primary { background: #667eea; color: white; }
.action-btn.warning { background: #f39c12; color: white; }
.action-btn.info { background: #3498db; color: white; }
.action-btn.success { background: #27ae60; color: white; }
.action-btn.danger { background: #e74c3c; color: white; }
.action-btn.secondary { background: #95a5a6; color: white; }

.action-btn:hover {
  transform: translateY(-1px);
}

/* 表格视图样式 */
.orders-table-container {
  overflow-x: auto;
}

.orders-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

.orders-table th,
.orders-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e1e8ed;
}

.orders-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

.orders-table tr:hover {
  background: #f8f9fa;
}

.orders-table tr.row-urgent {
  background: #fef5f5;
}

.orders-table tr.row-selected {
  background: #f8f9ff;
}

.order-number-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.urgent-indicator {
  color: #e74c3c;
}

.customer-cell .phone {
  font-size: 12px;
  color: #7f8c8d;
}

.items-cell {
  position: relative;
  cursor: help;
}

.items-tooltip {
  position: absolute;
  top: 100%;
  left: 0;
  background: #2c3e50;
  color: white;
  padding: 8px;
  border-radius: 6px;
  font-size: 12px;
  z-index: 10;
  opacity: 0;
  visibility: hidden;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.items-cell:hover .items-tooltip {
  opacity: 1;
  visibility: visible;
}

.amount-cell {
  font-weight: 600;
  color: #27ae60;
}

.table-actions {
  display: flex;
  gap: 6px;
}

.table-btn {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: all 0.2s ease;
}

.table-btn.primary { background: #667eea; color: white; }
.table-btn.danger { background: #e74c3c; color: white; }
.table-btn.secondary { background: #95a5a6; color: white; }

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}

.pagination-info {
  font-size: 14px;
  color: #7f8c8d;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #e1e8ed;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-btn:hover:not(:disabled) {
  background: #f8f9fa;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 4px;
}

.page-number {
  width: 32px;
  height: 32px;
  border: 1px solid #e1e8ed;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.page-number.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
}

.modal-content.large {
  max-width: 900px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 24px 0 24px;
  margin-bottom: 24px;
}

.modal-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  color: #7f8c8d;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
}

.close-btn:hover {
  background: #f8f9fa;
}

.modal-body {
  padding: 0 24px 24px 24px;
}

.order-detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #f1f3f4;
}

/* 状态时间轴 */
.status-timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.timeline-item {
  display: flex;
  align-items: center;
  gap: 12px;
  opacity: 0.5;
}

.timeline-item.active,
.timeline-item.current {
  opacity: 1;
}

.timeline-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e1e8ed;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7f8c8d;
}

.timeline-item.active .timeline-dot {
  background: #27ae60;
  color: white;
}

.timeline-item.current .timeline-dot {
  background: #667eea;
  color: white;
}

.timeline-title {
  font-weight: 600;
  color: #2c3e50;
}

.timeline-time {
  font-size: 12px;
  color: #7f8c8d;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item label {
  font-size: 12px;
  color: #7f8c8d;
  font-weight: 600;
}

.detail-item span {
  font-size: 14px;
  color: #2c3e50;
}

/* 商品清单 */
.items-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item-detail-card {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.item-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  background: white;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
}

.item-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.item-specs,
.item-remark {
  font-size: 12px;
  color: #7f8c8d;
}

.item-pricing {
  text-align: right;
}

.unit-price,
.quantity,
.subtotal {
  font-size: 14px;
  color: #2c3e50;
}

.subtotal {
  font-weight: 600;
  color: #27ae60;
}

.amount-details {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
}

.amount-details .amount-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.amount-details .amount-row.total {
  font-weight: bold;
  font-size: 16px;
  color: #2c3e50;
  border-top: 1px solid #e1e8ed;
  padding-top: 8px;
  margin-top: 8px;
}

.order-note {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 8px;
  color: #2c3e50;
}

/* 取消订单表单 */
.cancel-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.form-select,
.form-textarea {
  padding: 12px;
  border: 1px solid #e1e8ed;
  border-radius: 6px;
  font-size: 14px;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.btn-secondary {
  background: #f8f9fa;
  color: #6c757d;
}

.btn-danger {
  background: #e74c3c;
  color: white;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .order-management {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .header-stats {
    justify-content: space-between;
    width: 100%;
  }
  
  .filters-section {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .orders-grid {
    grid-template-columns: 1fr;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .orders-table-container {
    font-size: 12px;
  }
}
</style>