<template>
  <div class="delivery-management">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">配送管理</h1>
        <p class="page-subtitle">管理配送员、配送路线和实时配送状态监控</p>
      </div>
      <div class="header-actions">
        <button @click="showDeliveryPersonDialog = true" class="btn btn-secondary">
          <i class="fas fa-user-plus"></i>
          新增配送员
        </button>
        <button @click="optimizeRoutes" class="btn btn-primary">
          <i class="fas fa-route"></i>
          路线优化
        </button>
      </div>
    </div>

    <!-- 实时监控面板 -->
    <div class="monitoring-panel">
      <div class="panel-header">
        <h2>实时配送监控</h2>
        <div class="refresh-controls">
          <span class="last-update">最后更新: {{ lastUpdateTime }}</span>
          <button @click="refreshData" class="refresh-btn" :disabled="loading">
            <i class="fas fa-sync-alt" :class="{ spinning: loading }"></i>
          </button>
        </div>
      </div>

      <div class="monitoring-stats">
        <div class="stat-card delivering">
          <div class="stat-icon">
            <i class="fas fa-shipping-fast"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ deliveringCount }}</div>
            <div class="stat-label">配送中</div>
          </div>
        </div>
        
        <div class="stat-card available">
          <div class="stat-icon">
            <i class="fas fa-user-check"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ availableDeliveryPersons }}</div>
            <div class="stat-label">可用配送员</div>
          </div>
        </div>
        
        <div class="stat-card avg-time">
          <div class="stat-icon">
            <i class="fas fa-clock"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ avgDeliveryTime }}分</div>
            <div class="stat-label">平均配送时间</div>
          </div>
        </div>
        
        <div class="stat-card completion-rate">
          <div class="stat-icon">
            <i class="fas fa-chart-line"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ completionRate }}%</div>
            <div class="stat-label">今日完成率</div>
          </div>
        </div>
      </div>

      <!-- 地图视图 -->
      <div class="map-container">
        <div class="map-header">
          <h3>实时配送地图</h3>
          <div class="map-controls">
            <div class="legend">
              <div class="legend-item">
                <span class="legend-dot delivering"></span>
                <span>配送中</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot available"></span>
                <span>待派单</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot offline"></span>
                <span>离线</span>
              </div>
            </div>
          </div>
        </div>
        <div class="map-view">
          <div class="map-placeholder">
            <i class="fas fa-map-marked-alt"></i>
            <p>配送地图视图</p>
            <p class="map-info">显示配送员实时位置和配送路线</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 配送员管理 -->
    <div class="delivery-persons-section">
      <div class="section-header">
        <h2>配送员管理</h2>
        <div class="search-filters">
          <div class="search-box">
            <i class="fas fa-search"></i>
            <input 
              v-model="searchKeyword" 
              placeholder="搜索配送员姓名、电话或工号"
              @input="handleSearch"
              class="search-input"
            >
          </div>
          <select v-model="statusFilter" @change="handleFilter" class="filter-select">
            <option value="">全部状态</option>
            <option value="ONLINE">在线</option>
            <option value="DELIVERING">配送中</option>
            <option value="OFFLINE">离线</option>
            <option value="BREAK">休息中</option>
          </select>
        </div>
      </div>

      <div class="delivery-persons-grid">
        <div 
          v-for="person in filteredDeliveryPersons" 
          :key="person.id"
          class="person-card"
          :class="person.status.toLowerCase()"
        >
          <div class="person-header">
            <div class="person-avatar">
              <img :src="person.avatar || '/images/default-avatar.jpg'" :alt="person.name">
              <div class="status-indicator" :class="person.status.toLowerCase()"></div>
            </div>
            <div class="person-info">
              <h4 class="person-name">{{ person.name }}</h4>
              <div class="person-code">工号: {{ person.employeeCode }}</div>
              <div class="person-phone">{{ person.phone }}</div>
            </div>
            <div class="person-actions">
              <button @click="editDeliveryPerson(person)" class="action-btn edit">
                <i class="fas fa-edit"></i>
              </button>
              <button @click="viewPersonDetail(person)" class="action-btn detail">
                <i class="fas fa-eye"></i>
              </button>
            </div>
          </div>

          <div class="person-stats">
            <div class="stat-item">
              <div class="stat-label">今日订单</div>
              <div class="stat-value">{{ person.todayOrders }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">总收入</div>
              <div class="stat-value">¥{{ person.todayIncome }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">评分</div>
              <div class="stat-value rating">
                {{ person.rating }}
                <div class="rating-stars">
                  <i v-for="i in 5" :key="i" :class="i <= person.rating ? 'fas fa-star' : 'far fa-star'"></i>
                </div>
              </div>
            </div>
          </div>

          <div class="person-current-delivery" v-if="person.currentDelivery">
            <div class="delivery-header">
              <span class="delivery-label">当前配送:</span>
              <span class="order-number">{{ person.currentDelivery.orderNumber }}</span>
            </div>
            <div class="delivery-info">
              <div class="delivery-from">
                <i class="fas fa-store"></i>
                {{ person.currentDelivery.storeName }}
              </div>
              <div class="delivery-to">
                <i class="fas fa-map-marker-alt"></i>
                {{ person.currentDelivery.customerAddress }}
              </div>
              <div class="delivery-time">
                <i class="fas fa-clock"></i>
                预计 {{ person.currentDelivery.estimatedTime }} 分钟送达
              </div>
            </div>
            <div class="delivery-actions">
              <button @click="trackOrder(person.currentDelivery)" class="track-btn">
                <i class="fas fa-map-marker-alt"></i>
                实时跟踪
              </button>
              <button @click="contactCustomer(person.currentDelivery)" class="contact-btn">
                <i class="fas fa-phone"></i>
                联系客户
              </button>
            </div>
          </div>

          <div class="person-status-actions">
            <button 
              v-if="person.status === 'OFFLINE'" 
              @click="togglePersonStatus(person, 'ONLINE')"
              class="status-btn online"
            >
              <i class="fas fa-play"></i>
              上线
            </button>
            <button 
              v-if="person.status === 'ONLINE'" 
              @click="togglePersonStatus(person, 'OFFLINE')"
              class="status-btn offline"
            >
              <i class="fas fa-pause"></i>
              下线
            </button>
            <button 
              v-if="person.status === 'ONLINE'" 
              @click="assignOrder(person)"
              class="status-btn assign"
            >
              <i class="fas fa-clipboard-list"></i>
              派单
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 配送订单列表 -->
    <div class="delivery-orders-section">
      <div class="section-header">
        <h2>配送订单</h2>
        <div class="filters">
          <select v-model="orderStatusFilter" @change="handleOrderFilter" class="filter-select">
            <option value="">全部状态</option>
            <option value="PENDING_ASSIGN">待分配</option>
            <option value="ASSIGNED">已分配</option>
            <option value="PICKING_UP">取餐中</option>
            <option value="DELIVERING">配送中</option>
            <option value="COMPLETED">已完成</option>
          </select>
          <select v-model="timeRangeFilter" @change="handleOrderFilter" class="filter-select">
            <option value="today">今天</option>
            <option value="yesterday">昨天</option>
            <option value="week">本周</option>
          </select>
        </div>
      </div>

      <div class="orders-table-container">
        <table class="orders-table">
          <thead>
            <tr>
              <th>订单号</th>
              <th>门店</th>
              <th>客户地址</th>
              <th>配送员</th>
              <th>状态</th>
              <th>下单时间</th>
              <th>预计送达</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="order in filteredDeliveryOrders" 
              :key="order.id"
              :class="{ urgent: isOrderUrgent(order) }"
            >
              <td>
                <div class="order-number-cell">
                  {{ order.orderNumber }}
                  <span v-if="isOrderUrgent(order)" class="urgent-badge">
                    <i class="fas fa-exclamation-triangle"></i>
                  </span>
                </div>
              </td>
              <td>{{ order.storeName }}</td>
              <td class="address-cell">{{ order.customerAddress }}</td>
              <td>
                <div v-if="order.deliveryPersonName" class="delivery-person-cell">
                  <span class="person-name">{{ order.deliveryPersonName }}</span>
                  <span class="person-phone">{{ order.deliveryPersonPhone }}</span>
                </div>
                <span v-else class="unassigned">未分配</span>
              </td>
              <td>
                <div class="status-badge" :class="order.deliveryStatus.toLowerCase()">
                  {{ getDeliveryStatusText(order.deliveryStatus) }}
                </div>
              </td>
              <td>{{ formatTime(order.orderTime) }}</td>
              <td>{{ formatTime(order.estimatedDeliveryTime) }}</td>
              <td>
                <div class="order-actions">
                  <button 
                    v-if="order.deliveryStatus === 'PENDING_ASSIGN'" 
                    @click="assignOrderToPerson(order)"
                    class="assign-order-btn"
                    title="分配配送员"
                  >
                    <i class="fas fa-user-plus"></i>
                  </button>
                  <button @click="trackOrderDetail(order)" class="track-order-btn" title="跟踪订单">
                    <i class="fas fa-map-marker-alt"></i>
                  </button>
                  <button @click="viewOrderDetail(order)" class="view-order-btn" title="查看详情">
                    <i class="fas fa-eye"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 新增/编辑配送员对话框 -->
    <div v-if="showDeliveryPersonDialog || editingPerson" class="modal-overlay" @click="closePersonDialog">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ editingPerson ? '编辑配送员' : '新增配送员' }}</h3>
          <button @click="closePersonDialog" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="saveDeliveryPerson" class="person-form">
            <div class="form-row">
              <div class="form-group">
                <label>姓名 <span class="required">*</span></label>
                <input 
                  v-model="personForm.name" 
                  type="text" 
                  required
                  placeholder="请输入配送员姓名"
                  class="form-input"
                >
              </div>
              <div class="form-group">
                <label>工号 <span class="required">*</span></label>
                <input 
                  v-model="personForm.employeeCode" 
                  type="text" 
                  required
                  placeholder="请输入工号"
                  class="form-input"
                >
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>手机号 <span class="required">*</span></label>
                <input 
                  v-model="personForm.phone" 
                  type="tel" 
                  required
                  placeholder="请输入手机号"
                  class="form-input"
                >
              </div>
              <div class="form-group">
                <label>身份证号 <span class="required">*</span></label>
                <input 
                  v-model="personForm.idNumber" 
                  type="text" 
                  required
                  placeholder="请输入身份证号"
                  class="form-input"
                >
              </div>
            </div>

            <div class="form-group">
              <label>配送区域</label>
              <select v-model="personForm.deliveryArea" class="form-select">
                <option value="">请选择配送区域</option>
                <option value="朝阳区">朝阳区</option>
                <option value="海淀区">海淀区</option>
                <option value="西城区">西城区</option>
                <option value="东城区">东城区</option>
                <option value="丰台区">丰台区</option>
              </select>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>配送工具</label>
                <select v-model="personForm.vehicleType" class="form-select">
                  <option value="ELECTRIC_BIKE">电动车</option>
                  <option value="MOTORCYCLE">摩托车</option>
                  <option value="BICYCLE">自行车</option>
                </select>
              </div>
              <div class="form-group">
                <label>车牌号</label>
                <input 
                  v-model="personForm.vehiclePlate" 
                  type="text" 
                  placeholder="请输入车牌号"
                  class="form-input"
                >
              </div>
            </div>

            <div class="form-group">
              <label>紧急联系人</label>
              <input 
                v-model="personForm.emergencyContact" 
                type="text" 
                placeholder="紧急联系人姓名和电话"
                class="form-input"
              >
            </div>

            <div class="form-actions">
              <button type="button" @click="closePersonDialog" class="btn btn-secondary">
                取消
              </button>
              <button type="submit" class="btn btn-primary" :disabled="submitting">
                {{ submitting ? '保存中...' : '保存' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 配送员详情对话框 -->
    <div v-if="viewingPerson" class="modal-overlay" @click="closePersonDetail">
      <div class="modal-content large" @click.stop>
        <div class="modal-header">
          <h3>{{ viewingPerson.name }} - 配送员详情</h3>
          <button @click="closePersonDetail" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <div class="modal-body">
          <div class="person-detail-content">
            <div class="detail-section">
              <h4>基本信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>姓名:</label>
                  <span>{{ viewingPerson.name }}</span>
                </div>
                <div class="detail-item">
                  <label>工号:</label>
                  <span>{{ viewingPerson.employeeCode }}</span>
                </div>
                <div class="detail-item">
                  <label>手机号:</label>
                  <span>{{ viewingPerson.phone }}</span>
                </div>
                <div class="detail-item">
                  <label>配送区域:</label>
                  <span>{{ viewingPerson.deliveryArea }}</span>
                </div>
              </div>
            </div>

            <div class="detail-section">
              <h4>今日数据</h4>
              <div class="detail-stats">
                <div class="stat-card">
                  <div class="stat-title">配送订单</div>
                  <div class="stat-number">{{ viewingPerson.todayOrders }}</div>
                </div>
                <div class="stat-card">
                  <div class="stat-title">配送收入</div>
                  <div class="stat-number">¥{{ viewingPerson.todayIncome }}</div>
                </div>
                <div class="stat-card">
                  <div class="stat-title">客户评分</div>
                  <div class="stat-number">{{ viewingPerson.rating }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DeliveryManagement',
  data() {
    return {
      loading: false,
      lastUpdateTime: new Date().toLocaleTimeString(),
      searchKeyword: '',
      statusFilter: '',
      orderStatusFilter: '',
      timeRangeFilter: 'today',
      showDeliveryPersonDialog: false,
      editingPerson: null,
      viewingPerson: null,
      submitting: false,

      // 统计数据
      deliveringCount: 8,
      availableDeliveryPersons: 12,
      avgDeliveryTime: 28,
      completionRate: 96.5,

      deliveryPersons: [
        {
          id: 1,
          name: '张师傅',
          employeeCode: 'DL001',
          phone: '138****1234',
          status: 'DELIVERING',
          todayOrders: 15,
          todayIncome: 285,
          rating: 4.8,
          avatar: null,
          deliveryArea: '朝阳区',
          vehicleType: 'ELECTRIC_BIKE',
          vehiclePlate: '京A12345',
          currentDelivery: {
            orderNumber: 'TO202501090001',
            storeName: '川香麻辣烫',
            customerAddress: '朝阳区建国门外大街1号',
            estimatedTime: 12
          }
        },
        {
          id: 2,
          name: '李师傅',
          employeeCode: 'DL002',
          phone: '139****5678',
          status: 'ONLINE',
          todayOrders: 12,
          todayIncome: 228,
          rating: 4.6,
          avatar: null,
          deliveryArea: '海淀区',
          vehicleType: 'MOTORCYCLE',
          vehiclePlate: '京B67890',
          currentDelivery: null
        },
        {
          id: 3,
          name: '王师傅',
          employeeCode: 'DL003',
          phone: '137****9012',
          status: 'OFFLINE',
          todayOrders: 8,
          todayIncome: 152,
          rating: 4.5,
          avatar: null,
          deliveryArea: '西城区',
          vehicleType: 'ELECTRIC_BIKE',
          vehiclePlate: '京C34567',
          currentDelivery: null
        }
      ],

      deliveryOrders: [
        {
          id: 1,
          orderNumber: 'TO202501090001',
          storeName: '川香麻辣烫',
          customerAddress: '朝阳区建国门外大街1号国贸大厦A座2501',
          deliveryPersonId: 1,
          deliveryPersonName: '张师傅',
          deliveryPersonPhone: '138****1234',
          deliveryStatus: 'DELIVERING',
          orderTime: new Date(Date.now() - 1800000),
          estimatedDeliveryTime: new Date(Date.now() + 720000)
        },
        {
          id: 2,
          orderNumber: 'TO202501090002',
          storeName: '老北京炸酱面',
          customerAddress: '海淀区中关村大街15号科技大厦B座1203',
          deliveryPersonId: null,
          deliveryPersonName: '',
          deliveryPersonPhone: '',
          deliveryStatus: 'PENDING_ASSIGN',
          orderTime: new Date(Date.now() - 900000),
          estimatedDeliveryTime: new Date(Date.now() + 1800000)
        },
        {
          id: 3,
          orderNumber: 'TO202501090003',
          storeName: '兰州拉面',
          customerAddress: '西城区西单北大街88号购物中心3层',
          deliveryPersonId: 2,
          deliveryPersonName: '李师傅',
          deliveryPersonPhone: '139****5678',
          deliveryStatus: 'ASSIGNED',
          orderTime: new Date(Date.now() - 1200000),
          estimatedDeliveryTime: new Date(Date.now() + 1200000)
        }
      ],

      personForm: {
        name: '',
        employeeCode: '',
        phone: '',
        idNumber: '',
        deliveryArea: '',
        vehicleType: 'ELECTRIC_BIKE',
        vehiclePlate: '',
        emergencyContact: ''
      }
    }
  },
  computed: {
    filteredDeliveryPersons() {
      let result = this.deliveryPersons

      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        result = result.filter(person => 
          person.name.toLowerCase().includes(keyword) ||
          person.employeeCode.toLowerCase().includes(keyword) ||
          person.phone.includes(keyword)
        )
      }

      if (this.statusFilter) {
        result = result.filter(person => person.status === this.statusFilter)
      }

      return result
    },

    filteredDeliveryOrders() {
      let result = this.deliveryOrders

      if (this.orderStatusFilter) {
        result = result.filter(order => order.deliveryStatus === this.orderStatusFilter)
      }

      return result
    }
  },
  mounted() {
    this.startAutoRefresh()
  },
  beforeUnmount() {
    this.stopAutoRefresh()
  },
  methods: {
    handleSearch() {
      // 搜索逻辑
    },

    handleFilter() {
      // 筛选逻辑
    },

    handleOrderFilter() {
      // 订单筛选逻辑
    },

    refreshData() {
      this.loading = true
      setTimeout(() => {
        this.loading = false
        this.lastUpdateTime = new Date().toLocaleTimeString()
        this.$message.success('数据已刷新')
      }, 1000)
    },

    startAutoRefresh() {
      this.autoRefreshTimer = setInterval(() => {
        this.lastUpdateTime = new Date().toLocaleTimeString()
        // 这里可以添加自动刷新数据的逻辑
      }, 30000)
    },

    stopAutoRefresh() {
      if (this.autoRefreshTimer) {
        clearInterval(this.autoRefreshTimer)
      }
    },

    optimizeRoutes() {
      this.$message.info('路线优化功能开发中')
    },

    editDeliveryPerson(person) {
      this.editingPerson = person
      this.personForm = {
        name: person.name,
        employeeCode: person.employeeCode,
        phone: person.phone,
        idNumber: person.idNumber || '',
        deliveryArea: person.deliveryArea,
        vehicleType: person.vehicleType,
        vehiclePlate: person.vehiclePlate,
        emergencyContact: person.emergencyContact || ''
      }
    },

    viewPersonDetail(person) {
      this.viewingPerson = person
    },

    closePersonDetail() {
      this.viewingPerson = null
    },

    togglePersonStatus(person, newStatus) {
      person.status = newStatus
      this.$message.success(`${person.name} 已${newStatus === 'ONLINE' ? '上线' : '下线'}`)
    },

    assignOrder(person) {
      this.$message.info('派单功能开发中')
    },

    trackOrder(delivery) {
      this.$message.info(`正在跟踪订单 ${delivery.orderNumber}`)
    },

    contactCustomer(delivery) {
      this.$message.info('联系客户功能开发中')
    },

    assignOrderToPerson(order) {
      this.$message.info('分配配送员功能开发中')
    },

    trackOrderDetail(order) {
      this.$message.info(`跟踪订单 ${order.orderNumber}`)
    },

    viewOrderDetail(order) {
      this.$message.info(`查看订单 ${order.orderNumber} 详情`)
    },

    async saveDeliveryPerson() {
      this.submitting = true
      
      try {
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        if (this.editingPerson) {
          Object.assign(this.editingPerson, this.personForm)
          this.$message.success('配送员信息更新成功')
        } else {
          const newPerson = {
            id: Date.now(),
            ...this.personForm,
            status: 'OFFLINE',
            todayOrders: 0,
            todayIncome: 0,
            rating: 5.0,
            avatar: null,
            currentDelivery: null
          }
          this.deliveryPersons.push(newPerson)
          this.$message.success('配送员添加成功')
        }
        
        this.closePersonDialog()
      } catch (error) {
        this.$message.error('操作失败，请重试')
      } finally {
        this.submitting = false
      }
    },

    closePersonDialog() {
      this.showDeliveryPersonDialog = false
      this.editingPerson = null
      this.personForm = {
        name: '',
        employeeCode: '',
        phone: '',
        idNumber: '',
        deliveryArea: '',
        vehicleType: 'ELECTRIC_BIKE',
        vehiclePlate: '',
        emergencyContact: ''
      }
    },

    getDeliveryStatusText(status) {
      const statusMap = {
        'PENDING_ASSIGN': '待分配',
        'ASSIGNED': '已分配',
        'PICKING_UP': '取餐中',
        'DELIVERING': '配送中',
        'COMPLETED': '已完成'
      }
      return statusMap[status] || status
    },

    isOrderUrgent(order) {
      const now = new Date()
      const estimatedTime = new Date(order.estimatedDeliveryTime)
      return now > estimatedTime && order.deliveryStatus !== 'COMPLETED'
    },

    formatTime(time) {
      return new Date(time).toLocaleString()
    }
  }
}
</script>

<style scoped>
.delivery-management {
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

.header-actions {
  display: flex;
  gap: 12px;
}

.btn {
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-secondary {
  background: #f8f9fa;
  color: #6c757d;
  border: 1px solid #e1e8ed;
}

.btn:hover {
  transform: translateY(-1px);
}

/* 实时监控面板 */
.monitoring-panel {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.panel-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.refresh-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.last-update {
  font-size: 14px;
  color: #7f8c8d;
}

.refresh-btn {
  background: none;
  border: none;
  color: #3498db;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.refresh-btn:hover {
  background: #ecf0f1;
}

.refresh-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.monitoring-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border-left: 4px solid;
}

.stat-card.delivering { border-left-color: #3498db; background: linear-gradient(135deg, #74b9ff, #0984e3); }
.stat-card.available { border-left-color: #27ae60; background: linear-gradient(135deg, #55efc4, #00b894); }
.stat-card.avg-time { border-left-color: #f39c12; background: linear-gradient(135deg, #ffeaa7, #fdcb6e); }
.stat-card.completion-rate { border-left-color: #9b59b6; background: linear-gradient(135deg, #fd79a8, #e84393); }

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.stat-content {
  color: white;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

/* 地图容器 */
.map-container {
  border: 1px solid #e1e8ed;
  border-radius: 12px;
  overflow: hidden;
}

.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e1e8ed;
}

.map-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.legend {
  display: flex;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #7f8c8d;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.legend-dot.delivering { background: #3498db; }
.legend-dot.available { background: #27ae60; }
.legend-dot.offline { background: #95a5a6; }

.map-view {
  height: 300px;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.map-placeholder {
  text-align: center;
  color: #7f8c8d;
}

.map-placeholder i {
  font-size: 48px;
  margin-bottom: 16px;
  display: block;
}

.map-info {
  font-size: 14px;
  margin-top: 8px;
}

/* 配送员管理 */
.delivery-persons-section,
.delivery-orders-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.search-filters,
.filters {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-box {
  position: relative;
}

.search-box i {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #7f8c8d;
}

.search-input {
  padding: 10px 14px 10px 36px;
  border: 1px solid #e1e8ed;
  border-radius: 6px;
  font-size: 14px;
  min-width: 250px;
}

.filter-select {
  padding: 10px 12px;
  border: 1px solid #e1e8ed;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  min-width: 120px;
}

.delivery-persons-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.person-card {
  border: 2px solid #e1e8ed;
  border-radius: 12px;
  padding: 20px;
  background: white;
  transition: all 0.2s ease;
}

.person-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
}

.person-card.online { border-left-color: #27ae60; }
.person-card.delivering { border-left-color: #3498db; }
.person-card.offline { border-left-color: #95a5a6; }

.person-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.person-avatar {
  position: relative;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  background: #f8f9fa;
}

.person-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid white;
}

.status-indicator.online { background: #27ae60; }
.status-indicator.delivering { background: #3498db; }
.status-indicator.offline { background: #95a5a6; }

.person-info {
  flex: 1;
}

.person-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 4px 0;
}

.person-code,
.person-phone {
  font-size: 14px;
  color: #7f8c8d;
  margin-bottom: 2px;
}

.person-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  transition: all 0.2s ease;
}

.action-btn.edit { background: #e3f2fd; color: #1976d2; }
.action-btn.detail { background: #f3e5f5; color: #7b1fa2; }

.action-btn:hover {
  transform: scale(1.1);
}

.person-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-item {
  text-align: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-label {
  font-size: 12px;
  color: #7f8c8d;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.stat-value.rating {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.rating-stars {
  display: flex;
  gap: 2px;
}

.rating-stars i {
  font-size: 12px;
  color: #ffc107;
}

.person-current-delivery {
  background: #e8f4fd;
  border: 1px solid #3498db;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.delivery-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.delivery-label {
  font-size: 14px;
  font-weight: 600;
  color: #3498db;
}

.order-number {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.delivery-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.delivery-from,
.delivery-to,
.delivery-time {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #2c3e50;
}

.delivery-from i,
.delivery-to i,
.delivery-time i {
  width: 16px;
  color: #3498db;
}

.delivery-actions {
  display: flex;
  gap: 8px;
}

.track-btn,
.contact-btn {
  flex: 1;
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  transition: all 0.2s ease;
}

.track-btn {
  background: #3498db;
  color: white;
}

.contact-btn {
  background: #27ae60;
  color: white;
}

.track-btn:hover,
.contact-btn:hover {
  transform: translateY(-1px);
}

.person-status-actions {
  display: flex;
  gap: 8px;
}

.status-btn {
  flex: 1;
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.status-btn.online {
  background: #27ae60;
  color: white;
}

.status-btn.offline {
  background: #95a5a6;
  color: white;
}

.status-btn.assign {
  background: #3498db;
  color: white;
}

.status-btn:hover {
  transform: translateY(-1px);
}

/* 配送订单表格 */
.orders-table-container {
  overflow-x: auto;
}

.orders-table {
  width: 100%;
  border-collapse: collapse;
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

.orders-table tr.urgent {
  background: #fef5f5;
}

.order-number-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.urgent-badge {
  color: #e74c3c;
  font-size: 12px;
}

.address-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.delivery-person-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.person-name {
  font-weight: 600;
  color: #2c3e50;
}

.person-phone {
  font-size: 12px;
  color: #7f8c8d;
}

.unassigned {
  color: #e74c3c;
  font-style: italic;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.status-badge.pending_assign { background: #fff3cd; color: #856404; }
.status-badge.assigned { background: #cce5ff; color: #004085; }
.status-badge.picking_up { background: #e2e3e5; color: #383d41; }
.status-badge.delivering { background: #d4edda; color: #155724; }
.status-badge.completed { background: #d1ecf1; color: #0c5460; }

.order-actions {
  display: flex;
  gap: 6px;
}

.assign-order-btn,
.track-order-btn,
.view-order-btn {
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

.assign-order-btn { background: #cce5ff; color: #004085; }
.track-order-btn { background: #d4edda; color: #155724; }
.view-order-btn { background: #f3e5f5; color: #7b1fa2; }

.assign-order-btn:hover,
.track-order-btn:hover,
.view-order-btn:hover {
  transform: scale(1.1);
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
  max-width: 800px;
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

.person-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
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

.required {
  color: #e74c3c;
}

.form-input,
.form-select {
  padding: 12px;
  border: 1px solid #e1e8ed;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #667eea;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 12px;
}

.person-detail-content {
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

.detail-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.stat-title {
  font-size: 12px;
  color: #7f8c8d;
  margin-bottom: 8px;
}

.stat-number {
  font-size: 20px;
  font-weight: bold;
  color: #2c3e50;
}

@media (max-width: 768px) {
  .delivery-management {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .monitoring-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .search-filters,
  .filters {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .delivery-persons-grid {
    grid-template-columns: 1fr;
  }
  
  .person-stats {
    grid-template-columns: 1fr;
  }
  
  .form-row,
  .detail-grid,
  .detail-stats {
    grid-template-columns: 1fr;
  }
  
  .orders-table-container {
    font-size: 12px;
  }
}
</style>