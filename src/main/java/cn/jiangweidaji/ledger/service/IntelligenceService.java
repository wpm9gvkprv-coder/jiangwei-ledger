package cn.jiangweidaji.ledger.service;
import cn.jiangweidaji.ledger.api.*;
import cn.jiangweidaji.ledger.domain.*;
import cn.jiangweidaji.ledger.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import java.util.*;
import java.math.*;
@Service
public class IntelligenceService {
 private final BillMapper bills;private final CategoryMapper categories;private final AlertMapper alerts;
 public IntelligenceService(BillMapper bills,CategoryMapper categories,AlertMapper alerts){this.bills=bills;this.categories=categories;this.alerts=alerts;}
 public record Suggestion(String status,Long categoryId,String categoryName,String matchedKeyword,String explanation){}
 public Suggestion classify(String description,String type){
  var match=KeywordClassifier.classify(description,type);
  if(match==null)return new Suggestion("NO_MATCH",null,null,null,"未匹配关键词，请手动选择类别");
  var category=categories.selectOne(new QueryWrapper<Category>().isNull("user_id").eq("name",match.categoryName()).eq("type",type));
  return new Suggestion("MATCHED",category.getId(),category.getName(),match.keyword(),"按最长关键词优先，同长度按固定规则顺序推荐；由用户确认");
 }
 private List<Bill> expenses(Long uid){return bills.selectList(new QueryWrapper<Bill>().eq("user_id",uid).eq("type","EXPENSE").orderByAsc("bill_date","id"));}
 private IqrDetector.Result result(List<Bill> rows,Bill target){
  var start=target.getBillDate().minusDays(90);
  var history=rows.stream().filter(b->!b.getBillDate().isBefore(start))
   .filter(b->b.getBillDate().isBefore(target.getBillDate())||(b.getBillDate().equals(target.getBillDate())&&b.getId()<target.getId()))
   .map(Bill::getAmount).toList();
  return IqrDetector.detect(history,target.getAmount());
 }
 public Object detect(Long uid,Long id){
  Bill b=bills.selectOne(new QueryWrapper<Bill>().eq("id",id).eq("user_id",uid));if(b==null)throw BusinessException.missing();
  if(!"EXPENSE".equals(b.getType()))throw new BusinessException(400,"仅对支出账单进行异常检测");return result(expenses(uid),b);
 }
 // 账单变更后重算历史依赖，避免修改/删除历史账单后保留失效提醒。由外层事务调用。
 public void rebuild(Long uid){
  var rows=expenses(uid);Map<Long,Alert> existing=new HashMap<>();
  alerts.selectList(new QueryWrapper<Alert>().eq("user_id",uid)).forEach(a->existing.put(a.getBillId(),a));
  for(Bill b:rows){var r=result(rows,b);if(!"ANOMALY".equals(r.status()))continue;
   Alert a=existing.remove(b.getId());boolean create=a==null;
   if(create){a=new Alert();a.setUserId(uid);a.setBillId(b.getId());a.setReadFlag(false);}
   else if(a.getAmount().compareTo(b.getAmount())!=0||a.getThresholdAmount().compareTo(r.upperBound())!=0)a.setReadFlag(false);
   a.setAmount(b.getAmount());a.setThresholdAmount(r.upperBound());a.setSampleCount(r.sampleCount());a.setReason("支出超过历史四分位上界Q3+1.5×IQR，请核对；该提醒不代表确定异常");
   if(create)alerts.insert(a);else alerts.updateById(a);
  }
  existing.values().forEach(a->alerts.deleteById(a.getId()));
 }
 public Map<String,Object> alerts(Long uid,int page,int size){
  if(page<1||page>1000000||size<1||size>100)throw new BusinessException(400,"分页参数不合法");
  var q=new QueryWrapper<Alert>().eq("user_id",uid);long total=alerts.selectCount(q);
  return Map.of("total",total,"page",page,"size",size,"records",alerts.selectList(q.orderByDesc("id").last("LIMIT "+size+" OFFSET "+((long)(page-1)*size))));
 }
 public void read(Long uid,Long id){var a=alerts.selectOne(new QueryWrapper<Alert>().eq("user_id",uid).eq("id",id));if(a==null)throw BusinessException.missing();a.setReadFlag(true);alerts.updateById(a);}
}
