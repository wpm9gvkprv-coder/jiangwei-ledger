package cn.jiangweidaji.ledger.domain;
import com.baomidou.mybatisplus.annotation.*;
@TableName("budget")
public class Budget {
 @TableId(type=IdType.AUTO)
 private Long id;
 private Long userId;
 private String budgetMonth;
 private java.math.BigDecimal amount;
 public Long getId() { return id; }
 public void setId(Long value) { this.id=value; }
 public Long getUserId() { return userId; }
 public void setUserId(Long value) { this.userId=value; }
 public String getBudgetMonth() { return budgetMonth; }
 public void setBudgetMonth(String value) { this.budgetMonth=value; }
 public java.math.BigDecimal getAmount() { return amount; }
 public void setAmount(java.math.BigDecimal value) { this.amount=value; }
}
