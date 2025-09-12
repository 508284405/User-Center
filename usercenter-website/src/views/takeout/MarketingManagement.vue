<template>
  <div class="marketing-management">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">营销活动</h1>
        <p class="page-subtitle">管理外卖优惠券、促销活动和营销策略</p>
      </div>
      <div class="header-actions">
        <button @click="showCouponDialog = true" class="btn btn-secondary">
          <i class="fas fa-ticket-alt"></i>
          新建优惠券
        </button>
        <button @click="showActivityDialog = true" class="btn btn-primary">
          <i class="fas fa-bullhorn"></i>
          新建活动
        </button>
      </div>
    </div>

    <!-- 营销概览 -->
    <div class="marketing-overview">
      <div class="overview-stats">
        <div class="stat-card active-coupons">
          <div class="stat-icon">
            <i class="fas fa-ticket-alt"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ activeCoupons }}</div>
            <div class="stat-label">进行中的优惠券</div>
            <div class="stat-trend positive">
              <i class="fas fa-arrow-up"></i>
              +12.5%
            </div>
          </div>
        </div>
        
        <div class="stat-card coupon-usage">
          <div class="stat-icon">
            <i class="fas fa-chart-pie"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ couponUsageRate }}%</div>
            <div class="stat-label">优惠券使用率</div>
            <div class="stat-trend positive">
              <i class="fas fa-arrow-up"></i>
              +3.2%
            </div>
          </div>
        </div>
        
        <div class="stat-card marketing-revenue">
          <div class="stat-icon">
            <i class="fas fa-dollar-sign"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">¥{{ marketingRevenue.toLocaleString() }}</div>
            <div class="stat-label">营销带来收入</div>
            <div class="stat-trend positive">
              <i class="fas fa-arrow-up"></i>
              +18.6%
            </div>
          </div>
        </div>
        
        <div class="stat-card new-customers">
          <div class="stat-icon">
            <i class="fas fa-user-plus"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ newCustomers }}</div>
            <div class="stat-label">新增客户</div>
            <div class="stat-trend positive">
              <i class="fas fa-arrow-up"></i>
              +25.8%
            </div>
          </div>
        </div>
      </div>

      <!-- 活动效果图表 -->
      <div class="chart-container">
        <div class="chart-header">
          <h3>营销效果分析</h3>
          <div class="chart-controls">
            <select v-model="chartPeriod" @change="updateChart" class="chart-select">
              <option value="7">近7天</option>
              <option value="30">近30天</option>
              <option value="90">近90天</option>
            </select>
          </div>
        </div>
        <div class="chart-content">
          <canvas ref="marketingChart" width="800" height="300"></canvas>
        </div>
      </div>
    </div>

    <!-- 标签导航 -->
    <div class="content-tabs">
      <div class="tab-header">
        <button 
          v-for="tab in tabs" 
          :key="tab.key"
          @click="activeTab = tab.key"
          :class="{ active: activeTab === tab.key }"
          class="tab-btn"
        >
          <i :class="tab.icon"></i>
          {{ tab.label }}
        </button>
      </div>
    </div>

    <!-- 优惠券管理 -->
    <div v-if="activeTab === 'coupons'" class="coupons-section">
      <div class="section-header">
        <div class="search-filters">
          <div class="search-box">
            <i class="fas fa-search"></i>
            <input 
              v-model="couponSearchKeyword" 
              placeholder="搜索优惠券名称或编码"
              @input="handleCouponSearch"
              class="search-input"
            >
          </div>
          <select v-model="couponStatusFilter" @change="handleCouponFilter" class="filter-select">
            <option value="">全部状态</option>
            <option value="ACTIVE">进行中</option>
            <option value="PAUSED">已暂停</option>
            <option value="EXPIRED">已过期</option>
            <option value="DRAFT">草稿</option>
          </select>
          <select v-model="couponTypeFilter" @change="handleCouponFilter" class="filter-select">
            <option value="">全部类型</option>
            <option value="DISCOUNT">折扣券</option>
            <option value="CASH">满减券</option>
            <option value="DELIVERY">免配送费</option>
            <option value="NEWUSER">新用户券</option>
          </select>
        </div>
      </div>

      <div class="coupons-grid">
        <div 
          v-for="coupon in filteredCoupons" 
          :key="coupon.id"
          class="coupon-card"
          :class="coupon.status.toLowerCase()"
        >
          <div class="coupon-header">
            <div class="coupon-type-badge" :class="coupon.type.toLowerCase()">
              {{ getCouponTypeText(coupon.type) }}
            </div>
            <div class="coupon-status-badge" :class="coupon.status.toLowerCase()">
              {{ getCouponStatusText(coupon.status) }}
            </div>
          </div>

          <div class="coupon-content">
            <div class="coupon-title">{{ coupon.name }}</div>
            <div class="coupon-description">{{ coupon.description }}</div>
            
            <div class="coupon-value">
              <div v-if="coupon.type === 'DISCOUNT'" class="discount-value">
                {{ coupon.discountRate }}折
              </div>
              <div v-else-if="coupon.type === 'CASH'" class="cash-value">
                满¥{{ coupon.minOrderAmount }} 减¥{{ coupon.discountAmount }}
              </div>
              <div v-else-if="coupon.type === 'DELIVERY'" class="delivery-value">
                免配送费
              </div>
            </div>

            <div class="coupon-usage">
              <div class="usage-progress">
                <div class="progress-bar">
                  <div 
                    class="progress-fill" 
                    :style="{ width: coupon.totalQuantity > 0 ? (coupon.usedQuantity / coupon.totalQuantity * 100) + '%' : '0%' }"
                  ></div>
                </div>
                <div class="usage-text">
                  已使用 {{ coupon.usedQuantity }} / {{ coupon.totalQuantity || '∞' }}
                </div>
              </div>
              <div class="usage-rate">
                使用率: {{ coupon.totalQuantity > 0 ? ((coupon.usedQuantity / coupon.totalQuantity) * 100).toFixed(1) : 0 }}%
              </div>
            </div>

            <div class="coupon-validity">
              <div class="validity-item">
                <i class="fas fa-calendar-alt"></i>
                {{ formatDate(coupon.startDate) }} - {{ formatDate(coupon.endDate) }}
              </div>
              <div class="validity-item">
                <i class="fas fa-store"></i>
                {{ coupon.applicableStores.length === 0 ? '全部门店' : `${coupon.applicableStores.length}家门店` }}
              </div>
            </div>
          </div>

          <div class="coupon-actions">
            <button @click="editCoupon(coupon)" class="action-btn edit">
              <i class="fas fa-edit"></i>
              编辑
            </button>
            <button 
              v-if="coupon.status === 'ACTIVE'" 
              @click="pauseCoupon(coupon)"
              class="action-btn pause"
            >
              <i class="fas fa-pause"></i>
              暂停
            </button>
            <button 
              v-if="coupon.status === 'PAUSED'" 
              @click="resumeCoupon(coupon)"
              class="action-btn resume"
            >
              <i class="fas fa-play"></i>
              恢复
            </button>
            <button @click="duplicateCoupon(coupon)" class="action-btn duplicate">
              <i class="fas fa-copy"></i>
              复制
            </button>
            <button @click="viewCouponStats(coupon)" class="action-btn stats">
              <i class="fas fa-chart-line"></i>
              统计
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 营销活动管理 -->
    <div v-if="activeTab === 'activities'" class="activities-section">
      <div class="section-header">
        <div class="search-filters">
          <div class="search-box">
            <i class="fas fa-search"></i>
            <input 
              v-model="activitySearchKeyword" 
              placeholder="搜索活动名称"
              @input="handleActivitySearch"
              class="search-input"
            >
          </div>
          <select v-model="activityStatusFilter" @change="handleActivityFilter" class="filter-select">
            <option value="">全部状态</option>
            <option value="UPCOMING">即将开始</option>
            <option value="ACTIVE">进行中</option>
            <option value="ENDED">已结束</option>
            <option value="CANCELLED">已取消</option>
          </select>
        </div>
      </div>

      <div class="activities-list">
        <div 
          v-for="activity in filteredActivities" 
          :key="activity.id"
          class="activity-card"
          :class="activity.status.toLowerCase()"
        >
          <div class="activity-header">
            <div class="activity-banner">
              <img :src="activity.bannerUrl || '/images/default-activity.jpg'" :alt="activity.name">
              <div class="activity-status-overlay" :class="activity.status.toLowerCase()">
                {{ getActivityStatusText(activity.status) }}
              </div>
            </div>
            <div class="activity-info">
              <h3 class="activity-title">{{ activity.name }}</h3>
              <div class="activity-description">{{ activity.description }}</div>
              <div class="activity-meta">
                <div class="meta-item">
                  <i class="fas fa-calendar"></i>
                  {{ formatDate(activity.startDate) }} - {{ formatDate(activity.endDate) }}
                </div>
                <div class="meta-item">
                  <i class="fas fa-users"></i>
                  参与人数: {{ activity.participantCount }}
                </div>
                <div class="meta-item">
                  <i class="fas fa-dollar-sign"></i>
                  带来收入: ¥{{ activity.revenue.toLocaleString() }}
                </div>
              </div>
            </div>
          </div>

          <div class="activity-stats">
            <div class="stat-item">
              <div class="stat-label">参与门店</div>
              <div class="stat-value">{{ activity.participatingStores }}家</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">订单增长</div>
              <div class="stat-value">+{{ activity.orderGrowth }}%</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">客户满意度</div>
              <div class="stat-value">{{ activity.satisfaction }}/5</div>
            </div>
          </div>

          <div class="activity-actions">
            <button @click="editActivity(activity)" class="action-btn edit">
              <i class="fas fa-edit"></i>
              编辑
            </button>
            <button 
              v-if="activity.status === 'UPCOMING'" 
              @click="startActivity(activity)"
              class="action-btn start"
            >
              <i class="fas fa-play"></i>
              开始
            </button>
            <button 
              v-if="activity.status === 'ACTIVE'" 
              @click="endActivity(activity)"
              class="action-btn end"
            >
              <i class="fas fa-stop"></i>
              结束
            </button>
            <button @click="viewActivityDetail(activity)" class="action-btn detail">
              <i class="fas fa-eye"></i>
              详情
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 营销分析 -->
    <div v-if="activeTab === 'analytics'" class="analytics-section">
      <div class="analytics-grid">
        <!-- 优惠券效果分析 -->
        <div class="analysis-card">
          <div class="card-header">
            <h3>优惠券效果分析</h3>
            <select v-model="analyticsPeriod" class="period-select">
              <option value="7">近7天</option>
              <option value="30">近30天</option>
              <option value="90">近90天</option>
            </select>
          </div>
          <div class="card-content">
            <div class="metrics-grid">
              <div class="metric-item">
                <div class="metric-value">{{ couponMetrics.totalIssued.toLocaleString() }}</div>
                <div class="metric-label">发放总量</div>
              </div>
              <div class="metric-item">
                <div class="metric-value">{{ couponMetrics.totalUsed.toLocaleString() }}</div>
                <div class="metric-label">使用总量</div>
              </div>
              <div class="metric-item">
                <div class="metric-value">{{ couponMetrics.usageRate }}%</div>
                <div class="metric-label">使用率</div>
              </div>
              <div class="metric-item">
                <div class="metric-value">¥{{ couponMetrics.savedAmount.toLocaleString() }}</div>
                <div class="metric-label">用户节省金额</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 热门优惠券排行 -->
        <div class="analysis-card">
          <div class="card-header">
            <h3>热门优惠券排行</h3>
          </div>
          <div class="card-content">
            <div class="ranking-list">
              <div 
                v-for="(item, index) in topCoupons" 
                :key="item.id"
                class="ranking-item"
              >
                <div class="rank-number" :class="`rank-${index + 1}`">
                  {{ index + 1 }}
                </div>
                <div class="item-info">
                  <div class="item-name">{{ item.name }}</div>
                  <div class="item-meta">使用{{ item.usageCount }}次</div>
                </div>
                <div class="item-usage">
                  {{ item.usageRate }}%
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 用户行为分析 -->
        <div class="analysis-card">
          <div class="card-header">
            <h3>用户行为分析</h3>
          </div>
          <div class="card-content">
            <div class="behavior-stats">
              <div class="behavior-item">
                <div class="behavior-icon">
                  <i class="fas fa-eye"></i>
                </div>
                <div class="behavior-data">
                  <div class="behavior-value">{{ userBehavior.viewRate }}%</div>
                  <div class="behavior-label">优惠券查看率</div>
                </div>
              </div>
              <div class="behavior-item">
                <div class="behavior-icon">
                  <i class="fas fa-hand-pointer"></i>
                </div>
                <div class="behavior-data">
                  <div class="behavior-value">{{ userBehavior.collectRate }}%</div>
                  <div class="behavior-label">优惠券领取率</div>
                </div>
              </div>
              <div class="behavior-item">
                <div class="behavior-icon">
                  <i class="fas fa-shopping-cart"></i>
                </div>
                <div class="behavior-data">
                  <div class="behavior-value">{{ userBehavior.useRate }}%</div>
                  <div class="behavior-label">优惠券使用率</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ROI分析 */
        <div class="analysis-card full-width">
          <div class="card-header">
            <h3>营销ROI分析</h3>
          </div>
          <div class="card-content">
            <div class="roi-chart">
              <canvas ref="roiChart" width="600" height="200"></canvas>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建/编辑优惠券对话框 -->
    <div v-if="showCouponDialog || editingCoupon" class="modal-overlay" @click="closeCouponDialog">
      <div class="modal-content large" @click.stop>
        <div class="modal-header">
          <h3>{{ editingCoupon ? '编辑优惠券' : '新建优惠券' }}</h3>
          <button @click="closeCouponDialog" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="saveCoupon" class="coupon-form">
            <div class="form-section">
              <h4>基本信息</h4>
              <div class="form-row">
                <div class="form-group">
                  <label>优惠券名称 <span class="required">*</span></label>
                  <input 
                    v-model="couponForm.name" 
                    type="text" 
                    required
                    placeholder="请输入优惠券名称"
                    class="form-input"
                  >
                </div>
                <div class="form-group">
                  <label>优惠券类型 <span class="required">*</span></label>
                  <select v-model="couponForm.type" required class="form-select">
                    <option value="">请选择类型</option>
                    <option value="DISCOUNT">折扣券</option>
                    <option value="CASH">满减券</option>
                    <option value="DELIVERY">免配送费</option>
                    <option value="NEWUSER">新用户券</option>
                  </select>
                </div>
              </div>

              <div class="form-group">
                <label>优惠券描述</label>
                <textarea 
                  v-model="couponForm.description" 
                  rows="3"
                  placeholder="优惠券使用说明和描述"
                  class="form-textarea"
                ></textarea>
              </div>
            </div>

            <div class="form-section">
              <h4>优惠设置</h4>
              <div v-if="couponForm.type === 'DISCOUNT'" class="form-row">
                <div class="form-group">
                  <label>折扣比例 <span class="required">*</span></label>
                  <input 
                    v-model="couponForm.discountRate" 
                    type="number" 
                    min="1"
                    max="99"
                    step="1"
                    placeholder="例: 8 (表示8折)"
                    class="form-input"
                  >
                </div>
                <div class="form-group">
                  <label>最低消费金额</label>
                  <input 
                    v-model="couponForm.minOrderAmount" 
                    type="number" 
                    min="0"
                    step="0.01"
                    placeholder="最低消费金额"
                    class="form-input"
                  >
                </div>
              </div>

              <div v-if="couponForm.type === 'CASH'" class="form-row">
                <div class="form-group">
                  <label>减免金额 <span class="required">*</span></label>
                  <input 
                    v-model="couponForm.discountAmount" 
                    type="number" 
                    min="0.01"
                    step="0.01"
                    placeholder="减免金额"
                    class="form-input"
                  >
                </div>
                <div class="form-group">
                  <label>最低消费金额 <span class="required">*</span></label>
                  <input 
                    v-model="couponForm.minOrderAmount" 
                    type="number" 
                    min="0"
                    step="0.01"
                    placeholder="最低消费金额"
                    class="form-input"
                  >
                </div>
              </div>
            </div>

            <div class="form-section">
              <h4>发放设置</h4>
              <div class="form-row">
                <div class="form-group">
                  <label>发放数量</label>
                  <input 
                    v-model="couponForm.totalQuantity" 
                    type="number" 
                    min="1"
                    placeholder="留空表示不限数量"
                    class="form-input"
                  >
                </div>
                <div class="form-group">
                  <label>每人限领</label>
                  <input 
                    v-model="couponForm.limitPerUser" 
                    type="number" 
                    min="1"
                    placeholder="每人最多领取数量"
                    class="form-input"
                  >
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>有效期开始 <span class="required">*</span></label>
                  <input 
                    v-model="couponForm.startDate" 
                    type="datetime-local" 
                    required
                    class="form-input"
                  >
                </div>
                <div class="form-group">
                  <label>有效期结束 <span class="required">*</span></label>
                  <input 
                    v-model="couponForm.endDate" 
                    type="datetime-local" 
                    required
                    class="form-input"
                  >
                </div>
              </div>
            </div>

            <div class="form-section">
              <h4>适用范围</h4>
              <div class="form-group">
                <label>适用门店</label>
                <div class="store-selection">
                  <label class="checkbox-label">
                    <input 
                      type="checkbox" 
                      :checked="couponForm.applicableStores.length === 0"
                      @change="toggleAllStores"
                      class="form-checkbox"
                    >
                    全部门店
                  </label>
                  <div v-if="couponForm.applicableStores.length > 0" class="store-list">
                    <label 
                      v-for="store in availableStores" 
                      :key="store.id"
                      class="checkbox-label"
                    >
                      <input 
                        type="checkbox" 
                        :value="store.id"
                        v-model="couponForm.applicableStores"
                        class="form-checkbox"
                      >
                      {{ store.name }}
                    </label>
                  </div>
                </div>
              </div>
            </div>

            <div class="form-actions">
              <button type="button" @click="closeCouponDialog" class="btn btn-secondary">
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

    <!-- 新建/编辑活动对话框 -->
    <div v-if="showActivityDialog || editingActivity" class="modal-overlay" @click="closeActivityDialog">
      <div class="modal-content large" @click.stop>
        <div class="modal-header">
          <h3>{{ editingActivity ? '编辑活动' : '新建活动' }}</h3>
          <button @click="closeActivityDialog" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="saveActivity" class="activity-form">
            <div class="form-section">
              <h4>基本信息</h4>
              <div class="form-group">
                <label>活动名称 <span class="required">*</span></label>
                <input 
                  v-model="activityForm.name" 
                  type="text" 
                  required
                  placeholder="请输入活动名称"
                  class="form-input"
                >
              </div>

              <div class="form-group">
                <label>活动描述</label>
                <textarea 
                  v-model="activityForm.description" 
                  rows="3"
                  placeholder="活动详细描述"
                  class="form-textarea"
                ></textarea>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>开始时间 <span class="required">*</span></label>
                  <input 
                    v-model="activityForm.startDate" 
                    type="datetime-local" 
                    required
                    class="form-input"
                  >
                </div>
                <div class="form-group">
                  <label>结束时间 <span class="required">*</span></label>
                  <input 
                    v-model="activityForm.endDate" 
                    type="datetime-local" 
                    required
                    class="form-input"
                  >
                </div>
              </div>
            </div>

            <div class="form-actions">
              <button type="button" @click="closeActivityDialog" class="btn btn-secondary">
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
  </div>
</template>

<script>
export default {
  name: 'MarketingManagement',
  data() {
    return {
      activeTab: 'coupons',
      chartPeriod: '30',
      analyticsPeriod: '30',
      submitting: false,
      
      // 搜索和筛选
      couponSearchKeyword: '',
      couponStatusFilter: '',
      couponTypeFilter: '',
      activitySearchKeyword: '',
      activityStatusFilter: '',
      
      // 对话框状态
      showCouponDialog: false,
      showActivityDialog: false,
      editingCoupon: null,
      editingActivity: null,

      // 统计数据
      activeCoupons: 15,
      couponUsageRate: 68.5,
      marketingRevenue: 125680,
      newCustomers: 428,

      tabs: [
        { key: 'coupons', label: '优惠券管理', icon: 'fas fa-ticket-alt' },
        { key: 'activities', label: '营销活动', icon: 'fas fa-bullhorn' },
        { key: 'analytics', label: '营销分析', icon: 'fas fa-chart-line' }
      ],

      availableStores: [
        { id: 1, name: '川香麻辣烫' },
        { id: 2, name: '老北京炸酱面' },
        { id: 3, name: '兰州拉面' },
        { id: 4, name: '黄焖鸡米饭' }
      ],

      coupons: [
        {
          id: 1,
          name: '新用户专享8折券',
          description: '新注册用户专享，全场8折优惠',
          type: 'DISCOUNT',
          discountRate: 8,
          minOrderAmount: 30,
          totalQuantity: 1000,
          usedQuantity: 356,
          status: 'ACTIVE',
          startDate: '2025-01-01T00:00',
          endDate: '2025-01-31T23:59',
          applicableStores: []
        },
        {
          id: 2,
          name: '满50减10元',
          description: '满50元减10元，全场通用',
          type: 'CASH',
          discountAmount: 10,
          minOrderAmount: 50,
          totalQuantity: 500,
          usedQuantity: 278,
          status: 'ACTIVE',
          startDate: '2025-01-01T00:00',
          endDate: '2025-01-15T23:59',
          applicableStores: [1, 2]
        },
        {
          id: 3,
          name: '免配送费券',
          description: '免除配送费用，任意金额可用',
          type: 'DELIVERY',
          totalQuantity: 200,
          usedQuantity: 89,
          status: 'PAUSED',
          startDate: '2025-01-10T00:00',
          endDate: '2025-01-20T23:59',
          applicableStores: []
        }
      ],

      activities: [
        {
          id: 1,
          name: '春节大促销',
          description: '春节期间全场满减活动，多重优惠叠加使用',
          status: 'ACTIVE',
          startDate: '2025-01-20T00:00',
          endDate: '2025-02-10T23:59',
          bannerUrl: null,
          participantCount: 1250,
          revenue: 86420,
          participatingStores: 28,
          orderGrowth: 45.6,
          satisfaction: 4.7
        },
        {
          id: 2,
          name: '周末双倍积分',
          description: '周末下单享受双倍积分奖励',
          status: 'UPCOMING',
          startDate: '2025-02-15T00:00',
          endDate: '2025-02-16T23:59',
          bannerUrl: null,
          participantCount: 0,
          revenue: 0,
          participatingStores: 32,
          orderGrowth: 0,
          satisfaction: 0
        }
      ],

      // 分析数据
      couponMetrics: {
        totalIssued: 25680,
        totalUsed: 17890,
        usageRate: 69.7,
        savedAmount: 89450
      },

      topCoupons: [
        { id: 1, name: '新用户专享8折券', usageCount: 356, usageRate: 71.2 },
        { id: 2, name: '满50减10元', usageCount: 278, usageRate: 55.6 },
        { id: 3, name: '免配送费券', usageCount: 89, usageRate: 44.5 }
      ],

      userBehavior: {
        viewRate: 85.2,
        collectRate: 42.8,
        useRate: 68.5
      },

      couponForm: {
        name: '',
        description: '',
        type: '',
        discountRate: null,
        discountAmount: null,
        minOrderAmount: null,
        totalQuantity: null,
        limitPerUser: 1,
        startDate: '',
        endDate: '',
        applicableStores: []
      },

      activityForm: {
        name: '',
        description: '',
        startDate: '',
        endDate: ''
      }
    }
  },
  computed: {
    filteredCoupons() {
      let result = this.coupons

      if (this.couponSearchKeyword) {
        const keyword = this.couponSearchKeyword.toLowerCase()
        result = result.filter(coupon => 
          coupon.name.toLowerCase().includes(keyword)
        )
      }

      if (this.couponStatusFilter) {
        result = result.filter(coupon => coupon.status === this.couponStatusFilter)
      }

      if (this.couponTypeFilter) {
        result = result.filter(coupon => coupon.type === this.couponTypeFilter)
      }

      return result
    },

    filteredActivities() {
      let result = this.activities

      if (this.activitySearchKeyword) {
        const keyword = this.activitySearchKeyword.toLowerCase()
        result = result.filter(activity => 
          activity.name.toLowerCase().includes(keyword)
        )
      }

      if (this.activityStatusFilter) {
        result = result.filter(activity => activity.status === this.activityStatusFilter)
      }

      return result
    }
  },
  mounted() {
    this.initCharts()
  },
  methods: {
    initCharts() {
      this.initMarketingChart()
      this.initROIChart()
    },

    initMarketingChart() {
      const canvas = this.$refs.marketingChart
      if (!canvas) return

      const ctx = canvas.getContext('2d')
      ctx.fillStyle = '#f5f5f5'
      ctx.fillRect(0, 0, canvas.width, canvas.height)
      ctx.fillStyle = '#666'
      ctx.font = '16px Arial'
      ctx.textAlign = 'center'
      ctx.fillText('营销效果分析图表', canvas.width / 2, canvas.height / 2)
    },

    initROIChart() {
      const canvas = this.$refs.roiChart
      if (!canvas) return

      const ctx = canvas.getContext('2d')
      ctx.fillStyle = '#f5f5f5'
      ctx.fillRect(0, 0, canvas.width, canvas.height)
      ctx.fillStyle = '#666'
      ctx.font = '16px Arial'
      ctx.textAlign = 'center'
      ctx.fillText('营销ROI分析图表', canvas.width / 2, canvas.height / 2)
    },

    updateChart() {
      console.log('更新图表，周期:', this.chartPeriod)
      this.initMarketingChart()
    },

    handleCouponSearch() {
      // 优惠券搜索逻辑
    },

    handleCouponFilter() {
      // 优惠券筛选逻辑
    },

    handleActivitySearch() {
      // 活动搜索逻辑
    },

    handleActivityFilter() {
      // 活动筛选逻辑
    },

    getCouponTypeText(type) {
      const typeMap = {
        'DISCOUNT': '折扣券',
        'CASH': '满减券',
        'DELIVERY': '免配送费',
        'NEWUSER': '新用户券'
      }
      return typeMap[type] || type
    },

    getCouponStatusText(status) {
      const statusMap = {
        'ACTIVE': '进行中',
        'PAUSED': '已暂停',
        'EXPIRED': '已过期',
        'DRAFT': '草稿'
      }
      return statusMap[status] || status
    },

    getActivityStatusText(status) {
      const statusMap = {
        'UPCOMING': '即将开始',
        'ACTIVE': '进行中',
        'ENDED': '已结束',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    },

    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString()
    },

    editCoupon(coupon) {
      this.editingCoupon = coupon
      this.couponForm = {
        name: coupon.name,
        description: coupon.description,
        type: coupon.type,
        discountRate: coupon.discountRate,
        discountAmount: coupon.discountAmount,
        minOrderAmount: coupon.minOrderAmount,
        totalQuantity: coupon.totalQuantity,
        limitPerUser: coupon.limitPerUser || 1,
        startDate: coupon.startDate,
        endDate: coupon.endDate,
        applicableStores: [...coupon.applicableStores]
      }
    },

    pauseCoupon(coupon) {
      coupon.status = 'PAUSED'
      this.$message.success(`优惠券 ${coupon.name} 已暂停`)
    },

    resumeCoupon(coupon) {
      coupon.status = 'ACTIVE'
      this.$message.success(`优惠券 ${coupon.name} 已恢复`)
    },

    duplicateCoupon(coupon) {
      const newCoupon = {
        ...coupon,
        id: Date.now(),
        name: `${coupon.name}(副本)`,
        usedQuantity: 0,
        status: 'DRAFT'
      }
      this.coupons.push(newCoupon)
      this.$message.success('优惠券复制成功')
    },

    viewCouponStats(coupon) {
      this.$message.info(`查看优惠券 ${coupon.name} 统计数据`)
    },

    toggleAllStores() {
      if (this.couponForm.applicableStores.length === 0) {
        this.couponForm.applicableStores = this.availableStores.map(store => store.id)
      } else {
        this.couponForm.applicableStores = []
      }
    },

    async saveCoupon() {
      this.submitting = true
      
      try {
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        if (this.editingCoupon) {
          Object.assign(this.editingCoupon, this.couponForm)
          this.$message.success('优惠券更新成功')
        } else {
          const newCoupon = {
            id: Date.now(),
            ...this.couponForm,
            usedQuantity: 0,
            status: 'DRAFT'
          }
          this.coupons.push(newCoupon)
          this.$message.success('优惠券创建成功')
        }
        
        this.closeCouponDialog()
      } catch (error) {
        this.$message.error('操作失败，请重试')
      } finally {
        this.submitting = false
      }
    },

    closeCouponDialog() {
      this.showCouponDialog = false
      this.editingCoupon = null
      this.couponForm = {
        name: '',
        description: '',
        type: '',
        discountRate: null,
        discountAmount: null,
        minOrderAmount: null,
        totalQuantity: null,
        limitPerUser: 1,
        startDate: '',
        endDate: '',
        applicableStores: []
      }
    },

    editActivity(activity) {
      this.editingActivity = activity
      this.activityForm = {
        name: activity.name,
        description: activity.description,
        startDate: activity.startDate,
        endDate: activity.endDate
      }
    },

    startActivity(activity) {
      activity.status = 'ACTIVE'
      this.$message.success(`活动 ${activity.name} 已开始`)
    },

    endActivity(activity) {
      activity.status = 'ENDED'
      this.$message.success(`活动 ${activity.name} 已结束`)
    },

    viewActivityDetail(activity) {
      this.$message.info(`查看活动 ${activity.name} 详细数据`)
    },

    async saveActivity() {
      this.submitting = true
      
      try {
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        if (this.editingActivity) {
          Object.assign(this.editingActivity, this.activityForm)
          this.$message.success('活动更新成功')
        } else {
          const newActivity = {
            id: Date.now(),
            ...this.activityForm,
            status: 'UPCOMING',
            bannerUrl: null,
            participantCount: 0,
            revenue: 0,
            participatingStores: 0,
            orderGrowth: 0,
            satisfaction: 0
          }
          this.activities.push(newActivity)
          this.$message.success('活动创建成功')
        }
        
        this.closeActivityDialog()
      } catch (error) {
        this.$message.error('操作失败，请重试')
      } finally {
        this.submitting = false
      }
    },

    closeActivityDialog() {
      this.showActivityDialog = false
      this.editingActivity = null
      this.activityForm = {
        name: '',
        description: '',
        startDate: '',
        endDate: ''
      }
    }
  }
}
</script>

<style scoped>
.marketing-management {
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

/* 营销概览 */
.marketing-overview {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.overview-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
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
  position: relative;
  overflow: hidden;
}

.stat-card.active-coupons { 
  border-left-color: #3498db; 
  background: linear-gradient(135deg, #74b9ff, #0984e3);
}
.stat-card.coupon-usage { 
  border-left-color: #27ae60; 
  background: linear-gradient(135deg, #55efc4, #00b894);
}
.stat-card.marketing-revenue { 
  border-left-color: #f39c12; 
  background: linear-gradient(135deg, #ffeaa7, #fdcb6e);
}
.stat-card.new-customers { 
  border-left-color: #9b59b6; 
  background: linear-gradient(135deg, #fd79a8, #e84393);
}

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
  flex: 1;
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
  margin-bottom: 4px;
}

.stat-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-trend.positive { color: rgba(255,255,255,0.9); }

.chart-container {
  border: 1px solid #e1e8ed;
  border-radius: 12px;
  overflow: hidden;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e1e8ed;
}

.chart-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.chart-select {
  padding: 6px 10px;
  border: 1px solid #e1e8ed;
  border-radius: 4px;
  font-size: 12px;
  background: white;
}

.chart-content {
  padding: 20px;
  background: white;
}

.chart-content canvas {
  max-width: 100%;
  height: auto;
}

/* 标签导航 */
.content-tabs {
  margin-bottom: 32px;
}

.tab-header {
  display: flex;
  background: white;
  border-radius: 12px;
  padding: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.tab-btn {
  flex: 1;
  padding: 12px 20px;
  border: none;
  background: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  color: #7f8c8d;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.tab-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.tab-btn:hover:not(.active) {
  background: #f8f9fa;
  color: #2c3e50;
}

/* 优惠券和活动区域 */
.coupons-section,
.activities-section,
.analytics-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.section-header {
  margin-bottom: 24px;
}

.search-filters {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
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

/* 优惠券网格 */
.coupons-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.coupon-card {
  border: 2px solid #e1e8ed;
  border-radius: 12px;
  padding: 20px;
  background: white;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

.coupon-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
}

.coupon-card.paused {
  opacity: 0.7;
  background: #f8f9fa;
}

.coupon-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.coupon-type-badge,
.coupon-status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.coupon-type-badge.discount { background: #e3f2fd; color: #1976d2; }
.coupon-type-badge.cash { background: #e8f5e8; color: #2e7d32; }
.coupon-type-badge.delivery { background: #fff3e0; color: #f57c00; }
.coupon-type-badge.newuser { background: #f3e5f5; color: #7b1fa2; }

.coupon-status-badge.active { background: #d4edda; color: #155724; }
.coupon-status-badge.paused { background: #fff3cd; color: #856404; }
.coupon-status-badge.expired { background: #f8d7da; color: #721c24; }
.coupon-status-badge.draft { background: #e2e3e5; color: #383d41; }

.coupon-content {
  margin-bottom: 20px;
}

.coupon-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.coupon-description {
  font-size: 14px;
  color: #7f8c8d;
  margin-bottom: 12px;
}

.coupon-value {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 12px;
  text-align: center;
}

.discount-value,
.cash-value,
.delivery-value {
  font-size: 18px;
  font-weight: bold;
  color: #e74c3c;
}

.coupon-usage {
  margin-bottom: 12px;
}

.usage-progress {
  margin-bottom: 8px;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #e1e8ed;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  transition: width 0.3s ease;
}

.usage-text,
.usage-rate {
  font-size: 12px;
  color: #7f8c8d;
}

.coupon-validity {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.validity-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #7f8c8d;
}

.validity-item i {
  width: 14px;
}

.coupon-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  flex: 1;
  min-width: 80px;
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

.action-btn.edit { background: #e3f2fd; color: #1976d2; }
.action-btn.pause { background: #fff3cd; color: #856404; }
.action-btn.resume { background: #d4edda; color: #155724; }
.action-btn.duplicate { background: #f3e5f5; color: #7b1fa2; }
.action-btn.stats { background: #e8f4fd; color: #1565c0; }

.action-btn:hover {
  transform: translateY(-1px);
}

/* 营销活动列表 */
.activities-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.activity-card {
  border: 2px solid #e1e8ed;
  border-radius: 12px;
  padding: 20px;
  background: white;
  transition: all 0.2s ease;
}

.activity-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
}

.activity-header {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.activity-banner {
  width: 120px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  background: #f8f9fa;
}

.activity-banner img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.activity-status-overlay {
  position: absolute;
  top: 4px;
  right: 4px;
  padding: 2px 6px;
  border-radius: 8px;
  font-size: 10px;
  font-weight: 600;
  color: white;
}

.activity-status-overlay.upcoming { background: #f39c12; }
.activity-status-overlay.active { background: #27ae60; }
.activity-status-overlay.ended { background: #95a5a6; }
.activity-status-overlay.cancelled { background: #e74c3c; }

.activity-info {
  flex: 1;
}

.activity-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.activity-description {
  font-size: 14px;
  color: #7f8c8d;
  margin-bottom: 12px;
}

.activity-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #7f8c8d;
}

.meta-item i {
  width: 14px;
}

.activity-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
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

.activity-actions {
  display: flex;
  gap: 12px;
}

.action-btn.start { background: #d4edda; color: #155724; }
.action-btn.end { background: #fff3cd; color: #856404; }
.action-btn.detail { background: #f3e5f5; color: #7b1fa2; }

/* 营销分析 */
.analytics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.analysis-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e1e8ed;
}

.analysis-card.full-width {
  grid-column: 1 / -1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.period-select {
  padding: 6px 10px;
  border: 1px solid #e1e8ed;
  border-radius: 4px;
  font-size: 12px;
  background: white;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.metric-item {
  background: white;
  padding: 16px;
  border-radius: 8px;
  text-align: center;
}

.metric-value {
  font-size: 20px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 4px;
}

.metric-label {
  font-size: 12px;
  color: #7f8c8d;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: white;
  border-radius: 8px;
}

.rank-number {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: white;
  font-size: 12px;
  background: #95a5a6;
}

.rank-1 { background: linear-gradient(135deg, #ffd700, #ffb347); }
.rank-2 { background: linear-gradient(135deg, #c0c0c0, #a8a8a8); }
.rank-3 { background: linear-gradient(135deg, #cd7f32, #b8860b); }

.item-info {
  flex: 1;
}

.item-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 2px;
}

.item-meta {
  font-size: 12px;
  color: #7f8c8d;
}

.item-usage {
  font-weight: 600;
  color: #27ae60;
}

.behavior-stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.behavior-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: white;
  border-radius: 8px;
}

.behavior-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #667eea;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.behavior-data {
  flex: 1;
}

.behavior-value {
  font-size: 20px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 4px;
}

.behavior-label {
  font-size: 12px;
  color: #7f8c8d;
}

.roi-chart {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.roi-chart canvas {
  max-width: 100%;
  height: auto;
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

.coupon-form,
.activity-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-section {
  border-bottom: 1px solid #e1e8ed;
  padding-bottom: 20px;
}

.form-section:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.form-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 16px 0;
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
.form-select,
.form-textarea {
  padding: 12px;
  border: 1px solid #e1e8ed;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #667eea;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.store-selection {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.store-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-top: 8px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #2c3e50;
  cursor: pointer;
}

.form-checkbox {
  width: 16px;
  height: 16px;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 12px;
}

@media (max-width: 768px) {
  .marketing-management {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .overview-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .search-filters {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .coupons-grid {
    grid-template-columns: 1fr;
  }
  
  .activity-header {
    flex-direction: column;
  }
  
  .activity-stats {
    grid-template-columns: 1fr;
  }
  
  .analytics-grid {
    grid-template-columns: 1fr;
  }
  
  .metrics-grid {
    grid-template-columns: 1fr;
  }
  
  .form-row,
  .store-list {
    grid-template-columns: 1fr;
  }
}
</style>