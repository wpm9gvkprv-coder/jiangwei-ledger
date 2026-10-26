<script setup>
import {ref,onMounted} from 'vue'
import {ElMessage} from 'element-plus'
import {session,loadUser} from '../lib/session.js'
import {api,notifyError} from '../lib/api.js'
const form=ref({displayName:''}),formRef=ref(),busy=ref(false),ready=ref(false),error=ref('')
async function load(){error.value='';try{await loadUser();form.value.displayName=session.user.displayName;ready.value=true}catch(e){error.value=e.message}}
async function save(){if(busy.value||!await formRef.value.validate().catch(()=>false))return;busy.value=true;try{session.user=await api.put('/me',{displayName:form.value.displayName.trim()});ElMessage.success('个人信息已更新')}catch(e){notifyError(e)}finally{busy.value=false}}
onMounted(load)
</script><template><section class="panel profile-panel"><div class="profile-top"><span class="avatar large">{{session.user?.displayName?.slice(0,1)||'我'}}</span><div><h2>{{session.user?.displayName||'我的账户'}}</h2><p>你的专属生活账本</p></div></div><el-alert v-if="error" :title="error" type="error" :closable="false"/><el-button v-if="error" @click="load">重新加载</el-button><el-form v-if="ready" ref="formRef" :model="form" label-position="top" @submit.prevent="save"><el-form-item label="用户名"><el-input :model-value="session.user?.username" disabled/></el-form-item><el-form-item label="显示名称" prop="displayName" :rules="[{required:true,whitespace:true,message:'请输入显示名称',trigger:'blur'},{max:40,message:'名称最多40字',trigger:'blur'}]"><el-input v-model="form.displayName" maxlength="40"/></el-form-item><p class="small muted">修改显示名称不会影响登录用户名和已有账单。</p><el-button type="primary" native-type="submit" :loading="busy">保存个人信息</el-button></el-form></section></template>
