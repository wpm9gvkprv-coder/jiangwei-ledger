<script setup>
import {ref,onMounted} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {ElMessage,ElMessageBox} from 'element-plus'
import {api,notifyError} from '../lib/api.js'
import {session,loadUser,clearSession} from '../lib/session.js'
import AppIcon from './AppIcon.vue'
const route=useRoute(),router=useRouter(),open=ref(false)
const links=[['/','收支总览','DataBoard'],['/bills','账单明细','Tickets'],['/analysis','消费分析','TrendCharts'],['/budget','月度预算','Wallet'],['/categories','收支分类','Grid'],['/alerts','异常提醒','Bell']]
onMounted(async()=>{if(!session.user)try{await loadUser()}catch(e){notifyError(e)}})
async function logout(){try{await ElMessageBox.confirm('退出后，该账号的其他会话也需要重新登录。','退出登录',{confirmButtonText:'确认退出',cancelButtonText:'取消',type:'info'})}catch{return}try{await api.post('/auth/logout');clearSession();router.replace('/login');ElMessage.success('已退出登录')}catch(e){notifyError(e)}}
</script><template>
<div class="app-shell"><div v-if="open" class="nav-overlay" @click="open=false"/>
<aside class="sidebar" :class="{visible:open}">
 <router-link to="/" class="brand" @click="open=false"><span class="brand-icon"><AppIcon name="Notebook" :size="25"/></span><span>酱味账本<small>JIANGWEI LEDGER</small></span></router-link>
 <div class="nav-caption">我的账本</div><nav aria-label="主要导航"><router-link v-for="[path,label,icon] in links" :key="path" :to="path" :class="{active:route.path===path}" @click="open=false"><AppIcon :name="icon"/><span>{{label}}</span><span class="nav-dot"/></router-link></nav>
 <div class="sidebar-note"><AppIcon name="Sunny" :size="22"/><p>小小记录<br>让生活更有数</p></div>
 <div class="sidebar-bottom"><router-link to="/profile" class="user-link" @click="open=false"><span class="avatar">{{session.user?.displayName?.slice(0,1)||'我'}}</span><span><b>{{session.user?.displayName||'我的账户'}}</b><small>个人账本</small></span><AppIcon name="ArrowRight" :size="14"/></router-link><button class="logout" @click="logout"><AppIcon name="SwitchButton" :size="16"/>退出登录</button></div>
</aside>
<main class="main-content"><header class="topbar"><button class="mobile-menu icon-button" aria-label="展开导航" @click="open=!open"><AppIcon name="Expand"/></button><div class="breadcrumbs">我的账本 <span>/</span> <b>{{route.meta.title}}</b></div><span class="privacy-note"><AppIcon name="Lock" :size="14"/>只属于你的生活记录</span></header><div class="workspace"><div class="page-heading"><div><h1>{{route.meta.title}}</h1><p>{{route.meta.subtitle}}</p></div><el-button type="primary" size="large" @click="router.push({path:'/bills',query:{new:'1'}})"><AppIcon name="Plus" :size="17"/>记一笔</el-button></div><router-view/></div><footer class="page-footer">酱味大鸡 · 智能个人记账与消费分析系统</footer></main></div>
</template>
