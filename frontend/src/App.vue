<template>
  <div class="min-h-screen relative overflow-x-hidden text-slate-900 antialiased">
    <!-- Decorative gradient blobs -->
    <div class="pointer-events-none fixed inset-0 z-0" aria-hidden="true">
      <div class="absolute -top-40 -left-40 h-96 w-96 rounded-full bg-violet-300/30 blur-[100px] animate-float"></div>
      <div class="absolute top-1/4 -right-32 h-80 w-80 rounded-full bg-blue-300/25 blur-[80px] animate-float" style="animation-delay: 2s"></div>
      <div class="absolute bottom-1/4 -left-20 h-72 w-72 rounded-full bg-pink-300/20 blur-[90px] animate-float" style="animation-delay: 4s"></div>
      <div class="absolute -bottom-20 right-1/4 h-64 w-64 rounded-full bg-emerald-300/15 blur-[70px] animate-float" style="animation-delay: 1s"></div>
    </div>

    <div class="relative z-10 mx-auto max-w-[1500px] px-4 py-8 sm:px-6">
      <div class="grid gap-5 lg:grid-cols-[200px_minmax(0,1fr)]">
        <TopNav />
        <main class="min-w-0 animate-fade-in">
          <router-view />
        </main>
      </div>
      <footer class="mt-16 border-t border-white/40 pt-8 text-center text-xs text-slate-400">
        MBTI 性格测试系统 &copy; {{ new Date().getFullYear() }}
      </footer>
    </div>
  </div>
</template>

<script setup>
import TopNav from './components/TopNav.vue'
</script>
