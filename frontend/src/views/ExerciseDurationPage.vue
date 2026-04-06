<template>
  <div class="exercise-duration-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <button @click="goBack" class="back-btn">
        <img :src="backIcon" alt="返回" class="back-icon" />
      </button>
      <h1 class="page-title">运动时长</h1>
    </header>

    <!-- 时间范围选择 -->
    <div class="time-range-selector">
      <div class="time-tabs">
        <button 
          :class="{ active: timeRange === 'week' }"
          @click="timeRange = 'week'"
        >周</button>
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

    <!-- 运动时长统计 -->
    <div class="duration-stats">
      <div class="stat-card">
        <div class="stat-value">{{ totalDuration }}</div>
        <div class="stat-label">总时长(分钟)</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ averageDuration }}</div>
        <div class="stat-label">日均时长</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ maxDuration }}</div>
        <div class="stat-label">最长单日</div>
      </div>
    </div>

    <!-- 运动时长趋势图 -->
    <div class="chart-section">
      <div class="section-header">
        <h2>时长趋势</h2>
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
            <svg class="duration-chart" :viewBox="`0 0 ${chartWidth} ${chartHeight}`">
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

    <!-- 详细记录 -->
    <div class="records-section">
      <div class="section-header">
        <h2>详细记录</h2>
      </div>
      <div class="records-list">
        <div 
          v-for="(record, index) in chartData" 
          :key="index"
          class="record-item"
        >
          <div class="record-date">{{ record.date }}</div>
          <div class="record-duration">{{ record.duration }} 分钟</div>
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

const router = useRouter()

const goBack = () => {
  router.back()
}

// 时间范围
const timeRange = ref<'week' | 'month' | 'year'>('week')

// 模拟运动时长数据（实际项目中应从后端获取）
const durationData = ref([
  { date: '2025-01-01', duration: 45 },
  { date: '2025-01-02', duration: 30 },
  { date: '2025-01-03', duration: 60 },
  { date: '2025-01-04', duration: 20 },
  { date: '2025-01-05', duration: 75 },
  { date: '2025-01-06', duration: 55 },
  { date: '2025-01-07', duration: 40 },
  { date: '2025-01-08', duration: 50 },
  { date: '2025-01-09', duration: 35 },
  { date: '2025-01-10', duration: 65 },
  { date: '2025-01-11', duration: 25 },
  { date: '2025-01-12', duration: 80 },
  { date: '2025-01-13', duration: 60 },
  { date: '2025-01-14', duration: 45 },
  { date: '2025-01-15', duration: 55 }
])

// 根据时间范围筛选数据
const chartData = computed(() => {
  const records = [...durationData.value].sort((a, b) => 
    new Date(a.date).getTime() - new Date(b.date).getTime()
  )
  
  switch (timeRange.value) {
    case 'week':
      // 获取最近7天的数据
      return records.slice(-7)
    case 'month':
      // 获取最近30天的数据
      return records.slice(-30)
    case 'year':
      // 获取按月聚合的数据
      return aggregateDataByPeriod(records, 'month')
    default:
      return records.slice(-7)
  }
})

// 按周期聚合数据
const aggregateDataByPeriod = (records: Array<{date: string, duration: number}>, period: 'week' | 'month') => {
  if (records.length === 0) return []
  
  const result: Array<{date: string, duration: number}> = []
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
        // 计算平均时长
        const avgDuration = weekRecords.reduce((sum, r) => sum + r.duration, 0) / weekRecords.length
        // 使用周的结束日期作为标签
        result.unshift({
          date: `${weekEnd.getFullYear()}-${String(weekEnd.getMonth() + 1).padStart(2, '0')}-${String(weekEnd.getDate()).padStart(2, '0')}`,
          duration: parseFloat(avgDuration.toFixed(1))
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
        // 计算平均时长
        const avgDuration = monthRecords.reduce((sum, r) => sum + r.duration, 0) / monthRecords.length
        // 使用月份作为标签
        result.unshift({
          date: `${monthStart.getFullYear()}-${String(monthStart.getMonth() + 1).padStart(2, '0')}`,
          duration: parseFloat(avgDuration.toFixed(1))
        })
      }
    }
  }
  
  return result
}

// 格式化日期标签
const formatDateLabel = (dateStr: string, period: 'week' | 'month' | 'year') => {
  const date = new Date(dateStr)
  
  switch (period) {
    case 'week':
      // 显示星期几
      const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      return weekdays[date.getDay()]
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

// 总运动时长
const totalDuration = computed(() => {
  return chartData.value.reduce((sum, record) => sum + record.duration, 0)
})

// 日均运动时长
const averageDuration = computed(() => {
  if (chartData.value.length === 0) return 0
  return Math.round(totalDuration.value / chartData.value.length)
})

// 最长单日时长
const maxDuration = computed(() => {
  if (chartData.value.length === 0) return 0
  return Math.max(...chartData.value.map(r => r.duration))
})

// 图表尺寸
const chartWidth = 600
const chartHeight = 200

// Y轴标签
const yAxisLabels = computed(() => {
  const durations = chartData.value.map((r: { duration: number }) => r.duration)
  const min = Math.min(...durations)
  const max = Math.max(...durations)
  const range = max - min
  const step = range / 4
  
  return [
    max.toFixed(0),
    (max - step).toFixed(0),
    (max - step * 2).toFixed(0),
    (max - step * 3).toFixed(0),
    min.toFixed(0)
  ]
})

// X轴标签
const xAxisLabels = computed(() => {
  return chartData.value.map((r: { date: string }) => {
    // 根据时间范围显示不同的标签格式
    switch (timeRange.value) {
      case 'week':
        // 显示星期几
        return formatDateLabel(r.date, 'week')
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
  const durations = chartData.value.map((r: { duration: number }) => r.duration)
  const minDuration = Math.min(...durations)
  const maxDuration = Math.max(...durations)
  const durationRange = maxDuration - minDuration || 1 // 防止除零
  
  return chartData.value.map((record: { duration: number }, index: number) => {
    const x = (index / (chartData.value.length - 1)) * chartWidth
    const y = chartHeight - ((record.duration - minDuration) / durationRange) * chartHeight
    return { x, y }
  })
})

// 折线点字符串
const polylinePoints = computed(() => {
  return chartPoints.value.map((point: { x: number, y: number }) => `${point.x},${point.y}`).join(' ')
})
</script>

<style scoped lang="scss">
.exercise-duration-page {
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

.time-range-selector {
  margin-bottom: 20px;
  
  .time-tabs {
    display: flex;
    gap: 8px;
    
    button {
      flex: 1;
      padding: 10px;
      border: 1px solid #e0e0e0;
      border-radius: 16px;
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
}

.duration-stats {
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

.chart-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
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
      
      .duration-chart {
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

.records-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
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
      
      .record-duration {
        font-size: 16px;
        font-weight: bold;
        color: #333;
      }
    }
  }
}
</style>