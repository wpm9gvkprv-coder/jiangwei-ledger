package cn.jiangweidaji.ledger.security;
import cn.jiangweidaji.ledger.domain.User;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Service
public class TokenService {
 private final byte[] key; private final long minutes;
 public TokenService(@Value("${app.jwt-secret}") String secret,@Value("${app.jwt-minutes:120}") long minutes){
  key=secret.getBytes(StandardCharsets.UTF_8);this.minutes=minutes;
  if(key.length<32)throw new IllegalStateException("JWT_SECRET必须至少32字节");
 }
 public String issue(User user){
  try {
   var now=Instant.now();
   var claims=new JWTClaimsSet.Builder().subject(user.getId().toString()).issuer("jiangweidaji-ledger")
    .issueTime(Date.from(now)).expirationTime(Date.from(now.plusSeconds(minutes*60)))
    .claim("version",user.getTokenVersion()).build();
   var jwt=new SignedJWT(new JWSHeader(JWSAlgorithm.HS256),claims);
   jwt.sign(new MACSigner(key));return jwt.serialize();
  }catch(JOSEException e){throw new IllegalStateException("令牌生成失败",e);}
 }
 public JWTClaimsSet verify(String value) throws Exception {
  var jwt=SignedJWT.parse(value);
  if(!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())||!jwt.verify(new MACVerifier(key)))throw new IllegalArgumentException();
  var claims=jwt.getJWTClaimsSet();
  if(!"jiangweidaji-ledger".equals(claims.getIssuer())||claims.getExpirationTime()==null||!claims.getExpirationTime().after(new Date()))throw new IllegalArgumentException();
  return claims;
 }
}
