<template>
  <div class="admin-layout">
    <el-container>
      <el-aside width="200px">
        <div class="logo-container">
          <img src="../../assets/logo.svg" alt="和庆酒店" class="logo" />
          <span class="title">后台管理</span>
        </div>
        <el-menu
          :default-active="$route.path"
          class="el-menu-vertical"
          :router="true"
        >
          <template v-if="userRole === 'admin'">
            <el-menu-item index="/admin/dashboard">
              <el-icon><DataLine /></el-icon>
              <span>数据看板</span>
            </el-menu-item>
            <el-menu-item index="/admin/users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/staff">
              <el-icon><UserFilled /></el-icon>
              <span>员工管理</span>
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
                <el-breadcrumb-item>{{ currentPage }}</el-breadcrumb-item>
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
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { DataLine, User, UserFilled, Calendar, House, List, Document, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 从登录状态获取用户数据
const userRole = ref(localStorage.getItem('userRole') || 'admin')
const username = ref(localStorage.getItem('username') || '管理员')

const currentPage = computed(() => {
  const pathMap = {
    '/admin/dashboard': '数据看板',
    '/admin/users': '用户管理',
    '/admin/staff': '员工管理',
    '/admin/reception/bookings': '预订管理',
    '/admin/reception/checkin': '入住登记',
    '/admin/reception/visitors': '访客登记',
    '/admin/cleaning/tasks': '清洁任务',
    '/admin/cleaning/records': '清洁记录'
  }
  return pathMap[route.path] || '首页'
})

const handleLogout = () => {
  // TODO: 实现登出逻辑
  router.push('/admin/login')
}
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