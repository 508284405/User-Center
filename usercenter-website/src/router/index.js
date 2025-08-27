import { createRouter, createWebHistory } from 'vue-router'
import { reinitializeTheme } from '@/composables/useTheme'

const routes = [
    // Auth routes
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/auth/Login.vue')
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/auth/Register.vue')
    },
    {
        path: '/google-callback',
        name: 'GoogleCallback',
        component: () => import('../views/auth/GoogleCallback.vue')
    },
    
    // Error pages
    {
        path: '/403',
        name: 'Forbidden',
        component: () => import('../views/shared/error/403.vue')
    },
    
    // Shared utilities
    {
        path: '/theme-demo',
        name: 'ThemeDemo',
        component: () => import('../views/shared/demo/ThemeDemo.vue')
    },
    
    // Legacy dashboard redirect for backward compatibility
    {
        path: '/dashboard',
        redirect: '/platform/usercenter/identity'
    },
    
    // Main platform routes
    {
        path: '/platform',
        component: () => import('../components/layout/MainLayout.vue'),
        meta: { requiresAuth: true },
        children: [
            {
                path: '',
                redirect: '/platform/usercenter/identity'
            },
            
            // SmartCS AI Platform
            {
                path: 'smartcs',
                component: () => import('../views/smartcs/SmartCSLayout.vue'),
                meta: { requiresAuth: true },
                children: [
                    {
                        path: '',
                        redirect: '/platform/smartcs/knowledge'
                    },
                    
                    // Knowledge Management
                    {
                        path: 'knowledge',
                        component: () => import('../views/smartcs/knowledge/KnowledgeLayout.vue'),
                        meta: { requiresAuth: true },
                        children: [
                            {
                                path: '',
                                redirect: '/platform/smartcs/knowledge/bases'
                            },
                            {
                                path: 'bases',
                                name: 'KnowledgeBases',
                                component: () => import('../views/smartcs/knowledge/KnowledgeBaseManagementView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'knowledge:base:view',
                                    title: '知识库管理'
                                }
                            },
                            {
                                path: 'bases/:id',
                                name: 'KnowledgeBaseDetail',
                                component: () => import('../views/smartcs/knowledge/KnowledgeBaseDetailView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'knowledge:base:view',
                                    title: '知识库详情'
                                }
                            },
                            {
                                path: 'content',
                                name: 'KnowledgeContent',
                                component: () => import('../views/smartcs/knowledge/ContentManagementView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'knowledge:content:view',
                                    title: '内容管理'
                                }
                            },
                            {
                                path: 'content/:id',
                                name: 'DocumentDetail',
                                component: () => import('../views/smartcs/knowledge/DocumentDetailView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'knowledge:content:view',
                                    title: '文档详情'
                                }
                            },
                            {
                                path: 'content/:id/chunks',
                                name: 'ChunkManagement',
                                component: () => import('../views/smartcs/knowledge/ChunkManagementView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'knowledge:content:view',
                                    title: '切片管理'
                                }
                            },
                            {
                                path: 'faq',
                                name: 'KnowledgeFAQ',
                                component: () => import('../views/smartcs/knowledge/FaqManagementView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'knowledge:faq:view',
                                    title: 'FAQ管理'
                                }
                            },
                            {
                                path: 'index',
                                name: 'KnowledgeIndex',
                                component: () => import('../views/smartcs/infrastructure/index/IndexListView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'knowledge:index:view',
                                    title: '索引管理'
                                }
                            },
                            {
                                path: 'provider',
                                name: 'ModelProvider',
                                component: () => import('../views/smartcs/knowledge/ProviderManagementView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'knowledge:provider:view',
                                    title: '模型提供商管理'
                                }
                            },
                            {
                                path: 'model',
                                name: 'ModelManagement',
                                component: () => import('../views/smartcs/knowledge/ModelManagementView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'knowledge:model:view',
                                    title: '模型管理'
                                }
                            },
                            {
                                path: 'app',
                                name: 'AppManagement',
                                component: () => import('../views/smartcs/knowledge/AppManagementView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'app:view',
                                    title: 'APP管理'
                                }
                            },
                            {
                                path: 'app/:id',
                                name: 'AppDetail',
                                component: () => import('../views/smartcs/knowledge/AppDetailView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'app:view',
                                    title: 'APP详情'
                                }
                            },
                            {
                                path: 'chat',
                                name: 'SmartChat',
                                component: () => import('../views/smartcs/knowledge/ChatView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'knowledge:chat:view',
                                    title: '智能聊天'
                                }
                            },
                            {
                                path: 'debug/query-transformer',
                                name: 'QueryTransformerDebug',
                                component: () => import('../views/smartcs/debug/QueryTransformerDebugView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'knowledge:debug:view',
                                    title: '查询转换器调试'
                                }
                            }
                        ]
                    },
                    
                    // Intent Management
                    {
                        path: 'intent',
                        component: () => import('../views/smartcs/intent/IntentLayout.vue'),
                        meta: { requiresAuth: true },
                        children: [
                            {
                                path: '',
                                redirect: '/platform/smartcs/intent/classification'
                            },
                            {
                                path: 'classification',
                                name: 'IntentClassification',
                                component: () => import('../views/smartcs/intent/classification/IntentManagementView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'intent:view',
                                    title: '意图分类管理'
                                }
                            },
                            {
                                path: 'catalog',
                                name: 'IntentCatalog',
                                component: () => import('../views/smartcs/intent/catalog/CatalogManagementView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'intent:catalog:view',
                                    title: '意图目录管理'
                                }
                            },
                            {
                                path: 'testing',
                                name: 'IntentTesting',
                                component: () => import('../views/smartcs/intent/testing/ClassificationTestView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'intent:test:view',
                                    title: '分类测试'
                                }
                            },
                            {
                                path: 'snapshots',
                                name: 'IntentSnapshots',
                                component: () => import('../views/smartcs/intent/snapshots/SnapshotManagementView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'intent:snapshot:view',
                                    title: '快照管理'
                                }
                            }
                        ]
                    },
                    
                    // Moderation Management
                    {
                        path: 'moderation',
                        component: () => import('../views/smartcs/moderation/ModerationLayout.vue'),
                        meta: { requiresAuth: true },
                        children: [
                            {
                                path: '',
                                redirect: '/platform/smartcs/moderation/policies'
                            },
                            {
                                path: 'policies',
                                name: 'ModerationPolicies',
                                component: () => import('../views/smartcs/moderation/PolicyManagementView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'moderation:policy:view',
                                    title: '审核策略管理'
                                }
                            },
                            {
                                path: 'dimensions',
                                name: 'ModerationDimensions',
                                component: () => import('../views/smartcs/moderation/DimensionManagementView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'moderation:dimension:view',
                                    title: '审核维度管理'
                                }
                            },
                            {
                                path: 'templates',
                                name: 'ModerationTemplates',
                                component: () => import('../views/smartcs/moderation/TemplateManagementView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'moderation:template:view',
                                    title: '模板管理'
                                }
                            },
                            {
                                path: 'records',
                                name: 'ModerationRecords',
                                component: () => import('../views/smartcs/moderation/RecordManagementView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'moderation:record:view',
                                    title: '审核记录'
                                }
                            },
                            {
                                path: 'config',
                                name: 'ModerationConfig',
                                component: () => import('../views/smartcs/moderation/ConfigManagementView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'moderation:config:view',
                                    title: '审核配置'
                                }
                            }
                        ]
                    }
                ]
            },
            
            // E-commerce Platform
            {
                path: 'ecommerce',
                component: () => import('../views/ecommerce/EcommerceLayout.vue'),
                meta: { requiresAuth: true },
                children: [
                    {
                        path: '',
                        redirect: '/platform/ecommerce/catalog'
                    },
                    
                    // Product Catalog
                    {
                        path: 'catalog',
                        children: [
                            {
                                path: '',
                                redirect: '/platform/ecommerce/catalog/products'
                            },
                            {
                                path: 'products',
                                name: 'ProductManagement',
                                component: () => import('../views/ecommerce/catalog/products/ProductList.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'product:view',
                                    title: '商品管理'
                                }
                            },
                            {
                                path: 'products/:id',
                                name: 'ProductDetail',
                                component: () => import('../views/ecommerce/catalog/products/ProductDetail.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'product:view',
                                    title: '商品详情'
                                }
                            },
                            {
                                path: 'categories',
                                name: 'CategoryManagement',
                                component: () => import('../views/ecommerce/catalog/products/CategoryManagement.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'product:category:view',
                                    title: '分类管理'
                                }
                            },
                            {
                                path: 'promotions',
                                name: 'PromotionManagement',
                                component: () => import('../views/ecommerce/catalog/promotions/ActivityList.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'promotion:view',
                                    title: '促销管理'
                                }
                            }
                        ]
                    },
                    
                    // Order Management
                    {
                        path: 'orders',
                        children: [
                            {
                                path: '',
                                redirect: '/platform/ecommerce/orders/management'
                            },
                            {
                                path: 'management',
                                name: 'OrderManagement',
                                component: () => import('../views/ecommerce/orders/management/OrderList.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'order:view',
                                    title: '订单管理'
                                }
                            },
                            {
                                path: 'management/:id',
                                name: 'OrderDetail',
                                component: () => import('../views/ecommerce/orders/management/OrderDetail.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'order:view',
                                    title: '订单详情'
                                }
                            },
                            {
                                path: 'after-sales',
                                name: 'AfterSalesManagement',
                                component: () => import('../views/ecommerce/orders/after-sales/AfterSalesList.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'after-sales:view',
                                    title: '售后管理'
                                }
                            }
                        ]
                    },
                    
                    // Logistics Management
                    {
                        path: 'logistics',
                        component: () => import('../views/ecommerce/logistics/Layout.vue'),
                        children: [
                            {
                                path: '',
                                redirect: '/platform/ecommerce/logistics/packages'
                            },
                            {
                                path: 'packages',
                                name: 'LogisticsPackages',
                                component: () => import('../views/ecommerce/logistics/packages/List.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'logistics:packages:view',
                                    title: '包裹管理'
                                }
                            },
                            {
                                path: 'orders',
                                name: 'LogisticsOrders',
                                component: () => import('../views/ecommerce/logistics/orders/List.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'logistics:orders:view',
                                    title: '物流订单管理'
                                }
                            }
                        ]
                    },
                    
                    // Loyalty Management
                    {
                        path: 'loyalty',
                        children: [
                            {
                                path: '',
                                redirect: '/platform/ecommerce/loyalty/points'
                            },
                            {
                                path: 'points',
                                name: 'PointsManagement',
                                component: () => import('../views/ecommerce/loyalty/UserPointsManagement.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'points:view',
                                    title: '积分管理'
                                }
                            },
                            {
                                path: 'products',
                                name: 'PointsProducts',
                                component: () => import('../views/ecommerce/loyalty/PointsProductList.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'points:product:view',
                                    title: '积分商品'
                                }
                            }
                        ]
                    }
                ]
            },
            
            // User Center
            {
                path: 'usercenter',
                component: () => import('../views/usercenter/UserCenterLayout.vue'),
                meta: { requiresAuth: true },
                children: [
                    {
                        path: '',
                        redirect: '/platform/usercenter/identity'
                    },
                    
                    // Identity Management
                    {
                        path: 'identity',
                        children: [
                            {
                                path: '',
                                redirect: '/platform/usercenter/identity/users'
                            },
                            {
                                path: 'users',
                                name: 'UserManagement',
                                component: () => import('../views/usercenter/identity/users/UserManagementView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'user:view',
                                    title: '用户管理'
                                }
                            },
                            {
                                path: 'roles',
                                name: 'RoleManagement',
                                component: () => import('../views/usercenter/identity/roles/RoleManagementView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'role:view',
                                    title: '角色管理'
                                }
                            }
                        ]
                    },
                    
                    // System Management
                    {
                        path: 'system',
                        children: [
                            {
                                path: '',
                                redirect: '/platform/usercenter/system/menu'
                            },
                            {
                                path: 'menu',
                                name: 'MenuManagement',
                                component: () => import('../views/usercenter/system/menu/MenuManagementView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'menu:view',
                                    title: '菜单管理'
                                }
                            },
                            {
                                path: 'logs',
                                name: 'OperationLog',
                                component: () => import('../views/usercenter/system/logs/OperationLogView.vue'),
                                meta: {
                                    requiresAuth: true,
                                    permission: 'log:view',
                                    title: '操作日志'
                                }
                            }
                        ]
                    },
                    
                    // Profile
                    {
                        path: 'profile',
                        name: 'Profile',
                        component: () => import('../views/usercenter/identity/ProfileView.vue'),
                        meta: {
                            requiresAuth: true,
                            title: '个人资料'
                        }
                    }
                ]
            },
            
            // Direct AI Management routes (bypass SmartCS platform layout)
            // These routes are accessed from the sidebar AI management submenu
            {
                path: 'ai-management/faq',
                name: 'DirectFAQManagement',
                component: () => import('../views/smartcs/knowledge/FaqManagementView.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'knowledge:faq:view',
                    title: 'FAQ管理'
                }
            },
            {
                path: 'ai-management/knowledge-bases',
                name: 'DirectKnowledgeBases',
                component: () => import('../views/smartcs/knowledge/KnowledgeBaseManagementView.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'knowledge:base:view',
                    title: '知识库管理'
                }
            },
            {
                path: 'ai-management/knowledge-bases/:id',
                name: 'DirectKnowledgeBaseDetail',
                component: () => import('../views/smartcs/knowledge/KnowledgeBaseDetailView.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'knowledge:base:view',
                    title: '知识库详情'
                }
            },
            {
                path: 'ai-management/content',
                name: 'DirectContentManagement',
                component: () => import('../views/smartcs/knowledge/ContentManagementView.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'knowledge:content:view',
                    title: '内容管理'
                }
            },
            {
                path: 'ai-management/content/:id',
                name: 'DirectDocumentDetail',
                component: () => import('../views/smartcs/knowledge/DocumentDetailView.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'knowledge:content:view',
                    title: '文档详情'
                }
            },
            {
                path: 'ai-management/content/:id/chunks',
                name: 'DirectChunkManagement',
                component: () => import('../views/smartcs/knowledge/ChunkManagementView.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'knowledge:content:view',
                    title: '切片管理'
                }
            },
            {
                path: 'ai-management/index',
                name: 'DirectKnowledgeIndex',
                component: () => import('../views/smartcs/infrastructure/index/IndexListView.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'knowledge:index:view',
                    title: '索引管理'
                }
            },
            {
                path: 'ai-management/provider',
                name: 'DirectModelProvider',
                component: () => import('../views/smartcs/knowledge/ProviderManagementView.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'knowledge:provider:view',
                    title: '模型提供商管理'
                }
            },
            {
                path: 'ai-management/model',
                name: 'DirectModelManagement',
                component: () => import('../views/smartcs/knowledge/ModelManagementView.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'knowledge:model:view',
                    title: '模型管理'
                }
            },
            {
                path: 'ai-management/app',
                name: 'DirectAppManagement',
                component: () => import('../views/smartcs/knowledge/AppManagementView.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'app:view',
                    title: 'APP管理'
                }
            },
            {
                path: 'ai-management/app/:id',
                name: 'DirectAppDetail',
                component: () => import('../views/smartcs/knowledge/AppDetailView.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'app:view',
                    title: 'APP详情'
                }
            },

            {
                path: 'ai-management/intent-catalog',
                name: 'DirectIntentCatalog',
                component: () => import('../views/smartcs/intent/catalog/CatalogManagementView.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'intent:catalog:view',
                    title: '意图目录管理'
                }
            },
            {
                path: 'ai-management/chat',
                name: 'DirectSmartChat',
                component: () => import('../views/smartcs/knowledge/ChatView.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'knowledge:chat:view',
                    title: '智能聊天'
                }
            },

            // Dictionary Management
            {
                path: 'ai-management/dictionary',
                name: 'DirectDictionaryManagement',
                component: () => import('../views/smartcs/dictionary/DictionaryManagementView.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'dictionary:view',
                    title: '字典管理'
                }
            },
            
            // Debug Tools
            {
                path: 'ai-management/debug/query-transformer',
                name: 'DirectQueryTransformerDebug',
                component: () => import('../views/smartcs/debug/QueryTransformerDebugView.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'knowledge:debug:view',
                    title: '查询转换器调试'
                }
            },

            // Direct Moderation Management routes (bypass SmartCS platform layout)
            {
                path: 'ai-management/moderation',
                component: () => import('../views/smartcs/moderation/DirectModerationLayout.vue'),
                meta: { requiresAuth: true },
                children: [
                    {
                        path: '',
                        name: 'DirectModerationPolicies',
                        component: () => import('../views/smartcs/moderation/PolicyManagementView.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'moderation:policy:view',
                            title: '审核策略管理'
                        }
                    },
                    {
                        path: 'dimensions',
                        name: 'DirectModerationDimensions',
                        component: () => import('../views/smartcs/moderation/DimensionManagementView.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'moderation:dimension:view',
                            title: '审核维度管理'
                        }
                    },
                    {
                        path: 'templates',
                        name: 'DirectModerationTemplates',
                        component: () => import('../views/smartcs/moderation/TemplateManagementView.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'moderation:template:view',
                            title: '模板管理'
                        }
                    }
                ]
            },
            
            // 客服管理路由
            {
                path: 'customer-service',
                component: () => import('../views/customer-service/CustomerServiceLayout.vue'),
                meta: { requiresAuth: true },
                children: [
                    {
                        path: '',
                        redirect: '/platform/customer-service/sessions'
                    },
                    {
                        path: 'sessions',
                        name: 'SessionsList',
                        component: () => import('../views/customer-service/SessionsListView.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'customer-service:view',
                            title: '会话列表'
                        }
                    },
                    {
                        path: 'agents',
                        name: 'OnlineAgents',
                        component: () => import('../views/customer-service/AgentsView.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'customer-service:view',
                            title: '在线客服'
                        }
                    },
                    {
                        path: 'chat/:sessionId?',
                        name: 'AgentChat',
                        component: () => import('../views/customer-service/ChatView.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'customer-service:chat',
                            title: '客服对话'
                        }
                    }
                ]
            }
        ]
    },
    
    // Legacy route redirects for backward compatibility
    {
        path: '/dashboard/intent-management',
        redirect: '/platform/smartcs/intent'
    },
    {
        path: '/dashboard/intent-management/classification',
        redirect: '/platform/smartcs/intent/classification'
    },
    {
        path: '/dashboard/intent-management/catalog',
        redirect: '/platform/smartcs/intent/catalog'
    },
    {
        path: '/dashboard/intent-management/test',
        redirect: '/platform/smartcs/intent/testing'
    },
    {
        path: '/dashboard/intent-management/snapshot',
        redirect: '/platform/smartcs/intent/snapshots'
    },
    {
        path: '/dashboard/knowledge',
        redirect: '/platform/smartcs/knowledge'
    },
    {
        path: '/dashboard/moderation',
        redirect: '/platform/smartcs/moderation'
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// Navigation guard for authentication
router.beforeEach((to, from, next) => {
    const isAuthenticated = !!localStorage.getItem('access_token')
    
    if (to.meta.requiresAuth && !isAuthenticated) {
        next('/login')
    } else {
        next()
    }
})

// Theme reinitialization after route change
router.afterEach(() => {
    reinitializeTheme()
})

export default router