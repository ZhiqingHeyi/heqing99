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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

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

  try {
    await loginFormRef.value.validate()
    loading.value = true

    const loginData = {
      username: loginForm.username,
      password: loginForm.password,
      role: loginForm.role
    }
    
    console.log('正在提交登录信息:', loginData)

    // 发送登录请求
    // 根据当前环境确定API请求路径
    const baseUrl = window.location.port === '3000' ? '/api' : '';
    const response = await fetch(`${baseUrl}/admin/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(loginData)
    })

    console.log('服务器响应状态:', response.status)

    // 获取响应文本
    const responseText = await response.text()
    console.log('服务器响应内容:', responseText)

    if (!responseText) {
      ElMessage.error('服务器返回空响应')
      return
    }

    // 解析JSON
    let data
    try {
      data = JSON.parse(responseText)
      console.log('解析后的JSON数据:', data)
    } catch (e) {
      console.error('解析响应JSON失败:', e)
      ElMessage.error('服务器响应格式错误：' + e.message)
      return
    }

    if (data && data.success === true) {
      // 存储用户信息
      const userData = data.data || {}
      console.log('用户数据:', userData)
      
      localStorage.setItem('token', userData.token || '')
      localStorage.setItem('userRole', userData.role || '')
      localStorage.setItem('username', userData.username || '')
      localStorage.setItem('userId', userData.userId ? userData.userId.toString() : '')
      if (userData.name) {
        localStorage.setItem('name', userData.name)
      }
      
      ElMessage.success(data.message || '登录成功')

      // 根据角色跳转到不同的页面
      const role = userData.role || ''
      console.log('用户角色:', role)
      
      // 设置延迟，确保localStorage已经更新
      setTimeout(() => {
        try {
          switch (role) {
            case 'admin':
              console.log('正在跳转到管理员面板...')
              router.push('/admin/dashboard')
              break
            case 'receptionist':
              console.log('正在跳转到前台面板...')
              router.push('/admin/reception/bookings')
              break
            case 'cleaner':
              console.log('正在跳转到清洁人员面板...')
              router.push('/admin/cleaning/tasks')
              break
            default:
              console.warn('未知角色:', role)
              router.push('/admin/dashboard')
          }
        } catch (error) {
          console.error('路由跳转错误:', error)
        }
      }, 100)
    } else {
      console.error('登录失败:', data ? data.message : '未知错误')
      ElMessage.error(data && data.message ? data.message : '登录失败')
    }
  } catch (error) {
    console.error('登录请求异常:', error)
    ElMessage.error('登录请求失败: ' + error.message)
  } finally {
    loading.value = false
  }
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