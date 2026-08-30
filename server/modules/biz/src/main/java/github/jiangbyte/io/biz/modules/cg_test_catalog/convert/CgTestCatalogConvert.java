package github.jiangbyte.io.biz.modules.cg_test_catalog.convert;

/**
 * Catalog MapStruct 转换：入参与实体映射。
 *
 * Author: Charlie
 */

import github.jiangbyte.io.biz.modules.cg_test_catalog.entity.CgTestCatalog;
import github.jiangbyte.io.biz.modules.cg_test_catalog.param.CgTestCatalogAddParam;
import github.jiangbyte.io.biz.modules.cg_test_catalog.param.CgTestCatalogEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CgTestCatalogConvert {

    /** 新增入参转实体。 */
    CgTestCatalog toEntity(CgTestCatalogAddParam param);

    /** 编辑入参更新到实体。 */
    void update(CgTestCatalogEditParam param, @MappingTarget CgTestCatalog entity);
}
