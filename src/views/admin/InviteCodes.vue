<template>
  <div class="invite-codes-container">
    <div class="page-header">
      <div class="header-content">
        <h2><span class="gradient-text">邀请码管理</span></h2>
        <p class="header-description">生成和管理员工注册邀请码</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="generateInviteCode" class="generate-button">
          <el-icon class="button-icon"><Plus /></el-icon>生成新邀请码
        </el-button>
        <el-button type="success" @click="exportInviteCodes" :disabled="filteredInviteCodes.length === 0" class="export-button">
          <el-icon class="button-icon"><Download /></el-icon>导出邀请码
        </el-button>
        <el-button type="warning" @click="batchExpireInviteCodes" :disabled="getActiveCodesCount() === 0" class="expire-button">
          <el-icon class="button-icon"><Delete /></el-icon>批量作废
        </el-button>
      </div>
    </div>

    <div class="dashboard-cards">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon active">
          <el-icon><Tickets /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ getActiveCodesCount() }}</div>
          <div class="stat-label">有效邀请码</div>
        </div>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon used">
          <el-icon><Check /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ getUsedCodesCount() }}</div>
          <div class="stat-label">已使用</div>
        </div>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon expired">
          <el-icon><Timer /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ getExpiredCodesCount() }}</div>
          <div class="stat-label">已过期</div>
        </div>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon total">
          <el-icon><DataAnalysis /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ getTotalCodesCount() }}</div>
          <div class="stat-label">总数量</div>
        </div>
      </el-card>
    </div>
    
    <el-card class="filter-card">
      <div class="filter-content">
        <span class="filter-label">状态筛选：</span>
        <el-radio-group v-model="inviteCodeFilter" size="large">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="active">有效</el-radio-button>
          <el-radio-button label="used">已使用</el-radio-button>
          <el-radio-button label="expired">已过期</el-radio-button>
        </el-radio-group>
      </div>
    </el-card>

    <el-card class="list-card">
      <div class="list-header">
        <div class="list-title">邀请码列表</div>
        <div class="list-summary">共 <span class="highlight-text">{{ filteredInviteCodes.length }}</span> 个邀请码</div>
      </div>
      
      <el-table 
        :data="filteredInviteCodes" 
        style="width: 100%" 
        border 
        v-loading="inviteCodeLoading"
        @selection-change="handleSelectionChange"
        class="invite-code-table"
        stripe
        highlight-current-row
      >
        <el-table-column type="selection" width="55" fixed="left" />
        <el-table-column prop="code" label="邀请码" min-width="180" />
        <el-table-column prop="role" label="角色权限" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.role === 'receptionist' ? 'primary' : row.role === 'cleaner' ? 'success' : 'info'"
              effect="dark"
              class="role-tag"
            >
              {{ row.role === 'receptionist' ? '前台' : row.role === 'cleaner' ? '保洁' : '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="160" />
        <el-table-column prop="expireAt" label="过期时间" min-width="160" />
        <el-table-column prop="usedBy" label="使用状态" min-width="100" align="center" />
        <el-table-column prop="status" label="状态" min-width="90" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.status === 'active' ? 'success' : row.status === 'used' ? 'info' : 'danger'"
              effect="dark"
              class="status-tag"
            >
              {{ row.status === 'active' ? '有效' : row.status === 'used' ? '已使用' : '已过期' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="220">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button 
                type="primary" 
                link 
                :disabled="row.status !== 'active'"
                @click="copyInviteCode(row.code)"
                class="action-button"
              >
                <el-icon><CopyDocument /></el-icon>复制
              </el-button>
              <el-button 
                type="danger" 
                link 
                :disabled="row.status !== 'active'"
                @click="invalidateInviteCode(row)"
                class="action-button"
              >
                <el-icon><Close /></el-icon>作废
              </el-button>
              <el-button
                type="info"
                link
                @click="showInviteCodeQR(row)"
                class="action-button"
              >
                <el-icon><View /></el-icon>二维码
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="instruction-card">
      <div class="card-header">
        <div class="card-title">
          <el-icon class="header-icon"><InfoFilled /></el-icon>
          员工注册流程说明
        </div>
      </div>
      <div class="invite-instruction">
        <div class="instruction-steps">
          <div class="step">
            <div class="step-number">1</div>
            <div class="step-text">生成邀请码并设置相应的角色权限和有效期</div>
          </div>
          <div class="step">
            <div class="step-number">2</div>
            <div class="step-text">将邀请码发送给待注册员工（可复制发送或分享二维码）</div>
          </div>
          <div class="step">
            <div class="step-number">3</div>
            <div class="step-text">员工在注册页面输入邀请码完成注册</div>
          </div>
          <div class="step">
            <div class="step-number">4</div>
            <div class="step-text">注册成功后，系统自动分配相应的角色权限</div>
          </div>
        </div>
        
        <div class="register-link">
          <div class="link-label">员工注册链接：</div>
          <div class="link-content">
            <el-link type="primary" href="/admin/register" target="_blank" class="register-url">
              {{ baseUrl }}/admin/register
            </el-link>
            <el-button size="small" type="primary" plain @click="copyRegisterLink" class="copy-link-btn">
              <el-icon><CopyDocument /></el-icon>
              复制链接
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 生成邀请码对话框 -->
    <el-dialog
      v-model="generateCodeVisible"
      title="生成邀请码"
      width="500px"
      class="custom-dialog"
      destroy-on-close
    >
      <el-form :model="inviteCodeForm" label-width="100px" class="custom-form">
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
          <el-input-number v-model="inviteCodeForm.quantity" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="inviteCodeForm.note" placeholder="可选备注信息" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="generateCodeVisible = false" class="cancel-button">取消</el-button>
          <el-button type="primary" @click="confirmGenerateCode" class="confirm-button">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 邀请码二维码对话框 -->
    <el-dialog
      v-model="qrCodeVisible"
      title="邀请码二维码"
      width="400px"
      class="custom-dialog qr-dialog"
      destroy-on-close
    >
      <div class="qr-code-container">
        <div class="qr-code" ref="qrCodeRef"></div>
        <div class="qr-code-info">
          <div class="qr-info-item">
            <span class="qr-info-label">邀请码：</span>
            <span class="qr-info-value">{{ currentInviteCode }}</span>
          </div>
          <div class="qr-info-item">
            <span class="qr-info-label">角色：</span>
            <span class="qr-info-value">{{ currentInviteCodeRole === 'receptionist' ? '前台' : '保洁' }}</span>
          </div>
          <div class="qr-info-item">
            <span class="qr-info-label">有效期至：</span>
            <span class="qr-info-value">{{ currentInviteCodeExpireAt }}</span>
          </div>
        </div>
        <div class="qr-actions">
          <el-button type="primary" @click="downloadQRCode" class="download-qr-button">
            <el-icon class="button-icon"><Download /></el-icon>下载二维码
          </el-button>
        </div>
      </div>
    </el-dialog>
    
    <!-- 批量作废对话框 -->
    <el-dialog
      v-model="batchExpireVisible"
      title="批量作废邀请码"
      width="450px"
      class="custom-dialog warning-dialog"
      destroy-on-close
    >
      <div class="batch-expire-content">
        <div class="warning-icon">
          <el-icon><WarningFilled /></el-icon>
        </div>
        <p class="expire-title">您确定要作废以下邀请码吗？</p>
        <p class="expire-description" v-if="selectedCodes.length > 0">已选择 <span class="highlight-text">{{ selectedCodes.length }}</span> 个邀请码</p>
        <p class="expire-description" v-else>将作废所有 <span class="highlight-text">{{ getActiveCodesCount() }}</span> 个有效邀请码</p>
        <p class="warning-text">注意：作废操作不可恢复，请谨慎操作！</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchExpireVisible = false" class="cancel-button">取消</el-button>
          <el-button type="danger" @click="confirmBatchExpire" class="confirm-button delete-button">确定作废</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
console.log('[InviteCodes.vue] Script setup started'); // 日志1

import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Delete, CopyDocument, Close, View, Tickets, Check, Timer, DataAnalysis, InfoFilled, WarningFilled } from '@element-plus/icons-vue'
import QRCode from 'qrcodejs2-fix'
import apiClient from '@/api'; // 导入配置好的 axios 实例
import { format } from 'date-fns'; // 导入日期格式化工具

// console.log('[InviteCodes.vue] Imports loaded'); // 移除日志2

// 移除模拟数据
// const inviteCodes = ref([...]) 
const inviteCodes = ref([]) // 初始化为空数组

// 获取应用基础URL (如果需要动态获取，应调整)
// const baseUrl = 'http://localhost:3000' 
// TODO: Consider if baseUrl is needed and how to obtain it if yes. Remove if unused.
const baseUrl = window.location.origin; // 使用 window.location.origin 动态获取基础 URL

// 邀请码加载状态
const inviteCodeLoading = ref(false)

// 邀请码过滤器
const inviteCodeFilter = ref('all')

// 已选中的邀请码
const selectedCodes = ref([])

// 邀请码对话框
const generateCodeVisible = ref(false)
const inviteCodeForm = reactive({
  role: 'receptionist',
  expireType: 'days',
  expireDays: 30,
  expireDate: null,
  quantity: 1,
  note: ''
})

// 禁用过去的日期
const disablePastDates = (time) => {
  return time.getTime() < Date.now() - 86400000; // 禁用今天之前的日期
}

// 二维码对话框
const qrCodeVisible = ref(false)
const currentInviteCode = ref('')
const currentInviteCodeRole = ref('')
const currentInviteCodeExpireAt = ref('')
const qrCodeRef = ref(null)

// 批量作废对话框
const batchExpireVisible = ref(false)

// 辅助函数：计算邀请码状态
const calculateStatus = (code) => {
  const now = new Date();
  const expiryDate = new Date(code.expiryDate);

  if (!code.enabled) {
    return 'expired'; // 后端标记为禁用
  }
  if (expiryDate < now) {
    return 'expired'; // 已过有效期
  }
  // 后端 maxUses 默认为1
  if (code.currentUses >= (code.maxUses || 1)) { 
    return 'used'; // 已达到使用次数
  }
  return 'active'; // 其他情况为有效
};

// 过滤后的邀请码列表 (使用计算状态)
const filteredInviteCodes = computed(() => {
  const codesWithStatus = inviteCodes.value.map(code => ({
    ...code,
    status: calculateStatus(code) // 计算状态
  }));
  
  if (inviteCodeFilter.value === 'all') return codesWithStatus;
  return codesWithStatus.filter(code => code.status === inviteCodeFilter.value);
});

// 获取有效邀请码数量 (使用计算状态)
const getActiveCodesCount = () => {
  return inviteCodes.value.filter(code => calculateStatus(code) === 'active').length;
};

// 获取已使用邀请码数量 (使用计算状态)
const getUsedCodesCount = () => {
  return inviteCodes.value.filter(code => calculateStatus(code) === 'used').length;
};

// 获取已过期邀请码数量 (使用计算状态)
const getExpiredCodesCount = () => {
  return inviteCodes.value.filter(code => calculateStatus(code) === 'expired').length;
};

// 获取总邀请码数量
const getTotalCodesCount = () => {
  return inviteCodes.value.length;
};

// 函数：获取邀请码列表
const fetchInviteCodes = async () => {
  // console.log('[InviteCodes.vue] fetchInviteCodes function started'); // 移除日志5
  inviteCodeLoading.value = true;
  try {
    // console.log('[InviteCodes.vue] Calling API: /api/invitation-codes'); // 移除日志6
    // 使用导入的 apiClient 发起请求
    const response = await apiClient.get('/api/invitation-codes'); 
    // console.log('[InviteCodes.vue] API response received:', response); // 移除日志7
    // 注意：axios 响应拦截器已经处理了 response.data，这里直接使用 response
    if (response && Array.isArray(response)) { // 检查是否为数组
      inviteCodes.value = response.map(code => ({
        id: code.id,
        code: code.code,
        role: code.role ? code.role.toLowerCase() : 'unknown', 
        createdAt: code.createTime ? format(new Date(code.createTime), 'yyyy-MM-dd HH:mm:ss') : '', 
        expireAt: code.expiryDate ? format(new Date(code.expiryDate), 'yyyy-MM-dd HH:mm:ss') : '', 
        usedBy: code.currentUses > 0 ? '已使用' : '', 
        note: '', 
        enabled: code.enabled,
        expiryDate: code.expiryDate,
        currentUses: code.currentUses,
        maxUses: code.maxUses
      }));
    } else {
      console.warn('[InviteCodes.vue] API response is not an array:', response);
      inviteCodes.value = []; 
    }
  } catch (error) { 
    // 错误已在拦截器中打印，这里不再重复打印
    // console.error("[InviteCodes.vue] Error in fetchInviteCodes:", error); 
    ElMessage.error(error.message || '获取邀请码列表失败，请稍后重试'); // 使用拦截器处理后的错误消息
    inviteCodes.value = []; 
  } finally {
    inviteCodeLoading.value = false;
  }
};

// 生成新邀请码
const generateInviteCode = () => {
  // 重置表单
  inviteCodeForm.role = 'receptionist'
  inviteCodeForm.expireType = 'days'
  inviteCodeForm.expireDays = 30
  inviteCodeForm.expireDate = null
  inviteCodeForm.quantity = 1
  inviteCodeForm.note = ''
  
  generateCodeVisible.value = true
}

// 确认生成邀请码 (调用后端API)
const confirmGenerateCode = async () => {
  // 前端表单验证
  if (inviteCodeForm.expireType === 'date' && !inviteCodeForm.expireDate) {
    ElMessage.error('请选择截止日期');
    return;
  }
  if (!inviteCodeForm.role) {
    ElMessage.error('请选择角色权限');
    return;
  }
  if (inviteCodeForm.quantity < 1 || inviteCodeForm.quantity > 50) {
     ElMessage.error('生成数量必须在 1 到 50 之间');
     return;
  }

  inviteCodeLoading.value = true;
  let successCount = 0;
  let failCount = 0;

  // 计算过期时间
  let expiryDateISO;
  if (inviteCodeForm.expireType === 'days') {
    const expireDate = new Date();
    expireDate.setDate(expireDate.getDate() + inviteCodeForm.expireDays);
    expireDate.setHours(23, 59, 59, 999); // 设置为当天结束
    expiryDateISO = expireDate.toISOString();
  } else {
    const selectedDate = new Date(inviteCodeForm.expireDate);
    selectedDate.setHours(23, 59, 59, 999); // 设置为选择日期的结束
    expiryDateISO = selectedDate.toISOString();
  }

  // 准备请求参数
  const roleUpper = inviteCodeForm.role.toUpperCase(); // 转为大写以匹配后端枚举
  const maxUses = 1; // 默认为1次使用

  // 循环调用创建API
  for (let i = 0; i < inviteCodeForm.quantity; i++) {
    try {
      const payload = {
        role: roleUpper,
        maxUses: maxUses,
        expiryDate: expiryDateISO
      };
      // 使用 apiClient
      await apiClient.post('/api/invitation-codes', payload);
      successCount++;
    } catch (error) {
      // console.error(`创建第 ${i + 1} 个邀请码失败:`, error); // 拦截器已处理
      failCount++;
      // 可以在此中断或继续创建剩余的
    }
  }

  inviteCodeLoading.value = false;
  generateCodeVisible.value = false;

  // 显示结果消息
  if (successCount > 0) {
    ElMessage.success(`成功生成 ${successCount} 个邀请码`);
    await fetchInviteCodes(); // 刷新列表
  }
  if (failCount > 0) {
    ElMessage.error(`有 ${failCount} 个邀请码生成失败，请检查后台日志`);
  }
};

// 导出邀请码
const exportInviteCodes = () => {
  ElMessage({
    message: '邀请码导出功能已模拟，实际场景下会导出Excel文件',
    type: 'success',
    duration: 2000
  })
}

// 复制邀请码
const copyInviteCode = (code) => {
  navigator.clipboard.writeText(code).then(() => {
    ElMessage({
      message: '邀请码已复制到剪贴板',
      type: 'success',
      duration: 2000
    })
  }).catch(err => {
    console.error('复制失败:', err)
    ElMessage.error('复制失败，请手动复制')
  })
}

// 复制注册链接
const copyRegisterLink = () => {
  const link = `${baseUrl}/admin/register` // 确保 baseUrl 已定义
  navigator.clipboard.writeText(link).then(() => {
    ElMessage({
      message: '注册链接已复制到剪贴板',
      type: 'success',
      duration: 2000
    })
  }).catch(err => {
    console.error('复制失败:', err)
    ElMessage.error('复制失败，请手动复制')
  })
}

// 显示邀请码二维码
const showInviteCodeQR = (row) => {
  currentInviteCode.value = row.code
  currentInviteCodeRole.value = row.role
  currentInviteCodeExpireAt.value = row.expireAt
  qrCodeVisible.value = true
  
  // 延迟执行以确保DOM已渲染
  setTimeout(() => {
    if (qrCodeRef.value) {
      // 清空之前的二维码
      qrCodeRef.value.innerHTML = ''
      
      // 生成二维码
      new QRCode(qrCodeRef.value, {
        text: `${baseUrl}/admin/register?code=${row.code}`,
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
    
    ElMessage({
      message: '二维码已下载',
      type: 'success',
      duration: 2000
    })
  }
}

// 作废单个邀请码 (调用后端API)
const invalidateInviteCode = (row) => {
  // 检查是否已经是 active 状态，以及是否有 ID
  if (!row || !row.id || calculateStatus(row) !== 'active') { 
    ElMessage.warning('此邀请码无法作废');
    return;
  }
  
  ElMessageBox.confirm(`确定要作废邀请码 ${row.code} 吗？此操作不可恢复。`, '确认作废', {
    confirmButtonText: '确定作废',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => { 
    inviteCodeLoading.value = true;
    try {
      // 使用 apiClient
      await apiClient.put(`/api/invitation-codes/${row.id}/disable`); 
      ElMessage.success('邀请码已成功作废');
      await fetchInviteCodes(); 
    } catch (error) {
      // console.error(`作废邀请码 ${row.code} (ID: ${row.id}) 失败:`, error); // 拦截器已处理
      ElMessage.error(error.message || '作废邀请码失败，请稍后重试');
    } finally {
      inviteCodeLoading.value = false;
    }
  }).catch(() => {
    // 用户取消操作，无需提示
    // ElMessage.info('已取消作废操作');
  });
};

// 批量作废邀请码
const batchExpireInviteCodes = () => {
  if (getActiveCodesCount() === 0) return
  batchExpireVisible.value = true
}

// 确认批量作废 (调用后端API)
const confirmBatchExpire = async () => {
  let codesToDisable = [];
  if (selectedCodes.value.length > 0) {
    // 获取选中行中状态为 active 的 ID
    codesToDisable = selectedCodes.value
      .filter(row => row.id && calculateStatus(row) === 'active')
      .map(row => row.id);
  } else {
    // 获取所有状态为 active 的 ID
    codesToDisable = inviteCodes.value
      .filter(code => code.id && calculateStatus(code) === 'active')
      .map(code => code.id);
  }

  if (codesToDisable.length === 0) {
    ElMessage.warning('没有可作废的有效邀请码');
    batchExpireVisible.value = false;
    return;
  }

  inviteCodeLoading.value = true;
  let successCount = 0;
  let failCount = 0;

  // 使用 Promise.allSettled 来处理所有请求
  const results = await Promise.allSettled(
    codesToDisable.map(id => apiClient.put(`/api/invitation-codes/${id}/disable`))
  );

  results.forEach((result, index) => {
    if (result.status === 'fulfilled') {
      successCount++;
    } else {
      failCount++;
      console.error(`批量作废邀请码 (ID: ${codesToDisable[index]}) 失败:`, result.reason);
    }
  });

  inviteCodeLoading.value = false;
  batchExpireVisible.value = false;
  selectedCodes.value = []; // 清空选择

  // 显示结果消息
  if (successCount > 0) {
    ElMessage.success(`成功作废 ${successCount} 个邀请码`);
    await fetchInviteCodes(); // 刷新列表
  }
  if (failCount > 0) {
    ElMessage.error(`有 ${failCount} 个邀请码作废失败，请检查后台日志`);
    // 如果部分成功，列表已刷新，无需额外操作
  }
  if (successCount === 0 && failCount === 0) { 
      // Should not happen based on previous check, but as a safeguard
      ElMessage.info('没有邀请码被作废');
  }
};

// 表格选择变化
const handleSelectionChange = (selection) => {
  selectedCodes.value = selection
}

// 页面加载时获取数据
onMounted(() => {
  // console.log('[InviteCodes.vue] Component mounted (onMounted hook)'); // 移除日志4
  fetchInviteCodes();
});

// console.log('[InviteCodes.vue] Script setup finished'); // 移除日志8
</script>

<style scoped>
.invite-codes-container {
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

.generate-button, .export-button, .expire-button {
  border: none;
  padding: 12px 16px;
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

.generate-button {
  background: linear-gradient(135deg, #3498db, #2980b9);
}

.export-button {
  background: linear-gradient(135deg, #2ecc71, #27ae60);
}

.expire-button {
  background: linear-gradient(135deg, #e67e22, #d35400);
}

.generate-button::before, .export-button::before, .expire-button::before {
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

.generate-button::before {
  background: linear-gradient(135deg, #2980b9, #1a5276);
}

.export-button::before {
  background: linear-gradient(135deg, #27ae60, #1e8449);
}

.expire-button::before {
  background: linear-gradient(135deg, #d35400, #a04000);
}

.generate-button:hover, .export-button:hover, .expire-button:hover {
  transform: translateY(-3px);
}

.generate-button:hover {
  box-shadow: 0 8px 20px rgba(41, 128, 185, 0.3);
}

.export-button:hover {
  box-shadow: 0 8px 20px rgba(39, 174, 96, 0.3);
}

.expire-button:hover {
  box-shadow: 0 8px 20px rgba(211, 84, 0, 0.3);
}

.generate-button:hover::before, .export-button:hover::before, .expire-button:hover::before {
  opacity: 1;
}

.button-icon {
  margin-right: 8px;
  font-size: 16px;
}

.dashboard-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
  border: none;
  padding: 10px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
}

.stat-card::after {
  content: '';
  position: absolute;
  right: -20px;
  bottom: -20px;
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, transparent, rgba(0, 0, 0, 0.02));
  border-radius: 50%;
  z-index: 0;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  position: relative;
  z-index: 1;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.stat-icon.active {
  background: linear-gradient(135deg, #3498db, #2980b9);
}

.stat-icon.used {
  background: linear-gradient(135deg, #2ecc71, #27ae60);
}

.stat-icon.expired {
  background: linear-gradient(135deg, #e74c3c, #c0392b);
}

.stat-icon.total {
  background: linear-gradient(135deg, #9b59b6, #8e44ad);
}

.stat-content {
  flex: 1;
  position: relative;
  z-index: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  font-family: 'Helvetica Neue', sans-serif;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.filter-card {
  margin-bottom: 24px;
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
  border: none;
}

.filter-card :deep(.el-card__body) {
  padding: 16px 24px;
}

.filter-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.filter-label {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.filter-content :deep(.el-radio-group) {
  flex: 1;
}

.filter-content :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #3498db;
  border-color: #3498db;
  box-shadow: -1px 0 0 0 #3498db;
}

.filter-content :deep(.el-radio-button__inner) {
  padding: 10px 24px;
  border-radius: 8px;
  margin: 0 4px;
  border: 1px solid #dcdfe6;
  font-weight: 500;
}

.filter-content :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 8px;
  margin-left: 0;
}

.filter-content :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 8px;
  margin-right: 0;
}

.list-card {
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
  padding-bottom: 0;
  border: none;
  overflow: hidden;
  margin-bottom: 24px;
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

.invite-code-table {
  margin: 10px 0;
}

.invite-code-table :deep(.el-table__header-wrapper) {
  background-color: #f8f9fb;
}

.invite-code-table :deep(.el-table__header) {
  font-weight: 600;
}

.invite-code-table :deep(.el-table__header th) {
  background-color: #f5f7fa;
  color: #303133;
  font-size: 15px;
  padding: 16px 0;
}

.invite-code-table :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.invite-code-table :deep(.el-table__row td) {
  padding: 16px 0;
}

.invite-code-table :deep(.el-table__row:hover) {
  background-color: #f0f7ff !important;
}

.invite-code-text {
  font-family: 'Consolas', monospace;
  font-weight: 600;
  color: #3498db;
  background-color: #f0f7ff;
  padding: 4px 8px;
  border-radius: 4px;
  letter-spacing: 0.5px;
}

.role-tag, .status-tag {
  border-radius: 6px;
  padding: 4px 12px;
  font-weight: 500;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1);
  font-size: 13px;
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

.instruction-card {
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
  border: none;
  overflow: hidden;
}

.card-header {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(to right, #f8f9fa, #f0f7ff);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 20px;
  color: #3498db;
}

.invite-instruction {
  padding: 24px;
}

.instruction-steps {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.step {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.step-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.step-text {
  font-size: 16px;
  color: #606266;
  padding-top: 5px;
  flex: 1;
}

.register-link {
  margin-top: 24px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.link-label {
  font-weight: 500;
  color: #303133;
  font-size: 16px;
}

.link-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.register-url {
  font-size: 16px;
  font-family: 'Consolas', monospace;
  color: #3498db;
}

.copy-link-btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
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

.delete-button {
  background: linear-gradient(135deg, #e74c3c, #c0392b);
}

.delete-button:hover {
  background: linear-gradient(135deg, #c0392b, #a93226);
  box-shadow: 0 6px 15px rgba(231, 76, 60, 0.2);
}

.cancel-button {
  border: 1px solid #dcdfe6;
}

.cancel-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.08);
  background-color: #f8f9fa;
}

/* 二维码对话框样式 */
.qr-dialog :deep(.el-dialog__body) {
  padding: 30px;
}

.qr-code-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.qr-code {
  width: 200px;
  height: 200px;
  margin: 0 auto;
  padding: 10px;
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
}

.qr-code-info {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.qr-info-item {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.qr-info-label {
  width: 80px;
  color: #606266;
  font-weight: 500;
}

.qr-info-value {
  flex: 1;
  color: #303133;
  font-weight: 500;
}

.qr-actions {
  display: flex;
  justify-content: center;
  width: 100%;
  margin-top: 10px;
}

.download-qr-button {
  background: linear-gradient(135deg, #3498db, #2980b9);
  border: none;
  border-radius: 10px;
  padding: 12px 24px;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.download-qr-button:hover {
  background: linear-gradient(135deg, #2980b9, #1a5276);
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(41, 128, 185, 0.2);
}

/* 批量作废对话框样式 */
.warning-dialog :deep(.el-dialog__body) {
  padding: 30px;
}

.batch-expire-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  text-align: center;
}

.warning-icon {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background-color: #fff3f3;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: #e74c3c;
  margin-bottom: 16px;
}

.expire-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.expire-description {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

.warning-text {
  font-size: 14px;
  color: #e74c3c;
  margin: 10px 0 0;
  font-weight: 500;
  background-color: #fff3f3;
  padding: 12px 16px;
  border-radius: 8px;
  width: 100%;
  text-align: center;
}

@media (max-width: 1200px) {
  .dashboard-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .invite-codes-container {
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
    flex-direction: column;
    gap: 10px;
  }
  
  .generate-button, .export-button, .expire-button {
    width: 100%;
    justify-content: center;
  }
  
  .dashboard-cards {
    grid-template-columns: 1fr;
  }
  
  .filter-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .filter-content :deep(.el-radio-group) {
    width: 100%;
    display: flex;
    justify-content: space-between;
  }
  
  .filter-content :deep(.el-radio-button__inner) {
    padding: 10px 0;
    flex: 1;
    text-align: center;
  }
  
  .table-actions {
    flex-wrap: wrap;
  }
  
  .register-link {
    flex-direction: column;
  }
  
  .link-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .copy-link-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>