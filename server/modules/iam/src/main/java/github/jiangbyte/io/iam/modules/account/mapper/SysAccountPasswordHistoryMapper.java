package github.jiangbyte.io.iam.modules.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.iam.modules.account.entity.SysAccountPasswordHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账号密码历史表 MyBatis-Plus Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface SysAccountPasswordHistoryMapper extends BaseMapper<SysAccountPasswordHistory> {
}
