import axios from 'axios'
import { activeDoctor } from './state'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor to inject X-Medico-Usuario if set
api.interceptors.request.use((config) => {
  if (activeDoctor.value) {
    config.headers['X-Medico-Usuario'] = activeDoctor.value
  }
  return config
}, (error) => {
  return Promise.reject(error)
})

export default api
