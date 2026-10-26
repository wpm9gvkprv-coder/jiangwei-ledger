import { reactive } from 'vue'
import { api,TOKEN_KEY } from './api.js'
export const session=reactive({user:null,expired:false})
export function clearSession(){sessionStorage.removeItem(TOKEN_KEY);session.user=null}
export async function signIn(username,password){const result=await api.post('/auth/login',{username,password});sessionStorage.setItem(TOKEN_KEY,result.token);session.user=result.user;session.expired=false}
export async function loadUser(){session.user=await api.get('/me');return session.user}
