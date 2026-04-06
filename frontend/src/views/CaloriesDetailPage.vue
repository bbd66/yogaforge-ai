<template>
  <div class="calories-detail-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <button @click="goBack" class="back-btn">
        <img :src="backIcon" alt="返回" class="back-icon" />
      </button>
      <h1 class="page-title">消耗卡路里</h1>
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

    <!-- 卡路里统计 -->
    <div class="calories-stats">
      <div class="stat-card">
        <div class="stat-value">{{ totalCalories }}</div>
        <div class="stat-label">总消耗</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ averageCalories }}</div>
        <div class="stat-label">日均消耗</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ maxCalories }}</div>
        <div class="stat-label">最高单日</div>
      </div>
    </div>

    <!-- 卡路里趋势图 -->
    <div class="chart-section">
      <div class="section-header">
        <h2>消耗趋势</h2>
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
            <svg class="calories-chart" :viewBox="`0 0 ${chartWidth} ${chartHeight}`">
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
          <div class="record-calories">{{ record.calories }} kcal</div>
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

// 模拟卡路里数据（实际项目中应从后端获取）
const caloriesData = ref([
  { date: '2025-01-01', calories: 320 },
  { date: '2025-01-02', calories: 280 },
  { date: '2025-01-03', calories: 350 },
  { date: '2025-01-04', calories: 200 },
  { date: '2025-01-05', calories: 400 },
  { date: '2025-01-06', calories: 380 },
  { date: '2025-01-07', calories: 300 },
  { date: '2025-01-08', calories: 330 },
  { date: '2025-01-09', calories: 290 },
  { date: '2025-01-10', calories: 360 },
  { date: '2025-01-11', calories: 220 },
  { date: '2025-01-12', calories: 410 },
  { date: '2025-01-13', calories: 390 },
  { date: '2025-01-14', calories: 310 },
  { date: '2025-01-15', calories: 340 }
])

// 根据时间范围筛选数据
const chartData = computed(() => {
  const records = [...caloriesData.value].sort((a, b) => 
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
const aggregateDataByPeriod = (records: Array<{date: string, calories: number}>, period: 'week' | 'month') => {
  if (records.length === 0) return []
  
  const result: Array<{date: string, calories: number}> = []
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
        // 计算平均卡路里
        const avgCalories = weekRecords.reduce((sum, r) => sum + r.calories, 0) / weekRecords.length
        // 使用周的结束日期作为标签
        result.unshift({
          date: `${weekEnd.getFullYear()}-${String(weekEnd.getMonth() + 1).padStart(2, '0')}-${String(weekEnd.getDate()).padStart(2, '0')}`,
          calories: parseFloat(avgCalories.toFixed(1))
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
        // 计算平均卡路里
        const avgCalories = monthRecords.reduce((sum, r) => sum + r.calories, 0) / monthRecords.length
        // 使用月份作为标签
        result.unshift({
          date: `${monthStart.getFullYear()}-${String(monthStart.getMonth() + 1).padStart(2, '0')}`,
          calories: parseFloat(avgCalories.toFixed(1))
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

// 总消耗卡路里
const totalCalories = computed(() => {
  return chartData.value.reduce((sum, record) => sum + record.calories, 0)
})

// 日均消耗卡路里
const averageCalories = computed(() => {
  if (chartData.value.length === 0) return 0
  return Math.round(totalCalories.value / chartData.value.length)
})

// 最高单日消耗
const maxCalories = computed(() => {
  if (chartData.value.length === 0) return 0
  return Math.max(...chartData.value.map(r => r.calories))
})

// 图表尺寸
const chartWidth = 600
const chartHeight = 200

// Y轴标签
const yAxisLabels = computed(() => {
  const calories = chartData.value.map((r: { calories: number }) => r.calories)
  const min = Math.min(...calories)
  const max = Math.max(...calories)
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
  const calories = chartData.value.map((r: { calories: number }) => r.calories)
  const minCalories = Math.min(...calories)
  const maxCalories = Math.max(...calories)
  const caloriesRange = maxCalories - minCalories || 1 // 防止除零
  
  return chartData.value.map((record: { calories: number }, index: number) => {
    const x = (index / (chartData.value.length - 1)) * chartWidth
    const y = chartHeight - ((record.calories - minCalories) / caloriesRange) * chartHeight
    return { x, y }
  })
})

// 折线点字符串
const polylinePoints = computed(() => {
  return chartPoints.value.map((point: { x: number, y: number }) => `${point.x},${point.y}`).join(' ')
})
</script>

<style scoped lang="scss">
.calories-detail-page {
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

.calories-stats {
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
        display: flex;
        align-items: center;
        justify-content: flex-end;
        
        &::after {
          content: 'kcal';
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
      
      .calories-chart {
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
      
      .record-calories {
        font-size: 16px;
        font-weight: bold;
        color: #333;
      }
    }
  }
}
</style>