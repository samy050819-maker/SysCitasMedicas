import { ref } from 'vue'

// Username of the doctor currently active for VPD filtering (e.g. 'dramartinez')
export const activeDoctor = ref('')

// List of all active doctors for global VPD switcher
export const doctorList = ref([])

export function setActiveDoctor(username) {
  activeDoctor.value = username
}
