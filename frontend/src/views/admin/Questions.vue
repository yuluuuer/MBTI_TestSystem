<template>
  <section class="space-y-8">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="section-title">题库管理</h1>
        <p class="mt-1 text-sm text-slate-400">管理测试题目和选项</p>
      </div>
      <button class="btn-primary" @click="showCreate = true">新增题目</button>
    </div>

    <div class="space-y-3">
      <div v-if="examTypes.length > 0" class="flex flex-wrap items-center gap-2">
        <span class="text-xs font-medium text-slate-500">所属测试</span>
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

      <div class="flex flex-wrap items-center gap-2">
        <span class="text-xs font-medium text-slate-500">维度筛选</span>
        <button
          :class="['btn-secondary text-xs', !filterDimension ? 'ring-2 ring-violet-400' : '']"
          @click="filterDimension = ''"
        >全部</button>
        <button
          v-for="d in dimensions"
          :key="d"
          :class="['btn-secondary text-xs', filterDimension === d ? 'ring-2 ring-violet-400' : '']"
          @click="filterDimension = d"
        >{{ d }}</button>
      </div>
    </div>

    <div v-if="loading" class="flex justify-center py-12">
      <div class="h-8 w-8 animate-spin rounded-full border-3 border-violet-200 border-t-violet-600"></div>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="(q, i) in questions"
        :key="q.id"
        :class="[
          'card p-6 animate-slide-up transition-all duration-300',
          q.isActive ? 'border-emerald-200/50' : 'border-amber-200/70 bg-amber-50/40'
        ]"
        :style="{ animationDelay: `${i * 30}ms` }"
      >
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <div class="flex items-center gap-2 mb-2">
              <span class="badge-muted">#{{ q.sortOrder }}</span>
              <span
                :class="[
                  'inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-semibold ring-1',
                  q.isActive
                    ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20'
                    : 'bg-amber-50 text-amber-700 ring-amber-600/20'
                ]"
              >
                <span
                  :class="[
                    'h-1.5 w-1.5 rounded-full',
                    q.isActive ? 'bg-emerald-500' : 'bg-amber-500'
                  ]"
                ></span>
                {{ q.isActive ? '启用中' : '已禁用' }}
              </span>
            </div>
            <p class="font-medium text-slate-900">{{ q.title }}</p>
            <div class="mt-3 space-y-1">
              <div v-for="op in q.options" :key="op.id" class="flex items-center gap-2 text-sm text-slate-500">
                <span class="badge-muted">{{ op.dimension }}</span>
                <span>{{ op.label }}</span>
                <span class="text-xs text-slate-400">(权重: {{ op.weight }})</span>
              </div>
            </div>
          </div>
          <div class="flex gap-2 ml-4">
            <button class="btn-secondary text-xs px-3 py-1.5" @click="editQuestion(q)">编辑</button>
            <button class="btn-danger text-xs px-3 py-1.5" @click="deleteQuestion(q.id)">删除</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showCreate || editing" class="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm" @click.self="closeModal">
      <div class="card p-8 max-w-lg w-full mx-4 max-h-[90vh] overflow-y-auto">
        <h2 class="text-lg font-semibold mb-6">{{ editing ? '编辑题目' : '新增题目' }}</h2>
        <form @submit.prevent="saveQuestion" class="space-y-4">
          <div>
            <label class="mb-1.5 block text-sm font-medium text-slate-700">题目标题</label>
            <input class="input" v-model="form.title" required />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="mb-1.5 block text-sm font-medium text-slate-700">排序号</label>
              <input class="input" type="number" v-model.number="form.sortOrder" min="1" required />
            </div>
            <div>
              <label class="mb-1.5 block text-sm font-medium text-slate-700">所属测试</label>
              <select class="input" v-model="form.examTypeId" required>
                <option v-for="et in examTypes" :key="et.id" :value="et.id">{{ et.name }}</option>
              </select>
            </div>
          </div>
          <div class="flex items-center justify-between rounded-xl border border-white/60 bg-white/50 px-4 py-3">
            <div>
              <p class="text-sm font-medium text-slate-700">题目状态</p>
              <p :class="['mt-0.5 text-xs', form.isActive ? 'text-emerald-600' : 'text-amber-600']">
                {{ form.isActive ? '启用中' : '已禁用' }}
              </p>
            </div>
            <button
              type="button"
              role="switch"
              :aria-checked="form.isActive"
              :class="[
                'relative h-7 w-12 rounded-full transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-violet-300',
                form.isActive ? 'bg-emerald-500' : 'bg-slate-300'
              ]"
              @click="form.isActive = !form.isActive"
            >
              <span
                :class="[
                  'absolute left-0 top-1 h-5 w-5 rounded-full bg-white shadow-sm transition-transform duration-200',
                  form.isActive ? 'translate-x-6' : 'translate-x-1'
                ]"
              ></span>
            </button>
          </div>

          <div>
            <label class="mb-1.5 block text-sm font-medium text-slate-700">选项</label>
            <div v-for="(opt, idx) in form.options" :key="idx" class="flex gap-2 mb-2">
              <input class="input flex-1" v-model="opt.label" placeholder="选项标签" required />
              <select class="input w-20" v-model="opt.dimension" required>
                <option v-for="d in dimensions" :key="d" :value="d">{{ d }}</option>
              </select>
              <input class="input w-16" type="number" v-model.number="opt.weight" min="1" max="5" required />
              <button type="button" class="btn-danger px-2" @click="form.options.splice(idx, 1)" v-if="form.options.length > 2">×</button>
            </div>
            <button type="button" class="btn-secondary text-xs mt-2" @click="form.options.push({ label: '', dimension: 'E', weight: 1 })">添加选项</button>
          </div>

          <div class="flex gap-3 pt-4">
            <button type="submit" class="btn-primary flex-1" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
            <button type="button" class="btn-secondary flex-1" @click="closeModal">取消</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { useRouter } from 'vue-router'
import api from '../../api'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(true)
const questions = ref([])
const examTypes = ref([])
const filterType = ref('')
const filterDimension = ref('')
const showCreate = ref(false)
const editing = ref(null)
const saving = ref(false)
const dimensions = ['E', 'I', 'S', 'N', 'T', 'F', 'J', 'P']

const defaultForm = () => ({
  title: '', sortOrder: 1, isActive: true, examTypeId: '',
  options: [
    { label: '', dimension: 'E', weight: 1 },
    { label: '', dimension: 'I', weight: 1 }
  ]
})
const form = ref(defaultForm())

onMounted(async () => {
  await authStore.fetchMe()
  if (!authStore.isAdmin) { router.replace('/'); return }
  await loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params = {}
    if (filterType.value) params.examTypeId = filterType.value
    if (filterDimension.value) params.dimension = filterDimension.value
    const [qRes, etRes] = await Promise.all([
      api.get('/api/admin/questions', { params }),
      api.get('/api/admin/exam-types')
    ])
    questions.value = qRes.data.questions || []
    examTypes.value = (etRes.data.examTypes || []).map(et => ({ id: et.id, name: et.name }))
  } catch { /* ignore */ } finally { loading.value = false }
}

watch([filterType, filterDimension], () => loadData())

function editQuestion(q) {
  editing.value = q.id
  form.value = {
    title: q.title,
    sortOrder: q.sortOrder,
    isActive: q.isActive,
    examTypeId: q.examTypeId,
    options: q.options.map(op => ({ label: op.label, dimension: op.dimension, weight: op.weight }))
  }
  if (examTypes.value.length && !form.value.examTypeId) form.value.examTypeId = examTypes.value[0].id
}

function closeModal() {
  showCreate.value = false
  editing.value = null
  form.value = defaultForm()
}

async function saveQuestion() {
  saving.value = true
  try {
    if (editing.value) {
      await api.put('/api/admin/questions', { id: editing.value, ...form.value })
    } else {
      await api.post('/api/admin/questions', form.value)
    }
    closeModal()
    await loadData()
  } catch { /* ignore */ } finally { saving.value = false }
}

async function deleteQuestion(id) {
  if (!confirm('确定删除此题目？')) return
  await api.delete('/api/admin/questions', { data: { id } })
  await loadData()
}
</script>
