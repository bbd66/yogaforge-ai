import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Pet } from '@/types'
// 导入宠物图标
import petAvatar from '@/图标/宠物.png'

export const usePetStore = defineStore('pet', () => {
  const pet = ref<Pet>({
    id: '1',
    name: '小橘',
    type: 'cat',
    avatar: petAvatar,
    status: {
      hunger: 80,
      happiness: 75,
      energy: 60,
      level: 5,
      exp: 350
    },
    createdAt: new Date().toISOString()
  })

  const motivationalQuote = ref('每一次运动都是对未来最好的投资！')

  // 可用喂食次数（由商城购买食物增加，在首页喂食时减少）
  const feedCount = ref(0)

  // 喂食
  const feed = () => {
    if (pet.value.status.energy < 100) {
      pet.value.status.energy = Math.min(100, pet.value.status.energy + 20)
      pet.value.status.happiness = Math.min(100, pet.value.status.happiness + 5)
    }
  }

  // 玩耍
  const play = () => {
    if (pet.value.status.energy > 10) {
      pet.value.status.happiness = Math.min(100, pet.value.status.happiness + 15)
      pet.value.status.energy = Math.max(0, pet.value.status.energy - 10)
      pet.value.status.hunger = Math.min(100, pet.value.status.hunger + 5)
    }
  }

  // 清洁
  const clean = () => {
    pet.value.status.happiness = Math.min(100, pet.value.status.happiness + 10)
  }

  // 增加经验值
  const addExp = (amount: number) => {
    pet.value.status.exp += amount
    // 简单的升级逻辑
    const requiredExp = pet.value.status.level * 100
    if (pet.value.status.exp >= requiredExp) {
      pet.value.status.level++
      pet.value.status.exp -= requiredExp
    }
  }

  // 更新励志语录
  const updateMotivationalQuote = (newQuote: string) => {
    motivationalQuote.value = newQuote
  }

  // 增加/减少喂食次数
  const addFeedCount = (delta: number) => {
    const next = feedCount.value + delta
    feedCount.value = Math.max(0, next)
  }

  return {
    pet,
    motivationalQuote,
    feedCount,
    feed,
    play,
    clean,
    addExp,
    updateMotivationalQuote,
    addFeedCount
  }
})