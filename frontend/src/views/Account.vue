<template>
  <section class="mx-auto max-w-3xl space-y-8">
    <div>
      <h1 class="section-title">个人账户</h1>
      <p class="mt-1 text-sm text-slate-400">管理你的个人信息</p>
    </div>

    <div v-if="!authStore.isLoggedIn" class="card p-8 text-center">
      <p class="text-sm text-slate-500 mb-4">请先登录</p>
      <router-link to="/login?from=/account" class="btn-primary">登录</router-link>
    </div>

    <div v-else class="card p-8 space-y-8">
      <div class="flex items-center gap-4">
        <div class="flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-violet-500 to-indigo-500 text-2xl font-bold text-white shadow-lg">
          {{ (authStore.user?.name || authStore.user?.email || '?').charAt(0).toUpperCase() }}
        </div>
        <div>
          <p class="text-lg font-semibold text-slate-900">{{ authStore.user?.name || '未设置昵称' }}</p>
          <p class="text-sm text-slate-400">{{ authStore.user?.email }}</p>
          <span v-if="authStore.isAdmin" class="badge-primary mt-1">管理员</span>
        </div>
      </div>

      <div class="grid gap-3 sm:grid-cols-2">
        <div v-for="item in profileItems" :key="item.label" class="rounded-xl border border-white/60 bg-white/50 px-4 py-3">
          <p class="text-xs text-slate-400">{{ item.label }}</p>
          <p class="mt-1 break-words text-sm font-medium text-slate-800">{{ item.value || '-' }}</p>
        </div>
      </div>

      <div class="rounded-xl border border-white/60 bg-white/40 p-4">
        <div class="flex items-center justify-between gap-3">
          <div>
            <p class="text-sm font-medium text-slate-700">昵称</p>
            <p class="mt-1 text-sm text-slate-500">{{ authStore.user?.name || '未设置昵称' }}</p>
          </div>
          <button v-if="!editingName" type="button" class="btn-secondary px-4 py-2 text-sm" @click="openNameEdit">修改昵称</button>
        </div>
        <div v-if="editingName" class="mt-3 flex flex-col gap-2 sm:flex-row">
          <input class="input flex-1" v-model="name" placeholder="设置昵称" maxlength="50" />
          <button class="btn-primary px-4" @click="saveName" :disabled="savingName || !name.trim()">
            {{ savingName ? '保存中...' : '保存' }}
          </button>
          <button type="button" class="btn-secondary px-4" @click="cancelNameEdit">取消</button>
        </div>
        <p v-if="nameMsg" class="mt-2 text-xs" :class="nameMsgOk ? 'text-emerald-600' : 'text-red-500'">{{ nameMsg }}</p>
      </div>

      <div class="rounded-xl border border-white/60 bg-white/40 p-4">
        <div class="flex items-center justify-between gap-3">
          <div>
            <h2 class="text-sm font-medium text-slate-700">密码</h2>
            <p class="mt-1 text-sm text-slate-500">用于登录当前账号</p>
          </div>
          <button v-if="!editingPassword" type="button" class="btn-secondary px-4 py-2 text-sm" @click="openPasswordEdit">修改密码</button>
        </div>
        <div v-if="editingPassword" class="mt-3 grid gap-3 sm:grid-cols-2">
          <div>
            <label class="mb-1.5 block text-xs text-slate-500">原密码</label>
            <input class="input" type="password" v-model="oldPassword" autocomplete="current-password" />
          </div>
          <div>
            <label class="mb-1.5 block text-xs text-slate-500">新密码</label>
            <input class="input" type="password" v-model="newPassword" autocomplete="new-password" minlength="6" />
          </div>
        </div>
        <div v-if="editingPassword" class="mt-3 flex gap-2">
          <button class="btn-primary px-4" @click="changePassword" :disabled="savingPassword || !oldPassword || newPassword.length < 6">
            {{ savingPassword ? '修改中...' : '保存' }}
          </button>
          <button type="button" class="btn-secondary px-4" @click="cancelPasswordEdit">取消</button>
        </div>
        <p v-if="passwordMsg" class="mt-2 text-xs" :class="passwordMsgOk ? 'text-emerald-600' : 'text-red-500'">{{ passwordMsg }}</p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../api'

const router = useRouter()
const authStore = useAuthStore()

const name = ref('')
const oldPassword = ref('')
const newPassword = ref('')
const editingName = ref(false)
const editingPassword = ref(false)
const savingName = ref(false)
const savingPassword = ref(false)
const nameMsg = ref('')
const nameMsgOk = ref(false)
const passwordMsg = ref('')
const passwordMsgOk = ref(false)

const profileItems = computed(() => [
  { label: '账号', value: authStore.user?.email },
  { label: '手机号', value: authStore.user?.phone },
  { label: '性别', value: authStore.user?.gender },
  { label: '出生日期', value: authStore.user?.birthDate },
  { label: '角色', value: authStore.isAdmin ? '管理员' : '普通用户' },
  { label: '状态', value: authStore.user?.isActive === false ? '禁用' : '正常' }
])

onMounted(async () => {
  await authStore.fetchMe()
  if (!authStore.isLoggedIn) {
    router.replace('/login?from=/account')
    return
  }
  name.value = authStore.user?.name || ''
})

function openNameEdit() {
  name.value = authStore.user?.name || ''
  nameMsg.value = ''
  editingName.value = true
}

function cancelNameEdit() {
  name.value = authStore.user?.name || ''
  nameMsg.value = ''
  editingName.value = false
}

function openPasswordEdit() {
  oldPassword.value = ''
  newPassword.value = ''
  passwordMsg.value = ''
  editingPassword.value = true
}

function cancelPasswordEdit() {
  oldPassword.value = ''
  newPassword.value = ''
  passwordMsg.value = ''
  editingPassword.value = false
}

async function saveName() {
  if (!name.value.trim()) return
  savingName.value = true
  nameMsg.value = ''
  try {
    await api.put('/api/user/profile', { name: name.value.trim() })
    nameMsg.value = '昵称修改成功'
    nameMsgOk.value = true
    await authStore.fetchMe()
    editingName.value = false
  } catch (err) {
    nameMsg.value = err.message || '昵称修改失败'
    nameMsgOk.value = false
  } finally {
    savingName.value = false
  }
}

async function changePassword() {
  if (!oldPassword.value || newPassword.value.length < 6) return
  savingPassword.value = true
  passwordMsg.value = ''
  try {
    await api.put('/api/user/password', {
      oldPassword: oldPassword.value,
      newPassword: newPassword.value
    })
    oldPassword.value = ''
    newPassword.value = ''
    passwordMsg.value = '密码修改成功'
    passwordMsgOk.value = true
    editingPassword.value = false
  } catch (err) {
    passwordMsg.value = err.message || '密码修改失败'
    passwordMsgOk.value = false
  } finally {
    savingPassword.value = false
  }
}
</script>
