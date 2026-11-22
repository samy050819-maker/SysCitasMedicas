<template>
  <div class="max-w-4xl mx-auto py-8 px-4 sm:px-6">
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-slate-900 flex items-center gap-2">
        <span>📅</span> Agendar Nueva Cita Médica
      </h1>
      <p class="text-sm text-slate-500 mt-1">
        HU01: Asignación de citas médicas con validación de solapamiento pesimista en Oracle XE (FOR UPDATE WAIT 3).
      </p>
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

    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
      <div class="bg-gradient-to-r from-clinical-600 to-clinical-700 px-6 py-4 text-white">
        <h2 class="text-base font-semibold">Formulario de Reserva</h2>
        <p class="text-xs text-clinical-100">Complete los datos para consultar horarios disponibles y confirmar la cita</p>
      </div>

      <form @submit.prevent="reservarCita" class="p-6 sm:p-8 space-y-6">
        <!-- Grid: Paciente & Especialidad / Médico -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- Paciente Selector -->
          <div>
            <label class="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-2">
              1. Seleccione Paciente *
            </label>
            <select
              v-model="form.idPaciente"
              required
              class="w-full px-3 py-2.5 rounded-lg border border-slate-300 focus:ring-2 focus:ring-clinical-500 focus:border-clinical-500 bg-white text-sm"
            >
              <option value="" disabled>-- Seleccione un paciente --</option>
              <option v-for="pac in pacientes" :key="pac.idPaciente" :value="pac.idPaciente">
                {{ pac.nombreCompleto }} (Doc: {{ pac.documento || pac.idPaciente }})
              </option>
            </select>
          </div>

          <!-- Especialidad Filter -->
          <div>
            <label class="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-2">
              2. Filtrar por Especialidad (Opcional)
            </label>
            <select
              v-model="filtroEspecialidad"
              class="w-full px-3 py-2.5 rounded-lg border border-slate-300 focus:ring-2 focus:ring-clinical-500 focus:border-clinical-500 bg-white text-sm"
            >
              <option value="">Todas las especialidades</option>
              <option v-for="esp in especialidadesDisponibles" :key="esp" :value="esp">
                {{ esp }}
              </option>
            </select>
          </div>
        </div>

        <!-- Médico Selector & Fecha -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- Médico Selector -->
          <div>
            <label class="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-2">
              3. Seleccione Médico *
            </label>
            <select
              v-model="form.idMedico"
              required
              @change="consultarHorarios"
              class="w-full px-3 py-2.5 rounded-lg border border-slate-300 focus:ring-2 focus:ring-clinical-500 focus:border-clinical-500 bg-white text-sm"
            >
              <option value="" disabled>-- Seleccione un médico --</option>
              <option v-for="med in medicosFiltrados" :key="med.idMedico" :value="med.idMedico">
                Dr(a). {{ med.nombreCompleto }} — {{ med.especialidad }}
              </option>
            </select>
          </div>

          <!-- Fecha Selector -->
          <div>
            <label class="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-2">
              4. Fecha de Cita *
            </label>
            <input
              type="date"
              v-model="form.fecha"
              required
              @change="consultarHorarios"
              class="w-full px-3 py-2.5 rounded-lg border border-slate-300 focus:ring-2 focus:ring-clinical-500 focus:border-clinical-500 bg-white text-sm"
            />
            <span class="text-xs text-slate-400 mt-1 block">Rango con datos: Sep 2026 a Dic 2026</span>
          </div>
        </div>

        <!-- Horarios Disponibles Selector -->
        <div class="border-t border-slate-100 pt-6">
          <div class="flex items-center justify-between mb-2">
            <label class="block text-xs font-semibold text-slate-700 uppercase tracking-wider">
              5. Franja Horaria Disponible (1 hora) *
            </label>
            <span v-if="cargandoHorarios" class="text-xs text-clinical-600 font-medium animate-pulse">
              Consultando PKG_CITAS en Oracle...
            </span>
          </div>

          <div v-if="horariosDisponibles.length > 0">
            <select
              v-model="horarioSeleccionado"
              required
              class="w-full px-3 py-2.5 rounded-lg border border-slate-300 focus:ring-2 focus:ring-clinical-500 focus:border-clinical-500 bg-white text-sm"
            >
              <option value="" disabled>-- Seleccione un horario disponible --</option>
              <option
                v-for="horario in horariosDisponibles"
                :key="horario.fechaHoraInicio"
                :value="horario"
              >
                ⏰ {{ horario.etiquetaDesplegable }} (Bloque agenda: {{ horario.idAgenda }})
              </option>
            </select>
          </div>

          <div
            v-else-if="form.idMedico && form.fecha && !cargandoHorarios"
            class="p-4 rounded-lg bg-amber-50 border border-amber-200 text-amber-800 text-sm flex items-center gap-2"
          >
            <span>⚠️</span>
            <span>No se encontraron cupos u horarios libres para el médico y fecha seleccionados. Intente otra fecha o médico.</span>
          </div>

          <div
            v-else
            class="p-4 rounded-lg bg-slate-50 border border-slate-200 text-slate-500 text-sm italic"
          >
            Seleccione médico y fecha para cargar automáticamente las franjas horarias disponibles desde la base de datos.
          </div>
        </div>

        <!-- Submit Button -->
        <div class="border-t border-slate-100 pt-6 flex justify-end gap-3">
          <button
            type="button"
            @click="limpiarFormulario"
            class="px-5 py-2.5 rounded-lg border border-slate-300 text-slate-700 hover:bg-slate-50 text-sm font-medium transition"
          >
            Limpiar
          </button>
          <button
            type="submit"
            :disabled="!horarioSeleccionado || guardando"
            class="px-6 py-2.5 rounded-lg bg-clinical-600 hover:bg-clinical-700 disabled:opacity-50 text-white text-sm font-semibold shadow-sm transition flex items-center gap-2"
          >
            <span v-if="guardando" class="animate-spin inline-block">⏳</span>
            <span>{{ guardando ? 'Reservando...' : 'Confirmar Reserva' }}</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import api from '../services/api'
import AlertBanner from '../components/AlertBanner.vue'

const pacientes = ref([])
const medicos = ref([])
const filtroEspecialidad = ref('')
const horariosDisponibles = ref([])
const horarioSeleccionado = ref(null)
const cargandoHorarios = ref(false)
const guardando = ref(false)

const form = reactive({
  idPaciente: '',
  idMedico: '',
  fecha: '2026-10-15' // default date within active data range
})

const alert = reactive({
  visible: false,
  type: 'info',
  title: '',
  message: '',
  oracleCode: null
})

const especialidadesDisponibles = computed(() => {
  const set = new Set(medicos.value.map(m => m.especialidad).filter(Boolean))
  return Array.from(set).sort()
})

const medicosFiltrados = computed(() => {
  if (!filtroEspecialidad.value) return medicos.value
  return medicos.value.filter(m => m.especialidad === filtroEspecialidad.value)
})

onMounted(async () => {
  try {
    const [resPac, resMed] = await Promise.all([
      api.get('/pacientes'),
      api.get('/medicos')
    ])
    pacientes.value = resPac.data
    medicos.value = resMed.data
  } catch (err) {
    showAlert('error', 'Error de conexión', 'No se pudieron cargar los datos iniciales de pacientes y médicos.')
  }
})

async function consultarHorarios() {
  horarioSeleccionado.value = null
  horariosDisponibles.value = []

  if (!form.idMedico || !form.fecha) return

  cargandoHorarios.value = true
  try {
    const res = await api.get('/citas/horarios-disponibles', {
      params: {
        idMedico: form.idMedico,
        fecha: form.fecha,
        duracionMin: 60
      }
    })
    horariosDisponibles.value = res.data || []
  } catch (err) {
    handleError(err, 'Error al consultar horarios disponibles')
  } finally {
    cargandoHorarios.value = false
  }
}

async function reservarCita() {
  if (!horarioSeleccionado.value || !form.idPaciente) return

  guardando.value = true
  alert.visible = false

  try {
    const payload = {
      idPaciente: form.idPaciente,
      idMedico: form.idMedico,
      fechaHoraInicio: horarioSeleccionado.value.fechaHoraInicio,
      fechaHoraFin: horarioSeleccionado.value.fechaHoraFin
    }

    const res = await api.post('/citas/reservar', payload)
    showAlert('success', '¡Cita Reservada Exitosamente!', res.data.mensaje || 'La cita médica fue registrada y confirmada en Oracle Database.')
    
    // Refresh available slots
    await consultarHorarios()
  } catch (err) {
    handleError(err, 'No se pudo reservar la cita')
  } finally {
    guardando.value = false
  }
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
    showAlert(
      err.response.status === 409 ? 'error' : 'warning',
      defaultTitle,
      mensaje || 'Ocurrió un error en la base de datos.',
      codigoOracle
    )
  } else {
    showAlert('error', defaultTitle, err.message || 'Error de red o servidor no disponible.')
  }
}

function limpiarFormulario() {
  form.idPaciente = ''
  form.idMedico = ''
  horarioSeleccionado.value = null
  horariosDisponibles.value = []
  alert.visible = false
}
</script>
