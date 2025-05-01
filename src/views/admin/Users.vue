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
        <div class="list-summary">共 <span class="highlight-text">{{ pagination.total }}</span> 位用户</div>
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
        <el-table-column label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag 
              size="small"
              :type="row.role === 'ADMIN' || row.role === 'SUPER_ADMIN' ? 'warning' : (row.role === 'RECEPTIONIST' || row.role === 'CLEANER' ? 'info' : 'success')"
              disable-transitions
            >
              {{ getRoleDisplayName(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="160" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.status === 'ACTIVE' ? 'success' : 'danger'"
              effect="dark"
              class="status-tag"
            >
              {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button type="primary" link @click="handleEdit(row)" class="action-button">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-button type="warning" link @click="handleResetPassword(row)" class="action-button">
                <el-icon><Key /></el-icon>重置密码
              </el-button>
              <el-button 
                :type="row.status === 'ACTIVE' ? 'danger' : 'success'"
                link 
                @click="handleToggleStatus(row)"
                class="action-button"
              >
                <el-icon><component :is="row.status === 'ACTIVE' ? 'Lock' : 'Unlock'" /></el-icon>
                {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
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
        <el-form-item label="角色" prop="role" v-if="dialogType === 'add'">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option
              v-for="item in userRoles"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false" class="cancel-button">取消</el-button>
          <el-button type="primary" @click="handleSubmit" class="confirm-button" :loading="loading">确定</el-button>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Key, Lock, Unlock, Iphone } from '@element-plus/icons-vue'
import { userApi } from '@/api'

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
    status: 'active',
    id: '1'
  },
  // 更多模拟数据...
])

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 表单对话框
const dialogVisible = ref(false)
const dialogType = ref('add')
const userFormRef = ref(null)
const userForm = reactive({
  username: '',
  name: '',
  phone: '',
  email: '',
  password: '',
  role: ''
})

// 可选的角色列表
const userRoles = [
  { value: 'ADMIN', label: '管理员' },
  { value: 'RECEPTIONIST', label: '前台' },
  { value: 'CLEANER', label: '清洁人员' },
  { value: 'CUSTOMER', label: '普通用户' },
]

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
    { required: dialogType.value === 'add', message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  role: [
    { required: dialogType.value === 'add', message: '请选择用户角色', trigger: 'change' }
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
  pagination.currentPage = 1
  fetchUsers()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  pagination.currentPage = 1
  handleSearch()
}

// 获取用户列表
const fetchUsers = async () => {
  console.log('fetchUsers called. Current page:', pagination.currentPage, 'pageSize:', pagination.pageSize, 'Filters:', searchForm);
  loading.value = true;
  try {
    const response = await userApi.getAllUsers(
      pagination.currentPage,
      pagination.pageSize,
      searchForm
    );
    
    // 添加日志，检查返回的数据结构和 status 值
    console.log('API response received:', response);

    if (response.success && response.data && response.data.users) {
      // 在更新前打印具体的用户数据
      console.log('Fetched users data before update:', JSON.stringify(response.data.users)); 
      userList.value = response.data.users;
      pagination.total = response.data.total || 0;
      console.log('userList updated. Total:', pagination.total);
    } else {
      console.error('获取用户列表失败或数据结构错误', response);
      userList.value = []; // 清空列表以防万一
      pagination.total = 0;
      ElMessage.error(response?.message || '获取用户列表失败');
    }
  } catch (error) {
    console.error('获取用户列表失败 (catch block):', error);
    userList.value = []; // 出错时也清空列表
    pagination.total = 0;
    ElMessage.error(error.message || '获取用户列表失败');
  } finally {
    loading.value = false;
    console.log('fetchUsers finished.');
  }
};

// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  fetchUsers()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchUsers()
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
      loading.value = true;
      try {
        let response = null;
        if (dialogType.value === 'add') {
          response = await userApi.createUser(userForm);
          ElMessage.success(response.message || '添加成功');
        } else {
          if (!userForm.id) {
             console.error('编辑用户时缺少 ID');
             ElMessage.error('编辑失败，缺少用户ID');
             loading.value = false;
             return;
          }
          const userDataToUpdate = { ...userForm };
          delete userDataToUpdate.password;
          response = await userApi.updateUserInfo(userForm.id, userDataToUpdate);
          ElMessage.success(response.message || '修改成功');
        }
        
        dialogVisible.value = false;
        fetchUsers();
      } catch (error) {
        console.error('保存用户信息失败:', error);
        ElMessage.error(error.message || '操作失败');
      } finally {
        loading.value = false;
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
    // 确认对话框
    await ElMessageBox.confirm(
      `确定要${row.status === 'ACTIVE' ? '禁用' : '启用'}用户 "${row.username}" 吗？`,
      row.status === 'ACTIVE' ? '禁用用户' : '启用用户',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true;
    // 调用更新后的API，移除第二个参数
    const response = await userApi.toggleUserStatus(row.id);
    
    if (response.success) {
      // 删除本地状态修改
      // row.status = row.status === 'active' ? 'disabled' : 'active'
      ElMessage.success(response.message || '用户状态已切换');
      // 添加 fetchUsers() 刷新列表
      fetchUsers(); 
    } else {
      ElMessage.error(response.message || '切换用户状态失败');
    }
  } catch (error) {
    if (error !== 'cancel' && error.message !== 'cancel') {
      console.error('切换用户状态失败:', error);
      ElMessage.error(error.message || '操作失败');
    }
  } finally {
     loading.value = false; // 结束加载
  }
}

// 删除用户
const deleteUser = async (row) => {
  try {
    // 确认对话框
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 吗？此操作不可恢复！`,
      '删除用户',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'danger'
      }
    )
    
    // 使用更新的API调用
    const response = await userApi.deleteUser(row.id)
    
    if (response.success) {
      // 暂时注释掉直接修改列表和总数的代码，让 fetchUsers 重新获取最新数据
      // const index = userList.value.findIndex(u => u.id === row.id)
      // if (index > -1) {
      //   userList.value.splice(index, 1)
      // }
      // pagination.total -= 1 // 修改
      ElMessage.success('删除用户成功')
      // 删除成功后重新请求数据，以保证分页和列表的准确性
      // 如果当前页在删除后为空，可能需要跳转到前一页（这是一个优化点，暂时不处理）
      fetchUsers()
    } else {
      ElMessage.error(response.message || '删除用户失败')
    }
  } catch (error) {
    // 取消操作或API错误
    if (error !== 'cancel' && error.message !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 根据角色获取用户
const getUsersByRole = async (role) => {
  if (!role) return
  
  loading.value = true
  try {
    // 使用更新的API调用
    const response = await userApi.getUsersByRole(
      role,
      pagination.currentPage,
      pagination.pageSize
    )
    
    if (response.success && response.data) {
      // 更新用户列表
      userList.value = response.data.users || []
      
      // 更新分页信息
      pagination.total = response.data.total || 0
      
      // 设置当前选中的角色
      currentRole.value = role
    } else {
      console.error('获取角色用户列表失败', response)
      ElMessage.error(response?.message || '获取角色用户列表失败')
    }
  } catch (error) {
    console.error('获取角色用户列表失败:', error)
    ElMessage.error(error.message || '获取角色用户列表失败')
  } finally {
    loading.value = false
  }
}

// 获取角色用户数量
const getUserCountByRole = async () => {
  try {
    // 获取客户数量
    const customerResponse = await userApi.getUserCountByRole('CUSTOMER')
    if (customerResponse.success && customerResponse.data) {
      roleCounts.customer = customerResponse.data.count || 0
    }
    
    // 获取员工数量
    const staffResponse = await userApi.getUserCountByRole('STAFF')
    if (staffResponse.success && staffResponse.data) {
      roleCounts.staff = staffResponse.data.count || 0
    }
    
    // 获取管理员数量
    const adminResponse = await userApi.getUserCountByRole('ADMIN')
    if (adminResponse.success && adminResponse.data) {
      roleCounts.admin = adminResponse.data.count || 0
    }
    
    // 计算会员比例
    calculateMemberRatio()
  } catch (error) {
    console.error('获取角色用户数量失败:', error)
  }
}

// (新) 角色名称映射函数
const getRoleDisplayName = (role) => {
  const roleMap = {
    ADMIN: '管理员',
    RECEPTIONIST: '前台',
    CLEANER: '清洁人员',
    CUSTOMER: '普通用户',
    SUPER_ADMIN: '超级管理员'
  };
  return roleMap[role] || role;
};

// 初始化
onMounted(() => {
  fetchUsers()
  // getUserCountByRole() // 如果需要也放在这里
})
</script>

<style scoped>
.users-container {
  padding: 24px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: #fff;
  padding: 24px 28px;
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;
}

.page-header::after {
  content: '';
  position: absolute;
  right: -40px;
  top: -40px;
  width: 180px;
  height: 180px;
  background: linear-gradient(135deg, rgba(52, 152, 219, 0.05), rgba(44, 62, 80, 0.08));
  border-radius: 50%;
  z-index: 0;
}

.header-content {
  position: relative;
  z-index: 1;
}

.header-content h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.gradient-text {
  background: linear-gradient(135deg, #3498db, #2c3e50);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  position: relative;
}

.gradient-text::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 40px;
  height: 3px;
  background: linear-gradient(135deg, #3498db, #2c3e50);
  border-radius: 3px;
}

.header-description {
  margin: 16px 0 0;
  color: #606266;
  font-size: 15px;
  max-width: 450px;
}

.add-button {
  background: linear-gradient(135deg, #3498db, #2980b9);
  border: none;
  padding: 12px 24px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  font-weight: 500;
  letter-spacing: 0.5px;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.add-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #2980b9, #1a5276);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.add-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(41, 128, 185, 0.3);
}

.add-button:hover::before {
  opacity: 1;
}

.button-icon {
  margin-right: 8px;
  font-size: 16px;
}

.search-card {
  margin-bottom: 24px;
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
  padding: 5px 10px;
  border: none;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  padding: 10px;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 10px;
  margin-right: 20px;
}

.search-form :deep(.el-input__wrapper),
.search-form :deep(.el-select__wrapper) {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
  border-radius: 10px;
  transition: all 0.3s ease;
}

.search-form :deep(.el-input__wrapper:hover),
.search-form :deep(.el-select__wrapper:hover) {
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
}

.search-form :deep(.el-input__wrapper:focus-within),
.search-form :deep(.el-select__wrapper:focus-within) {
  box-shadow: 0 0 0 1px #3498db inset, 0 3px 10px rgba(52, 152, 219, 0.1);
}

.search-button,
.reset-button {
  display: flex;
  align-items: center;
  border-radius: 10px;
  padding: 10px 18px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.search-button {
  background: linear-gradient(135deg, #3498db, #2980b9);
  border: none;
  margin-right: 12px;
}

.search-button:hover {
  background: linear-gradient(135deg, #2980b9, #1a5276);
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(41, 128, 185, 0.2);
}

.reset-button {
  border: 1px solid #dcdfe6;
}

.reset-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.08);
  background-color: #f8f9fa;
}

.list-card {
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
  padding-bottom: 0;
  border: none;
  overflow: hidden;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(to right, #f8f9fa, #f0f7ff);
}

.list-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  position: relative;
  padding-left: 15px;
}

.list-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background: linear-gradient(to bottom, #3498db, #2980b9);
  border-radius: 2px;
}

.list-summary {
  font-size: 15px;
  color: #606266;
}

.highlight-text {
  color: #3498db;
  font-weight: 600;
  font-size: 16px;
}

.user-table {
  margin: 10px 0;
}

.user-table :deep(.el-table__header-wrapper) {
  background-color: #f8f9fb;
}

.user-table :deep(.el-table__header) {
  font-weight: 600;
}

.user-table :deep(.el-table__header th) {
  background-color: #f5f7fa;
  color: #303133;
  font-size: 15px;
  padding: 16px 0;
}

.user-table :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.user-table :deep(.el-table__row td) {
  padding: 16px 0;
}

.user-table :deep(.el-table__row:hover) {
  background-color: #f0f7ff !important;
}

.status-tag {
  border-radius: 6px;
  padding: 4px 12px;
  font-weight: 500;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1);
  font-size: 13px;
}

.table-actions {
  display: flex;
  justify-content: center;
  gap: 6px;
}

.action-button {
  display: flex;
  align-items: center;
  font-size: 14px;
  padding: 6px 10px;
  border-radius: 6px;
  transition: all 0.25s ease;
  font-weight: 500;
}

.action-button:hover {
  background-color: rgba(64, 158, 255, 0.1);
  transform: translateY(-2px);
}

.action-button:deep(.el-icon) {
  margin-right: 4px;
  font-size: 14px;
}

.pagination-container {
  padding: 24px;
  text-align: right;
  background-color: #fff;
  border-bottom-left-radius: 16px;
  border-bottom-right-radius: 16px;
  border-top: 1px solid #f0f0f0;
}

.pagination-container :deep(.el-pagination) {
  justify-content: flex-end;
}

.pagination-container :deep(.el-pagination__total) {
  font-size: 14px;
  color: #606266;
}

.pagination-container :deep(.el-pagination .btn-prev),
.pagination-container :deep(.el-pagination .btn-next),
.pagination-container :deep(.el-pagination .number) {
  background-color: #f8f9fa;
  border-radius: 6px;
  margin: 0 3px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.pagination-container :deep(.el-pagination .btn-prev:hover),
.pagination-container :deep(.el-pagination .btn-next:hover),
.pagination-container :deep(.el-pagination .number:hover) {
  background-color: #ecf5ff;
  color: #3498db;
}

.pagination-container :deep(.el-pagination .active) {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
}

.custom-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.custom-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  background: linear-gradient(to right, #f8f9fa, #f0f7ff);
  border-bottom: 1px solid #f0f0f0;
  text-align: center;
}

.custom-dialog :deep(.el-dialog__title) {
  font-weight: 600;
  font-size: 18px;
  color: #303133;
  position: relative;
}

.custom-dialog :deep(.el-dialog__headerbtn) {
  top: 24px;
  right: 24px;
}

.custom-dialog :deep(.el-dialog__body) {
  padding: 32px 40px;
}

.custom-dialog :deep(.el-dialog__footer) {
  padding: 20px 24px;
  background-color: #f9fafc;
  border-top: 1px solid #f0f0f0;
}

.custom-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
  padding-right: 20px;
}

.custom-form :deep(.el-input__wrapper),
.custom-form :deep(.el-select__wrapper) {
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
  border-radius: 10px;
  padding: 2px 15px;
  transition: all 0.3s ease;
}

.custom-form :deep(.el-input__wrapper:hover),
.custom-form :deep(.el-select__wrapper:hover) {
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
}

.custom-form :deep(.el-input__wrapper:focus-within),
.custom-form :deep(.el-select__wrapper:focus-within) {
  box-shadow: 0 0 0 1px #3498db inset, 0 3px 10px rgba(52, 152, 219, 0.1);
}

.dialog-footer {
  display: flex;
  justify-content: center;
  width: 100%;
  gap: 16px;
}

.cancel-button,
.confirm-button {
  min-width: 120px;
  border-radius: 10px;
  transition: all 0.3s ease;
  font-weight: 500;
  padding: 12px 20px;
  font-size: 15px;
}

.confirm-button {
  background: linear-gradient(135deg, #3498db, #2980b9);
  border: none;
}

.confirm-button:hover {
  background: linear-gradient(135deg, #2980b9, #1a5276);
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(41, 128, 185, 0.2);
}

.cancel-button {
  border: 1px solid #dcdfe6;
}

.cancel-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.08);
  background-color: #f8f9fa;
}

@media (max-width: 768px) {
  .users-container {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 20px;
  }
  
  .add-button {
    width: 100%;
    justify-content: center;
  }
  
  .search-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-form .el-form-item {
    margin-right: 0;
    width: 100%;
  }
  
  .table-actions {
    flex-wrap: wrap;
  }
}
</style>