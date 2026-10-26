import { createRouter,createWebHashHistory } from 'vue-router'
import { TOKEN_KEY } from './lib/api.js'
const router=createRouter({history:createWebHashHistory(),routes:[
 {path:'/login',component:()=>import('./views/Auth.vue'),meta:{public:true,title:'欢迎回来'}},
 {path:'/',component:()=>import('./components/Layout.vue'),children:[
 {path:'',name:'overview',component:()=>import('./views/Dashboard.vue'),meta:{title:'收支总览',subtitle:'每一笔收支，都有迹可循。'}},
 {path:'bills',name:'bills',component:()=>import('./views/Bills.vue'),meta:{title:'账单明细',subtitle:'记录日常，也看见生活的轨迹。'}},
 {path:'analysis',component:()=>import('./views/Analysis.vue'),meta:{title:'消费分析',subtitle:'从数字里，了解自己的消费习惯。'}},
 {path:'budget',component:()=>import('./views/Budget.vue'),meta:{title:'月度预算',subtitle:'为这个月的支出，留好余地。'}},
 {path:'categories',component:()=>import('./views/Categories.vue'),meta:{title:'收支分类',subtitle:'让每一笔账，都有合适的位置。'}},
 {path:'alerts',component:()=>import('./views/Alerts.vue'),meta:{title:'异常提醒',subtitle:'留意与平时不同的支出，再做判断。'}},
 {path:'profile',component:()=>import('./views/Profile.vue'),meta:{title:'个人信息',subtitle:'管理你的账本身份。'}}]},
 {path:'/:pathMatch(.*)*',redirect:'/'}]})
router.beforeEach(to=>{if(!to.meta.public&&!sessionStorage.getItem(TOKEN_KEY))return {path:'/login',query:{redirect:to.fullPath}};document.title=`${to.meta.title||'收支总览'} · 酱味账本`})
export default router
