import { createRouter, createWebHistory } from 'vue-router'
import AgendarCitaView from '../views/AgendarCitaView.vue'
import GestionCitasView from '../views/GestionCitasView.vue'
import HistoriaClinicaView from '../views/HistoriaClinicaView.vue'
import FarmaciaView from '../views/FarmaciaView.vue'
import ReportesView from '../views/ReportesView.vue'

const routes = [
  { path: '/', redirect: '/agendar' },
  { path: '/agendar', name: 'agendar', component: AgendarCitaView },
  { path: '/gestion', name: 'gestion', component: GestionCitasView },
  { path: '/historias', name: 'historias', component: HistoriaClinicaView },
  { path: '/farmacia', name: 'farmacia', component: FarmaciaView },
  { path: '/reportes', name: 'reportes', component: ReportesView }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
