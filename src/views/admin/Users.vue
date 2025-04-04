<template>
  <div class="users-container">
    <div class="page-header">
      <div class="header-content">
        <h2><span class="gradient-text">用户管理</span></h2>
        <p class="header-description">管理系统用户账号、权限和状态</p>
      </div>
      <el-button type="primary" @click="handleAdd" class="add-button">
        <el-icon class="button-icon"><Plus /></el-icon>添加用户
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable prefix-icon="Search" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable prefix-icon="Iphone" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="search-button">
            <el-icon class="button-icon"><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetSearch" class="reset-button">
            <el-icon class="button-icon"><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="list-card">
      <div class="list-header">
        <div class="list-title">用户列表</div>
        <div class="list-summary">共 <span class="highlight-text">{{ total }}</span> 位用户</div>
      </div>
      
      <el-table 
        :data="userList" 
        style="width: 100%" 
        v-loading="loading"
        border
        stripe
        highlight-current-row
        class="user-table"
      >
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="createTime" label="注册时间" min-width="160" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.status === 'active' ? 'success' : 'danger'"
              effect="dark"
              class="status-tag"
            >
              {{ row.status === 'active' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button type="primary" link @click="handleEdit(row)" class="action-button">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-button type="warning" link @click="handleResetPassword(row)" class="action-button">
                <el-icon><Key /></el-icon>重置密码
              </el-button>
              <el-button 
                :type="row.status === 'active' ? 'danger' : 'success'"
                link 
                @click="handleToggleStatus(row)"
                class="action-button"
              >
                <el-icon><component :is="row.status === 'active' ? 'Lock' : 'Unlock'" /></el-icon>
                {{ row.status === 'active' ? '禁用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </el-card>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
      v-model="dialogVisible"
      width="500px"
      class="custom-dialog"
      destroy-on-close
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="100px"
        class="custom-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false" class="cancel-button">取消</el-button>
          <el-button type="primary" @click="handleSubmit" class="confirm-button">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      title="重置密码"
      v-model="resetPasswordVisible"
      width="400px"
      class="custom-dialog"
      destroy-on-close
    >
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordRules"
        label-width="100px"
        class="custom-form"
      >
        <el-form-item label="新密码" prop="password">
          <el-input v-model="resetPasswordForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPasswordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetPasswordVisible = false" class="cancel-button">取消</el-button>
          <el-button type="primary" @click="handleResetPasswordSubmit" class="confirm-button">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Key, Lock, Unlock, Iphone } from '@element-plus/icons-vue'

// 搜索表单
const searchForm = reactive({
  username: '',
  phone: '',
  status: ''
})

// 用户列表数据
const loading = ref(false)
const userList = ref([
  {
    username: 'user1',
    name: '张三',
    phone: '13800138000',
    email: 'zhangsan@example.com',
    createTime: '2024-03-31 10:00:00',
    status: 'active'
  },
  // 更多模拟数据...
])

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)

// 表单对话框
const dialogVisible = ref(false)
const dialogType = ref('add')
const userFormRef = ref(null)
const userForm = reactive({
  username: '',
  name: '',
  phone: '',
  email: '',
  password: ''
})

// 表单验证规则
const userFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ]
}

// 重置密码
const resetPasswordVisible = ref(false)
const resetPasswordFormRef = ref(null)
const resetPasswordForm = reactive({
  userId: '',
  password: '',
  confirmPassword: ''
})

const resetPasswordRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetPasswordForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchUserList()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    // TODO: 调用后端API获取用户列表
    await new Promise(resolve => setTimeout(resolve, 1000))
    loading.value = false
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
    loading.value = false
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchUserList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchUserList()
}

// 添加用户
const handleAdd = () => {
  dialogType.value = 'add'
  Object.keys(userForm).forEach(key => {
    userForm[key] = ''
  })
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.keys(userForm).forEach(key => {
    if (key !== 'password') {
      userForm[key] = row[key]
    }
  })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // TODO: 调用后端API保存用户信息
        await new Promise(resolve => setTimeout(resolve, 1000))
        ElMessage.success(dialogType.value === 'add' ? '添加成功' : '修改成功')
        dialogVisible.value = false
        fetchUserList()
      } catch (error) {
        console.error('保存用户信息失败:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

// 重置密码
const handleResetPassword = (row) => {
  resetPasswordForm.userId = row.id
  resetPasswordForm.password = ''
  resetPasswordForm.confirmPassword = ''
  resetPasswordVisible.value = true
}

const handleResetPasswordSubmit = async () => {
  if (!resetPasswordFormRef.value) return
  
  await resetPasswordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // TODO: 调用后端API重置密码
        await new Promise(resolve => setTimeout(resolve, 1000))
        ElMessage.success('密码重置成功')
        resetPasswordVisible.value = false
      } catch (error) {
        console.error('重置密码失败:', error)
        ElMessage.error('重置密码失败')
      }
    }
  })
}

// 启用/禁用用户
const handleToggleStatus = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.status === 'active' ? '禁用' : '启用'}该用户吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // TODO: 调用后端API修改用户状态
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success(`${row.status === 'active' ? '禁用' : '启用'}成功`)
    row.status = row.status === 'active' ? 'disabled' : 'active'
  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改用户状态失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 初始化
fetchUserList()
</script>

<style scoped>
.users-container {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  padding: 20px 24px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.header-content h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.gradient-text {
  background: linear-gradient(to right, #3498db, #2c3e50);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-description {
  margin: 5px 0 0;
  color: #606266;
  font-size: 14px;
}

.add-button {
  background: linear-gradient(to right, #3498db, #2980b9);
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.add-button:hover {
  background: linear-gradient(to right, #2980b9, #1a5276);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.button-icon {
  margin-right: 5px;
}

.search-card {
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.search-button,
.reset-button {
  display: flex;
  align-items: center;
  border-radius: 6px;
  padding: 9px 15px;
  transition: all 0.3s ease;
}

.search-button {
  background: linear-gradient(to right, #3498db, #2980b9);
  border: none;
  margin-right: 10px;
}

.search-button:hover {
  background: linear-gradient(to right, #2980b9, #1a5276);
  transform: translateY(-2px);
  box-shadow: 0 5px 10px rgba(0, 0, 0, 0.1);
}

.reset-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 10px rgba(0, 0, 0, 0.05);
}

.list-card {
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding-bottom: 0;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.list-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.list-summary {
  font-size: 14px;
  color: #909399;
}

.highlight-text {
  color: #3498db;
  font-weight: 600;
}

.user-table :deep(.el-table__header-wrapper) {
  background-color: #f8f9fb;
}

.user-table :deep(.el-table__header) {
  font-weight: 600;
}

.user-table :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.user-table :deep(.el-table__row:hover) {
  background-color: #f0f7ff;
}

.status-tag {
  border-radius: 4px;
  padding: 2px 10px;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.table-actions {
  display: flex;
  justify-content: center;
}

.action-button {
  display: flex;
  align-items: center;
  font-size: 13px;
  margin: 0 4px;
  padding: 4px 10px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.action-button:hover {
  background-color: rgba(64, 158, 255, 0.1);
  transform: translateY(-1px);
}

.action-button:deep(.el-icon) {
  margin-right: 4px;
  font-size: 14px;
}

.pagination-container {
  padding: 20px 0;
  text-align: right;
  background-color: #fff;
  border-bottom-left-radius: 10px;
  border-bottom-right-radius: 10px;
}

.custom-dialog :deep(.el-dialog__header) {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  text-align: center;
}

.custom-dialog :deep(.el-dialog__title) {
  font-weight: 600;
  font-size: 18px;
  color: #303133;
}

.custom-dialog :deep(.el-dialog__body) {
  padding: 30px 40px;
}

.custom-dialog :deep(.el-dialog__footer) {
  padding: 20px;
  border-top: 1px solid #f0f0f0;
}

.custom-form :deep(.el-form-item__label) {
  font-weight: 500;
}

.custom-form :deep(.el-input__inner) {
  border-radius: 6px;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  width: 100%;
}

.cancel-button,
.confirm-button {
  min-width: 100px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.confirm-button {
  background: linear-gradient(to right, #3498db, #2980b9);
  border: none;
  margin-left: 20px;
}

.confirm-button:hover {
  background: linear-gradient(to right, #2980b9, #1a5276);
  transform: translateY(-2px);
  box-shadow: 0 5px 10px rgba(0, 0, 0, 0.1);
}

.cancel-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 10px rgba(0, 0, 0, 0.05);
}
</style>