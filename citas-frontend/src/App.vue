<template>
  <div class="min-h-screen flex flex-col bg-slate-50 text-slate-800">
    <Navbar />

    <main class="flex-1">
      <RouterView />
    </main>

    <footer class="bg-white border-t border-slate-200 py-6 text-center text-xs text-slate-500">
      <div class="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-2">
        <p>
          Sistema de Citas Médicas &copy; 2026 — Oracle PL/SQL (Packages, Triggers, VPD) + Spring Boot 3 + Vue 3
        </p>
        <p class="font-mono text-[11px] text-slate-400">
          HikariCP CLIENT_IDENTIFIER Enabled
        </p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import Navbar from './components/Navbar.vue'
</script>
