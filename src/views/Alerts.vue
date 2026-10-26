<script setup>
import {ref,onMounted} from 'vue'
import {ElMessage} from 'element-plus'
import {api,notifyError} from '../lib/api.js'
import {money} from '../lib/format.js'
import StateBox from '../components/StateBox.vue'
import AppIcon from '../components/AppIcon.vue'
import AnomalyDrawer from '../components/AnomalyDrawer.vue'
const rows=ref([]),total=ref(0),page=ref(1),size=ref(10),loading=ref(false),error=ref(''),analysis=ref(false),billId=ref(null),reading=ref(null);let serial=0
async function load(){const n=++serial;loading.value=true;error.value='';try{const r=await api.get('/alerts',{params:{page:page.value,size:size.value}});if(n===serial){rows.value=r.records;total.value=r.total}}catch(e){if(n===serial)error.value=e.message}finally{if(n===serial)loading.value=false}}
function inspect(row){billId.value=row.billId;analysis.value=true}
async function read(row){if(reading.value)return;reading.value=row.id;try{await api.put(`/alerts/${row.id}/read`);row.readFlag=true;ElMessage.success('已标记为已读')}catch(e){notifyError(e)}finally{reading.value=null}}
onMounted(load)
</script><template><div class="notice-strip"><AppIcon name="InfoFilled" :size="20"/><p>提醒来自历史消费统计，仅供你核对。合理的大额购买，也可能与平时不同。</p></div><section class="panel" v-loading="loading"><div class="panel-heading"><div><h2>支出提醒 <span class="count">{{total}}</span></h2><p>账单修改后，提醒会同步重新计算</p></div><el-button @click="load"><AppIcon name="Refresh" :size="16"/>刷新</el-button></div><StateBox :error="error" :empty="!loading&&!rows.length" title="暂时没有需要核对的支出" description="记录至少8笔历史支出后，系统会开始进行统计分析。" @retry="load"><div class="alert-list"><article v-for="r in rows" :key="r.id" class="alert-item" :class="{read:r.readFlag}"><span class="category-icon orange"><AppIcon name="Warning"/></span><div class="alert-body"><div class="alert-title"><h3>{{money(r.amount)}} 的支出值得核对</h3><span class="status-label" :class="{done:r.readFlag}">{{r.readFlag?'已读':'未读'}}</span></div><p>账单 #{{r.billId}} · 参考上界 {{money(r.thresholdAmount)}} · 历史样本 {{r.sampleCount}}笔</p><small>{{r.reason}}</small><div class="alert-actions"><el-button type="primary" plain size="small" @click="inspect(r)">查看分析</el-button><el-button v-if="!r.readFlag" size="small" :loading="reading===r.id" @click="read(r)">标记已读</el-button></div></div></article></div><div class="pagination"><span>共{{total}}条提醒</span><el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="prev,pager,next" @current-change="load"/></div></StateBox></section><AnomalyDrawer v-model="analysis" :bill-id="billId"/></template>
