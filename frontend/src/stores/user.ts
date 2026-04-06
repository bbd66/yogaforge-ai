import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'

export const useUserStore = defineStore('user', () => {
  const user = ref<User>({
    id: '1',
    username: '运动达人',
    avatar: '👤',
    email: 'user@petfit.com',
    createdAt: new Date().toISOString()
  })

  const coins = ref<number>(1000) // 初始金币数

  const isLoggedIn = ref(true)

  const login = async (email: string, password: string) => {
    // 模拟登录
    isLoggedIn.value = true
  }

  const logout = () => {
    isLoggedIn.value = false
  }

  const setUser = (payload: { id: string | number; username: string; email: string; avatar: string }) => {
    user.value = {
      id: String(payload.id),
      username: payload.username,
      email: payload.email,
      avatar: payload.avatar,
      createdAt: user.value.createdAt
    }
  }

  const addCoins = (amount: number) => {
    coins.value += amount
  }

  const deductCoins = (amount: number) => {
    if (coins.value >= amount) {
      coins.value -= amount
      return true
    }
    return false
  }

  return {
    user,
    coins,
    isLoggedIn,
    login,
    logout,
    setUser,
    addCoins,
    deductCoins
  }
})
