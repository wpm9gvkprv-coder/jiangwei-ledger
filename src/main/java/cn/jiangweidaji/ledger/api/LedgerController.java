package cn.jiangweidaji.ledger.api;
import cn.jiangweidaji.ledger.service.LedgerService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api")
public class LedgerController {
 private final LedgerService ledger;
 public LedgerController(LedgerService ledger){this.ledger=ledger;}
 @GetMapping("/categories") public ApiResult<?> categories(@AuthenticationPrincipal Long uid){return ApiResult.ok(ledger.categories(uid));}
 @PostMapping("/categories") public ApiResult<?> addCategory(@AuthenticationPrincipal Long uid,@Valid @RequestBody Requests.CategoryInput in){return ApiResult.ok(ledger.saveCategory(uid,null,in));}
 @PutMapping("/categories/{id}") public ApiResult<?> editCategory(@AuthenticationPrincipal Long uid,@PathVariable Long id,@Valid @RequestBody Requests.CategoryInput in){return ApiResult.ok(ledger.saveCategory(uid,id,in));}
 @DeleteMapping("/categories/{id}") public ApiResult<?> deleteCategory(@AuthenticationPrincipal Long uid,@PathVariable Long id){ledger.deleteCategory(uid,id);return ApiResult.ok(null);}
 @GetMapping("/bills") public ApiResult<?> bills(@AuthenticationPrincipal Long uid,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size,@RequestParam(required=false) String type,@RequestParam(required=false) Long categoryId,@RequestParam(required=false) LocalDate from,@RequestParam(required=false) LocalDate to,@RequestParam(required=false) String keyword){return ApiResult.ok(ledger.listBills(uid,page,size,type,categoryId,from,to,keyword));}
 @GetMapping("/bills/{id}") public ApiResult<?> bill(@AuthenticationPrincipal Long uid,@PathVariable Long id){return ApiResult.ok(ledger.bill(uid,id));}
 @PostMapping("/bills") public ApiResult<?> addBill(@AuthenticationPrincipal Long uid,@Valid @RequestBody Requests.BillInput in){return ApiResult.ok(ledger.saveBill(uid,null,in));}
 @PutMapping("/bills/{id}") public ApiResult<?> editBill(@AuthenticationPrincipal Long uid,@PathVariable Long id,@Valid @RequestBody Requests.BillInput in){return ApiResult.ok(ledger.saveBill(uid,id,in));}
 @DeleteMapping("/bills/{id}") public ApiResult<?> deleteBill(@AuthenticationPrincipal Long uid,@PathVariable Long id){ledger.deleteBill(uid,id);return ApiResult.ok(null);}
 @GetMapping("/statistics/summary") public ApiResult<?> summary(@AuthenticationPrincipal Long uid,@RequestParam String month){return ApiResult.ok(ledger.summary(uid,month));}
 @GetMapping("/statistics/categories") public ApiResult<?> categoryStats(@AuthenticationPrincipal Long uid,@RequestParam String month,@RequestParam(defaultValue="EXPENSE") String type){return ApiResult.ok(ledger.categoryStats(uid,month,type));}
 @GetMapping("/statistics/trends") public ApiResult<?> trends(@AuthenticationPrincipal Long uid,@RequestParam String from,@RequestParam String to){return ApiResult.ok(ledger.trends(uid,from,to));}
 @GetMapping("/budgets/{month}") public ApiResult<?> budget(@AuthenticationPrincipal Long uid,@PathVariable String month){return ApiResult.ok(ledger.budget(uid,month));}
 @PutMapping("/budgets/{month}") public ApiResult<?> budget(@AuthenticationPrincipal Long uid,@PathVariable String month,@Valid @RequestBody Requests.BudgetInput in){return ApiResult.ok(ledger.setBudget(uid,month,in));}
}
