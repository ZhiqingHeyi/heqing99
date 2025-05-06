import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import AdminLayout from '../views/admin/Layout.vue'
import AdminLogin from '../views/admin/Login.vue'
import InviteCodes from '../views/admin/InviteCodes.vue'
import AdminRegister from '../views/admin/Register.vue'
import AdminDashboard from '../views/admin/Dashboard.vue'
import AdminUsers from '../views/admin/Users.vue'
import AdminStaff from '../views/admin/Staff.vue'
import AdminRoomManagement from '../views/admin/RoomManagement.vue'
import ReceptionBookings from '../views/admin/reception/Bookings.vue'
import ReceptionCheckin from '../views/admin/reception/Checkin.vue'
import ReceptionVisitors from '../views/admin/reception/Visitors.vue'
import ReceptionCheckout from '../views/admin/reception/Checkout.vue'
import CleaningTasks from '../views/admin/cleaning/Tasks.vue'
import CleaningRecords from '../views/admin/cleaning/Records.vue'
import { useAuthStore } from '@/store/auth'

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
      meta: { requiresAdminAuth: true },
      children: [
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: AdminDashboard,
          meta: { title: '数据看板' }
        },
        {
          path: 'users',
          name: 'admin-users',
          component: AdminUsers,
          meta: { title: '用户管理' }
        },
        {
          path: 'staff',
          name: 'admin-staff',
          component: AdminStaff,
          meta: { title: '员工管理' }
        },
        {
          path: 'invite-codes',
          name: 'admin-invite-codes',
          component: InviteCodes,
          meta: { title: '邀请码管理' }
        },
        {
          path: 'reception/bookings',
          name: 'reception-bookings',
          component: ReceptionBookings,
          meta: { title: '预订管理' }
        },
        {
          path: 'reception/checkin',
          name: 'reception-checkin',
          component: ReceptionCheckin,
          meta: { title: '入住登记' }
        },
        {
          path: 'reception/checkout',
          name: 'reception-checkout',
          component: ReceptionCheckout,
          meta: { title: '退房管理' }
        },
        {
          path: 'reception/visitors',
          name: 'reception-visitors',
          component: ReceptionVisitors,
          meta: { title: '访客登记' }
        },
        {
          path: 'reception/visitor-records',
          name: 'reception-visitor-records',
          meta: { title: '访客记录' }
        },
        {
          path: 'cleaning/tasks',
          name: 'cleaning-tasks',
          component: CleaningTasks,
          meta: { title: '清洁任务' }
        },
        {
          path: 'cleaning/records',
          name: 'cleaning-records',
          component: CleaningRecords,
          meta: { title: '清洁记录' }
        },
        {
          path: 'rooms',
          name: 'room-management',
          component: AdminRoomManagement,
          meta: { title: '房间管理' }
        }
      ]
    }
  ]
})

// 全局路由守卫
router.beforeEach(async (to, from, next) => {
  console.log(`路由守卫检查: 从 ${from.path} 到 ${to.path}`);

  // Initialize store instance here, as Pinia is now available
  const authStore = useAuthStore();

  // Check if Pinia store is initialized (important for initial load)
  // Pinia state is initialized directly from localStorage in the store definition,
  // so explicit initialization call might not be needed unless async ops are involved.
  // Example: await authStore.initializeAuth(); // If you implement async checks

  console.log(`Auth Store State: LoggedIn=${authStore.isLoggedIn}, Role=${authStore.userRole}`);

  // 如果访问员工注册页面，允许直接访问
  if (to.path === '/admin/register') {
    console.log('允许访问员工注册页面');
    next();
    return;
  }

  // --- REPLACE localStorage checks with authStore checks ---
  // const token = localStorage.getItem('token'); // Removed
  // const userRole = localStorage.getItem('userRole'); // Removed
  const allowedAdminRoles = ['ADMIN', 'RECEPTIONIST', 'CLEANER'];
  const isAdminAuthenticated = authStore.isLoggedIn && allowedAdminRoles.includes(authStore.userRole);
  const isCustomerLoggedIn = authStore.isLoggedIn && authStore.userRole === 'CUSTOMER';

  console.log(`后台认证状态: ${isAdminAuthenticated}, 普通用户登录状态: ${isCustomerLoggedIn}`);

  // 检查是否需要普通用户认证 (如 /user/**)
  if (to.matched.some(record => record.meta.requiresAuth) && !isCustomerLoggedIn) {
    console.log('目标路由需要普通用户认证，但用户未登录，重定向到 /login');
    next({
      path: '/login',
      query: { redirect: to.fullPath } // Preserve redirect query
    });
    return;
  }

  // 检查是否需要后台用户认证 (如 /admin/**)
  if (to.matched.some(record => record.meta.requiresAdminAuth) && !isAdminAuthenticated) {
    console.log('访问后台页面，但后台用户未认证，重定向到 /admin/login');
    next({ path: '/admin/login', query: { redirect: to.fullPath } });
    return; 
  }

  // 如果已登录的普通用户访问登录/注册页，重定向到首页
  if (to.meta.public && isCustomerLoggedIn && (to.name === 'login' || to.name === 'register')) {
      console.log('普通用户已登录，尝试访问公共页面，重定向到首页');
      next({ path: '/' });
      return;
  }

  // 如果已登录的后台用户访问登录页，重定向到 dashboard (或基于角色的页面)
  if (to.path === '/admin/login' && isAdminAuthenticated) {
    console.log('后台用户已登录，从登录页重定向到对应后台首页');
    if (authStore.userRole === 'ADMIN') {
      next('/admin/dashboard');
    } else if (authStore.userRole === 'RECEPTIONIST') {
      next('/admin/reception/bookings');
    } else if (authStore.userRole === 'CLEANER') {
      next('/admin/cleaning/tasks');
    } else {
      next('/admin/dashboard'); // Fallback
    }
    return;
  }

  // 增加：只允许 ADMIN 访问 /admin/dashboard (以及其他权限控制)
  // Note: More granular role-based access should ideally be handled within components or dedicated middleware
  if (to.path === '/admin/dashboard' && isAdminAuthenticated && authStore.userRole !== 'ADMIN') {
    console.log(`非 ADMIN 角色 (${authStore.userRole}) 尝试访问 /admin/dashboard，进行重定向。`);
    if (authStore.userRole === 'RECEPTIONIST') {
      next('/admin/reception/bookings');
    } else if (authStore.userRole === 'CLEANER') {
      next('/admin/cleaning/tasks');
    } else {
      next('/admin/login');
    }
    return;
  }

  // 其他所有情况
  console.log('无需特殊处理或已处理，允许导航');
  next();
});

export default router