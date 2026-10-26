package cn.jiangweidaji.ledger.domain;
import com.baomidou.mybatisplus.annotation.*;
@TableName("bill")
public class Bill {
 @TableId(type=IdType.AUTO)
 private Long id;
 private Long userId;
 private Long categoryId;
 private String type;
 private java.math.BigDecimal amount;
 private java.time.LocalDate billDate;
 private String description;
 public Long getId() { return id; }
 public void setId(Long value) { this.id=value; }
 public Long getUserId() { return userId; }
 public void setUserId(Long value) { this.userId=value; }
 public Long getCategoryId() { return categoryId; }
 public void setCategoryId(Long value) { this.categoryId=value; }
 public String getType() { return type; }
 public void setType(String value) { this.type=value; }
 public java.math.BigDecimal getAmount() { return amount; }
 public void setAmount(java.math.BigDecimal value) { this.amount=value; }
 public java.time.LocalDate getBillDate() { return billDate; }
 public void setBillDate(java.time.LocalDate value) { this.billDate=value; }
 public String getDescription() { return description; }
 public void setDescription(String value) { this.description=value; }
}
