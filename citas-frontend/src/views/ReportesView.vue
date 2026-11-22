<template>
  <div class="max-w-7xl mx-auto py-8 px-4 sm:px-6">
    <div class="mb-8 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-slate-900 flex items-center gap-2">
          <span>📊</span> Dashboard de Reportes Gerenciales
        </h1>
        <p class="text-sm text-slate-500 mt-1">
          HU10: Métricas de atención, tasas de cancelación y productividad médica consumiendo PKG_REPORTES.PRC_REPORTE_MEDICOS_CURSOR (SYS_REFCURSOR).
        </p>
      </div>

      <!-- Date Filter Controls -->
      <div class="flex items-center gap-2 bg-white p-2 rounded-xl border border-slate-200 shadow-xs">
        <div>
          <label class="block text-[10px] font-bold uppercase tracking-wider text-slate-500">Desde</label>
          <input
            type="date"
            v-model="fechaInicio"
            class="text-xs px-2 py-1 rounded border border-slate-300 focus:ring-1 focus:ring-clinical-500"
          />
        </div>
        <div>
          <label class="block text-[10px] font-bold uppercase tracking-wider text-slate-500">Hasta</label>
          <input
            type="date"
            v-model="fechaFin"
            class="text-xs px-2 py-1 rounded border border-slate-300 focus:ring-1 focus:ring-clinical-500"
          />
        </div>
        <button
          @click="cargarReportes"
          :disabled="cargando"
          class="self-end px-3 py-1.5 rounded-lg bg-clinical-600 hover:bg-clinical-700 text-white text-xs font-semibold transition"
        >
          {{ cargando ? 'Cargando...' : 'Filtrar' }}
        </button>
      </div>
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

    <!-- KPI Cards Grid -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
      <!-- Total Citas -->
      <div class="bg-white rounded-2xl p-6 border border-slate-200 shadow-xs">
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-slate-500 uppercase tracking-wider">Total Citas</span>
          <span class="p-2 rounded-xl bg-blue-50 text-blue-600 text-lg">📅</span>
        </div>
        <div class="mt-4 text-3xl font-extrabold text-slate-900">{{ kpis.totalCitas }}</div>
        <div class="mt-1 text-xs text-slate-400">En el período consultado</div>
      </div>

      <!-- Citas Atendidas -->
      <div class="bg-white rounded-2xl p-6 border border-slate-200 shadow-xs">
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-slate-500 uppercase tracking-wider">Atendidas</span>
          <span class="p-2 rounded-xl bg-emerald-50 text-emerald-600 text-lg">✅</span>
        </div>
        <div class="mt-4 text-3xl font-extrabold text-emerald-600">{{ kpis.atendidas }}</div>
        <div class="mt-1 text-xs text-slate-400">
          {{ kpis.totalCitas ? ((kpis.atendidas / kpis.totalCitas) * 100).toFixed(1) : 0 }}% de efectividad
        </div>
      </div>

      <!-- Citas Canceladas -->
      <div class="bg-white rounded-2xl p-6 border border-slate-200 shadow-xs">
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-slate-500 uppercase tracking-wider">Canceladas</span>
          <span class="p-2 rounded-xl bg-rose-50 text-rose-600 text-lg">❌</span>
        </div>
        <div class="mt-4 text-3xl font-extrabold text-rose-600">{{ kpis.canceladas }}</div>
        <div class="mt-1 text-xs text-slate-400">
          Tasa global: <span class="font-bold text-rose-700">{{ kpis.porcentajeCancelacionGlobal }}%</span>
        </div>
      </div>

      <!-- Programadas y Confirmadas -->
      <div class="bg-white rounded-2xl p-6 border border-slate-200 shadow-xs">
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-slate-500 uppercase tracking-wider">Pendientes</span>
          <span class="p-2 rounded-xl bg-amber-50 text-amber-600 text-lg">⏳</span>
        </div>
        <div class="mt-4 text-3xl font-extrabold text-amber-600">{{ kpis.pendientes }}</div>
        <div class="mt-1 text-xs text-slate-400">Programadas + Confirmadas</div>
      </div>
    </div>

    <!-- Chart: Bar Chart Comparison -->
    <div class="bg-white rounded-2xl border border-slate-200 shadow-xs p-6 mb-8">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h2 class="font-bold text-base text-slate-900">Comparativa de Citas por Médico</h2>
          <p class="text-xs text-slate-500">Desglose de citas Atendidas vs. Canceladas por cada profesional</p>
        </div>
        <span class="text-xs font-semibold text-slate-400 bg-slate-100 px-2.5 py-1 rounded-md">
          Chart.js 4.4
        </span>
      </div>

      <div class="relative h-80 w-full">
        <canvas ref="chartCanvas"></canvas>
      </div>
    </div>

    <!-- Table: Detailed Doctors Report -->
    <div class="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
      <div class="px-6 py-4 border-b border-slate-200 bg-slate-50 flex items-center justify-between">
        <div>
          <h2 class="font-bold text-sm text-slate-800">Detalle de Actividad Médica</h2>
          <p class="text-xs text-slate-400">Datos obtenidos directamente desde el cursor Oracle PKG_REPORTES</p>
        </div>
        <span class="text-xs font-semibold text-clinical-700 bg-clinical-50 border border-clinical-200 px-2.5 py-1 rounded-lg">
          {{ medicosReporte.length }} Médicos reportados
        </span>
      </div>

      <div class="overflow-x-auto">
        <table class="w-full text-left text-sm text-slate-600">
          <thead class="bg-slate-50 text-xs font-semibold text-slate-500 uppercase tracking-wider border-b border-slate-200">
            <tr>
              <th class="px-6 py-3.5">ID</th>
              <th class="px-6 py-3.5">Médico</th>
              <th class="px-6 py-3.5 text-center">Total Citas</th>
              <th class="px-6 py-3.5 text-center">Programadas</th>
              <th class="px-6 py-3.5 text-center">Confirmadas</th>
              <th class="px-6 py-3.5 text-center text-emerald-700">Atendidas</th>
              <th class="px-6 py-3.5 text-center text-rose-700">Canceladas</th>
              <th class="px-6 py-3.5 text-center">No Asistió</th>
              <th class="px-6 py-3.5 text-right">% Cancelación</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-if="cargando">
              <td colspan="9" class="px-6 py-8 text-center text-slate-400">
                <span class="animate-pulse">Calculando métricas y cursores en Oracle...</span>
              </td>
            </tr>
            <tr v-else-if="medicosReporte.length === 0">
              <td colspan="9" class="px-6 py-8 text-center text-slate-400">
                No hay actividad registrada en el rango de fechas seleccionado.
              </td>
            </tr>
            <tr
              v-for="med in medicosReporte"
              :key="med.idMedico"
              class="hover:bg-slate-50/80 transition"
            >
              <td class="px-6 py-4 font-mono font-medium text-slate-900">
                #{{ med.idMedico }}
              </td>
              <td class="px-6 py-4 font-semibold text-slate-800">
                {{ med.nombreMedico }}
              </td>
              <td class="px-6 py-4 text-center font-bold text-slate-900">
                {{ med.totalCitas }}
              </td>
              <td class="px-6 py-4 text-center text-slate-600">
                {{ med.programadas }}
              </td>
              <td class="px-6 py-4 text-center text-slate-600">
                {{ med.confirmadas }}
              </td>
              <td class="px-6 py-4 text-center font-bold text-emerald-600">
                {{ med.atendidas }}
              </td>
              <td class="px-6 py-4 text-center font-bold text-rose-600">
                {{ med.canceladas }}
              </td>
              <td class="px-6 py-4 text-center text-slate-500">
                {{ med.noAsistio }}
              </td>
              <td class="px-6 py-4 text-right">
                <span
                  class="font-mono font-bold px-2 py-0.5 rounded text-xs"
                  :class="med.porcentajeCancelacion > 25 ? 'bg-rose-100 text-rose-800' : 'bg-emerald-100 text-emerald-800'"
                >
                  {{ Number(med.porcentajeCancelacion).toFixed(1) }}%
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'
import api from '../services/api'
import AlertBanner from '../components/AlertBanner.vue'

Chart.register(...registerables)

const fechaInicio = ref('2026-09-14') // Default range specified in README §3.2 and §7
const fechaFin = ref('2026-12-22')

const medicosReporte = ref([])
const cargando = ref(false)
const chartCanvas = ref(null)
let chartInstance = null

const alert = reactive({
  visible: false,
  type: 'info',
  title: '',
  message: '',
  oracleCode: null
})

const kpis = computed(() => {
  const totalCitas = medicosReporte.value.reduce((acc, m) => acc + (m.totalCitas || 0), 0)
  const atendidas = medicosReporte.value.reduce((acc, m) => acc + (m.atendidas || 0), 0)
  const canceladas = medicosReporte.value.reduce((acc, m) => acc + (m.canceladas || 0), 0)
  const pendientes = medicosReporte.value.reduce((acc, m) => acc + (m.programadas || 0) + (m.confirmadas || 0), 0)
  const porcentajeCancelacionGlobal = totalCitas > 0 ? ((canceladas / totalCitas) * 100).toFixed(1) : 0

  return { totalCitas, atendidas, canceladas, pendientes, porcentajeCancelacionGlobal }
})

onMounted(async () => {
  await cargarReportes()
})

async function cargarReportes() {
  cargando.value = true
  alert.visible = false

  try {
    const res = await api.get('/reportes/medicos', {
      params: {
        fechaInicio: fechaInicio.value,
        fechaFin: fechaFin.value
      }
    })
    medicosReporte.value = res.data || []

    await nextTick()
    renderChart()
  } catch (err) {
    handleError(err, 'Error al obtener reporte gerencial')
  } finally {
    cargando.value = false
  }
}

function renderChart() {
  if (!chartCanvas.value) return

  if (chartInstance) {
    chartInstance.destroy()
  }

  const labels = medicosReporte.value.map(m => m.nombreMedico.split(' ')[0] + ' ' + (m.nombreMedico.split(' ')[1] || ''))
  const dataAtendidas = medicosReporte.value.map(m => m.atendidas)
  const dataCanceladas = medicosReporte.value.map(m => m.canceladas)
  const dataPendientes = medicosReporte.value.map(m => (m.programadas || 0) + (m.confirmadas || 0))

  chartInstance = new Chart(chartCanvas.value, {
    type: 'bar',
    data: {
      labels,
      datasets: [
        {
          label: 'Atendidas',
          data: dataAtendidas,
          backgroundColor: '#059669', // Emerald
          borderRadius: 4
        },
        {
          label: 'Canceladas',
          data: dataCanceladas,
          backgroundColor: '#e11d48', // Rose
          borderRadius: 4
        },
        {
          label: 'Programadas / Confirmadas',
          data: dataPendientes,
          backgroundColor: '#0284c7', // Sky
          borderRadius: 4
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'top',
          labels: { font: { family: 'Inter', size: 12 } }
        },
        tooltip: {
          padding: 10,
          boxPadding: 4
        }
      },
      scales: {
        x: {
          grid: { display: false },
          ticks: { font: { family: 'Inter', size: 11 } }
        },
        y: {
          beginAtZero: true,
          ticks: { font: { family: 'Inter', size: 11 } },
          grid: { color: '#f1f5f9' }
        }
      }
    }
  })
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
    showAlert('error', defaultTitle, mensaje || 'Error en Oracle Database.', codigoOracle)
  } else {
    showAlert('error', defaultTitle, err.message)
  }
}
</script>
