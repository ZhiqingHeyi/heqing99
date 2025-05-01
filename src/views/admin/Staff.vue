<template>
  <div class="staff-container">
    <div class="page-header">
      <div class="header-content">
        <h2><span class="gradient-text">员工管理</span></h2>
        <p class="header-description">管理酒店员工信息、排班和权限</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd" class="add-button">
          <el-icon class="button-icon"><Plus /></el-icon>添加员工
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="员工姓名">
          <el-input v-model="searchForm.name" placeholder="请输入员工姓名" clearable prefix-icon="User" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable prefix-icon="Iphone" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择角色" clearable>
            <el-option label="前台" value="receptionist" />
            <el-option label="保洁" value="cleaner" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="在职" value="active" />
            <el-option label="离职" value="inactive" />
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

    <!-- 员工列表 -->
    <el-card class="list-card">
      <div class="list-header">
        <div class="list-title">员工列表</div>
        <div class="list-summary">共 <span class="highlight-text">{{ total }}</span> 名员工</div>
      </div>
      
      <el-table 
        :data="staffList" 
        style="width: 100%" 
        v-loading="loading"
        border
        stripe
        highlight-current-row
        class="staff-table"
      >
        <el-table-column prop="name" label="姓名" min-width="90" />
        <el-table-column prop="role" label="角色" min-width="90" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.role === 'RECEPTIONIST' ? 'primary' : row.role === 'CLEANER' ? 'success' : 'info'" 
              effect="dark"
              class="role-tag"
            >
              {{ row.role === 'RECEPTIONIST' ? '前台' : row.role === 'CLEANER' ? '保洁' : row.role }} 
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="workSchedule" label="工作时间" min-width="150">
          <template #default="{ row }">
            <span class="work-schedule">
              {{ 
                row.workSchedule === 'morning' ? '早班 (6:00-14:00)' : 
                row.workSchedule === 'afternoon' ? '中班 (14:00-22:00)' : 
                '晚班 (22:00-6:00)' 
              }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="joinDate" label="入职日期" min-width="100" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.status === 'active' ? 'success' : 'info'"
              effect="dark"
              class="status-tag"
            >
              {{ row.status === 'active' ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="280">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button type="primary" link @click="handleEdit(row)" class="action-button">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-button type="primary" link @click="handleSchedule(row)" class="action-button">
                <el-icon><Calendar /></el-icon>排班
              </el-button>
              <el-button 
                :type="row.status === 'active' ? 'danger' : 'success'"
                link 
                @click="handleToggleStatus(row)"
                class="action-button"
              >
                <el-icon><component :is="row.status === 'active' ? 'CircleClose' : 'CircleCheck'" /></el-icon>
                {{ row.status === 'active' ? '离职' : '复职' }}
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

    <!-- 添加/编辑员工对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '添加员工' : '编辑员工'"
      v-model="dialogVisible"
      width="500px"
      class="custom-dialog"
      destroy-on-close
    >
      <el-form
        ref="staffFormRef"
        :model="staffForm"
        :rules="staffFormRules"
        label-width="100px"
        class="custom-form"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="staffForm.name" />
        </el-form-item>
        <el-form-item label="用户名" prop="username" v-if="dialogType === 'add'">
          <el-input v-model="staffForm.username" placeholder="用于登录，创建后不可修改"/>
        </el-form-item>
        <el-form-item label="登录密码" prop="password" v-if="dialogType === 'add'">
          <el-input v-model="staffForm.password" type="password" show-password placeholder="设置初始登录密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" v-if="dialogType === 'add'">
          <el-input v-model="staffForm.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="staffForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="前台" value="RECEPTIONIST" />
            <el-option label="保洁" value="CLEANER" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="staffForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="staffForm.email" />
        </el-form-item>
        <el-form-item label="入职日期" prop="joinDate">
          <el-date-picker
            v-model="staffForm.joinDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="工作时间" prop="workSchedule">
          <el-select v-model="staffForm.workSchedule" placeholder="请选择工作时间" style="width: 100%">
            <el-option label="早班 (6:00-14:00)" value="morning" />
            <el-option label="中班 (14:00-22:00)" value="afternoon" />
            <el-option label="晚班 (22:00-6:00)" value="night" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false" class="cancel-button">取消</el-button>
          <el-button type="primary" @click="handleSubmit" class="confirm-button">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 排班对话框 -->
    <el-dialog
      title="员工排班"
      v-model="scheduleVisible"
      width="700px"
      class="custom-dialog schedule-dialog"
      destroy-on-close
    >
      <div class="schedule-header">
        <div class="employee-info">
          <el-avatar :size="50" class="employee-avatar">{{ currentStaff?.name?.charAt(0) }}</el-avatar>
          <div class="employee-details">
            <div class="employee-name">{{ currentStaff?.name }}</div>
            <div class="employee-role">
              <el-tag size="small" :type="currentStaff?.role === 'receptionist' ? 'primary' : 'success'">
                {{ currentStaff?.role === 'receptionist' ? '前台' : '保洁' }}
              </el-tag>
            </div>
          </div>
        </div>
        <div class="schedule-actions">
          <el-button type="info" @click="applyWeekendRest">周末休息</el-button>
          <el-button type="primary" @click="applyRotationSchedule">轮班排期</el-button>
        </div>
      </div>
      
      <el-calendar v-model="currentDate" class="schedule-calendar">
        <template #dateCell="{ data }">
          <div class="calendar-cell" :class="{ 'is-weekend': isWeekend(data.day) }">
            <p class="date-display">{{ formatCalendarDate(data.day) }}</p>
            <el-select
              v-model="scheduleData[data.day]"
              placeholder="选择班次"
              size="small"
              style="width: 100%"
              class="shift-select"
            >
              <el-option label="早班" value="morning" />
              <el-option label="中班" value="afternoon" />
              <el-option label="晚班" value="night" />
              <el-option label="休息" value="rest" />
            </el-select>
          </div>
        </template>
      </el-calendar>
      
      <div class="schedule-legend">
        <div class="legend-item">
          <span class="legend-color morning"></span>
          <span class="legend-label">早班 (6:00-14:00)</span>
        </div>
        <div class="legend-item">
          <span class="legend-color afternoon"></span>
          <span class="legend-label">中班 (14:00-22:00)</span>
        </div>
        <div class="legend-item">
          <span class="legend-color night"></span>
          <span class="legend-label">晚班 (22:00-6:00)</span>
        </div>
        <div class="legend-item">
          <span class="legend-color rest"></span>
          <span class="legend-label">休息</span>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="scheduleVisible = false" class="cancel-button">取消</el-button>
          <el-button type="primary" @click="handleScheduleSubmit" class="confirm-button">保存排班</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 邀请码管理对话框 -->
    <el-dialog
      title="邀请码管理"
      v-model="inviteCodeVisible"
      width="700px"
    >
      <div class="invite-code-container">
        <div class="invite-code-header">
          <div class="left-actions">
            <el-button type="primary" @click="generateInviteCode">生成新邀请码</el-button>
            <el-button type="success" @click="exportInviteCodes" :disabled="filteredInviteCodes.length === 0">
              <el-icon><Download /></el-icon>导出邀请码
            </el-button>
            <el-button type="warning" @click="batchExpireInviteCodes" :disabled="getActiveCodesCount() === 0">
              <el-icon><Delete /></el-icon>批量作废
            </el-button>
          </div>
          <div class="right-actions">
            <el-select v-model="inviteCodeFilter" placeholder="筛选邀请码状态" style="width: 150px;">
              <el-option label="全部" value="all" />
              <el-option label="有效" value="active" />
              <el-option label="已使用" value="used" />
              <el-option label="已过期" value="expired" />
            </el-select>
          </div>
        </div>

        <el-card class="invite-code-stats" shadow="hover">
          <div class="stats-container">
            <div class="stat-item">
              <div class="stat-value">{{ getActiveCodesCount() }}</div>
              <div class="stat-label">有效邀请码</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ getUsedCodesCount() }}</div>
              <div class="stat-label">已使用</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ getExpiredCodesCount() }}</div>
              <div class="stat-label">已过期</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ getTotalCodesCount() }}</div>
              <div class="stat-label">总数量</div>
            </div>
          </div>
        </el-card>
        
        <el-table 
          :data="filteredInviteCodes" 
          style="width: 100%" 
          border 
          v-loading="inviteCodeLoading"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="code" label="邀请码" width="150" />
          <el-table-column prop="role" label="角色权限">
            <template #default="{ row }">
              <el-tag :type="row.role === 'receptionist' ? 'primary' : 'success'">
                {{ row.role === 'receptionist' ? '前台' : '保洁' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" />
          <el-table-column prop="expireAt" label="过期时间" />
          <el-table-column prop="usedBy" label="使用人" />
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="row.status === 'active' ? 'success' : row.status === 'used' ? 'info' : 'danger'">
                {{ row.status === 'active' ? '有效' : row.status === 'used' ? '已使用' : '已过期' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                link 
                :disabled="row.status !== 'active'"
                @click="copyInviteCode(row.code)"
              >
                复制
              </el-button>
              <el-button 
                type="danger" 
                link 
                :disabled="row.status !== 'active'"
                @click="invalidateInviteCode(row)"
              >
                作废
              </el-button>
              <el-button
                type="info"
                link
                @click="showInviteCodeQR(row)"
              >
                二维码
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 生成邀请码对话框 -->
      <el-dialog
        v-model="generateCodeVisible"
        title="生成邀请码"
        width="500px"
        append-to-body
      >
        <el-form :model="inviteCodeForm" label-width="100px">
          <el-form-item label="角色权限" required>
            <el-select v-model="inviteCodeForm.role" placeholder="请选择角色权限" style="width: 100%">
              <el-option label="前台" value="receptionist" />
              <el-option label="保洁" value="cleaner" />
            </el-select>
          </el-form-item>
          <el-form-item label="过期方式" required>
            <el-radio-group v-model="inviteCodeForm.expireType">
              <el-radio label="days">固定天数</el-radio>
              <el-radio label="date">指定日期</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="有效期" required v-if="inviteCodeForm.expireType === 'days'">
            <el-select v-model="inviteCodeForm.expireDays" placeholder="请选择有效期" style="width: 100%">
              <el-option label="1天" :value="1" />
              <el-option label="3天" :value="3" />
              <el-option label="7天" :value="7" />
              <el-option label="15天" :value="15" />
              <el-option label="30天" :value="30" />
              <el-option label="90天" :value="90" />
            </el-select>
          </el-form-item>
          <el-form-item label="截止日期" required v-if="inviteCodeForm.expireType === 'date'">
            <el-date-picker
              v-model="inviteCodeForm.expireDate"
              type="date"
              placeholder="选择过期日期"
              :disabled-date="disablePastDates"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="生成数量" required>
            <el-input-number v-model="inviteCodeForm.count" :min="1" :max="50" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="inviteCodeForm.note" placeholder="可选备注信息" type="textarea" rows="2" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="generateCodeVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmGenerateCode">确定</el-button>
          </span>
        </template>
      </el-dialog>
      
      <!-- 邀请码二维码对话框 -->
      <el-dialog
        v-model="qrCodeVisible"
        title="邀请码二维码"
        width="350px"
        append-to-body
      >
        <div class="qr-code-container">
          <div class="qr-code" ref="qrCodeRef"></div>
          <div class="qr-code-info">
            <p>邀请码：{{ currentInviteCode }}</p>
            <p>角色：{{ currentInviteCodeRole === 'receptionist' ? '前台' : '保洁' }}</p>
            <p>有效期至：{{ currentInviteCodeExpireAt }}</p>
          </div>
          <div class="qr-actions">
            <el-button type="primary" @click="downloadQRCode">下载二维码</el-button>
          </div>
        </div>
      </el-dialog>
      
      <!-- 批量作废对话框 -->
      <el-dialog
        v-model="batchExpireVisible"
        title="批量作废邀请码"
        width="400px"
        append-to-body
      >
        <div class="batch-expire-content">
          <p>您确定要作废以下邀请码吗？</p>
          <p v-if="selectedCodes.length > 0">已选择 {{ selectedCodes.length }} 个邀请码</p>
          <p v-else>将作废所有 {{ getActiveCodesCount() }} 个有效邀请码</p>
          <p class="warning-text">注意：作废操作不可恢复，请谨慎操作！</p>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="batchExpireVisible = false">取消</el-button>
            <el-button type="danger" @click="confirmBatchExpire">确定作废</el-button>
          </span>
        </template>
      </el-dialog>
      
      <!-- 邀请注册说明 -->
      <div class="invite-instruction">
        <h3>邀请员工注册流程</h3>
        <ol>
          <li>生成邀请码并设置相应的角色权限和有效期</li>
          <li>将邀请码发送给待注册员工（可复制发送或分享二维码）</li>
          <li>员工在注册页面输入邀请码完成注册</li>
          <li>注册成功后，系统自动分配相应的角色权限</li>
        </ol>
        <div class="register-link">
          <p>员工注册链接：<el-link type="primary" href="/register" target="_blank">{{ window.location.origin }}/register</el-link></p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Delete, Ticket, Plus, Search, Refresh, Edit, Calendar, CircleClose, CircleCheck, User, Iphone } from '@element-plus/icons-vue'
import QRCode from 'qrcodejs2-fix'
import { useRouter } from 'vue-router'
// 导入 API 函数
import { userApi } from '@/api/index.js'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  name: '',
  phone: '',
  role: '',
  status: ''
})

// 员工列表数据
const loading = ref(false)
// 移除模拟数据，初始化为空数组
const staffList = ref([])

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
// 移除模拟数据，初始化为 0
const total = ref(0)

// 表单对话框
const dialogVisible = ref(false)
const dialogType = ref('add')
const staffFormRef = ref(null)
const staffForm = reactive({
  id: null, // 添加 id 字段，用于编辑时存储
  name: '',
  username: '', // 添加 username
  password: '', // 添加 password
  confirmPassword: '', // 添加 confirmPassword
  role: '',
  phone: '',
  email: '',
  joinDate: '',
  workSchedule: ''
})

// 添加密码验证逻辑
const validatePass = (rule, value, callback) => {
  if (dialogType.value === 'add') { // 只在添加时验证密码
    if (value === '') {
      callback(new Error('请输入密码'))
    } else {
      if (staffForm.confirmPassword !== '') {
        if (!staffFormRef.value) return
        staffFormRef.value.validateField('confirmPassword', () => null)
      }
      callback()
    }
  }
  callback(); // 编辑时不验证
}

// 添加密码验证逻辑
const validatePass2 = (rule, value, callback) => {
  if (dialogType.value === 'add') { // 只在添加时验证确认密码
    if (value === '') {
      callback(new Error('请再次输入密码'))
    } else if (value !== staffForm.password) {
      callback(new Error("两次输入密码不一致!"))
    } else {
      callback()
    }
  }
  callback(); // 编辑时不验证
}

// 表单验证规则
const staffFormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  username: [
    { required: computed(() => dialogType.value === 'add'), message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: computed(() => dialogType.value === 'add'), validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: computed(() => dialogType.value === 'add'), validator: validatePass2, trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  joinDate: [
    { required: true, message: '请选择入职日期', trigger: 'change' }
  ],
  workSchedule: [
    { required: true, message: '请选择工作时间', trigger: 'change' }
  ]
}

// 排班相关
const scheduleVisible = ref(false)
const currentDate = ref(new Date())
const scheduleData = reactive({})
const currentStaffId = ref(null)

// 邀请码管理
const inviteCodeVisible = ref(false)
const generateCodeVisible = ref(false)
const qrCodeVisible = ref(false)
const batchExpireVisible = ref(false)
const inviteCodeLoading = ref(false)
const inviteCodeFilter = ref('all')
const qrCodeRef = ref(null)
const currentInviteCode = ref('')
const currentInviteCodeRole = ref('')
const currentInviteCodeExpireAt = ref('')
const selectedCodes = ref([])

// 邀请码表单
const inviteCodeForm = reactive({
  role: 'receptionist',
  expireType: 'days',
  expireDays: 7,
  expireDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
  count: 1,
  note: ''
})

// 邀请码列表数据
const inviteCodeList = ref([
  {
    id: 1,
    code: 'HQ2404A1BCD3',
    role: 'receptionist',
    createdAt: '2024-04-01 10:00:00',
    expireAt: '2024-04-08 10:00:00',
    usedBy: '',
    status: 'active'
  },
  {
    id: 2,
    code: 'HQ2403E4FGH5',
    role: 'cleaner',
    createdAt: '2024-03-25 14:30:00',
    expireAt: '2024-04-01 14:30:00',
    usedBy: '',
    status: 'expired'
  },
  {
    id: 3,
    code: 'HQ2403I6JKL7',
    role: 'receptionist',
    createdAt: '2024-03-28 09:15:00',
    expireAt: '2024-04-04 09:15:00',
    usedBy: '王小明',
    status: 'used'
  }
])

// 根据筛选条件过滤邀请码列表
const filteredInviteCodes = computed(() => {
  if (inviteCodeFilter.value === 'all') {
    return inviteCodeList.value
  } else {
    return inviteCodeList.value.filter(code => code.status === inviteCodeFilter.value)
  }
})

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchStaffList()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

// 获取员工列表
const fetchStaffList = async () => {
  loading.value = true
  try {
    // 构建查询参数，包含分页和搜索条件
    // 注意：后端 page 参数通常是 0-based，而 el-pagination 是 1-based
    const params = {
      page: currentPage.value - 1, // 转换为 0-based
      pageSize: pageSize.value,
      // 传递搜索表单的值，空字符串会被忽略
      username: searchForm.name || null, // 后端可能用 username
      phone: searchForm.phone || null,
      status: searchForm.status || null,
      // 添加一个参数，明确只获取员工角色
      // (需要后端支持此参数，或者后端 /api/users 默认只返回员工)
    };

    // 调用 API (修正为调用新的 getStaffList)
    const response = await userApi.getStaffList(params); 

    // 检查返回的数据结构，修正为匹配后端实际返回的 { users: [...], total: ... }
    if (response && response.users && typeof response.total !== 'undefined') { 
      staffList.value = response.users; // 使用 'users' key
      total.value = response.total;     // 使用 'total' key
    } else {
      // 如果数据结构不符合预期，给出提示或默认值
      console.warn('获取员工列表数据结构异常:', response);
      staffList.value = [];
      total.value = 0;
      ElMessage.warning('获取员工列表数据格式错误');
    }

  } catch (error) {
    console.error('获取员工列表失败:', error)
    // 显示拦截器或 API 函数中抛出的错误消息
    ElMessage.error(error.message || '获取员工列表失败') 
    staffList.value = []; // 清空列表，避免显示旧数据
    total.value = 0;
  } finally {
    loading.value = false
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchStaffList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchStaffList()
}

// 添加员工
const handleAdd = () => {
  dialogType.value = 'add'
  Object.keys(staffForm).forEach(key => {
    // 重置表单，包括新加的字段
    staffForm[key] = '' 
  })
  staffForm.id = null; // 确保 id 为 null
  dialogVisible.value = true
  // 清除上次的校验结果 (如果存在)
  if (staffFormRef.value) {
    staffFormRef.value.resetFields();
  }
}

// 编辑员工
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.keys(staffForm).forEach(key => {
    // 确保只复制 staffForm 中存在的字段
    if (staffForm.hasOwnProperty(key) && row.hasOwnProperty(key)) {
       staffForm[key] = row[key]
    }
  })
  // 特别设置 id
  staffForm.id = row.id;
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!staffFormRef.value) return
  
  await staffFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response;
        // 准备提交的数据
        const dataToSend = { ...staffForm };
        // 删除不需要发送给后端的字段 (确认密码)
        delete dataToSend.confirmPassword;
        
        // 添加时需要 username 和 password，编辑时不需要传密码(除非修改密码)
        if (dialogType.value === 'add') {
          // 确保 username 和 password 已提供
          if (!dataToSend.username || !dataToSend.password) {
            throw new Error('添加员工时必须提供用户名和密码');
          }
          delete dataToSend.id; 
        } else {
          // 编辑时，通常不发送密码字段，除非有修改密码的功能
          delete dataToSend.password; 
          // 编辑时通常不允许修改 username
          delete dataToSend.username;
        }
        
        // 确认 role 值是大写 (已在 el-option 中修改 value)
        // dataToSend.role = dataToSend.role.toUpperCase(); 

        if (dialogType.value === 'add') {
          response = await userApi.createUser(dataToSend); // 使用 createUser 添加员工
        } else {
          // 确保 staffForm.id 有值
          if (!staffForm.id) {
            throw new Error('缺少员工ID，无法编辑');
          }
          response = await userApi.updateStaffInfo(staffForm.id, dataToSend); // 使用 updateStaffInfo 编辑员工
        }

        // 检查操作是否成功 (根据后端返回的 response 结构判断)
        // 假设后端成功时返回 { success: true, ... } 或类似结构
        if (response && (response.success === true || response.code === 200 || response.userId)) { // 假设成功标识
           ElMessage.success(dialogType.value === 'add' ? '添加成功' : '修改成功')
           dialogVisible.value = false
           fetchStaffList() // 刷新列表
        } else {
           // API 调用成功但业务逻辑失败
           throw new Error(response?.message || (dialogType.value === 'add' ? '添加失败' : '修改失败'));
        }

      } catch (error) {
        console.error('保存员工信息失败:', error)
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

// 排班管理
const handleSchedule = (row) => {
  currentStaffId.value = row.id
  // TODO: 获取员工当前排班数据
  scheduleVisible.value = true
}

const handleScheduleSubmit = async () => {
  try {
    // TODO: 调用后端API保存排班信息
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('排班保存成功')
    scheduleVisible.value = false
  } catch (error) {
    console.error('保存排班信息失败:', error)
    ElMessage.error('保存排班失败')
  }
}

// 在职/离职状态切换
const handleToggleStatus = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要将该员工标记为${row.status === 'active' ? '离职' : '复职'}状态吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用后端API修改员工状态
    await userApi.toggleUserStatus(row.id);
    
    ElMessage.success(`${row.status === 'active' ? '离职' : '复职'}操作成功`);
    // 移除本地状态修改，调用 fetchStaffList 刷新
    // row.status = row.status === 'active' ? 'inactive' : 'active' 
    fetchStaffList(); // 刷新列表以获取最新状态

  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改员工状态失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 邀请码统计函数
const getActiveCodesCount = () => {
  return inviteCodeList.value.filter(code => code.status === 'active').length
}

const getUsedCodesCount = () => {
  return inviteCodeList.value.filter(code => code.status === 'used').length
}

const getExpiredCodesCount = () => {
  return inviteCodeList.value.filter(code => code.status === 'expired').length
}

const getTotalCodesCount = () => {
  return inviteCodeList.value.length
}

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  selectedCodes.value = selection
}

// 禁用过去的日期
const disablePastDates = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

// 显示邀请码管理对话框
const showInviteCodeDialog = () => {
  inviteCodeVisible.value = true
}

// 显示生成邀请码对话框
const generateInviteCode = () => {
  inviteCodeForm.expireType = 'days'
  inviteCodeForm.expireDays = 7
  inviteCodeForm.expireDate = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
  inviteCodeForm.count = 1
  inviteCodeForm.note = ''
  generateCodeVisible.value = true
}

// 确认生成邀请码
const confirmGenerateCode = () => {
  inviteCodeLoading.value = true
  
  // 模拟生成邀请码
  setTimeout(() => {
    const now = new Date()
    let expireDate
    
    if (inviteCodeForm.expireType === 'days') {
      expireDate = new Date(now.getTime() + inviteCodeForm.expireDays * 24 * 60 * 60 * 1000)
    } else {
      expireDate = new Date(inviteCodeForm.expireDate)
    }
    
    for (let i = 0; i < inviteCodeForm.count; i++) {
      // 随机生成邀请码
      const randomCode = 'HQ' + 
        now.getFullYear().toString().slice(2) + 
        (now.getMonth() + 1).toString().padStart(2, '0') + 
        [...Array(6)].map(() => String.fromCharCode(65 + Math.floor(Math.random() * 26))).join('') + 
        Math.floor(Math.random() * 10);
      
      inviteCodeList.value.unshift({
        id: inviteCodeList.value.length + 1,
        code: randomCode,
        role: inviteCodeForm.role,
        createdAt: now.toLocaleString(),
        expireAt: expireDate.toLocaleString(),
        usedBy: '',
        status: 'active',
        note: inviteCodeForm.note
      })
    }
    
    inviteCodeLoading.value = false
    generateCodeVisible.value = false
    ElMessage.success(`成功生成${inviteCodeForm.count}个邀请码`)
  }, 1000)
}

// 复制邀请码
const copyInviteCode = (code) => {
  navigator.clipboard.writeText(code).then(() => {
    ElMessage.success('邀请码已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

// 作废邀请码
const invalidateInviteCode = (row) => {
  ElMessageBox.confirm(
    '确定要作废该邀请码吗？作废后无法恢复。',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const index = inviteCodeList.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      inviteCodeList.value[index].status = 'expired'
      ElMessage.success('邀请码已作废')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 跳转到邀请码管理页面
const goToInviteCodes = () => {
  router.push('/admin/invite-codes')
}

// 显示邀请码二维码
const showInviteCodeQR = (row) => {
  currentInviteCode.value = row.code
  currentInviteCodeRole.value = row.role
  currentInviteCodeExpireAt.value = row.expireAt
  qrCodeVisible.value = true

  // 在对话框打开后生成二维码
  setTimeout(() => {
    if (qrCodeRef.value) {
      qrCodeRef.value.innerHTML = ''
      new QRCode(qrCodeRef.value, {
        text: `${window.location.origin}/register?code=${row.code}`,
        width: 200,
        height: 200,
        colorDark: '#000000',
        colorLight: '#ffffff',
        correctLevel: QRCode.CorrectLevel.H
      })
    }
  }, 100)
}

// 下载二维码
const downloadQRCode = () => {
  const canvas = qrCodeRef.value.querySelector('canvas')
  if (canvas) {
    const url = canvas.toDataURL('image/png')
    const a = document.createElement('a')
    a.download = `邀请码-${currentInviteCode.value}.png`
    a.href = url
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    ElMessage.success('二维码已下载')
  } else {
    ElMessage.error('下载失败，请稍后重试')
  }
}

// 导出邀请码
const exportInviteCodes = () => {
  const codes = filteredInviteCodes.value
  if (codes.length === 0) return
  
  // 创建CSV内容
  let csvContent = '邀请码,角色,创建时间,过期时间,状态,备注\n'
  
  codes.forEach(item => {
    const role = item.role === 'receptionist' ? '前台' : '保洁'
    const status = item.status === 'active' ? '有效' : item.status === 'used' ? '已使用' : '已过期'
    const note = item.note || ''
    
    csvContent += `${item.code},${role},${item.createdAt},${item.expireAt},${status},${note}\n`
  })
  
  // 创建Blob对象并下载
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `邀请码列表_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  
  ElMessage.success('邀请码已导出')
}

// 批量作废邀请码
const batchExpireInviteCodes = () => {
  if (getActiveCodesCount() === 0) {
    ElMessage.warning('没有有效的邀请码可作废')
    return
  }
  
  batchExpireVisible.value = true
}

// 确认批量作废
const confirmBatchExpire = () => {
  let count = 0
  
  if (selectedCodes.value.length > 0) {
    // 作废选中的邀请码
    selectedCodes.value.forEach(selected => {
      if (selected.status === 'active') {
        const index = inviteCodeList.value.findIndex(item => item.id === selected.id)
        if (index !== -1) {
          inviteCodeList.value[index].status = 'expired'
          count++
        }
      }
    })
  } else {
    // 作废所有有效邀请码
    inviteCodeList.value.forEach((item, index) => {
      if (item.status === 'active') {
        inviteCodeList.value[index].status = 'expired'
        count++
      }
    })
  }
  
  batchExpireVisible.value = false
  if (count > 0) {
    ElMessage.success(`成功作废${count}个邀请码`)
  } else {
    ElMessage.info('没有邀请码被作废')
  }
  
  // 清空选择
  selectedCodes.value = []
}

// 初始化
fetchStaffList()
</script>

<style scoped>
.staff-container {
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

.header-actions {
  display: flex;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.add-button, .invite-button {
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

.add-button {
  background: linear-gradient(135deg, #3498db, #2980b9);
}

.invite-button {
  background: linear-gradient(135deg, #2ecc71, #27ae60);
}

.add-button::before, .invite-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.add-button::before {
  background: linear-gradient(135deg, #2980b9, #1a5276);
}

.invite-button::before {
  background: linear-gradient(135deg, #27ae60, #1e8449);
}

.add-button:hover, .invite-button:hover {
  transform: translateY(-3px);
}

.add-button:hover {
  box-shadow: 0 8px 20px rgba(41, 128, 185, 0.3);
}

.invite-button:hover {
  box-shadow: 0 8px 20px rgba(39, 174, 96, 0.3);
}

.add-button:hover::before, .invite-button:hover::before {
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

.staff-table {
  margin: 10px 0;
}

.staff-table :deep(.el-table__header-wrapper) {
  background-color: #f8f9fb;
}

.staff-table :deep(.el-table__header) {
  font-weight: 600;
}

.staff-table :deep(.el-table__header th) {
  background-color: #f5f7fa;
  color: #303133;
  font-size: 15px;
  padding: 16px 0;
}

.staff-table :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.staff-table :deep(.el-table__row td) {
  padding: 16px 0;
}

.staff-table :deep(.el-table__row:hover) {
  background-color: #f0f7ff !important;
}

.role-tag, .status-tag {
  border-radius: 6px;
  padding: 4px 12px;
  font-weight: 500;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1);
  font-size: 13px;
}

.work-schedule {
  font-size: 14px;
  color: #606266;
  padding: 5px 10px;
  background-color: #f8f9fa;
  border-radius: 6px;
  display: inline-block;
}

.table-actions {
  display: flex;
  justify-content: flex-start;
  gap: 8px;
  flex-wrap: nowrap;
  white-space: nowrap;
}

.action-button {
  display: flex;
  align-items: center;
  font-size: 14px;
  padding: 6px 10px;
  border-radius: 6px;
  transition: all 0.25s ease;
  font-weight: 500;
  white-space: nowrap;
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

/* 排班日历样式 */
.schedule-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(to right, #f8f9fa, #f0f7ff);
  border-bottom: 1px solid #f0f0f0;
}

.employee-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.employee-avatar {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  font-weight: 600;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.employee-details {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.employee-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.schedule-actions {
  display: flex;
  gap: 12px;
}

.schedule-calendar {
  padding: 20px;
}

.schedule-calendar :deep(.el-calendar__header) {
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 20px;
  margin-bottom: 20px;
}

.schedule-calendar :deep(.el-calendar__title) {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.calendar-cell {
  height: 120px;
  padding: 10px;
  border-radius: 10px;
  background-color: #f9fafc;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.calendar-cell:hover {
  background-color: #f0f7ff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.calendar-cell.is-weekend {
  background-color: #f0f7ff;
}

.date-display {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 10px 0;
  color: #303133;
}

.shift-select :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.schedule-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 20px;
  border-top: 1px solid #f0f0f0;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.legend-color.morning {
  background: linear-gradient(135deg, #3498db, #2980b9);
}

.legend-color.afternoon {
  background: linear-gradient(135deg, #e67e22, #d35400);
}

.legend-color.night {
  background: linear-gradient(135deg, #9b59b6, #8e44ad);
}

.legend-color.rest {
  background: linear-gradient(135deg, #2ecc71, #27ae60);
}

.legend-label {
  font-size: 13px;
  color: #606266;
}

@media (max-width: 768px) {
  .staff-container {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 20px;
  }
  
  .header-actions {
    width: 100%;
  }
  
  .add-button, .invite-button {
    flex: 1;
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
  
  .schedule-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .schedule-actions {
    width: 100%;
  }
  
  .schedule-actions button {
    flex: 1;
  }
}

.qr-code {
  width: 200px;
  height: 200px;
  margin-bottom: 10px;
}

.qr-code-info {
  text-align: center;
  margin-bottom: 10px;
}

.qr-actions {
  display: flex;
  justify-content: center;
}

.batch-expire-content {
  text-align: center;
}

.warning-text {
  color: #f56c6c;
  margin-top: 10px;
}
</style>