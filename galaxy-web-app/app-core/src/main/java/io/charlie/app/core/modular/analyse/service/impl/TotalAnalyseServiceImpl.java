package io.charlie.app.core.modular.analyse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.charlie.app.core.modular.analyse.entity.TotalAnalyseData;
import io.charlie.app.core.modular.analyse.service.TotalAnalyseService;
import io.charlie.app.core.modular.problem.problem.mapper.ProProblemMapper;
import io.charlie.app.core.modular.problem.submit.mapper.ProSubmitMapper;
import io.charlie.app.core.modular.set.set.mapper.ProSetMapper;
import io.charlie.app.core.modular.sys.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 03/08/2025
 * @description TODO
 */
@Service
@RequiredArgsConstructor
public class TotalAnalyseServiceImpl implements TotalAnalyseService {
    private final ProSubmitMapper proSubmitMapper;
    private final ProProblemMapper proProblemMapper;
    private final SysUserMapper sysUserMapper;
    private final ProSetMapper proSetMapper;

    @Override
    public TotalAnalyseData getTotalAnalyse() {
        TotalAnalyseData totalAnalyseData = new TotalAnalyseData();
        // 总提交数
        totalAnalyseData.setTotalSubmitCount(proSubmitMapper.selectCount(new QueryWrapper<>()));
        // 总题目数
        totalAnalyseData.setTotalProblemCount(proProblemMapper.selectCount(new QueryWrapper<>()));
        // 总用户数
        totalAnalyseData.setTotalUserCount(sysUserMapper.selectCount(new QueryWrapper<>()));
        // 总题集数
        totalAnalyseData.setTotalSetCount(proSetMapper.selectCount(new QueryWrapper<>()));
        return totalAnalyseData;
    }
}
