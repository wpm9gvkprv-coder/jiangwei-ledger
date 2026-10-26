package cn.jiangweidaji.ledger.service;
import java.util.*;
public final class KeywordClassifier {
 private KeywordClassifier(){}
 public record Match(String categoryName,String keyword){}
 private record Rule(String category,String type,String word){}
 private static final List<Rule> RULES=new ArrayList<>();
 static {
  add("餐饮","EXPENSE","外卖","食堂","早餐","午餐","晚餐","奶茶","咖啡","餐厅");
  add("交通","EXPENSE","出租车","高铁","地铁","公交","打车","车票","滴滴");
  add("购物","EXPENSE","超市","淘宝","京东","购物","衣服","书籍");
  add("娱乐","EXPENSE","电影","游戏","KTV","演唱会","游乐场");
  add("工资","INCOME","工资","薪资","薪水","奖金");
 }
 private static void add(String category,String type,String...words){for(String w:words)RULES.add(new Rule(category,type,w));}
 public static Match classify(String description,String type){
  String text=description.toLowerCase(Locale.ROOT);Rule best=null;
  for(Rule rule:RULES)if(rule.type.equals(type)&&text.contains(rule.word.toLowerCase(Locale.ROOT))&&(best==null||rule.word.length()>best.word.length()))best=rule;
  return best==null?null:new Match(best.category,best.word);
 }
}
