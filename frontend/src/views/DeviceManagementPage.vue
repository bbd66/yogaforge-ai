<template>
  <div class="device-management-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <button @click="goBack" class="back-btn">
        <img :src="backIcon" alt="返回" class="back-icon" />
      </button>
      <h1 class="page-title">设备管理</h1>
    </header>

    <!-- 设备统计 -->
    <div class="device-stats">
      <div class="stats-card">
        <div class="stats-value">{{ devices.length }}</div>
        <div class="stats-label">设备总数</div>
      </div>
      <div class="stats-card">
        <div class="stats-value">{{ activeDevicesCount }}</div>
        <div class="stats-label">活跃设备</div>
      </div>
    </div>

    <!-- 设备列表 -->
    <div class="devices-list">
      <div 
        v-for="device in devices" 
        :key="device.id"
        class="device-item"
        :class="{ 'in-warranty': device.inWarranty }"
      >
        <div class="device-header">
          <div class="device-icon">
            <img :src="getDeviceIcon(device.type)" :alt="device.name" />
          </div>
          <div class="device-info">
            <h3 class="device-name">{{ device.name }}</h3>
            <p class="device-type">{{ device.type }}</p>
          </div>
          <div class="device-status" :class="{ 'active': device.active }">
            {{ device.active ? '使用中' : '未激活' }}
          </div>
        </div>
        
        <div class="device-details">
          <div class="detail-row">
            <span class="detail-label">激活日期</span>
            <span class="detail-value">{{ device.activationDate }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">使用时长</span>
            <span class="detail-value">{{ device.usageDays }}天</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">保修状态</span>
            <span class="detail-value" :class="{ 'warranty-active': device.inWarranty }">
              {{ device.inWarranty ? '保修期内' : '保修已过期' }}
            </span>
          </div>
          <div v-if="device.warrantyEndDate" class="detail-row">
            <span class="detail-label">保修结束</span>
            <span class="detail-value">{{ device.warrantyEndDate }}</span>
          </div>
        </div>
        
        <div class="device-actions">
          <button 
            v-if="device.active" 
            @click="deactivateDevice(device.id)"
            class="action-btn deactivate"
          >
            停用设备
          </button>
          <button 
            v-else 
            @click="activateDevice(device.id)"
            class="action-btn activate"
          >
            激活设备
          </button>
          <button 
            @click="extendWarranty(device.id)"
            class="action-btn extend"
            :disabled="!device.inWarranty"
          >
            延长保修
          </button>
        </div>
      </div>
    </div>

    <!-- 添加设备按钮 -->
    <div class="add-device-section">
      <button @click="showAddDeviceModal" class="add-device-btn">
        + 添加新设备
      </button>
    </div>

    <!-- 添加设备模态框 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <h2>添加新设备</h2>
        <form @submit.prevent="addDevice">
          <div class="form-group">
            <label>设备名称</label>
            <input 
              v-model="newDevice.name" 
              type="text" 
              placeholder="请输入设备名称"
              required
            />
          </div>
          <div class="form-group">
            <label>设备类型</label>
            <select v-model="newDevice.type" required>
              <option value="智能手环">智能手环</option>
              <option value="智能手表">智能手表</option>
              <option value="体重秤">体重秤</option>
              <option value="跑步机">跑步机</option>
              <option value="动感单车">动感单车</option>
            </select>
          </div>
          <div class="form-group">
            <label>激活日期</label>
            <input 
              v-model="newDevice.activationDate" 
              type="date" 
              required
            />
          </div>
          <div class="form-group">
            <label>保修月数</label>
            <input 
              v-model.number="newDevice.warrantyMonths" 
              type="number" 
              min="1"
              max="60"
              placeholder="请输入保修月数"
              required
            />
          </div>
          <div class="modal-actions">
            <button type="button" @click="closeModal" class="cancel-btn">取消</button>
            <button type="submit" class="submit-btn">添加设备</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

// 导入图标
import backIcon from '@/图标/返回.svg'
import deviceIcon from '@/图标/设备使用数据.svg'
import weightIcon from '@/图标/体重.svg'
import yogaMatIcon from '@/图标/瑜伽垫.png'
import treadmillIcon from '@/图标/跑步机.png'
import jumpRopeIcon from '@/图标/跳绳.png'

const router = useRouter()

const goBack = () => {
  router.back()
}

// 设备数据
const devices = ref([
  {
    id: '1',
    name: '智能跳绳',
    type: '跳绳',
    activationDate: '2025-01-01',
    usageDays: 45,
    warrantyMonths: 12,
    warrantyEndDate: '2025-12-31',
    inWarranty: true,
    active: true
  },
  {
    id: '2',
    name: '动作视觉识别瑜伽垫',
    type: '智能瑜伽垫',
    activationDate: '2024-11-15',
    usageDays: 105,
    warrantyMonths: 12,
    warrantyEndDate: '2025-11-14',
    inWarranty: true,
    active: true
  },
  {
    id: '3',
    name: '智慧体重秤',
    type: '体重秤',
    activationDate: '2024-06-20',
    usageDays: 265,
    warrantyMonths: 12,
    warrantyEndDate: '2025-06-19',
    inWarranty: false,
    active: false
  },
  {
    id: '4',
    name: '跑步机 Pro',
    type: '跑步机',
    activationDate: '2024-03-10',
    usageDays: 365,
    warrantyMonths: 24,
    warrantyEndDate: '2026-03-09',
    inWarranty: true,
    active: true
  }
])

// 计算活跃设备数量
const activeDevicesCount = computed(() => {
  return devices.value.filter(device => device.active).length
})

// 获取设备图标
const getDeviceIcon = (type: string) => {
  switch (type) {
    case '体重秤':
    case '智慧体重秤':
      return weightIcon
    case '智能瑜伽垫':
    case '瑜伽垫':
      return yogaMatIcon
    case '跑步机':
    case '跑步机 Pro':
      return treadmillIcon
    case '跳绳':
    case '智能跳绳':
      return jumpRopeIcon
    default:
      return deviceIcon
  }
}

// 激活设备
const activateDevice = (deviceId: string) => {
  const device = devices.value.find(d => d.id === deviceId)
  if (device) {
    device.active = true
    console.log('激活设备:', deviceId)
  }
}

// 停用设备
const deactivateDevice = (deviceId: string) => {
  const device = devices.value.find(d => d.id === deviceId)
  if (device) {
    device.active = false
    console.log('停用设备:', deviceId)
  }
}

// 延长保修
const extendWarranty = (deviceId: string) => {
  const device = devices.value.find(d => d.id === deviceId)
  if (device && device.inWarranty) {
    // 延长一年保修
    device.warrantyMonths += 12
    // 更新保修结束日期
    const endDate = new Date(device.warrantyEndDate || device.activationDate)
    endDate.setFullYear(endDate.getFullYear() + 1)
    device.warrantyEndDate = endDate.toISOString().split('T')[0]
    console.log('延长保修:', deviceId)
  }
}

// 模态框相关
const showModal = ref(false)
const newDevice = ref({
  name: '',
  type: '智能手环',
  activationDate: '',
  warrantyMonths: 12
})

const showAddDeviceModal = () => {
  // 设置默认激活日期为今天
  newDevice.value.activationDate = new Date().toISOString().split('T')[0]
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  // 重置表单
  newDevice.value = {
    name: '',
    type: '智能手环',
    activationDate: '',
    warrantyMonths: 12
  }
}

// 添加设备
const addDevice = () => {
  if (!newDevice.value.name || !newDevice.value.activationDate) {
    alert('请填写完整信息')
    return
  }

  // 计算使用天数
  const activationDate = new Date(newDevice.value.activationDate)
  const today = new Date()
  const diffTime = Math.abs(today.getTime() - activationDate.getTime())
  const usageDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

  // 计算保修结束日期
  const warrantyEndDate = new Date(activationDate)
  warrantyEndDate.setMonth(warrantyEndDate.getMonth() + newDevice.value.warrantyMonths)

  // 添加新设备
  const device = {
    id: String(devices.value.length + 1),
    name: newDevice.value.name,
    type: newDevice.value.type,
    activationDate: newDevice.value.activationDate,
    usageDays: usageDays,
    warrantyMonths: newDevice.value.warrantyMonths,
    warrantyEndDate: warrantyEndDate.toISOString().split('T')[0],
    inWarranty: warrantyEndDate > today,
    active: true
  }

  devices.value.push(device)
  console.log('添加新设备:', device)

  // 关闭模态框
  closeModal()
}
</script>

<style scoped lang="scss">
.device-management-page {
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

.device-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
  
  .stats-card {
    background: white;
    border-radius: 16px;
    padding: 20px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    
    .stats-value {
      font-size: 24px;
      font-weight: bold;
      color: #91B3F0;
      margin-bottom: 8px;
    }
    
    .stats-label {
      font-size: 14px;
      color: #666;
    }
  }
}

.devices-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.device-item {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  &.in-warranty {
    border-left: 4px solid #91B3F0;
  }
  
  .device-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 16px;
    
    .device-icon {
      width: 50px;
      height: 50px;
      border-radius: 50%;
      background: #f0f0f0;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      
      img {
        width: 30px;
        height: 30px;
        object-fit: contain;
      }
    }
    
    .device-info {
      flex: 1;
      
      .device-name {
        font-size: 18px;
        font-weight: bold;
        color: #333;
        margin: 0 0 4px 0;
      }
      
      .device-type {
        font-size: 14px;
        color: #999;
        margin: 0;
      }
    }
    
    .device-status {
      font-size: 12px;
      padding: 4px 8px;
      border-radius: 12px;
      background: #f0f0f0;
      color: #999;
      
      &.active {
        background: #E996EF;
        color: white;
      }
    }
  }
  
  .device-details {
    margin-bottom: 16px;
    
    .detail-row {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      .detail-label {
        font-size: 14px;
        color: #666;
      }
      
      .detail-value {
        font-size: 14px;
        color: #333;
        font-weight: 500;
        
        &.warranty-active {
          color: #91B3F0;
        }
      }
    }
  }
  
  .device-actions {
    display: flex;
    gap: 12px;
    
    .action-btn {
      flex: 1;
      padding: 10px;
      border: none;
      border-radius: 12px;
      font-size: 14px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.3s;
      
      &.deactivate {
        background: #FF6B6B;
        color: white;
      }
      
      &.activate {
        background: #91B3F0;
        color: white;
      }
      
      &.extend {
        background: #F08713;
        color: white;
        
        &:disabled {
          background: #f0f0f0;
          color: #ccc;
          cursor: not-allowed;
        }
      }
      
      &:active {
        transform: scale(0.98);
      }
    }
  }
}

.add-device-section {
  text-align: center;
  
  .add-device-btn {
    width: 100%;
    padding: 16px;
    background: #91B3F0;
    color: white;
    border: none;
    border-radius: 16px;
    font-size: 16px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s;
    
    &:active {
      transform: scale(0.98);
    }
  }
}

.modal-overlay {
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
}

.modal-content {
  background: white;
  border-radius: 16px;
  padding: 24px;
  width: 90%;
  max-width: 400px;
  max-height: 80vh;
  overflow-y: auto;
  
  h2 {
    font-size: 20px;
    color: #333;
    margin: 0 0 20px 0;
    text-align: center;
  }
  
  .form-group {
    margin-bottom: 16px;
    
    label {
      display: block;
      font-size: 14px;
      color: #666;
      margin-bottom: 8px;
    }
    
    input, select {
      width: 100%;
      padding: 12px;
      border: 1px solid #e0e0e0;
      border-radius: 12px;
      font-size: 16px;
      
      &:focus {
        outline: none;
        border-color: #91B3F0;
      }
    }
  }
  
  .modal-actions {
    display: flex;
    gap: 12px;
    margin-top: 20px;
    
    .cancel-btn, .submit-btn {
      flex: 1;
      padding: 12px;
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
    
    .cancel-btn {
      background: #f0f0f0;
      color: #666;
    }
    
    .submit-btn {
      background: #91B3F0;
      color: white;
    }
  }
}
</style>