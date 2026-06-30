import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/Home.vue') },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/test', name: 'Test', component: () => import('../views/Test.vue'), meta: { requiresAuth: true } },
  { path: '/result/:id', name: 'Result', component: () => import('../views/Result.vue'), meta: { requiresAuth: true } },
  { path: '/history', name: 'History', component: () => import('../views/History.vue'), meta: { requiresAuth: true } },
  { path: '/account', name: 'Account', component: () => import('../views/Account.vue'), meta: { requiresAuth: true } },
  { path: '/admin/questions', name: 'AdminQuestions', component: () => import('../views/admin/Questions.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/exam-types', name: 'AdminExamTypes', component: () => import('../views/admin/ExamTypes.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/dimensions', name: 'AdminDimensions', component: () => import('../views/admin/Dimensions.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/users', name: 'AdminUsers', component: () => import('../views/admin/Users.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/stats', name: 'AdminStats', component: () => import('../views/admin/Stats.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Navigation guard
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // Fetch user info if not loaded yet
  if (!authStore.user && !authStore.loading) {
    await authStore.fetchMe()
  }

  if (to.path === '/login' && authStore.isLoggedIn) {
    next({ path: '/' })
    return
  }

  // Check authentication. All app pages except login require a signed-in user.
  if (to.path !== '/login' && !authStore.isLoggedIn) {
    next({ path: '/login', query: { from: to.fullPath } })
    return
  }

  // Check admin access
  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    next({ path: '/' })
    return
  }

  next()
})

export default router
