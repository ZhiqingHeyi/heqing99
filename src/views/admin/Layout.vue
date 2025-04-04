<template>
  <div class="admin-layout">
    <el-container>
      <!-- 针对保洁人员角色进行特殊处理的移动端垂直布局 -->
      <template v-if="userRole === 'cleaner'">
        <el-container class="cleaner-mobile-layout">
          <!-- 顶部导航栏 -->
          <el-header height="auto" class="cleaner-header">
            <div class="logo-container">
              <img src="../../assets/logo.svg" alt="和庆酒店" class="logo" />
              <span class="title">保洁管理</span>
            </div>
            
            <div class="user-display">
              <el-avatar :size="36" class="user-avatar">{{ username.charAt(0) }}</el-avatar>
              <span class="username">{{ username }}</span>
            </div>
          </el-header>
          
          <!-- 保洁人员菜单 - 直观的大图标按钮 -->
          <div class="cleaner-menu">
            <div class="menu-grid">
              <div 
                class="menu-item" 
                :class="{ active: activeMenu === '/admin/cleaning/tasks' }"
                @click="switchComponent('tasks')"
              >
                <div class="menu-icon">
                  <el-icon><List /></el-icon>
                </div>
                <div class="menu-label">清洁任务</div>
              </div>
              <div 
                class="menu-item"
                :class="{ active: activeMenu === '/admin/cleaning/records' }"
                @click="switchComponent('records')"
              >
                <div class="menu-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="menu-label">清洁记录</div>
              </div>
            </div>
          </div>
                    
          <el-main class="cleaner-main">
            <div class="content-wrapper">
              <component :is="currentComponent" />
            </div>
          </el-main>
          
          <!-- 底部工具栏 -->
          <el-footer height="60px" class="cleaner-footer">
            <div class="footer-content">
              <div class="time-display">
                {{ currentTime }}
              </div>
              <el-button class="logout-btn" size="large" @click="handleLogout">
                <el-icon><Switch /></el-icon>
                退出登录
              </el-button>
            </div>
          </el-footer>
        </el-container>
      </template>
      
      <!-- 其他角色保持原有布局 -->
      <template v-else>
        <el-aside width="240px" class="tech-sidebar">
          <div class="logo-container">
            <img src="../../assets/logo.svg" alt="和庆酒店" class="logo" />
            <span class="title">后台管理系统</span>
          </div>
          <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical tech-menu"
            background-color="#0c1a2b"
            text-color="#8a9ab1"
            active-text-color="#f8f9fc"
          >
            <template v-if="userRole === 'admin'">
              <el-menu-item index="/admin/dashboard" @click="switchComponent('dashboard')" class="tech-menu-item">
                <el-icon><DataLine /></el-icon>
                <span>数据看板</span>
              </el-menu-item>
              <el-menu-item index="/admin/users" @click="switchComponent('users')" class="tech-menu-item">
                <el-icon><User /></el-icon>
                <span>用户管理</span>
              </el-menu-item>
              <el-menu-item index="/admin/staff" @click="switchComponent('staff')" class="tech-menu-item">
                <el-icon><UserFilled /></el-icon>
                <span>员工管理</span>
              </el-menu-item>
              <el-menu-item index="/admin/invite-codes" @click="switchComponent('inviteCodes')" class="tech-menu-item">
                <el-icon><Key /></el-icon>
                <span>邀请码管理</span>
              </el-menu-item>
            </template>

            <template v-if="userRole === 'receptionist'">
              <el-menu-item index="/admin/reception/bookings" @click="switchComponent('bookings')" class="tech-menu-item">
                <el-icon><Calendar /></el-icon>
                <span>预订管理</span>
              </el-menu-item>
              <el-menu-item index="/admin/reception/checkin" @click="switchComponent('checkin')" class="tech-menu-item">
                <el-icon><House /></el-icon>
                <span>入住登记</span>
              </el-menu-item>
              <el-menu-item index="/admin/reception/visitors" @click="switchComponent('visitors')" class="tech-menu-item">
                <el-icon><List /></el-icon>
                <span>访客登记</span>
              </el-menu-item>
            </template>
          </el-menu>
          
          <div class="sidebar-footer">
            <div class="version">V 1.0.0</div>
            <div class="copyright">© 2023 和庆酒店</div>
          </div>
        </el-aside>
        
        <el-container>
          <el-header height="70px" class="tech-header">
            <div class="header-content">
              <div class="breadcrumb">
                <el-breadcrumb separator="/">
                  <el-breadcrumb-item>后台管理</el-breadcrumb-item>
                  <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
                </el-breadcrumb>
              </div>
              
              <div class="header-controls">
                <div class="time-display">
                  {{ currentTime }}
                </div>
                <el-button class="notification-btn" icon="el-icon-bell" circle></el-button>
                <div class="user-info">
                  <el-dropdown trigger="click">
                    <span class="user-dropdown">
                      <el-avatar :size="32" class="user-avatar">{{ username.charAt(0) }}</el-avatar>
                      <span class="username">{{ username }}</span>
                      <el-icon class="el-icon--right"><arrow-down /></el-icon>
                    </span>
                    <template #dropdown>
                      <el-dropdown-menu class="user-dropdown-menu">
                        <el-dropdown-item>
                          <el-icon><User /></el-icon>个人信息
                        </el-dropdown-item>
                        <el-dropdown-item>
                          <el-icon><Setting /></el-icon>系统设置
                        </el-dropdown-item>
                        <el-dropdown-item divided @click="handleLogout">
                          <el-icon><Switch /></el-icon>退出登录
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
            </div>
          </el-header>
          
          <el-main class="tech-main">
            <div class="content-wrapper">
              <!-- 使用动态组件渲染 -->
              <component :is="currentComponent" />
            </div>
          </el-main>
        </el-container>
      </template>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, shallowRef, markRaw, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { DataLine, User, UserFilled, Calendar, House, List, Document, ArrowDown, Key, Setting, Switch } from '@element-plus/icons-vue'

// 导入组件
import Dashboard from './Dashboard.vue'
import InviteCodes from './InviteCodes.vue'
import Staff from './Staff.vue'
import Users from './Users.vue'
// 导入前台接待组件
import Bookings from './reception/Bookings.vue'
import Checkin from './reception/Checkin.vue'
import Visitors from './reception/Visitors.vue'
// 导入保洁组件
import CleaningTasks from './cleaning/Tasks.vue'
import CleaningRecords from './cleaning/Records.vue'

const router = useRouter()

// 从登录状态获取用户数据
const userRole = ref(localStorage.getItem('userRole') || 'admin')
const username = ref(localStorage.getItem('username') || '管理员')

// 当前激活的菜单
const activeMenu = ref('/admin/dashboard')

// 当前显示的组件
const currentComponent = shallowRef(null)

// 当前页面标题
const currentPageTitle = ref('数据看板')

// 当前时间显示
const currentTime = ref('')
let timeInterval = null

// 格式化当前时间
const updateTime = () => {
  const now = new Date()
  const options = {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }
  currentTime.value = new Intl.DateTimeFormat('zh-CN', options).format(now)
}

// 页面标题映射
const pageTitles = {
  'dashboard': '数据看板',
  'users': '用户管理',
  'staff': '员工管理',
  'inviteCodes': '邀请码管理',
  // 前台相关页面
  'bookings': '预订管理',
  'checkin': '入住登记',
  'visitors': '访客登记',
  // 保洁相关页面
  'tasks': '清洁任务',
  'records': '清洁记录'
}

// 组件映射
const componentMap = {
  'dashboard': markRaw(Dashboard),
  'users': markRaw(Users),
  'staff': markRaw(Staff),
  'inviteCodes': markRaw(InviteCodes),
  // 前台相关组件
  'bookings': markRaw(Bookings),
  'checkin': markRaw(Checkin),
  'visitors': markRaw(Visitors),
  // 保洁相关组件
  'tasks': markRaw(CleaningTasks),
  'records': markRaw(CleaningRecords)
}

// 切换组件
const switchComponent = (componentName) => {
  console.log('切换到组件:', componentName)
  currentComponent.value = componentMap[componentName]
  currentPageTitle.value = pageTitles[componentName]

  // 更新激活菜单
  const pathMap = {
    'dashboard': '/admin/dashboard',
    'users': '/admin/users',
    'staff': '/admin/staff',
    'inviteCodes': '/admin/invite-codes',
    // 前台相关路径
    'bookings': '/admin/reception/bookings',
    'checkin': '/admin/reception/checkin',
    'visitors': '/admin/reception/visitors',
    // 保洁相关路径
    'tasks': '/admin/cleaning/tasks',
    'records': '/admin/cleaning/records'
  }
  activeMenu.value = pathMap[componentName]

  // 同步更新路由，但不触发实际导航
  const path = pathMap[componentName]
  router.push(path).catch(() => {})
}

// 处理退出登录
const handleLogout = () => {
  // 清除登录状态
  localStorage.removeItem('userRole')
  localStorage.removeItem('username')
  localStorage.removeItem('token')
  // 重定向到登录页面
  router.push('/admin/login?logout=true')
}

// 组件挂载时设置初始组件和开始时间更新
onMounted(() => {
  // 初始化时间显示
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
  
  // 根据当前URL路径决定显示哪个组件
  const path = window.location.pathname
  console.log('当前路径:', path)
  
  if (path.includes('/admin/invite-codes')) {
    switchComponent('inviteCodes')
  } else if (path.includes('/admin/users')) {
    switchComponent('users')
  } else if (path.includes('/admin/staff')) {
    switchComponent('staff')
  } else if (path.includes('/admin/reception/bookings')) {
    switchComponent('bookings')
  } else if (path.includes('/admin/reception/checkin')) {
    switchComponent('checkin')
  } else if (path.includes('/admin/reception/visitors')) {
    switchComponent('visitors')
  } else if (path.includes('/admin/cleaning/tasks')) {
    switchComponent('tasks')
  } else if (path.includes('/admin/cleaning/records')) {
    switchComponent('records')
  } else {
    // 默认显示对应角色的首页
    if (userRole.value === 'receptionist') {
      switchComponent('bookings')
    } else if (userRole.value === 'cleaner') {
      switchComponent('tasks')
    } else {
      // 管理员默认显示数据看板
      switchComponent('dashboard')
    }
  }
})

// 组件卸载前清除定时器
onBeforeUnmount(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
}

.tech-sidebar {
  background-color: #0c1a2b;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.logo-container {
  display: flex;
  align-items: center;
  padding: 20px 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.logo {
  width: 32px;
  height: 32px;
  margin-right: 10px;
}

.title {
  color: #fff;
  font-size: 18px;
  font-weight: 500;
}

.tech-menu {
  border-right: none;
}

.tech-menu-item {
  margin: 8px 0;
}

.tech-header {
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin: 0 8px;
  font-size: 14px;
}

.tech-main {
  background-color: #f5f7fa;
  overflow-y: auto;
  padding: 20px;
}

.content-wrapper {
  max-width: 1600px;
  margin: 0 auto;
}

.sidebar-footer {
  margin-top: auto;
  padding: 15px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  text-align: center;
}

.version {
  color: #586a88;
  font-size: 12px;
  margin-bottom: 5px;
}

.copyright {
  color: #586a88;
  font-size: 12px;
}

.time-display {
  font-size: 14px;
  color: #606266;
}

.user-avatar {
  background-color: #409eff;
  color: #fff;
}

/* 保洁人员专用样式 - 移动端垂直布局 */
.cleaner-mobile-layout {
  min-height: 100vh;
  height: auto;
  display: flex;
  flex-direction: column;
}

.cleaner-header {
  background-color: #0c1a2b;
  color: #fff;
  padding: 12px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.cleaner-header .logo-container {
  border-bottom: none;
  padding: 0;
}

.cleaner-header .title {
  font-size: 20px;
}

.user-display {
  display: flex;
  align-items: center;
}

.user-display .username {
  color: #fff;
  font-size: 16px;
}

.cleaner-menu {
  padding: 15px;
  background-color: #f8f9fa;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.menu-item {
  background-color: #fff;
  border-radius: 10px;
  padding: 15px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
  height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.menu-item.active {
  background-color: #e6f1ff;
  border: 2px solid #409eff;
}

.menu-icon {
  font-size: 40px;
  color: #409eff;
  margin-bottom: 10px;
}

.menu-icon .el-icon {
  font-size: 40px;
}

.menu-label {
  font-size: 18px;
  color: #303133;
  font-weight: 500;
}

.cleaner-main {
  flex: 1;
  padding: 0;
  background-color: #f5f7fa;
  overflow-y: auto;
}

.cleaner-footer {
  background-color: #fff;
  border-top: 1px solid #f0f0f0;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
  padding: 10px 15px;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logout-btn {
  font-size: 16px;
}

@media screen and (max-width: 768px) {
  .menu-item {
    height: 100px;
  }
  
  .menu-icon {
    font-size: 30px;
  }
  
  .menu-label {
    font-size: 16px;
  }
}
</style> 