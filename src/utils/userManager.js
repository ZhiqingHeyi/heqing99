import { reactive } from 'vue'
import mitt from 'mitt'
import { userApi, membershipApi } from '../api'

// 使用mitt创建事件总线
export const UserEventBus = mitt()

// 创建响应式用户状态
export const userState = reactive({
  isInitialized: false,
  isAuthenticated: false,
  isLoading: false,
  error: null,
  userData: {
    id: null,
    userName: '',
    realName: '',
    phone: '',
    email: '',
    birthday: '',
    gender: '',
    level: '普通会员',
    points: 0,
    totalSpent: 0,
    joinDate: '',
    lastLogin: ''
  },
  tokenData: {
    accessToken: null,
    refreshToken: null,
    expiresAt: null
  }
})

// 用户管理器类 - 单例模式
class UserManagerClass {
  constructor() {
    // 防止重复实例化
    if (UserManagerClass.instance) {
      return UserManagerClass.instance
    }
    
    UserManagerClass.instance = this

    // 初始化定时刷新
    this.refreshInterval = null;
    this.startAutoRefresh();
  }

  // 开始自动刷新
  startAutoRefresh() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
    // 每5分钟刷新一次用户数据
    this.refreshInterval = setInterval(() => {
      if (userState.isAuthenticated) {
        this.fetchUserData().catch(console.error);
      }
    }, 5 * 60 * 1000);
  }

  // 停止自动刷新
  stopAutoRefresh() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
      this.refreshInterval = null;
    }
  }

  // 检查数据一致性
  async checkDataConsistency() {
    if (!userState.isAuthenticated || !userState.userData.id) {
      return;
    }

    try {
      const memberInfo = await membershipApi.getMemberInfo(userState.userData.id);
      
      if (memberInfo && memberInfo.data) {
        const serverData = memberInfo.data;
        const localData = {
          level: localStorage.getItem('userLevel'),
          points: parseInt(localStorage.getItem('userPoints') || '0'),
          totalSpent: parseFloat(localStorage.getItem('userTotalSpent') || '0')
        };

        // 检查数据是否一致
        if (serverData.memberLevel !== localData.level ||
            serverData.points !== localData.points ||
            Math.abs(serverData.totalSpent - localData.totalSpent) > 0.01) {
          
          // 数据不一致，更新本地存储
          localStorage.setItem('userLevel', serverData.memberLevel);
          localStorage.setItem('userPoints', String(serverData.points));
          localStorage.setItem('userTotalSpent', String(serverData.totalSpent));

          // 更新状态
          Object.assign(userState.userData, {
            level: serverData.memberLevel,
            points: serverData.points,
            totalSpent: serverData.totalSpent
          });

          // 触发数据更新事件
          UserEventBus.emit('userDataUpdated', userState.userData);
        }
      }
    } catch (error) {
      console.error('检查数据一致性失败:', error);
      UserEventBus.emit('userDataError', error.message);
    }
  }

  // 初始化用户状态
  async initUserState() {
    if (userState.isInitialized) return userState
    
    userState.isLoading = true
    
    try {
      // 从本地存储获取token
      const storedToken = localStorage.getItem('userToken')
      
      if (!storedToken) {
        userState.isAuthenticated = false
        userState.isInitialized = true
        return userState
      }
      
      // 解析token数据
      userState.tokenData.accessToken = storedToken
      userState.tokenData.refreshToken = localStorage.getItem('userRefreshToken')
      userState.tokenData.expiresAt = localStorage.getItem('tokenExpiresAt')
      
      // 检查token是否过期
      const isTokenExpired = this.isTokenExpired()
      
      if (isTokenExpired) {
        // 尝试刷新token
        const refreshSuccess = await this.refreshToken()
        
        if (!refreshSuccess) {
          this.clearUserState()
          return userState
        }
      }
      
      // 获取用户ID
      const userId = localStorage.getItem('userId')
      
      if (userId) {
        userState.userData.id = userId
        userState.isAuthenticated = true
        
        // 获取用户详细信息
        await this.fetchUserData()
      }
      
    } catch (error) {
      console.error('初始化用户状态时出错:', error)
      userState.error = error.message || '初始化用户状态失败'
    } finally {
      userState.isLoading = false
      userState.isInitialized = true
    }
    
    return userState
  }
  
  // 获取用户数据
  async getUserData() {
    if (!userState.isAuthenticated || !userState.userData.id) {
      throw new Error('用户未登录');
    }

    try {
      await this.fetchUserData();
      return userState.userData;
    } catch (error) {
      console.error('获取用户数据失败:', error);
      throw error;
    }
  }

  // 获取用户详细信息
  async fetchUserData() {
    if (!userState.isAuthenticated || !userState.userData.id) {
      throw new Error('用户未登录或ID无效')
    }
    
    userState.isLoading = true
    userState.error = null
    
    try {
      // 调用API获取用户基本信息，使用更新后的API
      const userResponse = await userApi.getUserInfo(userState.userData.id)
      
      if (!userResponse || !userResponse.success) {
        throw new Error('获取用户信息失败')
      }
      
      // 更新用户基本信息
      const userData = userResponse.data;
      
      // 更新用户状态
      Object.assign(userState.userData, {
        userName: userData.username,
        realName: userData.name,  // 使用name字段
        phone: userData.phone,
        email: userData.email,
        birthday: userData.birthday,
        gender: userData.gender
      });
      
      // 更新本地存储
      localStorage.setItem('userName', userData.username);
      localStorage.setItem('userRealName', userData.name);
      localStorage.setItem('userPhone', userData.phone || '');
      localStorage.setItem('userEmail', userData.email || '');
      localStorage.setItem('userBirthday', userData.birthday || '');
      localStorage.setItem('userGender', userData.gender || 'unknown');
      
      // 获取会员信息
      const membershipResponse = await membershipApi.getMemberInfo(userState.userData.id)
      
      if (membershipResponse && membershipResponse.success) {
        // 更新会员相关信息
        const memberData = membershipResponse.data;
        
        // 更新状态
        Object.assign(userState.userData, {
          level: memberData.level,
          points: memberData.points,
          totalSpent: memberData.totalSpent,
          joinDate: memberData.joinDate
        });

        // 更新本地存储
        localStorage.setItem('userLevel', memberData.level);
        localStorage.setItem('userPoints', String(memberData.points));
        localStorage.setItem('userTotalSpent', String(memberData.totalSpent));
      }
      
      // 触发用户数据更新事件
      UserEventBus.emit('userDataUpdated', userState.userData)
      
      return userState.userData
    } catch (error) {
      console.error('获取用户数据时出错:', error)
      userState.error = error.message || '获取用户数据失败'
      
      // 触发用户数据错误事件
      UserEventBus.emit('userDataError', userState.error)
      throw error
    } finally {
      userState.isLoading = false
    }
  }
  
  // 更新用户信息
  async updateUserInfo(userData) {
    if (!userState.isAuthenticated || !userState.userData.id) {
      throw new Error('用户未登录');
    }
    
    userState.isLoading = true;
    userState.error = null;
    
    try {
      // 构建API所需的更新数据格式
      const updateData = {
        username: userData.userName,
        name: userData.realName,
        phone: userData.phone,
        email: userData.email,
        gender: userData.gender,
        birthday: userData.birthday
      };
      
      // 调用更新后的API
      const response = await userApi.updateUserInfo(userState.userData.id, updateData);
      
      if (!response || !response.success) {
        throw new Error(response?.message || '更新用户信息失败');
      }
      
      // 更新用户状态
      Object.assign(userState.userData, {
        userName: updateData.username,
        realName: updateData.name,
        phone: updateData.phone,
        email: updateData.email,
        gender: updateData.gender,
        birthday: updateData.birthday
      });
      
      // 更新本地存储
      localStorage.setItem('userName', updateData.username);
      localStorage.setItem('userRealName', updateData.name);
      localStorage.setItem('userPhone', updateData.phone || '');
      localStorage.setItem('userEmail', updateData.email || '');
      localStorage.setItem('userGender', updateData.gender || 'unknown');
      localStorage.setItem('userBirthday', updateData.birthday || '');
      
      // 触发用户数据更新事件
      UserEventBus.emit('userDataUpdated', userState.userData);
      
      return userState.userData;
    } catch (error) {
      console.error('更新用户信息失败:', error);
      userState.error = error.message || '更新用户信息失败';
      UserEventBus.emit('userDataError', userState.error);
      throw error;
    } finally {
      userState.isLoading = false;
    }
  }
  
  // 修改密码
  async changePassword(oldPassword, newPassword) {
    if (!userState.isAuthenticated || !userState.userData.id) {
      throw new Error('用户未登录');
    }
    
    userState.isLoading = true;
    userState.error = null;
    
    try {
      // 调用更新后的API
      const response = await userApi.changePassword(userState.userData.id, {
        oldPassword,
        newPassword
      });
      
      if (!response || !response.success) {
        throw new Error(response?.message || '修改密码失败');
      }
      
      // 密码修改成功，触发事件
      UserEventBus.emit('passwordChanged');
      
      return true;
    } catch (error) {
      console.error('修改密码失败:', error);
      userState.error = error.message || '修改密码失败';
      UserEventBus.emit('userDataError', userState.error);
      throw error;
    } finally {
      userState.isLoading = false;
    }
  }
  
  // 登录
  async login(username, password, rememberMe = false) {
    userState.isLoading = true
    userState.error = null
    
    try {
      // 调用登录API
      const response = await userApi.login({ username, password })
      
      if (!response || !response.success) {
        throw new Error(response?.message || '登录失败')
      }
      
      // 获取用户数据
      const userData = response.data
      
      // 保存token和用户基本信息
      userState.tokenData.accessToken = userData.token
      userState.tokenData.refreshToken = userData.refreshToken || null
      
      // 计算token过期时间
      if (userData.expiresIn) {
        const expiresAt = new Date()
        expiresAt.setSeconds(expiresAt.getSeconds() + userData.expiresIn)
        userState.tokenData.expiresAt = expiresAt.toISOString()
        localStorage.setItem('tokenExpiresAt', userState.tokenData.expiresAt)
      }
      
      // 保存到本地存储
      localStorage.setItem('userToken', userData.token)
      if (userData.refreshToken) {
        localStorage.setItem('userRefreshToken', userData.refreshToken)
      }
      
      // 保存用户ID和基本信息
      localStorage.setItem('userId', userData.userId)
      localStorage.setItem('userName', userData.username)
      localStorage.setItem('userRole', userData.role || 'USER')
      
      // 更新状态
      userState.userData.id = userData.userId
      userState.userData.userName = userData.username
      userState.isAuthenticated = true
      
      // 获取用户详细信息
      await this.fetchUserData()
      
      // 触发登录成功事件
      UserEventBus.emit('loginSuccess', userState.userData)
      
      return userState
    } catch (error) {
      console.error('登录失败:', error)
      userState.error = error.message || '登录失败'
      
      // 触发登录失败事件
      UserEventBus.emit('loginError', userState.error)
      throw error
    } finally {
      userState.isLoading = false
    }
  }
  
  // 注销
  async logout() {
    userState.isLoading = true
    
    try {
      // 如果有登出API，调用它
      if (userState.isAuthenticated) {
        await userApi.logout(userState.tokenData.accessToken)
      }
      
      // 清除状态
      this.clearUserState()
      
      return true
    } catch (error) {
      console.error('退出登录时出错:', error)
      // 即使API调用失败，也清除本地状态
      this.clearUserState()
      return true
    } finally {
      userState.isLoading = false
    }
  }
  
  // 清除用户状态
  clearUserState() {
    // 停止自动刷新
    this.stopAutoRefresh();

    // 清除本地存储
    localStorage.removeItem('userToken')
    localStorage.removeItem('userRefreshToken')
    localStorage.removeItem('tokenExpiresAt')
    localStorage.removeItem('userId')
    localStorage.removeItem('userLevel')
    localStorage.removeItem('userPoints')
    localStorage.removeItem('userTotalSpent')
    
    // 重置状态
    userState.isAuthenticated = false
    userState.error = null
    userState.tokenData.accessToken = null
    userState.tokenData.refreshToken = null
    userState.tokenData.expiresAt = null
    
    // 重置用户数据
    Object.assign(userState.userData, {
      id: null,
      userName: '',
      realName: '',
      phone: '',
      email: '',
      birthday: '',
      gender: '',
      level: '普通会员',
      points: 0,
      totalSpent: 0,
      joinDate: '',
      lastLogin: ''
    })
    
    // 触发用户退出事件
    UserEventBus.emit('userLoggedOut')
  }
  
  // 检查token是否过期
  isTokenExpired() {
    if (!userState.tokenData.expiresAt) return true
    
    const expiresAt = new Date(userState.tokenData.expiresAt).getTime()
    const now = new Date().getTime()
    
    // 提前5分钟刷新，避免刚好过期的情况
    return now >= expiresAt - 5 * 60 * 1000
  }
  
  // 刷新token
  async refreshToken() {
    if (!userState.tokenData.refreshToken) return false
    
    try {
      // 调用刷新token API
      const response = await userApi.refreshToken({
        refreshToken: userState.tokenData.refreshToken
      })
      
      if (!response || !response.success) {
        return false
      }
      
      const { token, refreshToken, expiresAt } = response.data
      
      // 更新本地存储
      localStorage.setItem('userToken', token)
      localStorage.setItem('userRefreshToken', refreshToken)
      localStorage.setItem('tokenExpiresAt', expiresAt)
      
      // 更新状态
      userState.tokenData.accessToken = token
      userState.tokenData.refreshToken = refreshToken
      userState.tokenData.expiresAt = expiresAt
      
      return true
    } catch (error) {
      console.error('刷新token时出错:', error)
      return false
    }
  }

  // 处理会员等级变更
  async handleMemberLevelChange(oldLevel, newLevel, reason) {
    if (!userState.isAuthenticated || !userState.userData.id) {
      throw new Error('用户未登录或ID无效');
    }

    try {
      // 调用后端API记录等级变更
      const response = await membershipApi.createLevelChangeRecord({
        userId: userState.userData.id,
        oldLevel,
        newLevel,
        reason
      });

      if (!response || !response.success) {
        throw new Error(response?.message || '记录会员等级变更失败');
      }

      // 更新本地状态
      userState.userData.level = newLevel;
      localStorage.setItem('userLevel', newLevel);

      // 触发等级变更事件
      UserEventBus.emit('memberLevelChanged', {
        oldLevel,
        newLevel,
        reason
      });

      return true;
    } catch (error) {
      console.error('处理会员等级变更时出错:', error);
      userState.error = error.message;
      UserEventBus.$emit('userDataError', error.message);
      throw error;
    }
  }

  // 获取会员等级变更历史
  async getMemberLevelChangeHistory(page = 1, size = 10) {
    if (!userState.isAuthenticated || !userState.userData.id) {
      throw new Error('用户未登录或ID无效');
    }

    try {
      const response = await membershipApi.getLevelChangeHistory(
        userState.userData.id,
        page,
        size
      );

      if (!response || !response.success) {
        throw new Error(response?.message || '获取会员等级变更历史失败');
      }

      return response.data;
    } catch (error) {
      console.error('获取会员等级变更历史时出错:', error);
      userState.error = error.message;
      UserEventBus.$emit('userDataError', error.message);
      throw error;
    }
  }

  // 优化错误处理
  handleError(error, operation) {
    console.error(`${operation}时出错:`, error);
    
    // 设置错误状态
    userState.error = error.message || `${operation}失败`;
    
    // 检查是否是认证相关错误
    if (error.status === 401 || error.status === 403) {
      this.clearUserState();
      UserEventBus.$emit('authenticationError', error.message);
      return;
    }
    
    // 触发错误事件
    UserEventBus.$emit('userDataError', {
      operation,
      message: error.message,
      timestamp: new Date()
    });
    
    // 如果是网络错误，标记需要重新同步
    if (error.name === 'NetworkError') {
      userState.needsSync = true;
    }
  }

  // 检查并处理会员升级
  async checkAndHandleMemberUpgrade() {
    if (!userState.isAuthenticated || !userState.userData.id) {
      return;
    }

    try {
      const currentLevel = userState.userData.level;
      const totalSpent = userState.userData.totalSpent;
      
      // 根据消费金额计算应该的等级
      let newLevel = currentLevel;
      if (totalSpent >= 30000) {
        newLevel = '钻石会员';
      } else if (totalSpent >= 10000) {
        newLevel = '金牌会员';
      } else if (totalSpent >= 5000) {
        newLevel = '银牌会员';
      } else if (totalSpent >= 1500) {
        newLevel = '铜牌会员';
      }

      // 如果等级需要变更
      if (newLevel !== currentLevel) {
        await this.handleMemberLevelChange(
          currentLevel,
          newLevel,
          `累计消费达到${totalSpent}元自动升级`
        );
      }
    } catch (error) {
      this.handleError(error, '检查会员升级');
    }
  }
}

// 创建单例实例
export const UserManager = new UserManagerClass()

// 默认导出
export default UserManager