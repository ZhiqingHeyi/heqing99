<template>
  <div class="admin-layout">
    <el-container>
      <el-aside width="200px">
        <div class="logo-container">
          <img src="../../assets/logo.svg" alt="和庆酒店" class="logo" />
          <span class="title">后台管理</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical"
        >
          <template v-if="userRole === 'admin'">
            <el-menu-item index="/admin/dashboard" @click="switchComponent('dashboard')">
              <el-icon><DataLine /></el-icon>
              <span>数据看板</span>
            </el-menu-item>
            <el-menu-item index="/admin/users" @click="switchComponent('users')">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/staff" @click="switchComponent('staff')">
              <el-icon><UserFilled /></el-icon>
              <span>员工管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/invite-codes" @click="switchComponent('inviteCodes')">
              <el-icon><Key /></el-icon>
              <span>邀请码管理</span>
            </el-menu-item>
          </template>

          <template v-if="userRole === 'receptionist'">
            <el-menu-item index="/admin/reception/bookings">
              <el-icon><Calendar /></el-icon>
              <span>预订管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/reception/checkin">
              <el-icon><House /></el-icon>
              <span>入住登记</span>
            </el-menu-item>
            <el-menu-item index="/admin/reception/visitors">
              <el-icon><List /></el-icon>
              <span>访客登记</span>
            </el-menu-item>
          </template>

          <template v-if="userRole === 'cleaner'">
            <el-menu-item index="/admin/cleaning/tasks">
              <el-icon><List /></el-icon>
              <span>清洁任务</span>
            </el-menu-item>
            <el-menu-item index="/admin/cleaning/records">
              <el-icon><Document /></el-icon>
              <span>清洁记录</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header height="60px">
          <div class="header-content">
            <div class="breadcrumb">
              <el-breadcrumb>
                <el-breadcrumb-item>后台管理</el-breadcrumb-item>
                <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
              </el-breadcrumb>
            </div>
            <div class="user-info">
              <el-dropdown>
                <span class="user-dropdown">
                  {{ username }}
                  <el-icon class="el-icon--right"><arrow-down /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>
        
        <el-main>
          <!-- 使用动态组件渲染 -->
          <component :is="currentComponent" />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, shallowRef, markRaw, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { DataLine, User, UserFilled, Calendar, House, List, Document, ArrowDown, Key } from '@element-plus/icons-vue'

// 导入组件
import Dashboard from './Dashboard.vue'
import InviteCodes from './InviteCodes.vue'
import Staff from './Staff.vue'
import Users from './Users.vue'

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

// 页面标题映射
const pageTitles = {
  'dashboard': '数据看板',
  'users': '用户管理',
  'staff': '员工管理',
  'inviteCodes': '邀请码管理'
}

// 组件映射
const componentMap = {
  'dashboard': markRaw(Dashboard),
  'users': markRaw(Users),
  'staff': markRaw(Staff),
  'inviteCodes': markRaw(InviteCodes)
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
    'inviteCodes': '/admin/invite-codes'
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

// 组件挂载时设置初始组件
onMounted(() => {
  // 根据当前URL路径决定显示哪个组件
  const path = window.location.pathname
  console.log('当前路径:', path)
  
  if (path.includes('/admin/invite-codes')) {
    switchComponent('inviteCodes')
  } else if (path.includes('/admin/users')) {
    switchComponent('users')
  } else if (path.includes('/admin/staff')) {
    switchComponent('staff')
  } else {
    // 默认显示数据看板
    switchComponent('dashboard')
  }
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  background-color: #001529;
  color: white;
}

.logo {
  width: 32px;
  height: 32px;
  margin-right: 8px;
}

.title {
  font-size: 16px;
  font-weight: bold;
}

.el-menu-vertical {
  height: calc(100vh - 60px);
  border-right: none;
}

.el-header {
  background-color: white;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}

.header-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-dropdown {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.el-main {
  background-color: #f5f7fa;
  padding: 20px;
}
</style> 