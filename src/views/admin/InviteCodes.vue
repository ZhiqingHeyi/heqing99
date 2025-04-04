<template>
  <div class="invite-codes-container">
    <div class="page-header">
      <h2>邀请码管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="generateInviteCode">
          <el-icon><Plus /></el-icon>生成新邀请码
        </el-button>
        <el-button type="success" @click="exportInviteCodes" :disabled="filteredInviteCodes.length === 0">
          <el-icon><Download /></el-icon>导出邀请码
        </el-button>
        <el-button type="warning" @click="batchExpireInviteCodes" :disabled="getActiveCodesCount() === 0">
          <el-icon><Delete /></el-icon>批量作废
        </el-button>
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
    
    <el-card class="filter-card">
      <div class="filter-content">
        <span class="filter-label">状态筛选：</span>
        <el-radio-group v-model="inviteCodeFilter" size="small">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="active">有效</el-radio-button>
          <el-radio-button label="used">已使用</el-radio-button>
          <el-radio-button label="expired">已过期</el-radio-button>
        </el-radio-group>
      </div>
    </el-card>

    <el-card class="list-card">
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
        <el-table-column prop="note" label="备注" />
        <el-table-column label="操作" width="180">
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
    </el-card>

    <el-card class="instruction-card">
      <div class="invite-instruction">
        <h3>邀请员工注册流程</h3>
        <ol>
          <li>生成邀请码并设置相应的角色权限和有效期</li>
          <li>将邀请码发送给待注册员工（可复制发送或分享二维码）</li>
          <li>员工在注册页面输入邀请码完成注册</li>
          <li>注册成功后，系统自动分配相应的角色权限</li>
        </ol>
        <div class="register-link">
          <p>员工注册链接：<el-link type="primary" href="/register" target="_blank">{{ baseUrl }}/register</el-link></p>
        </div>
      </div>
    </el-card>

    <!-- 生成邀请码对话框 -->
    <el-dialog
      v-model="generateCodeVisible"
      title="生成邀请码"
      width="500px"
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
            <el-radio :value="'days'">固定天数</el-radio>
            <el-radio :value="'date'">指定日期</el-radio>
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Delete } from '@element-plus/icons-vue'
import QRCode from 'qrcodejs2-fix'

// 基础URL，用于生成链接，替代window.location.origin
const baseUrl = 'http://localhost:3000'

// 邀请码管理状态
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
    status: 'active',
    note: '前台新员工'
  },
  {
    id: 2,
    code: 'HQ2403E4FGH5',
    role: 'cleaner',
    createdAt: '2024-03-25 14:30:00',
    expireAt: '2024-04-01 14:30:00',
    usedBy: '',
    status: 'expired',
    note: '临时人员'
  },
  {
    id: 3,
    code: 'HQ2403I6JKL7',
    role: 'receptionist',
    createdAt: '2024-03-28 09:15:00',
    expireAt: '2024-04-04 09:15:00',
    usedBy: '王小明',
    status: 'used',
    note: '前台实习生'
  },
  {
    id: 4,
    code: 'HQ2404MNOPQ8',
    role: 'cleaner',
    createdAt: '2024-04-02 11:20:00',
    expireAt: '2024-04-09 11:20:00',
    usedBy: '',
    status: 'active',
    note: '清洁部主管招聘'
  },
  {
    id: 5,
    code: 'HQ2404RSTUV9',
    role: 'receptionist',
    createdAt: '2024-04-03 15:45:00',
    expireAt: '2024-04-10 15:45:00',
    usedBy: '',
    status: 'active',
    note: '前台夜班人员'
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
        text: `${baseUrl}/register?code=${row.code}`,
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

// 在组件挂载时执行的逻辑
onMounted(() => {
  console.log('邀请码管理组件已挂载')
})
</script>

<style scoped>
.invite-codes-container {
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

.invite-code-stats {
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-content {
  display: flex;
  align-items: center;
}

.filter-label {
  margin-right: 10px;
  font-weight: 500;
}

.list-card {
  margin-bottom: 20px;
}

.instruction-card {
  margin-bottom: 20px;
}

.stats-container {
  display: flex;
  justify-content: space-between;
}

.stat-item {
  text-align: center;
  padding: 10px 20px;
  border-radius: 4px;
  background-color: #f5f7fa;
  flex: 1;
  margin: 0 10px;
}

.stat-item:first-child {
  margin-left: 0;
}

.stat-item:last-child {
  margin-right: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 500;
  margin-bottom: 5px;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.qr-code-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.qr-code {
  width: 200px;
  height: 200px;
  margin-bottom: 20px;
}

.qr-code-info {
  text-align: center;
  margin-bottom: 20px;
}

.qr-code-info p {
  margin: 5px 0;
}

.qr-actions {
  display: flex;
  justify-content: center;
}

.batch-expire-content {
  text-align: center;
  padding: 20px;
}

.warning-text {
  color: #f56c6c;
  margin-top: 10px;
  font-weight: 500;
}

.invite-instruction {
  padding: 15px;
}

.invite-instruction h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-weight: 500;
  color: #303133;
}

.invite-instruction ol {
  padding-left: 20px;
  margin: 10px 0;
}

.invite-instruction li {
  margin-bottom: 10px;
  color: #606266;
}

.register-link {
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px dashed #dcdfe6;
}
</style> 