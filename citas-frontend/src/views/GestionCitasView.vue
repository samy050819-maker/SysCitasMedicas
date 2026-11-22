<template>
  <div class="max-w-7xl mx-auto py-8 px-4 sm:px-6">
    <div class="mb-8 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-slate-900 flex items-center gap-2">
          <span>📋</span> Gestión y Cancelación de Citas
        </h1>
        <p class="text-sm text-slate-500 mt-1">
          HU02 & HU09: Cancelación individual y cancelación masiva de citas por médico y fecha (FORALL + BULK COLLECT).
        </p>
      </div>

      <!-- Button to toggle massive cancellation drawer/modal -->
      <button
        @click="mostrarModalMasiva = true"
        class="px-4 py-2.5 rounded-lg bg-rose-600 hover:bg-rose-700 text-white text-sm font-semibold shadow-xs flex items-center gap-2 self-start transition"
      >
        <span>⚠️</span> Cancelación Masiva por Médico
      </button>
    </div>

    <!-- Alert Notifications -->
    <AlertBanner
      :visible="alert.visible"
      :type="alert.type"
      :title="alert.title"
      :message="alert.message"
      :oracle-code="alert.oracleCode"
      @close="alert.visible = false"
    />

    <!-- Filter Bar -->
    <div class="bg-white p-4 rounded-xl border border-slate-200 shadow-xs mb-6 flex flex-wrap items-center justify-between gap-4">
      <div class="flex items-center gap-3">
        <label class="text-xs font-semibold uppercase tracking-wider text-slate-600">
          Filtrar por Estado:
        </label>
        <select
          v-model="filtroEstado"
          @change="cargarCitas"
          class="px-3 py-1.5 rounded-lg border border-slate-300 text-sm focus:ring-2 focus:ring-clinical-500 bg-white"
        >
          <option value="">Todos los estados</option>
          <option v-for="est in estadosCita" :key="est.idParametro" :value="est.idParametro">
            {{ est.valor }} ({{ est.idParametro }})
          </option>
        </select>
      </div>

      <div class="flex items-center gap-2">
        <input
          v-model="busquedaTexto"
          type="text"
          placeholder="Buscar paciente, médico..."
          class="px-3 py-1.5 rounded-lg border border-slate-300 text-sm focus:ring-2 focus:ring-clinical-500 w-64"
        />
        <button
          @click="cargarCitas"
          class="px-3 py-1.5 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 text-sm font-medium transition"
          title="Recargar listado"
        >
          🔄 Actualizar
        </button>
      </div>
    </div>

    <!-- Table of Appointments -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-left text-sm text-slate-600">
          <thead class="bg-slate-50 text-xs font-semibold text-slate-500 uppercase tracking-wider border-b border-slate-200">
            <tr>
              <th class="px-6 py-3.5">ID Cita</th>
              <th class="px-6 py-3.5">Paciente</th>
              <th class="px-6 py-3.5">Médico / Especialidad</th>
              <th class="px-6 py-3.5">Fecha y Horario</th>
              <th class="px-6 py-3.5">Estado</th>
              <th class="px-6 py-3.5 text-right">Acciones</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-if="cargando">
              <td colspan="6" class="px-6 py-8 text-center text-slate-400">
                <span class="animate-pulse">Cargando citas desde Oracle Database...</span>
              </td>
            </tr>
            <tr v-else-if="citasFiltradas.length === 0">
              <td colspan="6" class="px-6 py-8 text-center text-slate-400">
                No se encontraron citas con los filtros aplicados.
              </td>
            </tr>
            <tr
              v-for="cita in citasFiltradas"
              :key="cita.idCita"
              class="hover:bg-slate-50/80 transition"
            >
              <td class="px-6 py-4 font-mono font-medium text-slate-900">
                #{{ cita.idCita }}
              </td>
              <td class="px-6 py-4 font-semibold text-slate-800">
                {{ cita.nombrePaciente }}
              </td>
              <td class="px-6 py-4">
                <div class="text-slate-800 font-medium">{{ cita.nombreMedico }}</div>
                <div class="text-xs text-slate-400">{{ cita.especialidad }}</div>
              </td>
              <td class="px-6 py-4">
                <div class="font-medium text-slate-700">{{ formatearFecha(cita.fechaHoraInicio) }}</div>
                <div class="text-xs text-slate-400">
                  {{ formatearHora(cita.fechaHoraInicio) }} - {{ formatearHora(cita.fechaHoraFin) }}
                </div>
              </td>
              <td class="px-6 py-4">
                <span :class="badgeClass(cita.idEstado)" class="px-2.5 py-1 rounded-full text-xs font-semibold inline-block">
                  {{ cita.estado }}
                </span>
              </td>
              <td class="px-6 py-4 text-right">
                <button
                  v-if="cita.idEstado === 501 || cita.idEstado === 502"
                  @click="cancelarCitaIndividual(cita.idCita)"
                  :disabled="cancelandoId === cita.idCita"
                  class="px-3 py-1 rounded-lg border border-rose-300 text-rose-700 hover:bg-rose-50 text-xs font-medium transition disabled:opacity-50"
                  title="Cancelar cita (PRC_CANCELAR_CITA)"
                >
                  {{ cancelandoId === cita.idCita ? 'Cancelando...' : 'Cancelar Cita' }}
                </button>
                <span v-else class="text-xs text-slate-400 italic">Sin acciones</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="px-6 py-3 bg-slate-50 border-t border-slate-200 text-xs text-slate-500 flex justify-between items-center">
        <span>Mostrando {{ citasFiltradas.length }} citas</span>
        <span>Total en vista: {{ citas.length }}</span>
      </div>
    </div>

    <!-- Modal: Cancelación Masiva -->
    <div
      v-if="mostrarModalMasiva"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-xs p-4"
    >
      <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full overflow-hidden border border-slate-200">
        <div class="bg-rose-600 px-6 py-4 text-white flex items-center justify-between">
          <div class="flex items-center gap-2">
            <span class="text-xl">🚨</span>
            <h3 class="font-bold text-base">Cancelación Masiva de Citas</h3>
          </div>
          <button @click="mostrarModalMasiva = false" class="text-white/80 hover:text-white text-lg">✕</button>
        </div>

        <form @submit.prevent="ejecutarCancelacionMasiva" class="p-6 space-y-4">
          <p class="text-xs text-slate-600 leading-relaxed">
            Ejecuta <span class="font-mono font-semibold">PKG_CITAS.PRC_CANCELACION_MASIVA</span> mediante
            <span class="font-mono">FORALL</span> y <span class="font-mono">BULK COLLECT</span>. Cancela todas las citas
            (501/502 → 504) del médico en la fecha elegida y actualiza los bloques de agenda a 403.
          </p>

          <div>
            <label class="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Médico *
            </label>
            <select
              v-model="masivaForm.idMedico"
              required
              class="w-full px-3 py-2 rounded-lg border border-slate-300 text-sm focus:ring-2 focus:ring-rose-500 bg-white"
            >
              <option value="" disabled>-- Seleccione médico --</option>
              <option v-for="m in medicos" :key="m.idMedico" :value="m.idMedico">
                Dr(a). {{ m.nombreCompleto }} ({{ m.especialidad }})
              </option>
            </select>
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Fecha de las Citas *
            </label>
            <input
              type="date"
              v-model="masivaForm.fecha"
              required
              class="w-full px-3 py-2 rounded-lg border border-slate-300 text-sm focus:ring-2 focus:ring-rose-500 bg-white"
            />
          </div>

          <div class="border-t border-slate-100 pt-4 flex justify-end gap-3">
            <button
              type="button"
              @click="mostrarModalMasiva = false"
              class="px-4 py-2 rounded-lg border border-slate-300 text-slate-700 hover:bg-slate-50 text-sm font-medium"
            >
              Cancelar
            </button>
            <button
              type="submit"
              :disabled="ejecutandoMasiva"
              class="px-5 py-2 rounded-lg bg-rose-600 hover:bg-rose-700 text-white text-sm font-semibold shadow-xs flex items-center gap-2"
            >
              <span v-if="ejecutandoMasiva" class="animate-spin inline-block">⏳</span>
              <span>{{ ejecutandoMasiva ? 'Procesando masivo...' : 'Ejecutar Cancelación' }}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import api from '../services/api'
import AlertBanner from '../components/AlertBanner.vue'

const citas = ref([])
const estadosCita = ref([])
const medicos = ref([])
const filtroEstado = ref('')
const busquedaTexto = ref('')
const cargando = ref(false)
const cancelandoId = ref(null)
const mostrarModalMasiva = ref(false)
const ejecutandoMasiva = ref(false)

const masivaForm = reactive({
  idMedico: '',
  fecha: '2026-10-15'
})

const alert = reactive({
  visible: false,
  type: 'info',
  title: '',
  message: '',
  oracleCode: null
})

const citasFiltradas = computed(() => {
  if (!busquedaTexto.value) return citas.value
  const q = busquedaTexto.value.toLowerCase()
  return citas.value.filter(c =>
    (c.nombrePaciente && c.nombrePaciente.toLowerCase().includes(q)) ||
    (c.nombreMedico && c.nombreMedico.toLowerCase().includes(q)) ||
    (c.especialidad && c.especialidad.toLowerCase().includes(q)) ||
    String(c.idCita).includes(q)
  )
})

onMounted(async () => {
  await Promise.all([cargarEstados(), cargarMedicos(), cargarCitas()])
})

async function cargarEstados() {
  try {
    const res = await api.get('/parametros/ESTADO_CITA')
    estadosCita.value = res.data
  } catch (err) {
    console.error('Error al cargar estados de cita', err)
  }
}

async function cargarMedicos() {
  try {
    const res = await api.get('/medicos')
    medicos.value = res.data
  } catch (err) {
    console.error('Error al cargar médicos', err)
  }
}

async function cargarCitas() {
  cargando.value = true
  try {
    const params = {}
    if (filtroEstado.value) params.idEstado = filtroEstado.value
    const res = await api.get('/citas', { params })
    citas.value = res.data
  } catch (err) {
    showAlert('error', 'Error al cargar citas', err.message)
  } finally {
    cargando.value = false
  }
}

async function cancelarCitaIndividual(idCita) {
  if (!confirm(`¿Confirma la cancelación de la cita #${idCita}?`)) return

  cancelandoId.value = idCita
  try {
    const res = await api.put(`/citas/${idCita}/cancelar`)
    showAlert('success', 'Cita Cancelada', res.data.mensaje || `Cita #${idCita} cancelada exitosamente en Oracle.`)
    await cargarCitas()
  } catch (err) {
    handleError(err, 'Error al cancelar la cita')
  } finally {
    cancelandoId.value = null
  }
}

async function ejecutarCancelacionMasiva() {
  if (!confirm('¿Confirma la cancelación masiva de citas para el médico y fecha especificados?')) return

  ejecutandoMasiva.value = true
  try {
    const res = await api.post('/citas/cancelacion-masiva', masivaForm)
    showAlert('success', 'Cancelación Masiva Exitosa', res.data.mensaje || 'Se cancelaron las citas del médico y se liberaron agendas.')
    mostrarModalMasiva.value = false
    await cargarCitas()
  } catch (err) {
    handleError(err, 'Error en cancelación masiva')
  } finally {
    ejecutandoMasiva.value = false
  }
}

function badgeClass(idEstado) {
  switch (idEstado) {
    case 501: // Programada
      return 'bg-blue-100 text-blue-800'
    case 502: // Confirmada
      return 'bg-emerald-100 text-emerald-800'
    case 503: // Atendida
      return 'bg-indigo-100 text-indigo-800'
    case 504: // Cancelada
      return 'bg-rose-100 text-rose-800'
    case 505: // No asistió
      return 'bg-slate-100 text-slate-800'
    default:
      return 'bg-slate-100 text-slate-700'
  }
}

function formatearFecha(fechaStr) {
  if (!fechaStr) return ''
  const d = new Date(fechaStr)
  return d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' })
}

function formatearHora(fechaStr) {
  if (!fechaStr) return ''
  const d = new Date(fechaStr)
  return d.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' })
}

function showAlert(type, title, message, oracleCode = null) {
  alert.type = type
  alert.title = title
  alert.message = message
  alert.oracleCode = oracleCode
  alert.visible = true
}

function handleError(err, defaultTitle) {
  if (err.response?.data) {
    const { codigoOracle, mensaje } = err.response.data
    showAlert('error', defaultTitle, mensaje || 'Error en la operación', codigoOracle)
  } else {
    showAlert('error', defaultTitle, err.message)
  }
}
</script>
