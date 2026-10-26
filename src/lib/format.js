export const money = value => new Intl.NumberFormat('zh-CN',{style:'currency',currency:'CNY',minimumFractionDigits:2,maximumFractionDigits:2}).format(Number(value ?? 0))
export function today(){const d=new Date();return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`}
export const currentMonth=()=>today().slice(0,7)
export function shiftMonth(month,n){const [y,m]=month.split('-').map(Number);const d=new Date(y,m-1+n,1);return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}`}
export function monthRange(month){const end=shiftMonth(month,1);const d=new Date(Number(end.slice(0,4)),Number(end.slice(5))-1,0);return [month+'-01',month+'-'+String(d.getDate()).padStart(2,'0')]}
export function validMoney(value){const s=String(value).trim();if(!/^(?:0|[1-9]\d{0,9})(?:\.\d{1,2})?$/.test(s))return false;return BigInt(s.replace('.','')||'0')>0n}
export const moneyRule={validator:(_r,v,cb)=>validMoney(v)?cb():cb(new Error('请输入0.01至9999999999.99之间的金额，最多两位小数')),trigger:'blur'}
export const typeName=t=>t==='INCOME'?'收入':'支出'
export const budgetText=b=>b?.status==='NOT_SET'?'尚未设置预算':b?.status==='EXCEEDED'?(Number(b.remaining)<0?'已超出预算':'预算已用完'):b?.status==='NEAR_LIMIT'?'接近预算上限':'预算使用正常'
export const pct=v=>Math.min(100,Math.max(0,Number(v)||0))
export function categoryTone(name=''){if(/餐|食/.test(name))return 'orange';if(/交通/.test(name))return 'blue';if(/购物/.test(name))return 'purple';if(/娱乐/.test(name))return 'pink';return 'green'}
