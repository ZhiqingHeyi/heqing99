import { reactive } from 'vue'
import mitt from 'mitt'
import { userApi, membershipApi } from '@/api/index'

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
  
  // 获取用户详细信息
  async fetchUserData() {
    if (!userState.isAuthenticated || !userState.userData.id) {
      throw new Error('用户未登录或ID无效')
    }
    
    userState.isLoading = true
    userState.error = null
    
    try {
      // 调用API获取用户基本信息
      const userResponse = await userApi.getUserInfo(userState.userData.id)
      
      if (!userResponse || !userResponse.success) {
        throw new Error(userResponse?.message || '获取用户信息失败')
      }
      
      // 更新用户基本信息
      Object.assign(userState.userData, userResponse.data)
      
      // 获取会员信息
      const membershipResponse = await membershipApi.getMemberInfo(userState.userData.id)
      
      if (membershipResponse && membershipResponse.success) {
        // 更新会员相关信息
        Object.assign(userState.userData, {
          level: membershipResponse.data.level,
          points: membershipResponse.data.points,
          totalSpent: membershipResponse.data.totalSpent,
          joinDate: membershipResponse.data.joinDate
        })
      }
      
      // 触发用户数据更新事件
      UserEventBus.$emit('userDataUpdated', userState.userData)
      
      return userState.userData
    } catch (error) {
      console.error('获取用户数据时出错:', error)
      userState.error = error.message || '获取用户数据失败'
      
      // 触发用户数据错误事件
      UserEventBus.$emit('userDataError', userState.error)
      throw error
    } finally {
      userState.isLoading = false
    }
  }
  
  // 更新用户信息
  async updateUserInfo(userData) {
    if (!userState.isAuthenticated || !userState.userData.id) {
      throw new Error('用户未登录或ID无效')
    }
    
    userState.isLoading = true
    userState.error = null
    
    try {
      // 调用API更新用户信息
      const response = await userApi.updateUserInfo(userState.userData.id, userData)
      
      if (!response || !response.success) {
        throw new Error(response?.message || '更新用户信息失败')
      }
      
      // 更新本地状态
      Object.assign(userState.userData, userData)
      
      // 触发用户数据更新事件
      UserEventBus.$emit('userDataUpdated', userState.userData)
      
      return true
    } catch (error) {
      console.error('更新用户信息时出错:', error)
      userState.error = error.message || '更新用户信息失败'
      
      // 触发用户数据错误事件
      UserEventBus.$emit('userDataError', userState.error)
      throw error
    } finally {
      userState.isLoading = false
    }
  }
  
  // 修改密码
  async changePassword(oldPassword, newPassword) {
    if (!userState.isAuthenticated || !userState.userData.id) {
      throw new Error('用户未登录或ID无效')
    }
    
    userState.isLoading = true
    userState.error = null
    
    try {
      // 调用API修改密码
      const response = await userApi.changePassword(userState.userData.id, {
        oldPassword,
        newPassword
      })
      
      if (!response || !response.success) {
        throw new Error(response?.message || '修改密码失败')
      }
      
      return true
    } catch (error) {
      console.error('修改密码时出错:', error)
      userState.error = error.message || '修改密码失败'
      throw error
    } finally {
      userState.isLoading = false
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
      
      const { token, refreshToken, expiresAt, userId, userData } = response.data
      
      // 保存token到本地存储
      localStorage.setItem('userToken', token)
      
      if (rememberMe) {
        localStorage.setItem('userRefreshToken', refreshToken)
        localStorage.setItem('tokenExpiresAt', expiresAt)
      }
      
      localStorage.setItem('userId', userId)
      
      // 更新状态
      userState.isAuthenticated = true
      userState.tokenData.accessToken = token
      userState.tokenData.refreshToken = refreshToken
      userState.tokenData.expiresAt = expiresAt
      userState.userData.id = userId
      
      // 如果API返回了用户数据，直接更新
      if (userData) {
        Object.assign(userState.userData, userData)
      } else {
        // 否则获取用户详细信息
        await this.fetchUserData()
      }
      
      return true
    } catch (error) {
      console.error('登录时出错:', error)
      userState.error = error.message || '登录失败'
      
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
    // 清除本地存储
    localStorage.removeItem('userToken')
    localStorage.removeItem('userRefreshToken')
    localStorage.removeItem('tokenExpiresAt')
    localStorage.removeItem('userId')
    
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
    UserEventBus.$emit('userLoggedOut')
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
}

// 创建单例实例
export const UserManager = new UserManagerClass()

// 默认导出
export default UserManager 