<template>
  <div class="takeout-dashboard">
    <div class="content-header">
      <h1 class="title">外卖管理</h1>
      <p class="subtitle">全面掌控外卖业务数据和运营状况</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card revenue">
        <div class="stat-icon">
          <i class="fas fa-dollar-sign"></i>
        </div>
        <div class="stat-content">
          <div class="stat-label">今日营收</div>
          <div class="stat-value">¥{{ todayRevenue.toLocaleString() }}</div>
          <div class="stat-change" :class="{ positive: revenueChange > 0, negative: revenueChange < 0 }">
            <i :class="revenueChange > 0 ? 'fas fa-arrow-up' : 'fas fa-arrow-down'"></i>
            {{ Math.abs(revenueChange) }}%
          </div>
        </div>
      </div>

      <div class="stat-card orders">
        <div class="stat-icon">
          <i class="fas fa-shopping-cart"></i>
        </div>
        <div class="stat-content">
          <div class="stat-label">今日订单</div>
          <div class="stat-value">{{ todayOrders }}</div>
          <div class="stat-change" :class="{ positive: ordersChange > 0, negative: ordersChange < 0 }">
            <i :class="ordersChange > 0 ? 'fas fa-arrow-up' : 'fas fa-arrow-down'"></i>
            {{ Math.abs(ordersChange) }}%
          </div>
        </div>
      </div>

      <div class="stat-card stores">
        <div class="stat-icon">
          <i class="fas fa-store"></i>
        </div>
        <div class="stat-content">
          <div class="stat-label">活跃门店</div>
          <div class="stat-value">{{ activeStores }}</div>
          <div class="stat-change">
            <span>总共 {{ totalStores }} 家</span>
          </div>
        </div>
      </div>

      <div class="stat-card delivery">
        <div class="stat-icon">
          <i class="fas fa-motorcycle"></i>
        </div>
        <div class="stat-content">
          <div class="stat-label">配送中订单</div>
          <div class="stat-value">{{ deliveringOrders }}</div>
          <div class="stat-change">
            <span>平均配送时间 {{ avgDeliveryTime }}分钟</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-row">
        <!-- 订单趋势图 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>订单趋势</h3>
            <div class="chart-controls">
              <select v-model="orderTrendPeriod" @change="loadOrderTrend" class="period-select">
                <option value="7">近7天</option>
                <option value="30">近30天</option>
                <option value="90">近90天</option>
              </select>
            </div>
          </div>
          <div class="chart-content">
            <canvas ref="orderTrendChart" width="400" height="200"></canvas>
          </div>
        </div>

        <!-- 营收分析图 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>营收分析</h3>
            <div class="chart-controls">
              <select v-model="revenuePeriod" @change="loadRevenueData" class="period-select">
                <option value="7">近7天</option>
                <option value="30">近30天</option>
                <option value="90">近90天</option>
              </select>
            </div>
          </div>
          <div class="chart-content">
            <canvas ref="revenueChart" width="400" height="200"></canvas>
          </div>
        </div>
      </div>

      <div class="chart-row">
        <!-- 门店排行 -->
        <div class="chart-card ranking">
          <div class="chart-header">
            <h3>门店营收排行</h3>
            <div class="chart-controls">
              <select v-model="rankingPeriod" @change="loadStoreRanking" class="period-select">
                <option value="today">今日</option>
                <option value="week">本周</option>
                <option value="month">本月</option>
              </select>
            </div>
          </div>
          <div class="ranking-list">
            <div 
              v-for="(store, index) in storeRanking" 
              :key="store.id"
              class="ranking-item"
              :class="{ top3: index < 3 }"
            >
              <div class="rank-number" :class="`rank-${index + 1}`">
                {{ index + 1 }}
              </div>
              <div class="store-info">
                <div class="store-name">{{ store.name }}</div>
                <div class="store-location">{{ store.location }}</div>
              </div>
              <div class="store-stats">
                <div class="revenue">¥{{ store.revenue.toLocaleString() }}</div>
                <div class="orders">{{ store.orders }}单</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 热销商品 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>热销商品</h3>
            <div class="chart-controls">
              <select v-model="hotItemsPeriod" @change="loadHotItems" class="period-select">
                <option value="today">今日</option>
                <option value="week">本周</option>
                <option value="month">本月</option>
              </select>
            </div>
          </div>
          <div class="hot-items-list">
            <div 
              v-for="(item, index) in hotItems" 
              :key="item.id"
              class="hot-item"
            >
              <div class="item-image">
                <img :src="item.image || '/images/default-food.jpg'" :alt="item.name">
              </div>
              <div class="item-info">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-store">{{ item.storeName }}</div>
              </div>
              <div class="item-stats">
                <div class="sales">售出 {{ item.sales }}</div>
                <div class="revenue">¥{{ item.revenue }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 实时监控 -->
    <div class="realtime-section">
      <div class="section-header">
        <h3>实时监控</h3>
        <div class="refresh-time">
          最后更新: {{ lastUpdateTime }}
          <button @click="refreshData" class="refresh-btn" :disabled="loading">
            <i class="fas fa-sync-alt" :class="{ spinning: loading }"></i>
          </button>
        </div>
      </div>

      <div class="monitoring-grid">
        <!-- 待处理订单 -->
        <div class="monitor-card urgent">
          <div class="monitor-header">
            <h4>待处理订单</h4>
            <div class="count">{{ pendingOrders.length }}</div>
          </div>
          <div class="monitor-list">
            <div 
              v-for="order in pendingOrders.slice(0, 5)" 
              :key="order.id"
              class="monitor-item"
              @click="goToOrder(order.id)"
            >
              <div class="order-info">
                <div class="order-number">{{ order.orderNumber }}</div>
                <div class="order-time">{{ formatTime(order.createTime) }}</div>
              </div>
              <div class="order-status" :class="order.status.toLowerCase()">
                {{ order.statusText }}
              </div>
            </div>
            <div v-if="pendingOrders.length === 0" class="empty-state">
              <i class="fas fa-check-circle"></i>
              <span>暂无待处理订单</span>
            </div>
          </div>
        </div>

        <!-- 配送异常 -->
        <div class="monitor-card warning">
          <div class="monitor-header">
            <h4>配送异常</h4>
            <div class="count">{{ deliveryIssues.length }}</div>
          </div>
          <div class="monitor-list">
            <div 
              v-for="issue in deliveryIssues.slice(0, 5)" 
              :key="issue.id"
              class="monitor-item"
              @click="handleDeliveryIssue(issue)"
            >
              <div class="issue-info">
                <div class="order-number">{{ issue.orderNumber }}</div>
                <div class="issue-type">{{ issue.issueType }}</div>
              </div>
              <div class="issue-duration">
                {{ issue.duration }}分钟
              </div>
            </div>
            <div v-if="deliveryIssues.length === 0" class="empty-state">
              <i class="fas fa-shipping-fast"></i>
              <span>配送正常</span>
            </div>
          </div>
        </div>

        <!-- 门店状态 -->
        <div class="monitor-card info">
          <div class="monitor-header">
            <h4>门店状态</h4>
            <div class="count">{{ storeStatuses.length }}</div>
          </div>
          <div class="monitor-list">
            <div 
              v-for="store in storeStatuses.slice(0, 5)" 
              :key="store.id"
              class="monitor-item"
              @click="goToStore(store.id)"
            >
              <div class="store-info">
                <div class="store-name">{{ store.name }}</div>
                <div class="store-status" :class="store.status.toLowerCase()">
                  {{ store.statusText }}
                </div>
              </div>
              <div class="store-metrics">
                <div class="metric">{{ store.todayOrders }}单</div>
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
  name: 'TakeoutDashboard',
  data() {
    return {
      loading: false,
      lastUpdateTime: new Date().toLocaleTimeString(),
      
      // 统计数据
      todayRevenue: 25680,
      revenueChange: 12.5,
      todayOrders: 156,
      ordersChange: -3.2,
      activeStores: 28,
      totalStores: 32,
      deliveringOrders: 15,
      avgDeliveryTime: 28,

      // 图表周期
      orderTrendPeriod: '7',
      revenuePeriod: '7',
      rankingPeriod: 'today',
      hotItemsPeriod: 'today',

      // 门店排行
      storeRanking: [
        { id: 1, name: '川香麻辣烫', location: '朝阳区', revenue: 3200, orders: 45 },
        { id: 2, name: '老北京炸酱面', location: '海淀区', revenue: 2800, orders: 38 },
        { id: 3, name: '兰州拉面', location: '西城区', revenue: 2650, orders: 42 },
        { id: 4, name: '黄焖鸡米饭', location: '东城区', revenue: 2400, orders: 35 },
        { id: 5, name: '沙县小吃', location: '丰台区', revenue: 2200, orders: 31 }
      ],

      // 热销商品
      hotItems: [
        { id: 1, name: '麻辣香锅', storeName: '川香麻辣烫', sales: 25, revenue: 625, image: null },
        { id: 2, name: '炸酱面', storeName: '老北京炸酱面', sales: 22, revenue: 440, image: null },
        { id: 3, name: '牛肉拉面', storeName: '兰州拉面', sales: 20, revenue: 520, image: null },
        { id: 4, name: '黄焖鸡', storeName: '黄焖鸡米饭', sales: 18, revenue: 432, image: null },
        { id: 5, name: '蒸饺', storeName: '沙县小吃', sales: 16, revenue: 288, image: null }
      ],

      // 实时监控数据
      pendingOrders: [
        { id: 1, orderNumber: 'TO202501080001', createTime: new Date(Date.now() - 300000), status: 'PENDING', statusText: '待确认' },
        { id: 2, orderNumber: 'TO202501080002', createTime: new Date(Date.now() - 180000), status: 'PENDING', statusText: '待确认' }
      ],

      deliveryIssues: [
        { id: 1, orderNumber: 'TO202501080015', issueType: '配送超时', duration: 45 },
        { id: 2, orderNumber: 'TO202501080023', issueType: '联系不到客户', duration: 20 }
      ],

      storeStatuses: [
        { id: 1, name: '川香麻辣烫', status: 'BUSY', statusText: '繁忙', todayOrders: 45 },
        { id: 2, name: '老北京炸酱面', status: 'NORMAL', statusText: '正常', todayOrders: 38 },
        { id: 3, name: '兰州拉面', status: 'NORMAL', statusText: '正常', todayOrders: 42 }
      ]
    }
  },
  mounted() {
    this.initCharts()
    this.startAutoRefresh()
  },
  beforeUnmount() {
    this.stopAutoRefresh()
  },
  methods: {
    initCharts() {
      this.initOrderTrendChart()
      this.initRevenueChart()
    },

    initOrderTrendChart() {
      const canvas = this.$refs.orderTrendChart
      if (!canvas) return

      // 这里应该使用图表库如Chart.js或ECharts来绘制图表
      // 简化实现，显示placeholder
      const ctx = canvas.getContext('2d')
      ctx.fillStyle = '#f5f5f5'
      ctx.fillRect(0, 0, canvas.width, canvas.height)
      ctx.fillStyle = '#666'
      ctx.font = '16px Arial'
      ctx.textAlign = 'center'
      ctx.fillText('订单趋势图表', canvas.width / 2, canvas.height / 2)
    },

    initRevenueChart() {
      const canvas = this.$refs.revenueChart
      if (!canvas) return

      const ctx = canvas.getContext('2d')
      ctx.fillStyle = '#f5f5f5'
      ctx.fillRect(0, 0, canvas.width, canvas.height)
      ctx.fillStyle = '#666'
      ctx.font = '16px Arial'
      ctx.textAlign = 'center'
      ctx.fillText('营收分析图表', canvas.width / 2, canvas.height / 2)
    },

    loadOrderTrend() {
      console.log('加载订单趋势数据，周期:', this.orderTrendPeriod)
      this.initOrderTrendChart()
    },

    loadRevenueData() {
      console.log('加载营收数据，周期:', this.revenuePeriod)
      this.initRevenueChart()
    },

    loadStoreRanking() {
      console.log('加载门店排行，周期:', this.rankingPeriod)
    },

    loadHotItems() {
      console.log('加载热销商品，周期:', this.hotItemsPeriod)
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
      }, 30000) // 30秒刷新一次时间显示
    },

    stopAutoRefresh() {
      if (this.autoRefreshTimer) {
        clearInterval(this.autoRefreshTimer)
      }
    },

    formatTime(time) {
      const now = new Date()
      const diff = Math.floor((now - time) / 60000) // 分钟差
      if (diff < 1) return '刚刚'
      if (diff < 60) return `${diff}分钟前`
      return time.toLocaleTimeString()
    },

    goToOrder(orderId) {
      this.$router.push({ name: 'TakeoutOrderDetail', params: { id: orderId } })
    },

    handleDeliveryIssue(issue) {
      this.$router.push({ name: 'TakeoutDeliveryManagement', query: { issue: issue.id } })
    },

    goToStore(storeId) {
      this.$router.push({ name: 'TakeoutStoreDetail', params: { id: storeId } })
    }
  }
}
</script>

<style scoped>
.takeout-dashboard {
  padding: 24px;
  background: #f8f9fa;
  min-height: 100vh;
}

.content-header {
  margin-bottom: 32px;
}

.title {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 16px;
  color: #7f8c8d;
  margin: 0;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  display: flex;
  align-items: center;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.12);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.revenue .stat-icon { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.orders .stat-icon { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stores .stat-icon { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.delivery .stat-icon { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #7f8c8d;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 4px;
}

.stat-change {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-change.positive { color: #27ae60; }
.stat-change.negative { color: #e74c3c; }

/* 图表区域 */
.charts-section {
  margin-bottom: 32px;
}

.chart-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.period-select {
  padding: 8px 12px;
  border: 1px solid #e1e8ed;
  border-radius: 6px;
  font-size: 14px;
  background: white;
}

.chart-content canvas {
  max-width: 100%;
  height: auto;
}

/* 排行榜 */
.chart-card.ranking {
  max-height: 500px;
}

.ranking-list {
  max-height: 360px;
  overflow-y: auto;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f1f3f4;
}

.ranking-item:last-child {
  border-bottom: none;
}

.rank-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: white;
  font-size: 14px;
  margin-right: 12px;
  background: #95a5a6;
}

.rank-1 { background: linear-gradient(135deg, #ffd700, #ffb347); }
.rank-2 { background: linear-gradient(135deg, #c0c0c0, #a8a8a8); }
.rank-3 { background: linear-gradient(135deg, #cd7f32, #b8860b); }

.store-info {
  flex: 1;
  margin-right: 12px;
}

.store-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.store-location {
  font-size: 12px;
  color: #7f8c8d;
}

.store-stats {
  text-align: right;
}

.revenue {
  font-weight: 600;
  color: #27ae60;
  margin-bottom: 2px;
}

.orders {
  font-size: 12px;
  color: #7f8c8d;
}

/* 热销商品 */
.hot-items-list {
  max-height: 360px;
  overflow-y: auto;
}

.hot-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f1f3f4;
}

.hot-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  overflow: hidden;
  margin-right: 12px;
  background: #f8f9fa;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  margin-right: 12px;
}

.item-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.item-store {
  font-size: 12px;
  color: #7f8c8d;
}

.item-stats {
  text-align: right;
}

.sales {
  font-size: 12px;
  color: #7f8c8d;
  margin-bottom: 2px;
}

/* 实时监控 */
.realtime-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.refresh-time {
  display: flex;
  align-items: center;
  gap: 12px;
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

.monitoring-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.monitor-card {
  border-radius: 8px;
  padding: 20px;
  border-left: 4px solid;
}

.monitor-card.urgent {
  background: #fdf2f2;
  border-left-color: #e74c3c;
}

.monitor-card.warning {
  background: #fef9e7;
  border-left-color: #f39c12;
}

.monitor-card.info {
  background: #f0f8ff;
  border-left-color: #3498db;
}

.monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.monitor-header h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.count {
  font-size: 20px;
  font-weight: bold;
  color: #e74c3c;
}

.monitor-list {
  max-height: 200px;
  overflow-y: auto;
}

.monitor-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  cursor: pointer;
  transition: background-color 0.2s;
}

.monitor-item:hover {
  background: rgba(0,0,0,0.02);
}

.monitor-item:last-child {
  border-bottom: none;
}

.order-number, .store-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 2px;
}

.order-time, .issue-type, .store-status {
  font-size: 12px;
  color: #7f8c8d;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #7f8c8d;
}

.empty-state i {
  font-size: 32px;
  margin-bottom: 8px;
  display: block;
}

@media (max-width: 768px) {
  .takeout-dashboard {
    padding: 16px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .chart-row {
    grid-template-columns: 1fr;
  }
  
  .monitoring-grid {
    grid-template-columns: 1fr;
  }
}
</style>