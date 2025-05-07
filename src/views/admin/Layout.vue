<template>
  <div class="admin-layout">
    <el-container>
      <!-- Cleaner layout (assuming it might need separate logic or components later) -->
      <template v-if="userRole === 'CLEANER'"> 
        <el-container class="cleaner-mobile-layout">
          <el-header height="auto" class="cleaner-header"> 
            <div class="logo-container">
              <img src="../../assets/logo.svg" alt="鹤清酒店" class="logo" />
              <span class="title">保洁管理</span>
            </div>
            <div class="user-display">
              <el-avatar :size="36" class="user-avatar">{{ username?.charAt(0) }}</el-avatar>
              <span class="username">{{ username }}</span>
            </div>
          </el-header>
          <div class="cleaner-menu">
            <div class="menu-grid">
              <div 
                class="menu-item" 
                :class="{ active: activeMenu === '/admin/cleaning/tasks' }"
                @click="navigateTo('/admin/cleaning/tasks')"
              >
                <div class="menu-icon"><el-icon><List /></el-icon></div>
                <div class="menu-label">清洁任务</div>
              </div>
              <div 
                class="menu-item"
                :class="{ active: activeMenu === '/admin/cleaning/records' }"
                @click="navigateTo('/admin/cleaning/records')"
              >
                <div class="menu-icon"><el-icon><Document /></el-icon></div>
                <div class="menu-label">清洁记录</div>
              </div>
            </div>
          </div>
          <el-main class="cleaner-main">
            <div class="content-wrapper">
              <!-- Use router-view for cleaner role as well -->
              <router-view v-slot="{ Component }">
                <keep-alive> 
                  <component :is="Component" />
                </keep-alive>
              </router-view>
            </div>
          </el-main>
          <el-footer height="60px" class="cleaner-footer">
            <div class="footer-content">
              <div class="time-display">{{ currentTime }}</div>
              <el-button class="logout-btn" size="large" @click="handleLogout">
                <el-icon><Switch /></el-icon>退出登录
              </el-button>
            </div>
          </el-footer>
        </el-container>
      </template>
      
      <!-- Standard layout for Admin and Receptionist -->
      <template v-else>
        <el-aside width="240px" class="tech-sidebar">
          <div class="logo-container">
            <img src="../../assets/logo.svg" alt="鹤清酒店" class="logo" />
            <span class="title">后台管理系统</span>
          </div>
          <el-menu
            :default-active="activeMenu" 
            class="el-menu-vertical tech-menu"
            background-color="#0c1a2b"
            text-color="#8a9ab1"
            active-text-color="#f8f9fc"
            router  
          >
            <!-- Admin Menu -->
            <template v-if="userRole === 'ADMIN'">
              <el-menu-item index="/admin/dashboard" class="tech-menu-item">
                <el-icon><DataLine /></el-icon><span>数据看板</span>
              </el-menu-item>
              <el-menu-item index="/admin/users" class="tech-menu-item">
                <el-icon><User /></el-icon><span>用户管理</span>
              </el-menu-item>
              <el-menu-item index="/admin/staff" class="tech-menu-item">
                <el-icon><UserFilled /></el-icon><span>员工管理</span>
              </el-menu-item>
              <el-menu-item index="/admin/rooms" class="tech-menu-item">
                <el-icon><HomeFilled /></el-icon><span>房间管理</span>
              </el-menu-item>
              <el-menu-item index="/admin/invite-codes" class="tech-menu-item">
                <el-icon><Key /></el-icon><span>邀请码管理</span>
              </el-menu-item>
            </template>

            <!-- Receptionist Menu -->
            <template v-if="userRole === 'RECEPTIONIST'">
              <el-menu-item index="/admin/reception/bookings" class="tech-menu-item">
                <el-icon><Calendar /></el-icon><span>预订管理</span>
              </el-menu-item>
              <el-menu-item index="/admin/reception/checkin" class="tech-menu-item">
                <el-icon><House /></el-icon><span>入住登记</span>
              </el-menu-item>
              <el-menu-item index="/admin/reception/checkout" class="tech-menu-item">
                <el-icon><Right /></el-icon><span>退房管理</span>
              </el-menu-item>
              <el-menu-item index="/admin/reception/visitors" class="tech-menu-item">
                <el-icon><List /></el-icon><span>访客登记</span>
              </el-menu-item>
            </template>
          </el-menu>
          
          <div class="sidebar-footer">
            <div class="version">V 1.0.0</div>
            <div class="copyright">© 2025 鹤清酒店</div>
          </div>
        </el-aside>
        
        <el-container class="main-area-container">
          <el-header height="70px" class="tech-header">
            <div class="header-content">
              <div class="breadcrumb">
                <el-breadcrumb separator="/">
                  <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">后台管理</el-breadcrumb-item>
                  <!-- Use route meta title for breadcrumb -->
                  <el-breadcrumb-item>{{ currentRouteTitle }}</el-breadcrumb-item> 
                </el-breadcrumb>
              </div>
              <div class="header-controls">
                 <div class="time-display">{{ currentTime }}</div>
                 <el-button class="notification-btn" type="primary" text circle><el-icon><Bell /></el-icon></el-button>
                 <div class="user-info">
                   <el-dropdown trigger="click">
                     <span class="user-dropdown">
                       <el-avatar :size="36" class="user-avatar">{{ username?.charAt(0) }}</el-avatar>
                       <span class="username">{{ username }}</span>
                       <el-icon class="el-icon--right"><arrow-down /></el-icon>
                     </span>
                     <template #dropdown>
                       <el-dropdown-menu class="user-dropdown-menu">
                         <el-dropdown-item @click="showPersonalInfo"><el-icon><User /></el-icon>个人信息</el-dropdown-item>
                         <el-dropdown-item @click="showSystemSettings"><el-icon><Setting /></el-icon>系统设置</el-dropdown-item>
                         <el-dropdown-item divided @click="handleLogout"><el-icon><Switch /></el-icon>退出登录</el-dropdown-item>
                       </el-dropdown-menu>
                     </template>
                   </el-dropdown>
                 </div>
               </div>
            </div>
          </el-header>
          
          <el-main class="tech-main">
            <div class="content-wrapper">
              <!-- Standard Router View -->
              <router-view v-slot="{ Component }">
                <keep-alive> 
                  <component :is="Component" />
                </keep-alive>
              </router-view>
            </div>
          </el-main>
        </el-container>
      </template>
    </el-container>

    <!-- Dialogs remain the same -->
    <el-dialog v-model="personalInfoDialogVisible" title="个人信息" width="30%">
      <span>这里是管理员 {{ username }} 的个人信息占位符。</span>
      <template #footer><span class="dialog-footer"><el-button @click="personalInfoDialogVisible = false">关闭</el-button></span></template>
    </el-dialog>
    <el-dialog v-model="systemSettingsDialogVisible" title="系统设置" width="30%">
      <span>这里是系统设置占位符。功能开发中...</span>
      <template #footer><span class="dialog-footer"><el-button @click="systemSettingsDialogVisible = false">关闭</el-button></span></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue' // Removed shallowRef, markRaw
import { useRoute, useRouter } from 'vue-router' // Added useRoute
import { DataLine, User, UserFilled, Calendar, House, List, Document, ArrowDown, Key, Setting, Switch, Bell, HomeFilled, Right } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth' // 1. Import Auth Store

// --- Removed component imports, they are loaded via router ---
// import Dashboard from './Dashboard.vue'
// ... other component imports removed ...

const router = useRouter()
const route = useRoute() // Get current route instance
const authStore = useAuthStore() // 2. Get Auth Store instance

// Dialog visibility
const personalInfoDialogVisible = ref(false)
const systemSettingsDialogVisible = ref(false)

// User info from localStorage
const userRole = ref(localStorage.getItem('userRole') || 'admin')
const username = ref(localStorage.getItem('username') || '管理员')

// --- activeMenu is now computed based on current route --- 
const activeMenu = computed(() => route.path)

// --- currentComponent and related logic removed --- 
// const currentComponent = shallowRef(null)
// const componentMap = { ... }
// const switchComponent = (componentName) => { ... }

// --- currentPageTitle is now computed based on route meta ---
const currentRouteTitle = computed(() => route.meta?.title || '管理页面')

// Time display
const currentTime = ref('')
let timeInterval = null

const updateTime = () => {
  const now = new Date()
  const options = { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false }
  currentTime.value = new Intl.DateTimeFormat('zh-CN', options).format(now)
}

// Navigate function for cleaner role buttons
const navigateTo = (path) => {
  router.push(path)
}

// Logout handler
const handleLogout = () => {
  authStore.logout(); // 3. Call authStore.logout()
  // 4. Remove localStorage.removeItem calls
  // localStorage.removeItem('userRole')
  // localStorage.removeItem('username')
  // localStorage.removeItem('token')
  // localStorage.removeItem('userId')
  // localStorage.removeItem('name')
  router.push('/admin/login?logout=true'); // 5. Keep router.push
}

// Show dialogs
const showPersonalInfo = () => { personalInfoDialogVisible.value = true }
const showSystemSettings = () => { systemSettingsDialogVisible.value = true }

// Component mounted: start timer
onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
  // --- Removed logic that manually switched components based on path ---
})

// Component unmounted: clear timer
onBeforeUnmount(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style scoped>
/* Styles remain largely the same, maybe minor adjustments needed */
.admin-layout {
  height: 100vh;
  background-color: #f5f7fa;
}

/* Styles for cleaner layout */
.cleaner-mobile-layout { /* ... */ }
.cleaner-header { /* ... */ }
.cleaner-menu { /* ... */ }
.menu-grid { /* ... */ }
.menu-item { /* ... */ }
.menu-item.active { /* ... */ }
.menu-icon { /* ... */ }
.menu-label { /* ... */ }
.cleaner-main { /* ... */ }
.cleaner-footer { /* ... */ }
.footer-content { /* ... */ }
.time-display { /* ... */ }
.logout-btn { /* ... */ }

/* Styles for standard tech layout */
.tech-sidebar {
  background-color: #0c1a2b;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  box-shadow: 4px 0 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
}

.logo-container { /* ... */ }
.logo { /* ... */ }
.title { /* ... */ }

.tech-menu {
  border-right: none;
  padding: 10px 0;
  flex-grow: 1; /* Allow menu to grow */
}

.tech-menu-item {
  margin: 8px 0;
  height: 52px;
  line-height: 52px;
  border-radius: 0;
  transition: all 0.3s ease;
}

.tech-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.05) !important;
}

.sidebar-footer {
  padding: 20px;
  text-align: center;
  color: #606a7e;
  font-size: 12px;
  margin-top: auto; /* Push footer to bottom */
}

.version { /* ... */ }
.copyright { /* ... */ }

.tech-header {
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  padding: 0 20px;
  flex-shrink: 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.breadcrumb { /* ... */ }

.header-controls {
  display: flex;
  align-items: center;
}

.notification-btn { /* ... */ }
.user-info { /* ... */ }
.user-dropdown { /* ... */ }
.user-avatar { /* ... */ }
.username { /* ... */ }
.user-dropdown-menu { /* ... */ }

.tech-main {
  background-color: #f5f7fa; 
  padding: 20px;
  overflow-y: auto; /* Allow main content to scroll */
  flex: 1;
}

.content-wrapper {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* Add styles for cleaner layout elements if needed */
.cleaner-mobile-layout { display: flex; flex-direction: column; height: 100vh; }
.cleaner-header { background: #fff; padding: 10px 15px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.cleaner-menu { padding: 15px; background: #f9f9f9; }
.menu-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(100px, 1fr)); gap: 15px; }
.menu-item { background: #fff; border-radius: 8px; padding: 15px; text-align: center; cursor: pointer; transition: all 0.2s ease; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
.menu-item:hover { transform: translateY(-3px); box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
.menu-item.active { background: #409EFF; color: #fff; }
.menu-icon { font-size: 28px; margin-bottom: 8px; }
.menu-label { font-size: 14px; }
.cleaner-main { flex-grow: 1; padding: 15px; overflow-y: auto; }
.cleaner-footer { background: #fff; border-top: 1px solid #eee; }
.footer-content { display: flex; justify-content: space-between; align-items: center; height: 100%; padding: 0 15px; }

/* New style for the main area container */
.main-area-container {
  margin-left: 240px; /* Match sidebar width */
  height: 100vh;
  display: flex;
  flex-direction: column;
}

</style> 