package io.charlie.web.modular.data.testcase.mapper;

import io.charlie.web.modular.data.testcase.entity.DataTestCase;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-10-26
* @description 题目测试用例表 Mapper 接口
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface DataTestCaseMapper extends BaseMapper<DataTestCase> {

}
