<template>
  <div class="community-page" :style="{ backgroundImage: `url(${communityBackground})` }">
    <!-- 头部 -->
    <header class="header">
      <h1></h1>
    </header>

    <!-- 队伍信息卡片 -->
    <div v-if="myTeam" class="my-team-card" :style="{ backgroundImage: `url(${teamCardBackground})` }">
      <div class="team-header">
        <div class="team-avatar">
          <img :src="myTeam.avatar" :alt="myTeam.name" />
        </div>
        <div class="team-info">
          <div class="team-name">{{ myTeam.name }}</div>
          <div class="team-members">已坚持 {{ myTeam.memberCount }} 天</div>
        </div>
      </div>
    </div>

    <!-- 组队操作 -->
    <div v-else class="join-team-section">
      <div class="join-icon">👥</div>
      <p class="join-title">加入或创建队伍</p>
      <p class="join-desc">与小伙伴一起运动，让坚持更有动力</p>
      <div class="join-actions">
        <button @click="createTeam" class="join-btn primary">创建队伍</button>
        <button @click="joinTeam" class="join-btn">加入队伍</button>
      </div>
    </div>

    <!-- 队伍排行榜 -->
    <div class="ranking-section">
      <div class="section-header">
        <h2>成员排行榜</h2>
        <div class="time-filter">
          <button 
            :class="{ active: rankingPeriod === 'day' }"
            @click="rankingPeriod = 'day'"
          >日</button>
          <button 
            :class="{ active: rankingPeriod === 'week' }"
            @click="rankingPeriod = 'week'"
          >周</button>
          <button 
            :class="{ active: rankingPeriod === 'month' }"
            @click="rankingPeriod = 'month'"
          >月</button>
        </div>
      </div>

      <div class="ranking-list">
        <div 
          v-for="(team, index) in users" 
          :key="team.id"
          class="ranking-item"
          :class="{ 'first-place': index === 0, 'second-place': index === 1, 'third-place': index === 2 }"
          :style="getRankingItemStyle(index)"
        >
          <div class="rank-number" :class="getRankClass(index + 1)">
            {{ index + 1 }}
          </div>
          <div class="team-avatar-small">
            <img :src="team.avatar" :alt="team.name" />
          </div>
          <div class="team-details">
            <div class="team-name">{{ team.name }}</div>
            <div class="team-meta">
              已坚持 {{ team.memberCount }} 天 · {{ getCaloriesText(team) }}
            </div>
          </div>
          <div class="team-badge" v-if="index < 3">
            {{ getBadge(index) }}
          </div>
        </div>
      </div>
    </div>
    
    <!-- 邀请成员按钮 -->
    <div class="invite-section">
      <div class="invite-btn" @click="showInviteQRCode">
        <img :src="inviteFriendIcon" alt="邀请好友" class="invite-icon" />
      </div>
    </div>
    
    <!-- 二维码弹窗 -->
    <div v-if="showQRCode" class="qr-modal" @click="closeQRCode">
      <div class="qr-content" @click.stop>
        <div class="qr-header">
          <h3>邀请成员</h3>
          <button @click="closeQRCode" class="close-btn">×</button>
        </div>
        <div class="qr-code">
          <!-- 这里应该是二维码图片，暂时用文本表示 -->
          <div class="qr-placeholder">{{ qrCodeData }}</div>
        </div>
        <div class="share-options">
          <button @click="shareToWeChat('friend')" class="share-btn">
            <img :src="wechatIcon" alt="微信" class="share-icon" />
            <span>微信好友</span>
          </button>
          <button @click="shareToWeChat('moment')" class="share-btn">
            <img :src="momentIcon" alt="朋友圈" class="share-icon" />
            <span>朋友圈</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { Team } from '@/types'
// 导入微信相关图标
import wechatIcon from '@/图标/微信.png?url'
import momentIcon from '@/图标/朋友圈.png?url'
// 导入成员头像
import user1Icon from '@/图标/哈哈哈我是第一名.jpg?url'
import user2Icon from '@/图标/可恶还差一点.png?url'
import user3Icon from '@/图标/马到成功.jpg?url'
import user4Icon from '@/图标/幸福每一天.jpg?url'
import user5Icon from '@/图标/不到一百斤不改名.jpg?url'
import user6Icon from '@/图标/Lucy.jpg?url'
import user7Icon from '@/图标/海风.jpg?url'
import user8Icon from '@/图标/莉莉周.jpeg?url'
// 导入队伍卡片背景
import teamCardBackground from '@/图标/队伍卡片背景.png?url'
// 导入前三名卡片背景
import firstPlaceBackground from '@/图标/第一名卡片背景.png?url'
import secondPlaceBackground from '@/图标/第二名卡片背景.png?url'
import thirdPlaceBackground from '@/图标/第三名卡片背景.png?url'
// 导入邀请好友图片
import inviteFriendIcon from '@/图标/邀请好友.png?url'
// 导入社群页背景
import communityBackground from '@/图标/社群页背景.png?url'

const rankingPeriod = ref<'day' | 'week' | 'month'>('week')
const showQRCode = ref(false)
const qrCodeData = ref('')

const myTeam = ref<Team>({
  id: '', 
  name: '运动小分队',
  avatar: user2Icon,
  memberCount: 45, // 已坚持天数
  totalSteps: 12500, // 本月累计消耗卡路里
  todayCalories: 2450, // 今日消耗卡路里
  weekCalories: 8650, // 本周消耗卡路里
  monthCalories: 12500, // 本月消耗卡路里
  rank: 0 
})

const users = ref<Team[]>([
  { id: '1', name: '哈哈我是第一名', avatar: user1Icon, memberCount: 68, totalSteps: 15200, todayCalories: 3200, weekCalories: 11200, monthCalories: 15200, rank: 1 },
  { id: '2', name: '可恶还差一点', avatar: user2Icon, memberCount: 45, totalSteps: 12500, todayCalories: 2450, weekCalories: 8650, monthCalories: 12500, rank: 2 },
  { id: '3', name: '马到成功', avatar: user3Icon, memberCount: 32, totalSteps: 11800, todayCalories: 1890, weekCalories: 7560, monthCalories: 11800, rank: 3 },
  { id: '4', name: '幸福每一天', avatar: user4Icon, memberCount: 78, totalSteps: 9600, todayCalories: 1650, weekCalories: 6800, monthCalories: 9600, rank: 4 },
  { id: '5', name: '不到一百斤不改名', avatar: user5Icon, memberCount: 25, totalSteps: 8900, todayCalories: 1420, weekCalories: 5980, monthCalories: 8900, rank: 5 },
  { id: '6', name: 'Lucy', avatar: user6Icon, memberCount: 56, totalSteps: 7800, todayCalories: 1280, weekCalories: 5200, monthCalories: 7800, rank: 6 },
  { id: '7', name: '海风', avatar: user7Icon, memberCount: 18, totalSteps: 6500, todayCalories: 980, weekCalories: 4320, monthCalories: 6500, rank: 7 },
  { id: '8', name: '莉莉周', avatar: user8Icon, memberCount: 41, totalSteps: 5200, todayCalories: 850, weekCalories: 3650, monthCalories: 5200, rank: 8 }
])

const formatNumber = (num: number) => {
  return num.toLocaleString()
}

const getCaloriesText = (team: Team) => {
  if (rankingPeriod.value === 'day') {
    return `今日消耗 ${formatNumber(team.todayCalories || 0)} 卡路里`
  } else if (rankingPeriod.value === 'week') {
    return `本周消耗 ${formatNumber(team.weekCalories || 0)} 卡路里`
  } else {
    return `本月消耗 ${formatNumber(team.monthCalories || 0)} 卡路里`
  }
}

const getRankClass = (rank: number) => {
  if (rank === 1) return 'gold'
  if (rank === 2) return 'silver'
  if (rank === 3) return 'bronze'
  return ''
}

const getBadge = (index: number) => {
  const badges = ['', '', '']
  return badges[index]
}

const createTeam = () => {
  console.log('创建队伍')
}

const joinTeam = () => {
  console.log('加入队伍')
}

const showInviteQRCode = () => {
  // 生成邀请二维码数据
  qrCodeData.value = `邀请您加入队伍：${myTeam.value.name}`
  showQRCode.value = true
  console.log('显示邀请二维码')
}

const closeQRCode = () => {
  showQRCode.value = false
}

const shareToWeChat = (platform: 'friend' | 'moment') => {
  console.log(`分享到微信${platform === 'friend' ? '好友' : '朋友圈'}`)
  // 这里可以调用微信API或使用浏览器分享功能
  if (navigator.share) {
    navigator.share({
      title: '邀请加入队伍',
      text: qrCodeData.value,
      url: window.location.href
    }).catch(error => console.log('分享取消或失败:', error))
  } else {
    // 降级处理，复制链接到剪贴板
    navigator.clipboard.writeText(window.location.href).then(() => {
      alert('链接已复制到剪贴板')
    })
  }
}

const getRankingItemStyle = (index: number) => {
  if (index === 0) {
    return { backgroundImage: `url(${firstPlaceBackground})` }
  } else if (index === 1) {
    return { backgroundImage: `url(${secondPlaceBackground})` }
  } else if (index === 2) {
    return { backgroundImage: `url(${thirdPlaceBackground})` }
  }
  return {}
}
</script>

<style scoped lang="scss">
.community-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
  background-size: cover;
  background-position: center;
  
  // 深色主题
  body.dark-theme & {
    background: #1a1a1a;
    background-size: cover;
    background-position: center;
  }
}

.header {
  margin-bottom: 20px;
  
  h1 {
    color: #333;
    font-size: 24px;
    font-weight: bold;
  }
}

.my-team-card {
  background-size: cover;
  background-position: center;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  
  // 深色主题
  body.dark-theme & {
    background-size: cover;
    background-position: center;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  }
  
  .team-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 20px;
    
    .team-avatar {
      width: 50px;
      height: 50px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
    
    .team-info {
      flex: 1;
      
      .team-name {
        font-size: 18px;
        font-weight: bold;
        margin-bottom: 4px;
        
        // 深色主题
        body.dark-theme & {
          color: #f0f0f0;
        }
      }
      
      .team-members {
        font-size: 14px;
        opacity: 0.9;
        
        // 深色主题
        body.dark-theme & {
          color: #ddd;
        }
      }
    }
    
    .team-rank {
      text-align: center;
      
      .rank-label {
        font-size: 12px;
        opacity: 0.9;
        margin-bottom: 4px;
        
        // 深色主题
        body.dark-theme & {
          color: #ddd;
        }
      }
      
      .rank-value {
        font-size: 24px;
        font-weight: bold;
        
        // 深色主题
        body.dark-theme & {
          color: #f0f0f0;
        }
      }
    }
  }
  
  .team-actions {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    
    .action-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      padding: 12px;
      border: 1px solid rgba(255, 255, 255, 0.5);
      border-radius: 8px;
      background: rgba(255, 255, 255, 0.2);
      color: white;
      font-size: 14px;
      cursor: pointer;
      transition: all 0.3s;
      
      // 深色主题
      body.dark-theme & {
        background: rgba(255, 255, 255, 0.1);
        border-color: rgba(255, 255, 255, 0.3);
      }
      
      &.primary {
        background: rgba(255, 255, 255, 0.3);
      }
      
      &:active {
        transform: scale(0.95);
      }
    }
  }
}

.join-team-section {
  background: white;
  border-radius: 16px;
  padding: 40px 20px;
  margin-bottom: 20px;
  text-align: center;
  
  // 深色主题
  body.dark-theme & {
    background: #333;
  }
  
  .join-icon {
    font-size: 64px;
    margin-bottom: 16px;
    
    // 深色主题
    body.dark-theme & {
      color: #f0f0f0;
    }
  }
  
  .join-title {
    font-size: 18px;
    color: #333;
    font-weight: bold;
    margin-bottom: 8px;
    
    // 深色主题
    body.dark-theme & {
      color: #f0f0f0;
    }
  }
  
  .join-desc {
    font-size: 14px;
    color: #999;
    margin-bottom: 24px;
    
    // 深色主题
    body.dark-theme & {
      color: #bbb;
    }
  }
  
  .join-actions {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    
    .join-btn {
      padding: 12px;
      border: 1px solid #F08713;
      border-radius: 8px;
      background: white;
      color: #F08713;
      font-size: 14px;
      cursor: pointer;
      transition: all 0.3s;
      
      // 深色主题
      body.dark-theme & {
        background: #333;
        border-color: #F08713;
      }
      
      &.primary {
        background: #F08713;
        color: white;
      }
      
      &:active {
        transform: scale(0.95);
      }
    }
  }
}

.ranking-section {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  // 深色主题
  body.dark-theme & {
    background: rgba(51, 51, 51, 0.75);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  h2 {
    font-size: 18px;
    color: #333;
    font-weight: bold;
  }
  
  .time-filter {
    display: flex;
    gap: 8px;
    
    button {
      padding: 6px 12px;
      border: 1px solid #e0e0e0;
      border-radius: 12px;
      background: white;
      color: #666;
      font-size: 14px;
      cursor: pointer;
      transition: all 0.3s;
      
      &.active {
        background: #91B3F0;
        color: white;
        border-color: #91B3F0;
      }
    }
  }
  
  .member-count {
    font-size: 14px;
    color: #999;
  }
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  
  .ranking-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: #f9f9f9;
    border-radius: 8px;
    transition: all 0.3s;
    background-size: cover;
    background-position: center;
    
    // 深色主题
    body.dark-theme & {
      background: #444;
    }
    
    &.first-place,
    &.second-place,
    &.third-place {
      background-size: cover;
      background-position: center;
      
      .team-name {
        color: white;
      }
      
      .team-meta {
        color: rgba(255, 255, 255, 0.9);
      }
      
      .rank-number {
        color: white;
      }
    }
    
    .rank-number {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #666;
      font-weight: bold;
      font-size: 16px;
      
      &.gold {
        background: none;
        color: #FFD700;
      }
      
      &.silver {
        background: none;
        color: #C0C0C0;
      }
      
      &.bronze {
        background: none;
        color: #CD7F32;
      }
    }
    
    .team-avatar-small {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
    
    .team-details {
      flex: 1;
      
      .team-name {
        font-size: 16px;
        color: #333;
        font-weight: 500;
        margin-bottom: 4px;
        
        // 深色主题
        body.dark-theme & {
          color: #f0f0f0;
        }
      }
      
      .team-meta {
        font-size: 12px;
        color: #999;
        
        // 深色主题
        body.dark-theme & {
          color: #bbb;
        }
      }
    }
    
    .team-badge {
      font-size: 24px;
    }
  }
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  
  .member-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: #f9f9f9;
    border-radius: 8px;
    
    .member-avatar {
      width: 40px;
      height: 40px;
      background: #f0f0f0;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
    }
    
    .member-info {
      flex: 1;
      
      .member-name {
        font-size: 16px;
        color: #333;
        margin-bottom: 4px;
      }
      
      .member-steps {
        font-size: 12px;
        color: #999;
      }
    }
    
    .member-trend {
      font-size: 20px;
      
      .up {
        color: #6BCF7F;
      }
      
      .down {
        color: #FF6B6B;
      }
    }
  }
}

.invite-section {
  margin-top: 20px;
  text-align: center;
  
  .invite-btn {
    display: inline-block;
    cursor: pointer;
    transition: all 0.3s;
    
    .invite-icon {
      width: 180px;
      height: 60px;
      object-fit: contain;
    }
    
    &:active {
      transform: scale(0.95);
    }
  }
}

.qr-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  
  .qr-content {
    background: white;
    border-radius: 16px;
    padding: 20px;
    width: 300px;
    text-align: center;
    
    // 深色主题
    body.dark-theme & {
      background: #333;
    }
    
    .qr-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      
      h3 {
        margin: 0;
        font-size: 18px;
        color: #333;
        
        // 深色主题
        body.dark-theme & {
          color: #f0f0f0;
        }
      }
      
      .close-btn {
        background: none;
        border: none;
        font-size: 24px;
        cursor: pointer;
        color: #999;
        
        // 深色主题
        body.dark-theme & {
          color: #f0f0f0;
        }
      }
    }
    
    .qr-code {
      margin-bottom: 20px;
      
      .qr-placeholder {
        background: #f0f0f0;
        border-radius: 8px;
        padding: 20px;
        min-height: 150px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 14px;
        color: #666;
        
        // 深色主题
        body.dark-theme & {
          background: #444;
          color: #f0f0f0;
        }
      }
    }
    
    .share-options {
      display: flex;
      gap: 12px;
      
      .share-btn {
        flex: 1;
        padding: 12px;
        border: 1px solid #e0e0e0;
        border-radius: 8px;
        background: white;
        color: #333;
        font-size: 14px;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
        transition: all 0.3s;
        
        // 深色主题
        body.dark-theme & {
          background: #444;
          border-color: #555;
          color: #f0f0f0;
        }
        
        &:active {
          transform: scale(0.95);
        }
        
        .share-icon {
          width: 24px;
          height: 24px;
          object-fit: contain;
        }
      }
    }
  }
}
</style>