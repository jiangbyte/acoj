package github.jiangbyte.io.sys.modules.weakpassword.convert;

import github.jiangbyte.io.sys.modules.weakpassword.entity.SysWeakPassword;
import github.jiangbyte.io.sys.modules.weakpassword.param.SysWeakPasswordAddParam;
import github.jiangbyte.io.sys.modules.weakpassword.param.SysWeakPasswordEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

/**
 * 弱密码库 MapStruct 转换：入参与实体映射。
 *
 * Author: Charlie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysWeakPasswordConvert {

    /** 新增入参转实体。 */
    SysWeakPassword toEntity(SysWeakPasswordAddParam param);

    /** 编辑入参更新到实体。 */
    void update(SysWeakPasswordEditParam param, @MappingTarget SysWeakPassword entity);

}
