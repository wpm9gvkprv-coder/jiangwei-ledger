package cn.jiangweidaji.ledger.domain;
import com.baomidou.mybatisplus.annotation.*;
@TableName("app_user")
public class User {
 @TableId(type=IdType.AUTO)
 private Long id;
 private String username;
 private String passwordHash;
 private String displayName;
 private Integer tokenVersion;
 public Long getId() { return id; }
 public void setId(Long value) { this.id=value; }
 public String getUsername() { return username; }
 public void setUsername(String value) { this.username=value; }
 public String getPasswordHash() { return passwordHash; }
 public void setPasswordHash(String value) { this.passwordHash=value; }
 public String getDisplayName() { return displayName; }
 public void setDisplayName(String value) { this.displayName=value; }
 public Integer getTokenVersion() { return tokenVersion; }
 public void setTokenVersion(Integer value) { this.tokenVersion=value; }
}
