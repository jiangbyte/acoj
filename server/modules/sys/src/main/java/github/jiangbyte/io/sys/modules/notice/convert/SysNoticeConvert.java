package github.jiangbyte.io.sys.modules.notice.convert;

import github.jiangbyte.io.sys.modules.notice.entity.SysNotice;
import github.jiangbyte.io.sys.modules.notice.param.SysNoticeAddParam;
import github.jiangbyte.io.sys.modules.notice.param.SysNoticeEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

/**
 * 公告/通知对象转换：新增/编辑参数与 {@link github.jiangbyte.io.sys.modules.notice.entity.SysNotice} 实体的 MapStruct 映射。
 *
 * Author: Charlie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysNoticeConvert {

    /**
     * 将新增参数映射为消息实体。
     */
    SysNotice toEntity(SysNoticeAddParam param);

    /**
     * 将编辑参数覆盖到已有消息实体。
     */
    void update(SysNoticeEditParam param, @MappingTarget SysNotice entity);

}
