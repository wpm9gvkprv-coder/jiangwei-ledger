package cn.jiangweidaji.ledger;
import org.springframework.test.context.ActiveProfilesResolver;
public class TestProfiles implements ActiveProfilesResolver {
 @Override public String[] resolve(Class<?> testClass) {
  return Boolean.getBoolean("ledger.mysql") ? new String[]{"test","mysqltest"} : new String[]{"test"};
 }
}
