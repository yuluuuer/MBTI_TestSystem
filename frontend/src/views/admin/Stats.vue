<template>
  <section class="space-y-8">
    <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
      <div>
        <h1 class="section-title">数据统计</h1>
        <p class="mt-1 text-sm text-slate-400">按测试查看作答情况和 MBTI 分布</p>
      </div>
      <div class="w-full lg:w-80">
        <label class="mb-1.5 block text-sm font-medium text-slate-700">选择测试</label>
        <select class="input" v-model="selectedExamTypeId" @change="loadStats">
          <option v-for="exam in stats?.examTypes || []" :key="exam.id" :value="exam.id">{{ exam.name }}</option>
        </select>
      </div>
    </div>

    <div v-if="loading" class="flex justify-center py-12">
      <div class="h-8 w-8 animate-spin rounded-full border-3 border-violet-200 border-t-violet-600"></div>
    </div>

    <template v-else-if="stats">
      <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div v-for="(item, i) in overviewCards" :key="item.label" class="card p-6 animate-slide-up" :style="{ animationDelay: `${i * 80}ms` }">
          <p class="text-sm text-slate-400">{{ item.label }}</p>
          <p class="mt-2 text-3xl font-bold bg-gradient-to-r from-violet-600 to-indigo-500 bg-clip-text text-transparent">{{ item.value }}</p>
        </div>
      </div>

      <div class="grid gap-8 xl:grid-cols-[1.1fr_0.9fr]">
        <div class="card p-8">
          <h2 class="section-title mb-6">MBTI 类型分布</h2>
          <div class="space-y-3">
            <div v-for="item in sortedMbtiDistribution" :key="item.type" class="grid grid-cols-[56px_1fr_76px] items-center gap-3">
              <span class="text-sm font-semibold text-slate-900">{{ item.type }}</span>
              <div class="h-7 overflow-hidden rounded-full bg-white/60">
                <div
                  class="flex h-full items-center justify-end rounded-full pr-3 text-xs font-semibold text-white transition-all duration-500"
                  :style="{
                    width: `${Math.max(item.percentage, item.count > 0 ? 8 : 0)}%`,
                    background: 'linear-gradient(90deg, #8b5cf6, #6366f1, #3b82f6)'
                  }"
                >
                  <span v-if="item.count > 0">{{ item.count }}</span>
                </div>
              </div>
              <span class="text-right text-sm text-slate-500">{{ item.percentage }}%</span>
            </div>
          </div>
        </div>

        <div class="card p-8">
          <h2 class="section-title mb-6">维度分布</h2>
          <div class="space-y-5">
            <div v-for="pair in dimensionPairs" :key="pair.label" class="space-y-2">
              <div class="flex items-center justify-between text-sm">
                <span class="font-medium text-slate-700">{{ pair.left }} / {{ pair.right }}</span>
                <span class="text-slate-400">{{ pair.leftVal }} / {{ pair.rightVal }}</span>
              </div>
              <div class="flex h-4 overflow-hidden rounded-full bg-white/50">
                <div class="h-full" :style="{ width: `${pair.leftPct}%`, background: 'linear-gradient(90deg, #8b5cf6, #6366f1)' }"></div>
                <div class="h-full" :style="{ width: `${pair.rightPct}%`, background: 'linear-gradient(90deg, #3b82f6, #06b6d4)' }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="card p-8">
        <div class="mb-6 flex items-center justify-between">
          <h2 class="section-title">用户作答情况</h2>
          <span class="badge-muted">{{ answeredCount }} / {{ stats.userAnswerStatuses?.length || 0 }} 已作答</span>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full min-w-[640px] text-left text-sm">
            <thead class="text-xs text-slate-400">
              <tr class="border-b border-white/50">
                <th class="py-3 font-medium">用户</th>
                <th class="py-3 font-medium">账号</th>
                <th class="py-3 font-medium">状态</th>
                <th class="py-3 font-medium">MBTI</th>
                <th class="py-3 font-medium">提交时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in stats.userAnswerStatuses" :key="user.userId" class="border-b border-white/40 last:border-0">
                <td class="py-3 font-medium text-slate-900">{{ user.name || '未设置姓名' }}</td>
                <td class="py-3 text-slate-500">{{ user.account }}</td>
                <td class="py-3">
                  <span
                    :class="[
                      'inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ring-1',
                      user.status === '已作答'
                        ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20'
                        : 'bg-amber-50 text-amber-700 ring-amber-600/20'
                    ]"
                  >
                    {{ user.status }}
                  </span>
                </td>
                <td class="py-3 text-slate-700">{{ user.mbtiType || '-' }}</td>
                <td class="py-3 text-slate-500">{{ formatDateTime(user.submittedAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card p-8">
        <h2 class="section-title mb-6">近 7 天提交</h2>
        <div class="flex h-40 items-end gap-2">
          <div v-for="day in stats.recentActivity" :key="day.date" class="flex flex-1 flex-col items-center gap-1">
            <span class="text-xs text-slate-400">{{ day.count }}</span>
            <div
              class="w-full rounded-t-lg transition-all duration-500"
              :style="{
                height: `${Math.max(4, (day.count / maxActivity) * 120)}px`,
                background: 'linear-gradient(180deg, #8b5cf6, #6366f1)'
              }"
            ></div>
            <span class="text-[10px] text-slate-400">{{ day.date.slice(5) }}</span>
          </div>
        </div>
      </div>
    </template>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import api from '../../api'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(true)
const stats = ref(null)
const selectedExamTypeId = ref('')

const overviewCards = computed(() => {
  if (!stats.value) return []
  const o = stats.value.overview
  return [
    { label: '参与人数', value: stats.value.userAnswerStatuses?.length || 0 },
    { label: '已作答', value: answeredCount.value },
    { label: '未作答', value: Math.max(0, (stats.value.userAnswerStatuses?.length || 0) - answeredCount.value) },
    { label: '题目数', value: o.totalQuestions }
  ]
})

const answeredCount = computed(() => (
  stats.value?.userAnswerStatuses?.filter(user => user.status === '已作答').length || 0
))

const sortedMbtiDistribution = computed(() => {
  if (!stats.value) return []
  return [...stats.value.mbtiDistribution].sort((a, b) => b.count - a.count || a.type.localeCompare(b.type))
})

const maxActivity = computed(() => {
  if (!stats.value) return 1
  return Math.max(1, ...stats.value.recentActivity.map(d => d.count))
})

const dimensionPairs = computed(() => {
  if (!stats.value) return []
  const d = stats.value.dimensionDistribution
  return [
    { left: 'E', right: 'I', leftVal: d.E || 0, rightVal: d.I || 0, leftPct: pct(d.E, d.I), rightPct: pct(d.I, d.E) },
    { left: 'S', right: 'N', leftVal: d.S || 0, rightVal: d.N || 0, leftPct: pct(d.S, d.N), rightPct: pct(d.N, d.S) },
    { left: 'T', right: 'F', leftVal: d.T || 0, rightVal: d.F || 0, leftPct: pct(d.T, d.F), rightPct: pct(d.F, d.T) },
    { left: 'J', right: 'P', leftVal: d.J || 0, rightVal: d.P || 0, leftPct: pct(d.J, d.P), rightPct: pct(d.P, d.J) }
  ]
})

function pct(a, b) {
  const total = (a || 0) + (b || 0)
  if (total === 0) return 50
  return Math.round((a / total) * 100)
}

async function loadStats() {
  loading.value = true
  try {
    const res = await api.get('/api/admin/stats', {
      params: selectedExamTypeId.value ? { examTypeId: selectedExamTypeId.value } : {}
    })
    stats.value = res.data
    selectedExamTypeId.value = res.data.selectedExamTypeId || ''
  } catch { /* ignore */ } finally { loading.value = false }
}

function formatDateTime(value) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 16)
}

onMounted(async () => {
  await authStore.fetchMe()
  if (!authStore.isAdmin) { router.replace('/'); return }
  await loadStats()
})
</script>
