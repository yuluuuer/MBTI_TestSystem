<template>
  <aside class="h-fit lg:-ml-2">
    <!-- Brand -->
    <router-link to="/" class="mb-8 block">
      <p class="text-xl font-bold tracking-tight bg-gradient-to-r from-violet-600 via-indigo-500 to-blue-500 bg-clip-text text-transparent">MBTI</p>
      <p class="text-sm text-slate-400">性格测试系统</p>
    </router-link>

    <!-- Navigation -->
    <nav class="space-y-1">
      <router-link
        v-for="item in visibleNavItems"
        :key="item.href"
        :to="item.href"
        :class="[
          'flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm transition-all duration-300',
          isActive(item.href)
            ? 'bg-gradient-to-r from-violet-500/10 via-indigo-500/10 to-blue-500/10 font-semibold text-slate-900 shadow-sm border border-violet-200/40'
            : 'text-slate-500 hover:bg-white/50 hover:text-slate-900'
        ]"
      >
        <span :class="isActive(item.href) ? 'text-violet-600' : 'text-slate-400'" v-html="item.icon"></span>
        {{ item.label }}
      </router-link>
    </nav>

    <!-- User section -->
    <div class="mt-10 border-t border-white/40 pt-6">
      <p v-if="authStore.loading" class="text-xs text-slate-400">加载中...</p>
      <router-link v-if="!authStore.loading && !authStore.isLoggedIn" class="btn-primary w-full text-center" to="/login">
        登录 / 注册
      </router-link>
      <div v-if="authStore.isLoggedIn" class="space-y-4">
        <div class="flex items-center gap-3">
          <div class="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br from-violet-500 to-indigo-500 text-xs font-bold text-white shadow-md">
            {{ (authStore.user?.name || authStore.user?.email || '?').charAt(0).toUpperCase() }}
          </div>
          <div class="min-w-0 flex-1">
            <p class="truncate text-sm font-medium text-slate-900">{{ authStore.user?.name || authStore.user?.email }}</p>
            <span v-if="authStore.isAdmin" class="badge-primary mt-0.5">管理员</span>
          </div>
        </div>
        <button
          type="button"
          @click="handleLogout"
          class="w-full rounded-xl border border-white/40 px-3 py-2 text-xs text-slate-500 transition-all duration-300 hover:bg-white/60 hover:text-slate-700 hover:border-violet-200/40"
        >
          退出登录
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const navItems = [
  {
    href: '/test',
    label: '开始测试',
    requiresAuth: true,
    icon: '<svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12h3.75M9 15h3.75M9 18h3.75m3 .75H18a2.25 2.25 0 002.25-2.25V6.108c0-1.135-.845-2.098-1.976-2.192a48.424 48.424 0 00-1.123-.08m-5.801 0c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 00.75-.75 2.25 2.25 0 00-.1-.664m-5.8 0A2.251 2.251 0 0113.5 2.25H15a2.25 2.25 0 012.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25m0 0H4.875c-.621 0-1.125.504-1.125 1.125v11.25c0 .621.504 1.125 1.125 1.125h9.75c.621 0 1.125-.504 1.125-1.125V9.375c0-.621-.504-1.125-1.125-1.125H8.25z" /></svg>'
  },
  {
    href: '/admin/questions',
    label: '题库管理',
    requiresAdmin: true,
    icon: '<svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25" /></svg>'
  },
  {
    href: '/admin/users',
    label: '用户管理',
    requiresAdmin: true,
    icon: '<svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z" /></svg>'
  },
  {
    href: '/admin/exam-types',
    label: '测试管理',
    requiresAdmin: true,
    icon: '<svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.75 12h16.5m-16.5 3.75h16.5M3.75 19.5h16.5M5.625 4.5h12.75a1.875 1.875 0 010 3.75H5.625a1.875 1.875 0 010-3.75z" /></svg>'
  },
  {
    href: '/admin/dimensions',
    label: '维度管理',
    requiresAdmin: true,
    icon: '<svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09zM18.259 8.715L18 9.75l-.259-1.035a3.375 3.375 0 00-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 002.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 002.455 2.456L21.75 6l-1.036.259a3.375 3.375 0 00-2.455 2.456zM16.894 20.567L16.5 21.75l-.394-1.183a2.25 2.25 0 00-1.423-1.423L13.5 18.75l1.183-.394a2.25 2.25 0 001.423-1.423l.394-1.183.394 1.183a2.25 2.25 0 001.423 1.423l1.183.394-1.183.394a2.25 2.25 0 00-1.423 1.423z" /></svg>'
  },
  {
    href: '/admin/stats',
    label: '数据统计',
    requiresAdmin: true,
    icon: '<svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V8.625zM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V4.125z" /></svg>'
  },
  {
    href: '/history',
    label: '测试记录',
    requiresAuth: true,
    icon: '<svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>'
  },
  {
    href: '/account',
    label: '个人账户',
    requiresAuth: true,
    icon: '<svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" /></svg>'
  },
]

const visibleNavItems = computed(() => navItems.filter(item => {
  if (item.requiresAdmin) return authStore.isAdmin
  if (item.requiresAuth) return authStore.isLoggedIn
  return true
}))

function isActive(href) {
  return route.path === href || route.path.startsWith(href + '/')
}

async function handleLogout() {
  await authStore.logout()
  router.push('/')
}
</script>
