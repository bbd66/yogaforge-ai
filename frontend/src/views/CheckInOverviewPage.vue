<template>
  <div class="checkin-overview-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <button @click="goBack" class="back-btn">
        <img :src="backIcon" alt="返回" class="back-icon" />
      </button>
      <h1 class="page-title">打卡总览</h1>
    </header>

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
        <div class="weekday" v-for="weekday in weekdays" :key="weekday">{{ weekday }}</div>
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
          v-for="dayNumber in daysInMonth" 
          :key="dayNumber"
          class="day-cell"
          :class="{ 
            'today': isToday(dayNumber),
            'checked-in': isCheckedIn(dayNumber),
            'other-month': isOtherMonth(dayNumber)
          }"
          @click="selectDay(dayNumber)"
        >
          <div class="day-number">{{ dayNumber }}</div>
          <div v-if="isCheckedIn(dayNumber)" class="checkin-indicator"></div>
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
      <div class="detail-item">
        <span>步数</span>
        <span>{{ getSteps(selectedDate) }} 步</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

// 导入图标
import backIcon from '@/图标/返回.svg'

const router = useRouter()

const goBack = () => {
  router.back()
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
const isToday = (_day: number) => {
  const today = new Date()
  const currentMonth = new Date()
  currentMonth.setMonth(currentMonth.getMonth() + currentMonthOffset.value)
  
  return (
    today.getFullYear() === currentMonth.getFullYear() &&
    today.getMonth() === currentMonth.getMonth() &&
    today.getDate() === _day
  )
}

// 检查是否是其他月份的日期
const isOtherMonth = (_day: number) => {
  // 在这个实现中，我们只显示当前月份的日期，所以这个函数总是返回false
  // 但在更复杂的实现中，可能会显示前后月份的日期
  return false
}

// 模拟打卡数据（实际项目中应从后端获取）
const checkInData = ref([
  '2025-10-01',
  '2025-10-03',
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
const isCheckedIn = (_day: number) => {
  const currentDate = new Date()
  currentDate.setMonth(currentDate.getMonth() + currentMonthOffset.value)
  const year = currentDate.getFullYear()
  const month = String(currentDate.getMonth() + 1).padStart(2, '0')
  const dateStr = `${year}-${month}-${String(_day).padStart(2, '0')}`
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
const selectDay = (_day: number) => {
  const currentDate = new Date()
  currentDate.setMonth(currentDate.getMonth() + currentMonthOffset.value)
  const year = currentDate.getFullYear()
  const month = String(currentDate.getMonth() + 1).padStart(2, '0')
  const dateStr = `${year}-${month}-${String(_day).padStart(2, '0')}`
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

// 连续打卡天数（模拟数据）
const streakDays = ref(5)

// 完成率
const completionRate = computed(() => {
  const totalDays = daysInMonth.value
  const checkedDays = checkInDays.value
  return Math.round((checkedDays / totalDays) * 100)
})

// 获取某天的运动时长（模拟数据）
const getExerciseMinutes = (date: string) => {
  // 模拟数据，实际应从后端获取
  const minutes = [30, 45, 60, 40, 50, 35, 55]
  const index = Math.abs(date.split('-').join('').split('').reduce((sum, char) => sum + char.charCodeAt(0), 0)) % minutes.length
  return minutes[index]
}

// 获取某天消耗的卡路里（模拟数据）
const getCaloriesBurned = (date: string) => {
  // 模拟数据，实际应从后端获取
  const calories = [200, 300, 400, 250, 350, 220, 380]
  const index = Math.abs(date.split('-').join('').split('').reduce((sum, char) => sum + char.charCodeAt(0), 0)) % calories.length
  return calories[index]
}

// 获取某天的步数（模拟数据）
const getSteps = (date: string) => {
  // 模拟数据，实际应从后端获取
  const steps = [5000, 7000, 8000, 6000, 7500, 5500, 8500]
  const index = Math.abs(date.split('-').join('').split('').reduce((sum, char) => sum + char.charCodeAt(0), 0)) % steps.length
  return steps[index]
}
</script>

<style scoped lang="scss">
.checkin-overview-page {
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

.calendar {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
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

.checkin-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
  
  .stat-card {
    background: white;
    border-radius: 16px;
    padding: 20px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    
    .stat-value {
      font-size: 24px;
      font-weight: bold;
      color: #91B3F0;
      margin-bottom: 8px;
    }
    
    .stat-label {
      font-size: 14px;
      color: #666;
    }
  }
}

.checkin-details {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  h3 {
    font-size: 18px;
    color: #333;
    margin: 0 0 16px 0;
    text-align: center;
  }
  
  .detail-item {
    display: flex;
    justify-content: space-between;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    span:first-child {
      font-size: 16px;
      color: #666;
    }
    
    span:last-child {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }
}
</style>