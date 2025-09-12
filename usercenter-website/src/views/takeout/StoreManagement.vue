<template>
  <div class="store-management">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">门店管理</h1>
        <p class="page-subtitle">管理外卖门店信息、营业状态和配送设置</p>
      </div>
      <div class="header-actions">
        <button @click="showAddStoreDialog = true" class="btn btn-primary">
          <i class="fas fa-plus"></i>
          新增门店
        </button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-filters">
      <div class="search-box">
        <i class="fas fa-search"></i>
        <input 
          v-model="searchKeyword" 
          placeholder="搜索门店名称、地址或联系电话"
          @input="handleSearch"
          class="search-input"
        >
      </div>
      <div class="filters">
        <select v-model="statusFilter" @change="handleFilter" class="filter-select">
          <option value="">全部状态</option>
          <option value="OPEN">营业中</option>
          <option value="CLOSED">已关闭</option>
          <option value="BUSY">繁忙</option>
          <option value="MAINTENANCE">维护中</option>
        </select>
        <select v-model="regionFilter" @change="handleFilter" class="filter-select">
          <option value="">全部区域</option>
          <option value="朝阳区">朝阳区</option>
          <option value="海淀区">海淀区</option>
          <option value="西城区">西城区</option>
          <option value="东城区">东城区</option>
          <option value="丰台区">丰台区</option>
        </select>
      </div>
    </div>

    <!-- 门店列表 -->
    <div class="stores-grid">
      <div 
        v-for="store in filteredStores" 
        :key="store.id"
        class="store-card"
        :class="{ 'store-offline': store.status === 'CLOSED' }"
      >
        <div class="store-header">
          <div class="store-avatar">
            <img :src="store.avatar || '/images/default-store.jpg'" :alt="store.name">
          </div>
          <div class="store-basic-info">
            <h3 class="store-name">{{ store.name }}</h3>
            <div class="store-address">
              <i class="fas fa-map-marker-alt"></i>
              {{ store.address }}
            </div>
            <div class="store-contact">
              <i class="fas fa-phone"></i>
              {{ store.phone }}
            </div>
          </div>
          <div class="store-status">
            <div class="status-badge" :class="store.status.toLowerCase()">
              {{ getStatusText(store.status) }}
            </div>
            <div class="store-actions">
              <button @click="editStore(store)" class="action-btn edit">
                <i class="fas fa-edit"></i>
              </button>
              <button @click="toggleStoreStatus(store)" class="action-btn toggle">
                <i :class="store.status === 'OPEN' ? 'fas fa-pause' : 'fas fa-play'"></i>
              </button>
              <button @click="viewStoreDetail(store)" class="action-btn detail">
                <i class="fas fa-eye"></i>
              </button>
            </div>
          </div>
        </div>

        <div class="store-stats">
          <div class="stat-item">
            <div class="stat-label">今日订单</div>
            <div class="stat-value">{{ store.todayOrders }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">今日营收</div>
            <div class="stat-value">¥{{ store.todayRevenue.toLocaleString() }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">平均评分</div>
            <div class="stat-value">
              <span class="rating">{{ store.rating }}</span>
              <div class="stars">
                <i v-for="i in 5" :key="i" :class="i <= store.rating ? 'fas fa-star' : 'far fa-star'"></i>
              </div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-label">配送半径</div>
            <div class="stat-value">{{ store.deliveryRadius }}km</div>
          </div>
        </div>

        <div class="store-footer">
          <div class="business-hours">
            <i class="fas fa-clock"></i>
            营业时间: {{ store.businessHours }}
          </div>
          <div class="last-updated">
            最后更新: {{ formatTime(store.lastUpdateTime) }}
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <button 
        @click="currentPage = Math.max(1, currentPage - 1)"
        :disabled="currentPage === 1"
        class="page-btn"
      >
        上一页
      </button>
      <span class="page-info">
        第 {{ currentPage }} 页，共 {{ totalPages }} 页
      </span>
      <button 
        @click="currentPage = Math.min(totalPages, currentPage + 1)"
        :disabled="currentPage === totalPages"
        class="page-btn"
      >
        下一页
      </button>
    </div>

    <!-- 新增/编辑门店对话框 -->
    <div v-if="showAddStoreDialog || editingStore" class="modal-overlay" @click="closeStoreDialog">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ editingStore ? '编辑门店' : '新增门店' }}</h3>
          <button @click="closeStoreDialog" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="saveStore" class="store-form">
            <div class="form-row">
              <div class="form-group">
                <label>门店名称 <span class="required">*</span></label>
                <input 
                  v-model="storeForm.name" 
                  type="text" 
                  required
                  placeholder="请输入门店名称"
                  class="form-input"
                >
              </div>
              <div class="form-group">
                <label>联系电话 <span class="required">*</span></label>
                <input 
                  v-model="storeForm.phone" 
                  type="tel" 
                  required
                  placeholder="请输入联系电话"
                  class="form-input"
                >
              </div>
            </div>

            <div class="form-group">
              <label>详细地址 <span class="required">*</span></label>
              <input 
                v-model="storeForm.address" 
                type="text" 
                required
                placeholder="请输入门店详细地址"
                class="form-input"
              >
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>经度</label>
                <input 
                  v-model="storeForm.longitude" 
                  type="number" 
                  step="any"
                  placeholder="请输入经度"
                  class="form-input"
                >
              </div>
              <div class="form-group">
                <label>纬度</label>
                <input 
                  v-model="storeForm.latitude" 
                  type="number" 
                  step="any"
                  placeholder="请输入纬度"
                  class="form-input"
                >
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>配送半径 (km)</label>
                <input 
                  v-model="storeForm.deliveryRadius" 
                  type="number" 
                  min="0.5"
                  max="20"
                  step="0.1"
                  placeholder="配送半径"
                  class="form-input"
                >
              </div>
              <div class="form-group">
                <label>配送费 (元)</label>
                <input 
                  v-model="storeForm.deliveryFee" 
                  type="number" 
                  min="0"
                  step="0.5"
                  placeholder="配送费"
                  class="form-input"
                >
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>营业开始时间</label>
                <input 
                  v-model="storeForm.openTime" 
                  type="time"
                  class="form-input"
                >
              </div>
              <div class="form-group">
                <label>营业结束时间</label>
                <input 
                  v-model="storeForm.closeTime" 
                  type="time"
                  class="form-input"
                >
              </div>
            </div>

            <div class="form-group">
              <label>门店状态</label>
              <select v-model="storeForm.status" class="form-select">
                <option value="OPEN">营业中</option>
                <option value="CLOSED">已关闭</option>
                <option value="BUSY">繁忙</option>
                <option value="MAINTENANCE">维护中</option>
              </select>
            </div>

            <div class="form-group">
              <label>门店描述</label>
              <textarea 
                v-model="storeForm.description" 
                rows="3"
                placeholder="门店简介或特色描述"
                class="form-textarea"
              ></textarea>
            </div>

            <div class="form-actions">
              <button type="button" @click="closeStoreDialog" class="btn btn-secondary">
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

    <!-- 门店详情对话框 -->
    <div v-if="viewingStore" class="modal-overlay" @click="closeDetailDialog">
      <div class="modal-content large" @click.stop>
        <div class="modal-header">
          <h3>{{ viewingStore.name }} - 门店详情</h3>
          <button @click="closeDetailDialog" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <div class="modal-body">
          <div class="store-detail-content">
            <div class="detail-section">
              <h4>基本信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>门店名称:</label>
                  <span>{{ viewingStore.name }}</span>
                </div>
                <div class="detail-item">
                  <label>联系电话:</label>
                  <span>{{ viewingStore.phone }}</span>
                </div>
                <div class="detail-item">
                  <label>详细地址:</label>
                  <span>{{ viewingStore.address }}</span>
                </div>
                <div class="detail-item">
                  <label>当前状态:</label>
                  <span class="status-badge" :class="viewingStore.status.toLowerCase()">
                    {{ getStatusText(viewingStore.status) }}
                  </span>
                </div>
              </div>
            </div>

            <div class="detail-section">
              <h4>营业信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>营业时间:</label>
                  <span>{{ viewingStore.businessHours }}</span>
                </div>
                <div class="detail-item">
                  <label>配送半径:</label>
                  <span>{{ viewingStore.deliveryRadius }}km</span>
                </div>
                <div class="detail-item">
                  <label>配送费:</label>
                  <span>¥{{ viewingStore.deliveryFee }}</span>
                </div>
                <div class="detail-item">
                  <label>平均评分:</label>
                  <span class="rating-display">
                    {{ viewingStore.rating }}
                    <div class="stars">
                      <i v-for="i in 5" :key="i" :class="i <= viewingStore.rating ? 'fas fa-star' : 'far fa-star'"></i>
                    </div>
                  </span>
                </div>
              </div>
            </div>

            <div class="detail-section">
              <h4>今日数据</h4>
              <div class="detail-stats">
                <div class="stat-card">
                  <div class="stat-title">订单数量</div>
                  <div class="stat-number">{{ viewingStore.todayOrders }}</div>
                </div>
                <div class="stat-card">
                  <div class="stat-title">营业收入</div>
                  <div class="stat-number">¥{{ viewingStore.todayRevenue.toLocaleString() }}</div>
                </div>
                <div class="stat-card">
                  <div class="stat-title">平均单价</div>
                  <div class="stat-number">
                    ¥{{ viewingStore.todayOrders > 0 ? (viewingStore.todayRevenue / viewingStore.todayOrders).toFixed(2) : 0 }}
                  </div>
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
  name: 'StoreManagement',
  data() {
    return {
      searchKeyword: '',
      statusFilter: '',
      regionFilter: '',
      currentPage: 1,
      pageSize: 12,
      showAddStoreDialog: false,
      editingStore: null,
      viewingStore: null,
      submitting: false,
      
      stores: [
        {
          id: 1,
          name: '川香麻辣烫',
          address: '北京市朝阳区建国门外大街1号',
          phone: '010-12345678',
          status: 'OPEN',
          todayOrders: 45,
          todayRevenue: 3200,
          rating: 4.5,
          deliveryRadius: 3,
          deliveryFee: 3,
          businessHours: '09:00-22:00',
          lastUpdateTime: new Date(),
          avatar: null,
          longitude: 116.4074,
          latitude: 39.9042,
          description: '正宗川味麻辣烫，选料新鲜，口味正宗'
        },
        {
          id: 2,
          name: '老北京炸酱面',
          address: '北京市海淀区中关村大街15号',
          phone: '010-87654321',
          status: 'OPEN',
          todayOrders: 38,
          todayRevenue: 2800,
          rating: 4.3,
          deliveryRadius: 2.5,
          deliveryFee: 2,
          businessHours: '08:00-21:00',
          lastUpdateTime: new Date(),
          avatar: null,
          longitude: 116.3074,
          latitude: 39.9842,
          description: '传承老北京风味，手工制作炸酱面'
        },
        {
          id: 3,
          name: '兰州拉面',
          address: '北京市西城区西单北大街88号',
          phone: '010-11223344',
          status: 'BUSY',
          todayOrders: 42,
          todayRevenue: 2650,
          rating: 4.6,
          deliveryRadius: 4,
          deliveryFee: 4,
          businessHours: '07:00-23:00',
          lastUpdateTime: new Date(),
          avatar: null,
          longitude: 116.3774,
          latitude: 39.9142,
          description: '正宗兰州牛肉拉面，汤清面白，回味无穷'
        },
        {
          id: 4,
          name: '黄焖鸡米饭',
          address: '北京市东城区王府井大街120号',
          phone: '010-55667788',
          status: 'CLOSED',
          todayOrders: 0,
          todayRevenue: 0,
          rating: 4.2,
          deliveryRadius: 3.5,
          deliveryFee: 3.5,
          businessHours: '11:00-20:00',
          lastUpdateTime: new Date(),
          avatar: null,
          longitude: 116.4174,
          latitude: 39.9142,
          description: '经典黄焖鸡米饭，营养丰富，老少皆宜'
        }
      ],

      storeForm: {
        name: '',
        phone: '',
        address: '',
        longitude: null,
        latitude: null,
        deliveryRadius: 3,
        deliveryFee: 3,
        openTime: '09:00',
        closeTime: '22:00',
        status: 'OPEN',
        description: ''
      }
    }
  },
  computed: {
    filteredStores() {
      let result = this.stores

      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        result = result.filter(store => 
          store.name.toLowerCase().includes(keyword) ||
          store.address.toLowerCase().includes(keyword) ||
          store.phone.includes(keyword)
        )
      }

      if (this.statusFilter) {
        result = result.filter(store => store.status === this.statusFilter)
      }

      if (this.regionFilter) {
        result = result.filter(store => store.address.includes(this.regionFilter))
      }

      return result
    },

    totalPages() {
      return Math.ceil(this.filteredStores.length / this.pageSize)
    }
  },
  methods: {
    handleSearch() {
      this.currentPage = 1
    },

    handleFilter() {
      this.currentPage = 1
    },

    getStatusText(status) {
      const statusMap = {
        'OPEN': '营业中',
        'CLOSED': '已关闭',
        'BUSY': '繁忙',
        'MAINTENANCE': '维护中'
      }
      return statusMap[status] || status
    },

    editStore(store) {
      this.editingStore = store
      this.storeForm = {
        name: store.name,
        phone: store.phone,
        address: store.address,
        longitude: store.longitude,
        latitude: store.latitude,
        deliveryRadius: store.deliveryRadius,
        deliveryFee: store.deliveryFee,
        openTime: store.businessHours.split('-')[0],
        closeTime: store.businessHours.split('-')[1],
        status: store.status,
        description: store.description
      }
    },

    toggleStoreStatus(store) {
      const newStatus = store.status === 'OPEN' ? 'CLOSED' : 'OPEN'
      store.status = newStatus
      store.lastUpdateTime = new Date()
      
      this.$message.success(`门店状态已更新为${this.getStatusText(newStatus)}`)
    },

    viewStoreDetail(store) {
      this.viewingStore = store
    },

    closeStoreDialog() {
      this.showAddStoreDialog = false
      this.editingStore = null
      this.resetStoreForm()
    },

    closeDetailDialog() {
      this.viewingStore = null
    },

    resetStoreForm() {
      this.storeForm = {
        name: '',
        phone: '',
        address: '',
        longitude: null,
        latitude: null,
        deliveryRadius: 3,
        deliveryFee: 3,
        openTime: '09:00',
        closeTime: '22:00',
        status: 'OPEN',
        description: ''
      }
    },

    async saveStore() {
      this.submitting = true
      
      try {
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        const businessHours = `${this.storeForm.openTime}-${this.storeForm.closeTime}`
        
        if (this.editingStore) {
          // 更新门店
          Object.assign(this.editingStore, {
            ...this.storeForm,
            businessHours,
            lastUpdateTime: new Date()
          })
          this.$message.success('门店信息更新成功')
        } else {
          // 新增门店
          const newStore = {
            id: Date.now(),
            ...this.storeForm,
            businessHours,
            todayOrders: 0,
            todayRevenue: 0,
            rating: 5.0,
            lastUpdateTime: new Date(),
            avatar: null
          }
          this.stores.unshift(newStore)
          this.$message.success('门店创建成功')
        }
        
        this.closeStoreDialog()
      } catch (error) {
        this.$message.error('操作失败，请重试')
      } finally {
        this.submitting = false
      }
    },

    formatTime(time) {
      return new Date(time).toLocaleString()
    }
  }
}
</script>

<style scoped>
.store-management {
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

.header-actions .btn {
  padding: 12px 24px;
  font-size: 14px;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.search-filters {
  background: white;
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  display: flex;
  gap: 20px;
  align-items: center;
}

.search-box {
  position: relative;
  flex: 1;
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

.filters {
  display: flex;
  gap: 12px;
}

.filter-select {
  padding: 12px 16px;
  border: 2px solid #e1e8ed;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  min-width: 120px;
}

.stores-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.store-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.store-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.12);
}

.store-card.store-offline {
  opacity: 0.7;
  background: #f8f9fa;
}

.store-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
}

.store-avatar {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  overflow: hidden;
  margin-right: 16px;
  background: #f8f9fa;
}

.store-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.store-basic-info {
  flex: 1;
  margin-right: 16px;
}

.store-name {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.store-address, .store-contact {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #7f8c8d;
  margin-bottom: 4px;
}

.store-address i, .store-contact i {
  margin-right: 8px;
  width: 16px;
}

.store-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.status-badge.open { background: #e8f5e8; color: #27ae60; }
.status-badge.closed { background: #f8e8e8; color: #e74c3c; }
.status-badge.busy { background: #fff3e0; color: #f39c12; }
.status-badge.maintenance { background: #e3f2fd; color: #2196f3; }

.store-actions {
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

.action-btn.edit { background: #e3f2fd; color: #2196f3; }
.action-btn.toggle { background: #fff3e0; color: #f39c12; }
.action-btn.detail { background: #f3e5f5; color: #9c27b0; }

.action-btn:hover {
  transform: scale(1.1);
}

.store-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-item {
  text-align: center;
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

.rating {
  margin-right: 8px;
}

.stars {
  display: inline-flex;
  gap: 2px;
}

.stars i {
  font-size: 12px;
  color: #ffc107;
}

.store-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f1f3f4;
  font-size: 12px;
  color: #7f8c8d;
}

.business-hours i {
  margin-right: 6px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #e1e8ed;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.page-btn:hover:not(:disabled) {
  background: #f8f9fa;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #7f8c8d;
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

.store-form {
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

.form-input, .form-select, .form-textarea {
  padding: 12px 16px;
  border: 2px solid #e1e8ed;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-input:focus, .form-select:focus, .form-textarea:focus {
  outline: none;
  border-color: #667eea;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 12px;
}

.btn-secondary {
  background: #f8f9fa;
  color: #6c757d;
  border: 1px solid #e1e8ed;
}

.btn-secondary:hover {
  background: #e9ecef;
}

.store-detail-content {
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

.rating-display {
  display: flex;
  align-items: center;
  gap: 8px;
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
  .store-management {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .search-filters {
    flex-direction: column;
    gap: 16px;
  }
  
  .stores-grid {
    grid-template-columns: 1fr;
  }
  
  .form-row, .detail-grid, .detail-stats {
    grid-template-columns: 1fr;
  }
  
  .store-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>