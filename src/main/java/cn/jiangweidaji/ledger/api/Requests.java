package cn.jiangweidaji.ledger.api;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public final class Requests {
 private Requests(){}
 public record Register(@NotBlank @Pattern(regexp="[A-Za-z0-9_]{3,32}") String username,
  @NotBlank @Size(min=8,max=64) @Pattern(regexp="[\\x21-\\x7E]+",message="密码须使用ASCII可见字符") String password,
  @NotBlank @Size(max=40) String displayName){}
 public record Login(@NotBlank @Size(max=32) String username,@NotBlank @Size(max=64) String password){}
 public record Profile(@NotBlank @Size(max=40) String displayName){}
 public record CategoryInput(@NotBlank @Size(max=30) String name,@NotNull @Pattern(regexp="INCOME|EXPENSE") String type){}
 public record BillInput(@NotNull Long categoryId,@NotNull @Pattern(regexp="INCOME|EXPENSE") String type,
  @NotNull @DecimalMin("0.01") @DecimalMax("9999999999.99") @Digits(integer=10,fraction=2) BigDecimal amount,
  @NotNull @PastOrPresent LocalDate billDate,@NotBlank @Size(max=200) String description){}
 public record BudgetInput(@NotNull @DecimalMin("0.01") @DecimalMax("9999999999.99") @Digits(integer=10,fraction=2) BigDecimal amount){}
 public record ClassifyInput(@NotBlank @Size(max=200) String description,@NotNull @Pattern(regexp="INCOME|EXPENSE") String type){}
}
