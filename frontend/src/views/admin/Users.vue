<template>
  <section class="space-y-8">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="section-title">用户管理</h1>
        <p class="mt-1 text-sm text-slate-400">管理系统用户</p>
      </div>
      <button class="btn-primary" @click="showCreateUser">新增用户</button>
    </div>

    <div v-if="loading" class="flex justify-center py-12">
      <div class="h-8 w-8 animate-spin rounded-full border-3 border-violet-200 border-t-violet-600"></div>
    </div>

    <div v-else class="space-y-4">
      <div v-for="(u, i) in users" :key="u.id" class="card p-5 animate-slide-up" :style="{ animationDelay: `${i * 30}ms` }">
        <div class="grid gap-5 xl:grid-cols-[minmax(260px,1fr)_minmax(360px,1.1fr)_260px] xl:items-center">
          <div class="flex min-w-0 items-center gap-4">
            <div class="flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-violet-500 to-indigo-500 text-sm font-bold text-white">
              {{ (u.name || u.email).charAt(0).toUpperCase() }}
            </div>
            <div class="min-w-0 flex-1">
              <p class="truncate font-medium text-slate-900">{{ u.name || '未设置昵称' }}</p>
              <p class="truncate text-xs text-slate-400">{{ u.email }}</p>
              <div class="mt-2 flex flex-wrap gap-2">
                <span :class="u.role === 'ADMIN' ? 'badge-primary' : 'badge-muted'">{{ u.role === 'ADMIN' ? '管理员' : '用户' }}</span>
                <span
                  :class="[
                    'inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-semibold ring-1',
                    u.isActive
                      ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20'
                      : 'bg-amber-50 text-amber-700 ring-amber-600/20'
                  ]"
                >
                  <span :class="['h-1.5 w-1.5 rounded-full', u.isActive ? 'bg-emerald-500' : 'bg-amber-500']"></span>
                  {{ u.isActive ? '正常' : '禁用' }}
                </span>
              </div>
            </div>
          </div>

          <div class="grid gap-3 rounded-xl border border-white/50 bg-white/35 p-3 text-sm sm:grid-cols-3">
            <div>
              <p class="text-xs text-slate-400">手机号</p>
              <p class="mt-1 truncate text-slate-700">{{ u.phone || '-' }}</p>
            </div>
            <div>
              <p class="text-xs text-slate-400">性别</p>
              <p class="mt-1 text-slate-700">{{ u.gender || '-' }}</p>
            </div>
            <div>
              <p class="text-xs text-slate-400">出生日期</p>
              <p class="mt-1 text-slate-700">{{ u.birthDate || '-' }}</p>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-2 xl:justify-self-end">
            <button class="btn-secondary px-3 py-2 text-xs" @click="showEditUser(u)">编辑资料</button>
            <button class="btn-secondary px-3 py-2 text-xs" @click="resetPassword(u.id)">重置密码</button>
            <button class="btn-secondary px-3 py-2 text-xs" @click="showChangePw(u.id)">改密码</button>
            <button class="btn-danger px-3 py-2 text-xs" @click="deleteUser(u.id)" v-if="u.id !== authStore.user?.id">删除</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit user modal -->
    <div v-if="editingUser || creatingUser" class="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm" @click.self="closeEditUser">
      <div class="card p-8 max-w-lg w-full mx-4">
        <h2 class="text-lg font-semibold mb-6">{{ creatingUser ? '新增用户' : '编辑用户资料' }}</h2>
        <form @submit.prevent="saveUserProfile" class="space-y-4">
          <div v-if="creatingUser" class="grid gap-4 sm:grid-cols-2">
            <div>
              <label class="mb-1.5 block text-sm font-medium text-slate-700">账号</label>
              <input class="input" v-model="editForm.email" placeholder="请输入账号" required />
            </div>
            <div>
              <label class="mb-1.5 block text-sm font-medium text-slate-700">初始密码</label>
              <input class="input" v-model="editForm.password" type="password" placeholder="至少 6 位" required minlength="6" />
            </div>
          </div>

          <div class="grid gap-4 sm:grid-cols-2">
            <div>
              <label class="mb-1.5 block text-sm font-medium text-slate-700">姓名</label>
              <input class="input" v-model="editForm.name" maxlength="50" placeholder="请输入姓名" />
            </div>
            <div>
              <label class="mb-1.5 block text-sm font-medium text-slate-700">手机号</label>
              <input class="input" v-model="editForm.phone" maxlength="20" placeholder="请输入手机号" />
            </div>
          </div>

          <div>
            <label class="mb-1.5 block text-sm font-medium text-slate-700">性别</label>
            <div class="flex gap-4 rounded-xl border border-white/60 bg-white/50 px-4 py-3">
              <label class="inline-flex items-center gap-2 text-sm text-slate-700">
                <input type="radio" value="男" v-model="editForm.gender" />
                男
              </label>
              <label class="inline-flex items-center gap-2 text-sm text-slate-700">
                <input type="radio" value="女" v-model="editForm.gender" />
                女
              </label>
            </div>
          </div>

          <div>
            <label class="mb-1.5 block text-sm font-medium text-slate-700">出生日期</label>
            <input class="input" type="date" v-model="editForm.birthDate" />
          </div>

          <div v-if="creatingUser">
            <label class="mb-1.5 block text-sm font-medium text-slate-700">角色</label>
            <div class="flex gap-4 rounded-xl border border-white/60 bg-white/50 px-4 py-3">
              <label class="inline-flex items-center gap-2 text-sm text-slate-700">
                <input type="radio" value="USER" v-model="editForm.role" />
                普通用户
              </label>
              <label class="inline-flex items-center gap-2 text-sm text-slate-700">
                <input type="radio" value="ADMIN" v-model="editForm.role" />
                管理员
              </label>
            </div>
          </div>

          <div class="flex items-center justify-between rounded-xl border border-white/60 bg-white/50 px-4 py-3">
            <div>
              <p class="text-sm font-medium text-slate-700">状态</p>
              <p :class="['mt-0.5 text-xs', editForm.isActive ? 'text-emerald-600' : 'text-amber-600']">
                {{ editForm.isActive ? '正常' : '禁用' }}
              </p>
            </div>
            <div class="flex gap-4">
              <label class="inline-flex items-center gap-2 text-sm text-slate-700">
                <input type="radio" :value="true" v-model="editForm.isActive" />
                正常
              </label>
              <label class="inline-flex items-center gap-2 text-sm text-slate-700">
                <input type="radio" :value="false" v-model="editForm.isActive" />
                禁用
              </label>
            </div>
          </div>

          <div class="flex gap-3 pt-4">
            <button type="submit" class="btn-primary flex-1" :disabled="savingUser">{{ savingUser ? '保存中...' : creatingUser ? '新增' : '保存' }}</button>
            <button type="button" class="btn-secondary flex-1" @click="closeEditUser">取消</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Change password modal -->
    <div v-if="changePwUser" class="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm" @click.self="changePwUser = null">
      <div class="card p-8 max-w-sm w-full mx-4">
        <h2 class="text-lg font-semibold mb-6">修改密码</h2>
        <form @submit.prevent="changePassword" class="space-y-4">
          <div>
            <label class="mb-1.5 block text-sm font-medium text-slate-700">新密码</label>
            <input class="input" v-model="newPassword" type="password" placeholder="至少 6 位" required minlength="6" />
          </div>
          <div class="flex gap-3">
            <button type="submit" class="btn-primary flex-1">确认</button>
            <button type="button" class="btn-secondary flex-1" @click="changePwUser = null">取消</button>
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
const users = ref([])
const changePwUser = ref(null)
const newPassword = ref('')
const editingUser = ref(null)
const creatingUser = ref(false)
const savingUser = ref(false)
const editForm = ref({
  email: '',
  password: '',
  role: 'USER',
  name: '',
  phone: '',
  gender: '男',
  birthDate: '',
  isActive: true
})

onMounted(async () => {
  await authStore.fetchMe()
  if (!authStore.isAdmin) { router.replace('/'); return }
  await loadUsers()
})

async function loadUsers() {
  loading.value = true
  try {
    const res = await api.get('/api/admin/users')
    users.value = res.data.users || []
  } catch { /* ignore */ } finally { loading.value = false }
}

async function resetPassword(userId) {
  if (!confirm('确定重置该用户密码为 123456？')) return
  await api.put('/api/admin/users', { userId, action: 'resetPassword' })
  alert('密码已重置为 123456')
}

function showChangePw(userId) {
  changePwUser.value = userId
  newPassword.value = ''
}

function showCreateUser() {
  creatingUser.value = true
  editingUser.value = null
  editForm.value = defaultEditForm()
}

function showEditUser(user) {
  creatingUser.value = false
  editingUser.value = user.id
  editForm.value = {
    email: user.email || '',
    password: '',
    role: user.role || 'USER',
    name: user.name || '',
    phone: user.phone || '',
    gender: user.gender || '男',
    birthDate: user.birthDate || '',
    isActive: user.isActive !== false
  }
}

function closeEditUser() {
  editingUser.value = null
  creatingUser.value = false
  editForm.value = defaultEditForm()
}

async function saveUserProfile() {
  savingUser.value = true
  try {
    if (creatingUser.value) {
      await api.post('/api/admin/users', {
        email: editForm.value.email,
        password: editForm.value.password,
        role: editForm.value.role,
        name: editForm.value.name || null,
        phone: editForm.value.phone || null,
        gender: editForm.value.gender || null,
        birthDate: editForm.value.birthDate || null,
        isActive: editForm.value.isActive
      })
    } else {
      await api.put('/api/admin/users', {
        userId: editingUser.value,
        action: 'updateProfile',
        name: editForm.value.name || null,
        phone: editForm.value.phone || null,
        gender: editForm.value.gender || null,
        birthDate: editForm.value.birthDate || null,
        isActive: editForm.value.isActive
      })
    }
    closeEditUser()
    await loadUsers()
  } catch (err) {
    alert(err.message || (creatingUser.value ? '新增用户失败' : '保存用户资料失败'))
  } finally {
    savingUser.value = false
  }
}

function defaultEditForm() {
  return {
    email: '',
    password: '',
    role: 'USER',
    name: '',
    phone: '',
    gender: '男',
    birthDate: '',
    isActive: true
  }
}

async function changePassword() {
  await api.put('/api/admin/users', { userId: changePwUser.value, action: 'changePassword', newPassword: newPassword.value })
  changePwUser.value = null
  alert('密码修改成功')
}

async function deleteUser(userId) {
  if (!confirm('确定删除该用户？')) return
  try {
    await api.delete('/api/admin/users', { data: { userId } })
    await loadUsers()
  } catch (err) {
    alert(err.message || '删除用户失败')
  }
}
</script>
