package cn.jiangweidaji.ledger.domain;
import com.baomidou.mybatisplus.annotation.*;
@TableName("category")
public class Category {
 @TableId(type=IdType.AUTO)
 private Long id;
 private Long userId;
 private String name;
 private String type;
 public Long getId() { return id; }
 public void setId(Long value) { this.id=value; }
 public Long getUserId() { return userId; }
 public void setUserId(Long value) { this.userId=value; }
 public String getName() { return name; }
 public void setName(String value) { this.name=value; }
 public String getType() { return type; }
 public void setType(String value) { this.type=value; }
}
