<template>
  <section class="space-y-8">
    <div>
      <h1 class="section-title">维度管理</h1>
      <p class="mt-1 text-sm text-slate-400">管理 MBTI 维度描述</p>
    </div>

    <!-- Test selector -->
    <div>
      <label class="mb-1.5 block text-sm font-medium text-slate-700">选择测试</label>
      <select class="input max-w-xs" v-model="selectedType" @change="loadDimensions">
        <option v-for="et in examTypes" :key="et.id" :value="et.id">{{ et.name }}</option>
      </select>
    </div>

    <div v-if="loading" class="flex justify-center py-12">
      <div class="h-8 w-8 animate-spin rounded-full border-3 border-violet-200 border-t-violet-600"></div>
    </div>

    <div v-else class="grid gap-4 sm:grid-cols-2">
      <div v-for="dim in dimensions" :key="dim.code" class="card p-6">
        <div class="flex items-center gap-2 mb-3">
          <span class="badge-primary">{{ dim.code }}</span>
          <span class="font-semibold text-slate-900">{{ dim.name }}</span>
        </div>
        <p class="text-sm text-slate-600 leading-relaxed">{{ dim.description }}</p>
        <button class="btn-secondary text-xs mt-4" @click="editDim(dim)">编辑</button>
      </div>
    </div>

    <!-- Edit Modal -->
    <div v-if="editing" class="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm" @click.self="editing = null">
      <div class="card p-8 max-w-md w-full mx-4">
        <h2 class="text-lg font-semibold mb-6">编辑维度 {{ editing.code }}</h2>
        <form @submit.prevent="saveDim" class="space-y-4">
          <div>
            <label class="mb-1.5 block text-sm font-medium text-slate-700">名称</label>
            <input class="input" v-model="editForm.name" required maxlength="20" />
          </div>
          <div>
            <label class="mb-1.5 block text-sm font-medium text-slate-700">描述</label>
            <textarea class="input" v-model="editForm.description" rows="4" required maxlength="2000"></textarea>
          </div>
          <div class="flex gap-3 pt-4">
            <button type="submit" class="btn-primary flex-1" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
            <button type="button" class="btn-secondary flex-1" @click="editing = null">取消</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import api from '../../api'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(true)
const examTypes = ref([])
const dimensions = ref([])
const selectedType = ref('')
const editing = ref(null)
const saving = ref(false)
const editForm = ref({ name: '', description: '' })

onMounted(async () => {
  await authStore.fetchMe()
  if (!authStore.isAdmin) { router.replace('/'); return }
  try {
    const res = await api.get('/api/admin/exam-types')
    examTypes.value = res.data.examTypes || []
    if (examTypes.value.length > 0) {
      selectedType.value = examTypes.value[0].id
      await loadDimensions()
    }
  } catch { /* ignore */ } finally { loading.value = false }
})

async function loadDimensions() {
  if (!selectedType.value) return
  loading.value = true
  try {
    const res = await api.get('/api/admin/dimensions', { params: { examTypeId: selectedType.value } })
    dimensions.value = res.data.dimensions || []
  } catch { /* ignore */ } finally { loading.value = false }
}

function editDim(dim) {
  editing.value = dim
  editForm.value = { name: dim.name, description: dim.description }
}

async function saveDim() {
  saving.value = true
  try {
    await api.put('/api/admin/dimensions', {
      code: editing.value.code,
      name: editForm.value.name,
      description: editForm.value.description,
      examTypeId: selectedType.value
    })
    editing.value = null
    await loadDimensions()
  } catch { /* ignore */ } finally { saving.value = false }
}
</script>
