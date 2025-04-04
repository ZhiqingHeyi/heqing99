<template>
  <div class="staff-container">
    <div class="page-header">
      <h2>员工管理</h2>
      <div class="header-actions">
        <el-button type="success" @click="showInviteCodeDialog">邀请码管理</el-button>
        <el-button type="primary" @click="handleAdd">添加员工</el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="员工姓名">
          <el-input v-model="searchForm.name" placeholder="请输入员工姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
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
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 员工列表 -->
    <el-card class="list-card">
      <el-table :data="staffList" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
            <el-tag :type="row.role === 'receptionist' ? 'primary' : 'success'">
              {{ row.role === 'receptionist' ? '前台' : '保洁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="workSchedule" label="工作时间" />
        <el-table-column prop="joinDate" label="入职日期" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ row.status === 'active' ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleSchedule(row)">排班</el-button>
            <el-button 
              type="primary" 
              link 
              :type="row.status === 'active' ? 'danger' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 'active' ? '离职' : '复职' }}
            </el-button>
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
        />
      </div>
    </el-card>

    <!-- 添加/编辑员工对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '添加员工' : '编辑员工'"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        ref="staffFormRef"
        :model="staffForm"
        :rules="staffFormRules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="staffForm.name" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="staffForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="前台" value="receptionist" />
            <el-option label="保洁" value="cleaner" />
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
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 排班对话框 -->
    <el-dialog
      title="员工排班"
      v-model="scheduleVisible"
      width="600px"
    >
      <el-calendar v-model="currentDate">
        <template #dateCell="{ data }">
          <div class="calendar-cell">
            <p>{{ data.day.split('-').slice(1).join('-') }}</p>
            <el-select
              v-model="scheduleData[data.day]"
              placeholder="选择班次"
              size="small"
              style="width: 100px"
            >
              <el-option label="早班" value="morning" />
              <el-option label="中班" value="afternoon" />
              <el-option label="晚班" value="night" />
              <el-option label="休息" value="rest" />
            </el-select>
          </div>
        </template>
      </el-calendar>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="scheduleVisible = false">取消</el-button>
          <el-button type="primary" @click="handleScheduleSubmit">保存排班</el-button>
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
          <el-button type="primary" @click="generateInviteCode">生成新邀请码</el-button>
          <el-select v-model="inviteCodeFilter" placeholder="筛选邀请码状态" style="width: 150px;">
            <el-option label="全部" value="all" />
            <el-option label="有效" value="active" />
            <el-option label="已使用" value="used" />
            <el-option label="已过期" value="expired" />
          </el-select>
        </div>
        
        <el-table :data="filteredInviteCodes" style="width: 100%" border v-loading="inviteCodeLoading">
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
          <el-table-column label="操作" width="150">
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
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 生成邀请码对话框 -->
      <el-dialog
        v-model="generateCodeVisible"
        title="生成邀请码"
        width="400px"
        append-to-body
      >
        <el-form :model="inviteCodeForm" label-width="100px">
          <el-form-item label="角色权限" required>
            <el-select v-model="inviteCodeForm.role" placeholder="请选择角色权限" style="width: 100%">
              <el-option label="前台" value="receptionist" />
              <el-option label="保洁" value="cleaner" />
            </el-select>
          </el-form-item>
          <el-form-item label="有效期" required>
            <el-select v-model="inviteCodeForm.expireDays" placeholder="请选择有效期" style="width: 100%">
              <el-option label="1天" :value="1" />
              <el-option label="3天" :value="3" />
              <el-option label="7天" :value="7" />
              <el-option label="30天" :value="30" />
            </el-select>
          </el-form-item>
          <el-form-item label="生成数量" required>
            <el-input-number v-model="inviteCodeForm.count" :min="1" :max="10" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="generateCodeVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmGenerateCode">确定</el-button>
          </span>
        </template>
      </el-dialog>
      
      <!-- 邀请注册说明 -->
      <div class="invite-instruction">
        <h3>邀请员工注册流程</h3>
        <ol>
          <li>生成邀请码并设置相应的角色权限和有效期</li>
          <li>将邀请码发送给待注册员工</li>
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
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 搜索表单
const searchForm = reactive({
  name: '',
  phone: '',
  role: '',
  status: ''
})

// 员工列表数据
const loading = ref(false)
const staffList = ref([
  {
    name: '李四',
    role: 'receptionist',
    phone: '13800138001',
    email: 'lisi@example.com',
    workSchedule: '早班 (6:00-14:00)',
    joinDate: '2024-01-01',
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
const staffFormRef = ref(null)
const staffForm = reactive({
  name: '',
  role: '',
  phone: '',
  email: '',
  joinDate: '',
  workSchedule: ''
})

// 表单验证规则
const staffFormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
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
const inviteCodeLoading = ref(false)
const inviteCodeFilter = ref('all')

// 邀请码表单
const inviteCodeForm = reactive({
  role: 'receptionist',
  expireDays: 7,
  count: 1
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
    // TODO: 调用后端API获取员工列表
    await new Promise(resolve => setTimeout(resolve, 1000))
    loading.value = false
  } catch (error) {
    console.error('获取员工列表失败:', error)
    ElMessage.error('获取员工列表失败')
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
    staffForm[key] = ''
  })
  dialogVisible.value = true
}

// 编辑员工
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.keys(staffForm).forEach(key => {
    staffForm[key] = row[key]
  })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!staffFormRef.value) return
  
  await staffFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // TODO: 调用后端API保存员工信息
        await new Promise(resolve => setTimeout(resolve, 1000))
        ElMessage.success(dialogType.value === 'add' ? '添加成功' : '修改成功')
        dialogVisible.value = false
        fetchStaffList()
      } catch (error) {
        console.error('保存员工信息失败:', error)
        ElMessage.error('操作失败')
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
    
    // TODO: 调用后端API修改员工状态
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success(`${row.status === 'active' ? '离职' : '复职'}操作成功`)
    row.status = row.status === 'active' ? 'inactive' : 'active'
  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改员工状态失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 显示邀请码管理对话框
const showInviteCodeDialog = () => {
  inviteCodeVisible.value = true
}

// 显示生成邀请码对话框
const generateInviteCode = () => {
  generateCodeVisible.value = true
}

// 确认生成邀请码
const confirmGenerateCode = () => {
  inviteCodeLoading.value = true
  
  // 模拟生成邀请码
  setTimeout(() => {
    const now = new Date()
    const expireDate = new Date(now.getTime() + inviteCodeForm.expireDays * 24 * 60 * 60 * 1000)
    
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
        status: 'active'
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

// 初始化
fetchStaffList()
</script>

<style scoped>
.staff-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.list-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.calendar-cell {
  height: 80px;
  padding: 5px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.invite-code-container {
  margin-bottom: 20px;
}

.invite-code-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.invite-instruction {
  background-color: #f2f6fc;
  padding: 15px;
  border-radius: 4px;
  margin-top: 20px;
}

.invite-instruction h3 {
  margin-top: 0;
  margin-bottom: 10px;
  font-weight: 500;
}

.invite-instruction ol {
  padding-left: 20px;
  margin: 10px 0;
}

.invite-instruction li {
  margin-bottom: 5px;
}

.register-link {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #dcdfe6;
}
</style>