package github.jiangbyte.io.iam.modules.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.iam.modules.account.entity.SysAccountOauthBinding;
import org.apache.ibatis.annotations.Mapper;

/**
 * 三方登录绑定表 Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface SysAccountOauthBindingMapper extends BaseMapper<SysAccountOauthBinding> {
}
