<template>
  <section class="mx-auto max-w-md">
    <div class="mb-8 text-center">
      <h1 class="text-2xl font-bold bg-gradient-to-r from-violet-600 via-indigo-500 to-blue-500 bg-clip-text text-transparent">
        {{ mode === 'login' ? '欢迎回来' : '创建账户' }}
      </h1>
      <p class="mt-2 text-sm text-slate-500">
        {{ mode === 'login' ? '登录后可保存测试记录' : '注册一个新账户开始探索' }}
      </p>
    </div>

    <div class="card p-8">
      <!-- Mode tabs -->
      <div class="mb-6 flex border-b border-white/40">
        <button
          v-for="m in modes"
          :key="m.value"
          type="button"
          :class="[
            'relative -mb-px px-5 py-2.5 text-sm font-medium transition-colors',
            mode === m.value ? 'text-slate-900' : 'text-slate-400 hover:text-slate-600'
          ]"
          @click="mode = m.value"
        >
          {{ m.label }}
          <span
            v-if="mode === m.value"
            class="absolute bottom-0 left-0 right-0 h-0.5 bg-gradient-to-r from-violet-500 to-indigo-500 rounded-full"
          ></span>
        </button>
      </div>

      <!-- Admin tip -->
      <p v-if="mode === 'register'" class="mb-6 text-xs text-slate-400 leading-relaxed">
        注册后默认为普通用户，管理员可在后台维护用户资料。
      </p>

      <!-- Error -->
      <div v-if="error" class="mb-6 rounded-xl border border-red-200/60 bg-red-50/80 px-4 py-3 text-sm text-red-600 backdrop-blur-sm">
        {{ error }}
      </div>

      <!-- Form -->
      <form class="space-y-5" @submit.prevent="onSubmit">
        <div v-if="mode === 'register'">
          <label class="mb-1.5 block text-sm font-medium text-slate-700">姓名</label>
          <input class="input" placeholder="请输入姓名" v-model="name" maxlength="50" />
        </div>
        <div>
          <label class="mb-1.5 block text-sm font-medium text-slate-700">账号</label>
          <input class="input" placeholder="请输入账号" type="text" v-model="email" required />
        </div>
        <div>
          <label class="mb-1.5 block text-sm font-medium text-slate-700">密码</label>
          <input class="input" placeholder="至少 6 位" type="password" v-model="password" required />
        </div>
        <template v-if="mode === 'register'">
          <div>
            <label class="mb-1.5 block text-sm font-medium text-slate-700">手机号</label>
            <input class="input" placeholder="请输入手机号" type="tel" v-model="phone" maxlength="20" />
          </div>

          <div>
            <label class="mb-1.5 block text-sm font-medium text-slate-700">性别</label>
            <div class="flex gap-4 rounded-xl border border-white/60 bg-white/50 px-4 py-3">
              <label class="inline-flex items-center gap-2 text-sm text-slate-700">
                <input type="radio" value="男" v-model="gender" />
                男
              </label>
              <label class="inline-flex items-center gap-2 text-sm text-slate-700">
                <input type="radio" value="女" v-model="gender" />
                女
              </label>
            </div>
          </div>

          <div>
            <label class="mb-1.5 block text-sm font-medium text-slate-700">出生日期</label>
            <input class="input" type="date" v-model="birthDate" />
          </div>
        </template>
        <button :disabled="loading" class="btn-primary w-full py-3" type="submit">
          {{ loading ? '处理中...' : mode === 'login' ? '登录' : '注册并登录' }}
        </button>
      </form>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const modes = [
  { value: 'login', label: '登录' },
  { value: 'register', label: '注册' }
]

const mode = ref('login')
const email = ref('')
const name = ref('')
const password = ref('')
const phone = ref('')
const gender = ref('男')
const birthDate = ref('')
const error = ref('')
const loading = ref(false)

const from = route.query.from || '/'

async function onSubmit() {
  loading.value = true
  error.value = ''
  try {
    if (mode.value === 'login') {
      await authStore.login(email.value, password.value)
    } else {
      await authStore.register(email.value, password.value, {
        name: name.value || undefined,
        phone: phone.value || undefined,
        gender: gender.value || undefined,
        birthDate: birthDate.value || undefined
      })
    }
    router.push(from)
  } catch (err) {
    error.value = err.message || '操作失败'
  } finally {
    loading.value = false
  }
}
</script>
