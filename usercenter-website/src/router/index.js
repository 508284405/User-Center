import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue')
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/Register.vue')
    },
    {
        path: '/google-callback',
        name: 'GoogleCallback',
        component: () => import('../views/GoogleCallback.vue')
    },
    {
        path: '/403',
        name: 'Forbidden',
        component: () => import('../views/error/403.vue')
    },
    {
        path: '/dashboard',
        component: () => import('../components/layout/MainLayout.vue'),
        meta: { requiresAuth: true },
        children: [
            {
                path: '',
                redirect: '/dashboard/user'
            },
            {
                path: 'user',
                name: 'UserManagement',
                component: () => import('../views/user/UserManagement.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'user:view',
                    title: '用户管理'
                }
            },
            {
                path: 'role',
                name: 'RoleManagement',
                component: () => import('../views/role/RoleManagement.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'role:view',
                    title: '角色管理'
                }
            },
            {
                path: 'menu',
                name: 'MenuManagement',
                component: () => import('../views/menu/MenuManagement.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'menu:view',
                    title: '菜单管理'
                }
            },
            {
                path: 'log',
                name: 'OperationLog',
                component: () => import('../views/log/OperationLog.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'log:view',
                    title: '操作日志'
                }
            },
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('../views/profile/Profile.vue'),
                meta: {
                    requiresAuth: true,
                    title: '个人资料'
                }
            },
            {
                path: 'product',
                name: 'ProductList',
                component: () => import('../views/product/ProductList.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'product:view',
                    title: '商品列表'
                }
            },
            {
                path: 'product/category',
                name: 'CategoryManagement',
                component: () => import('../views/product/CategoryManagement.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'product:category:view',
                    title: '商品分类管理'
                }
            },
            {
                path: 'order/list',
                name: 'OrderList',
                component: () => import('../views/order/OrderList.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'order:view',
                    title: '订单列表'
                }
            },
            {
                path: 'order/detail/:orderNumber',
                name: 'OrderDetail',
                component: () => import('../views/order/OrderDetail.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'order:view',
                    title: '订单详情'
                }
            },
            {
                path: 'points',
                name: 'PointsManagement',
                component: () => import('../views/points/UserPointsManagement.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'points:view',
                    title: '用户积分管理'
                }
            },
            {
                path: 'points/product',
                name: 'PointsProductManagement',
                component: () => import('../views/points/PointsProductList.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'points:product:view',
                    title: '积分商品管理'
                }
            },
            {
                path: 'points/exchange',
                name: 'ExchangeRecordList',
                component: () => import('../views/points/ExchangeRecordList.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'points:exchange:view',
                    title: '兑换记录管理'
                }
            },
            {
                path: '/after-sales',
                name: 'AfterSalesList',
                component: () => import('../views/after-sales/AfterSalesList.vue'),
                meta: { requiresAuth: true, title: '售后管理' }
            },
            {
                path: '/after-sales/:id',
                name: 'AfterSalesDetail',
                component: () => import('../views/after-sales/AfterSalesDetail.vue'),
                meta: { requiresAuth: true, title: '售后详情' }
            },
            {
                path: '/after-sales/refund',
                name: 'RefundList',
                component: () => import('../views/after-sales/RefundList.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'aftersale:refund:view',
                    title: '售后退款管理'
                }
            },
            {
                path: '/after-sales/refund/:id',
                name: 'RefundDetail',
                component: () => import('../views/after-sales/RefundDetail.vue'),
                meta: {
                    requiresAuth: true,
                    permission: 'aftersale:refund:view',
                    title: '售后退款详情'
                }
            },
            // 物流管理路由
            {
                path: '/logistics',
                component: () => import('../views/logistics/Layout.vue'),
                meta: { requiresAuth: true },
                children: [
                    {
                        path: '',
                        redirect: '/logistics/packages'
                    },
                    {
                        path: 'packages',
                        name: 'LogisticsPackages',
                        component: () => import('../views/logistics/packages/List.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'logistics:packages:view',
                            title: '包裹管理'
                        }
                    },
                    {
                        path: 'packages/:id',
                        name: 'LogisticsPackageDetail',
                        component: () => import('../views/logistics/packages/Detail.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'logistics:packages:view',
                            title: '包裹详情'
                        }
                    },
                    {
                        path: 'orders',
                        name: 'LogisticsOrders',
                        component: () => import('../views/logistics/orders/List.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'logistics:orders:view',
                            title: '物流订单管理'
                        }
                    },
                    {
                        path: 'orders/create',
                        name: 'LogisticsOrderCreate',
                        component: () => import('../views/logistics/orders/Create.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'logistics:orders:create',
                            title: '创建物流订单'
                        }
                    },
                    {
                        path: 'orders/:id',
                        name: 'LogisticsOrderDetail',
                        component: () => import('../views/logistics/orders/Detail.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'logistics:orders:view',
                            title: '物流订单详情'
                        }
                    },
                    {
                        path: 'analysis',
                        name: 'LogisticsAnalysis',
                        component: () => import('../views/logistics/analysis/Analysis.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'logistics:analysis:view',
                            title: '物流分析'
                        }
                    }
                ]
            },
            // 客服管理路由
            {
                path: '/customer-service',
                component: () => import('../views/customer-service/CustomerServiceLayout.vue'),
                meta: { requiresAuth: true },
                children: [
                    {
                        path: '',
                        redirect: '/customer-service/pending'
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
            },
            // 知识库管理路由
            {
                path: 'knowledge',
                component: () => import('../views/dashboard/knowledge/KnowledgeLayout.vue'),
                meta: { requiresAuth: true },
                children: [
                    {
                        path: '',
                        redirect: '/dashboard/knowledge/faq'
                    },
                    {
                        path: 'index',
                        name: 'KnowledgeIndex',
                        component: () => import('../views/dashboard/infrastructure/index/IndexListView.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'knowledge:index:view',
                            title: '索引管理'
                        }
                    },
                    {
                        path: 'faq',
                        name: 'FaqManagement',
                        component: () => import('../views/dashboard/knowledge/FaqManagementView.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'knowledge:faq:view',
                            title: 'FAQ管理'
                        }
                    },
                    {
                        path: 'base',
                        name: 'KnowledgeBaseManagement',
                        component: () => import('../views/dashboard/knowledge/KnowledgeBaseManagementView.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'knowledge:base:view',
                            title: '知识库管理'
                        }
                    },
                    {
                        path: 'base/:knowledgeBaseId/contents',
                        name: 'ContentList',
                        component: () => import('../views/dashboard/knowledge/ContentListView.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'knowledge:content:view',
                            title: '文档列表'
                        }
                    },
                    {
                        path: 'content',
                        name: 'ContentManagement',
                        component: () => import('../views/dashboard/knowledge/ContentManagementView.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'knowledge:content:view',
                            title: '内容管理'
                        }
                    },
                    {
                        path: 'content/:contentId/chunks',
                        name: 'ChunkManagement',
                        component: () => import('../views/dashboard/knowledge/ChunkManagementView.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'knowledge:chunk:view',
                            title: '切片管理'
                        }
                    },
                    {
                        path: 'embeddings',
                        name: 'EmbeddingList',
                        component: () => import('../views/dashboard/knowledge/EmbeddingListView.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'knowledge:doc:view',
                            title: '向量列表'
                        }
                    },
                    
                    {
                        path: 'chat',
                        name: 'ChatTest',
                        component: () => import('../views/dashboard/knowledge/ChatView.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'knowledge:chat:view',
                            title: '智能聊天'
                        }
                    },
                    {
                        path: 'provider',
                        name: 'ProviderManagement',
                        component: () => import('../views/dashboard/knowledge/ProviderManagementView.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'knowledge:provider:view',
                            title: '模型提供商管理'
                        }
                    },
                    {
                        path: 'model',
                        name: 'ModelManagement',
                        component: () => import('../views/dashboard/knowledge/ModelManagementView.vue'),
                        meta: {
                            requiresAuth: true,
                            permission: 'knowledge:model:view',
                            title: '模型管理'
                        }
                    }
                ]
            },
            // 秒杀管理路由
            {
                path: '/seckill',
                component: () => import('../components/layout/MainLayout.vue'),
                meta: { requiresAuth: true },
                children: [
                    {
                        path: '',
                        redirect: '/seckill/activities'
                    },
                    {
                        path: 'activities',
                        name: 'SeckillActivityList',
                        component: () => import('../views/seckill/ActivityList.vue'),
                        meta: {
                            requiresAuth: true,
                            title: '秒杀活动管理'
                        }
                    },
                    {
                        path: 'orders',
                        name: 'SeckillOrderList',
                        component: () => import('../views/seckill/OrderList.vue'),
                        meta: {
                            requiresAuth: true,
                            title: '秒杀订单管理'
                        }
                    }
                ]
            }
        ]
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/dashboard'
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 不再需要这个路由守卫，已在permission.ts中定义
// 保留用于调试日志
router.afterEach((to, from) => {
    console.log(`路由跳转完成: 从 ${from.path} 到 ${to.path}`)
})

router.onError((error) => {
    console.error('路由错误:', error)
})

export default router