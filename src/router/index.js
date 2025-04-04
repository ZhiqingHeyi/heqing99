import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import AdminLayout from '../views/admin/Layout.vue'
import AdminLogin from '../views/admin/Login.vue'
import InviteCodes from '../views/admin/InviteCodes.vue'
import AdminRegister from '../views/admin/Register.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/About.vue')
    },
    {
      path: '/rooms',
      name: 'rooms',
      component: () => import('../views/Rooms.vue')
    },
    {
      path: '/contact',
      name: 'contact',
      component: () => import('../views/Contact.vue')
    },
    {
      path: '/booking',
      name: 'booking',
      component: () => import('../views/Booking.vue')
    },
    {
      path: '/room/:id',
      name: 'room-detail',
      component: () => import('../views/RoomDetail.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/user/Login.vue'),
      meta: { public: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/user/Register.vue'),
      meta: { public: true }
    },
    {
      path: '/user',
      name: 'user',
      component: () => import('../views/user/Profile.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/user/bookings',
      name: 'user-bookings',
      component: () => import('../views/user/Bookings.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/user/booking/:id',
      name: 'user-booking-detail',
      component: () => import('../views/user/BookingDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/user/membership',
      name: 'user-membership',
      component: () => import('../views/user/Membership.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: AdminLogin,
      meta: { public: true }
    },
    {
      path: '/admin/register',
      name: 'admin-register',
      component: AdminRegister,
      meta: { public: true }
    },
    {
      path: '/admin',
      component: AdminLayout,
      children: [
        {
          path: 'dashboard',
          name: 'admin-dashboard',
        },
        {
          path: 'users',
          name: 'admin-users',
        },
        {
          path: 'staff',
          name: 'admin-staff',
        },
        {
          path: 'invite-codes',
          name: 'admin-invite-codes',
          component: InviteCodes
        },
        {
          path: 'reception/bookings',
          name: 'reception-bookings',
        },
        {
          path: 'reception/checkin',
          name: 'reception-checkin',
        },
        {
          path: 'reception/visitors',
          name: 'reception-visitors',
        },
        {
          path: 'reception/visitor-records',
          name: 'reception-visitor-records',
        },
        {
          path: 'cleaning/tasks',
          name: 'cleaning-tasks',
        },
        {
          path: 'cleaning/records',
          name: 'cleaning-records',
        }
      ]
    }
  ]
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
  console.log('路由守卫检查:', to.path)
  
  // 如果访问员工注册页面，允许直接访问
  if (to.path === '/admin/register') {
    console.log('访问员工注册页面')
    next()
    return
  }
  
  // 如果直接访问邀请码管理页面，特殊处理
  if (to.path === '/admin/invite-codes') {
    console.log('正在尝试访问邀请码管理页面')
    const userRole = localStorage.getItem('userRole')
    const token = localStorage.getItem('token')
    const isLoggedIn = userRole !== null && token !== null
    
    if (isLoggedIn && userRole === 'admin') {
      console.log('有权限访问邀请码管理页面')
      next()
      return
    } else if (!isLoggedIn) {
      next('/admin/login')
      return
    } else {
      next('/admin/dashboard')
      return
    }
  }
  
  // 获取用户角色和登录状态
  const userRole = localStorage.getItem('userRole')
  const token = localStorage.getItem('token')
  const isLoggedIn = userRole !== null && token !== null
  const userToken = localStorage.getItem('userToken')
  const isUserLoggedIn = userToken !== null
  
  console.log('当前用户角色:', userRole, '管理员登录状态:', isLoggedIn, '后台Token:', token, '用户登录状态:', isUserLoggedIn)

  // 检查是否需要用户登录
  if (to.meta.requiresAuth && !isUserLoggedIn) {
    console.log('需要用户登录，重定向到登录页')
    
    // 为了调试，模拟一个登录状态
    if (to.path === '/user') {
      console.log('调试模式：设置临时用户登录状态')
      localStorage.setItem('userToken', 'temp-token')
      localStorage.setItem('userName', '测试用户')
      localStorage.setItem('userLevel', '普通用户')
      localStorage.setItem('userPoints', '0')
      localStorage.setItem('userTotalSpent', '0')
      next()
      return
    }
    
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // 如果访问登录页
  if (to.path === '/admin/login') {
    if (isLoggedIn) {
      // 已登录用户重定向到对应的首页
      console.log('已登录，从登录页重定向到首页')
      switch (userRole) {
        case 'admin':
          next('/admin/dashboard')
          break
        case 'receptionist':
          next('/admin/reception/bookings')
          break
        case 'cleaner':
          next('/admin/cleaning/tasks')
          break
        default:
          next('/admin/login')
      }
    } else {
      next()
    }
    return
  }

  // 如果访问后台页面
  if (to.path.startsWith('/admin') && to.path !== '/admin/login') {
    if (!isLoggedIn) {
      // 未登录用户重定向到登录页
      console.log('未登录，重定向到登录页')
      next('/admin/login')
      return
    }

    // 根据角色验证访问权限
    const adminPaths = ['/admin/dashboard', '/admin/users', '/admin/staff', '/admin/invite-codes']
    const receptionistPaths = ['/admin/reception/bookings', '/admin/reception/checkin', '/admin/reception/visitors']
    const cleanerPaths = ['/admin/cleaning/tasks', '/admin/cleaning/records']

    // 检查用户是否有权限访问该路径
    let hasPermission = false
    
    if (userRole === 'admin' && adminPaths.some(path => to.path === path || to.path.startsWith(path + '/'))) {
      hasPermission = true
    } else if (userRole === 'receptionist' && receptionistPaths.some(path => to.path === path || to.path.startsWith(path + '/'))) {
      hasPermission = true
    } else if (userRole === 'cleaner' && cleanerPaths.some(path => to.path === path || to.path.startsWith(path + '/'))) {
      hasPermission = true
    }
    
    console.log('权限检查结果:', hasPermission, '路径:', to.path)

    if (!hasPermission) {
      // 无权限访问，重定向到对应的首页
      console.log('无权限访问该页面，重定向到对应首页')
      switch (userRole) {
        case 'admin':
          next('/admin/dashboard')
          break
        case 'receptionist':
          next('/admin/reception/bookings')
          break
        case 'cleaner':
          next('/admin/cleaning/tasks')
          break
        default:
          next('/admin/login')
      }
      return
    }
  }

  next()
})

export default router