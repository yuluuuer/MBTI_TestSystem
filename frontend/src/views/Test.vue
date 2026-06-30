<template>
  <!-- Phase 1: Exam type selection -->
  <section v-if="phase === 'select' && !loading" class="space-y-8">
    <div>
      <h1 class="section-title">选择测试</h1>
      <p class="mt-1 text-sm text-slate-400">请选择已分配给你的测试题库</p>
    </div>

    <div v-if="error" class="rounded-xl border border-red-200/60 bg-red-50/80 px-4 py-3 text-sm text-red-600 backdrop-blur-sm">{{ error }}</div>

    <div v-if="examTypes.length === 0" class="flex flex-col items-center justify-center py-24">
      <p class="text-sm text-slate-500">暂无可参与的测试，请联系管理员。</p>
    </div>

    <div class="grid gap-4 sm:grid-cols-2">
      <button
        v-for="(type, i) in examTypes"
        :key="type.id"
        type="button"
        @click="selectExamType(type)"
        :disabled="!type.canStart"
        :class="[
          'card p-6 text-left transition-all duration-300 animate-slide-up',
          type.canStart
            ? 'cursor-pointer hover:shadow-lg hover:shadow-violet-500/10 hover:border-violet-300/60 hover:-translate-y-0.5'
            : 'cursor-not-allowed opacity-70'
        ]"
        :style="{ animationDelay: `${i * 80}ms` }"
      >
        <div class="flex items-center gap-3 mb-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-violet-500 to-indigo-500 text-sm font-bold text-white shadow-md">
            {{ type.name.charAt(0) }}
          </div>
          <div class="min-w-0 flex-1">
            <h3 class="text-base font-semibold text-slate-900">{{ type.name }}</h3>
            <span
              :class="[
                'mt-1 inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ring-1',
                type.canStart
                  ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20'
                  : type.statusText === '未作答'
                    ? 'bg-red-50 text-red-700 ring-red-600/20'
                    : 'bg-amber-50 text-amber-700 ring-amber-600/20'
              ]"
            >
              {{ type.statusText || (type.canStart ? '可作答' : '不可作答') }}
            </span>
          </div>
        </div>
        <p v-if="type.description" class="text-sm text-slate-500 leading-relaxed">{{ type.description }}</p>
        <div class="mt-4 space-y-1 text-xs text-slate-500">
          <p>题目数量：{{ type.questionCount || 0 }} 道</p>
          <p>测试时长：{{ type.durationMinutes || 30 }} 分钟</p>
          <p>开始时间：{{ formatDateTime(type.startTime) }}</p>
          <p>截止时间：{{ formatDateTime(type.endTime) }}</p>
        </div>
        <div class="mt-4 flex items-center gap-1.5 text-xs text-violet-600 font-medium">
          <span>{{ actionText(type) }}</span>
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3" />
          </svg>
        </div>
      </button>
    </div>
  </section>

  <!-- Loading state -->
  <section v-else-if="loading" class="flex flex-col items-center justify-center py-24">
    <div class="h-8 w-8 animate-spin rounded-full border-3 border-violet-200 border-t-violet-600"></div>
    <p class="mt-4 text-sm text-slate-500">正在加载题库...</p>
  </section>

  <!-- Phase 2: Confirm -->
  <section v-else-if="phase === 'confirm'" class="space-y-8">
    <div>
      <h1 class="section-title">确认测试信息</h1>
      <p class="mt-1 text-sm text-slate-400">确认后将开始计时并进入答题</p>
    </div>

    <div v-if="error" class="rounded-xl border border-red-200/60 bg-red-50/80 px-4 py-3 text-sm text-red-600 backdrop-blur-sm">{{ error }}</div>

    <div class="card p-8">
      <h2 class="text-xl font-semibold text-slate-900">{{ selectedType?.name }}</h2>
      <p v-if="selectedType?.description" class="mt-2 text-sm text-slate-500 leading-relaxed">{{ selectedType.description }}</p>

      <div class="mt-6 grid gap-4 sm:grid-cols-2">
        <div class="rounded-xl border border-white/60 bg-white/50 px-4 py-3">
          <p class="text-xs text-slate-400">题目数量</p>
          <p class="mt-1 text-lg font-semibold text-slate-900">{{ selectedType?.questionCount || 0 }} 道</p>
        </div>
        <div class="rounded-xl border border-white/60 bg-white/50 px-4 py-3">
          <p class="text-xs text-slate-400">测试时长</p>
          <p class="mt-1 text-lg font-semibold text-slate-900">{{ selectedType?.durationMinutes || 30 }} 分钟</p>
        </div>
        <div class="rounded-xl border border-white/60 bg-white/50 px-4 py-3">
          <p class="text-xs text-slate-400">开始时间</p>
          <p class="mt-1 text-sm font-medium text-slate-900">{{ formatDateTime(selectedType?.startTime) }}</p>
        </div>
        <div class="rounded-xl border border-white/60 bg-white/50 px-4 py-3">
          <p class="text-xs text-slate-400">截止时间</p>
          <p class="mt-1 text-sm font-medium text-slate-900">{{ formatDateTime(selectedType?.endTime) }}</p>
        </div>
      </div>

      <div class="mt-8 flex gap-3">
        <button type="button" class="btn-primary flex-1 py-3" :disabled="starting" @click="startTest">
          {{ starting ? '准备中...' : '确认并开始答题' }}
        </button>
        <button type="button" class="btn-secondary flex-1 py-3" @click="backToSelect">返回选择</button>
      </div>
    </div>
  </section>

  <!-- Phase 3: Testing -->
  <section v-else class="space-y-8">
    <div class="flex items-end justify-between">
      <div>
        <h1 class="section-title">{{ selectedType?.name || 'MBTI 测试' }}</h1>
        <p class="mt-1 text-sm text-slate-400">
          共 {{ questions.length }} 道题目
          <span v-if="sessionDeadline">，截止提交：{{ formatDateTime(sessionDeadline) }}</span>
        </p>
      </div>
      <span class="text-sm tabular-nums font-semibold bg-gradient-to-r from-violet-600 to-indigo-500 bg-clip-text text-transparent">{{ answeredCount }} / {{ questions.length }}</span>
    </div>

    <!-- Progress bar -->
    <div class="h-2 w-full rounded-full bg-white/50 overflow-hidden">
      <div
        class="h-full rounded-full transition-all duration-700 ease-out"
        :style="{ width: `${progress}%`, background: 'linear-gradient(90deg, #8b5cf6, #6366f1, #3b82f6)' }"
      ></div>
    </div>

    <div v-if="error" class="rounded-xl border border-red-200/60 bg-red-50/80 px-4 py-3 text-sm text-red-600 backdrop-blur-sm">{{ error }}</div>

    <!-- Questions -->
    <article
      v-for="(q, idx) in questions"
      :key="q.id"
      class="card p-6 animate-slide-up"
      :style="{ animationDelay: `${idx * 50}ms` }"
    >
      <div class="mb-4 flex items-center gap-3">
        <span :class="[
          'flex h-8 w-8 items-center justify-center rounded-full text-xs font-bold transition-all duration-300',
          answers[q.id]
            ? 'bg-gradient-to-br from-violet-500 to-indigo-500 text-white shadow-md shadow-violet-500/25'
            : 'bg-white/60 text-slate-400 border border-white/40'
        ]">{{ idx + 1 }}</span>
        <h3 class="text-[15px] font-medium text-slate-900">{{ q.title }}</h3>
      </div>
      <div class="ml-11 space-y-2">
        <label
          v-for="op in q.options"
          :key="op.id"
          :class="[
            'flex cursor-pointer items-center gap-3 rounded-xl border px-4 py-3 transition-all duration-300',
            answers[q.id] === op.id
              ? 'border-violet-300 bg-violet-50/50 shadow-sm shadow-violet-500/10'
              : 'border-white/40 bg-white/30 hover:border-violet-200/40 hover:bg-white/50'
          ]"
        >
          <div :class="[
            'h-2.5 w-2.5 rounded-full transition-all duration-300',
            answers[q.id] === op.id
              ? 'bg-gradient-to-br from-violet-500 to-indigo-500 scale-110 shadow-sm shadow-violet-500/30'
              : 'bg-slate-300'
          ]"></div>
          <span :class="['text-sm transition-colors', answers[q.id] === op.id ? 'font-medium text-slate-900' : 'text-slate-600']">
            {{ op.label }}
          </span>
          <input
            type="radio"
            :name="q.id"
            :checked="answers[q.id] === op.id"
            @change="onSelect(q.id, op.id)"
            class="sr-only"
          />
        </label>
      </div>
    </article>

    <!-- Submit -->
    <div class="sticky bottom-4 z-10">
      <button
        type="button"
        :disabled="!allAnswered || submitting"
        @click="onSubmit"
        :class="['btn-primary w-full py-3.5 text-base', allAnswered ? 'animate-pulse-glow' : '']"
      >
        {{ submitting ? '提交中...' : allAnswered ? '提交并查看结果' : `请完成全部题目 (${answeredCount}/${questions.length})` }}
      </button>
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

const phase = ref('select')
const examTypes = ref([])
const selectedType = ref(null)
const questions = ref([])
const answers = ref({})
const sessionId = ref('')
const loading = ref(true)
const starting = ref(false)
const submitting = ref(false)
const error = ref('')
const sessionDeadline = ref('')

const answeredCount = computed(() => questions.value.filter(q => answers.value[q.id]).length)
const allAnswered = computed(() => questions.value.length > 0 && answeredCount.value === questions.value.length)
const progress = computed(() => questions.value.length > 0 ? (answeredCount.value / questions.value.length) * 100 : 0)

onMounted(async () => {
  try {
    await authStore.fetchMe()
    if (!authStore.isLoggedIn) {
      router.replace('/login?from=/test')
      return
    }
    const res = await api.get('/api/exam-types')
    examTypes.value = res.data.examTypes || []
  } catch {
    error.value = '加载测试失败'
  } finally {
    loading.value = false
  }
})

watch(phase, (val) => {
  if (val !== 'select') return
  questions.value = []
  answers.value = {}
  sessionId.value = ''
  sessionDeadline.value = ''
})

async function loadQuestionsAndSession() {
  if (!selectedType.value) return
  try {
    starting.value = true
    error.value = ''
    const [questionRes, sessionRes] = await Promise.all([
      api.get(`/api/questions?examTypeId=${selectedType.value.id}`),
      api.post('/api/session/start', { examTypeId: selectedType.value.id })
    ])
    questions.value = questionRes.data.questions || []
    sessionId.value = sessionRes.data.sessionId || ''
    sessionDeadline.value = sessionRes.data.deadlineAt || ''
    phase.value = 'testing'
  } catch (err) {
    error.value = err.message || '初始化失败'
  } finally {
    starting.value = false
  }
}

function selectExamType(type) {
  if (!type.canStart) return
  selectedType.value = type
  phase.value = 'confirm'
}

function actionText(type) {
  if (type.canStart) return '查看并确认'
  if (type.statusText === '未作答') return '已截止，无法作答'
  if (type.statusText === '已作答') return '已完成'
  return '未到开始时间'
}

function backToSelect() {
  phase.value = 'select'
}

function startTest() {
  loadQuestionsAndSession()
}

function formatDateTime(value) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 16)
}

async function onSelect(questionId, optionId) {
  answers.value = { ...answers.value, [questionId]: optionId }
  if (!sessionId.value) return
  await api.post('/api/session/answer', { sessionId: sessionId.value, questionId, optionId })
}

async function onSubmit() {
  if (!allAnswered.value || !sessionId.value) return
  submitting.value = true
  error.value = ''
  try {
    const res = await api.post('/api/session/submit', { sessionId: sessionId.value })
    if (!res.data.ok) throw new Error('提交失败')
    router.push(`/result/${res.data.resultId}`)
  } catch (err) {
    error.value = err.message || '提交失败'
  } finally {
    submitting.value = false
  }
}
</script>
