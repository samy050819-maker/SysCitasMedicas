<template>
  <div class="max-w-7xl mx-auto py-8 px-4 sm:px-6">
    <div class="mb-8">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 class="text-2xl font-bold text-slate-900 flex items-center gap-2">
            <span>🩺</span> Historia Clínica & Seguridad VPD
          </h1>
          <p class="text-sm text-slate-500 mt-1">
            HU03, HU04 & HU07: Registro de consultas y aislamiento de datos por médico en Oracle XE (Virtual Private Database + CLIENT_IDENTIFIER).
          </p>
        </div>

        <button
          @click="mostrarFormulario = !mostrarFormulario"
          class="px-4 py-2 rounded-lg bg-clinical-600 hover:bg-clinical-700 text-white text-sm font-semibold shadow-xs flex items-center gap-2 self-start transition"
        >
          <span>{{ mostrarFormulario ? '✕ Cerrar Formulario' : '+ Registrar Nueva Consulta' }}</span>
        </button>
      </div>

      <!-- VPD Simulation Bar -->
      <div class="mt-6 p-4 bg-gradient-to-r from-sky-900 to-slate-900 text-white rounded-2xl shadow-md border border-sky-800">
        <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div>
            <div class="flex items-center gap-2">
              <span class="px-2 py-0.5 rounded-full text-[10px] font-mono tracking-wider font-bold bg-sky-500 text-white uppercase">
                VPD Oracle 21c
              </span>
              <h3 class="font-bold text-sm text-sky-200">Simulación de Sesión de Médico Activo (Pool HikariCP)</h3>
            </div>
            <p class="text-xs text-slate-300 mt-1">
              Al seleccionar un médico, el backend envía el header <code class="bg-black/40 px-1 py-0.5 rounded text-sky-300">X-Medico-Usuario</code> y ejecuta <code class="bg-black/40 px-1 py-0.5 rounded text-sky-300">DBMS_SESSION.SET_IDENTIFIER</code>. Oracle XE aplica la política VPD <code class="bg-black/40 px-1 py-0.5 rounded text-sky-300">FN_FILTRO_HISTORIAS_MEDICO</code> aislando las historias automáticamente.
            </p>
          </div>

          <div class="flex items-center gap-3 shrink-0">
            <label class="text-xs font-semibold text-sky-200">Médico en Sesión:</label>
            <select
              v-model="activeDoctor"
              @change="onDoctorChange"
              class="px-3 py-2 rounded-lg bg-slate-800 border border-sky-600 text-white text-xs font-medium focus:ring-2 focus:ring-sky-400 focus:outline-hidden"
            >
              <option value="">-- Sin filtro (Admin / Esquema completo) --</option>
              <option v-for="med in medicos" :key="med.idMedico" :value="med.usuario">
                Dr(a). {{ med.nombreCompleto }} ({{ med.usuario }})
              </option>
            </select>
          </div>
        </div>
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

    <!-- Form: Registrar Consulta Médica -->
    <div v-if="mostrarFormulario" class="mb-8 bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="bg-slate-50 border-b border-slate-200 px-6 py-4 flex items-center justify-between">
        <h3 class="font-semibold text-slate-800 text-sm flex items-center gap-2">
          <span>📝</span> Formulario: Registro de Consulta Médica (PRC_REGISTRAR_CONSULTA)
        </h3>
        <span class="text-xs text-slate-400">HU03 / HU04</span>
      </div>

      <form @submit.prevent="guardarConsulta" class="p-6 sm:p-8 space-y-6">
        <div>
          <label class="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-2">
            1. Seleccione Cita Médica *
          </label>
          <select
            v-model="form.idCita"
            required
            class="w-full px-3 py-2.5 rounded-lg border border-slate-300 text-sm focus:ring-2 focus:ring-clinical-500 bg-white"
          >
            <option value="" disabled>-- Seleccione una cita --</option>
            <option v-for="c in citasDisponibles" :key="c.idCita" :value="c.idCita">
              Cita #{{ c.idCita }} — Paciente: {{ c.nombrePaciente }} — Dr(a): {{ c.nombreMedico }} ({{ c.estado }})
            </option>
          </select>
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-2">
            2. Diagnóstico Médico *
          </label>
          <textarea
            v-model="form.diagnostico"
            required
            rows="3"
            placeholder="Ingrese diagnóstico clínico del paciente..."
            class="w-full px-3 py-2.5 rounded-lg border border-slate-300 text-sm focus:ring-2 focus:ring-clinical-500"
          ></textarea>
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-2">
            3. Notas de Evolución / Tratamiento
          </label>
          <textarea
            v-model="form.notas"
            rows="2"
            placeholder="Indicaciones terapéuticas, conducta y observaciones..."
            class="w-full px-3 py-2.5 rounded-lg border border-slate-300 text-sm focus:ring-2 focus:ring-clinical-500"
          ></textarea>
        </div>

        <div class="flex justify-end gap-3 pt-4 border-t border-slate-100">
          <button
            type="button"
            @click="mostrarFormulario = false"
            class="px-4 py-2 rounded-lg border border-slate-300 text-slate-700 hover:bg-slate-50 text-sm font-medium"
          >
            Cancelar
          </button>
          <button
            type="submit"
            :disabled="guardando"
            class="px-6 py-2 rounded-lg bg-clinical-600 hover:bg-clinical-700 text-white text-sm font-semibold shadow-xs flex items-center gap-2"
          >
            <span v-if="guardando" class="animate-spin">⏳</span>
            <span>{{ guardando ? 'Registrando...' : 'Guardar Consulta' }}</span>
          </button>
        </div>
      </form>
    </div>

    <!-- Historias Clínicas Table -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
      <div class="p-4 bg-slate-50/70 border-b border-slate-200 flex flex-wrap items-center justify-between gap-3">
        <div class="flex items-center gap-2">
          <h2 class="font-semibold text-sm text-slate-800">
            Historias Clínicas Registradas
          </h2>
          <span class="px-2 py-0.5 rounded-full text-xs font-bold" :class="activeDoctor ? 'bg-sky-100 text-sky-800' : 'bg-slate-200 text-slate-700'">
            {{ activeDoctor ? `Filtrado por: ${activeDoctor}` : 'Vista Global (Admin)' }}
          </span>
        </div>

        <button
          @click="cargarHistorias"
          class="px-3 py-1.5 rounded-lg bg-white border border-slate-200 text-slate-600 hover:bg-slate-50 text-xs font-medium transition"
        >
          🔄 Refrescar VPD
        </button>
      </div>

      <div class="overflow-x-auto">
        <table class="w-full text-left text-sm text-slate-600">
          <thead class="bg-slate-50 text-xs font-semibold text-slate-500 uppercase tracking-wider border-b border-slate-200">
            <tr>
              <th class="px-6 py-3.5">ID Historia</th>
              <th class="px-6 py-3.5">Paciente</th>
              <th class="px-6 py-3.5">Médico Tratante</th>
              <th class="px-6 py-3.5">Fecha Cita</th>
              <th class="px-6 py-3.5">Diagnóstico</th>
              <th class="px-6 py-3.5">Notas / Tratamiento</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-if="cargando">
              <td colspan="6" class="px-6 py-8 text-center text-slate-400">
                <span class="animate-pulse">Consultando historias clínicas bajo política VPD...</span>
              </td>
            </tr>
            <tr v-else-if="historias.length === 0">
              <td colspan="6" class="px-6 py-8 text-center text-slate-400">
                <div class="flex flex-col items-center gap-1">
                  <span>🔒 No se encontraron historias clínicas visibles para la sesión actual.</span>
                  <span class="text-xs text-slate-400">
                    {{ activeDoctor ? `La política VPD aisló el acceso: el médico "${activeDoctor}" no tiene historias asignadas o no tiene permisos.` : 'No hay registros en la base de datos.' }}
                  </span>
                </div>
              </td>
            </tr>
            <tr
              v-for="hist in historias"
              :key="hist.idHistoria"
              class="hover:bg-slate-50/80 transition"
            >
              <td class="px-6 py-4 font-mono font-medium text-slate-900">
                #{{ hist.idHistoria }}
                <div class="text-[10px] text-slate-400">Cita #{{ hist.idCita }}</div>
              </td>
              <td class="px-6 py-4 font-semibold text-slate-800">
                {{ hist.nombrePaciente }}
              </td>
              <td class="px-6 py-4">
                <div class="font-medium text-slate-800">{{ hist.nombreMedico }}</div>
                <div class="text-xs font-mono text-clinical-600">@{{ hist.medicoUsuario }}</div>
              </td>
              <td class="px-6 py-4 text-xs text-slate-600">
                {{ formatearFecha(hist.fechaCita) }}
              </td>
              <td class="px-6 py-4">
                <p class="font-medium text-slate-800 line-clamp-2">{{ hist.diagnostico }}</p>
              </td>
              <td class="px-6 py-4 text-slate-600 text-xs">
                <p class="line-clamp-2">{{ hist.notas || 'Sin notas' }}</p>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="px-6 py-3 bg-slate-50 border-t border-slate-200 text-xs text-slate-500">
        Total de registros devueltos por Oracle: {{ historias.length }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../services/api'
import { activeDoctor } from '../services/state'
import AlertBanner from '../components/AlertBanner.vue'

const historias = ref([])
const medicos = ref([])
const citasDisponibles = ref([])
const cargando = ref(false)
const guardando = ref(false)
const mostrarFormulario = ref(false)

const form = reactive({
  idCita: '',
  diagnostico: '',
  notas: ''
})

const alert = reactive({
  visible: false,
  type: 'info',
  title: '',
  message: '',
  oracleCode: null
})

onMounted(async () => {
  await Promise.all([cargarMedicos(), cargarCitas(), cargarHistorias()])
})

async function cargarMedicos() {
  try {
    const res = await api.get('/medicos')
    medicos.value = res.data
  } catch (err) {
    console.error('Error al cargar médicos', err)
  }
}

async function cargarCitas() {
  try {
    const res = await api.get('/citas')
    citasDisponibles.value = res.data
  } catch (err) {
    console.error('Error al cargar citas', err)
  }
}

async function cargarHistorias() {
  cargando.value = true
  try {
    const res = await api.get('/historias')
    historias.value = res.data
  } catch (err) {
    handleError(err, 'Error al consultar historias clínicas con VPD')
  } finally {
    cargando.value = false
  }
}

function onDoctorChange() {
  showAlert(
    'info',
    'Sesión de Médico Cambiada',
    activeDoctor.value
      ? `Se activó la identidad de médico "${activeDoctor.value}". Oracle XE filtrará TBL_HISTORIAS_CLINICAS por VPD.`
      : 'Se restableció la sesión sin identificador (vista de administrador).'
  )
  cargarHistorias()
}

async function guardarConsulta() {
  guardando.value = true
  alert.visible = false

  try {
    const res = await api.post('/historias', form)
    showAlert('success', 'Consulta Guardada', res.data.mensaje || 'Se ejecutó PKG_HISTORIA.PRC_REGISTRAR_CONSULTA exitosamente.')
    form.idCita = ''
    form.diagnostico = ''
    form.notas = ''
    mostrarFormulario.value = false
    await cargarHistorias()
  } catch (err) {
    handleError(err, 'Error al registrar consulta médica')
  } finally {
    guardando.value = false
  }
}

function formatearFecha(fechaStr) {
  if (!fechaStr) return ''
  const d = new Date(fechaStr)
  return d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
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
