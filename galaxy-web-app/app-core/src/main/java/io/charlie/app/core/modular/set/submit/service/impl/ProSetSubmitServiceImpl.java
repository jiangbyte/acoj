package io.charlie.app.core.modular.set.submit.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.judge.dto.JudgeSubmitDto;
import io.charlie.app.core.modular.judge.enums.JudgeStatus;
import io.charlie.app.core.modular.judge.service.SetJudgeMessageService;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.problem.problem.mapper.ProProblemMapper;
import io.charlie.app.core.modular.set.solved.entity.ProSetSolved;
import io.charlie.app.core.modular.set.solved.mapper.ProSetSolvedMapper;
import io.charlie.app.core.modular.set.submit.entity.ProSetSubmit;
import io.charlie.app.core.modular.set.submit.param.*;
import io.charlie.app.core.modular.set.submit.mapper.ProSetSubmitMapper;
import io.charlie.app.core.modular.set.submit.service.ProSetSubmitService;
import io.charlie.app.core.modular.sys.user.entity.SysUser;
import io.charlie.app.core.modular.sys.user.mapper.SysUserMapper;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.galaxy.utils.str.GaStringUtil;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 题单提交表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSetSubmitServiceImpl extends ServiceImpl<ProSetSubmitMapper, ProSetSubmit> implements ProSetSubmitService {
    private final ProProblemMapper proProblemMapper;
    private final SysUserMapper sysUserMapper;
    private final ProSetSolvedMapper proSetSolvedMapper;
    private final SetJudgeMessageService setJudgeMessageService;

    @Override
    public Page<ProSetSubmit> page(ProSetSubmitPageParam proSetSubmitPageParam) {
        QueryWrapper<ProSetSubmit> queryWrapper = new QueryWrapper<ProSetSubmit>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(proSetSubmitPageParam.getKeyword())) {
            List<SysUser> list = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>().like(SysUser::getNickname, proSetSubmitPageParam.getKeyword()));
            if (ObjectUtil.isNotEmpty(list)) {
                // 提取出用户ID(去重)
                List<String> userIds = CollStreamUtil.toList(list, SysUser::getId).stream().distinct().toList();
                queryWrapper.lambda().in(ProSetSubmit::getUserId, userIds);
            } else {
                queryWrapper.lambda().eq(ProSetSubmit::getUserId, "-1");
            }
        }
        if (ObjectUtil.isNotEmpty(proSetSubmitPageParam.getProblem())) {
            List<ProProblem> list = proProblemMapper.selectList(new LambdaQueryWrapper<ProProblem>().like(ProProblem::getTitle, proSetSubmitPageParam.getProblem()));
            if (ObjectUtil.isNotEmpty(list)) {
                List<String> problemList = CollStreamUtil.toList(list, ProProblem::getId).stream().distinct().toList();
                queryWrapper.lambda().in(ProSetSubmit::getProblemId, problemList);
            } else {
                queryWrapper.lambda().eq(ProSetSubmit::getProblemId, "-1");
            }
        }
        if (GaStringUtil.isNotEmpty(proSetSubmitPageParam.getLanguage())) {
            queryWrapper.lambda().eq(ProSetSubmit::getLanguage, proSetSubmitPageParam.getLanguage());
        }
        if (GaStringUtil.isNotEmpty(proSetSubmitPageParam.getStatus())) {
            queryWrapper.lambda().eq(ProSetSubmit::getStatus, proSetSubmitPageParam.getStatus());
        }
        if (ObjectUtil.isNotEmpty(proSetSubmitPageParam.getSubmitType())) {
            queryWrapper.lambda().eq(ProSetSubmit::getSubmitType, proSetSubmitPageParam.getSubmitType());
        }

        if (ObjectUtil.isAllNotEmpty(proSetSubmitPageParam.getSortField(), proSetSubmitPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSetSubmitPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSetSubmitPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSetSubmitPageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByDesc(ProSetSubmit::getCreateTime);
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSetSubmitPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSetSubmitPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSetSubmitAddParam proSetSubmitAddParam) {
        ProSetSubmit bean = BeanUtil.toBean(proSetSubmitAddParam, ProSetSubmit.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSetSubmitEditParam proSetSubmitEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSetSubmit>().eq(ProSetSubmit::getId, proSetSubmitEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSetSubmit bean = BeanUtil.toBean(proSetSubmitEditParam, ProSetSubmit.class);
        BeanUtil.copyProperties(proSetSubmitEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSetSubmitIdParam> proSetSubmitIdParamList) {
        if (ObjectUtil.isEmpty(proSetSubmitIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSetSubmitIdParamList, ProSetSubmitIdParam::getId));
    }

    @Override
    public ProSetSubmit detail(ProSetSubmitIdParam proSetSubmitIdParam) {
        ProSetSubmit proSetSubmit = this.getById(proSetSubmitIdParam.getId());
        if (ObjectUtil.isEmpty(proSetSubmit)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSetSubmit;
    }

    @Override
    public Page<ProSetSubmit> problemUserSubmitPage(ProSetUserSubmitPageParam proSetUserSubmitPageParam) {
        QueryWrapper<ProSetSubmit> queryWrapper = new QueryWrapper<ProSetSubmit>().checkSqlInjection();
        try {
            String loginIdAsString = StpUtil.getLoginIdAsString();
            queryWrapper.lambda().eq(ProSetSubmit::getUserId, loginIdAsString);
        } catch (Exception e) {
            queryWrapper.lambda().eq(ProSetSubmit::getUserId, "-1");
        }
        if (GaStringUtil.isNotEmpty(proSetUserSubmitPageParam.getProblemSetId())) {
            queryWrapper.lambda().eq(ProSetSubmit::getSetId, proSetUserSubmitPageParam.getProblemSetId());
        }
        if (ObjectUtil.isNotEmpty(proSetUserSubmitPageParam.getProblem())) {
            queryWrapper.lambda().eq(ProSetSubmit::getProblemId, proSetUserSubmitPageParam.getProblem());
        } else {
            queryWrapper.lambda().eq(ProSetSubmit::getProblemId, "-1");
        }
        if (GaStringUtil.isNotEmpty(proSetUserSubmitPageParam.getLanguage())) {
            queryWrapper.lambda().eq(ProSetSubmit::getLanguage, proSetUserSubmitPageParam.getLanguage());
        }
        if (GaStringUtil.isNotEmpty(proSetUserSubmitPageParam.getStatus())) {
            queryWrapper.lambda().eq(ProSetSubmit::getStatus, proSetUserSubmitPageParam.getStatus());
        }
        if (ObjectUtil.isNotEmpty(proSetUserSubmitPageParam.getSubmitType())) {
            queryWrapper.lambda().eq(ProSetSubmit::getSubmitType, proSetUserSubmitPageParam.getSubmitType());
        }

        if (ObjectUtil.isAllNotEmpty(proSetUserSubmitPageParam.getSortField(), proSetUserSubmitPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSetUserSubmitPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSetUserSubmitPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSetUserSubmitPageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByDesc(ProSetSubmit::getCreateTime);
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSetUserSubmitPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSetUserSubmitPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Override
    public String execute(ProSetSubmitExecuteParam proSetSubmitExecuteParam) {
        // 参数校验
        ProProblem proProblem = validateSubmission(proSetSubmitExecuteParam);

        // 构建并保存提交记录
        ProSetSubmit proSubmit = buildSubmitRecord(proSetSubmitExecuteParam, proProblem);
        this.save(proSubmit);

        ProSetSolved proSolved = buildSubmitRecord(proSetSubmitExecuteParam, proProblem, proSubmit);

        // 判断是否存在
        if (proSetSolvedMapper.exists(new LambdaQueryWrapper<ProSetSolved>()
                .eq(ProSetSolved::getUserId, proSubmit.getUserId())
                .eq(ProSetSolved::getProblemId, proSubmit.getProblemId())
        )) {
            // 存在则更新提交ID
            proSetSolvedMapper.update(new LambdaUpdateWrapper<ProSetSolved>()
                    .eq(ProSetSolved::getUserId, proSubmit.getUserId())
                    .eq(ProSetSolved::getProblemId, proSubmit.getProblemId())
                    .set(ProSetSolved::getSubmitId, proSubmit.getId())
                    .set(ProSetSolved::getUpdateTime, proSubmit.getUpdateTime())
            );
        } else {
            // 不存在则插入
            proSetSolvedMapper.insert(proSolved);
        }

        // 发送判题消息
        sendJudgeMessage(proSubmit, proSetSubmitExecuteParam, proProblem);

        return proSubmit.getId();
    }

    private ProProblem validateSubmission(ProSetSubmitExecuteParam param) {
        ProProblem problem = proProblemMapper.selectById(param.getProblemId());
        if (ObjectUtil.isEmpty(problem)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        // 校验语言
        if (ObjectUtil.isEmpty(problem.getAllowedLanguages())
                || !problem.getAllowedLanguages().contains(param.getLanguage().toLowerCase())) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        // 校验测试用例
        if (ObjectUtil.isEmpty(problem.getTestCase())) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        return problem;
    }

    private ProSetSubmit buildSubmitRecord(ProSetSubmitExecuteParam param, ProProblem problem) {
        ProSetSubmit submit = BeanUtil.toBean(param, ProSetSubmit.class);
        submit.setUserId(StpUtil.getLoginIdAsString());
        submit.setStatus(JudgeStatus.PENDING.getValue());
        submit.setCodeLength(param.getCode().length());
        return submit;
    }

    private ProSetSolved buildSubmitRecord(ProSetSubmitExecuteParam param, ProProblem problem, ProSetSubmit submit) {
        ProSetSolved solved = BeanUtil.toBean(param, ProSetSolved.class);
        solved.setSolved(false);
        solved.setUserId(StpUtil.getLoginIdAsString());
        solved.setProblemId(problem.getId());
        solved.setSubmitId(submit.getId());
        return solved;
    }

    private void sendJudgeMessage(ProSetSubmit submit, ProSetSubmitExecuteParam param, ProProblem problem) {
        try {
            JudgeSubmitDto message = new JudgeSubmitDto();
            // ======================= 任务参数 =======================
            message.setUserId(submit.getUserId());
            message.setId(submit.getId());
            message.setIsSet(false);
            // ======================= 题目参数 =======================
            message.setMaxTime(problem.getMaxTime());
            message.setMaxMemory(problem.getMaxMemory());
            message.setTestCase(problem.getTestCase());
            // ======================= 用户提交参数 =======================
            message.setProblemId(param.getProblemId());
            message.setLanguage(param.getLanguage());
            message.setSubmitType(param.getSubmitType());
            // 处理代码模板
            if (problem.getUseTemplate()) {
                problem.getCodeTemplate().stream()
                        .filter(t -> t.getLanguage().equals(param.getLanguage().toLowerCase()))
                        .findFirst()
                        .ifPresent(template -> {
                            if (ObjectUtil.isNotEmpty(template.getPrefix()) && ObjectUtil.isNotEmpty(template.getSuffix())) {
                                message.setCode(template.getPrefix() + param.getCode() + template.getSuffix());
                            }
                        });
            } else {
                message.setCode(param.getCode());
            }

            setJudgeMessageService.sendJudgeRequest(message);

            log.info("题目提交消息已发送到队列，提交ID: {}", submit.getId());
        } catch (Exception e) {
            log.error("发送题目提交消息到队列失败，提交ID: {}", submit.getId(), e);
            throw new BusinessException("提交失败，请稍后重试");
        }
    }
}