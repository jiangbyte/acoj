package github.jiangbyte.io.iam.modules.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.iam.modules.role.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色表 MyBatis-Plus Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
}
