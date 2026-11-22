<template>
  <div
    v-if="visible"
    :class="bannerClasses"
    class="rounded-lg p-4 mb-6 shadow-sm border transition-all duration-300 flex items-start justify-between"
    role="alert"
  >
    <div class="flex items-start gap-3">
      <!-- Icon based on type -->
      <span class="text-xl shrink-0 mt-0.5">
        <span v-if="type === 'success'">✅</span>
        <span v-else-if="type === 'error'">🚨</span>
        <span v-else-if="type === 'warning'">⚠️</span>
        <span v-else>ℹ️</span>
      </span>
      <div>
        <h4 v-if="title" class="font-semibold text-sm">{{ title }}</h4>
        <p class="text-sm mt-0.5 whitespace-pre-line">{{ message }}</p>
        <span
          v-if="oracleCode"
          class="inline-block mt-2 px-2 py-0.5 rounded text-xs font-mono font-bold bg-white/70 border border-current shadow-xs"
        >
          Código Oracle: ORA-{{ oracleCode }}
        </span>
      </div>
    </div>
    <button
      @click="dismiss"
      class="text-slate-400 hover:text-slate-600 text-lg leading-none p-1 ml-4"
      aria-label="Cerrar"
    >
      ✕
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  type: { type: String, default: 'info' }, // 'success' | 'error' | 'warning' | 'info'
  title: { type: String, default: '' },
  message: { type: String, default: '' },
  oracleCode: { type: [Number, String], default: null }
})

const emit = defineEmits(['close'])

const bannerClasses = computed(() => {
  switch (props.type) {
    case 'success':
      return 'bg-emerald-50 text-emerald-800 border-emerald-200'
    case 'error':
      return 'bg-rose-50 text-rose-800 border-rose-200'
    case 'warning':
      return 'bg-amber-50 text-amber-800 border-amber-200'
    default:
      return 'bg-sky-50 text-sky-800 border-sky-200'
  }
})

function dismiss() {
  emit('close')
}
</script>
