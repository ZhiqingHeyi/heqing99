<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="8" :md="6" :lg="5">
        <el-card class="user-card">
          <div class="user-avatar">
            <el-avatar :size="100" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
          </div>
          <h3 class="user-name">{{ userInfo.userName || '用户名' }}</h3>
          
          <!-- 会员等级徽章 -->
          <div class="member-badge" :class="getLevelClass(userInfo.level)">
            {{ userInfo.level }}
          </div>
          
          <p class="user-points">积分: {{ userInfo.points || 0 }}</p>
          <p class="user-total-spent">累计消费: ¥{{ userInfo.totalSpent || 0 }}</p>
          
          <!-- 显示升级进度 -->
          <div class="upgrade-progress" v-if="userInfo.level !== '钻石会员'">
            <span>距离{{ userInfo.nextLevel }}</span>
            <el-progress :percentage="getLevelProgress()" :stroke-width="8" :show-text="false"></el-progress>
          </div>
          
          <el-menu
            class="profile-menu"
            :default-active="activeMenu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="profile">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </el-menu-item>
            <el-menu-item index="bookings">
              <el-icon><Tickets /></el-icon>
              <span>我的预订</span>
            </el-menu-item>
            <el-menu-item index="points">
              <el-icon><Medal /></el-icon>
              <span>积分记录</span>
            </el-menu-item>
            <el-menu-item index="settings">
              <el-icon><Setting /></el-icon>
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
              <el-button
                type="primary"
                size="small"
                :class="isEditing ? 'btn-cancel-style' : 'btn-edit-style'"
                @click="isEditing = !isEditing"
              >
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

          <!-- 会员权益信息 -->
          <div v-if="activeMenu === 'profile'">
            <div class="section-header">
              <h2>会员特权</h2>
            </div>
            
            <div class="member-benefits">
              <el-table :data="getMemberBenefits()" style="width: 100%">
                <el-table-column prop="level" label="会员等级" width="120">
                  <template #default="{ row }">
                    <div class="level-tag" :class="getLevelClass(row.level)">{{ row.level }}</div>
                  </template>
                </el-table-column>
                <el-table-column prop="discount" label="折扣优惠" width="100" />
                <el-table-column prop="pointRate" label="积分比例" width="100" />
                <el-table-column prop="prepay" label="预付政策" width="150" />
                <el-table-column prop="cancel" label="取消政策" />
                <el-table-column prop="threshold" label="升级条件" width="180" />
              </el-table>
            </div>
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
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { User, Tickets, Medal, Setting } from '@element-plus/icons-vue'
import { userApi, membershipApi, reservationApi } from '@/api'

console.log('Profile.vue组件加载')

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
  level: localStorage.getItem('userLevel') || '普通用户',
  points: parseInt(localStorage.getItem('userPoints') || '0'),
  totalSpent: parseInt(localStorage.getItem('userTotalSpent') || '0'),
  nextLevel: ''
})

// 计算下一个等级需要的消费金额
const updateNextLevel = () => {
  if (userInfo.level === '普通用户') {
    userInfo.nextLevel = '铜牌会员（再消费¥' + (1500 - userInfo.totalSpent) + '）'
  } else if (userInfo.level === '铜牌会员') {
    userInfo.nextLevel = '银牌会员（再消费¥' + (5000 - userInfo.totalSpent) + '）'
  } else if (userInfo.level === '银牌会员') {
    userInfo.nextLevel = '金牌会员（再消费¥' + (10000 - userInfo.totalSpent) + '）'
  } else if (userInfo.level === '金牌会员') {
    userInfo.nextLevel = '钻石会员（再消费¥' + (30000 - userInfo.totalSpent) + '）'
  } else {
    userInfo.nextLevel = '已是最高等级'
  }
}

// 监听 totalSpent 的变化，更新 nextLevel
watch(() => userInfo.totalSpent, () => {
  updateNextLevel()
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
const bookingList = ref([])

// 模拟积分数据
const pointsList = ref([])

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
    fetchPointsList()
  }
}

// 跳转到预订页面
const goToBooking = () => {
  router.push('/booking')
}

// 保存用户信息
const saveUserInfo = async () => {
  let userId = null; // 在 try 块外部声明
  try {
    // 验证数据有效性
    if (!userForm.userName.trim() || !userForm.realName.trim() || !userForm.phone.trim()) {
      ElMessage.warning('用户名、姓名和手机号不能为空');
      return;
    }

    // 验证手机号格式
    const phoneRegex = /^1[3-9]\d{9}$/;
    if (!phoneRegex.test(userForm.phone)) {
      ElMessage.warning('请输入有效的手机号');
      return;
    }

    // 验证邮箱格式
    if (userForm.email) {
      const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
      if (!emailRegex.test(userForm.email)) {
        ElMessage.warning('请输入有效的邮箱地址');
        return;
      }
    }
    
    // 用户ID - 直接从 localStorage 获取
    userId = localStorage.getItem('userId');
    
    if (!userId) {
      ElMessage.error('未找到用户ID，请重新登录');
      return;
    }

    // 构建更新数据
    const updateData = {
      username: userForm.userName,
      name: userForm.realName,      // 使用name替代realName
      phone: userForm.phone,
      email: userForm.email,
      gender: userForm.gender,
      birthday: userForm.birthday ? new Date(userForm.birthday).toISOString().split('T')[0] : null
    };

    // 显示加载中
    const loading = ElLoading.service({
      lock: true,
      text: '保存中...',
      background: 'rgba(255, 255, 255, 0.7)'
    });

    try {
      // 使用更新的API调用
      const response = await userApi.updateUserInfo(userId, updateData);
      
      if (response.success) {
        ElMessage.success('信息更新成功');
        
        // 更新本地存储
        localStorage.setItem('userName', updateData.username);
        localStorage.setItem('userRealName', updateData.name);
        localStorage.setItem('userPhone', updateData.phone);
        localStorage.setItem('userEmail', updateData.email || '');
        localStorage.setItem('userGender', updateData.gender || 'unknown');
        localStorage.setItem('userBirthday', updateData.birthday || '');
        
        // 更新 userInfo (用于侧边栏等显示)
        userInfo.userName = updateData.username; 

        // 更新 userForm (用于表单显示)
        userForm.userName = updateData.username;
        userForm.realName = updateData.name;
        userForm.phone = updateData.phone;
        userForm.email = updateData.email;
        userForm.gender = updateData.gender;
        userForm.birthday = updateData.birthday; // 确保 updateData.birthday 是正确的格式或 null
        
        // 关闭编辑模式
        isEditing.value = false;
      } else {
        ElMessage.error(response.message || '更新失败，请稍后重试');
      }
    } catch (error) {
      console.error('更新用户信息失败:', error);
      ElMessage.error(error.message || '更新失败，请稍后重试');
    } finally {
      // 关闭加载
      loading.close();
    }
  } catch (error) {
    console.error('保存用户信息出错:', error);
    ElMessage.error('操作出错，请重试');
  }
};

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return;
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      // 用户ID - 直接从 localStorage 获取
      const userId = localStorage.getItem('userId');
      
      if (!userId) {
        ElMessage.error('未找到用户ID，请重新登录');
        return;
      }
      
      // 显示加载中
      const loading = ElLoading.service({
        lock: true,
        text: '提交中...',
        background: 'rgba(255, 255, 255, 0.7)'
      });
      
      try {
        // 使用更新的API调用
        const response = await userApi.changePassword(userId, {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        });
        
        if (response.success) {
          ElMessage.success('密码修改成功');
          
          // 重置表单
          passwordForm.oldPassword = '';
          passwordForm.newPassword = '';
          passwordForm.confirmPassword = '';
          
          // 重置表单验证
          passwordFormRef.value.resetFields();
        } else {
          ElMessage.error(response.message || '密码修改失败，请稍后重试');
        }
      } catch (error) {
        console.error('修改密码失败:', error);
        ElMessage.error(error.message || '密码修改失败，请稍后重试');
      } finally {
        // 关闭加载
        loading.close();
      }
    }
  });
};

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
    
    try {
      // 调用API退出登录
      await userApi.logout()
    } catch (apiError) {
      console.error('API退出登录失败:', apiError)
      // 即使API失败，也继续前端登出流程
    }
    
    // 清除登录信息
    localStorage.removeItem('userToken')
    localStorage.removeItem('userName')
    localStorage.removeItem('userId')
    localStorage.removeItem('userLevel')
    localStorage.removeItem('userPoints')
    localStorage.removeItem('userTotalSpent')
    
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
    
    // 调用取消预订API
    const response = await reservationApi.cancelReservation(row.id)
    
    if (response && response.success) {
      ElMessage.success('预订已取消')
      // 刷新预订列表
      fetchBookingList()
    } else {
      ElMessage.error((response?.message) || '取消预订失败')
    }
  } catch (error) {
    // 用户取消操作或API错误
    if (error !== 'cancel' && error.message) {
      console.error('取消预订失败:', error)
      ElMessage.error('取消预订失败: ' + error.message)
    }
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
    // 使用API获取预订列表
    const status = bookingTab.value !== 'all' ? bookingTab.value : null
    const response = await reservationApi.getUserReservations(currentPage.value, pageSize.value, status)
    
    if (response && response.success) {
      bookingList.value = response.data.list || []
      total.value = response.data.total || 0
    } else {
      ElMessage.warning('获取预订列表失败: ' + (response?.message || '未知错误'))
      bookingList.value = []
      total.value = 0
    }
    
    loading.value = false
  } catch (error) {
    console.error('获取预订列表失败:', error)
    ElMessage.error('获取预订列表失败: ' + error.message)
    bookingList.value = []
    total.value = 0
    loading.value = false
  }
}

// 获取积分记录
const fetchPointsList = async () => {
  loading.value = true
  
  try {
    // 从API获取积分记录
    const response = await membershipApi.getPointsHistory(currentPage.value, pageSize.value)
    
    if (response && response.success) {
      // 将积分记录转换为表格展示所需的格式
      const records = response.data.list || []
      pointsList.value = records.map(record => ({
        id: record.id,
        date: record.createTime.split(' ')[0],
        description: record.description,
        points: record.points,
        balance: record.balance
      }))
    } else {
      ElMessage.warning('获取积分记录失败: ' + (response?.message || '未知错误'))
      pointsList.value = []
    }
    
    loading.value = false
  } catch (error) {
    console.error('获取积分记录失败:', error)
    ElMessage.error('获取积分记录失败: ' + error.message)
    pointsList.value = []
    loading.value = false
  }
}

// 监听预订状态tab变化
watch(bookingTab, () => {
  fetchBookingList()
})

// 获取会员等级对应的样式类
const getLevelClass = (level) => {
  const classMap = {
    '普通用户': 'level-normal',
    '铜牌会员': 'level-bronze',
    '银牌会员': 'level-silver',
    '金牌会员': 'level-gold',
    '钻石会员': 'level-diamond'
  }
  return classMap[level] || 'level-normal'
}

// 计算会员升级进度百分比
const getLevelProgress = () => {
  if (userInfo.level === '普通用户') {
    return Math.min(100, (userInfo.totalSpent / 1500) * 100)
  } else if (userInfo.level === '铜牌会员') {
    return Math.min(100, ((userInfo.totalSpent - 1500) / (5000 - 1500)) * 100)
  } else if (userInfo.level === '银牌会员') {
    return Math.min(100, ((userInfo.totalSpent - 5000) / (10000 - 5000)) * 100)
  } else if (userInfo.level === '金牌会员') {
    return Math.min(100, ((userInfo.totalSpent - 10000) / (30000 - 10000)) * 100)
  }
  return 100
}

// 获取所有会员级别的权益信息
const getMemberBenefits = () => {
  return [
    {
      level: '普通用户',
      discount: '无折扣',
      pointRate: '无积分',
      prepay: '需全额或30%预付',
      cancel: '提前24小时可免费取消',
      threshold: '注册即可'
    },
    {
      level: '铜牌会员',
      discount: '98折',
      pointRate: '1元=1积分',
      prepay: '可到店支付',
      cancel: '提前12小时可免费取消',
      threshold: '累计消费满1500元'
    },
    {
      level: '银牌会员',
      discount: '95折',
      pointRate: '1元=1.2积分',
      prepay: '可免预付金',
      cancel: '提前6小时可免费取消',
      threshold: '累计消费满5000元'
    },
    {
      level: '金牌会员',
      discount: '9折',
      pointRate: '1元=1.5积分',
      prepay: '免预付金',
      cancel: '可随时免费取消',
      threshold: '累计消费满10000元'
    },
    {
      level: '钻石会员',
      discount: '8.5折',
      pointRate: '1元=2积分',
      prepay: '免预付金',
      cancel: '可免费取消+延迟退房',
      threshold: '累计消费满30000元'
    }
  ]
}

onMounted(async () => {
  // 加载用户数据
  console.log('Profile组件已挂载')
  console.log('当前用户信息:', userInfo)
  
  try {
    loading.value = true
    
    // 从 localStorage 获取 userId
    const userId = localStorage.getItem('userId');

    // 校验 userId 是否存在
    if (!userId) {
      ElMessage.error('无法获取用户ID，请重新登录');
      loading.value = false; // 停止加载状态
      return; // 阻止后续操作
    }

    // 获取用户基本信息，传递 userId
    const userResponse = await userApi.getUserInfo(userId);
    if (userResponse && userResponse.success && userResponse.data) { // 确保 userResponse 和 data 有效
      const userData = userResponse.data;
      userForm.userName = userData.username || userInfo.userName
      userForm.realName = userData.name || '' // 后端字段为 name
      userForm.phone = userData.phone || ''
      userForm.email = userData.email || ''
      userForm.birthday = userData.birthday || ''
      userForm.gender = userData.gender || ''

      // 更新显示的用户名
      userInfo.userName = userData.username || userInfo.userName
      localStorage.setItem('userName', userInfo.userName) // 仅更新用户名，其他信息在 memberInfo 获取
    } else {
       // 如果获取失败，可以记录日志或显示更具体的错误信息
       console.warn('获取用户基本信息失败:', userResponse?.message);
       // 可以选择是否显示给用户，取决于业务需求
       // ElMessage.warning(userResponse?.message || '获取用户基本信息失败');
       // 如果获取基本信息失败，可能不需要阻止后续操作，看产品设计
    }

    // 获取会员信息 (这个API不需要userId)
    const memberInfo = await membershipApi.getMemberInfo()
    if (memberInfo && memberInfo.success && memberInfo.data) { // 确保 memberInfo 和 data 有效
      userInfo.level = memberInfo.data.level || '普通用户'
      userInfo.points = memberInfo.data.points || 0
      userInfo.totalSpent = memberInfo.data.totalSpent || 0
      localStorage.setItem('userLevel', userInfo.level)
      localStorage.setItem('userPoints', String(userInfo.points))
      localStorage.setItem('userTotalSpent', String(userInfo.totalSpent))
      updateNextLevel()
    }
    
    loading.value = false
    
    // 加载初始的预订列表
    if (activeMenu.value === 'bookings') {
      fetchBookingList()
    }
    
    // 加载初始的积分记录
    if (activeMenu.value === 'points') {
      fetchPointsList()
    }
  } catch (error) {
    loading.value = false
    console.error('获取用户数据失败:', error)
    // 显示从 API 调用中捕获到的更具体的错误消息
    ElMessage.error('获取用户数据失败: ' + (error.message || '未知错误'));
  }
  
  // 确保路由配置和组件渲染正常
  document.title = '个人中心 - 会员管理'
})
</script>

<style scoped>
.profile-container {
  padding: 30px;
  background-color: #f8f5f0;
}

.user-card {
  margin-bottom: 30px;
  text-align: center;
  border: none;
  box-shadow: 0 4px 20px rgba(197, 157, 95, 0.1);
  background: linear-gradient(to bottom, #ffffff, #fdfbf6);
  border-radius: 12px;
}

.user-avatar {
  margin: 25px auto;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

.user-avatar::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 110px;
  height: 110px;
  border: 2px solid #c59d5f;
  border-radius: 50%;
  opacity: 0.3;
}

.user-name {
  font-size: 22px;
  margin: 15px 0;
  color: #1a1a1a;
  font-weight: 600;
  letter-spacing: 1px;
}

.user-level, .user-points {
  color: #666;
  margin: 8px 0;
  font-size: 15px;
}

.profile-menu {
  margin: 25px 0;
  border-right: none;
  background: transparent;
}

.profile-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  font-size: 15px;
  color: #666;
  border-radius: 8px;
  margin: 5px 0;
  transition: all 0.3s ease;
}

.profile-menu .el-menu-item:hover {
  background-color: #faf6f0;
  color: #c59d5f;
}

.profile-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #faf6f0, #f5eadb);
  color: #c59d5f;
  font-weight: 500;
}

.logout-btn {
  margin: 25px 0;
  width: 100%;
  height: 40px;
  font-size: 15px;
  border-radius: 20px;
  background: linear-gradient(135deg, #ff6b6b, #ff4757);
  border: none;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.2);
}

.content-card {
  min-height: 500px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border-radius: 12px;
  background: #ffffff;
  padding: 25px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0e6d6;
}

.section-header h2 {
  margin: 0;
  font-size: 24px;
  color: #1a1a1a;
  font-weight: 600;
  position: relative;
  padding-left: 15px;
}

.section-header h2::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: linear-gradient(to bottom, #c59d5f, #deb887);
  border-radius: 2px;
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

.member-badge {
  display: inline-block;
  padding: 8px 20px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 25px;
  margin: 15px 0;
  letter-spacing: 1px;
  box-shadow: 0 2px 8px rgba(197, 157, 95, 0.2);
  background: linear-gradient(135deg, #c59d5f, #deb887);
  color: #ffffff;
}

.level-normal {
  background-color: #909399;
  color: #fff;
}

.level-bronze {
  background-color: #cd7f32;
  color: #fff;
}

.level-silver {
  background-color: #c0c0c0;
  color: #333;
}

.level-gold {
  background-color: #d4af37;
  color: #333;
}

.level-diamond {
  background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%);
  color: #333;
  font-weight: 600;
}

.upgrade-progress {
  margin: 10px 0 20px;
  font-size: 12px;
  padding: 0 10px;
}

.user-total-spent {
  color: #666;
  margin: 5px 0;
}

.member-benefits {
  margin-bottom: 30px;
}

.level-tag {
  padding: 2px 8px;
  border-radius: 4px;
  display: inline-block;
  font-size: 12px;
  font-weight: 500;
}

/* Gold Button Styles */
.btn-edit-style,
.user-form .el-form-item:last-child .el-button {
  background-color: #c59d5f !important; /* Main gold */
  border-color: #c59d5f !important;
  color: #ffffff !important;
  border-radius: 4px; /* Adjust as needed */
  transition: background-color 0.3s, border-color 0.3s;
}

.btn-edit-style:hover,
.user-form .el-form-item:last-child .el-button:hover {
  background-color: #deb887 !important; /* Lighter gold on hover */
  border-color: #deb887 !important;
}

.btn-cancel-style {
  background-color: transparent !important;
  border: 1px solid #c59d5f !important; /* Main gold border */
  color: #c59d5f !important; /* Main gold text */
  border-radius: 4px;
  transition: background-color 0.3s, color 0.3s, border-color 0.3s;
}

.btn-cancel-style:hover {
  background-color: #fdfbf6 !important; /* Light background on hover */
  border-color: #a07d4a !important; /* Darker border/text on hover */
  color: #a07d4a !important;
}
/* End Gold Button Styles */
</style>