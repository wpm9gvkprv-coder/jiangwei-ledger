<script setup>
import {ref,watch} from 'vue'
import {api} from '../lib/api.js'
import {money} from '../lib/format.js'
import StateBox from './StateBox.vue'
const props=defineProps({modelValue:Boolean,billId:Number}),emit=defineEmits(['update:modelValue'])
const loading=ref(false),error=ref(''),bill=ref(null),result=ref(null);let serial=0
async function load(){if(!props.billId)return;const n=++serial;loading.value=true;error.value='';try{const [b,r]=await Promise.all([api.get(`/bills/${props.billId}`),api.get(`/intelligence/bills/${props.billId}/anomaly`)]);if(n===serial){bill.value=b;result.value=r}}catch(e){if(n===serial)error.value=e.message}finally{if(n===serial)loading.value=false}}
watch(()=>[props.modelValue,props.billId],([open])=>{if(open)load();else serial++})
</script><template><el-drawer :model-value="modelValue" @update:model-value="v=>emit('update:modelValue',v)" title="这笔支出的分析" size="min(540px, 100vw)"><div v-loading="loading" class="drawer-content"><StateBox :error="error" @retry="load"><template v-if="bill&&result"><p class="eyebrow">{{bill.billDate}} · 支出</p><h2 class="big-amount">{{money(bill.amount)}}</h2><p>{{bill.description}}</p><el-alert :title="result.status==='ANOMALY'?'这笔支出高于日常水平':result.status==='INSUFFICIENT_DATA'?'历史记录还不够':'这笔支出在历史范围内'" :type="result.status==='ANOMALY'?'warning':'info'" :closable="false" show-icon/><dl class="detail-list"><div><dt>历史样本</dt><dd>{{result.sampleCount}}笔</dd></div><template v-if="result.upperBound!==null"><div><dt>下四分位数 Q1</dt><dd>{{money(result.q1)}}</dd></div><div><dt>上四分位数 Q3</dt><dd>{{money(result.q3)}}</dd></div><div><dt>四分位距 IQR</dt><dd>{{money(result.iqr)}}</dd></div><div><dt>参考上界</dt><dd>{{money(result.upperBound)}}</dd></div></template></dl><p class="muted small">{{result.status==='INSUFFICIENT_DATA'?'需要至少8笔历史支出才能分析。继续记账，慢慢积累样本。':'参考上界为 Q3 + 1.5 × IQR。超过上界只表示与历史记录有所不同，不代表这笔消费不合理。'}}</p><p class="muted small">以这笔账之前90天内的本人支出为样本，同日较早记录也会计入。</p></template></StateBox></div></el-drawer></template>
