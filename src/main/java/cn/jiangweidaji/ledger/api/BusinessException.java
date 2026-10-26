package cn.jiangweidaji.ledger.api;
public class BusinessException extends RuntimeException {
 public final int status;
 public BusinessException(int status,String message){super(message);this.status=status;}
 public static BusinessException missing(){return new BusinessException(404,"记录不存在或不可访问");}
}
