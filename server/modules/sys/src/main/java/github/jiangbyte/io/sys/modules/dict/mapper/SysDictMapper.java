package github.jiangbyte.io.sys.modules.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.sys.modules.dict.entity.SysDict;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据字典表 MyBatis-Plus Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface SysDictMapper extends BaseMapper<SysDict> {
}
