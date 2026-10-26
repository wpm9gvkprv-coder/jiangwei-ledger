<script setup>
import {ref,onMounted,onBeforeUnmount,watch,nextTick} from 'vue'
import * as echarts from 'echarts/core'
import {PieChart,BarChart,LineChart} from 'echarts/charts'
import {TooltipComponent,LegendComponent,GridComponent,AriaComponent} from 'echarts/components'
import {SVGRenderer} from 'echarts/renderers'
echarts.use([PieChart,BarChart,LineChart,TooltipComponent,LegendComponent,GridComponent,AriaComponent,SVGRenderer])
const props=defineProps({option:Object,label:String,height:{type:Number,default:290}})
const host=ref();let chart,observer
onMounted(()=>{chart=echarts.init(host.value,null,{renderer:'svg'});chart.setOption(props.option||{});observer=new ResizeObserver(()=>chart?.resize());observer.observe(host.value)})
watch(()=>props.option,async v=>{await nextTick();chart?.setOption(v||{},true)},{deep:true})
onBeforeUnmount(()=>{observer?.disconnect();chart?.dispose();chart=null})
</script><template><div ref="host" class="chart" role="img" :aria-label="label" :style="{height:height+'px'}"/></template>
