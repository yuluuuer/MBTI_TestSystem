<template>
  <section class="space-y-8">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="section-title">测试管理</h1>
        <p class="mt-1 text-sm text-slate-400">管理测试题库、开放时间和参与人员</p>
      </div>
      <button class="btn-primary" @click="showCreate = true">新增测试</button>
    </div>

    <div v-if="loading" class="flex justify-center py-12">
      <div class="h-8 w-8 animate-spin rounded-full border-3 border-violet-200 border-t-violet-600"></div>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="(et, i) in examTypes"
        :key="et.id"
        :class="[
          'card p-6 animate-slide-up transition-all duration-300',
          et.isActive ? 'border-emerald-200/50' : 'border-amber-200/70 bg-amber-50/40'
        ]"
        :style="{ animationDelay: `${i * 50}ms` }"
      >
        <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div class="min-w-0 flex-1">
            <div class="flex flex-wrap items-center gap-2 mb-2">
              <span
                :class="[
                  'inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-semibold ring-1',
                  et.isActive
                    ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20'
                    : 'bg-amber-50 text-amber-700 ring-amber-600/20'
                ]"
              >
                <span :class="['h-1.5 w-1.5 rounded-full', et.isActive ? 'bg-emerald-500' : 'bg-amber-500']"></span>
                {{ et.isActive ? '启用中' : '已禁用' }}
              </span>
              <span class="badge-muted">{{ et._count?.questions || 0 }} 题</span>
              <span class="badge-muted">作答 {{ et.questionLimit || 36 }} 题</span>
              <span class="badge-muted">{{ participantText(et) }}</span>
            </div>
            <p class="font-semibold text-slate-900">{{ et.name }}</p>
            <p v-if="et.description" class="text-sm text-slate-500 mt-1">{{ et.description }}</p>
            <div class="mt-4 grid gap-2 text-xs text-slate-500 md:grid-cols-5">
              <span>创建时间：{{ formatDateTime(et.createdAt) }}</span>
              <span>开始时间：{{ formatDateTime(et.startTime) }}</span>
              <span>截止时间：{{ formatDateTime(et.endTime) }}</span>
              <span>测试时长：{{ et.durationMinutes || 30 }} 分钟</span>
              <span>作答题数：{{ et.questionLimit || 36 }} 题</span>
            </div>
          </div>
          <div class="flex flex-col gap-3 lg:ml-4 lg:w-48">
            <div class="grid grid-cols-2 gap-2">
              <button class="btn-secondary text-xs px-3 py-1.5" @click="editType(et)">编辑</button>
              <button class="btn-danger text-xs px-3 py-1.5" @click="deleteType(et.id)">删除</button>
            </div>
            <button class="btn-secondary text-xs px-3 py-1.5" @click="manageParticipants(et)">管理参测人员</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showCreate || editing" class="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm" @click.self="closeModal">
      <div class="card p-8 max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        <h2 class="text-lg font-semibold mb-6">{{ editing ? '编辑测试' : '新增测试' }}</h2>
        <form @submit.prevent="saveType" class="space-y-4">
          <div>
            <label class="mb-1.5 block text-sm font-medium text-slate-700">测试名称</label>
            <input class="input" v-model="form.name" required maxlength="50" />
          </div>
          <div>
            <label class="mb-1.5 block text-sm font-medium text-slate-700">描述</label>
            <textarea class="input" v-model="form.description" rows="3" maxlength="1000"></textarea>
          </div>

          <div class="grid gap-4 sm:grid-cols-2">
            <div>
              <label class="mb-1.5 block text-sm font-medium text-slate-700">开始时间</label>
              <input class="input" type="datetime-local" v-model="form.startTime" />
            </div>
            <div>
              <label class="mb-1.5 block text-sm font-medium text-slate-700">截止时间</label>
              <input class="input" type="datetime-local" v-model="form.endTime" />
            </div>
            <div>
              <label class="mb-1.5 block text-sm font-medium text-slate-700">测试时长（分钟）</label>
              <input class="input" type="number" min="1" max="600" v-model.number="form.durationMinutes" required />
            </div>
            <div>
              <label class="mb-1.5 block text-sm font-medium text-slate-700">作答题数</label>
              <input class="input" type="number" min="1" max="200" v-model.number="form.questionLimit" required />
            </div>
          </div>

          <div class="flex items-center justify-between rounded-xl border border-white/60 bg-white/50 px-4 py-3">
            <div>
              <p class="text-sm font-medium text-slate-700">测试状态</p>
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

          <div class="flex gap-3 pt-4">
            <button type="submit" class="btn-primary flex-1" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
            <button type="button" class="btn-secondary flex-1" @click="closeModal">取消</button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="participantEditing" class="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm" @click.self="closeParticipantModal">
      <div class="card p-8 max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        <div class="mb-6 flex items-start justify-between gap-4">
          <div>
            <h2 class="text-lg font-semibold">管理参测人员</h2>
            <p class="mt-1 text-sm text-slate-500">{{ participantEditing.name }}</p>
          </div>
          <span class="badge-muted whitespace-nowrap">{{ participantForm.participantIds.length }} 人已选</span>
        </div>

        <div class="mb-3 flex items-center justify-between">
          <p class="text-sm font-medium text-slate-700">选择可参与该测试的用户</p>
          <div class="flex gap-2">
            <button type="button" class="btn-secondary px-3 py-1.5 text-xs" @click="selectAllParticipants">
              所有人
            </button>
            <button type="button" class="btn-secondary px-3 py-1.5 text-xs" @click="clearParticipants">
              清空
            </button>
          </div>
        </div>

        <div class="mb-4 rounded-xl border border-violet-100 bg-violet-50/60 p-4">
          <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <p class="text-sm font-medium text-slate-800">批量导入参测人员</p>
              <p class="mt-1 text-xs text-slate-500">支持 .txt/.csv，每行：手机号, 姓名, 性别, 出生日期；新用户默认密码 123456。</p>
            </div>
            <a class="btn-secondary px-3 py-1.5 text-xs text-center" href="/参测人员批量导入样本.csv" download>下载样本</a>
          </div>
          <div class="mt-3 flex flex-col gap-2 sm:flex-row">
            <input
              class="input bg-white"
              type="file"
              accept=".txt,.csv,text/plain,text/csv"
              @change="handleImportFileChange"
            />
            <button
              type="button"
              class="btn-primary whitespace-nowrap px-4 py-2 text-sm"
              :disabled="importingParticipants || !importFile"
              @click="importParticipants"
            >
              {{ importingParticipants ? '导入中...' : '导入参测人员' }}
            </button>
          </div>
          <p v-if="importMessage" class="mt-2 text-xs text-emerald-600">{{ importMessage }}</p>
          <p v-if="importError" class="mt-2 text-xs text-red-500">{{ importError }}</p>
        </div>

        <div class="max-h-80 space-y-2 overflow-y-auto rounded-xl border border-white/60 bg-white/50 p-3">
          <label
            v-for="user in selectableUsers"
            :key="user.id"
            class="flex items-center justify-between gap-3 rounded-lg px-2 py-2 text-sm text-slate-700 hover:bg-white/60"
          >
            <span class="min-w-0">
              <span class="font-medium text-slate-900">{{ user.name || user.email }}</span>
              <span class="ml-2 text-xs text-slate-400">{{ user.phone || user.email }}</span>
            </span>
            <input type="checkbox" :value="user.id" v-model="participantForm.participantIds" />
          </label>
          <p v-if="selectableUsers.length === 0" class="py-6 text-center text-sm text-slate-400">暂无普通用户</p>
        </div>

        <p class="mt-3 text-xs text-slate-400">未选择任何人员时，该测试暂无用户可参与。</p>

        <div class="flex gap-3 pt-6">
          <button type="button" class="btn-primary flex-1" :disabled="savingParticipants" @click="saveParticipants">
            {{ savingParticipants ? '保存中...' : '保存参测人员' }}
          </button>
          <button type="button" class="btn-secondary flex-1" @click="closeParticipantModal">取消</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import api from '../../api'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(true)
const examTypes = ref([])
const users = ref([])
const showCreate = ref(false)
const editing = ref(null)
const saving = ref(false)
const participantEditing = ref(null)
const participantForm = ref({ participantIds: [] })
const savingParticipants = ref(false)
const importFile = ref(null)
const importingParticipants = ref(false)
const importMessage = ref('')
const importError = ref('')

const defaultForm = () => ({
  name: '',
  description: '',
  isActive: true,
  startTime: '',
  endTime: '',
  durationMinutes: 30,
  questionLimit: 36,
  participantIds: []
})
const form = ref(defaultForm())

const selectableUsers = computed(() => users.value.filter(user => user.role !== 'ADMIN' && user.isActive !== false))

onMounted(async () => {
  await authStore.fetchMe()
  if (!authStore.isAdmin) { router.replace('/'); return }
  await loadData()
})

async function loadData() {
  loading.value = true
  try {
    const [typeRes, userRes] = await Promise.all([
      api.get('/api/admin/exam-types'),
      api.get('/api/admin/users')
    ])
    examTypes.value = typeRes.data.examTypes || []
    users.value = userRes.data.users || []
  } catch { /* ignore */ } finally { loading.value = false }
}

function editType(et) {
  editing.value = et.id
  form.value = {
    name: et.name,
    description: et.description,
    isActive: et.isActive,
    startTime: toDatetimeLocal(et.startTime),
    endTime: toDatetimeLocal(et.endTime),
    durationMinutes: et.durationMinutes || 30,
    questionLimit: et.questionLimit || 36,
    participantIds: [...(et.participantIds || [])]
  }
}

function closeModal() {
  showCreate.value = false
  editing.value = null
  form.value = defaultForm()
}

function selectAllParticipants() {
  participantForm.value.participantIds = selectableUsers.value.map(user => user.id)
}

function clearParticipants() {
  participantForm.value.participantIds = []
}

function manageParticipants(et) {
  participantEditing.value = et
  participantForm.value = {
    participantIds: [...(et.participantIds || [])]
  }
  importFile.value = null
  importMessage.value = ''
  importError.value = ''
}

function closeParticipantModal() {
  participantEditing.value = null
  participantForm.value = { participantIds: [] }
  importFile.value = null
  importMessage.value = ''
  importError.value = ''
}

function handleImportFileChange(event) {
  importFile.value = event.target.files?.[0] || null
  importMessage.value = ''
  importError.value = ''
}

async function saveType() {
  saving.value = true
  try {
    const payload = {
      name: form.value.name,
      description: form.value.description,
      isActive: form.value.isActive,
      startTime: form.value.startTime ? `${form.value.startTime}:00` : null,
      endTime: form.value.endTime ? `${form.value.endTime}:00` : null,
      durationMinutes: form.value.durationMinutes,
      questionLimit: form.value.questionLimit,
      participantIds: editing.value
        ? examTypes.value.find(item => item.id === editing.value)?.participantIds || []
        : []
    }
    if (editing.value) {
      await api.put('/api/admin/exam-types', { id: editing.value, ...payload })
    } else {
      await api.post('/api/admin/exam-types', payload)
    }
    closeModal()
    await loadData()
  } catch (err) {
    alert(err.message || '保存测试失败')
  } finally { saving.value = false }
}

async function saveParticipants() {
  if (!participantEditing.value) return
  savingParticipants.value = true
  try {
    const et = participantEditing.value
    await api.put('/api/admin/exam-types', {
      id: et.id,
      name: et.name,
      description: et.description,
      isActive: et.isActive,
      startTime: et.startTime,
      endTime: et.endTime,
      durationMinutes: et.durationMinutes || 30,
      questionLimit: et.questionLimit || 36,
      participantIds: participantForm.value.participantIds
    })
    closeParticipantModal()
    await loadData()
  } catch (err) {
    alert(err.message || '保存参测人员失败')
  } finally {
    savingParticipants.value = false
  }
}

async function importParticipants() {
  if (!participantEditing.value || !importFile.value) return
  importingParticipants.value = true
  importMessage.value = ''
  importError.value = ''
  try {
    const data = new FormData()
    data.append('file', importFile.value)
    const res = await api.post(`/api/admin/exam-types/${participantEditing.value.id}/participants/import`, data)
    await loadData()
    const updated = examTypes.value.find(item => item.id === participantEditing.value.id)
    if (updated) {
      participantEditing.value = updated
      participantForm.value = { participantIds: [...(updated.participantIds || [])] }
    }
    importMessage.value = `导入成功：新增 ${res.data.createdCount || 0} 人，更新 ${res.data.updatedCount || 0} 人，当前参测 ${res.data.participantCount || 0} 人。`
    if (res.data.skippedRows?.length) {
      importMessage.value += ` 跳过 ${res.data.skippedRows.length} 行。`
    }
    importFile.value = null
  } catch (err) {
    importError.value = err.response?.data?.message || err.message || '导入参测人员失败'
  } finally {
    importingParticipants.value = false
  }
}

async function deleteType(id) {
  if (!confirm('确定删除此测试？关联的题目也会被删除。')) return
  await api.delete('/api/admin/exam-types', { data: { id } })
  await loadData()
}

function participantText(et) {
  const count = et.participantIds?.length || 0
  return count > 0 ? `${count} 人参与` : '暂无参测人员'
}

function formatDateTime(value) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 16)
}

function toDatetimeLocal(value) {
  return value ? value.slice(0, 16) : ''
}
</script>
