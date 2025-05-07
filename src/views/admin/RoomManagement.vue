<template>
  <div class="room-management-container">
    <div class="page-header">
      <div class="header-content">
        <h2><span class="gradient-text">房间管理</span></h2>
        <p class="header-description">管理酒店房间信息、房型和状态</p>
      </div>
    </div>

    <!-- 操作栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="filterOptions" class="search-form">
        <el-form-item label="楼层">
          <el-select v-model="filterOptions.floorFilter" placeholder="楼层" clearable>
            <el-option label="全部楼层" value=""></el-option>
            <el-option v-for="floor in floorOptions" :key="floor" :label="`${floor}层`" :value="floor"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="房型">
          <el-select v-model="filterOptions.typeFilter" placeholder="房型" clearable>
            <el-option label="全部房型" value=""></el-option>
            <el-option v-for="type in roomTypes" :key="type.id" :label="type.name" :value="type.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterOptions.statusFilter" placeholder="状态" clearable>
            <el-option label="全部状态" value=""></el-option>
            <el-option v-for="status in statusOptions" :key="status.value" :label="status.label" :value="status.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="房间号">
          <el-input v-model="filterOptions.searchKeyword" placeholder="搜索房间号" clearable prefix-icon="Search"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="openAddRoomDialog" class="add-button">
             <el-icon class="button-icon"><Plus /></el-icon>添加房间
          </el-button>
          <el-button type="warning" @click="openBatchAddRoomDialog" class="batch-add-button" style="margin-left: 10px;">
            <el-icon class="button-icon"><Upload /></el-icon>批量添加
          </el-button>
          <el-button type="success" @click="openAddRoomTypeDialog" class="add-type-button">
             <el-icon class="button-icon"><Plus /></el-icon>添加房型
          </el-button>
          <el-button type="info" plain @click="openManageRoomTypeDialog" class="manage-type-button">
             <el-icon class="button-icon"><Setting /></el-icon>管理房型
          </el-button>
          <el-button @click="resetFilters" class="reset-button">
             <el-icon class="button-icon"><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 房间列表 -->
    <el-card class="list-card">
      <div class="list-header">
        <div class="list-title">房间列表</div>
        <div class="list-summary">共 <span class="highlight-text">{{ totalRooms }}</span> 间房</div>
      </div>
      <el-table 
        :data="filteredRooms" 
        style="width: 100%" 
        border 
        stripe
        v-loading="loading"
        class="staff-table">
        <el-table-column prop="roomNumber" label="房间号" width="120" sortable></el-table-column>
        <el-table-column prop="floor" label="楼层" width="120" sortable></el-table-column>
        <el-table-column label="房型" min-width="160">
          <template #default="scope">
            {{ scope.row.roomType ? scope.row.roomType.name : '未分配' }}
          </template>
        </el-table-column>
        <el-table-column label="基础价格" width="120" sortable>
          <template #default="scope">
            ¥{{ scope.row.roomType ? scope.row.roomType.basePrice : '0.00' }}
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="160">
          <template #default="scope">
            {{ scope.row.notes || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="150">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="light">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="180" sortable>
          <template #default="scope">
            {{ formatDateTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="editRoom(scope.row)">编辑</el-button>
            <el-button size="small" type="warning" @click="changeRoomStatus(scope.row)">修改状态</el-button>
            <el-button size="small" type="danger" @click="confirmDeleteRoom(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalRooms"
        @size-change="loadRooms"
        @current-change="loadRooms">
      </el-pagination>
    </div>

    <!-- 添加房间对话框 -->
    <el-dialog
      v-model="addRoomDialogVisible"
      :title="isEditing ? '编辑房间' : '添加新房间'"
      width="600px"
      destroy-on-close
      class="custom-dialog">
      <el-form :model="roomForm" :rules="roomRules" ref="roomFormRef" label-width="120px" class="custom-form">
        <el-form-item label="房间号" prop="roomNumber">
          <el-input v-model="roomForm.roomNumber" placeholder="例如：301"></el-input>
        </el-form-item>
        <el-form-item label="楼层" prop="floor">
          <el-input-number v-model="roomForm.floor" :min="1" :max="20" placeholder="楼层" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="房型" prop="roomTypeId">
          <el-select v-model="roomForm.roomTypeId" placeholder="选择房型" style="width: 100%">
            <el-option v-for="type in roomTypes" :key="type.id" :label="type.name" :value="type.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="roomForm.status" placeholder="选择状态" style="width: 100%">
            <el-option v-for="status in statusOptions" :key="status.value" :label="status.label" :value="status.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="notes">
          <el-input v-model="roomForm.notes" type="textarea" :rows="3" placeholder="房间备注信息"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addRoomDialogVisible = false" class="cancel-button">取消</el-button>
          <el-button type="primary" @click="submitRoomForm" :loading="submitting" class="confirm-button">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 添加房型对话框 -->
    <el-dialog
      v-model="addRoomTypeDialogVisible"
      :title="isEditingType ? '编辑房型' : '添加新房型'"
      width="600px"
      destroy-on-close
      class="custom-dialog">
      <el-form :model="roomTypeForm" :rules="roomTypeRules" ref="roomTypeFormRef" label-width="120px" class="custom-form">
        <el-form-item label="房型名称" prop="name">
          <el-input v-model="roomTypeForm.name" placeholder="例如：豪华大床房"></el-input>
        </el-form-item>
        <el-form-item label="基础价格" prop="basePrice">
          <el-input-number v-model="roomTypeForm.basePrice" :min="0" :step="10" :precision="2" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="roomTypeForm.capacity" :min="1" :max="10" placeholder="可住人数" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="设施" prop="amenities">
          <el-input v-model="roomTypeForm.amenities" type="textarea" :rows="3" placeholder="房间设施，用逗号分隔"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="roomTypeForm.description" type="textarea" :rows="3" placeholder="房型详细描述"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addRoomTypeDialogVisible = false" class="cancel-button">取消</el-button>
          <el-button type="primary" @click="submitRoomTypeForm" :loading="submitting" class="confirm-button">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 管理房型对话框 -->
    <el-dialog
      v-model="manageRoomTypeDialogVisible"
      title="管理房型"
      width="80%"
      destroy-on-close
      class="custom-dialog manage-type-dialog">
      <el-table :data="roomTypes" style="width: 100%" border stripe>
        <el-table-column prop="name" label="房型名称" min-width="150"></el-table-column>
        <el-table-column prop="basePrice" label="基础价格" width="120">
          <template #default="scope">¥{{ scope.row.basePrice.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="capacity" label="容量" width="100">
          <template #default="scope">{{ scope.row.capacity }}人</template>
        </el-table-column>
        <el-table-column prop="amenities" label="设施" min-width="200"></el-table-column>
        <el-table-column prop="description" label="描述" min-width="200"></el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="editRoomTypeFromManageDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="confirmDeleteRoomType(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="manageRoomTypeDialogVisible = false" class="cancel-button">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 修改房间状态对话框 -->
    <el-dialog
      v-model="changeStatusDialogVisible"
      title="修改房间状态"
      width="400px"
      destroy-on-close
      class="custom-dialog">
      <el-form :model="statusForm" ref="statusFormRef" label-width="120px" class="custom-form">
        <el-form-item label="当前状态">
          <el-tag :type="getStatusType(statusForm.currentStatus)" effect="light">
            {{ getStatusText(statusForm.currentStatus) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="新状态" prop="newStatus">
          <el-select v-model="statusForm.newStatus" placeholder="选择新状态" style="width: 100%">
            <el-option v-for="status in statusOptions" :key="status.value" :label="status.label" :value="status.value"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="changeStatusDialogVisible = false" class="cancel-button">取消</el-button>
          <el-button type="primary" @click="submitStatusChange" :loading="submitting" class="confirm-button">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量添加房间对话框 -->
    <el-dialog
      v-model="batchAddRoomDialogVisible"
      title="批量添加房间"
      width="80%"
      destroy-on-close
      class="custom-dialog batch-add-dialog">
      <el-table :data="batchRoomsData" style="width: 100%" border stripe>
        <el-table-column label="房间号" width="120">
          <template #default="scope">
            <el-input v-model="scope.row.roomNumber" placeholder="例如：301"></el-input>
          </template>
        </el-table-column>
        <el-table-column label="楼层" width="120">
          <template #default="scope">
            <el-input-number v-model="scope.row.floor" :min="1" :max="20" placeholder="楼层" style="width: 100%"></el-input-number>
          </template>
        </el-table-column>
        <el-table-column label="房型" min-width="160">
          <template #default="scope">
            <el-select v-model="scope.row.roomTypeId" placeholder="选择房型" style="width: 100%">
              <el-option v-for="type in roomTypes" :key="type.id" :label="type.name" :value="type.id"></el-option>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="150">
          <template #default="scope">
            <el-select v-model="scope.row.status" placeholder="选择状态" style="width: 100%">
              <el-option v-for="status in statusOptions" :key="status.value" :label="status.label" :value="status.value"></el-option>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="200">
          <template #default="scope">
            <el-input v-model="scope.row.notes" type="textarea" :rows="2" placeholder="房间备注信息"></el-input>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="scope">
            <el-button size="small" type="danger" @click="removeBatchRow(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="batch-add-footer">
        <el-button type="primary" @click="addBatchRow" class="add-row-button">添加一行</el-button>
        <el-button @click="batchAddRoomDialogVisible = false" class="cancel-button">取消</el-button>
        <el-button type="success" @click="submitBatchRoomForm" :loading="submitting" class="confirm-button">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import apiClient from '@/api';

// 状态定义
const loading = ref(false);
const submitting = ref(false);

// 分页参数
const currentPage = ref(1);
const pageSize = ref(10);
const totalRooms = ref(0);

// 房间列表
const rooms = ref([]);
const roomTypes = ref([]);

// 筛选选项
const filterOptions = reactive({
  floorFilter: '',
  typeFilter: '',
  statusFilter: '',
  searchKeyword: '',
});

// 重置筛选条件
const resetFilters = () => {
  filterOptions.floorFilter = '';
  filterOptions.typeFilter = '';
  filterOptions.statusFilter = '';
  filterOptions.searchKeyword = '';
  // 如果需要重置后立即刷新列表，取消下一行注释
  // loadRooms(); 
};

// 楼层选项（从1到20）
const floorOptions = Array.from({ length: 20 }, (_, i) => i + 1);

// 状态选项
const statusOptions = [
  { value: 'AVAILABLE', label: '可用' },
  { value: 'OCCUPIED', label: '已入住' },
  { value: 'RESERVED', label: '已预订' },
  { value: 'MAINTENANCE', label: '维护中' },
  { value: 'CLEANING', label: '清洁中' },
  { value: 'NEEDS_CLEANING', label: '待清洁' },
];

// 对话框状态
const addRoomDialogVisible = ref(false);
const addRoomTypeDialogVisible = ref(false);
const changeStatusDialogVisible = ref(false);
const isEditing = ref(false);
const isEditingType = ref(false);
const manageRoomTypeDialogVisible = ref(false);
const batchAddRoomDialogVisible = ref(false);

// 表单对象
const roomFormRef = ref(null);
const roomTypeFormRef = ref(null);
const statusFormRef = ref(null);

// 房间表单
const roomForm = reactive({
  id: null,
  roomNumber: '',
  floor: 1,
  roomTypeId: null,
  status: 'AVAILABLE',
  notes: '',
});

// 房型表单
const roomTypeForm = reactive({
  id: null,
  name: '',
  basePrice: 100,
  capacity: 2,
  amenities: '',
  description: '',
});

// 状态修改表单
const statusForm = reactive({
  roomId: null,
  currentStatus: '',
  newStatus: '',
});

// 批量添加表单数据
const batchRoomsData = ref([]);

// 表单验证规则
const roomRules = {
  roomNumber: [
    { required: true, message: '请输入房间号', trigger: 'blur' },
    { pattern: /^[0-9A-Z]{3,6}$/, message: '房间号格式不正确', trigger: 'blur' }
  ],
  floor: [
    { required: true, message: '请选择楼层', trigger: 'change' }
  ],
  roomTypeId: [
    { required: true, message: '请选择房型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
};

const roomTypeRules = {
  name: [
    { required: true, message: '请输入房型名称', trigger: 'blur' }
  ],
  basePrice: [
    { required: true, message: '请输入基础价格', trigger: 'blur' }
  ],
  capacity: [
    { required: true, message: '请输入房间容量', trigger: 'blur' }
  ],
};

// 根据筛选条件过滤房间
const filteredRooms = computed(() => {
  return rooms.value.filter(room => {
    // 过滤楼层
    if (filterOptions.floorFilter && room.floor !== parseInt(filterOptions.floorFilter)) {
      return false;
    }
    
    // 过滤房型
    if (filterOptions.typeFilter && (!room.roomType || room.roomType.id !== filterOptions.typeFilter)) {
      return false;
    }
    
    // 过滤状态
    if (filterOptions.statusFilter && room.status !== filterOptions.statusFilter) {
      return false;
    }
    
    // 搜索房间号
    if (filterOptions.searchKeyword && !room.roomNumber.toLowerCase().includes(filterOptions.searchKeyword.toLowerCase())) {
      return false;
    }
    
    return true;
  });
});

// 加载房间列表
const loadRooms = async () => {
  loading.value = true;
  try {
    // 准备请求参数，包含分页和筛选
    const params = {
      page: currentPage.value - 1, // 后端分页从 0 开始
      size: pageSize.value,
      // 只有当筛选条件有值时才传递，否则传递 null 或 undefined (取决于后端接受方式，通常不传即可)
      floor: filterOptions.floorFilter || null,
      roomTypeId: filterOptions.typeFilter || null,
      status: filterOptions.statusFilter || null,
      keyword: filterOptions.searchKeyword || null,
    };

    // 清理 params 对象，移除值为 null 或空字符串的键
    Object.keys(params).forEach(key => {
      if (params[key] === null || params[key] === '') {
        delete params[key];
      }
    });

    // 使用 params 调用 API
    const response = await apiClient.get('/api/admin/rooms', { params });

    // 检查响应是否成功且包含 data 字段
    if (response && response.success && response.data) {
      rooms.value = response.data.list; // 从 data.list 获取房间列表
      totalRooms.value = response.data.total; // 从 data.total 获取总数
      // 仅在首次加载或需要时加载房型
      if (roomTypes.value.length === 0) {
          await loadRoomTypes(); 
      }
    } else {
      // 处理获取数据失败的情况
      console.error('加载房间失败: 响应格式不正确或失败', response);
      ElMessage.error(response?.message || '加载房间列表失败，响应格式不正确');
      rooms.value = []; // 清空列表
      totalRooms.value = 0; // 重置总数
    }

  } catch (error) {
    console.error('加载房间失败 (catch):', error);
    ElMessage.error(error.message || '加载房间列表时发生未知错误');
    rooms.value = [];
    totalRooms.value = 0;
  } finally {
    loading.value = false;
  }
};

// 加载房型列表
const loadRoomTypes = async () => {
  try {
    const response = await apiClient.get('/api/admin/roomtypes');
    
    // --- 后端 AdminDashboardController @GetMapping("/roomtypes") 返回 { success: true, data: [...] } 格式 --- 
    
    if (response && response.success && response.data) {
        roomTypes.value = response.data; // 直接使用 data 数组
    } else {
        console.error('加载房型失败: 响应格式不正确或失败', response);
        ElMessage.error(response?.message || '加载房型列表失败，响应格式不正确');
        roomTypes.value = []; // 清空
    }
    
  } catch (error) {
    console.error('加载房型失败 (catch):', error);
    ElMessage.error(error.message || '加载房型列表时发生未知错误');
    roomTypes.value = []; // 清空
  }
};

// 打开添加房间对话框
const openAddRoomDialog = () => {
  isEditing.value = false;
  // 重置表单
  Object.assign(roomForm, {
    id: null,
    roomNumber: '',
    floor: 1,
    roomTypeId: null,
    status: 'AVAILABLE',
    notes: '',
  });
  addRoomDialogVisible.value = true;
};

// 打开添加房型对话框
const openAddRoomTypeDialog = () => {
  isEditingType.value = false;
  // 重置表单
  Object.assign(roomTypeForm, {
    id: null,
    name: '',
    basePrice: 100,
    capacity: 2,
    amenities: '',
    description: '',
  });
  addRoomTypeDialogVisible.value = true;
};

// 编辑房间
const editRoom = (room) => {
  isEditing.value = true;
  Object.assign(roomForm, {
    id: room.id,
    roomNumber: room.roomNumber,
    floor: room.floor,
    roomTypeId: room.roomType ? room.roomType.id : null,
    status: room.status,
    notes: room.notes || '',
  });
  addRoomDialogVisible.value = true;
};

// 确认删除房间
const confirmDeleteRoom = (room) => {
  ElMessageBox.confirm(
    `确定要删除房间 ${room.roomNumber} 吗？此操作不可撤销！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      deleteRoom(room.id);
    })
    .catch(() => {
      ElMessage({
        type: 'info',
        message: '已取消删除',
      });
    });
};

// 删除房间
const deleteRoom = async (roomId) => {
  loading.value = true;
  try {
    await apiClient.delete(`/api/admin/rooms/${roomId}`);
    ElMessage.success('删除房间成功');
    await loadRooms();
  } catch (error) {
    console.error('删除房间失败 (catch):', error);
    ElMessage.error(error.message || '删除房间失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 提交房间表单
const submitRoomForm = async () => {
  if (!roomFormRef.value) return;
  
  await roomFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        const roomData = {
          ...roomForm,
          roomType: {
            id: roomForm.roomTypeId
          }
        };
        
        if (isEditing.value) {
          // 更新房间
          await apiClient.put(`/api/admin/rooms/${roomForm.id}`, roomData);
          ElMessage.success('更新房间成功');
        } else {
          // 添加新房间
          await apiClient.post('/api/admin/rooms', roomData);
          ElMessage.success('添加房间成功');
        }
        
        addRoomDialogVisible.value = false;
        await loadRooms();
      } catch (error) {
        console.error('保存房间失败 (catch):', error);
        ElMessage.error(error.message || '保存房间失败，请重试');
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 提交房型表单
const submitRoomTypeForm = async () => {
  if (!roomTypeFormRef.value) return;
  
  await roomTypeFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        if (isEditingType.value) {
          // 更新房型 - 使用正确的路径
          await apiClient.put(`/api/admin/roomtypes/${roomTypeForm.id}`, roomTypeForm);
          ElMessage.success('更新房型成功');
        } else {
          // 添加新房型 - 使用正确的路径
          await apiClient.post('/api/admin/roomtypes', roomTypeForm);
          ElMessage.success('添加房型成功');
        }
        
        addRoomTypeDialogVisible.value = false;
        await loadRoomTypes();
      } catch (error) {
        console.error('保存房型失败 (catch):', error);
        ElMessage.error(error.message || '保存房型失败，请重试');
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 修改房间状态
const changeRoomStatus = (room) => {
  statusForm.roomId = room.id;
  statusForm.currentStatus = room.status;
  statusForm.newStatus = '';
  changeStatusDialogVisible.value = true;
};

// 提交状态修改
const submitStatusChange = async () => {
  if (!statusForm.newStatus) {
    ElMessage.warning('请选择新状态');
    return;
  }
  
  submitting.value = true;
  try {
    await apiClient.put(`/api/rooms/${statusForm.roomId}/status`, {
      status: statusForm.newStatus
    });
    ElMessage.success('修改房间状态成功');
    changeStatusDialogVisible.value = false;
    await loadRooms();
  } catch (error) {
    console.error('修改房间状态失败 (catch):', error);
    ElMessage.error(error.message || '修改房间状态失败，请重试');
  } finally {
    submitting.value = false;
  }
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'AVAILABLE':
      return 'success';
    case 'OCCUPIED':
      return 'danger';
    case 'RESERVED':
      return 'warning';
    case 'MAINTENANCE':
      return 'info';
    case 'CLEANING':
      return 'primary';
    case 'NEEDS_CLEANING':
      return 'warning';
    default:
      return 'info';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'AVAILABLE':
      return '可用';
    case 'OCCUPIED':
      return '已入住';
    case 'RESERVED':
      return '已预订';
    case 'MAINTENANCE':
      return '维护中';
    case 'CLEANING':
      return '清洁中';
    case 'NEEDS_CLEANING':
      return '待清洁';
    default:
      return status;
  }
};

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  const date = new Date(dateTime);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  });
};

// 打开管理房型对话框
const openManageRoomTypeDialog = () => {
  manageRoomTypeDialogVisible.value = true;
};

// 从管理对话框编辑房型
const editRoomTypeFromManageDialog = (roomType) => {
  isEditingType.value = true;
  // 填充表单
  Object.assign(roomTypeForm, {
    id: roomType.id,
    name: roomType.name,
    basePrice: roomType.basePrice,
    capacity: roomType.capacity,
    amenities: roomType.amenities,
    description: roomType.description,
  });
  manageRoomTypeDialogVisible.value = false; // 关闭管理对话框
  addRoomTypeDialogVisible.value = true;    // 打开编辑对话框
};

// 确认删除房型
const confirmDeleteRoomType = (roomType) => {
  ElMessageBox.confirm(
    `确定要删除房型 ${roomType.name} 吗？此操作不可撤销！请确保没有房间正在使用此房型。`,
    '警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      deleteRoomType(roomType.id);
    })
    .catch(() => {
      ElMessage({ type: 'info', message: '已取消删除' });
    });
};

// 删除房型
const deleteRoomType = async (typeId) => {
  submitting.value = true; // 可以用 submitting 或 loading
  try {
    const response = await apiClient.delete(`/api/admin/roomtypes/${typeId}`);
    if (response && response.success) {
      ElMessage.success(response.message || '删除房型成功');
      await loadRoomTypes();
    } else {
      // 后端应该在 response.message 中返回具体错误
      ElMessage.error(response?.message || '删除房型失败');
    }
  } catch (error) {
    console.error('删除房型失败 (catch):', error);
    // error.message 通常由 apiClient 拦截器设置
    ElMessage.error(error.message || '删除房型失败，请重试');
  } finally {
    submitting.value = false;
  }
};

// 打开批量添加房间对话框
const openBatchAddRoomDialog = () => {
  batchRoomsData.value = []; // 清空数据
  batchAddRoomDialogVisible.value = true;
};

// 批量添加表格 - 删除一行
const removeBatchRow = (index) => {
  batchRoomsData.value.splice(index, 1);
};

// 提交批量添加表单
const submitBatchRoomForm = async () => {
  submitting.value = true;
  let hasValidationError = false;
  const validationErrors = [];

  // 1. 前端基础校验 & 数据清理
  const roomsPayload = batchRoomsData.value
    .map((row, index) => {
      const rowNum = index + 1;
      // 移除临时 ID
      const { _tempId, ...payloadRow } = row;
      
      // 基础校验
      if (!payloadRow.roomNumber) validationErrors.push(`第 ${rowNum} 行: 房间号不能为空`);
      if (!payloadRow.floor) validationErrors.push(`第 ${rowNum} 行: 楼层不能为空`);
      if (!payloadRow.roomTypeId) validationErrors.push(`第 ${rowNum} 行: 请选择房型`);
      if (!payloadRow.status) validationErrors.push(`第 ${rowNum} 行: 请选择状态`);

      // 可选：更具体的校验
      const roomNumberRegex = /^[0-9A-Z]{1,10}$/;
      if (payloadRow.roomNumber && !roomNumberRegex.test(payloadRow.roomNumber)) {
         validationErrors.push(`第 ${rowNum} 行: 房间号格式不正确 (1-10位字母或数字)`);
      }
      if (payloadRow.floor && (payloadRow.floor < 1 || payloadRow.floor > 50)) {
          validationErrors.push(`第 ${rowNum} 行: 楼层必须在 1 到 50 之间`);
      }

      return payloadRow;
    });

  // 检查是否有空数据行（所有字段都为空或默认值）
  const validRoomsPayload = roomsPayload.filter(row => 
     row.roomNumber || row.floor || row.roomTypeId || row.status || row.notes
  );

  // 如果有校验错误或没有有效数据，则停止提交
  if (validationErrors.length > 0) {
    ElMessageBox.alert(validationErrors.join('<br>'), '输入数据校验失败', {
        dangerouslyUseHTMLString: true,
        type: 'warning',
    });
    submitting.value = false;
    return;
  }
  
  if (validRoomsPayload.length === 0) {
      ElMessage.warning('没有有效的房间数据可供添加，请至少填写一行。');
      submitting.value = false;
      return;
  }

  // 2. 调用后端 API
  try {
    const response = await apiClient.post('/api/admin/rooms/batch', validRoomsPayload);
    
    // 检查后端响应
    if (response && response.success) {
      ElMessage.success(response.message || `成功添加 ${response.data?.count || validRoomsPayload.length} 个房间`);
      batchAddRoomDialogVisible.value = false;
      await loadRooms(); // 刷新列表
    } else {
      // 处理后端校验错误或其他失败情况
      let errorMsg = response?.message || '批量添加房间失败';
      if (response?.errors && Array.isArray(response.errors)) {
        const detailedErrors = response.errors.map(err => 
          `房间 ${err.roomNumber || '-'}: ${err.error}`
        ).join('<br>');
        errorMsg = '批量添加失败，详情如下:<br>' + detailedErrors;
        ElMessageBox.alert(errorMsg, '批量添加失败', { 
          type: 'error',
          dangerouslyUseHTMLString: true
        });
      } else {
         ElMessage.error(errorMsg);
      }
       console.error('批量添加失败:', response);
    }
  } catch (error) {
    // 处理网络错误或 axios 拦截器抛出的错误
    console.error('批量添加请求失败 (catch):', error);
    ElMessage.error(error.message || '批量添加房间时发生网络错误');
  } finally {
    submitting.value = false;
  }
};

// 添加批量添加表格 - 添加一行
const addBatchRow = () => {
  batchRoomsData.value.push({
    _tempId: Date.now(),
    roomNumber: '',
    floor: 1,
    roomTypeId: null,
    status: 'AVAILABLE',
    notes: '',
  });
};

// 初始化
onMounted(() => {
  loadRooms();
});
</script>

<style scoped>
/* 删除原有样式，粘贴 Staff.vue 的样式并清理 */
.staff-container { /* 修改为 room-management-container ? 或者保持通用? 保持 staff-container */
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
  background: linear-gradient(135deg, #3498db, #2c3e50); /* 统一颜色 */
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

.add-button, .invite-button { /* invite-button 可以移除或重命名 */
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

/* .invite-button {
  background: linear-gradient(135deg, #2ecc71, #27ae60);
} */
/* 添加 add-type-button 样式，使用 success 颜色 */
.add-type-button {
  border: none;
  padding: 12px 24px; /* 保持一致 */
  border-radius: 12px;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  font-weight: 500;
  letter-spacing: 0.5px;
  position: relative;
  overflow: hidden;
  z-index: 1;
  background: linear-gradient(135deg, #67c23a, #5cb85c); /* Success gradient */
  color: white; 
}

.add-button::before, .add-type-button::before { /* .invite-button::before */
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

.add-type-button::before { /* .invite-button::before */
  background: linear-gradient(135deg, #5cb85c, #4cae4c); /* Darker success gradient */
}

.add-button:hover, .add-type-button:hover { /* .invite-button:hover */
  transform: translateY(-3px);
}

.add-button:hover {
  box-shadow: 0 8px 20px rgba(41, 128, 185, 0.3);
}

.add-type-button:hover {
  box-shadow: 0 8px 20px rgba(88, 184, 92, 0.3);
}

.add-button:hover::before, .add-type-button:hover::before { /* .invite-button:hover::before */
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

.search-button, /* Staff.vue 中没有 search-button, 但可以保留 */
.reset-button {
  display: flex;
  align-items: center;
  border-radius: 10px;
  padding: 10px 18px;
  transition: all 0.3s ease;
  font-weight: 500;
}

/* .search-button {
  background: linear-gradient(135deg, #3498db, #2980b9);
  border: none;
  margin-right: 12px;
} */

/* .search-button:hover {
  background: linear-gradient(135deg, #2980b9, #1a5276);
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(41, 128, 185, 0.2);
} */

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

.staff-table { /* 应用于房间列表 */
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

.role-tag, .status-tag { /* status-tag 可以保留给房间状态 */
  border-radius: 6px;
  padding: 4px 12px;
  font-weight: 500;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1);
  font-size: 13px;
}

/* .work-schedule { ... } - 移除员工特有 */

.table-actions {
  display: flex;
  justify-content: flex-start;
  gap: 8px;
  flex-wrap: nowrap;
  white-space: nowrap;
}

.action-button { /* 通用操作按钮 */
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
  justify-content: center; /* 修改为 center */
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
  color: white; /* 添加颜色 */
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

/* 移除 Staff.vue 特有的样式，例如：排班、邀请码等 */
/* .schedule-dialog ... */
/* .schedule-header ... */
/* .employee-info ... */
/* .employee-avatar ... */
/* ... (其他排班相关) ... */
/* .qr-code ... */
/* .invite-code-container ... */
/* ... (其他邀请码相关) ... */

@media (max-width: 768px) {
  .staff-container { /* or .room-management-container */
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
  
  .add-button, .add-type-button { /* .invite-button */
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
  
  /* 移除 schedule header 的响应式 */
  /* .schedule-header {
    flex-direction: column;
    gap: 16px;
  } */
  
  /* .schedule-actions {
    width: 100%;
  } */
  
  /* .schedule-actions button {
    flex: 1;
  } */
}
</style> 