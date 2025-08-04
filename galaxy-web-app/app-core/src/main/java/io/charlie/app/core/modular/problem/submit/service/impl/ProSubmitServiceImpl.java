package io.charlie.app.core.modular.problem.submit.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.problem.problem.mapper.ProProblemMapper;
import io.charlie.app.core.modular.problem.problem.service.ProProblemService;
import io.charlie.app.core.modular.problem.submit.entity.ProSubmit;
import io.charlie.app.core.modular.problem.submit.enums.JudgeStatus;
import io.charlie.app.core.modular.problem.submit.param.*;
import io.charlie.app.core.modular.problem.submit.mapper.ProSubmitMapper;
import io.charlie.app.core.modular.problem.submit.service.ProSubmitService;
import io.charlie.app.core.modular.set.set.entity.ProSet;
import io.charlie.app.core.modular.sys.user.entity.SysUser;
import io.charlie.app.core.modular.sys.user.mapper.SysUserMapper;
import io.charlie.app.core.modular.sys.user.service.SysUserService;
import io.charlie.app.core.mq.problem.PReqMQConfig;
import io.charlie.app.core.mq.problem.dto.ProJudgeSubmitDto;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.galaxy.utils.GaStringUtil;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 提交表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSubmitServiceImpl extends ServiceImpl<ProSubmitMapper, ProSubmit> implements ProSubmitService {
    private final ProProblemMapper proProblemMapper;
    private final SysUserMapper sysUserMapper;

    // 在类中添加以下依赖
    private final AmqpTemplate amqpTemplate;

    @Override
    public Page<ProSubmit> page(ProSubmitPageParam proSubmitPageParam) {
        QueryWrapper<ProSubmit> queryWrapper = new QueryWrapper<ProSubmit>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(proSubmitPageParam.getKeyword())) {
            List<SysUser> list = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>().like(SysUser::getNickname, proSubmitPageParam.getKeyword()));
            if (ObjectUtil.isNotEmpty(list)) {
                // 提取出用户ID(去重)
                List<String> userIds = CollStreamUtil.toList(list, SysUser::getId).stream().distinct().toList();
                queryWrapper.lambda().in(ProSubmit::getUserId, userIds);
            } else {
                queryWrapper.lambda().eq(ProSubmit::getUserId, "-1");
            }
        }
        if (ObjectUtil.isNotEmpty(proSubmitPageParam.getProblem())) {
            List<ProProblem> list = proProblemMapper.selectList(new LambdaQueryWrapper<ProProblem>().like(ProProblem::getTitle, proSubmitPageParam.getProblem()));
            if (ObjectUtil.isNotEmpty(list)) {
                List<String> problemList = CollStreamUtil.toList(list, ProProblem::getId).stream().distinct().toList();
                queryWrapper.lambda().in(ProSubmit::getProblemId, problemList);
            } else {
                queryWrapper.lambda().eq(ProSubmit::getProblemId, "-1");
            }
        }
        if (GaStringUtil.isNotEmpty(proSubmitPageParam.getLanguage())) {
            queryWrapper.lambda().eq(ProSubmit::getLanguage, proSubmitPageParam.getLanguage());
        }
        if (GaStringUtil.isNotEmpty(proSubmitPageParam.getStatus())) {
            queryWrapper.lambda().eq(ProSubmit::getStatus, proSubmitPageParam.getStatus());
        }
        if (ObjectUtil.isNotEmpty(proSubmitPageParam.getSubmitType())) {
            queryWrapper.lambda().eq(ProSubmit::getSubmitType, proSubmitPageParam.getSubmitType());
        }

        if (ObjectUtil.isAllNotEmpty(proSubmitPageParam.getSortField(), proSubmitPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSubmitPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSubmitPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSubmitPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSubmitPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSubmitPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSubmitAddParam proSubmitAddParam) {
        ProSubmit bean = BeanUtil.toBean(proSubmitAddParam, ProSubmit.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSubmitEditParam proSubmitEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSubmit>().eq(ProSubmit::getId, proSubmitEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSubmit bean = BeanUtil.toBean(proSubmitEditParam, ProSubmit.class);
        BeanUtil.copyProperties(proSubmitEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSubmitIdParam> proSubmitIdParamList) {
        if (ObjectUtil.isEmpty(proSubmitIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSubmitIdParamList, ProSubmitIdParam::getId));
    }

    @Override
    public ProSubmit detail(ProSubmitIdParam proSubmitIdParam) {
        ProSubmit proSubmit = this.getById(proSubmitIdParam.getId());
        if (ObjectUtil.isEmpty(proSubmit)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSubmit;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String execute(ProSubmitExecuteParam proSubmitExecuteParam) {
        // 参数校验
        ProProblem proProblem = validateSubmission(proSubmitExecuteParam);

        // 构建并保存提交记录
        ProSubmit proSubmit = buildSubmitRecord(proSubmitExecuteParam, proProblem);
        this.save(proSubmit);

        // 发送判题消息
//        sendJudgeMessage(proSubmit, proSubmitExecuteParam, proProblem);

        return proSubmit.getId();
    }

    @Override
    public Page<ProSubmit> problemUserSubmitPage(ProUserSubmitPageParam proUserSubmitPageParam) {
        QueryWrapper<ProSubmit> queryWrapper = new QueryWrapper<ProSubmit>().checkSqlInjection();
        try {
            String loginIdAsString = StpUtil.getLoginIdAsString();
            queryWrapper.lambda().eq(ProSubmit::getUserId, loginIdAsString);
        } catch (Exception e) {
            queryWrapper.lambda().eq(ProSubmit::getUserId, "-1");
        }

        if (ObjectUtil.isNotEmpty(proUserSubmitPageParam.getProblem())) {
            queryWrapper.lambda().eq(ProSubmit::getProblemId, proUserSubmitPageParam.getProblem());
        } else {
            queryWrapper.lambda().eq(ProSubmit::getProblemId, "-1");
        }
        if (GaStringUtil.isNotEmpty(proUserSubmitPageParam.getLanguage())) {
            queryWrapper.lambda().eq(ProSubmit::getLanguage, proUserSubmitPageParam.getLanguage());
        }
        if (GaStringUtil.isNotEmpty(proUserSubmitPageParam.getStatus())) {
            queryWrapper.lambda().eq(ProSubmit::getStatus, proUserSubmitPageParam.getStatus());
        }
        if (ObjectUtil.isNotEmpty(proUserSubmitPageParam.getSubmitType())) {
            queryWrapper.lambda().eq(ProSubmit::getSubmitType, proUserSubmitPageParam.getSubmitType());
        }

        if (ObjectUtil.isAllNotEmpty(proUserSubmitPageParam.getSortField(), proUserSubmitPageParam.getSortOrder()) && ISortOrderEnum.isValid(proUserSubmitPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proUserSubmitPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proUserSubmitPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proUserSubmitPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proUserSubmitPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    private ProProblem validateSubmission(ProSubmitExecuteParam param) {
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

    private ProSubmit buildSubmitRecord(ProSubmitExecuteParam param, ProProblem problem) {
        ProSubmit submit = BeanUtil.toBean(param, ProSubmit.class);
        submit.setUserId(StpUtil.getLoginIdAsString());
        submit.setStatus(JudgeStatus.PENDING.getValue());
        return submit;
    }

    private void sendJudgeMessage(ProSubmit submit, ProSubmitExecuteParam param, ProProblem problem) {
        try {
            ProJudgeSubmitDto message = new ProJudgeSubmitDto();
            message.setId(submit.getId());
            message.setProblemId(param.getProblemId());
            message.setLanguage(param.getLanguage());
            message.setSubmitType(param.getSubmitType());
            message.setMaxTime(problem.getMaxTime());
            message.setMaxMemory(problem.getMaxMemory());
            message.setTestCase(problem.getTestCase());

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
            }

            // 发送消息
            amqpTemplate.convertAndSend(
                    PReqMQConfig.EXCHANGE,
                    PReqMQConfig.ROUTING_KEY,
                    message,
                    msg -> {
                        msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return msg;
                    }
            );

            log.info("题目提交消息已发送到队列，提交ID: {}", submit.getId());
        } catch (Exception e) {
            log.error("发送题目提交消息到队列失败，提交ID: {}", submit.getId(), e);
            throw new BusinessException("提交失败，请稍后重试");
        }
    }

}