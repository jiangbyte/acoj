package github.jiangbyte.io.profile.modules.identity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.profile.modules.identity.entity.ProfileIdentity;
import org.apache.ibatis.annotations.Mapper;

/**
 * {@link ProfileIdentity} MyBatis-Plus Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface ProfileIdentityMapper extends BaseMapper<ProfileIdentity> {
}
