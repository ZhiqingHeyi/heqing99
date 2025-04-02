<template>
  <div class="app-container">
    <template v-if="!isAdminRoute">
      <header class="header">
        <el-menu
          mode="horizontal"
          :ellipsis="false"
          class="nav-menu"
          router
        >
          <div class="logo-container">
            <img src="./assets/logo.svg" alt="和庆酒店" class="logo" />
            <span class="hotel-name">和庆酒店</span>
          </div>
          <div class="flex-grow" />
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/about">关于我们</el-menu-item>
          <el-menu-item index="/rooms">客房预订</el-menu-item>
          <el-menu-item index="/contact">联系我们</el-menu-item>
          <el-button type="primary" class="book-now-btn" @click="$router.push('/booking')">立即预订</el-button>
        </el-menu>
      </header>
      <main class="main-content">
        <router-view></router-view>
      </main>
    </template>
    <template v-else>
      <router-view></router-view>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isAdminRoute = computed(() => route.path.startsWith('/admin'))
</script>

<style>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.nav-menu {
  display: flex;
  align-items: center;
  height: 60px;
  padding: 0 20px;
}

.logo-container {
  display: flex;
  align-items: center;
  margin-right: 40px;
}

.logo {
  height: 40px;
  margin-right: 10px;
}

.hotel-name {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.flex-grow {
  flex-grow: 1;
}

.book-now-btn {
  margin-left: 20px;
}

.main-content {
  padding-top: 60px;
}
</style>