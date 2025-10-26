package io.charlie.web.oj.modular.sys.analyse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.sys.analyse.entity.TodayTotal;
import io.charlie.web.oj.modular.sys.analyse.service.TodayTotalService;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.charlie.web.oj.modular.sys.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 17/10/2025
 */
@Service
@RequiredArgsConstructor
public class TodayTotalServiceImpl implements TodayTotalService {
    private final DataSubmitMapper dataSubmitMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public TodayTotal getTodayTotal() {
        // 获取今天的开始时间和结束时间
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        // 转换为Date类型
        Date startDate = Date.from(todayStart.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(todayEnd.atZone(ZoneId.systemDefault()).toInstant());

        // 获得今天的提交数
        Long submissionCount = dataSubmitMapper.selectCount(
                new LambdaQueryWrapper<DataSubmit>()
                        .ge(DataSubmit::getCreateTime, startDate)
                        .le(DataSubmit::getCreateTime, endDate)
        );

        // 获得今天的新用户数
        Long newUserCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .ge(SysUser::getCreateTime, startDate)
                        .le(SysUser::getCreateTime, endDate)
        );

        // 创建并返回TodayTotal对象
        TodayTotal todayTotal = new TodayTotal();
        todayTotal.setTodaySubmitCount(submissionCount != null ? submissionCount : 0L);
        todayTotal.setTodayNewUserCount(newUserCount != null ? newUserCount : 0L);

        return todayTotal;
    }
}
