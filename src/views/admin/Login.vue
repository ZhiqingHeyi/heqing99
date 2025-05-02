<template>
  <div class="admin-login-container">
    <el-card class="login-card">
      <div class="login-header">
        <div class="logo-container">
          <img src="@/assets/logo.svg" alt="Logo" class="logo" />
        </div>
        <h2>管理系统登录</h2>
      </div>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="0"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="role">
          <el-select
            v-model="loginForm.role"
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option label="管理员" value="admin" />
            <el-option label="前台" value="receptionist" />
            <el-option label="清洁人员" value="cleaner" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { userApi } from '@/api'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const route = useRoute()
const loginFormRef = ref(null)
const loading = ref(false)
const authStore = useAuthStore()

// 当组件挂载时检查登录状态
onMounted(() => {
  // 检查URL参数，如果有logout参数，则清除所有登录状态
  if (route.query.logout === 'true') {
    clearLoginState()
    ElMessage.success('您已成功退出登录')
  }
  
  // 如果从其他页面被重定向到登录页，可能是因为登录状态过期
  if (route.query.redirect) {
    const token = localStorage.getItem('token')
    const userRole = localStorage.getItem('userRole')
    
    // 如果本地存储仍有登录信息，但被重定向到登录页，说明会话可能已过期
    if (token || userRole) {
      clearLoginState()
      ElMessage.warning('您的登录状态已过期，请重新登录')
    }
  }
})

// 清除登录状态的函数
const clearLoginState = () => {
  authStore.logout()
}

const loginForm = reactive({
  username: '',
  password: '',
  role: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 调用统一的用户登录API，不再传递 isAdmin
        const response = await userApi.login({
          username: loginForm.username,
          password: loginForm.password
        })
        
        // 允许 ADMIN, RECEPTIONIST, CLEANER 登录后台
        const allowedRoles = ['ADMIN', 'RECEPTIONIST', 'CLEANER'];

        if (response.success && response.role && allowedRoles.includes(response.role.toUpperCase())) {
          // 3. Call authStore.login()
          authStore.login({
            token: response.token,
            userId: response.userId,
            username: response.username,
            role: response.role.toUpperCase() // Ensure role is uppercase for store
          });
          
          ElMessage.success('登录成功');
          router.push('/admin/dashboard');
        } else if (response.success && response.role && !allowedRoles.includes(response.role.toUpperCase())) {
          // 登录成功但角色不允许登录后台
          ElMessage.error('该角色账号无权登录管理后台');
        } else {
          // 登录失败 (response.success 为 false 或缺少 role)
          ElMessage.error(response?.message || '登录失败，请检查用户名和密码');
        }
      } catch (error) {
        console.error('管理员登录失败:', error);
        // 错误处理保持不变，axios 拦截器会处理错误消息
        ElMessage.error(error.message || '登录失败，请检查网络连接或服务器状态');
      } finally {
        loading.value = false;
      }
    }
  })
}
</script>

<style scoped>
.admin-login-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  border-radius: 15px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo-container {
  margin-bottom: 20px;
}

.logo {
  width: 80px;
  height: 80px;
}

.login-header h2 {
  color: #333;
  font-size: 24px;
  margin: 0;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

:deep(.el-button) {
  height: 40px;
  font-size: 16px;
}

:deep(.el-form-item) {
  margin-bottom: 25px;
}

:deep(.el-input__inner) {
  height: 40px;
}

:deep(.el-select) {
  width: 100%;
}
</style>