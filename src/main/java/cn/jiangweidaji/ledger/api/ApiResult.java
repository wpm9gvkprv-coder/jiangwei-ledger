package cn.jiangweidaji.ledger.api;
public record ApiResult<T>(int code, String message, T data) {
 public static <T> ApiResult<T> ok(T data){return new ApiResult<>(0,"成功",data);}
}
