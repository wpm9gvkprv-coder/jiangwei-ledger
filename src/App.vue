<script setup>
import { onMounted,onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { clearSession,session } from './lib/session.js'
const router=useRouter()
function expired(){clearSession();session.expired=true;if(router.currentRoute.value.path!='/login')router.replace({path:'/login',query:{redirect:router.currentRoute.value.fullPath}})}
onMounted(()=>window.addEventListener('session-expired',expired));onUnmounted(()=>window.removeEventListener('session-expired',expired))
</script><template><router-view/></template>
