package github.jiangbyte.io.profile.modules.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.profile.modules.admin.entity.ProfileUserAdmin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理端用户资料表 {@code profile_user_admin} 的 MyBatis-Plus Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface ProfileUserAdminMapper extends BaseMapper<ProfileUserAdmin> {
}
