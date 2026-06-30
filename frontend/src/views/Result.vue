<template>
  <section v-if="loading" class="flex flex-col items-center justify-center py-24">
    <div class="h-8 w-8 animate-spin rounded-full border-3 border-violet-200 border-t-violet-600"></div>
    <p class="mt-4 text-sm text-slate-500">正在加载结果...</p>
  </section>

  <section v-else-if="result" class="space-y-8">
    <h1 class="section-title">你的 MBTI 结果</h1>

    <!-- Type card -->
    <div class="card overflow-hidden">
      <div class="relative border-b border-white/30 px-8 py-10" style="background: linear-gradient(135deg, rgba(139, 92, 246, 0.06), rgba(99, 102, 241, 0.04), rgba(59, 130, 246, 0.03))">
        <div class="pointer-events-none absolute -top-8 -right-8 h-32 w-32 rounded-full bg-violet-300/15 blur-[50px]"></div>
        <p class="text-sm text-slate-500">你的性格类型</p>
        <p class="mt-3 text-6xl font-bold tracking-widest bg-gradient-to-r from-violet-600 via-indigo-500 to-blue-500 bg-clip-text text-transparent">{{ result.mbtiType }}</p>
        <p class="mt-4 max-w-md text-base text-slate-600 leading-relaxed">{{ result.description }}</p>
      </div>
      <div class="px-8 py-4 text-sm text-slate-400">
        测试时间：{{ formatDate(result.createdAt) }}
      </div>
    </div>

    <!-- Dimension analysis -->
    <div class="card p-8">
      <h2 class="mb-6 text-lg font-semibold text-slate-900">详细维度分析</h2>
      <div class="space-y-8">
        <div v-for="dim in dimMeta" :key="dim.a + dim.b">
          <div class="mb-3 flex items-center justify-between">
            <span class="text-sm font-medium text-slate-700">{{ dim.label }}</span>
            <span class="text-xs font-semibold px-2 py-0.5 rounded-full bg-gradient-to-r from-violet-50 to-indigo-50 text-violet-600">{{ getDominant(dim) }} 占优</span>
          </div>
          <div class="flex items-center gap-3">
            <span class="w-4 text-right text-xs font-bold text-slate-900">{{ dim.a }}</span>
            <div class="flex flex-1 h-3 overflow-hidden rounded-full bg-white/50">
              <div
                class="h-full rounded-l-full transition-all duration-1000"
                :class="dim.gradient"
                :style="{ width: `${getPct(dim)}%` }"
              ></div>
            </div>
            <span class="w-4 text-left text-xs font-bold text-slate-400">{{ dim.b }}</span>
          </div>
          <div class="mt-1.5 flex justify-between text-xs text-slate-400 tabular-nums">
            <span>{{ Math.round(getPct(dim)) }}%</span>
            <span>{{ Math.round(100 - getPct(dim)) }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Actions -->
    <div class="flex gap-3">
      <router-link to="/test" class="btn-primary">再测一次</router-link>
      <router-link to="/history" class="btn-secondary">查看历史</router-link>
    </div>
  </section>

  <section v-else class="flex flex-col items-center justify-center py-24">
    <p class="text-sm text-slate-500">结果不存在或加载失败</p>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'

const route = useRoute()

const loading = ref(true)
const result = ref(null)

const dimMeta = [
  { label: '外向 vs 内向', a: 'E', b: 'I', gradient: 'bg-gradient-to-r from-violet-500 to-purple-500' },
  { label: '感觉 vs 直觉', a: 'S', b: 'N', gradient: 'bg-gradient-to-r from-blue-500 to-indigo-500' },
  { label: '思考 vs 情感', a: 'T', b: 'F', gradient: 'bg-gradient-to-r from-pink-500 to-rose-500' },
  { label: '判断 vs 知觉', a: 'J', b: 'P', gradient: 'bg-gradient-to-r from-emerald-500 to-teal-500' }
]

function getScores() {
  if (!result.value?.scoresJson) return {}
  try {
    return typeof result.value.scoresJson === 'string'
      ? JSON.parse(result.value.scoresJson)
      : result.value.scoresJson
  } catch { return {} }
}

function getPct(dim) {
  const scores = getScores()
  const va = scores[dim.a] || 0
  const vb = scores[dim.b] || 0
  const total = va + vb
  return total > 0 ? (va / total) * 100 : 50
}

function getDominant(dim) {
  const scores = getScores()
  const va = scores[dim.a] || 0
  const vb = scores[dim.b] || 0
  return va >= vb ? dim.a : dim.b
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(async () => {
  try {
    const id = route.params.id
    const res = await api.get(`/api/result/${id}`)
    result.value = res.data
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
})
</script>
