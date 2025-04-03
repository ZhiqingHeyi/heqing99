<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="8" :md="6" :lg="5">
        <el-card class="user-card">
          <div class="user-avatar">
            <el-avatar :size="100" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
          </div>
          <h3 class="user-name">{{ userInfo.userName || '用户名' }}</h3>
          <p class="user-level">会员等级: {{ userInfo.level || '普通会员' }}</p>
          <p class="user-points">积分: {{ userInfo.points || 0 }}</p>
          
          <el-menu
            class="profile-menu"
            :default-active="activeMenu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="profile">
              <i class="el-icon-user"></i>
              <span>个人信息</span>
            </el-menu-item>
            <el-menu-item index="bookings">
              <i class="el-icon-tickets"></i>
              <span>我的预订</span>
            </el-menu-item>
            <el-menu-item index="points">
              <i class="el-icon-medal"></i>
              <span>积分记录</span>
            </el-menu-item>
            <el-menu-item index="settings">
              <i class="el-icon-setting"></i>
              <span>账号设置</span>
            </el-menu-item>
          </el-menu>

          <el-button class="logout-btn" type="danger" @click="handleLogout">退出登录</el-button>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="16" :md="18" :lg="19">
        <el-card class="content-card">
          <!-- 个人信息 -->
          <div v-if="activeMenu === 'profile'">
            <div class="section-header">
              <h2>个人信息</h2>
              <el-button type="primary" size="small" @click="isEditing = !isEditing">
                {{ isEditing ? '取消' : '编辑' }}
              </el-button>
            </div>

            <el-form :model="userForm" label-width="100px" class="user-form">
              <el-form-item label="用户名">
                <el-input v-model="userForm.userName" :disabled="!isEditing" />
              </el-form-item>
              <el-form-item label="真实姓名">
                <el-input v-model="userForm.realName" :disabled="!isEditing" />
              </el-form-item>
              <el-form-item label="手机号码">
                <el-input v-model="userForm.phone" :disabled="!isEditing" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="userForm.email" :disabled="!isEditing" />
              </el-form-item>
              <el-form-item label="生日">
                <el-date-picker
                  v-model="userForm.birthday"
                  type="date"
                  placeholder="选择日期"
                  :disabled="!isEditing"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="性别">
                <el-select v-model="userForm.gender" placeholder="请选择" :disabled="!isEditing" style="width: 100%">
                  <el-option label="男" value="male" />
                  <el-option label="女" value="female" />
                  <el-option label="不详" value="unknown" />
                </el-select>
              </el-form-item>
              <el-form-item v-if="isEditing">
                <el-button type="primary" @click="saveUserInfo">保存</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 预订列表 -->
          <div v-if="activeMenu === 'bookings'">
            <div class="section-header">
              <h2>我的预订</h2>
              <el-button type="primary" size="small" @click="goToBooking">新增预订</el-button>
            </div>

            <el-tabs v-model="bookingTab" class="booking-tabs">
              <el-tab-pane label="全部预订" name="all"></el-tab-pane>
              <el-tab-pane label="待确认" name="pending"></el-tab-pane>
              <el-tab-pane label="已确认" name="confirmed"></el-tab-pane>
              <el-tab-pane label="已取消" name="cancelled"></el-tab-pane>
              <el-tab-pane label="已完成" name="completed"></el-tab-pane>
            </el-tabs>

            <el-table :data="bookingList" style="width: 100%" v-loading="loading">
              <el-table-column prop="bookingNo" label="预订号" width="120" />
              <el-table-column prop="roomType" label="房间类型" />
              <el-table-column prop="checkInDate" label="入住日期" />
              <el-table-column prop="checkOutDate" label="离店日期" />
              <el-table-column prop="status" label="状态">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)">
                    {{ getStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button 
                    type="danger" 
                    link 
                    @click="cancelBooking(row)"
                    v-if="['pending', 'confirmed'].includes(row.status)"
                  >取消</el-button>
                  <el-button type="primary" link @click="viewBookingDetail(row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination-container">
              <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="currentPage"
                :page-sizes="[5, 10, 20, 50]"
                :page-size="pageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
              />
            </div>
          </div>

          <!-- 积分记录 -->
          <div v-if="activeMenu === 'points'">
            <div class="section-header">
              <h2>积分记录</h2>
            </div>
            <el-table :data="pointsList" style="width: 100%" v-loading="loading">
              <el-table-column prop="date" label="日期" width="180" />
              <el-table-column prop="description" label="描述" />
              <el-table-column prop="points" label="积分变化">
                <template #default="{ row }">
                  <span :class="row.points > 0 ? 'points-up' : 'points-down'">
                    {{ row.points > 0 ? '+' : '' }}{{ row.points }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="balance" label="积分余额" />
            </el-table>
          </div>

          <!-- 账号设置 -->
          <div v-if="activeMenu === 'settings'">
            <div class="section-header">
              <h2>账号设置</h2>
            </div>
            
            <el-collapse>
              <el-collapse-item title="修改密码" name="password">
                <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
                  <el-form-item label="当前密码" prop="oldPassword">
                    <el-input v-model="passwordForm.oldPassword" type="password" show-password />
                  </el-form-item>
                  <el-form-item label="新密码" prop="newPassword">
                    <el-input v-model="passwordForm.newPassword" type="password" show-password />
                  </el-form-item>
                  <el-form-item label="确认新密码" prop="confirmPassword">
                    <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="changePassword">确认修改</el-button>
                  </el-form-item>
                </el-form>
              </el-collapse-item>
              
              <el-collapse-item title="订阅设置" name="notifications">
                <el-form label-width="200px">
                  <el-form-item label="接收促销邮件">
                    <el-switch v-model="notificationSettings.promotions" />
                  </el-form-item>
                  <el-form-item label="接收预订确认短信">
                    <el-switch v-model="notificationSettings.bookingSms" />
                  </el-form-item>
                  <el-form-item label="接收积分变动通知">
                    <el-switch v-model="notificationSettings.pointsNotifications" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="saveNotificationSettings">保存设置</el-button>
                  </el-form-item>
                </el-form>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const isEditing = ref(false)
const loading = ref(false)
const activeMenu = ref('profile')
const bookingTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 用户信息
const userInfo = reactive({
  userName: localStorage.getItem('userName') || '用户名',
  level: '白银会员',
  points: 320
})

// 用户表单
const userForm = reactive({
  userName: userInfo.userName,
  realName: '张三',
  phone: '13812345678',
  email: 'zhangsan@example.com',
  birthday: '',
  gender: 'male'
})

// 密码表单
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (passwordForm.confirmPassword !== '') {
      passwordFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ]
}

// 通知设置
const notificationSettings = reactive({
  promotions: true,
  bookingSms: true,
  pointsNotifications: true
})

// 模拟预订数据
const bookingList = ref([
  {
    id: 1,
    bookingNo: 'B2023001',
    roomType: '豪华大床房',
    checkInDate: '2023-04-15',
    checkOutDate: '2023-04-17',
    status: 'pending'
  },
  {
    id: 2,
    bookingNo: 'B2023002',
    roomType: '行政套房',
    checkInDate: '2023-05-01',
    checkOutDate: '2023-05-03',
    status: 'confirmed'
  },
  {
    id: 3,
    bookingNo: 'B2023003',
    roomType: '总统套房',
    checkInDate: '2023-03-10',
    checkOutDate: '2023-03-12',
    status: 'completed'
  }
])

// 模拟积分数据
const pointsList = ref([
  {
    id: 1,
    date: '2023-04-01',
    description: '住宿消费',
    points: 100,
    balance: 320
  },
  {
    id: 2,
    date: '2023-03-15',
    description: '积分兑换',
    points: -50,
    balance: 220
  },
  {
    id: 3,
    date: '2023-03-10',
    description: '注册奖励',
    points: 100,
    balance: 270
  },
  {
    id: 4,
    date: '2023-03-05',
    description: '生日奖励',
    points: 50,
    balance: 170
  },
  {
    id: 5,
    date: '2023-02-20',
    description: '首次预订奖励',
    points: 120,
    balance: 120
  }
])

// 获取预订状态对应的类型
const getStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    confirmed: 'success',
    cancelled: 'danger',
    completed: 'info'
  }
  return statusMap[status]
}

// 获取预订状态对应的文本
const getStatusText = (status) => {
  const statusMap = {
    pending: '待确认',
    confirmed: '已确认',
    cancelled: '已取消',
    completed: '已完成'
  }
  return statusMap[status]
}

// 处理菜单选择
const handleMenuSelect = (index) => {
  activeMenu.value = index
  if (index === 'bookings') {
    fetchBookingList()
  } else if (index === 'points') {
    // 加载积分数据
  }
}

// 跳转到预订页面
const goToBooking = () => {
  router.push('/booking')
}

// 保存用户信息
const saveUserInfo = async () => {
  ElMessage.success('个人信息保存成功')
  isEditing.value = false
  
  // 更新显示的用户名
  userInfo.userName = userForm.userName
  localStorage.setItem('userName', userForm.userName)
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      ElMessage.success('密码修改成功')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    }
  })
}

// 保存通知设置
const saveNotificationSettings = () => {
  ElMessage.success('通知设置保存成功')
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 清除登录信息
    localStorage.removeItem('userToken')
    localStorage.removeItem('userName')
    
    ElMessage.success('已退出登录')
    router.push('/')
  } catch (error) {
    // 用户取消操作
  }
}

// 查看预订详情
const viewBookingDetail = (row) => {
  console.log('查看预订详情:', row)
  // TODO: 显示预订详情
}

// 取消预订
const cancelBooking = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该预订吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    row.status = 'cancelled'
    ElMessage.success('预订已取消')
  } catch (error) {
    // 用户取消操作
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchBookingList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchBookingList()
}

// 获取预订列表
const fetchBookingList = async () => {
  loading.value = true
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 根据tab筛选数据
    if (bookingTab.value !== 'all') {
      bookingList.value = bookingList.value.filter(item => item.status === bookingTab.value)
    }
    
    total.value = bookingList.value.length
    loading.value = false
  } catch (error) {
    console.error('获取预订列表失败:', error)
    loading.value = false
  }
}

// 监听预订状态tab变化
watch(bookingTab, () => {
  fetchBookingList()
})

onMounted(() => {
  // 加载用户数据
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.user-card {
  margin-bottom: 20px;
  text-align: center;
}

.user-avatar {
  margin: 20px 0;
}

.user-name {
  font-size: 18px;
  margin: 10px 0;
}

.user-level, .user-points {
  color: #666;
  margin: 5px 0;
}

.profile-menu {
  margin: 20px 0;
  border-right: none;
}

.logout-btn {
  margin: 20px 0;
  width: 100%;
}

.content-card {
  min-height: 500px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.section-header h2 {
  margin: 0;
  font-size: 20px;
}

.user-form {
  max-width: 500px;
}

.booking-tabs {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.points-up {
  color: #67C23A;
}

.points-down {
  color: #F56C6C;
}
</style> 