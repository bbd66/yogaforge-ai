<template>
  <div class="data-page">
    <!-- 趋势图表 -->
    <div class="chart-section">
      <div class="section-header">
        <h2 class="subtle-header">打卡总览</h2>
      </div>
      <div class="checkin-overview">
        <!-- 月份选择 -->
        <div class="month-selector">
          <button @click="prevMonth" class="nav-btn">‹</button>
          <div class="current-month">{{ currentMonthLabel }}</div>
          <button @click="nextMonth" class="nav-btn">›</button>
        </div>
        
        <!-- 月历 -->
        <div class="calendar">
          <!-- 星期标题 -->
          <div class="weekdays">
            <div class="weekday" v-for="(weekday, index) in weekdays" :key="index">{{ weekday }}</div>
          </div>
          
          <!-- 日期网格 -->
          <div class="days-grid">
            <!-- 空白日期占位 -->
            <div 
              class="day-placeholder" 
              v-for="n in firstDayOfMonth" 
              :key="`placeholder-${n}`"
            ></div>
            
            <!-- 日期 -->
            <div 
              v-for="day in daysInMonth" 
              :key="day"
              class="day-cell"
              :class="{ 
                'today': isToday(day),
                'checked-in': isCheckedIn(day),
                'other-month': isOtherMonth(day)
              }"
              @click="selectDay(day)"
            >
              <div class="day-number">{{ day }}</div>
              <div v-if="isCheckedIn(day)" class="checkin-indicator"></div>
            </div>
          </div>
        </div>
        
        <!-- 打卡统计 -->
        <div class="checkin-stats">
          <div class="stat-card">
            <div class="stat-value">{{ checkInDays }}</div>
            <div class="stat-label">本月打卡</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ streakDays }}</div>
            <div class="stat-label">连续打卡</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ completionRate }}%</div>
            <div class="stat-label">完成率</div>
          </div>
        </div>
        
        <!-- 打卡详情 -->
        <div class="checkin-details" v-if="selectedDate">
          <h3>{{ selectedDateLabel }} 的打卡详情</h3>
          <div class="detail-item">
            <span>运动时长</span>
            <span>{{ getExerciseMinutes(selectedDate) }} 分钟</span>
          </div>
          <div class="detail-item">
            <span>消耗卡路里</span>
            <span>{{ getCaloriesBurned(selectedDate) }} kcal</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据概览卡片 -->
    <div class="overview-cards">
      <div class="overview-card" @click="goToCaloriesDetail">
        <div class="card-icon">
          <img :src="CaloriesIcon" alt="卡路里" />
        </div>
        <div class="card-value">{{ todayCalories }}</div>
        <div class="card-label">消耗卡路里</div>
      </div>
      <div class="overview-card" @click="goToExerciseDuration">
        <div class="card-icon">
          <img :src="DurationIcon" alt="运动时长" />
        </div>
        <div class="card-value">{{ todayMinutes }}</div>
        <div class="card-label">运动时长(分)</div>
      </div>
    </div>

    <!-- 成就、设备和体重模块暂时下线 -->
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
// 引入API客户端
import api from '@/api'

// 导入SVG图标
import CaloriesIcon from '@/图标/卡路里.svg'
import DurationIcon from '@/图标/运动时长.svg'

const router = useRouter()
// const timeRange = ref<'week' | 'month'>('week')

const todaySteps = ref(8650)
const todayCalories = ref(385)
const todayMinutes = ref(45)

/*
 * 后端接口调用示例（供后端开发人员参考）
 * 
 * 1. 获取用户运动数据接口
 * 接口地址: GET /api/user/workout-data
 * 请求参数: 
 *   - userId: string (用户ID)
 *   - period: 'week' | 'month' (时间范围)
 * 返回数据格式:
 *   {
 *     "success": true,
 *     "data": {
 *       "weeklyData": [
 *         { "date": "2025-01-01", "steps": 6500, "calories": 320, "minutes": 45 },
 *         { "date": "2025-01-02", "steps": 7200, "calories": 350, "minutes": 50 },
 *         // ... 其他日期数据
 *       ],
 *       "monthlyData": [
 *         { "week": "W1", "steps": 28000, "calories": 1200, "minutes": 200 },
 *         // ... 其他周数据
 *       ]
 *     }
 *   }
 * 
 * 2. 获取用户成就数据接口
 * 接口地址: GET /api/user/achievements
 * 请求参数:
 *   - userId: string (用户ID)
 * 返回数据格式:
 *   {
 *     "success": true,
 *     "data": [
 *       { "id": "1", "name": "初出茅庐", "description": "完成第一次运动", "icon": "🏅", "unlocked": true, "unlockedAt": "2025-01-01" },
 *       { "id": "2", "name": "坚持不懈", "description": "连续运动7天", "icon": "🔥", "unlocked": true, "unlockedAt": "2025-01-07" },
 *       // ... 其他成就数据
 *     ]
 *   }
 */

// 获取今日数据（来源于后端 /api/user/today-data）
const fetchTodayData = async () => {
  try {
    const response = await api.get('/user/today-data')
    if (response.success && response.data) {
      todaySteps.value = response.data.steps
      todayCalories.value = response.data.calories
      todayMinutes.value = response.data.minutes
    }
  } catch (error) {
    console.error('获取今日数据失败:', error)
  }
}

onMounted(() => {
  fetchTodayData()
})

const goToCaloriesDetail = () => {
  router.push('/calories-detail')
}

const goToExerciseDuration = () => {
  router.push('/exercise-duration')
}

// 当前显示的月份（用于月历导航）
const currentMonthOffset = ref(0)

// 当前月份标签
const currentMonthLabel = computed(() => {
  const date = new Date()
  date.setMonth(date.getMonth() + currentMonthOffset.value)
  return `${date.getFullYear()}年${date.getMonth() + 1}月`
})

// 上一个月
const prevMonth = () => {
  currentMonthOffset.value--
}

// 下一个月
const nextMonth = () => {
  currentMonthOffset.value++
}

// 星期标题
const weekdays = ['日', '一', '二', '三', '四', '五', '六']

// 当前月份的第一天是星期几 (0-6, 0表示星期日)
const firstDayOfMonth = computed(() => {
  const date = new Date()
  date.setMonth(date.getMonth() + currentMonthOffset.value)
  date.setDate(1)
  return date.getDay()
})

// 当前月份的天数
const daysInMonth = computed(() => {
  const date = new Date()
  date.setMonth(date.getMonth() + currentMonthOffset.value)
  const year = date.getFullYear()
  const month = date.getMonth()
  return new Date(year, month + 1, 0).getDate()
})

// 检查是否是今天
const isToday = (day: number) => {
  const today = new Date()
  const currentMonth = new Date()
  currentMonth.setMonth(currentMonth.getMonth() + currentMonthOffset.value)
  
  return (
    today.getFullYear() === currentMonth.getFullYear() &&
    today.getMonth() === currentMonth.getMonth() &&
    today.getDate() === day
  )
}

// 检查是否是其他月份的日期
const isOtherMonth = (day: number) => {
  // 在这个实现中，我们只显示当前月份的日期，所以这个函数总是返回false
  // 但在更复杂的实现中，可能会显示前后月份的日期
  return false
}

// 模拟打卡数据（实际项目中应从后端获取）
const checkInData = ref([
  '2025-10-01',
  '2025-10-02',
  '2025-10-03',
  '2025-10-04',
  '2025-10-05',
  '2025-10-07',
  '2025-10-09',
  '2025-10-11',
  '2025-10-13',
  '2025-10-15',
  '2025-10-17',
  '2025-10-19',
  '2025-10-21',
  '2025-10-23',
  '2025-10-25',
  '2025-10-27',
  '2025-10-29',
  '2025-10-31'
])

// 检查某天是否打卡
const isCheckedIn = (day: number) => {
  const currentDate = new Date()
  currentDate.setMonth(currentDate.getMonth() + currentMonthOffset.value)
  const year = currentDate.getFullYear()
  const month = String(currentDate.getMonth() + 1).padStart(2, '0')
  const dateStr = `${year}-${month}-${String(day).padStart(2, '0')}`
  return checkInData.value.includes(dateStr)
}

// 选中的日期
const selectedDate = ref<string | null>(null)

// 选中日期标签
const selectedDateLabel = computed(() => {
  if (!selectedDate.value) return ''
  const date = new Date(selectedDate.value)
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
})

// 选择日期
const selectDay = (day: number) => {
  const currentDate = new Date()
  currentDate.setMonth(currentDate.getMonth() + currentMonthOffset.value)
  const year = currentDate.getFullYear()
  const month = String(currentDate.getMonth() + 1).padStart(2, '0')
  const dateStr = `${year}-${month}-${String(day).padStart(2, '0')}`
  selectedDate.value = dateStr
}

// 打卡天数
const checkInDays = computed(() => {
  const currentDate = new Date()
  currentDate.setMonth(currentDate.getMonth() + currentMonthOffset.value)
  const year = currentDate.getFullYear()
  const month = String(currentDate.getMonth() + 1).padStart(2, '0')
  
  return checkInData.value.filter(date => 
    date.startsWith(`${year}-${month}`)
  ).length
})

// 计算连续打卡天数
const streakDays = computed(() => {
  const currentDate = new Date()
  currentDate.setMonth(currentDate.getMonth() + currentMonthOffset.value)
  const year = currentDate.getFullYear()
  const month = String(currentDate.getMonth() + 1).padStart(2, '0')
  
  // 获取当前月份的所有打卡日期并排序
  const currentMonthCheckIns = checkInData.value
    .filter(date => date.startsWith(`${year}-${month}`))
    .map(date => parseInt(date.split('-')[2]))
    .sort((a, b) => a - b)
  
  // 计算连续打卡天数
  if (currentMonthCheckIns.length === 0) return 0
  
  let streak = 1
  let maxStreak = 1
  
  for (let i = 1; i < currentMonthCheckIns.length; i++) {
    if (currentMonthCheckIns[i] === currentMonthCheckIns[i-1] + 1) {
      streak++
      maxStreak = Math.max(maxStreak, streak)
    } else {
      streak = 1
    }
  }
  
  return maxStreak
})

// 完成率
const completionRate = computed(() => {
  const totalDays = daysInMonth.value
  const checkedDays = checkInDays.value
  return Math.round((checkedDays / totalDays) * 100)
})

// 获取某天的运动时长（模拟数据）
const getExerciseMinutes = (date: string) => {
  // 只有打卡的日期才显示运动时长
  if (checkInData.value.includes(date)) {
    // 模拟数据，实际应从后端获取
    const minutes = [30, 45, 60, 40, 50, 35, 55]
    const index = Math.abs(date.split('-').join('').split('').reduce((sum, char) => sum + char.charCodeAt(0), 0)) % minutes.length
    return minutes[index]
  }
  return 0
}

// 获取某天消耗的卡路里（模拟数据）
const getCaloriesBurned = (date: string) => {
  // 只有打卡的日期才显示卡路里消耗
  if (checkInData.value.includes(date)) {
    // 模拟数据，实际应从后端获取
    const calories = [200, 300, 400, 250, 350, 220, 380]
    const index = Math.abs(date.split('-').join('').split('').reduce((sum, char) => sum + char.charCodeAt(0), 0)) % calories.length
    return calories[index]
  }
  return 0
}

// const goToCheckInOverview = () => {
  // router.push('/check-in-overview')
// }
</script>

<style scoped lang="scss">
.data-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
  background-image: url('@/图标/社群页背景.png');
  background-size: cover;
  background-position: center;
  
  // 深色主题
  body.dark-theme & {
    background: #1a1a1a;
    background-image: url('@/图标/社群页背景.png');
    background-size: cover;
    background-position: center;
  }
}
  

.overview-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
  
  .overview-card {
    background: white;
    border-radius: 12px;
    padding: 16px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    
    // 深色主题
    body.dark-theme & {
      background: #444;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
    }
    
    .card-icon {
      font-size: 24px;
      margin-bottom: 8px;
    }
    
    .card-value {
      font-size: 20px;
      font-weight: bold;
      color: #333;
      margin-bottom: 4px;
      
      // 深色主题
      body.dark-theme & {
        color: #f0f0f0;
      }
    }
    
    .card-label {
      font-size: 12px;
      color: #999;
      
      // 深色主题
      body.dark-theme & {
        color: #bbb;
      }
    }
  }
}

.chart-section {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  // 深色主题
  body.dark-theme & {
    background: #444;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  }
  
  .subtle-header {
    font-size: 14px;
    font-weight: normal;
    color: #999;
    text-align: center;
    margin: 0 0 16px 0;
    
    // 深色主题
    body.dark-theme & {
      color: #bbb;
    }
  }
}

.bottom-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  
  .overview-card {
    background: white;
    border-radius: 12px;
    padding: 16px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    
    // 深色主题
    body.dark-theme & {
      background: #444;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
    }
    
    .card-icon {
      font-size: 24px;
      margin-bottom: 8px;
    }
    
    .card-value {
      font-size: 20px;
      font-weight: bold;
      color: #333;
      margin-bottom: 4px;
      
      // 深色主题
      body.dark-theme & {
        color: #f0f0f0;
      }
    }
    
    .card-label {
      font-size: 12px;
      color: #999;
      
      // 深色主题
      body.dark-theme & {
        color: #bbb;
      }
    }
  }
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  margin-bottom: 10px;
  
  .weekday {
    font-size: 14px;
    color: #999;
    padding: 8px 0;
  }
}

.days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  
  .day-placeholder {
    padding: 12px 0;
  }
  
  .day-cell {
    position: relative;
    padding: 12px 0;
    text-align: center;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      background: #f0f0f0;
    }
    
    &.today {
      background: #E996EF;
      color: white;
    }
    
    &.checked-in {
      background: #91B3F0;
      color: white;
    }
    
    &.other-month {
      color: #ccc;
    }
    
    .day-number {
      font-size: 16px;
      font-weight: 500;
    }
    
    .checkin-indicator {
      position: absolute;
      bottom: 4px;
      left: 50%;
      transform: translateX(-50%);
      width: 6px;
      height: 6px;
      background: white;
      border-radius: 50%;
    }
  }
}

.month-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  
  .nav-btn {
    width: 32px;
    height: 32px;
    border: none;
    background: #f0f0f0;
    border-radius: 50%;
    font-size: 16px;
    font-weight: bold;
    color: #666;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s;
    
    &:active {
      transform: scale(0.95);
    }
  }
  
  .current-month {
    margin: 0 16px;
    font-size: 18px;
    font-weight: 500;
    color: #333;
  }
}

.checkin-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin: 20px 0;
  
  .stat-card {
    background: white;
    border-radius: 12px;
    padding: 16px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    
    // 深色主题
    body.dark-theme & {
      background: #444;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
    }
    
    .stat-value {
      font-size: 24px;
      font-weight: bold;
      color: #91B3F0;
      margin-bottom: 8px;
    }
    
    .stat-label {
      font-size: 14px;
      color: #666;
      
      // 深色主题
      body.dark-theme & {
        color: #bbb;
      }
    }
  }
}

.checkin-details {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-top: 20px;
  
  // 深色主题
  body.dark-theme & {
    background: #444;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  }
  
  h3 {
    font-size: 18px;
    color: #333;
    margin: 0 0 16px 0;
    text-align: center;
    
    // 深色主题
    body.dark-theme & {
      color: #f0f0f0;
    }
  }
  
  .detail-item {
    display: flex;
    justify-content: space-between;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    // 深色主题
    body.dark-theme & {
      border-bottom: 1px solid #555;
    }
    
    span:first-child {
      font-size: 16px;
      color: #666;
      
      // 深色主题
      body.dark-theme & {
        color: #bbb;
      }
    }
    
    span:last-child {
      font-size: 16px;
      font-weight: 500;
      color: #333;
      
      // 深色主题
      body.dark-theme & {
        color: #f0f0f0;
      }
    }
  }
}
</style>