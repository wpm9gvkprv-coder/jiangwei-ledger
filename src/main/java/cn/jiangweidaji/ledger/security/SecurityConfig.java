package cn.jiangweidaji.ledger.security;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.*;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import java.util.List;
import java.util.Arrays;
@Configuration
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean FilterRegistrationBean<JwtFilter> disableContainerRegistration(JwtFilter filter){
  var bean=new FilterRegistrationBean<>(filter);bean.setEnabled(false);return bean;
 }
 @Bean SecurityFilterChain chain(HttpSecurity http,JwtFilter filter)throws Exception{
  return http.csrf(c->c.disable()).cors(c->{})
   .sessionManagement(c->c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(c->c.requestMatchers("/api/auth/register","/api/auth/login","/api/health").permitAll().anyRequest().authenticated())
   .exceptionHandling(c->c.authenticationEntryPoint((req,res,e)->{res.setStatus(401);res.setContentType("application/json;charset=UTF-8");res.getWriter().write("{\"code\":401,\"message\":\"请登录或重新登录\",\"data\":null}");})
    .accessDeniedHandler((req,res,e)->{res.setStatus(403);res.setContentType("application/json;charset=UTF-8");res.getWriter().write("{\"code\":403,\"message\":\"无访问权限\",\"data\":null}");}))
   .addFilterBefore(filter,UsernamePasswordAuthenticationFilter.class).build();
 }
 @Bean CorsConfigurationSource cors(@Value("${app.cors-origins}") String origins){
  var cfg=new CorsConfiguration();cfg.setAllowedOrigins(Arrays.asList(origins.split(",")));
  cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));cfg.setAllowedHeaders(List.of("Authorization","Content-Type"));cfg.setMaxAge(3600L);
  var source=new UrlBasedCorsConfigurationSource();source.registerCorsConfiguration("/api/**",cfg);return source;
 }
}
