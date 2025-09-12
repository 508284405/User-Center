<template>
  <div class="menu-management">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">菜单管理</h1>
        <p class="page-subtitle">管理外卖门店菜单、商品分类和价格设置</p>
      </div>
      <div class="header-actions">
        <button @click="showCategoryDialog = true" class="btn btn-secondary">
          <i class="fas fa-folder-plus"></i>
          新增分类
        </button>
        <button @click="showItemDialog = true" class="btn btn-primary">
          <i class="fas fa-plus"></i>
          新增菜品
        </button>
      </div>
    </div>

    <!-- 门店选择和搜索 -->
    <div class="controls-section">
      <div class="store-selector">
        <label>选择门店:</label>
        <select v-model="selectedStoreId" @change="loadMenuData" class="store-select">
          <option value="">请选择门店</option>
          <option v-for="store in stores" :key="store.id" :value="store.id">
            {{ store.name }}
          </option>
        </select>
      </div>
      
      <div class="search-filters">
        <div class="search-box">
          <i class="fas fa-search"></i>
          <input 
            v-model="searchKeyword" 
            placeholder="搜索菜品名称或分类"
            @input="handleSearch"
            class="search-input"
          >
        </div>
        <div class="filter-group">
          <select v-model="statusFilter" @change="handleFilter" class="filter-select">
            <option value="">全部状态</option>
            <option value="AVAILABLE">在售</option>
            <option value="UNAVAILABLE">下架</option>
            <option value="OUT_OF_STOCK">缺货</option>
          </select>
        </div>
        <div class="filter-group">
          <select v-model="categoryFilter" @change="handleFilter" class="filter-select">
            <option value="">全部分类</option>
            <option v-for="category in categories" :key="category.id" :value="category.id">
              {{ category.name }}
            </option>
          </select>
        </div>
      </div>
    </div>

    <!-- 菜单内容 -->
    <div v-if="selectedStoreId" class="menu-content">
      <div class="menu-layout">
        <!-- 分类管理侧边栏 -->
        <div class="categories-sidebar">
          <div class="sidebar-header">
            <h3>菜品分类</h3>
            <button @click="showCategoryDialog = true" class="add-category-btn">
              <i class="fas fa-plus"></i>
            </button>
          </div>
          
          <div class="categories-list">
            <div 
              v-for="category in categories" 
              :key="category.id"
              class="category-item"
              :class="{ active: selectedCategoryId === category.id }"
              @click="selectCategory(category.id)"
            >
              <div class="category-info">
                <div class="category-name">{{ category.name }}</div>
                <div class="category-count">{{ getItemsCountByCategory(category.id) }}个菜品</div>
              </div>
              <div class="category-actions">
                <button @click.stop="editCategory(category)" class="category-action-btn">
                  <i class="fas fa-edit"></i>
                </button>
                <button @click.stop="deleteCategory(category)" class="category-action-btn danger">
                  <i class="fas fa-trash"></i>
                </button>
              </div>
              <div class="category-toggle">
                <label class="toggle-switch">
                  <input 
                    type="checkbox" 
                    :checked="category.enabled"
                    @change="toggleCategory(category)"
                  >
                  <span class="toggle-slider"></span>
                </label>
              </div>
            </div>
          </div>
        </div>

        <!-- 菜品列表主区域 -->
        <div class="items-main">
          <div class="items-header">
            <div class="header-info">
              <h3>{{ getSelectedCategoryName() }}</h3>
              <span class="items-count">共 {{ filteredItems.length }} 个菜品</span>
            </div>
            <div class="header-actions">
              <div class="view-mode">
                <button 
                  @click="viewMode = 'grid'" 
                  :class="{ active: viewMode === 'grid' }"
                  class="view-btn"
                >
                  <i class="fas fa-th-large"></i>
                </button>
                <button 
                  @click="viewMode = 'list'" 
                  :class="{ active: viewMode === 'list' }"
                  class="view-btn"
                >
                  <i class="fas fa-list"></i>
                </button>
              </div>
              <button @click="batchManage = !batchManage" class="batch-btn">
                <i class="fas fa-check-square"></i>
                批量管理
              </button>
            </div>
          </div>

          <!-- 批量操作栏 -->
          <div v-if="batchManage && selectedItems.length > 0" class="batch-actions-bar">
            <div class="selected-info">
              已选择 {{ selectedItems.length }} 个菜品
            </div>
            <div class="batch-buttons">
              <button @click="batchUpdateStatus('AVAILABLE')" class="batch-action-btn success">
                <i class="fas fa-eye"></i>
                批量上架
              </button>
              <button @click="batchUpdateStatus('UNAVAILABLE')" class="batch-action-btn warning">
                <i class="fas fa-eye-slash"></i>
                批量下架
              </button>
              <button @click="batchUpdateCategory" class="batch-action-btn info">
                <i class="fas fa-folder"></i>
                更换分类
              </button>
              <button @click="batchDelete" class="batch-action-btn danger">
                <i class="fas fa-trash"></i>
                批量删除
              </button>
            </div>
          </div>

          <!-- 网格视图 -->
          <div v-if="viewMode === 'grid'" class="items-grid">
            <div 
              v-for="item in filteredItems" 
              :key="item.id"
              class="item-card"
              :class="{ 
                'item-unavailable': item.status === 'UNAVAILABLE',
                'item-selected': selectedItems.includes(item.id)
              }"
            >
              <div class="item-image">
                <img :src="item.imageUrl || '/images/default-food.jpg'" :alt="item.name">
                <div v-if="batchManage" class="item-select">
                  <input 
                    type="checkbox" 
                    :value="item.id" 
                    v-model="selectedItems"
                    class="item-checkbox"
                  >
                </div>
                <div class="status-overlay" :class="item.status.toLowerCase()">
                  {{ getStatusText(item.status) }}
                </div>
              </div>
              
              <div class="item-content">
                <div class="item-header">
                  <h4 class="item-name">{{ item.name }}</h4>
                  <div class="item-price">
                    <span class="current-price">¥{{ item.price }}</span>
                    <span v-if="item.originalPrice && item.originalPrice > item.price" class="original-price">
                      ¥{{ item.originalPrice }}
                    </span>
                  </div>
                </div>
                
                <div class="item-description">
                  {{ item.description || '暂无描述' }}
                </div>
                
                <div class="item-stats">
                  <div class="stat-item">
                    <i class="fas fa-shopping-cart"></i>
                    月销 {{ item.monthlySales }}
                  </div>
                  <div class="stat-item">
                    <i class="fas fa-star"></i>
                    {{ item.rating || '暂无评分' }}
                  </div>
                  <div class="stat-item">
                    <i class="fas fa-box"></i>
                    库存 {{ item.stock || '∞' }}
                  </div>
                </div>

                <div class="item-actions">
                  <button @click="editItem(item)" class="action-btn edit">
                    <i class="fas fa-edit"></i>
                  </button>
                  <button @click="toggleItemStatus(item)" class="action-btn toggle">
                    <i :class="item.status === 'AVAILABLE' ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                  </button>
                  <button @click="duplicateItem(item)" class="action-btn duplicate">
                    <i class="fas fa-copy"></i>
                  </button>
                  <button @click="deleteItem(item)" class="action-btn delete">
                    <i class="fas fa-trash"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 列表视图 -->
          <div v-if="viewMode === 'list'" class="items-table-container">
            <table class="items-table">
              <thead>
                <tr>
                  <th v-if="batchManage" width="40">
                    <input 
                      type="checkbox" 
                      v-model="selectAllItems" 
                      @change="handleSelectAllItems"
                    >
                  </th>
                  <th>菜品</th>
                  <th>分类</th>
                  <th>价格</th>
                  <th>状态</th>
                  <th>月销量</th>
                  <th>库存</th>
                  <th>评分</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr 
                  v-for="item in filteredItems" 
                  :key="item.id"
                  :class="{ 
                    'row-unavailable': item.status === 'UNAVAILABLE',
                    'row-selected': selectedItems.includes(item.id)
                  }"
                >
                  <td v-if="batchManage">
                    <input 
                      type="checkbox" 
                      :value="item.id" 
                      v-model="selectedItems"
                    >
                  </td>
                  <td>
                    <div class="item-cell">
                      <div class="item-image-small">
                        <img :src="item.imageUrl || '/images/default-food.jpg'" :alt="item.name">
                      </div>
                      <div class="item-basic-info">
                        <div class="item-name">{{ item.name }}</div>
                        <div class="item-description-small">{{ item.description }}</div>
                      </div>
                    </div>
                  </td>
                  <td>{{ getCategoryName(item.categoryId) }}</td>
                  <td>
                    <div class="price-cell">
                      <span class="current-price">¥{{ item.price }}</span>
                      <span v-if="item.originalPrice && item.originalPrice > item.price" class="original-price">
                        ¥{{ item.originalPrice }}
                      </span>
                    </div>
                  </td>
                  <td>
                    <div class="status-badge" :class="item.status.toLowerCase()">
                      {{ getStatusText(item.status) }}
                    </div>
                  </td>
                  <td>{{ item.monthlySales }}</td>
                  <td>{{ item.stock || '∞' }}</td>
                  <td>
                    <div class="rating-cell">
                      <span>{{ item.rating || '-' }}</span>
                      <div v-if="item.rating" class="rating-stars">
                        <i v-for="i in 5" :key="i" :class="i <= item.rating ? 'fas fa-star' : 'far fa-star'"></i>
                      </div>
                    </div>
                  </td>
                  <td>
                    <div class="table-actions">
                      <button @click="editItem(item)" class="table-btn edit" title="编辑">
                        <i class="fas fa-edit"></i>
                      </button>
                      <button @click="toggleItemStatus(item)" class="table-btn toggle" title="切换状态">
                        <i :class="item.status === 'AVAILABLE' ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                      </button>
                      <button @click="duplicateItem(item)" class="table-btn duplicate" title="复制">
                        <i class="fas fa-copy"></i>
                      </button>
                      <button @click="deleteItem(item)" class="table-btn delete" title="删除">
                        <i class="fas fa-trash"></i>
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
              显示 {{ ((currentPage - 1) * pageSize + 1) }} - {{ Math.min(currentPage * pageSize, filteredItems.length) }} 条，
              共 {{ filteredItems.length }} 条记录
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
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon">
        <i class="fas fa-store"></i>
      </div>
      <h3>请先选择门店</h3>
      <p>选择门店后即可管理该门店的菜单</p>
    </div>

    <!-- 新增/编辑分类对话框 -->
    <div v-if="showCategoryDialog || editingCategory" class="modal-overlay" @click="closeCategoryDialog">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ editingCategory ? '编辑分类' : '新增分类' }}</h3>
          <button @click="closeCategoryDialog" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="saveCategory" class="category-form">
            <div class="form-group">
              <label>分类名称 <span class="required">*</span></label>
              <input 
                v-model="categoryForm.name" 
                type="text" 
                required
                placeholder="请输入分类名称"
                class="form-input"
              >
            </div>

            <div class="form-group">
              <label>排序</label>
              <input 
                v-model="categoryForm.sortOrder" 
                type="number" 
                min="0"
                placeholder="数字越小排序越靠前"
                class="form-input"
              >
            </div>

            <div class="form-group">
              <label>分类描述</label>
              <textarea 
                v-model="categoryForm.description" 
                rows="3"
                placeholder="分类描述（可选）"
                class="form-textarea"
              ></textarea>
            </div>

            <div class="form-group">
              <label class="checkbox-label">
                <input 
                  type="checkbox" 
                  v-model="categoryForm.enabled"
                  class="form-checkbox"
                >
                启用此分类
              </label>
            </div>

            <div class="form-actions">
              <button type="button" @click="closeCategoryDialog" class="btn btn-secondary">
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

    <!-- 新增/编辑菜品对话框 -->
    <div v-if="showItemDialog || editingItem" class="modal-overlay" @click="closeItemDialog">
      <div class="modal-content large" @click.stop>
        <div class="modal-header">
          <h3>{{ editingItem ? '编辑菜品' : '新增菜品' }}</h3>
          <button @click="closeItemDialog" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="saveItem" class="item-form">
            <div class="form-row">
              <div class="form-group">
                <label>菜品名称 <span class="required">*</span></label>
                <input 
                  v-model="itemForm.name" 
                  type="text" 
                  required
                  placeholder="请输入菜品名称"
                  class="form-input"
                >
              </div>
              <div class="form-group">
                <label>所属分类 <span class="required">*</span></label>
                <select v-model="itemForm.categoryId" required class="form-select">
                  <option value="">请选择分类</option>
                  <option v-for="category in enabledCategories" :key="category.id" :value="category.id">
                    {{ category.name }}
                  </option>
                </select>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>菜品价格 <span class="required">*</span></label>
                <input 
                  v-model="itemForm.price" 
                  type="number" 
                  min="0"
                  step="0.01"
                  required
                  placeholder="菜品价格"
                  class="form-input"
                >
              </div>
              <div class="form-group">
                <label>原价</label>
                <input 
                  v-model="itemForm.originalPrice" 
                  type="number" 
                  min="0"
                  step="0.01"
                  placeholder="原价（可选）"
                  class="form-input"
                >
              </div>
            </div>

            <div class="form-group">
              <label>菜品描述</label>
              <textarea 
                v-model="itemForm.description" 
                rows="3"
                placeholder="菜品描述和特色介绍"
                class="form-textarea"
              ></textarea>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>库存数量</label>
                <input 
                  v-model="itemForm.stock" 
                  type="number" 
                  min="0"
                  placeholder="留空表示不限库存"
                  class="form-input"
                >
              </div>
              <div class="form-group">
                <label>排序</label>
                <input 
                  v-model="itemForm.sortOrder" 
                  type="number" 
                  min="0"
                  placeholder="数字越小排序越靠前"
                  class="form-input"
                >
              </div>
            </div>

            <div class="form-group">
              <label>菜品状态</label>
              <select v-model="itemForm.status" class="form-select">
                <option value="AVAILABLE">在售</option>
                <option value="UNAVAILABLE">下架</option>
                <option value="OUT_OF_STOCK">缺货</option>
              </select>
            </div>

            <div class="form-group">
              <label>菜品图片</label>
              <div class="image-upload">
                <div class="image-preview">
                  <img v-if="itemForm.imageUrl" :src="itemForm.imageUrl" alt="菜品图片">
                  <div v-else class="image-placeholder">
                    <i class="fas fa-image"></i>
                    <span>暂无图片</span>
                  </div>
                </div>
                <div class="upload-actions">
                  <input 
                    type="file" 
                    accept="image/*"
                    @change="handleImageUpload"
                    class="file-input"
                    ref="fileInput"
                  >
                  <button type="button" @click="$refs.fileInput.click()" class="upload-btn">
                    <i class="fas fa-upload"></i>
                    上传图片
                  </button>
                  <button type="button" @click="itemForm.imageUrl = ''" class="remove-btn">
                    <i class="fas fa-trash"></i>
                    删除
                  </button>
                </div>
              </div>
            </div>

            <div class="form-actions">
              <button type="button" @click="closeItemDialog" class="btn btn-secondary">
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
  name: 'MenuManagement',
  data() {
    return {
      selectedStoreId: '',
      selectedCategoryId: null,
      searchKeyword: '',
      statusFilter: '',
      categoryFilter: '',
      viewMode: 'grid',
      batchManage: false,
      selectedItems: [],
      selectAllItems: false,
      currentPage: 1,
      pageSize: 20,
      submitting: false,
      
      // 对话框状态
      showCategoryDialog: false,
      showItemDialog: false,
      editingCategory: null,
      editingItem: null,

      stores: [
        { id: 1, name: '川香麻辣烫' },
        { id: 2, name: '老北京炸酱面' },
        { id: 3, name: '兰州拉面' },
        { id: 4, name: '黄焖鸡米饭' }
      ],

      categories: [
        { id: 1, name: '招牌菜品', enabled: true, sortOrder: 1, description: '店铺招牌特色菜品' },
        { id: 2, name: '精品套餐', enabled: true, sortOrder: 2, description: '营养搭配套餐' },
        { id: 3, name: '小菜凉菜', enabled: true, sortOrder: 3, description: '开胃小菜' },
        { id: 4, name: '饮品甜品', enabled: true, sortOrder: 4, description: '各类饮品和甜品' }
      ],

      menuItems: [
        {
          id: 1,
          name: '宫保鸡丁',
          categoryId: 1,
          price: 28.00,
          originalPrice: 32.00,
          description: '经典川菜，麻辣鲜香，鸡肉嫩滑',
          imageUrl: null,
          status: 'AVAILABLE',
          stock: 50,
          monthlySales: 156,
          rating: 4.5,
          sortOrder: 1
        },
        {
          id: 2,
          name: '麻婆豆腐',
          categoryId: 1,
          price: 22.00,
          originalPrice: null,
          description: '嫩滑豆腐，麻辣适中，下饭神器',
          imageUrl: null,
          status: 'AVAILABLE',
          stock: null, // 无限库存
          monthlySales: 98,
          rating: 4.3,
          sortOrder: 2
        },
        {
          id: 3,
          name: '经典双拼套餐',
          categoryId: 2,
          price: 35.00,
          originalPrice: 42.00,
          description: '主菜+配菜+米饭，营养均衡',
          imageUrl: null,
          status: 'AVAILABLE',
          stock: 30,
          monthlySales: 87,
          rating: 4.6,
          sortOrder: 1
        },
        {
          id: 4,
          name: '酸菜鱼套餐',
          categoryId: 2,
          price: 45.00,
          originalPrice: null,
          description: '酸菜鱼+小菜+米饭+汤',
          imageUrl: null,
          status: 'UNAVAILABLE',
          stock: 0,
          monthlySales: 23,
          rating: 4.2,
          sortOrder: 2
        }
      ],

      categoryForm: {
        name: '',
        description: '',
        enabled: true,
        sortOrder: 0
      },

      itemForm: {
        name: '',
        categoryId: '',
        price: 0,
        originalPrice: null,
        description: '',
        imageUrl: '',
        status: 'AVAILABLE',
        stock: null,
        sortOrder: 0
      }
    }
  },
  computed: {
    filteredItems() {
      let items = this.menuItems

      if (this.selectedCategoryId) {
        items = items.filter(item => item.categoryId === this.selectedCategoryId)
      }

      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        items = items.filter(item => 
          item.name.toLowerCase().includes(keyword) ||
          item.description.toLowerCase().includes(keyword)
        )
      }

      if (this.statusFilter) {
        items = items.filter(item => item.status === this.statusFilter)
      }

      if (this.categoryFilter) {
        items = items.filter(item => item.categoryId === parseInt(this.categoryFilter))
      }

      return items.sort((a, b) => a.sortOrder - b.sortOrder)
    },

    enabledCategories() {
      return this.categories.filter(cat => cat.enabled)
    },

    totalPages() {
      return Math.ceil(this.filteredItems.length / this.pageSize)
    },

    visiblePages() {
      const pages = []
      const start = Math.max(1, this.currentPage - 2)
      const end = Math.min(this.totalPages, this.currentPage + 2)
      
      for (let i = start; i <= end; i++) {
        pages.push(i)
      }
      return pages
    }
  },
  methods: {
    loadMenuData() {
      if (!this.selectedStoreId) {
        this.categories = []
        this.menuItems = []
        return
      }
      
      // 这里应该根据门店ID加载对应的菜单数据
      console.log('加载门店菜单数据:', this.selectedStoreId)
    },

    handleSearch() {
      this.currentPage = 1
    },

    handleFilter() {
      this.currentPage = 1
    },

    selectCategory(categoryId) {
      this.selectedCategoryId = this.selectedCategoryId === categoryId ? null : categoryId
      this.currentPage = 1
    },

    getSelectedCategoryName() {
      if (!this.selectedCategoryId) return '全部菜品'
      const category = this.categories.find(cat => cat.id === this.selectedCategoryId)
      return category ? category.name : '全部菜品'
    },

    getItemsCountByCategory(categoryId) {
      return this.menuItems.filter(item => item.categoryId === categoryId).length
    },

    getCategoryName(categoryId) {
      const category = this.categories.find(cat => cat.id === categoryId)
      return category ? category.name : '-'
    },

    getStatusText(status) {
      const statusMap = {
        'AVAILABLE': '在售',
        'UNAVAILABLE': '下架',
        'OUT_OF_STOCK': '缺货'
      }
      return statusMap[status] || status
    },

    handleSelectAllItems() {
      if (this.selectAllItems) {
        this.selectedItems = this.filteredItems.map(item => item.id)
      } else {
        this.selectedItems = []
      }
    },

    // 分类管理
    editCategory(category) {
      this.editingCategory = category
      this.categoryForm = {
        name: category.name,
        description: category.description,
        enabled: category.enabled,
        sortOrder: category.sortOrder
      }
    },

    toggleCategory(category) {
      category.enabled = !category.enabled
      this.$message.success(`分类 ${category.name} ${category.enabled ? '已启用' : '已禁用'}`)
    },

    deleteCategory(category) {
      if (confirm(`确定要删除分类 "${category.name}" 吗？\n删除后该分类下的所有菜品将被移动到"未分类"。`)) {
        const index = this.categories.findIndex(cat => cat.id === category.id)
        if (index > -1) {
          this.categories.splice(index, 1)
          // 更新菜品分类
          this.menuItems.forEach(item => {
            if (item.categoryId === category.id) {
              item.categoryId = null
            }
          })
          this.$message.success('分类删除成功')
        }
      }
    },

    async saveCategory() {
      this.submitting = true
      
      try {
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        if (this.editingCategory) {
          Object.assign(this.editingCategory, this.categoryForm)
          this.$message.success('分类更新成功')
        } else {
          const newCategory = {
            id: Date.now(),
            ...this.categoryForm
          }
          this.categories.push(newCategory)
          this.$message.success('分类创建成功')
        }
        
        this.closeCategoryDialog()
      } catch (error) {
        this.$message.error('操作失败，请重试')
      } finally {
        this.submitting = false
      }
    },

    closeCategoryDialog() {
      this.showCategoryDialog = false
      this.editingCategory = null
      this.categoryForm = {
        name: '',
        description: '',
        enabled: true,
        sortOrder: 0
      }
    },

    // 菜品管理
    editItem(item) {
      this.editingItem = item
      this.itemForm = {
        name: item.name,
        categoryId: item.categoryId,
        price: item.price,
        originalPrice: item.originalPrice,
        description: item.description,
        imageUrl: item.imageUrl,
        status: item.status,
        stock: item.stock,
        sortOrder: item.sortOrder
      }
    },

    toggleItemStatus(item) {
      const newStatus = item.status === 'AVAILABLE' ? 'UNAVAILABLE' : 'AVAILABLE'
      item.status = newStatus
      this.$message.success(`菜品 ${item.name} ${newStatus === 'AVAILABLE' ? '已上架' : '已下架'}`)
    },

    duplicateItem(item) {
      const newItem = {
        ...item,
        id: Date.now(),
        name: `${item.name}(副本)`,
        monthlySales: 0
      }
      this.menuItems.push(newItem)
      this.$message.success('菜品复制成功')
    },

    deleteItem(item) {
      if (confirm(`确定要删除菜品 "${item.name}" 吗？`)) {
        const index = this.menuItems.findIndex(i => i.id === item.id)
        if (index > -1) {
          this.menuItems.splice(index, 1)
          this.$message.success('菜品删除成功')
        }
      }
    },

    handleImageUpload(event) {
      const file = event.target.files[0]
      if (file) {
        // 这里应该上传到服务器并获取URL
        // 模拟上传结果
        const reader = new FileReader()
        reader.onload = (e) => {
          this.itemForm.imageUrl = e.target.result
        }
        reader.readAsDataURL(file)
      }
    },

    async saveItem() {
      this.submitting = true
      
      try {
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        if (this.editingItem) {
          Object.assign(this.editingItem, this.itemForm)
          this.$message.success('菜品更新成功')
        } else {
          const newItem = {
            id: Date.now(),
            ...this.itemForm,
            monthlySales: 0,
            rating: 5.0
          }
          this.menuItems.push(newItem)
          this.$message.success('菜品创建成功')
        }
        
        this.closeItemDialog()
      } catch (error) {
        this.$message.error('操作失败，请重试')
      } finally {
        this.submitting = false
      }
    },

    closeItemDialog() {
      this.showItemDialog = false
      this.editingItem = null
      this.itemForm = {
        name: '',
        categoryId: '',
        price: 0,
        originalPrice: null,
        description: '',
        imageUrl: '',
        status: 'AVAILABLE',
        stock: null,
        sortOrder: 0
      }
    },

    // 批量操作
    batchUpdateStatus(status) {
      this.selectedItems.forEach(itemId => {
        const item = this.menuItems.find(i => i.id === itemId)
        if (item) {
          item.status = status
        }
      })
      this.$message.success(`已批量${status === 'AVAILABLE' ? '上架' : '下架'} ${this.selectedItems.length} 个菜品`)
      this.selectedItems = []
      this.selectAllItems = false
    },

    batchUpdateCategory() {
      this.$message.info('批量更换分类功能开发中')
    },

    batchDelete() {
      if (confirm(`确定要删除选中的 ${this.selectedItems.length} 个菜品吗？`)) {
        this.selectedItems.forEach(itemId => {
          const index = this.menuItems.findIndex(i => i.id === itemId)
          if (index > -1) {
            this.menuItems.splice(index, 1)
          }
        })
        this.$message.success(`已删除 ${this.selectedItems.length} 个菜品`)
        this.selectedItems = []
        this.selectAllItems = false
      }
    }
  }
}
</script>

<style scoped>
.menu-management {
  padding: 24px;
  background: #f8f9fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
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

.controls-section {
  background: white;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.store-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.store-selector label {
  font-weight: 600;
  color: #2c3e50;
}

.store-select {
  padding: 10px 14px;
  border: 1px solid #e1e8ed;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  min-width: 200px;
}

.search-filters {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.search-box {
  position: relative;
  flex: 1;
  max-width: 300px;
}

.search-box i {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #7f8c8d;
}

.search-input {
  width: 100%;
  padding: 10px 14px 10px 36px;
  border: 1px solid #e1e8ed;
  border-radius: 6px;
  font-size: 14px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-select {
  padding: 10px 12px;
  border: 1px solid #e1e8ed;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  min-width: 120px;
}

.menu-content {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.menu-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  min-height: 600px;
}

/* 分类侧边栏 */
.categories-sidebar {
  background: #f8f9fa;
  border-right: 1px solid #e1e8ed;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e1e8ed;
}

.sidebar-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.add-category-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #667eea;
  color: white;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.add-category-btn:hover {
  background: #5a67d8;
}

.categories-list {
  padding: 12px;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 4px;
}

.category-item:hover {
  background: white;
}

.category-item.active {
  background: #667eea;
  color: white;
}

.category-info {
  flex: 1;
}

.category-name {
  font-weight: 600;
  margin-bottom: 2px;
}

.category-count {
  font-size: 12px;
  opacity: 0.8;
}

.category-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.category-item:hover .category-actions {
  opacity: 1;
}

.category-action-btn {
  width: 24px;
  height: 24px;
  border: none;
  background: rgba(255,255,255,0.2);
  color: #2c3e50;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.category-action-btn.danger {
  color: #e74c3c;
}

.category-toggle {
  display: flex;
  align-items: center;
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 36px;
  height: 20px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: 0.4s;
  border-radius: 20px;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 16px;
  width: 16px;
  left: 2px;
  bottom: 2px;
  background-color: white;
  transition: 0.4s;
  border-radius: 50%;
}

input:checked + .toggle-slider {
  background-color: #667eea;
}

input:checked + .toggle-slider:before {
  transform: translateX(16px);
}

/* 菜品主区域 */
.items-main {
  padding: 20px;
}

.items-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-info h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 4px 0;
}

.items-count {
  font-size: 14px;
  color: #7f8c8d;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.view-mode {
  display: flex;
  border: 1px solid #e1e8ed;
  border-radius: 6px;
  overflow: hidden;
}

.view-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.view-btn.active {
  background: #667eea;
  color: white;
}

.batch-btn {
  padding: 8px 12px;
  border: 1px solid #e1e8ed;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.batch-actions-bar {
  background: #f8f9fa;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selected-info {
  font-weight: 600;
  color: #667eea;
}

.batch-buttons {
  display: flex;
  gap: 8px;
}

.batch-action-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  color: white;
}

.batch-action-btn.success { background: #27ae60; }
.batch-action-btn.warning { background: #f39c12; }
.batch-action-btn.info { background: #3498db; }
.batch-action-btn.danger { background: #e74c3c; }

/* 网格视图 */
.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.item-card {
  border: 1px solid #e1e8ed;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.2s ease;
  background: white;
}

.item-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
}

.item-card.item-unavailable {
  opacity: 0.6;
  background: #f8f9fa;
}

.item-card.item-selected {
  border-color: #667eea;
  background: #f8f9ff;
}

.item-image {
  position: relative;
  height: 160px;
  background: #f8f9fa;
  overflow: hidden;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-select {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 2;
}

.item-checkbox {
  width: 18px;
  height: 18px;
}

.status-overlay {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  color: white;
}

.status-overlay.available { background: rgba(39, 174, 96, 0.9); }
.status-overlay.unavailable { background: rgba(231, 76, 60, 0.9); }
.status-overlay.out_of_stock { background: rgba(243, 156, 18, 0.9); }

.item-content {
  padding: 16px;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.item-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.item-price {
  text-align: right;
}

.current-price {
  font-size: 18px;
  font-weight: bold;
  color: #e74c3c;
}

.original-price {
  font-size: 14px;
  color: #7f8c8d;
  text-decoration: line-through;
  margin-left: 4px;
}

.item-description {
  font-size: 14px;
  color: #7f8c8d;
  margin-bottom: 12px;
  line-height: 1.4;
}

.item-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #7f8c8d;
}

.stat-item i {
  width: 12px;
}

.item-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  flex: 1;
  padding: 8px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: all 0.2s ease;
}

.action-btn.edit { background: #e3f2fd; color: #1976d2; }
.action-btn.toggle { background: #fff3e0; color: #f57c00; }
.action-btn.duplicate { background: #f3e5f5; color: #7b1fa2; }
.action-btn.delete { background: #ffebee; color: #d32f2f; }

.action-btn:hover {
  transform: translateY(-1px);
}

/* 表格视图 */
.items-table-container {
  overflow-x: auto;
}

.items-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

.items-table th,
.items-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e1e8ed;
}

.items-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

.items-table tr:hover {
  background: #f8f9fa;
}

.items-table tr.row-unavailable {
  opacity: 0.6;
  background: #f8f9fa;
}

.items-table tr.row-selected {
  background: #f8f9ff;
}

.item-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-image-small {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  overflow: hidden;
  background: #f8f9fa;
}

.item-image-small img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-basic-info {
  flex: 1;
}

.item-description-small {
  font-size: 12px;
  color: #7f8c8d;
  margin-top: 2px;
}

.price-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.status-badge.available { background: #d4edda; color: #155724; }
.status-badge.unavailable { background: #f8d7da; color: #721c24; }
.status-badge.out_of_stock { background: #fff3cd; color: #856404; }

.rating-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.rating-stars {
  display: flex;
  gap: 1px;
  margin-top: 2px;
}

.rating-stars i {
  font-size: 10px;
  color: #ffc107;
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

.table-btn.edit { background: #e3f2fd; color: #1976d2; }
.table-btn.toggle { background: #fff3e0; color: #f57c00; }
.table-btn.duplicate { background: #f3e5f5; color: #7b1fa2; }
.table-btn.delete { background: #ffebee; color: #d32f2f; }

.table-btn:hover {
  transform: scale(1.1);
}

/* 分页 */
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

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.empty-icon {
  font-size: 48px;
  color: #e1e8ed;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 20px;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.empty-state p {
  font-size: 16px;
  color: #7f8c8d;
  margin: 0;
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
  max-width: 500px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
}

.modal-content.large {
  max-width: 700px;
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

.category-form,
.item-form {
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

/* 图片上传 */
.image-upload {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.image-preview {
  width: 120px;
  height: 120px;
  border: 2px dashed #e1e8ed;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #f8f9fa;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  text-align: center;
  color: #7f8c8d;
}

.image-placeholder i {
  font-size: 24px;
  margin-bottom: 8px;
  display: block;
}

.upload-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-input {
  display: none;
}

.upload-btn,
.remove-btn {
  padding: 8px 16px;
  border: 1px solid #e1e8ed;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.upload-btn:hover {
  background: #f8f9fa;
}

.remove-btn {
  color: #e74c3c;
  border-color: #e74c3c;
}

.remove-btn:hover {
  background: #fef5f5;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 12px;
}

@media (max-width: 768px) {
  .menu-management {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .header-actions {
    align-self: stretch;
    justify-content: stretch;
  }
  
  .menu-layout {
    grid-template-columns: 1fr;
  }
  
  .categories-sidebar {
    border-right: none;
    border-bottom: 1px solid #e1e8ed;
  }
  
  .search-filters {
    flex-direction: column;
    gap: 12px;
  }
  
  .items-grid {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .image-upload {
    flex-direction: column;
  }
}
</style>