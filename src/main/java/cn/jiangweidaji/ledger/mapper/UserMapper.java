package cn.jiangweidaji.ledger.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.jiangweidaji.ledger.domain.User;
import org.apache.ibatis.annotations.Mapper;
@Mapper public interface UserMapper extends BaseMapper<User> {
 @org.apache.ibatis.annotations.Select("SELECT * FROM app_user WHERE id=#{id} FOR UPDATE")
 User lockUser(@org.apache.ibatis.annotations.Param("id") Long id);
}
