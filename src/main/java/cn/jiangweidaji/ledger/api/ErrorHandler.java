package cn.jiangweidaji.ledger.api;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;
@RestControllerAdvice
public class ErrorHandler {
 @ExceptionHandler(BusinessException.class)
 public ResponseEntity<?> business(BusinessException e){return error(e.status,e.getMessage());}
 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<?> validation(MethodArgumentNotValidException e){return error(400,e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).sorted().findFirst().orElse("参数不合法"));}
 @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class,ConstraintViolationException.class})
 public ResponseEntity<?> malformed(Exception e){return error(400,"请求格式或参数不合法");}
 @ExceptionHandler(DataIntegrityViolationException.class)
 public ResponseEntity<?> conflict(Exception e){return error(409,"数据重复或仍被其他记录引用");}
 private ResponseEntity<?> error(int code,String msg){return ResponseEntity.status(code).body(new ApiResult<>(code,msg,null));}
}
