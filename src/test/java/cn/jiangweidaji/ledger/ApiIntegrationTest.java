package cn.jiangweidaji.ledger;
import com.fasterxml.jackson.databind.*;
import cn.jiangweidaji.ledger.mapper.UserMapper;
import cn.jiangweidaji.ledger.domain.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles(resolver=TestProfiles.class) @Transactional
class ApiIntegrationTest {
 @Autowired MockMvc mvc; @Autowired ObjectMapper json; @Autowired UserMapper users;
 String token,other;String month=YearMonth.now().toString();String day=LocalDate.now().toString();
 JsonNode call(String method,String path,String auth,Object body,int expected)throws Exception{
  MockHttpServletRequestBuilder req=request(org.springframework.http.HttpMethod.valueOf(method),path);
  if(auth!=null)req.header("Authorization","Bearer "+auth);
  if(body!=null)req.contentType("application/json").content(json.writeValueAsString(body));
  String text=mvc.perform(req).andExpect(status().is(expected)).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
  return json.readTree(text);
 }
 String login(String name)throws Exception{return call("POST","/api/auth/login",null,Map.of("username",name,"password","Password123!"),200).path("data").path("token").asText();}
 @BeforeEach void setup()throws Exception{
  for(String name:List.of("tester_one","tester_two"))call("POST","/api/auth/register",null,Map.of("username",name,"password","Password123!","displayName",name),200);
  token=login("tester_one");other=login("tester_two");
 }
 Map<String,Object> billBody(String amount,long category,String type,String date){return Map.of("amount",amount,"categoryId",category,"type",type,"billDate",date,"description","测试账单");}
 long bill(String amount)throws Exception{return call("POST","/api/bills",token,billBody(amount,1,"EXPENSE",day),200).path("data").path("id").asLong();}
 @Test void authenticationProfileAndPasswordStorage()throws Exception{
  JsonNode u=call("GET","/api/me",token,null,200).path("data");assertThat(u.has("passwordHash")).isFalse();
  assertThat(users.selectOne(new QueryWrapper<User>().eq("username","tester_one")).getPasswordHash()).startsWith("$2");
  assertThat(call("PUT","/api/me",token,Map.of("displayName","王天昊"),200).path("data").path("displayName").asText()).isEqualTo("王天昊");
  call("POST","/api/auth/login",null,Map.of("username","tester_one","password","wrong123"),401);
 }
 @Test void duplicateRegistrationAndPasswordValidation()throws Exception{
  call("POST","/api/auth/register",null,Map.of("username","TESTER_ONE","password","Password123!","displayName","重复"),409);
  call("POST","/api/auth/register",null,Map.of("username","new_user","password","short","displayName","短密码"),400);
  call("POST","/api/auth/register",null,Map.of("username","new_user","password","中文密码测试123","displayName","中文"),400);
 }
 @Test void missingInvalidAndRevokedTokensRejected()throws Exception{
  call("GET","/api/bills",null,null,401);call("GET","/api/bills","invalid",null,401);
  call("GET","/api/bills",token.substring(0,token.length()-5)+"XXXXX",null,401);
  String anotherToken=login("tester_one");call("POST","/api/auth/logout",token,null,200);
  call("GET","/api/me",token,null,401);call("GET","/api/me",anotherToken,null,401);call("GET","/api/me",login("tester_one"),null,200);
 }
 @Test void billCrudPaginationAndFilters()throws Exception{
  long id=bill("12.34");bill("20.00");
  var list=call("GET","/api/bills?page=1&size=1&type=EXPENSE&categoryId=1",token,null,200).path("data");assertThat(list.path("total").asInt()).isEqualTo(2);assertThat(list.path("records").size()).isEqualTo(1);
  call("PUT","/api/bills/"+id,token,billBody("30.50",1,"EXPENSE",day),200);
  assertThat(call("GET","/api/bills/"+id,token,null,200).path("data").path("amount").decimalValue()).isEqualByComparingTo("30.50");
  call("DELETE","/api/bills/"+id,token,null,200);call("GET","/api/bills/"+id,token,null,404);
 }
 @Test void crossUserBillReadWriteAndListsBlocked()throws Exception{
  long id=bill("10.00");call("GET","/api/bills/"+id,other,null,404);call("PUT","/api/bills/"+id,other,billBody("2.00",1,"EXPENSE",day),404);call("DELETE","/api/bills/"+id,other,null,404);
  assertThat(call("GET","/api/bills",other,null,200).path("data").path("total").asInt()).isZero();
 }
 @Test void invalidAmountsDatesTypesAndPaginationRejected()throws Exception{
  for(String amount:List.of("0","-1","1.001","10000000000.00"))call("POST","/api/bills",token,billBody(amount,1,"EXPENSE",day),400);
  call("POST","/api/bills",token,billBody("10",1,"INCOME",day),400);
  call("POST","/api/bills",token,billBody("10",1,"EXPENSE",LocalDate.now().plusDays(1).toString()),400);
  call("GET","/api/bills?page=0",token,null,400);call("GET","/api/bills?size=101",token,null,400);call("GET","/api/bills?from=2026-10-20&to=2026-10-01",token,null,400);
  call("GET","/api/bills?type=BAD",token,null,400);call("GET","/api/bills?from=bad",token,null,400);
 }
 @Test void defaultAndCustomCategoryRules()throws Exception{
  call("DELETE","/api/categories/1",token,null,403);
  call("PUT","/api/categories/1",token,Map.of("name","改名","type","EXPENSE"),403);
  long id=call("POST","/api/categories",token,Map.of("name","学习","type","EXPENSE"),200).path("data").path("id").asLong();
  call("POST","/api/categories",token,Map.of("name","学习","type","EXPENSE"),409);
  call("POST","/api/categories",token,Map.of("name","餐饮","type","EXPENSE"),409);
  call("PUT","/api/categories/"+id,other,Map.of("name","盗改","type","EXPENSE"),404);
  call("POST","/api/bills",other,billBody("10",id,"EXPENSE",day),404);
  call("POST","/api/bills",token,billBody("10",id,"EXPENSE",day),200);
  call("DELETE","/api/categories/"+id,token,null,409);call("PUT","/api/categories/"+id,token,Map.of("name","学习","type","INCOME"),409);
 }
 @Test void categoryRenameAndDeleteUnused()throws Exception{
  long id=call("POST","/api/categories",token,Map.of("name","临时","type","EXPENSE"),200).path("data").path("id").asLong();
  call("PUT","/api/categories/"+id,token,Map.of("name","资料","type","EXPENSE"),200);call("DELETE","/api/categories/"+id,token,null,200);
 }
 @Test void statisticsTotalsCategoryPercentagesAndEmptyMonths()throws Exception{
  bill("30");call("POST","/api/bills",token,billBody("20",2,"EXPENSE",day),200);call("POST","/api/bills",token,billBody("100",6,"INCOME",day),200);
  var s=call("GET","/api/statistics/summary?month="+month,token,null,200).path("data");assertThat(s.path("income").decimalValue()).isEqualByComparingTo("100");assertThat(s.path("expense").decimalValue()).isEqualByComparingTo("50");assertThat(s.path("balance").decimalValue()).isEqualByComparingTo("50");
  var c=call("GET","/api/statistics/categories?month="+month,token,null,200).path("data");assertThat(c.get(0).path("percentage").decimalValue()).isEqualByComparingTo("60");
  var t=call("GET","/api/statistics/trends?from="+YearMonth.now().minusMonths(2)+"&to="+month,token,null,200).path("data");assertThat(t.size()).isEqualTo(3);assertThat(t.get(0).path("expense").asInt()).isZero();
  assertThat(call("GET","/api/statistics/summary?month="+month,other,null,200).path("data").path("expense").asInt()).isZero();
 }
 @Test void budgetBoundariesNotSetAndUserIsolation()throws Exception{
  assertThat(call("GET","/api/budgets/"+month,token,null,200).path("data").path("status").asText()).isEqualTo("NOT_SET");
  call("PUT","/api/budgets/"+month,token,Map.of("amount","100"),200);long id=bill("79.99");
  assertThat(call("GET","/api/budgets/"+month,token,null,200).path("data").path("status").asText()).isEqualTo("NORMAL");
  call("PUT","/api/bills/"+id,token,billBody("80",1,"EXPENSE",day),200);
  assertThat(call("GET","/api/budgets/"+month,token,null,200).path("data").path("status").asText()).isEqualTo("NEAR_LIMIT");
  call("PUT","/api/bills/"+id,token,billBody("100",1,"EXPENSE",day),200);
  var b=call("GET","/api/budgets/"+month,token,null,200).path("data");assertThat(b.path("status").asText()).isEqualTo("EXCEEDED");assertThat(b.path("usageRate").decimalValue()).isEqualByComparingTo("100");
  assertThat(call("GET","/api/budgets/"+month,other,null,200).path("data").path("status").asText()).isEqualTo("NOT_SET");
  call("PUT","/api/budgets/"+month,token,Map.of("amount","0"),400);
 }
 @Test void invalidMonthAndTrendRangeRejected()throws Exception{
  call("GET","/api/statistics/summary?month=2026-13",token,null,400);call("GET","/api/budgets/2026-1",token,null,400);
  call("GET","/api/statistics/trends?from=2026-01&to=2028-01",token,null,400);call("GET","/api/statistics/trends?from=2026-02&to=2026-01",token,null,400);
 }
 @Test void classifierApiSupportsMatchAndNoMatch()throws Exception{
  var r=call("POST","/api/intelligence/classify",token,Map.of("description","学校食堂午餐","type","EXPENSE"),200).path("data");assertThat(r.path("categoryName").asText()).isEqualTo("餐饮");
  assertThat(call("POST","/api/intelligence/classify",token,Map.of("description","未知用途","type","EXPENSE"),200).path("data").path("categoryId").isNull()).isTrue();
 }
 @Test void anomalyLifecycleReadAndCrossUserAccess()throws Exception{
  for(int i=0;i<8;i++)bill(String.valueOf(10+i*2));long id=bill("100");
  var detection=call("GET","/api/intelligence/bills/"+id+"/anomaly",token,null,200).path("data");assertThat(detection.path("upperBound").decimalValue()).isEqualByComparingTo("31");assertThat(detection.path("status").asText()).isEqualTo("ANOMALY");
  var list=call("GET","/api/alerts",token,null,200).path("data");assertThat(list.path("total").asInt()).isEqualTo(1);long alert=list.path("records").get(0).path("id").asLong();
  call("PUT","/api/alerts/"+alert+"/read",other,null,404);call("GET","/api/intelligence/bills/"+id+"/anomaly",other,null,404);call("PUT","/api/alerts/"+alert+"/read",token,null,200);
  assertThat(call("GET","/api/alerts",token,null,200).path("data").path("records").get(0).path("readFlag").asBoolean()).isTrue();
  call("PUT","/api/bills/"+id,token,billBody("20",1,"EXPENSE",day),200);assertThat(call("GET","/api/alerts",token,null,200).path("data").path("total").asInt()).isZero();
 }
 @Test void historicalDeletionRebuildsAlerts()throws Exception{
  long first=0;for(int i=0;i<8;i++){long id=bill("10");if(i==0)first=id;}bill("100");
  assertThat(call("GET","/api/alerts",token,null,200).path("data").path("total").asInt()).isEqualTo(1);
  call("DELETE","/api/bills/"+first,token,null,200);assertThat(call("GET","/api/alerts",token,null,200).path("data").path("total").asInt()).isZero();
 }
 @Test void windowAndFutureRecordsExcluded()throws Exception{
  for(int i=0;i<8;i++)call("POST","/api/bills",token,billBody("10",1,"EXPENSE",LocalDate.now().minusDays(91).toString()),200);
  long id=bill("100");assertThat(call("GET","/api/intelligence/bills/"+id+"/anomaly",token,null,200).path("data").path("sampleCount").asInt()).isZero();
  call("GET","/api/alerts?size=101",token,null,400);
 }
 @Test void malformedJsonAndInjectionLikeKeywordDoNotChangeData()throws Exception{
  mvc.perform(post("/api/bills").header("Authorization","Bearer "+token).contentType("application/json").content("{bad")).andExpect(status().isBadRequest());
  bill("15");mvc.perform(get("/api/bills").param("keyword","' OR 1=1 --").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(0));
  assertThat(call("GET","/api/bills",token,null,200).path("data").path("total").asInt()).isEqualTo(1);
 }
 @Test void corsAllowsConfiguredFrontendOnly()throws Exception{
  mvc.perform(options("/api/bills").header("Origin","http://localhost:5173").header("Access-Control-Request-Method","POST").header("Access-Control-Request-Headers","authorization,content-type")).andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:5173"));
  mvc.perform(options("/api/bills").header("Origin","https://untrusted.invalid").header("Access-Control-Request-Method","POST")).andExpect(status().isForbidden());
 }
}
