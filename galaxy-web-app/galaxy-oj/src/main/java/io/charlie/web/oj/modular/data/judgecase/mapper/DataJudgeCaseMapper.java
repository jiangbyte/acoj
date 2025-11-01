package io.charlie.web.oj.modular.data.judgecase.mapper;

import io.charlie.web.oj.modular.data.judgecase.entity.DataJudgeCase;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-10-26
* @description 判题结果用例表 Mapper 接口
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface DataJudgeCaseMapper extends BaseMapper<DataJudgeCase> {

}
