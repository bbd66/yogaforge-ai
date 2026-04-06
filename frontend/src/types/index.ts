// 宠物状态类型
export interface PetStatus {
  hunger: number // 饥饿值 0-100
  happiness: number // 心情值 0-100
  energy: number // 能量值 0-100
  level: number // 等级
  exp: number // 经验值
}

// 宠物类型
export interface Pet {
  id: string
  name: string
  type: string
  avatar: string
  status: PetStatus
  createdAt: string
}

// 运动数据类型
export interface WorkoutData {
  id: string
  date: string
  type: string // 运动类型
  duration: number // 时长(分钟)
  calories: number // 卡路里
  distance?: number // 距离(km)
  steps?: number // 步数
}

// 成就类型
export interface Achievement {
  id: string
  name: string
  description: string
  icon: string
  unlocked: boolean
  unlockedAt?: string
}

// 蓝牙设备类型
export interface BluetoothDevice {
  id: string
  name: string
  type: string
  connected: boolean
  battery?: number
  icon?: string
}

// 队伍类型
export interface Team {
  id: string
  name: string
  avatar: string
  memberCount: number // 已坚持天数
  totalSteps: number // 本月累计消耗卡路里
  todayCalories?: number // 今日消耗卡路里
  weekCalories?: number // 本周消耗卡路里
  monthCalories?: number // 本月消耗卡路里
  rank: number
}

// 用户类型
export interface User {
  id: string
  username: string
  avatar: string
  email: string
  phone?: string
  gender?: 'male' | 'female' | 'other'
  birthday?: string
  height?: number
  weight?: number
  createdAt: string
}
