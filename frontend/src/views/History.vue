<template>
  <section class="space-y-8">
    <div>
      <h1 class="section-title">测试记录</h1>
      <p class="mt-1 text-sm text-slate-400">查看你的历次测试结果</p>
    </div>

    <!-- Filter -->
    <div v-if="examTypes.length > 1" class="flex gap-2 flex-wrap">
      <button
        :class="['btn-secondary text-xs', !filterType ? 'ring-2 ring-violet-400' : '']"
        @click="filterType = ''"
      >全部</button>
      <button
        v-for="et in examTypes"
        :key="et.id"
        :class="['btn-secondary text-xs', filterType === et.id ? 'ring-2 ring-violet-400' : '']"
        @click="filterType = et.id"
      >{{ et.name }}</button>
    </div>

    <div v-if="loading" class="flex flex-col items-center justify-center py-24">
      <div class="h-8 w-8 animate-spin rounded-full border-3 border-violet-200 border-t-violet-600"></div>
    </div>

    <div v-else-if="results.length === 0" class="flex flex-col items-center justify-center py-24">
      <p class="text-sm text-slate-500">暂无测试记录</p>
      <router-link to="/test" class="btn-primary mt-4">开始测试</router-link>
    </div>

    <div v-else class="space-y-4">
      <router-link
        v-for="(r, i) in results"
        :key="r.id"
        :to="`/result/${r.id}`"
        class="card-hover p-6 flex items-center justify-between animate-slide-up block"
        :style="{ animationDelay: `${i * 50}ms` }"
      >
        <div class="flex items-center gap-4">
          <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-violet-500 to-indigo-500 text-lg font-bold text-white shadow-md">
            {{ r.mbtiType }}
          </div>
          <div>
            <p class="font-semibold text-slate-900">{{ r.mbtiType }}</p>
            <p class="text-xs text-slate-400 mt-0.5">
              {{ r.session?.examType?.name || '默认题库' }}
              · {{ formatDate(r.createdAt) }}
            </p>
          </div>
        </div>
        <svg class="w-5 h-5 text-slate-400" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
        </svg>
      </router-link>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../api'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(true)
const results = ref([])
const examTypes = ref([])
const filterType = ref('')

onMounted(async () => {
  await authStore.fetchMe()
  if (!authStore.isLoggedIn) {
    router.replace('/login?from=/history')
    return
  }
  await loadData()
})

async function loadData() {
  loading.value = true
  try {
    const [historyRes, typesRes] = await Promise.all([
      api.get('/api/history', { params: filterType.value ? { examTypeId: filterType.value } : {} }),
      api.get('/api/exam-types')
    ])
    results.value = historyRes.data.results || []
    examTypes.value = typesRes.data.examTypes || []
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

watch(filterType, () => loadData())

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}
</script>
