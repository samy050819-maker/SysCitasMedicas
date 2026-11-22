<template>
  <header class="bg-white border-b border-slate-200 sticky top-0 z-40 shadow-xs">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Logo and Brand -->
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-tr from-clinical-700 to-clinical-500 flex items-center justify-center text-white font-bold text-xl shadow-md">
            +
          </div>
          <div>
            <span class="text-lg font-bold bg-gradient-to-r from-clinical-800 to-clinical-600 bg-clip-text text-transparent">
              MediCitas
            </span>
            <span class="hidden sm:inline-block ml-2 text-xs uppercase tracking-wider px-2 py-0.5 rounded-full bg-slate-100 text-slate-600 font-semibold">
              Oracle 21c XE
            </span>
          </div>
        </div>

        <!-- Navigation Links -->
        <nav class="hidden md:flex items-center gap-1">
          <RouterLink
            to="/agendar"
            v-slot="{ isActive }"
            class="px-3 py-2 rounded-lg text-sm font-medium transition-colors"
            :class="isActive ? 'bg-clinical-50 text-clinical-700 font-semibold' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'"
          >
            📅 Agendar Cita
          </RouterLink>
          <RouterLink
            to="/gestion"
            v-slot="{ isActive }"
            class="px-3 py-2 rounded-lg text-sm font-medium transition-colors"
            :class="isActive ? 'bg-clinical-50 text-clinical-700 font-semibold' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'"
          >
            📋 Gestión Citas
          </RouterLink>
          <RouterLink
            to="/historias"
            v-slot="{ isActive }"
            class="px-3 py-2 rounded-lg text-sm font-medium transition-colors"
            :class="isActive ? 'bg-clinical-50 text-clinical-700 font-semibold' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'"
          >
            🩺 Historias & VPD
          </RouterLink>
          <RouterLink
            to="/farmacia"
            v-slot="{ isActive }"
            class="px-3 py-2 rounded-lg text-sm font-medium transition-colors"
            :class="isActive ? 'bg-clinical-50 text-clinical-700 font-semibold' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'"
          >
            💊 Farmacia
          </RouterLink>
          <RouterLink
            to="/reportes"
            v-slot="{ isActive }"
            class="px-3 py-2 rounded-lg text-sm font-medium transition-colors"
            :class="isActive ? 'bg-clinical-50 text-clinical-700 font-semibold' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'"
          >
            📊 Reportes
          </RouterLink>
        </nav>

        <!-- Right Side: Active Doctor (VPD Session) Indicator -->
        <div class="flex items-center gap-2">
          <div class="hidden lg:flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-lg px-3 py-1.5">
            <span class="w-2.5 h-2.5 rounded-full" :class="activeDoctor ? 'bg-emerald-500 animate-pulse' : 'bg-slate-400'"></span>
            <div class="text-xs">
              <span class="text-slate-400 block font-medium">Médico Activo (VPD):</span>
              <span class="font-semibold text-slate-700">
                {{ activeDoctor ? activeDoctor : 'Sin sesión (Ver todo)' }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Mobile Nav -->
    <div class="md:hidden border-t border-slate-200 px-4 py-2 flex items-center justify-between overflow-x-auto text-xs gap-1">
      <RouterLink to="/agendar" class="px-2 py-1 rounded text-slate-700 hover:bg-slate-100 whitespace-nowrap">📅 Agendar</RouterLink>
      <RouterLink to="/gestion" class="px-2 py-1 rounded text-slate-700 hover:bg-slate-100 whitespace-nowrap">📋 Citas</RouterLink>
      <RouterLink to="/historias" class="px-2 py-1 rounded text-slate-700 hover:bg-slate-100 whitespace-nowrap">🩺 Historias</RouterLink>
      <RouterLink to="/farmacia" class="px-2 py-1 rounded text-slate-700 hover:bg-slate-100 whitespace-nowrap">💊 Farmacia</RouterLink>
      <RouterLink to="/reportes" class="px-2 py-1 rounded text-slate-700 hover:bg-slate-100 whitespace-nowrap">📊 Reportes</RouterLink>
    </div>
  </header>
</template>

<script setup>
import { activeDoctor } from '../services/state'
</script>
