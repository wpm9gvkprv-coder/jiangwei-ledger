<script setup>
import * as icons from '@element-plus/icons-vue'
defineProps({name:String,size:{type:Number,default:20}})
</script><template><el-icon :size="size" aria-hidden="true"><component :is="icons[name]||icons.Wallet"/></el-icon></template>
