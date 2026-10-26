package cn.jiangweidaji.ledger.security;
import cn.jiangweidaji.ledger.mapper.UserMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
@Component
public class JwtFilter extends OncePerRequestFilter {
 private final TokenService tokens; private final UserMapper users;
 public JwtFilter(TokenService tokens,UserMapper users){this.tokens=tokens;this.users=users;}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  String header=req.getHeader("Authorization");
  if(header!=null&&header.startsWith("Bearer ")){
   try{
    var claims=tokens.verify(header.substring(7));
    var user=users.selectById(Long.parseLong(claims.getSubject()));
    if(user!=null&&user.getTokenVersion().equals(claims.getIntegerClaim("version")))
     SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.getId(),null,List.of()));
   }catch(Exception ignored){SecurityContextHolder.clearContext();}
  }
  chain.doFilter(req,res);
 }
}
