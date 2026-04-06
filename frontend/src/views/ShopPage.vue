<template>
  <div class="shop-page" :style="{ backgroundImage: `url(${homeBackground})` }">
    <header class="shop-header">
      <button class="back-btn" @click="goBack">
        <img :src="backIcon" alt="返回" class="back-icon" />
      </button>
      <h1>宠物商城</h1>
      <div class="coins-display">
        <span class="coins-text">金币:</span>
        <span class="coins-amount">{{ coins }}</span>
        <!-- 新增：获取金币 +10 按钮 -->
        <button class="gain-coin-btn" @click.stop="gainGoldTen">
          获取金币 +10
        </button>
      </div>
    </header>
    
    <div class="shop-content">
      <div class="product-grid">
        <div 
          v-for="product in products" 
          :key="product.id" 
          class="product-card"
          @click="purchaseProduct(product)"
        >
          <div class="product-image">
            <img :src="product.image" :alt="product.name" />
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <p class="product-price">{{ product.price }} 金币</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'
import { usePetStore } from '@/stores/pet'
import fishIcon from '@/图标/果冻.png?url'
import juiceIcon from '@/图标/果汁.png?url'
import clothesIcon from '@/图标/衣服.png?url'
import feedIcon from '@/图标/胡萝卜.png?url'
import toyIcon from '@/图标/玩具熊.png?url'
import burgerIcon from '@/图标/汉堡.png?url'
import backIcon from '@/图标/返回.svg?url'
import homeBackground from '@/图标/首页背景.png?url'

interface Product {
  id: number
  name: string
  price: number
  image: string
}

const router = useRouter()
const coins = ref(0)
const petStore = usePetStore()

const products = ref<Product[]>([
  { id: 1, name: '果冻', price: 50, image: fishIcon },
  { id: 2, name: '胡萝卜', price: 50, image: feedIcon },
  { id: 3, name: '果汁', price: 50, image: juiceIcon },
  { id: 4, name: '衣服', price: 200, image: clothesIcon },
  { id: 5, name: '玩具熊', price: 150, image: toyIcon },
  { id: 6, name: '汉堡包', price: 80, image: burgerIcon }
])

const loadCoins = async () => {
  try {
    const res = await api.get<{
      id: number
      name: string
      level: number
      exp: number
      gold: number
    }>('/pets/me')
    coins.value = res.gold
  } catch (e) {
    console.error('获取金币失败', e)
  }
}

onMounted(() => {
  loadCoins()
})

const goBack = () => {
  router.push('/home')
}

const purchaseProduct = (product: Product) => {
  if (coins.value < product.price) {
    alert(`金币不足！你需要 ${product.price} 金币，但你只有 ${coins.value} 金币`)
    return
  }

  api
    .post<{
      level: number
      exp: number
      gold: number
      expRequired: number
      levelUps: number
      expGain: number
      goldGain: number
    }>('/pets/me/purchase', {
      productId: product.id,
      price: product.price
    })
    .then(() => {
      coins.value = coins.value - product.price
      // 购买食物类型的商品：增加一次喂食机会
      if (['果冻', '胡萝卜', '果汁', '汉堡包'].includes(product.name)) {
        petStore.addFeedCount(1)
      }
      alert(`购买成功！你购买了 ${product.name}，花费：${product.price} 金币`)      
    })
    .catch((err: any) => {
      console.error('购买失败', err)
      const msg = err?.response?.data
      if (msg === 'INSUFFICIENT_GOLD') {
        alert('金币不足，无法购买该商品')
      } else if (msg === 'INVALID_PRICE') {
        alert('商品价格异常，无法购买')
      } else {
        alert('购买失败，请稍后再试')
      }
    })
}

// 新增：获取金币 +10，调后端接口并更新本地 coins
const gainGoldTen = () => {
  api
    .post<{
      id: number
      name: string
      level: number
      exp: number
      gold: number
    }>('/pets/me/gain-gold', {
      amount: 10,
      reason: 'SHOP_TEST'
    })
    .then((res) => {
      coins.value = res.gold
      alert('已成功获取 10 金币！')
    })
    .catch((err: any) => {
      console.error('获取金币失败', err)
      alert('获取金币失败，请稍后再试')
    })
}
</script>

<style scoped lang="scss">
.shop-page {
  min-height: 100vh;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  padding: 20px;
  padding-top: 60px;
  padding-bottom: 80px;
  
  body.dark-theme & {
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
  }
}

.shop-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 30px;
  padding: 0 10px;
  
  .back-btn {
    background: none;
    border: none;
    cursor: pointer;
    padding: 5px;
    
    .back-icon {
      width: 24px;
      height: 24px;
    }
  }
  
  h1 {
    color: white;
    font-size: 28px;
    font-weight: bold;
    margin: 0;
    flex: 1;
    text-align: center;
  }
  
  .coins-display {
    display: flex;
    align-items: center;
    gap: 5px;
    background: rgba(255, 255, 255, 0.2);
    padding: 8px 12px;
    border-radius: 20px;
    
    .coins-text {
      color: white;
      font-size: 14px;
    }
    
    .coins-amount {
      color: #FFD700;
      font-weight: bold;
      font-size: 16px;
    }

    .gain-coin-btn {
      margin-left: 6px;
      padding: 2px 8px;
      font-size: 12px;
      border-radius: 999px;
      border: none;
      cursor: pointer;
      background: rgba(255, 255, 255, 0.3);
      color: #fff;
      white-space: nowrap;
    }
  }
}

.shop-content {
  .product-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }
  
  .product-card {
    background: rgba(255, 255, 255, 0.9);
    border-radius: 12px;
    padding: 15px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    cursor: pointer;
    transition: transform 0.3s;
    
    &:active {
      transform: scale(0.95);
    }
    
    .product-image {
      width: 100%;
      height: 100px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 10px;
      
      img {
        max-width: 80%;
        max-height: 80%;
        object-fit: contain;
      }
    }
    
    .product-info {
      text-align: center;
      
      .product-name {
        font-size: 16px;
        color: #333;
        margin: 0 0 5px 0;
      }
      
      .product-price {
        font-size: 14px;
        color: #F08713;
        margin: 0;
        font-weight: bold;
      }
    }
  }
}
</style>