import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/AuthPage.vue')
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/HomePage.vue')
  },
  {
    path: '/data',
    name: 'Data',
    component: () => import('@/views/DataPage.vue')
  },
  {
    path: '/workout',
    name: 'Workout',
    component: () => import('@/views/WorkoutPage.vue')
  },
  {
    path: '/community',
    name: 'Community',
    component: () => import('@/views/CommunityPage.vue')
  },
  {
    path: '/shop',
    name: 'Shop',
    component: () => import('@/views/ShopPage.vue')
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/ProfilePage.vue')
  },
  {
    path: '/account-settings',
    name: 'AccountSettings',
    component: () => import('@/views/AccountSettings.vue')
  },
  {
    path: '/achievements',
    name: 'Achievements',
    component: () => import('@/views/AchievementsPage.vue')
  },
  {
    path: '/weight-management',
    name: 'WeightManagement',
    component: () => import('@/views/WeightManagementPage.vue')
  },
  {
    path: '/device-management',
    name: 'DeviceManagement',
    component: () => import('@/views/DeviceManagementPage.vue')
  },
  {
    path: '/check-in-overview',
    name: 'CheckInOverview',
    component: () => import('@/views/CheckInOverviewPage.vue')
  },
  {
    path: '/calories-detail',
    name: 'CaloriesDetail',
    component: () => import('@/views/CaloriesDetailPage.vue')
  },
  {
    path: '/exercise-duration',
    name: 'ExerciseDuration',
    component: () => import('@/views/ExerciseDurationPage.vue')
  },
  {
    path: '/theme-settings',
    name: 'ThemeSettings',
    component: () => import('@/views/ThemeSettings.vue')
  },
  {
    path: '/language-settings',
    name: 'LanguageSettings',
    component: () => import('@/views/LanguageSettings.vue')
  },
  {
    path: '/help-feedback',
    name: 'HelpFeedback',
    component: () => import('@/views/HelpFeedback.vue')
  },
  {
    path: '/privacy-policy',
    name: 'PrivacyPolicy',
    component: () => import('@/views/PrivacyPolicy.vue')
  },
  {
    path: '/about-us',
    name: 'AboutUs',
    component: () => import('@/views/AboutUs.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
