// 仅新增专用演示账号；通过真实API写入人工模拟数据，不清空现有数据。
const base=process.env.BACKEND_URL||'http://127.0.0.1:8080/api';
const username='demo_'+Date.now(),password='DemoPass123!';
let token='';
async function call(url,method='GET',body){const r=await fetch(base+url,{method,headers:{'Content-Type':'application/json',...(token?{Authorization:'Bearer '+token}:{})},...(body?{body:JSON.stringify(body)}:{})});const j=await r.json();if(!r.ok||j.code!==0)throw new Error(j.message||r.statusText);return j.data}
await call('/auth/register','POST',{username,password,displayName:'课程演示同学'});
token=(await call('/auth/login','POST',{username,password})).token;
console.log('新建模拟账号：'+username+' / '+password);
const cats=await call('/categories'),exp=cats.find(c=>c.type==='EXPENSE'),inc=cats.find(c=>c.type==='INCOME');
const d=new Date(),date=`${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
for(const amount of [10,12,14,16,18,20,22,24,100])await call('/bills','POST',{categoryId:exp.id,type:'EXPENSE',amount,billDate:date,description:'模拟午餐 '+amount});
await call('/bills','POST',{categoryId:inc.id,type:'INCOME',amount:1000,billDate:date,description:'模拟收入'});
await call('/budgets/'+date.slice(0,7),'PUT',{amount:200});
console.log('已写入10笔账单，收入1000元、支出236元、结余764元、预算200元。');
console.log('已有足够历史样本，100元支出触发IQR提醒。请用新账号登录查看。');
