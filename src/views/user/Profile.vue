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
                @click="toggleEdit"
              >
                {{ isEditing ? '取消' : '编辑' }}
              </el-button>
            </div>

            <el-form :model="userForm" :rules="userFormRules" ref="userFormRef" label-width="100px" class="user-form">
              <el-form-item label="用户名">
                <el-input v-model="userForm.userName" disabled />
              </el-form-item>
              <el-form-item label="真实姓名" prop="name">
                <el-input v-model="userForm.name" :disabled="!isEditing" />
              </el-form-item>
              <el-form-item label="手机号码" prop="phone">
                <el-input v-model="userForm.phone" :disabled="!isEditing" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="userForm.email" :disabled="!isEditing" />
              </el-form-item>
              <el-form-item label="生日">
                <el-date-picker
                  v-model="userForm.birthday"
                  type="date"
                  placeholder="选择日期"
                  :disabled="!isEditing"
                  style="width: 100%"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item label="性别">
                <el-select v-model="userForm.gender" placeholder="请选择" :disabled="!isEditing" style="width: 100%">
                  <el-option label="男" value="男" />
                  <el-option label="女" value="女" />
                  <el-option label="不详" value="不详" />
                </el-select>
              </el-form-item>
              <el-form-item v-if="isEditing">
                <el-button type="primary" @click="saveUserInfo" :loading="savingUserInfo" class="btn-save-style">保存</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 会员权益信息 -->
          <div v-if="activeMenu === 'profile'">
            <div class="section-header">
              <h2>会员特权</h2>
            </div>
            
            <div class="member-benefits">
              <el-table :data="memberBenefitsData" style="width: 100%" class="benefits-table">
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
              <el-button type="primary" size="small" @click="goToBookingPage" class="btn-edit-style">
                新增预订
              </el-button>
            </div>

            <!-- 预订过滤器 -->
            <div class="filter-bar bookings-filter-bar">
              <el-radio-group v-model="statusFilter" @change="filterAndFetchBookings" class="filter-group">
                <el-radio-button label="all">全部预订</el-radio-button>
                <el-radio-button label="PENDING">待确认</el-radio-button>
                <el-radio-button label="CONFIRMED">已确认</el-radio-button>
                <el-radio-button label="COMPLETED">已完成</el-radio-button>
                <el-radio-button label="CANCELLED">已取消</el-radio-button>
              </el-radio-group>
            </div>

            <!-- 加载状态 -->
            <div v-if="bookingsLoading" class="loading-container">
              <el-skeleton :rows="5" animated />
            </div>

            <!-- 预订列表 -->
            <div v-else-if="userBookings.length > 0" class="bookings-list">
              <el-table :data="userBookings" style="width: 100%">
                <el-table-column prop="id" label="预订号" width="100" />
                <el-table-column prop="roomTypeName" label="房间类型" />
                <el-table-column prop="checkInTime" label="入住日期" width="120">
                  <template #default="{ row }">{{ formatDate(row.checkInTime) }}</template>
                </el-table-column>
                <el-table-column prop="checkOutTime" label="离店日期" width="120">
                  <template #default="{ row }">{{ formatDate(row.checkOutTime) }}</template>
                </el-table-column>
                <el-table-column prop="roomCount" label="数量" width="60" />
                <el-table-column prop="totalPrice" label="总价" width="100">
                  <template #default="{ row }">¥{{ row.totalPrice }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getStatusType(row.status)">
                      {{ getStatusText(row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right">
                  <template #default="{ row }">
                    <el-button 
                      type="danger" 
                      link 
                      size="small"
                      @click="cancelBookingPrompt(row.id)"
                      v-if="canCancelBooking(row.status)"
                    >取消</el-button>
                    <el-button type="primary" link size="small" @click="viewBookingDetail(row.id)">详情</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            
            <!-- 无预订提示 -->
            <el-empty 
              v-else 
              description="您还没有相关预订记录" 
              :image-size="150"
              class="no-bookings"
            >
              <el-button type="primary" @click="goToBookingPage">立即预订</el-button>
            </el-empty>

            <!-- 分页 -->
            <div class="pagination-container" v-if="bookingPagination.total > 0">
              <el-pagination
                @size-change="handleBookingSizeChange"
                @current-change="handleBookingCurrentChange"
                :current-page="bookingPagination.current"
                :page-sizes="[5, 10, 20]"
                :page-size="bookingPagination.size"
                layout="total, sizes, prev, pager, next, jumper"
                :total="bookingPagination.total"
                background
              />
            </div>
          </div>

          <!-- 积分记录 -->
          <div v-if="activeMenu === 'points'">
            <div class="section-header">
              <h2>积分记录</h2>
            </div>
            <el-table :data="pointsHistory" style="width: 100%" v-loading="pointsLoading" class="points-table">
              <el-table-column prop="createTime" label="日期" width="180">
                <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
              </el-table-column>
              <el-table-column prop="type" label="类型" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.type === 'EARN' ? 'success' : (row.type === 'REDEEM' ? 'warning' : 'info')">
                    {{ getPointsTypeText(row.type) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="pointsChange" label="积分变化" width="100">
                <template #default="{ row }">
                  <span :class="row.pointsChange > 0 ? 'points-up' : 'points-down'">
                    {{ row.pointsChange > 0 ? '+' : '' }}{{ row.pointsChange }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="currentPoints" label="当前积分" width="100" />
              <el-table-column prop="description" label="描述" />
              <el-table-column prop="relatedId" label="关联单号" width="120">
                <template #default="{ row }">
                  <el-link v-if="row.relatedId && row.type === 'EARN'" @click="viewRelatedBooking(row.relatedId)">
                    #{{ row.relatedId }}
                  </el-link>
                  <span v-else-if="row.relatedId">#{{ row.relatedId }}</span>
                </template>
              </el-table-column>
            </el-table>
            
            <!-- 积分分页 -->
            <div class="pagination-container" v-if="pointsPagination.total > 0">
              <el-pagination
                @size-change="handlePointsSizeChange"
                @current-change="handlePointsCurrentChange"
                :current-page="pointsPagination.current"
                :page-sizes="[10, 20, 50]"
                :page-size="pointsPagination.size"
                layout="total, sizes, prev, pager, next, jumper"
                :total="pointsPagination.total"
                background
              />
            </div>
          </div>

          <!-- 账号设置 -->
          <div v-if="activeMenu === 'settings'">
            <div class="section-header">
              <h2>账号设置</h2>
            </div>
            
            <el-collapse v-model="activeCollapse">
              <el-collapse-item title="修改密码" name="password">
                <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" style="max-width: 400px; margin-top: 20px;">
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
                    <el-button type="primary" @click="changePassword" :loading="changingPassword" class="btn-edit-style">
                      确认修改
                    </el-button>
                  </el-form-item>
                </el-form>
              </el-collapse-item>
              
              <el-collapse-item title="订阅设置" name="notifications">
                <el-form label-width="200px" style="max-width: 500px; margin-top: 20px;">
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
                    <el-button type="primary" @click="saveNotificationSettings" :loading="savingNotifications" class="btn-edit-style">
                      保存设置
                    </el-button>
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
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { User, Tickets, Medal, Setting } from '@element-plus/icons-vue'
import { userApi, membershipApi, reservationApi } from '@/api'
import { useAuthStore } from '@/store/auth'

// 确保辅助函数在顶层定义

// 格式化日期 (YYYY-MM-DD)
const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  try {
    // 尝试解析完整 ISO 字符串或日期部分
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) {
        return '无效日期';
    }
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  } catch (e) {
    console.error('Error formatting date:', dateStr, e);
    return '格式化错误';
  }
}

// 格式化日期时间 (YYYY-MM-DD HH:mm)
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-';
  try {
    const date = new Date(dateTimeStr);
     if (isNaN(date.getTime())) {
        return '无效日期时间';
    }
    const datePart = formatDate(dateTimeStr); // Reuse formatDate for the date part
    if (datePart.includes('无效') || datePart.includes('错误')) {
        return datePart; // Return error if date part failed
    }
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    // const seconds = date.getSeconds().toString().padStart(2, '0'); // Optional: include seconds
    return `${datePart} ${hours}:${minutes}`;
     } catch (e) {
    console.error('Error formatting datetime:', dateTimeStr, e);
    return '格式化错误';
  }
}

// 获取积分类型文本
const getPointsTypeText = (type) => {
  switch (type) {
    case 'EARN': return '获得';
    case 'REDEEM': return '兑换';
    case 'EXPIRE': return '过期';
    case 'ADJUST': return '调整';
    default: return '未知';
  }
}

console.log('Profile.vue组件加载')

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const isEditing = ref(false)
const loading = ref(false)
const activeMenu = ref('profile')
const bookingTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// --- 新增：修复 Vue 警告所需的定义 ---
const savingUserInfo = ref(false);
const userFormRef = ref(null); // 确保 userFormRef 已定义
const toggleEdit = () => { isEditing.value = !isEditing.value; };
const userFormRules = reactive({ // 定义基础规则对象
  name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '请输入有效的邮箱地址', trigger: ['blur', 'change'] }]
});
const memberBenefitsData = ref([]); // 用于存储会员权益数据
const goToBookingPage = () => { router.push('/booking'); };
const changingPassword = ref(false);
const savingNotifications = ref(false);
// --- 结束：修复 Vue 警告所需的定义 ---

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
  name: '张三',
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
  console.log(`%c[Menu Click] Received index: ${index}`, 'color: blue; font-weight: bold;');
  activeMenu.value = index;
  console.log(`%c[Menu Click] activeMenu is now: ${activeMenu.value}`, 'color: blue;');
  // 可选：更新 URL 但不重新加载页面
  // router.replace({ query: { tab: index } });
}

// 跳转到预订页面
const goToBooking = () => {
  router.push('/booking')
}

// 新增：加载用户基本信息
const fetchUserInfo = async () => {
  console.log('%c[fetchUserInfo] Attempting to fetch user info...', 'color: purple;');
  try {
    const userId = authStore.userId; // 从 store 获取 userId
    if (!userId) {
         console.error('[fetchUserInfo] User ID not found in auth store.');
         ElMessage.error('用户未登录或会话已过期，请重新登录'); 
         authStore.logout(); // 强制登出
         router.push('/login');
         return;
    }
    const res = await userApi.getUserInfo(userId);
     console.log('[fetchUserInfo] API Response:', res);
    if (res.success && res.data) {
      const userData = res.data;
       // 更新 userInfo (用于显示)
       userInfo.userName = userData.username || '未知用户';
       userInfo.level = userData.memberLevel?.levelName || '普通用户';
       userInfo.points = userData.memberLevel?.points || 0;
       userInfo.totalSpent = userData.memberLevel?.totalSpent || 0;
       
      // 更新 userForm (用于编辑表单)
      Object.assign(userForm, {
          userName: userData.username || '',
          name: userData.name || '',
          phone: userData.phone || '',
          email: userData.email || '',
          // 注意：后端返回的 birthday 可能是字符串，表单需要 Date 对象或 null
          birthday: userData.birthday ? userData.birthday : null, 
          gender: userData.gender || '',
      });
       console.log('[fetchUserInfo] User info updated:', userInfo, userForm);
    } else {
       console.error('[fetchUserInfo] Failed to fetch user info:', res.message);
       ElMessage.error(res.message || '获取用户信息失败');
    }
  } catch (error) {
    console.error('[fetchUserInfo] Error fetching user info:', error);
    ElMessage.error(error.message || '获取用户信息时发生错误');
     // 可以在这里添加登出逻辑，如果获取用户信息是关键操作
     // authStore.logout();
     // router.push('/login');
  }
}

// 保存用户信息
const saveUserInfo = async () => {
  let userId = null; // 在 try 块外部声明
  try {
    // 验证数据有效性
    if (!userForm.userName.trim() || !userForm.name.trim() || !userForm.phone.trim()) {
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
      name: userForm.name,
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
        userForm.name = updateData.name;
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
    
    // 3. Call authStore.logout()
    authStore.logout();
    // 4. Remove localStorage.removeItem calls
    // localStorage.removeItem('userToken')
    // localStorage.removeItem('userName')
    // localStorage.removeItem('userId')
    // localStorage.removeItem('userLevel')
    // localStorage.removeItem('userPoints')
    // localStorage.removeItem('userTotalSpent')
    
    ElMessage.success('已退出登录')
    router.push('/') // 5. Keep router.push
  } catch (error) {
    // 用户取消操作
    if (error !== 'cancel') {
       ElMessage.info('取消退出'); // Improved cancel message handling
    }
  }
}

// 查看预订详情
const viewBookingDetail = (id) => {
  console.log('查看预订详情 ID:', id);
  // 实际跳转逻辑，需要预订详情页路由 '/user/booking/:id'
  if(id) {
      router.push({ name: 'user-booking-detail', params: { id: id } });
  } else {
      console.error("Invalid booking ID for details view");
  }
}

// --- 新增/修改的功能函数 ---

// 判断预订是否可取消
const canCancelBooking = (status) => {
  // 允许取消 'PENDING' 和 'CONFIRMED' 状态的预订
  return ['PENDING', 'CONFIRMED'].includes(status);
};

// 取消预订提示和操作
const cancelBookingPrompt = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消该预订吗? 取消后可能无法恢复。', '取消确认', {
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想',
      type: 'warning',
      confirmButtonClass: 'el-button--danger',
    });

    // 用户确认，执行取消
    const loadingInstance = ElLoading.service({ target: '.bookings-list', text: '正在取消...' });
    try {
      const response = await reservationApi.cancelReservation(id);
      if (response && response.success) {
        ElMessage.success('预订已取消');
        fetchUserBookings(); // 刷新列表
      } else {
        ElMessage.error(response?.message || '取消预订失败');
      }
    } catch (error) {
      console.error('取消预订API调用失败:', error);
      ElMessage.error('取消预订时发生错误: ' + (error?.message || '请稍后重试'));
    } finally {
      loadingInstance.close();
    }

  } catch (action) {
    // 用户点击了"再想想"或关闭了弹窗
    if (action === 'cancel') {
      ElMessage.info('取消操作已取消');
    }
  }
};

// 查看关联预订 (积分记录用)
const viewRelatedBooking = (bookingId) => {
  if (bookingId) {
    // 假设预订详情页路由是 /user/booking/:id
    // 注意：这里可能需要根据实际路由配置调整
    // 也可以考虑直接激活 "我的预订" 标签页并滚动到对应项，但这比较复杂
    // 最直接的是跳转到详情页（如果存在）
    // router.push(`/user/booking/${bookingId}`); 
    // 暂时先切换到预订列表，未来可实现详情跳转
    activeMenu.value = 'bookings';
    ElMessage.info(`已切换到预订列表，请查找预订号 #${bookingId}`);
  } else {
    console.warn('viewRelatedBooking called with invalid bookingId');
  }
};

// --- 结束 新增/修改的功能函数 ---

// 监听预订状态tab变化
// watch(bookingTab, () => {
//   fetchBookingList() // 这个 watch 依赖 fetchBookingList, 已移除
// })

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

// --- 从 Bookings.vue 迁移过来的状态 ---
const userBookings = ref([])
const bookingsLoading = ref(false)
const statusFilter = ref('all') // 预订状态过滤器
const bookingPagination = reactive({
  current: 1,
  size: 5, // 初始每页显示数量
  total: 0,
})
const statusMap = {
    PENDING: { text: '待确认', type: 'warning' },
    CONFIRMED: { text: '已确认', type: 'success' },
    CHECKED_IN: { text: '已入住', type: 'primary' },
    COMPLETED: { text: '已完成', type: 'info' },
    CANCELLED: { text: '已取消', type: 'danger' },
}
// --- 结束 Bookings.vue 迁移状态 ---

// --- 积分记录状态 ---
const pointsHistory = ref([])
const pointsLoading = ref(false)
const pointsPagination = reactive({
  current: 1,
  size: 10,
  total: 0,
})
// --- 结束积分记录状态 ---

// --- 账号设置状态 ---
const activeCollapse = ref(['password']) // 默认展开修改密码

onMounted(() => {
  // console.log('%c[DEBUG Profile.vue onMounted] START', 'color: green; font-weight: bold;');
  fetchUserInfo(); // 首先获取用户信息
  memberBenefitsData.value = getMemberBenefits(); // 添加调用以填充会员权益数据

  // 根据路由查询参数设置默认激活的菜单
  const defaultTab = route.query.tab;
  // console.log(`%c[DEBUG Profile.vue onMounted] route.query.tab: '${defaultTab}'`, 'color: green;');

  if (defaultTab && ['profile', 'bookings', 'points', 'settings'].includes(defaultTab)) {
     activeMenu.value = defaultTab;
     // console.log(`%c[DEBUG Profile.vue onMounted] Initial activeMenu set to: '${activeMenu.value}' based on query.`, 'color: green;');
  } else {
     activeMenu.value = 'profile'; // 明确设置默认值以防万一
     // console.log(`%c[DEBUG Profile.vue onMounted] No valid tab query or query missing. Defaulting activeMenu to: '${activeMenu.value}'`, 'color: green;');
  }
  
  // 根据初始 activeMenu 加载数据
  // console.log(`%c[DEBUG Profile.vue onMounted] Triggering initial data fetch check for activeMenu: '${activeMenu.value}'...`, 'color: green;');
  if (activeMenu.value === 'bookings') {
     // console.log('%c[DEBUG Profile.vue onMounted] Calling fetchUserBookings() for initial load.', 'color: green;');
     fetchUserBookings();
  } else if (activeMenu.value === 'points') {
     // console.log('%c[DEBUG Profile.vue onMounted] Calling fetchPointsHistory() for initial load.', 'color: green;');
     fetchPointsHistory();
  }
  // console.log('%c[DEBUG Profile.vue onMounted] END', 'color: green; font-weight: bold;');
});

// 监听激活菜单变化，按需加载数据
watch(activeMenu, (newMenu, oldMenu) => {
   // console.log(`%c[DEBUG Profile.vue watch activeMenu] START - Value changed from '${oldMenu}' to '${newMenu}'`, 'color: orange; font-weight: bold;');
   // 按需加载逻辑
  if (newMenu === 'bookings') {
    // console.log(`%c[DEBUG Profile.vue watch activeMenu] Checking if bookings need loading (current length: ${userBookings.value.length})`, 'color: orange;');
    // 修改条件：即使列表已有数据也重新加载，以便看到最新预订
    // if (userBookings.value.length === 0) { 
        // console.log('%c[DEBUG Profile.vue watch activeMenu] Calling fetchUserBookings()...', 'color: orange;');
        fetchUserBookings();
    // }
  } else if (newMenu === 'points') {
     // console.log(`%c[DEBUG Profile.vue watch activeMenu] Checking if points need loading (current length: ${pointsHistory.value.length})`, 'color: orange;');
    // 修改条件：即使列表已有数据也重新加载
    // if (pointsHistory.value.length === 0) { 
        // console.log('%c[DEBUG Profile.vue watch activeMenu] Calling fetchPointsHistory()...', 'color: orange;');
        fetchPointsHistory();
    // }
  }
   // console.log('%c[DEBUG Profile.vue watch activeMenu] END', 'color: orange; font-weight: bold;');
   // 更新URL hash 或 query 参数 (可选)
   // router.replace({ query: { tab: newMenu } }); 
});

// --- 从 Bookings.vue 迁移过来的方法 ---
// 获取用户预订列表
const fetchUserBookings = async () => {
  bookingsLoading.value = true;
  const params = {
    page: bookingPagination.current,
    pageSize: bookingPagination.size,
    status: statusFilter.value === 'all' ? null : statusFilter.value,
  };
  // console.log(`%c[DEBUG Profile.vue fetchUserBookings] START - Fetching bookings with params:`, 'color: cyan; font-weight: bold;', params);
  try {
    const res = await reservationApi.getUserReservations(params.page, params.pageSize, params.status);
     // console.log('%c[DEBUG Profile.vue fetchUserBookings] Raw API Response:', 'color: cyan;', res);
    if (res && res.success && res.data) { // Added null check for res
       // Corrected data extraction from Page object
       const bookingsData = res.data.content || []; // Use .content
       const totalBookings = res.data.totalElements || 0; // Use .totalElements
       // console.log(`%c[DEBUG Profile.vue fetchUserBookings] API Success. Received list (from .content):`, 'color: cyan;', bookingsData);
       // console.log(`%c[DEBUG Profile.vue fetchUserBookings] Received total (from .totalElements): ${totalBookings}`, 'color: cyan;');
       userBookings.value = bookingsData;
       bookingPagination.total = totalBookings;
       // console.log(`%c[DEBUG Profile.vue fetchUserBookings] Updated userBookings.value and bookingPagination.total.`, 'color: cyan;');
    } else {
      console.error('[Profile.vue fetchUserBookings] API call failed or returned unexpected structure:', res);
      ElMessage.error(res?.message || '获取预订记录失败');
       userBookings.value = []; // 清空数据
       bookingPagination.total = 0;
    }
  } catch (error) {
    console.error('[Profile.vue fetchUserBookings] API call threw an error:', error);
    ElMessage.error(error?.message || '获取预订记录时发生错误');
     userBookings.value = [];
     bookingPagination.total = 0;
  } finally {
    bookingsLoading.value = false;
    // console.log('%c[DEBUG Profile.vue fetchUserBookings] END', 'color: cyan; font-weight: bold;');
  }
}

// 状态过滤器改变时重新获取数据
const filterAndFetchBookings = () => {
    bookingPagination.current = 1; // 重置到第一页
    fetchUserBookings();
}

// 处理预订分页大小改变
const handleBookingSizeChange = (newSize) => {
  bookingPagination.size = newSize;
  bookingPagination.current = 1; // 页大小改变，回到第一页
  fetchUserBookings();
}

// 处理预订当前页改变
const handleBookingCurrentChange = (newPage) => {
  bookingPagination.current = newPage;
  fetchUserBookings();
}

// 获取积分记录
const fetchPointsHistory = async () => {
    pointsLoading.value = true;
    const params = {
        page: pointsPagination.current,
        pageSize: pointsPagination.size,
    };
     // console.log(`%c[DEBUG Profile.vue fetchPointsHistory] START - Fetching points with params:`, 'color: magenta; font-weight: bold;', params);
    try {
        const res = await membershipApi.getPointsHistory(params.page, params.pageSize);
         // console.log('%c[DEBUG Profile.vue fetchPointsHistory] Raw API Response:', 'color: magenta;', res);
        if (res && res.success && res.data) {
            // Corrected data extraction assuming points API also returns Page
            const historyData = res.data.content || []; // Use .content
            const totalHistory = res.data.totalElements || 0; // Use .totalElements
            // console.log(`%c[DEBUG Profile.vue fetchPointsHistory] API Success. Received list (from .content):`, 'color: magenta;', historyData);
            // console.log(`%c[DEBUG Profile.vue fetchPointsHistory] Received total (from .totalElements): ${totalHistory}`, 'color: magenta;');
            pointsHistory.value = historyData;
            pointsPagination.total = totalHistory;
            // console.log(`%c[DEBUG Profile.vue fetchPointsHistory] Updated pointsHistory.value and pointsPagination.total.`, 'color: magenta;');
        } else {
            console.error('[Profile.vue fetchPointsHistory] API call failed or returned unexpected structure:', res);
            ElMessage.error(res?.message || '获取积分记录失败');
             pointsHistory.value = [];
            pointsPagination.total = 0;
        }
    } catch (error) {
        console.error('[Profile.vue fetchPointsHistory] API call threw an error:', error);
        ElMessage.error(error?.message || '获取积分记录时发生错误');
         pointsHistory.value = [];
        pointsPagination.total = 0;
    } finally {
        pointsLoading.value = false;
         // console.log('%c[DEBUG Profile.vue fetchPointsHistory] END', 'color: magenta; font-weight: bold;');
    }
}

// 处理积分分页大小改变
const handlePointsSizeChange = (newSize) => {
    pointsPagination.size = newSize;
    pointsPagination.current = 1;
    fetchPointsHistory();
}

// 处理积分当前页改变
const handlePointsCurrentChange = (newPage) => {
    pointsPagination.current = newPage;
    fetchPointsHistory();
}

// ... (其他可能需要的辅助函数, 如 getStatusText, getStatusType) ...
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
  background: linear-gradient(135deg, #ffd700, #f0c040);
  color: #6b4f00;
}

.level-diamond {
  background: linear-gradient(135deg, #b9f2ff, #9aceff);
  color: #005a8d;
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

/* Tabs and Pagination Gold Styles */
.booking-tabs :deep(.el-tabs__active-bar) {
  background-color: #c59d5f; /* 主金色 */
}

.booking-tabs :deep(.el-tabs__item.is-active) {
  color: #c59d5f; /* 主金色 */
}

.booking-tabs :deep(.el-tabs__item:hover) {
  color: #c59d5f; /* 主金色 */
}

.pagination-container :deep(.el-pagination.is-background .el-pager li.is-active) {
  background-color: #c59d5f !important; /* 主金色 */
  color: #ffffff !important;
  /* border-radius: 4px; /* Optional: Match button radius */
}

.pagination-container :deep(.el-pagination.is-background .el-pager li.is-active:hover) {
    color: #ffffff !important; 
    background-color: #a07d4a !important; /* 深一点的金色 */
}

.pagination-container :deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
  color: #c59d5f !important; /* 主金色 */
}

 .pagination-container :deep(.el-pagination button:not(:disabled):hover) {
     color: #c59d5f !important; /* 主金色 */
 }
/* End Tabs and Pagination Gold Styles */

/* Switch Gold Style */
.el-switch.is-checked :deep(.el-switch__core) {
  background-color: #c59d5f !important; /* 主金色 */
  border-color: #c59d5f !important; /* 主金色 */
}
/* End Switch Gold Style */
</style>