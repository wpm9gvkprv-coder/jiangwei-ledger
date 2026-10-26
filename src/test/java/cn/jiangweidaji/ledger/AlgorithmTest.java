package cn.jiangweidaji.ledger;
import cn.jiangweidaji.ledger.service.*;
import cn.jiangweidaji.ledger.security.TokenService;
import cn.jiangweidaji.ledger.domain.User;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
class AlgorithmTest {
 private List<BigDecimal> values(String...v){return Arrays.stream(v).map(BigDecimal::new).toList();}
 @Test void iqrKnownExampleAndExactBoundary(){
  var data=values("10","12","14","16","18","20","22","24");
  var a=IqrDetector.detect(data,new BigDecimal("31.01"));
  assertThat(a.q1()).isEqualByComparingTo("13.5");assertThat(a.q3()).isEqualByComparingTo("20.5");assertThat(a.upperBound()).isEqualByComparingTo("31");assertThat(a.status()).isEqualTo("ANOMALY");
  assertThat(IqrDetector.detect(data,new BigDecimal("31")).status()).isEqualTo("NORMAL");
 }
 @Test void insufficientSamplesNeverFlag(){var r=IqrDetector.detect(values("1","2","3","4","5","6","7"),new BigDecimal("9999"));assertThat(r.status()).isEqualTo("INSUFFICIENT_DATA");assertThat(r.upperBound()).isNull();}
 @Test void equalHistoryAndDecimalPrecision(){var h=Collections.nCopies(8,new BigDecimal("10.10"));assertThat(IqrDetector.detect(h,new BigDecimal("10.10")).status()).isEqualTo("NORMAL");assertThat(IqrDetector.detect(h,new BigDecimal("10.11")).status()).isEqualTo("ANOMALY");}
 @Test void longestKeywordAndStableTie(){assertThat(KeywordClassifier.classify("出租车和外卖","EXPENSE").categoryName()).isEqualTo("交通");assertThat(KeywordClassifier.classify("电影外卖","EXPENSE").categoryName()).isEqualTo("餐饮");}
 @Test void unmatchedAndIncomeIsolation(){assertThat(KeywordClassifier.classify("不明用途","EXPENSE")).isNull();assertThat(KeywordClassifier.classify("工资","EXPENSE")).isNull();assertThat(KeywordClassifier.classify("发工资","INCOME").categoryName()).isEqualTo("工资");assertThat(KeywordClassifier.classify("ktv","EXPENSE").categoryName()).isEqualTo("娱乐");}
 @Test void expiredTokensRejected(){var service=new TokenService("test-only-secret-with-at-least-32-bytes",-1);User u=new User();u.setId(1L);u.setTokenVersion(0);String t=service.issue(u);assertThatThrownBy(()->service.verify(t)).isInstanceOf(Exception.class);}
}
