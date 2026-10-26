package cn.jiangweidaji.ledger.service;
import java.math.*;
import java.util.*;
public final class IqrDetector {
 private IqrDetector(){}
 public record Result(String status,int sampleCount,BigDecimal q1,BigDecimal q3,BigDecimal iqr,BigDecimal upperBound){}
 public static Result detect(List<BigDecimal> history,BigDecimal current){
  if(history.size()<8)return new Result("INSUFFICIENT_DATA",history.size(),null,null,null,null);
  List<BigDecimal> sorted=history.stream().sorted().toList();
  BigDecimal q1=quantile(sorted,new BigDecimal("0.25"));
  BigDecimal q3=quantile(sorted,new BigDecimal("0.75"));
  BigDecimal iqr=q3.subtract(q1),upper=q3.add(iqr.multiply(new BigDecimal("1.5")));
  return new Result(current.compareTo(upper)>0?"ANOMALY":"NORMAL",sorted.size(),q1,q3,iqr,upper);
 }
 // Type-7线性插值：位置=(n-1)*p，保留小数位置，不将四分位数取整。
 static BigDecimal quantile(List<BigDecimal> sorted,BigDecimal p){
  BigDecimal rank=p.multiply(BigDecimal.valueOf(sorted.size()-1));int lower=rank.intValue();
  if(lower==sorted.size()-1)return sorted.get(lower);
  return sorted.get(lower).add(sorted.get(lower+1).subtract(sorted.get(lower)).multiply(rank.subtract(BigDecimal.valueOf(lower))));
 }
}
