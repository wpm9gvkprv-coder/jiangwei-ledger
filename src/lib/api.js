import axios from 'axios'
import { ElMessage } from 'element-plus'
export const TOKEN_KEY='jiangwei-session'
export const api=axios.create({baseURL:import.meta.env.VITE_API_BASE || '/api',timeout:12000})
api.interceptors.request.use(config=>{const token=sessionStorage.getItem(TOKEN_KEY);if(token)config.headers.Authorization=`Bearer ${token}`;return config})
api.interceptors.response.use(response=>{if(response.data.code!==0)return Promise.reject(new Error(response.data.message||'操作未完成'));return response.data.data},error=>{
 if(error.response?.status===401&&!error.config?.url?.startsWith('/auth/')){sessionStorage.removeItem(TOKEN_KEY);window.dispatchEvent(new Event('session-expired'))}
 const message=error.response?.data?.message || (error.code==='ECONNABORTED'?'请求超时，请稍后重试':'暂时无法连接服务，请检查服务是否启动')
 return Promise.reject(Object.assign(new Error(message),{status:error.response?.status}))
})
export const notifyError=e=>{if(e?.status!==401)ElMessage.error(e?.message||'操作未完成，请重试')}
