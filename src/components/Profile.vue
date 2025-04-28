<template>
  <div class="profile-container">
    <!-- 个人信息卡片 -->
    <el-card class="info-card">
      <div slot="header">
        <span>个人基本信息</span>
        <el-button 
          type="text" 
          style="float: right; padding: 3px 0" 
          @click="toggleEdit"
        >
          {{ isEditing ? '取消' : '编辑' }}
        </el-button>
      </div>

      <el-form :model="userData" :disabled="!isEditing" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="userData.userName" :disabled="true"></el-input>
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="userData.realName"></el-input>
        </el-form-item>
        <el-form-item label="手机号码">
          <el-input v-model="userData.phone"></el-input>
        </el-form-item>
        <el-form-item label="电子邮箱">
          <el-input v-model="userData.email"></el-input>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker
            v-model="userData.birthday"
            type="date"
            placeholder="选择日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="userData.gender">
            <el-option label="男" value="male"></el-option>
            <el-option label="女" value="female"></el-option>
            <el-option label="保密" value="secret"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="isEditing">
          <el-button type="primary" @click="saveUserInfo">保存</el-button>
          <el-button @click="toggleEdit">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <!-- 会员等级信息卡片 -->
    <el-card class="member-card">
      <div slot="header">
        <span>会员等级信息</span>
      </div>
      <div class="member-info">
        <div class="level-info">
          <h3>当前等级：{{ userData.level }}</h3>
          <p>累计消费：{{ userData.totalSpent }}元</p>
          <p>享受折扣：{{ getMemberDiscount }}%</p>
        </div>
        <div class="level-progress">
          <el-progress 
            :percentage="getLevelProgress" 
            :format="formatProgress"
            :color="levelProgressColors">
          </el-progress>
          <p>距离下一等级还需消费：{{ getNextLevelSpending }}元</p>
        </div>
      </div>
    </el-card>

    <!-- 等级变更历史 -->
    <el-card class="history-card" v-loading="loading.history">
      <div slot="header">
        <span>等级变更历史</span>
      </div>
      <el-timeline>
        <el-timeline-item
          v-for="record in levelChangeHistory"
          :key="record.id"
          :timestamp="formatDate(record.createTime)"
          :type="getTimelineItemType(record)">
          <h4>{{ record.oldLevel }} → {{ record.newLevel }}</h4>
          <p>变更原因：{{ record.reason }}</p>
        </el-timeline-item>
      </el-timeline>
      <div class="pagination" v-if="totalHistoryPages > 1">
        <el-pagination
          @current-change="handleHistoryPageChange"
          :current-page="currentHistoryPage"
          :page-size="10"
          :total="totalHistoryRecords"
          layout="prev, pager, next">
        </el-pagination>
      </div>
    </el-card>

    <!-- 错误提示 -->
    <el-dialog
      title="错误提示"
      :visible.sync="errorDialogVisible"
      width="30%">
      <span>{{ errorMessage }}</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="errorDialogVisible = false">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { userManager } from '@/utils/userManager';
import { formatDate } from '@/utils/dateUtils';

export default {
  name: 'Profile',
  data() {
    return {
      isEditing: false,
      userData: {
        userName: '',
        realName: '',
        phone: '',
        email: '',
        birthday: '',
        gender: '',
        level: '',
        points: 0,
        totalSpent: 0
      },
      loading: {
        user: false,
        history: false
      },
      levelChangeHistory: [],
      currentHistoryPage: 1,
      totalHistoryPages: 0,
      totalHistoryRecords: 0,
      errorDialogVisible: false,
      errorMessage: '',
      levelProgressColors: [
        {color: '#909399', percentage: 20},
        {color: '#E6A23C', percentage: 40},
        {color: '#67C23A', percentage: 60},
        {color: '#409EFF', percentage: 80},
        {color: '#F56C6C', percentage: 100}
      ]
    };
  },

  computed: {
    getMemberDiscount() {
      const discounts = {
        '普通会员': 100,
        '铜牌会员': 95,
        '银牌会员': 90,
        '金牌会员': 85,
        '钻石会员': 80
      };
      return discounts[this.userData.level] || 100;
    },

    getLevelProgress() {
      const spendingThresholds = {
        '普通会员': 0,
        '铜牌会员': 1500,
        '银牌会员': 5000,
        '金牌会员': 10000,
        '钻石会员': 30000
      };
      
      const currentSpent = this.userData.totalSpent || 0;
      const currentLevel = this.userData.level;
      const nextLevel = this.getNextLevel(currentLevel);
      
      if (!nextLevel) return 100;
      
      const currentThreshold = spendingThresholds[currentLevel] || 0;
      const nextThreshold = spendingThresholds[nextLevel];
      const progress = ((currentSpent - currentThreshold) / (nextThreshold - currentThreshold)) * 100;
      
      return Math.min(Math.max(progress, 0), 100);
    },

    getNextLevelSpending() {
      const spendingThresholds = {
        '普通会员': 1500,
        '铜牌会员': 5000,
        '银牌会员': 10000,
        '金牌会员': 30000
      };
      
      const currentLevel = this.userData.level;
      const nextLevel = this.getNextLevel(currentLevel);
      
      if (!nextLevel) return 0;
      
      const nextThreshold = spendingThresholds[nextLevel];
      const currentSpent = this.userData.totalSpent || 0;
      
      return Math.max(nextThreshold - currentSpent, 0);
    }
  },

  methods: {
    formatDate,
    
    getNextLevel(currentLevel) {
      const levels = ['普通会员', '铜牌会员', '银牌会员', '金牌会员', '钻石会员'];
      const currentIndex = levels.indexOf(currentLevel);
      return currentIndex < levels.length - 1 ? levels[currentIndex + 1] : null;
    },

    formatProgress(percentage) {
      return percentage === 100 ? '已达最高等级' : `${percentage.toFixed(0)}%`;
    },

    getTimelineItemType(record) {
      const levels = ['普通会员', '铜牌会员', '银牌会员', '金牌会员', '钻石会员'];
      const oldIndex = levels.indexOf(record.oldLevel);
      const newIndex = levels.indexOf(record.newLevel);
      return newIndex > oldIndex ? 'success' : 'warning';
    },

    async loadUserData() {
      this.loading.user = true;
      try {
        const userData = await userManager.getUserData();
        this.userData = { ...userData };
        await this.loadLevelChangeHistory();
      } catch (error) {
        this.showError(error.message);
      } finally {
        this.loading.user = false;
      }
    },

    async loadLevelChangeHistory() {
      this.loading.history = true;
      try {
        const result = await userManager.getMemberLevelChangeHistory(
          this.currentHistoryPage
        );
        this.levelChangeHistory = result.content;
        this.totalHistoryPages = result.totalPages;
        this.totalHistoryRecords = result.totalElements;
      } catch (error) {
        this.showError(error.message);
      } finally {
        this.loading.history = false;
      }
    },

    async handleHistoryPageChange(page) {
      this.currentHistoryPage = page;
      await this.loadLevelChangeHistory();
    },

    showError(message) {
      this.errorMessage = message;
      this.errorDialogVisible = true;
    },

    toggleEdit() {
      if (this.isEditing) {
        // 取消编辑，恢复原始数据
        this.loadUserData();
      }
      this.isEditing = !this.isEditing;
    },

    async saveUserInfo() {
      try {
        await userManager.updateUserInfo(this.userData);
        this.isEditing = false;
        this.$message.success('个人信息更新成功');
        // 重新加载用户数据以确保显示最新信息
        await this.loadUserData();
      } catch (error) {
        this.showError(error.message);
      }
    }
  },

  created() {
    this.loadUserData();
    
    // 监听会员等级变更事件
    this.$root.$on('memberLevelChanged', async () => {
      await this.loadUserData();
    });
  },

  beforeDestroy() {
    this.$root.$off('memberLevelChanged');
  }
};
</script>

<style lang="scss" scoped>
.profile-container {
  padding: 20px;
  
  .info-card,
  .member-card,
  .history-card {
    margin-bottom: 20px;
  }

  .member-info {
    .level-info {
      margin-bottom: 20px;
      
      h3 {
        color: #409EFF;
        margin-bottom: 10px;
      }
    }

    .level-progress {
      margin-top: 20px;
    }
  }

  .pagination {
    margin-top: 20px;
    text-align: center;
  }
}
</style>