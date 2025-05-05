<template>
  <div class="bookings-container">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="header-content">
        <h2><span class="gradient-text">预订管理</span></h2>
        <p class="header-description">管理客户预订信息、确认和入住安排</p>
      </div>
      <el-button type="primary" @click="handleAdd" class="btn-add">
        <el-icon><Plus /></el-icon>新增预订
      </el-button>
    </div>

    <!-- 数据统计卡片 -->
    <div class="stats-container">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="8" :md="8">
          <el-card shadow="hover" class="stats-card pending-card" @click="showPendingBookings">
            <div class="stats-card-content">
              <div class="stats-icon-wrapper">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ pendingCount }}</div>
                <div class="stats-label">待确认预订</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8" :md="8">
          <el-card shadow="hover" class="stats-card confirmed-card" @click="showTodayCheckins">
            <div class="stats-card-content">
              <div class="stats-icon-wrapper">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ todayCheckinCount }}</div>
                <div class="stats-label">今日入住</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8" :md="8">
          <el-card shadow="hover" class="stats-card occupancy-card" @click="showOccupancyDetails">
            <div class="stats-card-content">
              <div class="stats-icon-wrapper">
                <el-icon><User /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ occupancyRate }}%</div>
                <div class="stats-label">入住率</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 快速操作区 -->
    <el-card class="quick-actions-card" shadow="hover">
      <template #header>
        <div class="quick-actions-header">
          <h3>快速操作</h3>
        </div>
      </template>
      <div class="quick-actions">
        <div class="quick-action-item" @click="handleAdd">
          <div class="quick-action-icon">
            <el-icon><Plus /></el-icon>
          </div>
          <span>新增预订</span>
        </div>
        <div class="quick-action-item" @click="openTodayCheckin">
          <div class="quick-action-icon">
            <el-icon><Calendar /></el-icon>
          </div>
          <span>今日入住</span>
        </div>
        <div class="quick-action-item" @click="openTodayCheckout">
          <div class="quick-action-icon">
            <el-icon><Right /></el-icon>
          </div>
          <span>今日离店</span>
        </div>
        <div class="quick-action-item" @click="exportBookings">
          <div class="quick-action-icon">
            <el-icon><Download /></el-icon>
          </div>
          <span>导出数据</span>
        </div>
        <div class="quick-action-item" @click="showRoomStatus">
          <div class="quick-action-icon">
            <el-icon><House /></el-icon>
          </div>
          <span>房态一览</span>
        </div>
      </div>
    </el-card>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="hover">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="预订号">
          <el-input v-model="searchForm.bookingNo" placeholder="请输入预订号" clearable />
        </el-form-item>
        <el-form-item label="客户姓名">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="预订状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待确认" value="pending" />
            <el-option label="已确认" value="confirmed" />
            <el-option label="已入住" value="checked-in" />
            <el-option label="已取消" value="cancelled" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item label="入住日期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            clearable
            @change="dateRangeChanged"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="search-btn">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetSearch" class="reset-btn">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预订列表 -->
    <el-card class="list-card" shadow="hover">
      <el-table 
        :data="bookingList" 
        style="width: 100%" 
        v-loading="loading"
        border
        stripe
        highlight-current-row
        class="booking-table"
      >
        <el-table-column prop="bookingNo" label="预订号" width="120" />
        <el-table-column prop="guestName" label="客户姓名" />
        <el-table-column prop="guestPhone" label="手机号" />
        <el-table-column prop="roomTypeName" label="房间类型" />
        <el-table-column label="房间号">
          <template #default="{ row }">
            <span v-if="['pending', 'confirmed'].includes(row.status?.toLowerCase())">
              未分配
            </span>
            <span v-else>
              {{ row.roomNumber }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="roomPrice" label="房价" width="100">
          <template #default="{ row }">
            <span>¥{{ row.roomPrice || '580' }}/晚</span>
          </template>
        </el-table-column>
        <el-table-column prop="checkInTime" label="入住日期">
          <template #default="{ row }">
            {{ formatDateToYMD(row.checkInTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="checkOutTime" label="离店日期">
          <template #default="{ row }">
            {{ formatDateToYMD(row.checkOutTime) }}
          </template>
        </el-table-column>
        <el-table-column label="支付状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getPaymentStatusType(row.paymentStatus)" effect="light">
              {{ getPaymentStatusText(row.paymentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light" class="status-tag">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons action-buttons-wrap">
              <el-button 
                type="primary" 
                link 
                @click="handleEdit(row)"
                v-if="row.status?.toLowerCase() === 'pending'"
                class="action-btn"
              ><el-icon><Edit /></el-icon>编辑</el-button>
              <el-button 
                type="success" 
                link 
                @click="handleConfirm(row)"
                v-if="row.status?.toLowerCase() === 'pending'"
                class="action-btn"
              ><el-icon><Check /></el-icon>确认</el-button>
              <el-button 
                type="primary" 
                link 
                @click="handleCheckIn(row)"
                v-if="row.status?.toLowerCase() === 'confirmed'"
                class="action-btn"
              ><el-icon><Key /></el-icon>办理入住</el-button>
              <el-button 
                type="warning" 
                link 
                @click="handleQuickPayment(row)"
                v-if="row.status?.toLowerCase() === 'confirmed' && ['UNPAID', 'DEPOSIT_PAID'].includes(row.paymentStatus?.toUpperCase())"
                class="action-btn"
              ><el-icon><Money /></el-icon>快捷支付</el-button>
              <el-button 
                type="danger" 
                link 
                @click="handleCancel(row)"
                v-if="['pending', 'confirmed'].includes(row.status?.toLowerCase())"
                class="action-btn"
              ><el-icon><Close /></el-icon>取消</el-button>
              <el-button 
                type="info" 
                link 
                @click="handleView(row)"
                class="action-btn"
              ><el-icon><View /></el-icon>查看</el-button>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty 
            description="暂无预订数据" 
            :image-size="100"
          >
            <template #description>
              <p>{{ searchForm.status || searchForm.customerName || searchForm.phone || searchForm.bookingNo || searchForm.dateRange ? '没有符合条件的预订数据' : '暂无预订数据' }}</p>
            </template>
            <el-button type="primary" @click="handleAdd">新增预订</el-button>
          </el-empty>
        </template>
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
          class="custom-pagination"
        />
      </div>
    </el-card>

    <!-- 预订表单对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '新增预订' : '编辑预订'"
      v-model="dialogVisible"
      width="650px"
      destroy-on-close
      class="custom-dialog"
    >
      <el-form
        ref="bookingFormRef"
        :model="bookingForm"
        :rules="bookingFormRules"
        label-width="100px"
        class="booking-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户姓名" prop="customerName">
              <el-input v-model="bookingForm.customerName" placeholder="请输入客户姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="bookingForm.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="bookingForm.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="房间类型" prop="roomType">
              <el-select v-model="bookingForm.roomType" placeholder="请选择房间类型" style="width: 100%">
                <el-option
                  v-for="roomType in roomTypeList"
                  :key="roomType.id"
                  :label="`${roomType.name} (¥${roomType.price}/晚)`" 
                  :value="roomType.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房间数量" prop="roomCount">
              <el-input-number v-model="bookingForm.roomCount" :min="1" :max="5" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="入住日期" prop="dateRange">
              <el-date-picker
                v-model="bookingForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="入住日期"
                end-placeholder="离店日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入住人数" prop="guestCount">
              <el-input-number v-model="bookingForm.guestCount" :min="1" :max="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计到店" prop="estimatedArrivalTime">
              <el-time-select
                v-model="bookingForm.estimatedArrivalTime"
                start="08:00"
                step="00:30"
                end="22:00"
                placeholder="请选择预计到店时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预订来源" prop="source">
              <el-select v-model="bookingForm.source" placeholder="请选择预订来源" style="width: 100%">
                <el-option label="直接预订" value="直接预订" />
                <el-option label="电话预订" value="电话预订" />
                <el-option label="在线平台" value="在线平台" />
                <el-option label="旅行社" value="旅行社" />
                <el-option label="合作单位" value="合作单位" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="团队预订">
              <el-switch
                v-model="bookingForm.isGroup"
                active-text="是"
                inactive-text="否"
                class="custom-switch"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="特殊要求" prop="specialRequests">
          <el-input
            v-model="bookingForm.specialRequests"
            type="textarea"
            rows="3"
            placeholder="请输入特殊要求（如：需要加床、安静房等）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="handleSubmit" class="submit-btn">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 入住登记对话框 -->
    <el-dialog
      title="办理入住"
      v-model="checkInVisible"
      width="550px"
      destroy-on-close
      class="custom-dialog"
    >
      <el-form
        ref="checkInFormRef"
        :model="checkInForm"
        :rules="checkInFormRules"
        label-width="100px"
      >
        <el-form-item label="房间号" prop="roomNumber">
          <el-select v-model="checkInForm.roomNumber" placeholder="请选择房间号" style="width: 100%">
            <template v-if="availableRooms && availableRooms.length > 0">
              <el-option
                v-for="(room, index) in availableRooms"
                :key="room.id || room.number || index"
                :label="room.number + (room.type ? ` (${room.type})` : '')"
                :value="room.number"
              />
            </template>
            <el-option
              v-else
              disabled
              value=""
              label="没有可用房间"
            />
          </el-select>
          <div class="room-count-info" v-if="availableRooms.length > 0">
            可用房间: {{ availableRooms.length }}间
          </div>
          <div class="no-rooms-warning" v-else>
            当前没有可用房间
          </div>
        </el-form-item>
        <el-form-item label="押金" prop="deposit">
          <el-input-number v-model="checkInForm.deposit" :min="0" :step="100" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="checkInForm.remarks"
            type="textarea"
            rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="checkInVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="handleCheckInSubmit" class="submit-btn">确认入住</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 房型预览对话框 -->
    <el-dialog
      title="房型预览"
      v-model="roomPreviewVisible"
      width="700px"
      class="custom-dialog room-preview-dialog"
    >
      <el-carousel height="350px" class="room-carousel">
        <el-carousel-item v-for="(image, index) in roomTypeImages[currentRoomType] || []" :key="index">
          <div class="carousel-content">
            <img :src="image.url" alt="房间图片" class="room-image" />
            <div class="image-title">{{ image.title }}</div>
          </div>
        </el-carousel-item>
      </el-carousel>
      
      <div class="room-info">
        <h3>{{ currentRoomType }} 详情</h3>
        <div class="room-details">
          <p><el-icon><User /></el-icon> 可住2人</p>
          <p><el-icon><Monitor /></el-icon> 50寸智能电视</p>
          <p><el-icon><Sugar /></el-icon> 免费早餐</p>
          <p><el-icon><Connection /></el-icon> 高速WiFi</p>
        </div>
        <div class="room-description">
          宽敞舒适的客房配备高品质床垫和床品，提供最佳睡眠体验。配有现代化设施，满足商务和休闲旅客需求。
        </div>
      </div>
    </el-dialog>

    <!-- 酒店房态一览对话框 -->
    <el-dialog
      title="酒店房态一览"
      v-model="roomStatusVisible"
      width="90%"
      class="custom-dialog room-status-dialog no-top-bar"
      destroy-on-close
    >
      <!-- 房态一览组件 -->
      <div class="room-status-container" v-loading="roomStatusLoading">
        <div class="status-header">
          <h3 class="status-title">房态一览</h3> 
          <div class="status-filters">
            <div class="filter-item">
              <span class="filter-label">楼层：</span>
              <el-select v-model="selectedFloor" @change="handleFloorChange" placeholder="选择楼层" style="min-width: 140px;">
                <el-option label="全部楼层" value="all" />
                <el-option v-for="floor in availableFloors" :key="floor" :label="`${floor}层`" :value="floor" />
              </el-select>
            </div>
            <div class="filter-item">
              <span class="filter-label">房型：</span>
              <el-select v-model="selectedRoomType" @change="handleRoomTypeChange" placeholder="选择房型" style="min-width: 180px;">
                <el-option label="全部房型" value="all" />
                <el-option v-for="type in roomTypeList" :key="type.id" :label="type.name" :value="type.id" />
              </el-select>
            </div>
            <div class="filter-item">
              <span class="filter-label">状态：</span>
              <el-select v-model="selectedStatus" @change="handleStatusChange" placeholder="选择状态" style="min-width: 140px;">
                <el-option label="全部状态" value="all" />
                <el-option label="可用" value="AVAILABLE" />
                <el-option label="已入住" value="OCCUPIED" />
                <el-option label="已预订" value="RESERVED" />
                <el-option label="维修中" value="MAINTENANCE" />
                <el-option label="清洁中" value="CLEANING" />
              </el-select>
            </div>
            <el-button type="primary" @click="fetchRoomStatusData()" class="refresh-btn">
              <el-icon><Refresh /></el-icon>刷新
            </el-button>
          </div>
        </div>
        
        <!-- 房间状态统计卡片 -->
        <div class="room-stats-cards">
          <el-row :gutter="20">
            <el-col :span="4">
              <el-card class="stat-card clickable-card available-card" shadow="hover" @click="filterByStatus('AVAILABLE')">
                <div class="stat-card-content">
                  <div class="stat-icon-wrapper available-bg">
                    <el-icon class="stat-icon"><Check /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ availableRoomCount }}</div>
                    <div class="stat-title">可用房间</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="4">
              <el-card class="stat-card clickable-card occupied-card" shadow="hover" @click="filterByStatus('OCCUPIED')">
                <div class="stat-card-content">
                  <div class="stat-icon-wrapper occupied-bg">
                    <el-icon class="stat-icon"><User /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ occupiedRoomCount }}</div>
                    <div class="stat-title">已入住</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="4">
              <el-card class="stat-card clickable-card reserved-card" shadow="hover" @click="filterByStatus('RESERVED')">
                <div class="stat-card-content">
                  <div class="stat-icon-wrapper reserved-bg">
                    <el-icon class="stat-icon"><Calendar /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ reservedRoomCount }}</div> 
                    <div class="stat-title">已预订</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="4">
              <el-card class="stat-card clickable-card cleaning-card" shadow="hover" @click="filterByStatus('CLEANING')">
                <div class="stat-card-content">
                  <div class="stat-icon-wrapper cleaning-bg">
                    <el-icon class="stat-icon"><Brush /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ cleaningRoomCount }}</div>
                    <div class="stat-title">清洁中</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="4">
              <el-card class="stat-card clickable-card maintenance-card" shadow="hover" @click="filterByStatus('MAINTENANCE')">
                <div class="stat-card-content">
                  <div class="stat-icon-wrapper maintenance-bg">
                    <el-icon class="stat-icon"><Tools /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ maintenanceRoomCount }}</div>
                    <div class="stat-title">维修中</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="4">
              <el-card class="stat-card clickable-card occupancy-card" shadow="hover" @click="showOccupancyInfo">
                <div class="stat-card-content">
                  <div class="stat-icon-wrapper occupancy-bg">
                    <el-icon class="stat-icon"><Histogram /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ currentOccupancyRate }}%</div>
                    <div class="stat-title">入住率</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
        
        <!-- 图例说明 -->
        <div class="room-status-legend">
          <div class="legend-item clickable-legend" @click="filterByStatus('AVAILABLE')">
            <div class="legend-color room-available"></div>
            <span>可用</span>
          </div>
          <div class="legend-item clickable-legend" @click="filterByStatus('OCCUPIED')">
            <div class="legend-color room-occupied"></div>
            <span>已入住</span>
          </div>
          <div class="legend-item clickable-legend" @click="filterByStatus('RESERVED')">
            <div class="legend-color room-reserved"></div>
            <span>已预订</span>
          </div>
          <div class="legend-item clickable-legend" @click="filterByStatus('CLEANING')">
            <div class="legend-color room-cleaning"></div>
            <span>清洁中</span>
          </div>
          <div class="legend-item clickable-legend" @click="filterByStatus('MAINTENANCE')">
            <div class="legend-color room-maintenance"></div>
            <span>维修中</span>
          </div>
        </div>
        
        <!-- 按楼层分组显示房间 -->
        <div class="rooms-by-floor">
          <template v-if="!roomStatusList.length && !roomStatusLoading">
              <el-empty description="暂无房间数据" />
          </template>
          
          <template v-else>
            <div v-for="floor in groupedRooms" :key="floor.floor" class="floor-section">
              <div class="floor-header">
                <h4>{{ floor.floor }}层</h4>
                <div class="floor-stats">
                  <span>共 {{ floor.rooms.length }} 间</span>
                  <span>可用: {{ floor.rooms.filter(r => r.status === 'AVAILABLE').length }} 间</span>
                  <span>已入住: {{ floor.rooms.filter(r => r.status === 'OCCUPIED').length }} 间</span>
          </div>
              </div>
              
              <div class="floor-rooms">
                <div v-for="room in floor.rooms" :key="room.id" 
                    class="room-cell" 
               :class="getRoomStatusClass(room.status)"> 
            <div class="room-number">{{ room.roomNumber }}</div>
                  <div class="room-type">{{ getRoomTypeName(room.roomTypeId) }}</div>
                  <div class="room-status">{{ getRoomStatusText(room.status) }}</div>
            </div>
          </div>
            </div>
          </template>
        </div>
      </div>
    </el-dialog>

    <!-- 入住率详情弹窗 -->
    <el-dialog
      title="入住率详情"
      v-model="occupancyDetailVisible"
      width="600px"
      class="custom-dialog occupancy-dialog"
      destroy-on-close
    >
      <div class="occupancy-detail">
        <div class="occupancy-stats">
        <div class="detail-item">
          <span class="item-label">总房间数：</span>
          <span class="item-value">{{ totalRooms }}</span>
        </div>
        <div class="detail-item">
          <span class="item-label">已入住房间：</span>
          <span class="item-value">{{ occupiedRooms }}</span>
        </div>
        <div class="detail-item">
          <span class="item-label">可用房间：</span>
          <span class="item-value">{{ totalRooms - occupiedRooms }}</span>
        </div>
        <div class="detail-item">
          <span class="item-label">入住率：</span>
          <span class="item-value">{{ occupancyRate }}%</span>
        </div>
        <div class="occupancy-chart">
            <el-progress :percentage="occupancyRate" :color="occupancyRateColor" :stroke-width="20"></el-progress>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 预订详情弹窗 -->
    <el-dialog
      title="预订详情"
      v-model="detailVisible"
      width="600px"
      class="custom-dialog"
    >
      <div class="booking-detail" v-if="currentBooking">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="预订号">{{ currentBooking.bookingNo }}</el-descriptions-item>
          <el-descriptions-item label="客户姓名">{{ currentBooking.guestName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentBooking.guestPhone }}</el-descriptions-item>
          <el-descriptions-item label="房间类型">{{ currentBooking.roomTypeName }}</el-descriptions-item>
          <el-descriptions-item label="房间数量">{{ currentBooking.roomCount }}</el-descriptions-item>
          <el-descriptions-item label="入住日期">{{ formatDateToYMD(currentBooking.checkInTime) }}</el-descriptions-item>
          <el-descriptions-item label="离店日期">{{ formatDateToYMD(currentBooking.checkOutTime) }}</el-descriptions-item>
          <el-descriptions-item label="预订状态">
            <el-tag :type="getStatusType(currentBooking.status)" effect="light">
              {{ getStatusText(currentBooking.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="getPaymentStatusType(currentBooking.paymentStatus)" effect="light">
              {{ getPaymentStatusText(currentBooking.paymentStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总价">¥{{ currentBooking.totalPrice }}</el-descriptions-item>
          <el-descriptions-item label="特殊要求">{{ currentBooking.specialRequests || '无' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(currentBooking.createTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="booking-detail-actions" v-if="currentBooking">
        <el-button 
          type="success" 
          plain 
          @click="handleConfirm(currentBooking)"
          v-if="currentBooking.status === 'pending'"
        >确认预订</el-button>
        <el-button 
          type="primary" 
          plain 
          @click="handleCheckIn(currentBooking)"
          v-if="currentBooking.status === 'confirmed'"
        >办理入住</el-button>
        <el-button 
          type="danger" 
          plain 
          @click="handleCancel(currentBooking)"
          v-if="['pending', 'confirmed'].includes(currentBooking.status)"
        >取消预订</el-button>
        <el-button @click="detailVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Refresh, Plus, Edit, Check, Close, View, Key, Money, Picture, Calendar,
  Clock, User, Download, Right, House, List, Monitor, Connection, Message, Star,
  Histogram, Sugar, Brush, Tools
} from '@element-plus/icons-vue'
import * as XLSX from 'xlsx' // 导入xlsx库
// ADD API Imports
import {
  fetchBookings,
  getBookingDetails,
  createBooking,
  updateBooking,
  confirmBooking,
  cancelBooking,
  processPayment,
  checkInBooking,
  fetchTodayCheckinStats,
  fetchRooms,
  fetchDashboardStats,
  fetchPendingBookingCount,
  fetchRoomTypes // 引入新的 API 函数
} from '@/api/reception';
import { useAuthStore } from '@/store/auth'; // 引入 auth store

// 搜索表单
const searchForm = reactive({
  bookingNo: '',
  customerName: '',
  phone: '',
  status: '',
  dateRange: null
})

// 统计数据 - Initialize with default values
const pendingCount = ref(0)
const todayCheckinCount = ref(0)
const occupancyRate = ref(0)
const totalRooms = ref(0) // Initialize, will be fetched
const occupiedRooms = ref(0) // Initialize, will be fetched

// 今日待办
const todayTasks = [
  { id: 1, type: 'vip', content: 'VIP客户接待 - 李先生 11:30到店', status: 'pending' },
  { id: 2, type: 'checkout', content: '302房延迟退房申请处理', status: 'pending' },
  { id: 3, type: 'service', content: '401房送加床服务', status: 'completed' },
  { id: 4, type: 'complaint', content: '处理305房噪音投诉', status: 'processing' },
  { id: 5, type: 'cleaning', content: '安排508房紧急清洁', status: 'completed' }
]

// 房型数据 (修改为从 API 获取)
const roomTypeList = ref([]) // 用于存储从 API 获取的房型列表 {id, name, price}

// 预订列表数据 - Initialize as empty
const loading = ref(false)
const bookingList = ref([]) // Initialize as empty array

// 分页 - Initialize total as 0
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0) // Initialize total as 0

// 预订表单对话框
const dialogVisible = ref(false)
const dialogType = ref('add')
const bookingFormRef = ref(null)
const bookingForm = reactive({
  customerName: '',
  phone: '',
  idCard: '',
  roomType: '',
  dateRange: null,
  roomCount: 1,
  specialRequests: '',
  isGroup: false,
  guestCount: 2,
  estimatedArrivalTime: '',
  source: '直接预订'
})

// 入住登记对话框
const checkInVisible = ref(false)
const checkInFormRef = ref(null)
const checkInForm = reactive({
  roomNumber: '',
  deposit: 500,
  remarks: ''
})

// 可用房间列表 - Initialize as empty
const availableRooms = ref([]) // Initialize as empty array

// 房型预览对话框
const roomPreviewVisible = ref(false)
const currentRoomType = ref('')
const roomTypeImages = {
  '标准双人间': [
    { url: 'https://example.com/standard-twin-1.jpg', title: '标准双人间-主视图' },
    { url: 'https://example.com/standard-twin-2.jpg', title: '标准双人间-浴室' }
  ],
  '豪华大床房': [
    { url: 'https://example.com/deluxe-king-1.jpg', title: '豪华大床房-主视图' },
    { url: 'https://example.com/deluxe-king-2.jpg', title: '豪华大床房-浴室' }
  ],
  '行政套房': [
    { url: 'https://example.com/executive-suite-1.jpg', title: '行政套房-客厅' },
    { url: 'https://example.com/executive-suite-2.jpg', title: '行政套房-卧室' },
    { url: 'https://example.com/executive-suite-3.jpg', title: '行政套房-浴室' }
  ]
}

// 表单验证规则
const bookingFormRules = {
  customerName: [
    { required: true, message: '请输入客户姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  roomType: [
    { required: true, message: '请选择房间类型', trigger: 'change' }
  ],
  dateRange: [
    { required: true, message: '请选择入住日期', trigger: 'change' }
  ],
  roomCount: [
    { required: true, message: '请选择房间数量', trigger: 'change' }
  ],
  guestCount: [
    { required: true, message: '请输入入住人数', trigger: 'blur' }
  ],
  estimatedArrivalTime: [
    { required: true, message: '请选择预计到店时间', trigger: 'change' }
  ],
  source: [
    { required: true, message: '请选择预订来源', trigger: 'change' }
  ]
}

const checkInFormRules = {
  roomNumber: [
    { required: true, message: '请选择房间号', trigger: 'change' }
  ],
  deposit: [
    { required: true, message: '请输入押金金额', trigger: 'blur' }
  ]
}

// 状态处理函数
const getStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    confirmed: 'primary',
    'checked-in': 'success',
    cancelled: 'info',
    completed: ''
  };
  return statusMap[status?.toLowerCase()];
}

const getStatusText = (status) => {
  const statusMap = {
    pending: '待确认',
    confirmed: '已确认',
    'checked-in': '已入住',
    cancelled: '已取消',
    completed: '已完成'
  };
  return statusMap[status?.toLowerCase()];
}

// 支付状态处理函数
const getPaymentStatusType = (status) => {
  const statusMap = {
    'UNPAID': 'danger',
    'DEPOSIT_PAID': 'warning',
    'PAID_FULL': 'success'
  };
  return statusMap[status?.toUpperCase()] || 'info';
}

const getPaymentStatusText = (status) => {
  const statusMap = {
    'UNPAID': '未支付',
    'DEPOSIT_PAID': '已付定金',
    'PAID_FULL': '已付全款'
  };
  return statusMap[status?.toUpperCase()] || '未知状态';
}

// ADD fetchData function and related logic
const fetchData = async (retry = 0, customParams = null, exactMatch = false) => {
  loading.value = true;
  try {
    console.log("开始获取预订数据...");
    
    // 保存精确匹配参数，用于前端筛选
    const frontendFilter = {
      enabled: exactMatch,
      checkInDate: customParams?.exactCheckInDate,
      checkOutDate: customParams?.exactCheckOutDate
    };
    
    // 准备API请求参数
    const params = {
      page: currentPage.value - 1, // 0-based index for Spring Pageable
      size: pageSize.value,       // Use 'size' instead of 'pageSize'
      status: searchForm.status || undefined,
      guestName: searchForm.customerName || undefined,
      guestPhone: searchForm.phone || undefined,
      bookingNo: searchForm.bookingNo || undefined
    };
    
    // 如果提供了自定义参数，优先使用自定义参数
    if (customParams) {
      console.log("使用自定义参数:", customParams);
      
      // 确保自定义参数中的status覆盖searchForm中的status
      if (customParams.status) {
        params.status = customParams.status;
      }
      
      // 精确匹配日期参数处理
      if (exactMatch) {
        console.log("使用精确日期匹配模式");
        
        // 处理入住日期精确匹配
        if (customParams.exactCheckInDate) {
          params.exactDate = 'checkIn'; // 表示要精确匹配入住日期
          params.date = customParams.exactCheckInDate; // 日期值
          console.log("设置精确入住日期匹配:", customParams.exactCheckInDate);
        }
        
        // 处理离店日期精确匹配
        if (customParams.exactCheckOutDate) {
          params.exactDate = 'checkOut'; // 表示要精确匹配离店日期
          params.date = customParams.exactCheckOutDate; // 日期值
          console.log("设置精确离店日期匹配:", customParams.exactCheckOutDate);
        }
      } 
      // 常规日期范围参数
      else {
        // 添加日期参数，确保使用正确的API参数名称
        if (customParams.startDate || customParams.checkInTime) {
          params.checkInTime = customParams.checkInTime || customParams.startDate; // 支持两种参数名
          console.log("设置入住日期参数:", params.checkInTime);
        }
        
        if (customParams.endDate || customParams.checkOutTime) {
          params.checkOutTime = customParams.checkOutTime || customParams.endDate; // 支持两种参数名
          console.log("设置离店日期参数:", params.checkOutTime);
        }
      }
    }
    // 否则处理表单中的日期范围参数
    else if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      try {
        // 确保日期是有效的字符串或Date对象后再处理
        if (searchForm.dateRange[0]) {
          // 检查是否已是字符串格式 (YYYY-MM-DD)
          if (typeof searchForm.dateRange[0] === 'string' && searchForm.dateRange[0].match(/^\d{4}-\d{2}-\d{2}$/)) {
            params.checkInTime = searchForm.dateRange[0]; // 更新参数名
          } else {
            // 处理Date对象
            const startDate = new Date(searchForm.dateRange[0]);
            if (!isNaN(startDate.getTime())) {
              params.checkInTime = startDate.toISOString().split('T')[0]; // 更新参数名
            }
          }
        }
        
        if (searchForm.dateRange[1]) {
          // 检查是否已是字符串格式 (YYYY-MM-DD)
          if (typeof searchForm.dateRange[1] === 'string' && searchForm.dateRange[1].match(/^\d{4}-\d{2}-\d{2}$/)) {
            params.checkOutTime = searchForm.dateRange[1]; // 更新参数名
          } else {
            // 处理Date对象
            const endDate = new Date(searchForm.dateRange[1]);
            if (!isNaN(endDate.getTime())) {
              params.checkOutTime = endDate.toISOString().split('T')[0]; // 更新参数名
            }
          }
        }
        
        console.log("设置日期范围参数:", params.checkInTime, "到", params.checkOutTime);
      } catch (dateError) {
        console.error("日期处理错误:", dateError);
        ElMessage.warning('日期格式处理出错，将忽略日期筛选');
      }
    }
    
    console.log("发送API请求参数:", params);
    
    // 发送请求获取预订列表
    const response = await fetchBookings(params);
    
    // 添加调试日志输出查看响应结构
    console.log("预订列表API响应:", response);
    
    // 规范化状态值处理
    let bookings = response.data?.data?.content || [];
    
    // 如果没有获取到内容，尝试其他可能的路径
    if (!bookings.length && response.data?.content) {
      console.log("从 response.data.content 获取预订列表");
      bookings = response.data.content;
    }
    
    console.log("获取到的预订列表条数:", bookings.length);
    
    // 规范化预订数据
    bookings = bookings.map(booking => {
      // 确保status字段存在并规范化
      if (booking.status) {
        // 如果状态是中文，转换为对应的英文键
        const statusTextToKey = {
          '待确认': 'pending',
          '已确认': 'confirmed',
          '已入住': 'checked-in',
          '已取消': 'cancelled',
          '已完成': 'completed'
        };
        
        if (statusTextToKey[booking.status]) {
          booking.status = statusTextToKey[booking.status];
        }
      }
      
      // 确保日期字段格式正确
      if (booking.checkInTime && !(booking.checkInTime instanceof Date)) {
        booking.checkInTime = new Date(booking.checkInTime);
      }
      
      if (booking.checkOutTime && !(booking.checkOutTime instanceof Date)) {
        booking.checkOutTime = new Date(booking.checkOutTime);
      }
      
      return booking;
    });
    
    // 如果需要前端精确筛选日期
    if (frontendFilter.enabled) {
      console.log("执行前端精确日期筛选");
      
      if (frontendFilter.checkInDate) {
        console.log("筛选入住日期等于:", frontendFilter.checkInDate);
        bookings = bookings.filter(booking => {
          const bookingDate = booking.checkInTime ? formatDateToYMD(booking.checkInTime) : null;
          return bookingDate === frontendFilter.checkInDate;
        });
        console.log("筛选后剩余预订:", bookings.length);
      }
      
      if (frontendFilter.checkOutDate) {
        console.log("筛选离店日期等于:", frontendFilter.checkOutDate);
        bookings = bookings.filter(booking => {
          const bookingDate = booking.checkOutTime ? formatDateToYMD(booking.checkOutTime) : null;
          return bookingDate === frontendFilter.checkOutDate;
        });
        console.log("筛选后剩余预订:", bookings.length);
      }
    }
    
    // 更新列表数据
    bookingList.value = bookings;
    
    // 获取总记录数
    let totalCount = 0;
    if (response.data?.data?.totalElements !== undefined) {
      totalCount = response.data.data.totalElements;
      console.log("从 response.data.data.totalElements 获取到总记录数:", totalCount);
    } else if (response.data?.totalElements !== undefined) {
      totalCount = response.data.totalElements;
      console.log("从 response.data.totalElements 获取到总记录数:", totalCount);
    } else if (response.data?.data?.total !== undefined) {
      totalCount = response.data.data.total;
      console.log("从 response.data.data.total 获取到总记录数:", totalCount);
    } else if (response.data?.total !== undefined) {
      totalCount = response.data.total;
      console.log("从 response.data.total 获取到总记录数:", totalCount);
    }
    
    total.value = totalCount;
    console.log("设置总记录数:", total.value);

    // Fetch statistics in parallel
    console.log("开始并行获取统计数据...");
    const [statsRes, todayCheckinRes, pendingCountRes] = await Promise.all([
      fetchDashboardStats(),
      fetchTodayCheckinStats(),
      fetchPendingBookingCount() // Use the helper function
    ]);

    // Update dashboard stats
    console.log("仪表盘统计响应:", statsRes);
    totalRooms.value = statsRes.data?.totalRooms || 0;
    occupiedRooms.value = statsRes.data?.occupiedRooms || 0;
    
    // Calculate occupancy rate
    occupancyRate.value = totalRooms.value > 0 
      ? Math.round((occupiedRooms.value / totalRooms.value) * 100) 
      : 0;
    console.log(`计算入住率: ${occupiedRooms.value}/${totalRooms.value} = ${occupancyRate.value}%`);

    // Update today checkin stats (adjust key based on actual API response)
    console.log("今日入住统计响应:", todayCheckinRes);
    todayCheckinCount.value = todayCheckinRes.data?.data?.todayCheckIns || 0; 
    console.log("设置今日入住计数:", todayCheckinCount.value);

    // Update pending count
    console.log("待确认预订计数响应:", pendingCountRes);
    pendingCount.value = pendingCountRes; // 已在helper函数中处理
    console.log("设置待确认预订计数:", pendingCount.value);

  } catch (error) {
    console.error("获取数据失败:", error);
    if (error.response) {
      console.error("错误状态码:", error.response.status);
      console.error("错误详情:", error.response.data);
    }
    
    ElMessage.error('获取数据失败，请稍后重试');
    // Reset data on error?
    bookingList.value = [];
    total.value = 0;
    pendingCount.value = 0;
    todayCheckinCount.value = 0;
    occupancyRate.value = 0;
    totalRooms.value = 0;
    occupiedRooms.value = 0;
    
    // 添加重试机制，最多重试2次
    if (retry < 2) {
      console.log(`第${retry + 1}次重试获取数据...`);
      setTimeout(() => {
        fetchData(retry + 1);
      }, 2000);
    }
  } finally {
    loading.value = false;
  }
};

// 获取房间类型列表的函数 (重命名)
const loadRoomTypes = async () => {
  try {
    // 修改为调用新的 API
    const response = await fetchRoomTypes(); 
    // 调整路径以匹配新 API 返回结构 (假设直接返回 data 数组)
    const types = response.data?.data || []; 
    // 直接使用返回的 DTO 数据
    roomTypeList.value = types.map(type => ({
      id: type.id,
      name: type.name, 
      price: type.basePrice ?? 'N/A' // 读取 basePrice
    }));
     if (!roomTypeList.value.length) {
        console.warn("未能成功获取房型列表数据。");
     }
  } catch (error) {
    console.error("获取房型列表失败:", error);
    ElMessage.error('获取房型列表失败');
    roomTypeList.value = []; // 出错时清空
  }
};

// Initial data fetch
onMounted(() => {
  fetchData();
  loadRoomTypes(); // 在 onMounted 中调用获取房型列表 (使用新名称)
});

// UPDATE Search and Pagination handlers to call fetchData
const handleSearch = () => {
  currentPage.value = 1; // Reset page to 1 on new search
  console.log("执行搜索，搜索条件:", {
    bookingNo: searchForm.bookingNo,
    customerName: searchForm.customerName,
    phone: searchForm.phone,
    status: searchForm.status,
    dateRange: searchForm.dateRange ? searchForm.dateRange.map(d => d.toISOString().split('T')[0]) : null
  });
  fetchData();
};

const resetSearch = () => {
  // 清空搜索表单的值
  searchForm.bookingNo = '';
  searchForm.customerName = '';
  searchForm.phone = '';
  searchForm.status = '';
  searchForm.dateRange = null;
  
  console.log("重置搜索表单完成");
  
  // 重置页码并获取数据
  currentPage.value = 1;
  fetchData();
};

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val;
  fetchData();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchData();
};

// 日期范围选择器变更处理
const dateRangeChanged = (val) => {
  console.log("日期范围变更:", val);
  
  if (!val || val.length !== 2) {
    searchForm.dateRange = null;
    console.log("已清空日期范围");
  } else {
    // 日期已经通过 value-format 处理为 YYYY-MM-DD 格式
    console.log("设置日期范围:", val);
  }
};

// --- CRUD Operations Handlers ---

const handleAdd = () => {
  dialogType.value = 'add';
  // Reset form fields
  Object.assign(bookingForm, {
    customerName: '',
    phone: '',
    idCard: '',
    roomType: '',
    dateRange: null,
    roomCount: 1,
    specialRequests: '',
    isGroup: false,
    guestCount: 2,
    estimatedArrivalTime: '',
    source: '直接预订'
    // Add other fields if any
  });
  dialogVisible.value = true;
  // Clear validation state if formRef exists
  if (bookingFormRef.value) {
    bookingFormRef.value.clearValidate();
  }
};

const handleEdit = async (row) => {
  dialogType.value = 'edit';
  try {
    // Fetch full details if needed, or use list data
    // const detailsRes = await getBookingDetails(row.id); // Assuming booking list might lack some fields
    // const bookingData = detailsRes.data?.data || row;
    const bookingData = row; // Assuming list data is sufficient for now

    // Populate form - Adjust field names and data types as needed
    Object.assign(bookingForm, {
      id: bookingData.id, // Need ID for update
      customerName: bookingData.customerName,
      phone: bookingData.phone,
      idCard: bookingData.idCard, // Assuming this exists in data
      roomType: bookingData.roomType, // Assuming name is used. If ID needed, adjust
      dateRange: [bookingData.checkInDate, bookingData.checkOutDate], // Adjust format if needed
      roomCount: bookingData.roomCount, // Assuming this exists
      specialRequests: bookingData.specialRequests,
      isGroup: bookingData.isGroup,
      guestCount: bookingData.guestCount,
      estimatedArrivalTime: bookingData.estimatedArrivalTime, // Assuming this exists
      source: bookingData.source // Assuming this exists
    });
    dialogVisible.value = true;
    if (bookingFormRef.value) {
      bookingFormRef.value.clearValidate();
    }
  } catch (error) {;
    console.error("Error preparing edit form:", error);
    ElMessage.error('加载编辑数据失败');
  }
};

const authStore = useAuthStore(); // 获取 auth store 实例

const handleSubmit = async () => {
  if (!bookingFormRef.value) return;
  await bookingFormRef.value.validate(async (valid) => {
    if (valid) {
      // 1.a: 增加日期范围校验
      if (!bookingForm.dateRange || bookingForm.dateRange.length !== 2 || !bookingForm.dateRange[0] || !bookingForm.dateRange[1]) {
        ElMessage.error('请选择有效的入住和离店日期');
        return;
      }
      try {
        // 确保日期是 Date 对象 (某些情况下可能不是)
        const checkInDate = new Date(bookingForm.dateRange[0]);
        const checkOutDate = new Date(bookingForm.dateRange[1]);
        if (isNaN(checkInDate.getTime()) || isNaN(checkOutDate.getTime())) {
           ElMessage.error('无效的日期对象，请重新选择日期');
           return;
        }
        if (checkOutDate <= checkInDate) {
            ElMessage.error('离店日期必须晚于入住日期');
            return;
        }

        // 1.d.v: 实现总金额计算
        const selectedRoomTypeData = roomTypeList.value.find(rt => rt.id === bookingForm.roomType);
        if (!selectedRoomTypeData) {
            ElMessage.error('无法找到所选房型的价格信息');
            return;
        }
        // ---- START: 添加价格验证 ----
        if (selectedRoomTypeData.price === 'N/A') {
            ElMessage.error('所选房型价格无效，无法创建预订');
            return; 
        }
        // ---- END: 添加价格验证 ----
        const pricePerNight = selectedRoomTypeData.price; // 此时 price 必然是有效数字
        const nights = Math.ceil((checkOutDate - checkInDate) / (1000 * 60 * 60 * 24)); // 计算天数
        const calculatedTotalAmount = pricePerNight * nights * bookingForm.roomCount;

        // 1.d.vi: 获取动态用户 ID (需要确认 authStore 和 user 结构)
        const currentUserId = authStore.userId; // 修改这里
        if (!currentUserId) { // 修改这里的检查
            ElMessage.error('无法获取当前用户信息，请重新登录');
            return;
        }

        loading.value = true;
        // 1.d: 构建精简且正确的 apiData
        const apiData = {
          userId: currentUserId,
          roomType: bookingForm.roomType, // 已经是 ID 了
          checkIn: checkInDate.toISOString(), // 1.d.vii: 使用验证过的日期
          checkOut: checkOutDate.toISOString(), // 1.d.vii: 使用验证过的日期
          roomCount: bookingForm.roomCount,
          contactName: bookingForm.customerName, // 1.d.iii: 字段映射
          phone: bookingForm.phone,
          remarks: bookingForm.specialRequests, // 1.d.iv: 字段映射
          totalAmount: calculatedTotalAmount // 1.d.v: 使用计算出的金额
          // 1.d.ix: 只包含需要的字段
        };
        
        console.log("Prepared apiData for create:", apiData); // Debug log

        if (dialogType.value === 'add') {
          await createBooking(apiData);
          ElMessage.success('新增预订成功');
        } else {
          // 更新逻辑可能也需要调整，确保发送正确的字段和 ID
          // 假设 updateBooking 需要 ID
          apiData.id = bookingForm.id; // 需要确保编辑时 bookingForm 有 id
          await updateBooking(bookingForm.id, apiData); 
          ElMessage.success('更新预订成功');
        }
        dialogVisible.value = false;
        fetchData(); // Refresh data
      } catch (error) {
        console.error("Error saving booking:", error);
        // 尝试解析后端返回的更详细错误信息
        const backendError = error.response?.data?.message || error.message || '保存预订失败';
        ElMessage.error(backendError); 
      } finally {
        loading.value = false;
      }
    } // 缺少的大括号
  });
};

// TODO: Implement helper function if needed
// const getRoomTypeIdFromName = (name) => { ... };

// TODO: Implement total amount calculation logic
const calculateTotalAmount = (form) => {
  // Basic example: needs room price logic
  const pricePerNight = 580; // Placeholder - fetch actual price based on roomType
  const nights = form.dateRange 
    ? (new Date(form.dateRange[1]) - new Date(form.dateRange[0])) / (1000 * 60 * 60 * 24) 
    : 0;
  return pricePerNight * nights * form.roomCount;
};

const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认要将预订号 ${row.bookingNo} 的状态更新为"已确认"吗?`, 
      '确认预订', 
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    loading.value = true;
    await confirmBooking(row.id);
    ElMessage.success('预订已确认');
    fetchData();
  } catch (error) {
    if (error !== 'cancel') {
      console.error("Error confirming booking:", error);
      ElMessage.error(error.response?.data?.message || '确认预订失败');
    }
  } finally {
    loading.value = false;
  }
};

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认要取消预订号 ${row.bookingNo} 吗? 此操作不可恢复。`, 
      '取消预订', 
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '保留预订',
        type: 'error',
      }
    );
    loading.value = true;
    await cancelBooking(row.id);
    ElMessage.success('预订已取消');
    fetchData();
  } catch (error) {
    if (error !== 'cancel') {
      console.error("Error cancelling booking:", error);
      ElMessage.error(error.response?.data?.message || '取消预订失败');
    }
  } finally {
    loading.value = false;
  }
};

const currentBookingForCheckin = ref(null); // Store the booking being checked in

const handleCheckIn = async (row) => {
  if (!row || !row.id) {
    ElMessage.error('无效的预订记录');
    return;
  }

  currentBookingForCheckin.value = row; // Store the booking context
  // Reset check-in form
  Object.assign(checkInForm, {
    roomNumber: '',
    deposit: 500,
    remarks: ''
  });
  
  // 先显示对话框，即使数据加载失败也允许用户看到界面
  checkInVisible.value = true;
  
  if (checkInFormRef.value) {
    checkInFormRef.value.clearValidate();
  }
  
  // 清空房间列表并显示加载状态
  availableRooms.value = [];
  loading.value = true;
  
  try {
    console.log("获取可用房间数据...");
    console.log("预订信息:", JSON.stringify(row, null, 2));
    
    // 尝试使用API直接获取房间数据，添加大的pageSize参数
    const params = { 
      status: 'AVAILABLE',
      pageSize: 200, // 确保获取足够多的记录
      page: 0,       // 从第一页开始
    };
    
    // --- 恢复按房型筛选 --- 
    // 检查预订信息中是否有 roomTypeId
    const bookingRoomTypeId = row.roomTypeId || (typeof row.roomType === 'object' ? row.roomType?.id : null);
    if (bookingRoomTypeId) {
      params.roomTypeId = bookingRoomTypeId;
      console.log("按预订的房型筛选，roomTypeId:", bookingRoomTypeId);
    } else {
      console.log("未在预订信息中找到 roomTypeId，将获取所有可用房型");
    }
    // --- 恢复结束 --- 
    
    console.log("发送获取房间请求参数:", params);
    const res = await fetchRooms(params);
    // 打印实际的响应结构，非常重要！
    console.log("API获取房间响应(res):", res);
    console.log("API获取房间响应(res.data):", JSON.stringify(res.data, null, 2)); 

    // 处理返回的房间数据
    let roomList = [];

    // --- 修正解析逻辑 --- 
    // 直接从 fetchRooms 返回的结构中提取列表
    // fetchRooms 返回 { data: { content: [...], list: [...] } }
    if (res && res.data && res.data.content && Array.isArray(res.data.content)) {
        roomList = res.data.content; // 优先使用 content
        console.log("从 res.data.content 获取到房间数据，数量:", roomList.length);
    } else if (res && res.data && res.data.list && Array.isArray(res.data.list)) {
        roomList = res.data.list; // 备用 list
        console.log("从 res.data.list 获取到房间数据，数量:", roomList.length);
    } else {
        console.warn("未能从API响应中解析出房间列表，检查 fetchRooms 返回结构和此处的解析逻辑", res);
    }

    console.log("解析得到的房间列表(原始)数量:", roomList.length);
    if (roomList.length > 0) {
      console.log("房间数据示例 (原始前3条):", JSON.stringify(roomList.slice(0, 3), null, 2));
    }
    
    // 如果获取到有效的房间数据
    if (roomList && roomList.length > 0) {
      // --- 再次尝试移除这里的 filter 步骤 --- 
      // const filteredRooms = roomList
      //   .filter(room => {
      //     if (!room) {
      //       return false;
      //     }
      //     const hasRoomNumber = !!(room.roomNumber || room.number || room.room_number);
      //     return hasRoomNumber;
      //   });
      // console.log("过滤无效对象和无房号之后，房间数量:", filteredRooms.length);
      const dataToMap = roomList; // 直接使用 roomList 进行映射
      console.log("准备映射 (map) 的房间数据 (总数):", dataToMap.length);

      console.log("准备映射 (map) 的房间数据 (前5条):", JSON.stringify(dataToMap.slice(0, 5), null, 2));

      // 格式化房间数据，用于下拉列表显示
      const mappedRooms = dataToMap.map((room, index) => { // 使用 dataToMap
        const formattedRoom = {
          id: room.id || `room-${Math.random().toString(36).substring(2, 10)}`, // 保留ID生成以防万一
          number: room.roomNumber || room.number || room.room_number || '',
          roomNumber: room.roomNumber || room.number || room.room_number || '',
          type: (typeof room.roomType === 'object' && room.roomType !== null ? room.roomType?.name : room.roomType) || room.roomTypeName || room.type || '未知房型', // 修正房型获取
          roomTypeName: (typeof room.roomType === 'object' && room.roomType !== null ? room.roomType?.name : room.roomType) || room.roomTypeName || room.type || '未知房型', 
          floor: room.floor || ''
        };
        if (index < 5) { 
          console.log(`格式化房间 (index ${index}):`, JSON.stringify(formattedRoom, null, 2));
        }
        return formattedRoom;
      });
      
      console.log("映射 (map) 完成后的房间数据 (前5条):", JSON.stringify(mappedRooms.slice(0, 5), null, 2));
      console.log("映射 (map) 完成后的总房间数量:", mappedRooms.length);

      availableRooms.value = mappedRooms;

      console.log("最终赋值给 availableRooms.value 的数据 (前5条):", JSON.stringify(availableRooms.value.slice(0, 5), null, 2));
      console.log("最终赋值给 availableRooms.value 的总数量:", availableRooms.value.length);
      
      if (availableRooms.value.length === 0) {
        ElMessage.warning('处理后无可用房间数据显示'); 
      } else {
        // 可选提示
      }
    } else {
      console.log("未获取到任何房间数据或解析失败");
      availableRooms.value = [];
      ElMessage.warning('未能从服务器获取可用房间列表');
    }
  } catch (error) { // 确保 catch 块存在
    console.error("获取房间数据失败:", error);
    if (error.response) {
      console.error('错误响应状态:', error.response.status);
      console.error('错误响应数据:', error.response.data);
    }
    ElMessage.error('获取可用房间列表时出错');
    availableRooms.value = [];
  } finally { // 确保 finally 块存在
    loading.value = false;
  } // 确保函数括号闭合
};

// TODO: Implement or fetch room type ID mapping
const roomTypeMap = ref({ // Placeholder - Should be fetched or configured
  '标准双人间': 2,
  '豪华大床房': 3,
  '行政套房': 4,
});
const getRoomTypeIdFromName = (name) => {
    // 首先尝试从硬编码的映射中获取ID
    const hardcodedId = roomTypeMap.value[name];
    if (hardcodedId) {
        console.log("从硬编码映射中找到房型ID:", name, "->", hardcodedId);
        return hardcodedId;
    }
    
    // 如果在硬编码映射中找不到，尝试从roomTypeList中查找
    if (roomTypeList.value && roomTypeList.value.length > 0) {
        const foundType = roomTypeList.value.find(type => 
            type.name === name || 
            type.name === name.trim() ||
            type.name.includes(name) || 
            name.includes(type.name)
        );
        
        if (foundType) {
            console.log("从roomTypeList中找到房型ID:", name, "->", foundType.id);
            return foundType.id;
        }
    }
    
    console.warn("无法找到房型的ID:", name);
    return null;
};

// 查看详情 (Placeholder - can open a new dialog or route)
const handleView = async (row) => {
  try {
    loading.value = true;
    // 获取预订详情
    const response = await getBookingDetails(row.id);
    currentBooking.value = response.data?.data || row;
    detailVisible.value = true;
  } catch (error) {
    console.error("获取预订详情失败:", error);
    ElMessage.error('获取预订详情失败');
  } finally {
    loading.value = false;
  }
};

// 房型预览
const handleRoomPreview = (row) => {
  currentRoomType.value = row.roomType;
  roomPreviewVisible.value = true;
};

const showRoomStatus = async () => { 
  // 重置过滤条件
  selectedFloor.value = 'all';
  selectedRoomType.value = 'all';
  selectedStatus.value = 'all';
  
  // 清除调试状态标记以便重新记录状态统计
  window._debuggedStatus = false;
  
  // 加载所有房间状态，确保不传任何状态筛选参数
  try {
    roomStatusLoading.value = true;
    
    // 重置房间列表
    roomStatusList.value = [];
    
    console.log("开始获取所有状态的房间数据...");
    
    // 不指定status参数，获取所有状态的房间
    const params = {
      pageSize: 1000,
      page: 0
    };
    
    const res = await fetchRooms(params);
    console.log("房态数据响应:", res);
    
    // 尝试从不同的响应路径获取房间列表
    let roomsList = [];
    
    if (res.data?.data?.content && Array.isArray(res.data.data.content)) {
      roomsList = res.data.data.content;
    } else if (res.data?.content && Array.isArray(res.data.content)) {
      roomsList = res.data.content;
    } else if (res.data?.data?.list && Array.isArray(res.data.data.list)) {
      roomsList = res.data.data.list;
    } else if (Array.isArray(res.data)) {
      roomsList = res.data;
    }
    
    console.log("获取到房间总数:", roomsList.length);
    
    // 标准化数据结构
    const normalizedRooms = roomsList.map(room => {
      return {
        id: room.id,
        roomNumber: room.roomNumber || room.number || '',
        roomTypeId: room.roomTypeId || (room.roomType && room.roomType.id) || null,
        roomTypeName: room.roomTypeName || (room.roomType && room.roomType.name) || '未知房型',
        floor: room.floor || '未知',
        status: room.status || 'UNKNOWN',
        price: room.price || (room.roomType && room.roomType.basePrice) || null
      };
    });
    
    // 更新房间列表
    roomStatusList.value = normalizedRooms;
    
    // 收集所有楼层
    const floors = [...new Set(normalizedRooms.map(room => room.floor))].filter(Boolean);
    availableFloors.value = floors.sort();
    
    // 打印各种状态的房间数量
    const statuses = [...new Set(normalizedRooms.map(r => r.status))];
    console.log("房间状态统计:");
    statuses.forEach(status => {
      const count = normalizedRooms.filter(r => r.status === status).length;
      console.log(`- ${status}: ${count}间`);
    });
  } catch (error) {
    console.error("获取房间数据失败:", error);
    ElMessage.error('获取房态数据失败');
  } finally {
    roomStatusLoading.value = false;
  }
  
  // 显示对话框
  roomStatusVisible.value = true; 
  
  console.log("房态一览对话框已打开，总房间数:", roomStatusList.value.length);
  console.log("入住率:", currentOccupancyRate.value + "%");
};

// Quick Actions related functions (Placeholder if functionality needed beyond buttons)
const showPendingBookings = () => {
  searchForm.status = 'pending';
  handleSearch();
  ElMessage.success('已筛选待确认预订');
};
const showTodayCheckins = () => {
  console.log("开始筛选今日入住预订...");
  try {
    // 重置搜索表单
    resetSearch();
    
    // 获取当前日期（不依赖于服务器时间）
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    
    // 使用YYYY-MM-DD格式的日期字符串
    const todayStr = `${year}-${month}-${day}`;
    console.log("今日入住筛选日期:", todayStr);
    
    // 设置状态为已确认，清空其他搜索条件
    searchForm.status = 'confirmed'; 
    searchForm.dateRange = null;
    
    // 设置今日入住筛选 - 使用精确的日期匹配
    // 方式1：使用前端筛选 - 将搜索表单的dateRange设为当天
    searchForm.dateRange = [todayStr, todayStr]; // 开始和结束日期都是今天
    
    // 方式2：直接使用API参数
    // 确保只匹配今天的入住日期
    const tempParams = {
      status: 'confirmed',
      exactCheckInDate: todayStr // 使用精确匹配参数
    };
    
    console.log("今日入住搜索参数:", tempParams);
    
    // 修改fetchData调用，添加精确匹配标志
    fetchData(0, tempParams, true); // 第三个参数表示需要精确匹配日期
    ElMessage.success('已筛选今日入住预订');
  } catch (error) {
    console.error("筛选今日入住预订时出错:", error);
    ElMessage.error('筛选失败，请稍后重试');
  }
};
const showOccupancyDetails = () => {
  // 打开入住率详情弹窗
  occupancyDetailVisible.value = true;
};

// Simplified room status for dialog (Keep as is for now)
const selectedFloor = ref('all');

// Helper to format date display if needed
const formatDate = (dateString) => {
  if (!dateString) return '-';
  try {
    // More robust date formatting
    const date = new Date(dateString);
    if (isNaN(date.getTime())) { // Check if date is valid
      return dateString;
    }
    return date.toLocaleDateString(); // Or use a specific format like 'yyyy-MM-dd'
  } catch (e) {
    console.error("Error formatting date:", dateString, e);
    return dateString; // Return original if parsing fails
  }
};

// 新增：将日期时间格式化为 YYYY-MM-DD
const formatDateToYMD = (dateString) => {
  if (!dateString) return '-'; // 处理空或无效输入
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) {
      return dateString; // 如果解析失败，返回原始字符串
    }
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0'); // 月份从0开始，需要+1并补零
    const day = date.getDate().toString().padStart(2, '0'); // 日期补零
    return `${year}-${month}-${day}`;
  } catch (e) {
    console.error("Error formatting date to YYYY-MM-DD:", dateString, e);
    return dateString; // 出错时返回原始字符串
  }
};

// Make sure to add formatDate where needed in template, e.g., for checkInDate/checkOutDate columns

// 移除支付相关方法
// const handlePayment = (row) => { ... };
// const handlePaymentSubmit = async () => { ... };

// 房态预览相关数据 (Ensure these are defined only once)
const viewType = ref('grid')
const selectedDate = ref(new Date())
// selectedFloor is defined above
const selectedRoomType = ref('all')
const selectedStatus = ref('all')
const roomStatusVisible = ref(false) // Ensure this is defined only once

// ADD Room Status State
const roomStatusList = ref([]);
const roomStatusLoading = ref(false);

// ADD Fetch Room Status Data function
const fetchRoomStatusData = async (floor = 'all') => {
  roomStatusLoading.value = true;
  roomStatusList.value = []; // 清空当前列表
  
  try {
    console.log("获取房态数据开始...");
    const params = {
      pageSize: 1000,
      page: 0,
      // 删除默认的status过滤，获取所有状态的房间
    };
    
    // 根据筛选条件设置API参数
    if (floor !== 'all' && floor) {
      params.floor = floor;
      console.log("按楼层筛选:", floor);
    }
    
    if (selectedRoomType.value !== 'all' && selectedRoomType.value) {
      params.roomTypeId = selectedRoomType.value;
      console.log("按房型筛选:", selectedRoomType.value);
    }
    
    if (selectedStatus.value !== 'all' && selectedStatus.value) {
      params.status = selectedStatus.value;
      console.log("按状态筛选:", selectedStatus.value);
    }
    
    console.log("房态查询参数:", params);
    const res = await fetchRooms(params);
    console.log("房态查询响应:", res);
    
    // 尝试从不同的响应路径获取房间列表
    let roomsList = [];
    
    if (res.data?.data?.content && Array.isArray(res.data.data.content)) {
      roomsList = res.data.data.content;
      console.log("从 res.data.data.content 获取房间列表, 数量:", roomsList.length);
    } else if (res.data?.content && Array.isArray(res.data.content)) {
      roomsList = res.data.content;
      console.log("从 res.data.content 获取房间列表, 数量:", roomsList.length);
    } else if (res.data?.data?.list && Array.isArray(res.data.data.list)) {
      roomsList = res.data.data.list;
      console.log("从 res.data.data.list 获取房间列表, 数量:", roomsList.length);
    } else if (Array.isArray(res.data)) {
      roomsList = res.data;
      console.log("从 res.data 数组获取房间列表, 数量:", roomsList.length);
    }
    
    // 标准化数据结构
    const normalizedRooms = roomsList.map(room => {
      // 提取原始状态以便调试
      const originalStatus = room.status;
      
      const normalizedRoom = {
        id: room.id,
        roomNumber: room.roomNumber || room.number || '',
        roomTypeId: room.roomTypeId || (room.roomType && room.roomType.id) || null,
        roomTypeName: room.roomTypeName || (room.roomType && room.roomType.name) || '未知房型',
        floor: room.floor || '未知',
        status: room.status || 'UNKNOWN',
        price: room.price || (room.roomType && room.roomType.basePrice) || null
      };
      
      // 每种状态的房间各记录一个，用于调试
      if (roomsList.length > 0 && !window._debuggedStatus) {
        const statuses = [...new Set(roomsList.map(r => r.status))];
        console.log("房间状态类型统计:", statuses);
        console.log("各状态房间数量:");
        statuses.forEach(status => {
          const count = roomsList.filter(r => r.status === status).length;
          console.log(`- ${status || 'undefined/null'}: ${count}间`);
        });
        window._debuggedStatus = true; // 防止重复记录
      }
      
      return normalizedRoom;
    });
    
    console.log("标准化后的房间数据, 数量:", normalizedRooms.length);

    // 更新房间列表
    roomStatusList.value = normalizedRooms;
    
    // 如果没有设置floorList.value的有效值，收集所有楼层
    if (!availableFloors.value || !availableFloors.value.length) {
      const floors = [...new Set(normalizedRooms.map(room => room.floor))].filter(Boolean);
      availableFloors.value = floors.sort(); // 楼层排序
      console.log("收集到的楼层列表:", availableFloors.value);
    }
  } catch (error) {
    console.error("获取房态数据失败:", error);
    if (error.response) {
      console.error("错误状态码:", error.response.status);
      console.error("错误详情:", error.response.data);
    }
    ElMessage.error('获取房态数据失败');
    roomStatusList.value = [];
  } finally {
    roomStatusLoading.value = false;
  }
};

// ADD Event Handlers for filters
const handleFloorChange = (floor) => {
  console.log("楼层切换:", floor);
  fetchRoomStatusData(floor);
};

const handleRoomTypeChange = () => {
  console.log("房型切换:", selectedRoomType.value);
  fetchRoomStatusData(selectedFloor.value);
};

const handleStatusChange = () => {
  console.log("状态切换:", selectedStatus.value);
  fetchRoomStatusData(selectedFloor.value);
    };
    
// ADD Helper function to get room type name
const getRoomTypeName = (typeId) => {
  if (!typeId) return '未知房型';
  const roomType = roomTypeList.value.find(type => type.id === typeId);
  return roomType ? roomType.name : '未知房型';
};

// 可用楼层列表
const availableFloors = ref([]);

// ADD Computed properties for room status counts
const cleaningRoomCount = computed(() => {
  return roomStatusList.value.filter(room => room.status === 'CLEANING').length;
});

const maintenanceRoomCount = computed(() => {
  return roomStatusList.value.filter(room => room.status === 'MAINTENANCE').length;
});
    
// ADD Computed property for grouped rooms
const groupedRooms = computed(() => {
  // 按楼层分组
  const floorGroups = {};
  
  roomStatusList.value.forEach(room => {
    const floor = room.floor || '未知';
    if (!floorGroups[floor]) {
      floorGroups[floor] = {
        floor,
        rooms: []
      };
    }
    floorGroups[floor].rooms.push(room);
  });
  
  // 转换为数组并按楼层排序
  return Object.values(floorGroups).sort((a, b) => {
    // 如果能转成数字则按数字排序，否则按字符串
    const floorA = parseInt(a.floor);
    const floorB = parseInt(b.floor);
    
    if (!isNaN(floorA) && !isNaN(floorB)) {
      return floorA - floorB;
    }
    
    return a.floor.localeCompare(b.floor);
  });
});

// 入住率详情弹窗
const occupancyDetailVisible = ref(false);

// 入住率颜色计算
const occupancyRateColor = computed(() => {
  const rate = occupancyRate.value;
  if (rate < 30) return '#67C23A'; // 绿色 - 低入住率
  if (rate < 70) return '#E6A23C'; // 黄色 - 中等入住率
  return '#F56C6C'; // 红色 - 高入住率
});

const formatRoomOption = (room) => {
  // 检查房间对象是否存在
  if (!room) {
    return '无效房间';
  }
  
  // 确保房间号存在
  const roomNumber = room.roomNumber || room.number || '';
  if (!roomNumber) {
    return '未知房间号';
  }
  
  // 尝试获取房间类型，优先使用roomTypeName
  let roomType = '';
  if (room.roomTypeName) {
    roomType = room.roomTypeName;
  } else if (room.type) {
    roomType = room.type;
  } else if (room.roomType) {
    // roomType可能是对象或字符串
    if (typeof room.roomType === 'object' && room.roomType !== null) {
      roomType = room.roomType.name || '';
    } else {
      roomType = room.roomType;
    }
  }
  
  return roomType ? `${roomNumber} (${roomType})` : roomNumber;
};

const handleCheckInSubmit = async () => {
  if (!checkInFormRef.value) return;
  
  try {
    // 手动验证表单
    await checkInFormRef.value.validate();
    
    // 检查是否选择了房间号
    if (!checkInForm.roomNumber) {
      console.log("未选择房间号");
      ElMessage.error('请选择一个房间号');
      return;
    }
    
    loading.value = true;
    console.log('提交入住表单:', checkInForm);
    
    // 确保预订信息完整
    if (!currentBookingForCheckin.value || !currentBookingForCheckin.value.id) {
      console.log("预订信息不完整:", currentBookingForCheckin.value);
      ElMessage.error('无法获取预订信息，请刷新页面重试');
      return;
    }
    
    // 在提交前打印选择的房间信息
    console.log('选择的房间号:', checkInForm.roomNumber);
    console.log('当前可用房间列表:', availableRooms.value);
    
    // 查找选择的房间详细信息
    const selectedRoom = availableRooms.value.find(room => 
      String(room.number || room.roomNumber) === String(checkInForm.roomNumber)
    );
    console.log('选择的房间详情:', selectedRoom);
    
    if (!selectedRoom) {
      // 即使房间详情未找到（例如使用了上次缓存的可用房间），也尝试继续，让后端处理
      console.warn("本地未找到所选房间的详细信息，将继续尝试提交");
    }
    
    // 构建API数据
    const apiData = {
      bookingId: currentBookingForCheckin.value.id, // 将 reservationId 修改为 bookingId
      roomNumber: checkInForm.roomNumber,
      roomId: selectedRoom?.id, // 传递房间ID，如果找到了
      deposit: checkInForm.deposit,
      remarks: checkInForm.remarks,
      // 确保必填字段有值
      guestName: currentBookingForCheckin.value.guestName || currentBookingForCheckin.value.contactName || '客人', // 兼容字段名
      guestMobile: currentBookingForCheckin.value.guestPhone || currentBookingForCheckin.value.phone || '无',
      checkInTime: new Date().toISOString(), // 入住时间为当前时间
      // 添加预计退房时间 (从预订信息获取)
      expectedCheckOutTime: currentBookingForCheckin.value.checkOutTime 
                             ? new Date(currentBookingForCheckin.value.checkOutTime).toISOString() 
                             : null // 如果预订信息中没有，则发送null
    };
    
    // 增加详细日志，检查最终发送的数据
    console.log("准备发送的入住 API 数据:", JSON.stringify(apiData, null, 2));
    
    // 调用API
    await checkInBooking(apiData);
    
    // 成功处理
    ElMessage.success('入住登记成功');
    checkInVisible.value = false;
    fetchData(); // 刷新预订列表
    
  } catch (error) {
    // 错误处理移至 API 层，这里只处理 UI 反馈
    // catch块现在由 API 调用中抛出的错误触发
    console.error("办理入住失败 (组件层面捕获):", error);
    
    // 从 error 对象中获取后端返回的消息（如果 API 层没有处理）
    let errorMessage = '入住登记失败';
    if (error && error.response && error.response.data && error.response.data.message) {
        errorMessage = error.response.data.message;
    } else if (error && error.message) {
        errorMessage = error.message;
    }
    ElMessage.error(errorMessage);
    
  } finally {
    loading.value = false;
  }
};

// 添加直接从数据库获取可用房间的函数
const loadAvailableRoomsFromDB = async () => {
  try {
    console.log("正在直接从数据库获取可用房间...");
    
    // 因为直接请求可能会遇到403错误，改为使用现有的fetchRooms函数
    const response = await fetchRooms({ status: 'AVAILABLE' });
    console.log("获取房间成功:", response);
    
    // 解析不同格式的响应数据
    let roomsList = [];
    
    if (response.data && Array.isArray(response.data)) {
      roomsList = response.data;
    } else if (response.data?.data && Array.isArray(response.data.data)) {
      roomsList = response.data.data;
    } else if (response.data?.data?.list && Array.isArray(response.data.data.list)) {
      roomsList = response.data.data.list;
    } else if (response.data?.list && Array.isArray(response.data.list)) {
      roomsList = response.data.list;
    }
    
    if (roomsList.length === 0) {
      console.warn("直接获取的数据中未找到房间列表");
      return [];
    }
    
    console.log("获取到房间列表:", roomsList.length, "间");
    
    // 处理和规范化房间数据
    return roomsList.map(room => ({
      id: room.id,
      roomNumber: room.roomNumber || room.room_number || room.number,
      number: room.roomNumber || room.room_number || room.number,
      roomTypeId: room.roomTypeId || room.room_type_id,
      roomTypeName: room.roomTypeName || room.room_type_name || room.type,
      type: room.roomTypeName || room.room_type_name || room.type || '标准房',
      floor: room.floor,
      status: room.status
    }));
  } catch (error) {
    console.error("直接获取房间数据失败:", error);
    return [];
  }
};

// ADD Computed properties for stats in dialog
const availableRoomCount = computed(() => {
  return roomStatusList.value.filter(room => room.status === 'AVAILABLE').length;
});

const occupiedRoomCount = computed(() => {
  return roomStatusList.value.filter(room => room.status === 'OCCUPIED').length;
});

const reservedRoomCount = computed(() => {
  return roomStatusList.value.filter(room => room.status === 'RESERVED').length;
});

const currentOccupancyRate = computed(() => {
  const total = roomStatusList.value.length;
  if (total === 0) return 0;
  
  // 计算已入住的房间数量
  const occupied = roomStatusList.value.filter(room => room.status === 'OCCUPIED').length;
  
  // 入住率 = 已入住房间数 / 总房间数 * 100%
  return Math.round((occupied / total) * 100);
});

// ADD Helper functions for room status display
const getRoomStatusClass = (status) => {
  // 标准化status为大写
  const normalizedStatus = status?.toUpperCase() || 'UNKNOWN';
  
  const statusMap = {
    'AVAILABLE': 'room-available',
    'VACANT': 'room-available', // 兼容可能的"空闲"状态
    'OCCUPIED': 'room-occupied',
    'RESERVED': 'room-reserved',
    'MAINTENANCE': 'room-maintenance',
    'CLEANING': 'room-cleaning',
    'OUT_OF_SERVICE': 'room-out-of-service'
  };
  return statusMap[normalizedStatus] || 'room-unknown';
};

const getRoomStatusText = (status) => {
  // 标准化status为大写
  const normalizedStatus = status?.toUpperCase() || 'UNKNOWN';
  
  const statusMap = {
    'AVAILABLE': '可用',
    'VACANT': '可用',
    'OCCUPIED': '已入住',
    'RESERVED': '已预订',
    'MAINTENANCE': '维修中',
    'CLEANING': '清洁中',
    'OUT_OF_SERVICE': '停用'
  };
  return statusMap[normalizedStatus] || '未知状态';
};

// 预订详情
const detailVisible = ref(false);
const currentBooking = ref(null);

// 其他处理程序
const openTodayCheckin = () => { 
  // 调用封装好的今日入住筛选函数
  showTodayCheckins(); 
};

// 添加缺失的今日离店功能
const openTodayCheckout = () => {
  console.log("开始筛选今日离店预订...");
  try {
    // 重置搜索表单
    resetSearch();
    
    // 获取当前日期（不依赖于服务器时间）
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    
    // 使用YYYY-MM-DD格式的日期字符串
    const todayStr = `${year}-${month}-${day}`;
    console.log("今日离店筛选日期:", todayStr);
    
    // 设置状态为已入住，清空其他搜索条件
    searchForm.status = 'checked-in'; 
    searchForm.dateRange = null;
    
    // 设置今日离店筛选 - 使用精确的日期匹配
    // 方式1：使用前端筛选 - 将搜索表单的dateRange设为当天
    searchForm.dateRange = [null, todayStr]; // 只设置结束日期为今天
    
    // 方式2：直接使用API参数
    // 确保只匹配今天的离店日期
    const tempParams = {
      status: 'checked-in',
      exactCheckOutDate: todayStr // 使用精确匹配参数
    };
    
    console.log("今日离店搜索参数:", tempParams);
    
    // 修改fetchData调用，添加精确匹配标志
    fetchData(0, tempParams, true); // 第三个参数表示需要精确匹配日期
    ElMessage.success('已筛选今日离店预订');
  } catch (error) {
    console.error("筛选今日离店预订时出错:", error);
    ElMessage.error('筛选失败，请稍后重试');
  }
};

const filterByStatus = (status) => {
  // 更新状态选择器的值
  selectedStatus.value = status;
  
  // 触发筛选
  console.log(`正在按状态筛选: ${status}`);
  ElMessage.success(`已筛选${getRoomStatusText(status)}的房间`);
  
  // 调用fetchRoomStatusData执行实际筛选
  fetchRoomStatusData(selectedFloor.value);
};

// 已入住房间列表（用于入住率详情弹窗）
const occupiedRoomsList = computed(() => {
  return roomStatusList.value.filter(room => room.status.toUpperCase() === 'OCCUPIED');
});

// 当打开入住率详情时，确保有房间数据
const showOccupancyInfo = () => {
  // 如果房间状态列表为空，先加载数据
  if (roomStatusList.value.length === 0) {
    fetchRoomStatusData();
  }
  // 打开入住率详情弹窗
  occupancyDetailVisible.value = true;
};

</script>

<style scoped>
/* 更改整体样式 */
.bookings-container {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* 页面标题区域样式 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: #fff;
  padding: 24px 30px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(220, 225, 235, 0.5);
}

.header-content h2 {
  margin: 0;
  font-size: 26px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.gradient-text {
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-description {
  margin: 6px 0 0;
  color: #6c757d;
  font-size: 14px;
}

/* 统计卡片区域样式 */
.stats-container {
  margin-bottom: 24px;
}

.stats-card {
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: pointer;
  height: 100%;
  overflow: hidden;
  border: 1px solid rgba(220, 225, 235, 0.5);
}

.stats-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.stats-card-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stats-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
}

.stats-icon-wrapper .el-icon {
  font-size: 28px;
  color: white;
}

.pending-card {
  background: linear-gradient(to bottom, rgba(249, 115, 22, 0.1), transparent 20%);
}

.pending-card .stats-icon-wrapper {
  background: linear-gradient(135deg, #f97316, #ea580c);
}

.confirmed-card {
  background: linear-gradient(to bottom, rgba(14, 165, 233, 0.1), transparent 20%);
}

.confirmed-card .stats-icon-wrapper {
  background: linear-gradient(135deg, #0ea5e9, #0284c7);
}

.occupancy-card {
  background: linear-gradient(to bottom, rgba(16, 185, 129, 0.1), transparent 20%);
}

.occupancy-card .stats-icon-wrapper {
  background: linear-gradient(135deg, #10b981, #059669);
}

.stats-card:hover .stats-icon-wrapper {
  transform: scale(1.1);
}

.stats-info {
  flex: 1;
}

.stats-value {
  font-size: 30px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6px;
  transition: all 0.3s ease;
}

.stats-card:hover .stats-value {
  transform: scale(1.05);
}

.stats-label {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

/* 快速操作区样式 */
.quick-actions-card {
  margin-bottom: 24px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(220, 225, 235, 0.5);
}

.quick-actions-header {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 10px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  padding: 15px 0;
}

.quick-action-item {
  flex: 1;
  min-width: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 15px;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;
}

.quick-action-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(13, 110, 253, 0.2);
}

.quick-action-icon .el-icon {
  font-size: 24px;
  color: white;
}

.quick-action-item:hover {
  background-color: #f1f5f9;
  transform: translateY(-3px);
}

.quick-action-item:hover .quick-action-icon {
  transform: scale(1.1);
  box-shadow: 0 8px 20px rgba(13, 110, 253, 0.3);
}

.quick-action-item span {
  font-size: 14px;
  color: #475569;
  font-weight: 500;
  margin-top: 8px;
  transition: all 0.3s;
}

/* 搜索卡片样式 */
.search-card {
  margin-bottom: 24px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(220, 225, 235, 0.5);
}

.search-form {
  padding: 5px;
}

.search-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #475569;
}

.search-form :deep(.el-input__wrapper),
.search-form :deep(.el-textarea__inner),
.search-form :deep(.el-select__wrapper) {
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.03);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.search-form :deep(.el-input__wrapper:hover),
.search-form :deep(.el-textarea__inner:hover),
.search-form :deep(.el-select__wrapper:hover) {
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.05);
}

/* 列表卡片样式 */
.list-card {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(220, 225, 235, 0.5);
}

/* 表格样式 */
.booking-table {
  width: 100%;
  margin: 10px 0;
}

.booking-table :deep(th.el-table__cell) {
  background-color: #f8fafc;
  font-weight: 600;
  color: #1e293b;
  padding: 16px 12px;
}

.booking-table :deep(.el-table__row) {
  transition: all 0.2s ease;
}

.booking-table :deep(.el-table__row:hover) {
  background-color: #f0f5ff !important;
}

/* 统一按钮样式 */
.search-btn {
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  border: none;
  height: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(13, 110, 253, 0.2);
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(13, 110, 253, 0.3);
}

.reset-btn {
  border-color: #dcdfe6;
  height: 40px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  background-color: #f8f9fa;
  color: #343a40;
}

/* 分页容器样式 */
.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding: 10px 0;
}

/* 对话框样式 */
.custom-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.custom-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  background: #f8fafc;
  border-bottom: 1px solid #eaedf3;
}

.custom-dialog :deep(.el-dialog__title) {
  font-weight: 600;
  font-size: 18px;
  color: #1e293b;
}

.custom-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

/* 表单样式 */
.booking-form .el-form-item__label {
  font-weight: 500;
  color: #475569;
}

.booking-form :deep(.el-input__wrapper),
.booking-form :deep(.el-textarea__inner),
.booking-form :deep(.el-select__wrapper) {
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.03);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.booking-form :deep(.el-input__wrapper:hover),
.booking-form :deep(.el-textarea__inner:hover),
.booking-form :deep(.el-select__wrapper:hover) {
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.05);
}

/* 对话框上的按钮 */
.dialog-footer .submit-btn {
  min-width: 140px;
  height: 44px;
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 500;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(13, 110, 253, 0.25);
  transition: all 0.3s ease;
}

.dialog-footer .submit-btn:hover {
  background: linear-gradient(135deg, #0b5ed7, #153576);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(13, 110, 253, 0.35);
}

.dialog-footer .cancel-btn {
  min-width: 120px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 500;
  border-radius: 8px;
  border: 1px solid #dee2e6;
  background-color: #fff;
  color: #6c757d;
  transition: all 0.3s ease;
}

.dialog-footer .cancel-btn:hover {
  background-color: #f8f9fa;
  color: #343a40;
  border-color: #ced4da;
}

/* 状态标签样式 */
.status-tag {
  padding: 6px 14px;
  border-radius: 6px;
  font-weight: 500;
  font-size: 13px;
  letter-spacing: 0.3px;
}

/* 操作区域按钮样式 */
.action-buttons {
  display: flex;
  gap: 8px;
}

.action-buttons-wrap {
  flex-wrap: wrap;
  row-gap: 5px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  border-radius: 6px;
  padding: 8px 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-2px);
  opacity: 0.9;
}

/* 预订详情弹窗 */
.booking-detail {
  padding: 10px;
}

.booking-detail-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-add {
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  border: none;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(13, 110, 253, 0.25);
}

.btn-add:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(13, 110, 253, 0.35);
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .submit-btn {
  min-width: 140px;
  height: 44px;
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 500;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(13, 110, 253, 0.25);
  transition: all 0.3s ease;
}

.dialog-footer .submit-btn:hover {
  background: linear-gradient(135deg, #0b5ed7, #153576);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(13, 110, 253, 0.35);
}

.dialog-footer .cancel-btn {
  min-width: 120px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 500;
  border-radius: 8px;
  border: 1px solid #dee2e6;
  background-color: #fff;
  color: #6c757d;
  transition: all 0.3s ease;
}

.dialog-footer .cancel-btn:hover {
  background-color: #f8f9fa;
  color: #343a40;
  border-color: #ced4da;
}

.room-count-info {
  margin-top: 10px;
  font-size: 14px;
  color: #6c757d;
}

.no-rooms-warning {
  margin-top: 10px;
  font-size: 14px;
  color: #f56c6c;
}

/* 房态一览对话框样式 */
.room-status-dialog {
  max-width: 1400px;
}

/* 房态一览组件样式 */
.room-status-container {
  padding: 10px;
}

/* 状态过滤器样式 */
.status-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 15px;
  margin-bottom: 20px;
  align-items: center;
}

/* 楼层选择器样式 */
.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item span {
  white-space: nowrap;
  font-weight: 500;
}

.filter-item .el-select {
  width: auto;
  min-width: 120px;
}

/* 确保下拉选项完全显示 */
:deep(.el-select-dropdown__item) {
  padding-right: 20px;
  white-space: nowrap;
}

/* 刷新按钮样式调整 */
.refresh-btn {
  margin-left: auto;
  padding: 9px 16px;
}

/* 房间状态统计卡片样式 */
.room-stats-cards {
  margin-bottom: 20px;
}

/* 图例说明样式 */
.room-status-legend {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.clickable-legend {
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.clickable-legend:hover {
  background-color: rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.clickable-legend .legend-color {
  transition: all 0.3s ease;
}

.clickable-legend:hover .legend-color {
  transform: scale(1.2);
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 4px;
}

/* 更大更明显的图标 */
.stat-icon {
  font-size: 28px !important;
}

.stat-icon-wrapper {
  width: 64px !important;
  height: 64px !important;
}

/* 按楼层分组显示房间样式 */
.rooms-by-floor {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

/* 楼层标题样式 */
.floor-section {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.floor-header {
  display: flex;
  justify-content: space-between;
  padding: 12px 15px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.floor-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

/* 楼层统计信息样式 */
.floor-stats {
  display: flex;
  gap: 15px;
  color: #606266;
  font-size: 14px;
}

/* 房间容器 */
.floor-rooms {
  padding: 15px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 15px;
}

/* 房间单元格样式 */
.room-cell {
  border-radius: 6px;
  padding: 12px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 5px;
  border: 1px solid #ebeef5;
}

.room-cell:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
}

/* 房间号样式 */
.room-number {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

/* 房间类型样式 */
.room-type {
  font-size: 12px;
  color: #606266;
}

/* 房间状态样式 */
.room-status {
  font-size: 12px;
  margin-top: 5px;
  padding: 2px 6px;
  border-radius: 4px;
  text-align: center;
  background-color: rgba(0, 0, 0, 0.05);
}

/* 房间状态颜色类 */
.room-available {
  background-color: #f0f9eb;
  border-color: #e1f3d8;
  color: #67c23a;
}

.room-occupied {
  background-color: #fef0f0;
  border-color: #fde2e2;
  color: #f56c6c;
}

.room-reserved {
  background-color: #fdf6ec;
  border-color: #faecd8;
  color: #e6a23c;
}

.room-cleaning {
  background-color: #e1f5fe;
  border-color: #b3e5fc;
  color: #03a9f4;
}

.room-maintenance {
  background-color: #fff7e6;
  border-color: #ffe7ba;
  color: #ff9800;
}

.room-out-of-service {
  background-color: #f5f5f5;
  border-color: #e0e0e0;
  color: #9e9e9e;
}

.room-unknown {
  background-color: #f5f5f5;
  border-color: #e0e0e0;
  color: #909399;
}

.stat-card.clickable-card {
  cursor: pointer;
}

.stat-card.clickable-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.stat-card.clickable-card .stat-icon-wrapper {
  transition: all 0.3s ease;
}

.stat-card.clickable-card:hover .stat-icon-wrapper {
  transform: scale(1.1);
}

.stat-card .stat-icon-wrapper {
  transition: all 0.3s ease;
}

.stat-card .stat-icon-wrapper .stat-icon {
  transition: all 0.3s ease;
}

.stat-card.clickable-card .stat-icon-wrapper .stat-icon {
  transition: all 0.3s ease;
}

.stat-card.clickable-card:hover .stat-icon-wrapper .stat-icon {
  transform: scale(1.1);
}

.stat-card .stat-info {
  transition: all 0.3s ease;
}

.stat-card.clickable-card .stat-info {
  transition: all 0.3s ease;
}

.stat-card.clickable-card:hover .stat-info {
  transform: scale(1.05);
}

.stat-card .stat-value {
  transition: all 0.3s ease;
}

.stat-card.clickable-card .stat-value {
  transition: all 0.3s ease;
}

.stat-card.clickable-card:hover .stat-value {
  transform: scale(1.05);
}

.stat-card .stat-title {
  transition: all 0.3s ease;
}

.stat-card.clickable-card .stat-title {
  transition: all 0.3s ease;
}

.stat-card.clickable-card:hover .stat-title {
  transform: scale(1.05);
}

.stat-card .stat-icon-wrapper.available-bg {
  background: linear-gradient(135deg, #28a745, #218838);
}

.stat-card .stat-icon-wrapper.occupied-bg {
  background: linear-gradient(135deg, #dc3545, #c52b33);
}

.stat-card .stat-icon-wrapper.reserved-bg {
  background: linear-gradient(135deg, #ffc107, #e6a23c);
}

.stat-card .stat-icon-wrapper.cleaning-bg {
  background: linear-gradient(135deg, #17a2b8, #157347);
}

.stat-card .stat-icon-wrapper.maintenance-bg {
  background: linear-gradient(135deg, #ff9800, #e67e22);
}

.stat-card .stat-icon-wrapper.occupancy-bg {
  background: linear-gradient(135deg, #6f42c1, #5a32a3);
}

/* 房间状态卡片 */
.stat-card {
  padding: 0 !important;
  overflow: hidden;
  border-radius: 10px;
  border: none !important;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08) !important;
}

.stat-card-content {
  position: relative;
  padding-top: 10px;
}

.stat-icon-wrapper {
  position: relative;
  z-index: 2;
  border-radius: 50%;
  margin: 0 auto 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  position: relative;
  z-index: 2;
  text-align: center;
  padding: 0 10px 15px;
}

.stat-value {
  font-size: 32px !important;
  font-weight: 700 !important;
  margin-bottom: 5px !important;
  color: #333 !important;
}

.stat-title {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

/* 顶部彩色条 */
.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 6px;
}

.available-card::before {
  background: linear-gradient(90deg, #28a745, #218838);
}

.occupied-card::before {
  background: linear-gradient(90deg, #dc3545, #c52b33);
}

.reserved-card::before {
  background: linear-gradient(90deg, #ffc107, #e6a23c);
}

.cleaning-card::before {
  background: linear-gradient(90deg, #17a2b8, #157347);
}

.maintenance-card::before {
  background: linear-gradient(90deg, #ff9800, #e67e22);
}

.occupancy-card::before {
  background: linear-gradient(90deg, #6f42c1, #5a32a3);
}

/* 更新状态卡片样式 */
.stat-card {
  border-radius: 12px !important;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08) !important;
  border: none !important;
  height: 100%;
}

.stat-card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 10px;
}

.stat-icon-wrapper {
  width: 70px !important;
  height: 70px !important;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 32px !important;
  color: white;
}

.stat-info {
  text-align: center;
  width: 100%;
}

.stat-value {
  font-size: 36px !important;
  font-weight: 700;
  color: #333;
  margin-bottom: 5px;
  line-height: 1.2;
}

.stat-title {
  font-size: 16px;
  color: #666;
  font-weight: 500;
}

/* 卡片顶部彩色条 */
.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 8px;
  z-index: 1;
}

/* 卡片悬停效果 */
.stat-card.clickable-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15) !important;
}

.stat-card.clickable-card:hover .stat-icon-wrapper {
  transform: scale(1.1);
}

.stat-card.clickable-card:hover .stat-value {
  transform: scale(1.05);
}

/* 入住率详情弹窗样式 */
.occupancy-dialog .el-dialog__body {
  padding: 20px;
}

.occupancy-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.occupancy-stats {
  background-color: #f8f9fa;
  border-radius: 10px;
  padding: 20px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 16px;
}

.item-label {
  font-weight: 500;
  color: #555;
}

.item-value {
  font-weight: 600;
  color: #333;
}

.occupancy-chart {
  margin-top: 15px;
}

.occupancy-chart .el-progress-bar__inner {
  transition: width 0.8s ease;
}

.occupied-rooms-list h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 18px;
  color: #333;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 10px;
}

/* 添加新样式移除顶部彩色条 */
.no-top-bar::before {
  display: none !important;
}

.no-top-bar .el-dialog__header {
  border-top: none !important;
  background: #fff !important;
}

/* 移除其他地方可能导致顶部彩色条的样式 */
.room-status-dialog::before,
.room-status-container::before,
.status-header::before {
  display: none !important;
}

/* 移除顶部横条的样式 */
/* 卡片顶部彩色条 - 移除 */
.stat-card::before {
  display: none !important;
}

/* 修改stat-card-content样式，确保没有顶部条 */
.stat-card-content {
  border-top: none !important;
}

/* 移除各种彩色条样式 */
.available-card::before,
.occupied-card::before,
.reserved-card::before,
.cleaning-card::before,
.maintenance-card::before,
.occupancy-card::before {
  display: none !important;
}

/* 对话框顶部条移除 */
.el-dialog::before,
.el-dialog__header::before {
  display: none !important;
}

/* 更新卡片样式，使用背景色代替顶部条 */
.available-card {
  background: linear-gradient(to bottom, rgba(40, 167, 69, 0.1), transparent 20%);
}

.occupied-card {
  background: linear-gradient(to bottom, rgba(220, 53, 69, 0.1), transparent 20%);
}

.reserved-card {
  background: linear-gradient(to bottom, rgba(255, 193, 7, 0.1), transparent 20%);
}

.cleaning-card {
  background: linear-gradient(to bottom, rgba(23, 162, 184, 0.1), transparent 20%);
}

.maintenance-card {
  background: linear-gradient(to bottom, rgba(255, 152, 0, 0.1), transparent 20%);
}

.occupancy-card {
  background: linear-gradient(to bottom, rgba(111, 66, 193, 0.1), transparent 20%);
}

/* 调整状态标题 */
.status-title {
  font-size: 20px;
  margin: 0 0 10px 0;
  color: #333;
  font-weight: 600;
}

/* 调整筛选框容器 */
.status-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin: 15px 0 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  align-items: center;
}

/* 调整筛选项 */
.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  white-space: nowrap;
  font-weight: 500;
  color: #333;
  min-width: 50px;
  text-align: right;
}

.filter-item .el-select {
  width: auto;
}

/* 确保下拉选项完全显示 */
:deep(.el-select-dropdown__item) {
  padding: 0 30px 0 20px;
  white-space: nowrap;
}

:deep(.el-select .el-input__wrapper) {
  padding-left: 15px;
  padding-right: 15px;
}

:deep(.el-input__inner) {
  font-size: 14px;
}

/* 刷新按钮样式调整 */
.refresh-btn {
  margin-left: auto;
  padding: 9px 20px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 5px;
}

.refresh-btn .el-icon {
  font-size: 16px;
}
</style>