<template>
  <section class="space-y-10">
    <!-- Hero -->
    <div class="relative overflow-hidden rounded-3xl p-10 md:p-14" style="background: linear-gradient(135deg, rgba(139, 92, 246, 0.08), rgba(99, 102, 241, 0.06), rgba(59, 130, 246, 0.04))">
      <div class="pointer-events-none absolute -top-10 -right-10 h-40 w-40 rounded-full bg-violet-300/20 blur-[60px]"></div>
      <div class="pointer-events-none absolute -bottom-8 -left-8 h-32 w-32 rounded-full bg-blue-300/15 blur-[50px]"></div>
      <div class="relative">
        <h1 class="text-4xl md:text-5xl font-bold tracking-tight">
          <span class="bg-gradient-to-r from-violet-600 via-indigo-500 to-blue-500 bg-clip-text text-transparent">MBTI 性格测试</span>
        </h1>
        <p class="mt-4 max-w-xl text-lg text-slate-600 leading-relaxed">
          通过科学的题目设计，探索你的性格偏好。<br />
          支持在线测试、历史记录追踪和题库管理。
        </p>
        <router-link to="/test" class="btn-primary mt-8 inline-flex gap-2 px-8 py-3.5 text-base animate-pulse-glow">
          开始测试
          <svg class="w-4 h-4 mt-0.5" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3" />
          </svg>
        </router-link>
      </div>
    </div>

    <!-- Feature cards -->
    <div class="grid gap-5 sm:grid-cols-2">
      <router-link
        v-for="(f, i) in visibleFeatures"
        :key="f.href"
        :to="f.href"
        class="card-hover group p-6 animate-slide-up"
        :style="{ animationDelay: `${i * 100}ms` }"
      >
        <div class="flex items-start gap-4">
          <div :class="`flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-xl bg-gradient-to-br ${f.gradient} text-white shadow-lg transition-transform duration-300 group-hover:scale-110 group-hover:rotate-3`">
            <span v-html="f.icon"></span>
          </div>
          <div>
            <p class="font-semibold text-slate-900">{{ f.title }}</p>
            <p class="mt-1 text-sm text-slate-500 leading-relaxed">{{ f.desc }}</p>
          </div>
        </div>
      </router-link>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const features = [
  {
    href: '/test',
    title: '开始测试',
    desc: '回答 24 道精选题目，发现你的 MBTI 性格类型',
    gradient: 'from-violet-500 to-purple-500',
    icon: '<svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12h3.75M9 15h3.75M9 18h3.75m3 .75H18a2.25 2.25 0 002.25-2.25V6.108c0-1.135-.845-2.098-1.976-2.192a48.424 48.424 0 00-1.123-.08m-5.801 0c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 00.75-.75 2.25 2.25 0 00-.1-.664m-5.8 0A2.251 2.251 0 0113.5 2.25H15a2.25 2.25 0 012.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25m0 0H4.875c-.621 0-1.125.504-1.125 1.125v11.25c0 .621.504 1.125 1.125 1.125h9.75c.621 0 1.125-.504 1.125-1.125V9.375c0-.621-.504-1.125-1.125-1.125H8.25z" /></svg>'
  },
  {
    href: '/history',
    title: '测试记录',
    desc: '查看历次测试结果，追踪性格变化轨迹',
    gradient: 'from-blue-500 to-cyan-500',
    icon: '<svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>'
  },
  {
    href: '/admin/questions',
    title: '题库管理',
    desc: '新增、编辑、删除题目（管理员）',
    requiresAdmin: true,
    gradient: 'from-pink-500 to-rose-500',
    icon: '<svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25" /></svg>'
  },
  {
    href: '/admin/stats',
    title: '数据统计',
    desc: '查看测试人数、类型分布等统计（管理员）',
    requiresAdmin: true,
    gradient: 'from-emerald-500 to-teal-500',
    icon: '<svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V8.625zM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V4.125z" /></svg>'
  },
]

const visibleFeatures = computed(() => features.filter(feature => {
  if (feature.requiresAdmin) return authStore.isAdmin
  return true
}))
</script>
