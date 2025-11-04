package io.charlie.web.oj.modular.sys.auth.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.*;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.web.oj.constant.EGroupConstant;
import io.charlie.web.oj.context.DataScopeUtil;
import io.charlie.web.oj.modular.data.ranking.utils.ActivityScoreCalculator;
import io.charlie.web.oj.modular.data.ranking.service.UserActivityService;
import io.charlie.web.oj.modular.sys.auth.enums.PlatformEnum;
import io.charlie.web.oj.modular.sys.auth.param.PasswordChangeParam;
import io.charlie.web.oj.modular.sys.auth.param.UsernamePasswordLoginParam;
import io.charlie.web.oj.modular.sys.auth.service.AuthService;
import io.charlie.web.oj.modular.sys.auth.param.UsernamePasswordEmailRegisterParam;
import io.charlie.web.oj.modular.sys.auth.result.CaptchaResult;
import io.charlie.web.oj.modular.sys.auth.result.LoginUser;
import io.charlie.web.oj.modular.sys.auth.utils.UserValidationUtil;
import io.charlie.web.oj.modular.sys.config.service.SysConfigService;
import io.charlie.web.oj.modular.sys.group.mapper.SysGroupMapper;
import io.charlie.web.oj.modular.sys.menu.mapper.SysMenuMapper;
import io.charlie.web.oj.modular.sys.relation.mapper.SysRoleMenuMapper;
import io.charlie.web.oj.modular.sys.relation.mapper.SysUserRoleMapper;
import io.charlie.web.oj.modular.sys.relation.service.SysUserRoleService;
import io.charlie.web.oj.constant.ERoleConstant;
import io.charlie.web.oj.modular.sys.role.mapper.SysRoleMapper;
import io.charlie.web.oj.modular.sys.role.service.SysRoleService;
import io.charlie.web.oj.constant.EUserConstant;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.charlie.web.oj.modular.sys.user.mapper.SysUserMapper;
import io.charlie.galaxy.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.dromara.trans.service.impl.TransService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 23/07/2025
 * @description 认证服务实现类
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final SysUserMapper sysUserMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SysUserRoleService sysUserRoleService;

    private final UserActivityService userActivityService;

    private final SysRoleService sysRoleService;
    private final SysGroupMapper sysGroupMapper;

    private final SysConfigService sysConfigService;

    private final TransService transService;

    private final DataScopeUtil dataScopeUtil;

    private final SysUserRoleMapper sysUserRoleMapper;

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    private final SysMenuMapper sysMenuMapper;

    @Override
    public CaptchaResult captcha() {
        CaptchaResult captchaResult = new CaptchaResult();

//        CircleCaptcha circleCaptcha = CaptchaUtil.createCircleCaptcha(100, 38, 4, 10);
//        String imageBase64Data = circleCaptcha.getImageBase64Data();

        RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 100, 4, 5);
        lineCaptcha.setGenerator(randomGenerator);
        String imageBase64Data = lineCaptcha.getImageBase64Data();

        captchaResult.setCaptcha(imageBase64Data);
        String uuid = IdUtil.fastSimpleUUID();
        captchaResult.setUuid(uuid);
        redisTemplate.opsForValue().set("captcha:" + uuid, lineCaptcha.getCode(), Duration.ofSeconds(5 * 60L));
        return captchaResult;
    }

    @Override
    public String doLogin(UsernamePasswordLoginParam usernamePasswordLoginParam) {
        // 测试用验证码，如果是9926，则不进行验证码校验
        if (!usernamePasswordLoginParam.getCaptchaCode().equals("9926")) {
            // 校验验证码
            if (ObjectUtil.isEmpty(usernamePasswordLoginParam.getUuid())) {
                throw new BusinessException("验证码标识不能为空");
            }
            if (ObjectUtil.isEmpty(usernamePasswordLoginParam.getCaptchaCode())) {
                throw new BusinessException("验证码不能为空");
            }
            String captchaCode = (String) redisTemplate.opsForValue().get("captcha:" + usernamePasswordLoginParam.getUuid());
            if (ObjectUtil.isEmpty(captchaCode)) {
                throw new BusinessException("验证码已过期");
            }
            if (!usernamePasswordLoginParam.getCaptchaCode().equals(captchaCode)) {
                throw new BusinessException("验证码错误");
            }
            // 到这里验证码校验通过，删除验证码
            redisTemplate.delete("captcha:" + usernamePasswordLoginParam.getUuid());
        }

        // 平台校验
        if (!PlatformEnum.isValid(usernamePasswordLoginParam.getPlatform().toUpperCase())) {
            throw new BusinessException("平台参数错误");
        }

        // 登录逻辑
        String username = usernamePasswordLoginParam.getUsername().toLowerCase();
        UserValidationUtil.validateUsername(username).throwIfFailed();

        String password = usernamePasswordLoginParam.getPassword();
        UserValidationUtil.validatePassword(password).throwIfFailed();

        // 数据库用户名是否存在
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (ObjectUtil.isEmpty(sysUser)) {
            throw new BusinessException("用户不存在");
        }
        if (!BCrypt.checkpw(password, sysUser.getPassword())) {
            throw new BusinessException("密码错误");
        }
        // 判断是否是ADMIN平台
        if ("ADMIN".equalsIgnoreCase(usernamePasswordLoginParam.getPlatform())) {
            if (!dataScopeUtil.canManageData(sysUser.getId())) {
                throw new BusinessException("无权限");
            }
        } else {
            // CLIENT 平台
            userActivityService.addActivity(sysUser.getId(), ActivityScoreCalculator.LOGIN, false);
        }

        // 更新登录时间
        sysUser.setLoginTime(new Date());
        sysUserMapper.updateById(sysUser);
        // redis 记录用户活跃情况（保留30天），进行评分自增记录

        StpUtil.login(sysUser.getId(), usernamePasswordLoginParam.getPlatform().toUpperCase());

        return StpUtil.getTokenValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String doRegister(UsernamePasswordEmailRegisterParam usernamePasswordEmailRegisterParam) {
        // 测试用验证码，如果是9927，则不进行验证码校验
        if (!usernamePasswordEmailRegisterParam.getCaptchaCode().equals("9927")) {

            // 校验验证码
            if (ObjectUtil.isEmpty(usernamePasswordEmailRegisterParam.getUuid())) {
                throw new BusinessException("验证码标识不能为空");
            }
            if (ObjectUtil.isEmpty(usernamePasswordEmailRegisterParam.getCaptchaCode())) {
                throw new BusinessException("验证码不能为空");
            }
            String captchaCode = (String) redisTemplate.opsForValue().get("captcha:" + usernamePasswordEmailRegisterParam.getUuid());
            if (ObjectUtil.isEmpty(captchaCode)) {
                throw new BusinessException("验证码已过期");
            }
            if (!captchaCode.equals(usernamePasswordEmailRegisterParam.getCaptchaCode())) {
                throw new BusinessException("验证码错误");
            }

            // 到这里验证码校验通过，删除验证码
            redisTemplate.delete("captcha:" + usernamePasswordEmailRegisterParam.getUuid());

        }

        // 用户名校验，必须是字母开头，长度6-20位，只能包含字母、数字、下划线
        String username = usernamePasswordEmailRegisterParam.getUsername().toLowerCase();
        UserValidationUtil.validateUsername(username).throwIfFailed();

        // 注册逻辑
//        String username = usernamePasswordEmailRegisterParam.getUsername();
        String password = usernamePasswordEmailRegisterParam.getPassword();
        UserValidationUtil.validatePassword(password).throwIfFailed();

        String email = usernamePasswordEmailRegisterParam.getEmail().toLowerCase();
        UserValidationUtil.validateEmail(email).throwIfFailed();

        // 用户名 邮箱 存在校验
        if (sysUserMapper.exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username))) {
            throw new BusinessException("用户名已存在");
        }
        if (sysUserMapper.exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email))) {
            throw new BusinessException("邮箱已存在");
        }

        // 加密密码
        String encodePassword = BCrypt.hashpw(password);

        SysUser sysUser = new SysUser();
        sysUser.setUsername(username);
        sysUser.setPassword(encodePassword);
        sysUser.setEmail(email);
        // 默认用户组
        sysUser.setGroupId(EGroupConstant.DEFAULT_USER_GROUP_ID);
        // 默认昵称
        sysUser.setNickname(EUserConstant.USER_DEFAULT_NICKNAME);
        // 默认头像
        sysUser.setAvatar(sysConfigService.getValueByCode("APP_DEFAULT_AVATAR"));
        // 默认背景图片
        sysUser.setBackground(sysConfigService.getValueByCode("APP_DEFAULT_USER_BACKGROUND"));
        // 默认 性别
        sysUser.setGender(EUserConstant.USER_DEFAULT_GENDER);
        // 默认 签名
        sysUser.setQuote(EUserConstant.USER_DEFAULT_QUOTE);
        // 默认数据
        sysUser.setDeleted(false);
        sysUser.setLoginTime(new Date());
        sysUserMapper.insert(sysUser);

        // 分配角色
        sysUserRoleService.assignRoles(sysUser.getId(), List.of(ERoleConstant.DEFAULT_USER_ROLE_ID));

        // 登录
        StpUtil.login(sysUser.getId(), PlatformEnum.CLIENT.getValue());
        return StpUtil.getTokenValue();
    }

    @Override
    public void doLogout() {
        StpUtil.logout();
    }

    @Override
    public LoginUser getLoginUser() {
        String userId = StpUtil.getLoginIdAsString();
        // 获取用户信息
        SysUser sysUser = sysUserMapper.selectById(userId);
        transService.transOne(sysUser);
        LoginUser loginUser = new LoginUser();
        BeanUtils.copyProperties(sysUser, loginUser);
        // 手机号，邮箱脱敏
//        loginUser.setTelephone(DesensitizedUtil.mobilePhone(sysUser.getTelephone()));
//        loginUser.setEmail(DesensitizedUtil.email(sysUser.getEmail()));
        // 查询这个用户的角色名
        loginUser.setRoleNames(sysRoleService.getRoleNamesByUserId(sysUser.getId()));

        List<String> list = sysMenuMapper.selectPermissionsByUserId(userId);
        if (CollectionUtil.isNotEmpty(list)) {
            List<String> permissions = new ArrayList<>();
            for (String s : list) {
                if (StrUtil.isBlank(s) || "null".equals(s)) {
                    continue;
                }

                // 判断是否是JSON数组格式
                if (JSONUtil.isTypeJSONArray(s)) {
                    // 解析JSON数组
                    JSONArray jsonArray = JSONUtil.parseArray(s);
                    for (Object item : jsonArray) {
                        if (item != null) {
                            permissions.add(item.toString());
                        }
                    }
                } else {
                    // 单个权限字符串
                    permissions.add(s);
                }
            }

            loginUser.setPermissions(permissions);
        }

        return loginUser;
    }

    @Override
    public void changePassword(PasswordChangeParam passwordChangeParam) {
        String loginIdAsString = StpUtil.getLoginIdAsString();
        // 获取用户信息
        SysUser sysUser = sysUserMapper.selectById(loginIdAsString);

        // 验证旧密码
        if (!BCrypt.checkpw(passwordChangeParam.getOldPassword(), sysUser.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 新密码是否两次一样
        if (!passwordChangeParam.getNewPassword().equals(passwordChangeParam.getConfirmPassword())) {
            throw new BusinessException("新密码不一样");
        }

        sysUser.setPassword(BCrypt.hashpw(passwordChangeParam.getNewPassword()));

        sysUserMapper.updateById(sysUser);
    }
}
