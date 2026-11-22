<template>
  <div class="max-w-7xl mx-auto py-8 px-4 sm:px-6">
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-slate-900 flex items-center gap-2">
        <span>💊</span> Farmacia y Dispensación de Medicamentos
      </h1>
      <p class="text-sm text-slate-500 mt-1">
        HU05 & HU06: Consulta de recetas pendientes y dispensación transaccional con control de stock y disparador TRG_ALERTA_STOCK en Oracle XE.
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

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- Left Column: Dispensar Form -->
      <div class="lg:col-span-1">
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden sticky top-24">
          <div class="bg-gradient-to-r from-emerald-600 to-teal-700 px-6 py-4 text-white">
            <h2 class="font-semibold text-sm flex items-center gap-2">
              <span>💉</span> Dispensar Medicamento
            </h2>
            <p class="text-xs text-emerald-100">PKG_FARMACIA.PRC_DISPENSAR_MEDICAMENTO</p>
          </div>

          <form @submit.prevent="dispensar" class="p-6 space-y-5">
            <!-- Seleccione Receta -->
            <div>
              <label class="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-2">
                1. Receta Pendiente *
              </label>
              <select
                v-model="form.idReceta"
                required
                @change="onRecetaSelected"
                class="w-full px-3 py-2.5 rounded-lg border border-slate-300 text-sm focus:ring-2 focus:ring-emerald-500 bg-white"
              >
                <option value="" disabled>-- Seleccione receta --</option>
                <option
                  v-for="rec in recetas"
                  :key="rec.idReceta + '-' + rec.idMedicamento"
                  :value="rec.idReceta"
                >
                  Receta #{{ rec.idReceta }} ({{ rec.nombrePaciente }})
                </option>
              </select>
            </div>

            <!-- Seleccione Medicamento -->
            <div>
              <label class="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-2">
                2. Medicamento *
              </label>
              <select
                v-model="form.idMedicamento"
                required
                class="w-full px-3 py-2.5 rounded-lg border border-slate-300 text-sm focus:ring-2 focus:ring-emerald-500 bg-white"
              >
                <option value="" disabled>-- Seleccione medicamento --</option>
                <option
                  v-for="med in medicamentos"
                  :key="med.idMedicamento"
                  :value="med.idMedicamento"
                >
                  {{ med.nombre }} ({{ med.presentacion }})
                </option>
              </select>
            </div>

            <!-- Cantidad a Dispensar -->
            <div>
              <label class="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-2">
                3. Cantidad a Dispensar *
              </label>
              <input
                type="number"
                v-model.number="form.cantidad"
                required
                min="1"
                class="w-full px-3 py-2.5 rounded-lg border border-slate-300 text-sm focus:ring-2 focus:ring-emerald-500"
              />
            </div>

            <div class="pt-2">
              <button
                type="submit"
                :disabled="dispensando"
                class="w-full py-2.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white font-semibold text-sm shadow-xs transition flex items-center justify-center gap-2"
              >
                <span v-if="dispensando" class="animate-spin">⏳</span>
                <span>{{ dispensando ? 'Dispensando en Oracle...' : 'Dispensar Medicamento' }}</span>
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Right Column: Recetas Pendientes & Medicamentos Catalogo -->
      <div class="lg:col-span-2 space-y-6">
        <!-- Recetas Pendientes Table -->
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          <div class="px-6 py-4 border-b border-slate-200 bg-slate-50 flex items-center justify-between">
            <div>
              <h2 class="font-semibold text-slate-800 text-sm">Recetas Médicas Pendientes</h2>
              <p class="text-xs text-slate-400">Estados 601 (Emitida) y 602 (Dispensada Parcial)</p>
            </div>
            <button
              @click="cargarDatos"
              class="px-3 py-1 bg-white border border-slate-200 rounded-lg text-xs font-medium text-slate-600 hover:bg-slate-50 transition"
            >
              🔄 Actualizar
            </button>
          </div>

          <div class="overflow-x-auto">
            <table class="w-full text-left text-sm text-slate-600">
              <thead class="bg-slate-50 text-xs font-semibold text-slate-500 uppercase tracking-wider border-b border-slate-200">
                <tr>
                  <th class="px-5 py-3"># Receta</th>
                  <th class="px-5 py-3">Paciente</th>
                  <th class="px-5 py-3">Médico</th>
                  <th class="px-5 py-3">Medicamento</th>
                  <th class="px-5 py-3">Cant.</th>
                  <th class="px-5 py-3">Estado</th>
                  <th class="px-5 py-3 text-right">Acción</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-if="cargando">
                  <td colspan="7" class="px-5 py-8 text-center text-slate-400">
                    <span class="animate-pulse">Cargando recetas pendientes...</span>
                  </td>
                </tr>
                <tr v-else-if="recetas.length === 0">
                  <td colspan="7" class="px-5 py-8 text-center text-slate-400">
                    No hay recetas pendientes de dispensar en este momento.
                  </td>
                </tr>
                <tr
                  v-for="rec in recetas"
                  :key="rec.idReceta + '-' + (rec.idMedicamento || '')"
                  class="hover:bg-slate-50/80 transition"
                >
                  <td class="px-5 py-3.5 font-mono font-medium text-slate-900">
                    #{{ rec.idReceta }}
                  </td>
                  <td class="px-5 py-3.5 font-semibold text-slate-800">
                    {{ rec.nombrePaciente }}
                  </td>
                  <td class="px-5 py-3.5 text-xs text-slate-600">
                    {{ rec.nombreMedico }}
                  </td>
                  <td class="px-5 py-3.5">
                    <span class="font-medium text-slate-800">{{ rec.nombreMedicamento || 'N/A' }}</span>
                  </td>
                  <td class="px-5 py-3.5 font-mono text-xs">
                    {{ rec.cantidadRecetada || '-' }}
                  </td>
                  <td class="px-5 py-3.5">
                    <span
                      class="px-2 py-0.5 rounded-full text-xs font-semibold"
                      :class="rec.idEstadoReceta === 601 ? 'bg-amber-100 text-amber-800' : 'bg-blue-100 text-blue-800'"
                    >
                      {{ rec.estadoReceta }}
                    </span>
                  </td>
                  <td class="px-5 py-3.5 text-right">
                    <button
                      @click="seleccionarParaDispensar(rec)"
                      class="px-2.5 py-1 rounded bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border border-emerald-200 text-xs font-medium transition"
                    >
                      Cargar en Form
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Catalogo de Medicamentos -->
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          <div class="px-6 py-4 border-b border-slate-200 bg-slate-50">
            <h2 class="font-semibold text-slate-800 text-sm">Catálogo de Medicamentos Disponibles</h2>
            <p class="text-xs text-slate-400">TBL_MEDICAMENTOS con presentaciones farmacéuticas</p>
          </div>

          <div class="p-6 grid grid-cols-1 md:grid-cols-2 gap-4">
            <div
              v-for="med in medicamentos"
              :key="med.idMedicamento"
              class="p-4 rounded-xl border border-slate-100 bg-slate-50/50 hover:bg-slate-50 transition"
            >
              <div class="flex items-start justify-between gap-2">
                <h4 class="font-semibold text-slate-900 text-sm">{{ med.nombre }}</h4>
                <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-200 text-slate-700">
                  ID: {{ med.idMedicamento }}
                </span>
              </div>
              <p class="text-xs text-slate-500 mt-1">{{ med.descripcion || 'Sin descripción' }}</p>
              <div class="mt-2 text-xs font-medium text-emerald-700 bg-emerald-50 px-2 py-1 rounded inline-block">
                📦 Presentación: {{ med.presentacion }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../services/api'
import AlertBanner from '../components/AlertBanner.vue'

const recetas = ref([])
const medicamentos = ref([])
const cargando = ref(false)
const dispensando = ref(false)

const form = reactive({
  idReceta: '',
  idMedicamento: '',
  cantidad: 1
})

const alert = reactive({
  visible: false,
  type: 'info',
  title: '',
  message: '',
  oracleCode: null
})

onMounted(async () => {
  await cargarDatos()
})

async function cargarDatos() {
  cargando.value = true
  try {
    const [resRec, resMed] = await Promise.all([
      api.get('/farmacia/recetas-pendientes'),
      api.get('/farmacia/medicamentos')
    ])
    recetas.value = resRec.data
    medicamentos.value = resMed.data
  } catch (err) {
    showAlert('error', 'Error al cargar datos de farmacia', err.message)
  } finally {
    cargando.value = false
  }
}

function onRecetaSelected() {
  const rec = recetas.value.find(r => r.idReceta === form.idReceta)
  if (rec && rec.idMedicamento) {
    form.idMedicamento = rec.idMedicamento
    if (rec.cantidadRecetada) {
      form.cantidad = rec.cantidadRecetada
    }
  }
}

function seleccionarParaDispensar(rec) {
  form.idReceta = rec.idReceta
  if (rec.idMedicamento) {
    form.idMedicamento = rec.idMedicamento
  }
  if (rec.cantidadRecetada) {
    form.cantidad = rec.cantidadRecetada
  }
  showAlert('info', 'Receta Seleccionada', `Se cargó la receta #${rec.idReceta} en el formulario de dispensación.`)
}

async function dispensar() {
  dispensando.value = true
  alert.visible = false

  try {
    const res = await api.post('/farmacia/dispensar', form)
    showAlert('success', 'Dispensación Exitosa', res.data.mensaje || 'Se dispensó el medicamento y se actualizó el stock.')
    await cargarDatos()
  } catch (err) {
    handleError(err, 'Error en dispensación de medicamento')
  } finally {
    dispensando.value = false
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
    showAlert('error', defaultTitle, mensaje || 'Error en Oracle Database.', codigoOracle)
  } else {
    showAlert('error', defaultTitle, err.message)
  }
}
</script>
