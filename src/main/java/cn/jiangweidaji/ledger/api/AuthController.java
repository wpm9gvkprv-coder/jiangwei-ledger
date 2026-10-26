package cn.jiangweidaji.ledger.api;
import cn.jiangweidaji.ledger.domain.User;
import cn.jiangweidaji.ledger.mapper.UserMapper;
import cn.jiangweidaji.ledger.security.TokenService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Locale;
import java.util.Map;
@RestController
@RequestMapping("/api")
public class AuthController {
 private final UserMapper users;private final PasswordEncoder encoder;private final TokenService tokens;
 public AuthController(UserMapper users,PasswordEncoder encoder,TokenService tokens){this.users=users;this.encoder=encoder;this.tokens=tokens;}
 private Map<String,Object> profile(User u){return Map.of("id",u.getId(),"username",u.getUsername(),"displayName",u.getDisplayName());}
 @GetMapping("/health") public ApiResult<?> health(){return ApiResult.ok(Map.of("status","UP"));}
 @PostMapping("/auth/register") public ApiResult<?> register(@Valid @RequestBody Requests.Register input){
  String username=input.username().toLowerCase(Locale.ROOT);
  if(users.selectCount(new QueryWrapper<User>().eq("username",username))>0)throw new BusinessException(409,"用户名已存在");
  User u=new User();u.setUsername(username);u.setPasswordHash(encoder.encode(input.password()));u.setDisplayName(input.displayName().trim());u.setTokenVersion(0);users.insert(u);
  return ApiResult.ok(profile(u));
 }
 @PostMapping("/auth/login") public ApiResult<?> login(@Valid @RequestBody Requests.Login input){
  User u=users.selectOne(new QueryWrapper<User>().eq("username",input.username().toLowerCase(Locale.ROOT)));
  if(u==null||!encoder.matches(input.password(),u.getPasswordHash()))throw new BusinessException(401,"用户名或密码错误");
  return ApiResult.ok(Map.of("token",tokens.issue(u),"tokenType","Bearer","user",profile(u)));
 }
 @PostMapping("/auth/logout") public ApiResult<?> logout(@AuthenticationPrincipal Long id){
  users.update(null,new UpdateWrapper<User>().eq("id",id).setSql("token_version = token_version + 1"));return ApiResult.ok(null);
 }
 @GetMapping("/me") public ApiResult<?> me(@AuthenticationPrincipal Long id){return ApiResult.ok(profile(users.selectById(id)));}
 @PutMapping("/me") public ApiResult<?> edit(@AuthenticationPrincipal Long id,@Valid @RequestBody Requests.Profile input){
  User u=users.selectById(id);u.setDisplayName(input.displayName().trim());users.updateById(u);return ApiResult.ok(profile(u));
 }
}
