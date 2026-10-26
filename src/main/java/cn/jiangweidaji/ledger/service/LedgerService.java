package cn.jiangweidaji.ledger.service;
import cn.jiangweidaji.ledger.api.*;
import cn.jiangweidaji.ledger.domain.*;
import cn.jiangweidaji.ledger.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
@Service
public class LedgerService {
 private final IntelligenceService smart; private final UserMapper users; private final CategoryMapper categories;private final BillMapper bills;private final BudgetMapper budgets;
 public LedgerService(CategoryMapper categories,BillMapper bills,BudgetMapper budgets,IntelligenceService smart,UserMapper users){this.smart=smart;this.users=users;this.categories=categories;this.bills=bills;this.budgets=budgets;}
 public List<Category> categories(Long uid){return categories.selectList(new QueryWrapper<Category>().and(q->q.isNull("user_id").or().eq("user_id",uid)).orderByAsc("id"));}
 public Category category(Long uid,Long id){var c=categories.selectById(id);if(c==null||(c.getUserId()!=null&&!c.getUserId().equals(uid)))throw BusinessException.missing();return c;}
 public Category saveCategory(Long uid,Long id,Requests.CategoryInput in){
  Category c=id==null?new Category():category(uid,id);
  if(id!=null&&c.getUserId()==null)throw new BusinessException(403,"默认分类不可修改");
  String name=in.name().trim();
  if(categories(uid).stream().anyMatch(x->!Objects.equals(x.getId(),id)&&x.getName().equalsIgnoreCase(name)&&x.getType().equals(in.type())))throw new BusinessException(409,"分类名称重复");
  if(id!=null&&!c.getType().equals(in.type())&&bills.selectCount(new QueryWrapper<Bill>().eq("category_id",id))>0)throw new BusinessException(409,"已使用分类不能变更收支类型");
  c.setUserId(uid);c.setName(name);c.setType(in.type());if(id==null)categories.insert(c);else categories.updateById(c);return c;
 }
 public void deleteCategory(Long uid,Long id){var c=category(uid,id);if(c.getUserId()==null)throw new BusinessException(403,"默认分类不可删除");if(bills.selectCount(new QueryWrapper<Bill>().eq("category_id",id))>0)throw new BusinessException(409,"该分类仍有关联账单");categories.deleteById(id);}
 public Bill bill(Long uid,Long id){var b=bills.selectOne(new QueryWrapper<Bill>().eq("id",id).eq("user_id",uid));if(b==null)throw BusinessException.missing();return b;}
 @Transactional public Bill saveBill(Long uid,Long id,Requests.BillInput in){
  users.lockUser(uid);Category c=category(uid,in.categoryId());if(!c.getType().equals(in.type()))throw new BusinessException(400,"账单与分类收支类型不一致");
  Bill b=id==null?new Bill():bill(uid,id);b.setUserId(uid);b.setCategoryId(in.categoryId());b.setType(in.type());b.setAmount(in.amount());b.setBillDate(in.billDate());b.setDescription(in.description().trim());
  if(id==null)bills.insert(b);else bills.updateById(b);smart.rebuild(uid);return b;
 }
 @Transactional public void deleteBill(Long uid,Long id){users.lockUser(uid);bill(uid,id);bills.deleteById(id);smart.rebuild(uid);}
 public Map<String,Object> listBills(Long uid,int page,int size,String type,Long categoryId,LocalDate from,LocalDate to,String keyword){
  if(page<1||page>1000000||size<1||size>100)throw new BusinessException(400,"page须为1至1000000，size须为1至100");
  if(type!=null&&!List.of("INCOME","EXPENSE").contains(type))throw new BusinessException(400,"收支类型不合法");
  if(from!=null&&to!=null&&from.isAfter(to))throw new BusinessException(400,"起始日期不可晚于结束日期");
  if(keyword!=null&&keyword.length()>200)throw new BusinessException(400,"关键词过长");
  var q=new QueryWrapper<Bill>().eq("user_id",uid).eq(type!=null,"type",type).eq(categoryId!=null,"category_id",categoryId).ge(from!=null,"bill_date",from).le(to!=null,"bill_date",to);
  if(keyword!=null&&!keyword.isBlank())q.like("description",keyword);
  long total=bills.selectCount(q);
  q.orderByDesc("bill_date","id").last("LIMIT "+size+" OFFSET "+((long)(page-1)*size));
  return Map.of("records",bills.selectList(q),"total",total,"page",page,"size",size);
 }
 public YearMonth month(String text){try{if(text==null||!text.matches("[0-9]{4}-[0-9]{2}"))throw new IllegalArgumentException();var m=YearMonth.parse(text);if(m.getYear()<1900||m.getYear()>9999)throw new IllegalArgumentException();return m;}catch(DateTimeParseException|IllegalArgumentException e){throw new BusinessException(400,"月份格式须为yyyy-MM，年份范围1900至9999");}}
 public List<Bill> monthBills(Long uid,YearMonth m){return bills.selectList(new QueryWrapper<Bill>().eq("user_id",uid).ge("bill_date",m.atDay(1)).le("bill_date",m.atEndOfMonth()));}
 private BigDecimal sum(List<Bill> rows,String type){return rows.stream().filter(b->b.getType().equals(type)).map(Bill::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add).setScale(2);}
 public Map<String,Object> summary(Long uid,String month){var rows=monthBills(uid,month(month));BigDecimal income=sum(rows,"INCOME"),expense=sum(rows,"EXPENSE");return Map.of("month",month,"income",income,"expense",expense,"balance",income.subtract(expense));}
 public List<Map<String,Object>> categoryStats(Long uid,String month,String type){
  if(!List.of("INCOME","EXPENSE").contains(type))throw new BusinessException(400,"收支类型不合法");
  var rows=monthBills(uid,month(month));BigDecimal total=sum(rows,type);Map<Long,BigDecimal> grouped=new TreeMap<>();
  rows.stream().filter(x->x.getType().equals(type)).forEach(x->grouped.merge(x.getCategoryId(),x.getAmount(),BigDecimal::add));
  List<Map<String,Object>> result=new ArrayList<>();grouped.forEach((id,amount)->result.add(Map.of("categoryId",id,"name",category(uid,id).getName(),"amount",amount,"percentage",total.signum()==0?BigDecimal.ZERO:amount.multiply(new BigDecimal("100")).divide(total,2,RoundingMode.HALF_UP))));return result;
 }
 public List<Map<String,Object>> trends(Long uid,String from,String to){var start=month(from);var end=month(to);long count=java.time.temporal.ChronoUnit.MONTHS.between(start,end);if(count<0||count>23)throw new BusinessException(400,"趋势范围须为1至24个月");List<Map<String,Object>> result=new ArrayList<>();for(var m=start;!m.isAfter(end);m=m.plusMonths(1))result.add(summary(uid,m.toString()));return result;}
 public Map<String,Object> budget(Long uid,String text){
  var m=month(text);var b=budgets.selectOne(new QueryWrapper<Budget>().eq("user_id",uid).eq("budget_month",text));BigDecimal spent=sum(monthBills(uid,m),"EXPENSE");
  Map<String,Object> result=new LinkedHashMap<>();result.put("month",text);result.put("spent",spent);result.put("amount",b==null?null:b.getAmount());
  result.put("usageRate",b==null?null:spent.multiply(new BigDecimal("100")).divide(b.getAmount(),2,RoundingMode.HALF_UP));
  result.put("remaining",b==null?null:b.getAmount().subtract(spent));
  result.put("status",b==null?"NOT_SET":spent.compareTo(b.getAmount())>=0?"EXCEEDED":spent.compareTo(b.getAmount().multiply(new BigDecimal("0.8")))>=0?"NEAR_LIMIT":"NORMAL");return result;
 }
 public Map<String,Object> setBudget(Long uid,String text,Requests.BudgetInput input){month(text);var b=budgets.selectOne(new QueryWrapper<Budget>().eq("user_id",uid).eq("budget_month",text));boolean create=b==null;if(create){b=new Budget();b.setUserId(uid);b.setBudgetMonth(text);}b.setAmount(input.amount());if(create)budgets.insert(b);else budgets.updateById(b);return budget(uid,text);}
}
