<template>
  <div class="achievements-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <button @click="goBack" class="back-btn">
        <img :src="backIcon" alt="返回" class="back-icon" />
      </button>
      <h1 class="page-title">成就</h1>
    </header>

    <!-- 成就统计 -->
    <div class="achievements-stats">
      <div class="stats-card">
        <div class="stats-value">{{ unlockedCount }}/{{ achievements.length }}</div>
        <div class="stats-label">已解锁成就</div>
      </div>
    </div>

    <!-- 成就列表 -->
    <div class="achievements-list">
      <div 
        v-for="achievement in achievements" 
        :key="achievement.id"
        class="achievement-item"
        :class="{ unlocked: achievement.unlocked }"
      >
        <div class="achievement-icon">
          <img :src="getAchievementIcon(achievement.id)" :alt="achievement.name" />
        </div>
        <div class="achievement-content">
          <h3 class="achievement-name">{{ achievement.name }}</h3>
          <p class="achievement-description">{{ achievement.description }}</p>
          <div v-if="achievement.unlocked" class="unlock-info">
            <span class="unlock-date">解锁于 {{ achievement.unlockedAt }}</span>
          </div>
          <div v-else class="locked-info">
            <span class="locked-text">未解锁</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

// 导入图标
import backIcon from '@/图标/返回.svg'
import achievementIcon1 from '@/图标/成就徽章1号.png'
import achievementIcon2 from '@/图标/成就徽章2号.png'
import achievementIcon3 from '@/图标/成就徽章3号.png'

const router = useRouter()

const goBack = () => {
  router.back()
}

// 成就数据
const achievements = ref([
  { 
    id: '1', 
    name: '好的开始', 
    description: '完成第一次运动', 
    unlocked: true, 
    unlockedAt: '2025-01-01' 
  },
  { 
    id: '2', 
    name: '一群人走得更远', 
    description: '成功和好友组队', 
    unlocked: true, 
    unlockedAt: '2025-01-07' 
  },
  { 
    id: '3', 
    name: '燃烧我的卡路里', 
    description: '累计卡路里消耗量达3500千卡', 
    unlocked: false 
  },
  { 
    id: '4', 
    name: '脂少还有你', 
    description: '累计消耗10000卡路里', 
    unlocked: false 
  },
  { 
    id: '5', 
    name: '坚持不懈', 
    description: '连续打卡1周', 
    unlocked: true, 
    unlockedAt: '2025-01-15' 
  }
])

// 计算已解锁的成就数量
const unlockedCount = computed(() => {
  return achievements.value.filter(a => a.unlocked).length
})

// 根据成就ID获取对应的图标
const getAchievementIcon = (id: string) => {
  switch (id) {
    case '1':
      return achievementIcon1
    case '2':
      return achievementIcon2
    case '3':
      return achievementIcon3
    case '4':
      return achievementIcon1
    case '5':
      return achievementIcon2
    default:
      return achievementIcon1
  }
}
</script>

<style scoped lang="scss">
.achievements-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
  padding-top: 60px;
  padding-bottom: 80px;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  z-index: 100;
  
  .back-btn {
    width: 40px;
    height: 40px;
    border: none;
    background: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .back-icon {
      width: 24px;
      height: 24px;
    }
  }
  
  .page-title {
    flex: 1;
    text-align: center;
    font-size: 18px;
    font-weight: bold;
    color: #333;
    margin: 0;
  }
}

.achievements-stats {
  margin-bottom: 20px;
  
  .stats-card {
    background: white;
    border-radius: 16px;
    padding: 20px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    
    .stats-value {
      font-size: 32px;
      font-weight: bold;
      color: #F08713;
      margin-bottom: 8px;
    }
    
    .stats-label {
      font-size: 16px;
      color: #666;
    }
  }
}

.achievements-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.achievement-item {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  opacity: 0.6;
  
  &.unlocked {
    opacity: 1;
  }
  
  .achievement-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: #f0f0f0;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    
    img {
      width: 40px;
      height: 40px;
      object-fit: contain;
    }
  }
  
  .achievement-content {
    flex: 1;
    
    .achievement-name {
      font-size: 18px;
      font-weight: bold;
      color: #333;
      margin: 0 0 8px 0;
    }
    
    .achievement-description {
      font-size: 14px;
      color: #666;
      margin: 0 0 12px 0;
      line-height: 1.4;
    }
    
    .unlock-info {
      .unlock-date {
        font-size: 12px;
        color: #999;
      }
    }
    
    .locked-info {
      .locked-text {
        font-size: 12px;
        color: #ccc;
        font-style: italic;
      }
    }
  }
}
</style>