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
        },
        {
          path: 'rooms',
          name: 'room-management',
        }
      ]
    }
  ]
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
  console.log(`路由守卫检查: 从 ${from.path} 到 ${to.path}`);

  // 如果访问员工注册页面，允许直接访问
  if (to.path === '/admin/register') {
    console.log('允许访问员工注册页面');
    next();
    return;
  }

  // 获取登录状态 (管理员和普通用户)
  const adminToken = localStorage.getItem('adminToken');
  const isAdminFlagSet = localStorage.getItem('isAdmin') === 'true';
  const isAdminAuthenticated = adminToken !== null && isAdminFlagSet;

  const userToken = localStorage.getItem('userToken');
  const isUserLoggedIn = userToken !== null;

  console.log(`管理员登录状态: ${isAdminAuthenticated} (Token: ${adminToken ? '存在' : '不存在'}, Flag: ${isAdminFlagSet}), 普通用户登录状态: ${isUserLoggedIn}`);

  // 如果直接访问邀请码管理页面，更新逻辑 (使用 isAdminAuthenticated)
  if (to.path === '/admin/invite-codes') {
    console.log('尝试访问邀请码管理页面');
    if (isAdminAuthenticated) {
      console.log('管理员已认证，允许访问');
      next();
    } else {
      console.log('管理员未认证，重定向到登录页');
      next({ path: '/admin/login', query: { redirect: to.fullPath } });
    }
    return; // 结束此路径的处理
  }

  // 检查是否需要普通用户登录
  if (to.meta.requiresAuth && !isUserLoggedIn) {
    console.log('目标路由需要普通用户认证，但用户未登录，重定向到 /login');
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    });
    return;
  }

  // 如果访问管理员登录页 (/admin/login)
  if (to.path === '/admin/login') {
    if (isAdminAuthenticated) {
      // 管理员已登录，重定向到后台主页
      console.log('管理员已登录，从登录页重定向到 /admin/dashboard');
      next('/admin/dashboard');
    } else {
      // 管理员未登录，允许访问登录页
      console.log('管理员未登录，允许访问登录页');
      next();
    }
    return; // 结束此路径的处理
  }

  // 如果访问后台页面 (除 /admin/login 和 /admin/register)
  if (to.path.startsWith('/admin')) {
    if (!isAdminAuthenticated) {
      // 管理员未登录，重定向到登录页
      console.log('访问后台页面，但管理员未认证，重定向到 /admin/login');
      next({ path: '/admin/login', query: { redirect: to.fullPath } });
    } else {
      // 管理员已登录，允许访问
      // 移除旧的基于角色的路径检查逻辑
      console.log('管理员已认证，允许访问后台页面:', to.path);
      next();
    }
    return; // 结束此路径的处理
  }

  // 其他所有情况 (例如公共页面或普通用户已登录访问用户页面)
  console.log('无需特殊处理或已处理，允许导航');
  next();
});

export default router