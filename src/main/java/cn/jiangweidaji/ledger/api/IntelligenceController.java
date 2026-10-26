package cn.jiangweidaji.ledger.api;
import cn.jiangweidaji.ledger.service.IntelligenceService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api")
public class IntelligenceController {
 private final IntelligenceService smart;
 public IntelligenceController(IntelligenceService smart){this.smart=smart;}
 @PostMapping("/intelligence/classify") public ApiResult<?> classify(@Valid @RequestBody Requests.ClassifyInput input){return ApiResult.ok(smart.classify(input.description(),input.type()));}
 @GetMapping("/intelligence/bills/{id}/anomaly") public ApiResult<?> detect(@AuthenticationPrincipal Long uid,@PathVariable Long id){return ApiResult.ok(smart.detect(uid,id));}
 @GetMapping("/alerts") public ApiResult<?> alerts(@AuthenticationPrincipal Long uid,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size){return ApiResult.ok(smart.alerts(uid,page,size));}
 @PutMapping("/alerts/{id}/read") public ApiResult<?> read(@AuthenticationPrincipal Long uid,@PathVariable Long id){smart.read(uid,id);return ApiResult.ok(null);}
}
