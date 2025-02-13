import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
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
    path: '/dashboard',
    component: () => import('../components/layout/MainLayout.vue'),
    children: [
      {
        path: '',
        redirect: '/dashboard/user'
      },
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('../views/user/UserManagement.vue')
      },
      {
        path: 'role',
        name: 'RoleManagement',
        component: () => import('../views/role/RoleManagement.vue')
      },
      {
        path: 'menu',
        name: 'MenuManagement',
        component: () => import('../views/menu/MenuManagement.vue')
      },
      {
        path: 'log',
        name: 'OperationLog',
        component: () => import('../views/log/OperationLog.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/profile/Profile.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router