package cn.jiangweidaji.ledger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(exclude=org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
public class LedgerApplication {
 public static void main(String[] args) { SpringApplication.run(LedgerApplication.class,args); }
}
