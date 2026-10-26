package cn.jiangweidaji.ledger.domain;
import com.baomidou.mybatisplus.annotation.*;
@TableName("consumption_alert")
public class Alert {
 @TableId(type=IdType.AUTO)
 private Long id;
 private Long userId;
 private Long billId;
 private java.math.BigDecimal amount;
 private java.math.BigDecimal thresholdAmount;
 private Integer sampleCount;
 private String reason;
 private Boolean readFlag;
 public Long getId() { return id; }
 public void setId(Long value) { this.id=value; }
 public Long getUserId() { return userId; }
 public void setUserId(Long value) { this.userId=value; }
 public Long getBillId() { return billId; }
 public void setBillId(Long value) { this.billId=value; }
 public java.math.BigDecimal getAmount() { return amount; }
 public void setAmount(java.math.BigDecimal value) { this.amount=value; }
 public java.math.BigDecimal getThresholdAmount() { return thresholdAmount; }
 public void setThresholdAmount(java.math.BigDecimal value) { this.thresholdAmount=value; }
 public Integer getSampleCount() { return sampleCount; }
 public void setSampleCount(Integer value) { this.sampleCount=value; }
 public String getReason() { return reason; }
 public void setReason(String value) { this.reason=value; }
 public Boolean getReadFlag() { return readFlag; }
 public void setReadFlag(Boolean value) { this.readFlag=value; }
}
