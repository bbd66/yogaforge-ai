<template>
  <div class="weight-management-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <button @click="goBack" class="back-btn">
        <img :src="backIcon" alt="返回" class="back-icon" />
      </button>
      <h1 class="page-title">体重管理</h1>
    </header>

    <!-- 体重统计 -->
    <div class="weight-stats">
      <div class="current-weight-card">
        <div class="weight-value">{{ currentWeight }}kg</div>
        <div class="weight-label">当前体重</div>
      </div>
    </div>

    <!-- 体重趋势图 -->
    <div class="weight-chart-section">
      <div class="section-header">
        <h2>体重趋势</h2>
        <div class="time-tabs">
          <button 
            :class="{ active: timeRange === 'month' }"
            @click="timeRange = 'month'"
          >月</button>
          <button 
            :class="{ active: timeRange === 'year' }"
            @click="timeRange = 'year'"
          >年</button>
        </div>
      </div>
      <!-- 月份导航按钮 -->
      <div v-if="timeRange === 'month'" class="month-nav-container">
        <button @click="prevMonth" class="nav-btn prev-btn">‹</button>
        <span class="current-month">{{ currentMonthLabel }}</span>
        <button @click="nextMonth" class="nav-btn next-btn">›</button>
      </div>
      <div class="chart-container">
        <div class="chart-wrapper">
          <!-- Y轴标签 -->
          <div class="y-axis-labels">
            <div class="y-label" v-for="label in yAxisLabels" :key="label">
              {{ label }}
            </div>
          </div>
          <!-- 图表区域 -->
          <div class="chart-area">
            <!-- 网格线 -->
            <div class="grid-lines">
              <div class="grid-line" v-for="i in 5" :key="i"></div>
            </div>
            <!-- 折线图 -->
            <svg class="weight-chart" :viewBox="`0 0 ${chartWidth} ${chartHeight}`">
              <!-- 折线 -->
              <polyline
                :points="polylinePoints"
                fill="none"
                stroke="#91B3F0"
                stroke-width="2"
              />
              <!-- 数据点 -->
              <circle
                v-for="(point, index) in chartPoints"
                :key="index"
                :cx="point.x"
                :cy="point.y"
                r="4"
                fill="#91B3F0"
                class="data-point"
              />
            </svg>
            <!-- X轴标签 -->
            <div class="x-axis-labels">
              <div 
                class="x-label" 
                v-for="(label, index) in xAxisLabels" 
                :key="index"
                :style="{ left: `${(index / (xAxisLabels.length - 1)) * chartWidth}px` }"
              >
                {{ label }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 体重记录列表 -->
    <div class="weight-records-section">
      <div class="section-header">
        <h2>体重记录</h2>
      </div>
      <div class="records-list">
        <div 
          v-for="(record, index) in weightRecords" 
          :key="index"
          class="record-item"
        >
          <div class="record-date">{{ record.date }}</div>
          <div class="record-weight">{{ record.weight }}kg</div>
        </div>
      </div>
    </div>

    <!-- 手动输入体重 -->
    <div class="input-section">
      <div class="section-header">
        <h2>手动记录体重</h2>
      </div>
      <div class="input-container">
        <input 
          v-model="newWeight"
          type="number"
          placeholder="输入当前体重(kg)"
          class="weight-input"
          step="0.1"
        />
        <button @click="addWeightRecord" class="add-btn">记录</button>
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

// 时间范围
const timeRange = ref<'month' | 'year'>('month')

// 当前显示的月份（用于月视图导航）
const currentMonthOffset = ref(0)

// 当前月份标签
const currentMonthLabel = computed(() => {
  const date = new Date()
  date.setMonth(date.getMonth() + currentMonthOffset.value)
  return `${date.getFullYear()}年${date.getMonth() + 1}月`
})

// 获取指定月份的数据
const getMonthData = (records: Array<{date: string, weight: number}>) => {
  const targetDate = new Date()
  targetDate.setMonth(targetDate.getMonth() + currentMonthOffset.value)
  const targetYear = targetDate.getFullYear()
  const targetMonth = targetDate.getMonth()
  
  return records.filter(record => {
    const recordDate = new Date(record.date)
    return recordDate.getFullYear() === targetYear && recordDate.getMonth() === targetMonth
  })
}

// 上一个月
const prevMonth = () => {
  currentMonthOffset.value--
}

// 下一个月
const nextMonth = () => {
  currentMonthOffset.value++
}

// 当前体重
const currentWeight = ref(65.5)

// 新体重输入
const newWeight = ref('')

// 体重记录数据
const weightRecords = ref([
  { date: '2025-10-01', weight: 68.2 },
  { date: '2025-10-03', weight: 67.8 },
  { date: '2025-10-05', weight: 67.5 },
  { date: '2025-10-07', weight: 67.0 },
  { date: '2025-10-09', weight: 66.8 },
  { date: '2025-10-11', weight: 66.5 },
  { date: '2025-10-13', weight: 66.2 },
  { date: '2025-10-15', weight: 65.8 },
  { date: '2025-10-17', weight: 65.5 }
])

// 图表尺寸
const chartWidth = 600
const chartHeight = 200

// 根据时间范围生成图表数据
const chartData = computed(() => {
  // 根据时间范围筛选数据
  const records = [...weightRecords.value].sort((a, b) => 
    new Date(a.date).getTime() - new Date(b.date).getTime()
  )
  
  switch (timeRange.value) {
    case 'month':
      // 获取当前月份的数据
      return getMonthData(records)
    case 'year':
      // 获取最近12个月的数据，按月聚合
      return aggregateDataByPeriod(records, 'month')
    default:
      return getMonthData(records)
  }
})

// Y轴标签
const yAxisLabels = computed(() => {
  const weights = chartData.value.map((r: { weight: number }) => r.weight)
  const min = Math.min(...weights)
  const max = Math.max(...weights)
  const range = max - min
  const step = range / 4
  
  return [
    max.toFixed(1),
    (max - step).toFixed(1),
    (max - step * 2).toFixed(1),
    (max - step * 3).toFixed(1),
    min.toFixed(1)
  ]
})

// X轴标签
const xAxisLabels = computed(() => {
  return chartData.value.map((r: { date: string }) => {
    // 根据时间范围显示不同的标签格式
    switch (timeRange.value) {
      case 'month':
        // 显示月日
        return formatDateLabel(r.date, 'month')
      case 'year':
        // 显示月份
        return formatDateLabel(r.date, 'year')
      default:
        return r.date.substring(5) // 默认显示月日
    }
  })
})

// 计算图表点坐标
const chartPoints = computed(() => {
  const weights = chartData.value.map((r: { weight: number }) => r.weight)
  const minWeight = Math.min(...weights)
  const maxWeight = Math.max(...weights)
  const weightRange = maxWeight - minWeight || 1 // 防止除零
  
  return chartData.value.map((record: { weight: number }, index: number) => {
    const x = (index / (chartData.value.length - 1)) * chartWidth
    const y = chartHeight - ((record.weight - minWeight) / weightRange) * chartHeight
    return { x, y }
  })
})

// 折线点字符串
const polylinePoints = computed(() => {
  return chartPoints.value.map((point: { x: number, y: number }) => `${point.x},${point.y}`).join(' ')
})

// 按周期聚合数据
const aggregateDataByPeriod = (records: Array<{date: string, weight: number}>, period: 'week' | 'month') => {
  if (records.length === 0) return []
  
  const result: Array<{date: string, weight: number}> = []
  const now = new Date()
  
  if (period === 'week') {
    // 按周聚合，获取最近4周的数据
    for (let i = 0; i < 4; i++) {
      const weekStart = new Date(now)
      weekStart.setDate(now.getDate() - (i + 1) * 7)
      const weekEnd = new Date(now)
      weekEnd.setDate(now.getDate() - i * 7)
      
      const weekRecords = records.filter(r => {
        const recordDate = new Date(r.date)
        return recordDate >= weekStart && recordDate < weekEnd
      })
      
      if (weekRecords.length > 0) {
        // 计算平均体重
        const avgWeight = weekRecords.reduce((sum, r) => sum + r.weight, 0) / weekRecords.length
        // 使用周的结束日期作为标签
        result.unshift({
          date: `${weekEnd.getFullYear()}-${String(weekEnd.getMonth() + 1).padStart(2, '0')}-${String(weekEnd.getDate()).padStart(2, '0')}`,
          weight: parseFloat(avgWeight.toFixed(1))
        })
      }
    }
  } else if (period === 'month') {
    // 按月聚合，获取最近12个月的数据
    for (let i = 0; i < 12; i++) {
      const monthStart = new Date(now.getFullYear(), now.getMonth() - i, 1)
      const monthEnd = new Date(now.getFullYear(), now.getMonth() - i + 1, 1)
      
      const monthRecords = records.filter(r => {
        const recordDate = new Date(r.date)
        return recordDate >= monthStart && recordDate < monthEnd
      })
      
      if (monthRecords.length > 0) {
        // 计算平均体重
        const avgWeight = monthRecords.reduce((sum, r) => sum + r.weight, 0) / monthRecords.length
        // 使用月份作为标签
        result.unshift({
          date: `${monthStart.getFullYear()}-${String(monthStart.getMonth() + 1).padStart(2, '0')}`,
          weight: parseFloat(avgWeight.toFixed(1))
        })
      }
    }
  }
  
  return result
}

// 格式化日期标签
const formatDateLabel = (dateStr: string, period: 'month' | 'year') => {
  const date = new Date(dateStr)
  
  switch (period) {
    case 'month':
      // 显示月日
      return `${date.getMonth() + 1}/${date.getDate()}`
    case 'year':
      // 显示月份
      return `${date.getMonth() + 1}月`
    default:
      return dateStr.substring(5)
  }
}

// 添加体重记录
const addWeightRecord = () => {
  if (!newWeight.value) {
    alert('请输入体重')
    return
  }
  
  const weight = parseFloat(newWeight.value)
  if (isNaN(weight) || weight <= 0) {
    alert('请输入有效的体重数值')
    return
  }
  
  // 获取当前日期
  const today = new Date().toISOString().split('T')[0]
  
  // 添加新记录
  weightRecords.value.unshift({
    date: today,
    weight: weight
  })
  
  // 更新当前体重
  currentWeight.value = weight
  
  // 清空输入框
  newWeight.value = ''
  
  console.log('添加体重记录:', { date: today, weight })
}
</script>

<style scoped lang="scss">
.weight-management-page {
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

.weight-stats {
  margin-bottom: 20px;
  
  .current-weight-card {
    background: white;
    border-radius: 16px;
    padding: 20px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    
    .weight-value {
      font-size: 32px;
      font-weight: bold;
      color: #91B3F0;
      margin-bottom: 8px;
    }
    
    .weight-label {
      font-size: 16px;
      color: #666;
    }
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
    margin: 0;
  }
  
  .time-tabs {
    display: flex;
    gap: 8px;
    
    button {
      padding: 6px 12px;
      border: 1px solid #e0e0e0;
      border-radius: 16px;
      background: white;
      color: #666;
      font-size: 12px;
      cursor: pointer;
      transition: all 0.3s;
      
      &.active {
        background: #91B3F0;
        color: white;
        border-color: #91B3F0;
      }
    }
  }
}

.weight-chart-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.month-nav-container {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 10px 0;
}

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
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.chart-container {
  height: 250px;
  overflow-x: auto;
  
  .chart-wrapper {
    display: flex;
    height: 100%;
    
    .y-axis-labels {
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      width: 40px;
      padding: 20px 8px 30px 0;
            
      .y-label {
        font-size: 10px;
        color: #999;
        text-align: right;
        display: flex;
        align-items: center;
        justify-content: flex-end;
              
        &::after {
          content: 'kg';
          margin-left: 2px;
        }
      }
    }
    
    .chart-area {
      flex: 1;
      position: relative;
      
      .grid-lines {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 30px;
        
        .grid-line {
          position: absolute;
          left: 0;
          right: 0;
          height: 1px;
          background: #f0f0f0;
          
          &:nth-child(1) { top: 0; }
          &:nth-child(2) { top: 25%; }
          &:nth-child(3) { top: 50%; }
          &:nth-child(4) { top: 75%; }
          &:nth-child(5) { bottom: 0; }
        }
      }
      
      .weight-chart {
        width: 100%;
        height: calc(100% - 30px);
      }
      
      .x-axis-labels {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 30px;
        
        .x-label {
          position: absolute;
          font-size: 10px;
          color: #999;
          transform: translateX(-50%);
        }
      }
    }
  }
}

.weight-records-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  .records-list {
    .record-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      .record-date {
        font-size: 14px;
        color: #666;
      }
      
      .record-weight {
        font-size: 16px;
        font-weight: bold;
        color: #333;
      }
    }
  }
}

.input-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  .input-container {
    display: flex;
    gap: 12px;
    
    .weight-input {
      flex: 1;
      padding: 12px 16px;
      border: 1px solid #e0e0e0;
      border-radius: 12px;
      font-size: 16px;
      
      &::placeholder {
        color: #ccc;
      }
      
      &:focus {
        outline: none;
        border-color: #91B3F0;
      }
    }
    
    .add-btn {
      padding: 12px 24px;
      background: #91B3F0;
      color: white;
      border: none;
      border-radius: 12px;
      font-size: 16px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.3s;
      
      &:active {
        transform: scale(0.98);
      }
    }
  }
}
</style>