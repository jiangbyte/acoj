package io.charlie.web.oj.modular.sys.auth.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.web.oj.modular.sys.auth.enums.PlatformEnum;
import io.charlie.web.oj.modular.sys.auth.param.PasswordChangeParam;
import io.charlie.web.oj.modular.sys.auth.param.UsernamePasswordLoginParam;
import io.charlie.web.oj.modular.sys.auth.service.AuthService;
import io.charlie.web.oj.modular.sys.auth.param.UsernamePasswordEmailRegisterParam;
import io.charlie.web.oj.modular.sys.auth.result.CaptchaResult;
import io.charlie.web.oj.modular.sys.auth.result.LoginUser;
import io.charlie.web.oj.modular.sys.config.service.SysConfigService;
import io.charlie.web.oj.modular.sys.dict.service.SysDictService;
import io.charlie.web.oj.modular.sys.group.entity.SysGroup;
import io.charlie.web.oj.modular.sys.group.enums.SysGroupEnums;
import io.charlie.web.oj.modular.sys.group.mapper.SysGroupMapper;
import io.charlie.web.oj.modular.sys.relation.service.SysUserRoleService;
import io.charlie.web.oj.modular.sys.role.constant.DefaultRoleData;
import io.charlie.web.oj.modular.sys.role.service.SysRoleService;
import io.charlie.web.oj.modular.sys.user.constant.DefaultUserData;
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

    private final SysRoleService sysRoleService;
    private final SysGroupMapper sysGroupMapper;

    private final SysConfigService sysConfigService;

    private final TransService transService;

    @Override
    public CaptchaResult captcha() {
        CircleCaptcha circleCaptcha = CaptchaUtil.createCircleCaptcha(100, 38, 4, 10);
        CaptchaResult captchaResult = new CaptchaResult();
        String imageBase64Data = circleCaptcha.getImageBase64Data();
        captchaResult.setCaptcha(imageBase64Data);
        String uuid = IdUtil.fastSimpleUUID();
        captchaResult.setUuid(uuid);
        redisTemplate.opsForValue().set("captcha:" + uuid, circleCaptcha.getCode(), Duration.ofSeconds(5 * 60L));
        return captchaResult;
    }

    @Override
    public String doLogin(UsernamePasswordLoginParam usernamePasswordLoginParam) {
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

        // 平台校验
        if (!PlatformEnum.isValid(usernamePasswordLoginParam.getPlatform().toUpperCase())) {
            throw new BusinessException("平台参数错误");
        }

        // 登录逻辑
        String username = usernamePasswordLoginParam.getUsername();
        if (ObjectUtil.isEmpty(username)) {
            throw new BusinessException("用户名不能为空");
        }

        // 校验长度
        if (username.length() < 6 || username.length() > 20) {
            throw new BusinessException("用户名长度必须在6-20位之间");
        }

        // 校验必须以字母开头
        if (!Character.isLetter(username.charAt(0))) {
            throw new BusinessException("用户名必须以字母开头");
        }

        // 校验只能包含字母、数字、下划线
        String usernamePattern = "^[a-zA-Z0-9_]+$";
        if (!username.matches(usernamePattern)) {
            throw new BusinessException("用户名只能包含字母、数字和下划线");
        }

        String password = usernamePasswordLoginParam.getPassword();

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
            if (!sysUserRoleService.canAdmin(sysUser.getId())) {
                throw new BusinessException("无权限");
            }
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

        // 用户名校验，必须是字母开头，长度6-20位，只能包含字母、数字、下划线
        String username = usernamePasswordEmailRegisterParam.getUsername();
        if (ObjectUtil.isEmpty(username)) {
            throw new BusinessException("用户名不能为空");
        }

        // 校验长度
        if (username.length() < 6 || username.length() > 20) {
            throw new BusinessException("用户名长度必须在6-20位之间");
        }

        // 校验必须以字母开头
        if (!Character.isLetter(username.charAt(0))) {
            throw new BusinessException("用户名必须以字母开头");
        }

        // 校验只能包含字母、数字、下划线
        String usernamePattern = "^[a-zA-Z0-9_]+$";
        if (!username.matches(usernamePattern)) {
            throw new BusinessException("用户名只能包含字母、数字和下划线");
        }

        // 到这里验证码校验通过，删除验证码
        redisTemplate.delete("captcha:" + usernamePasswordEmailRegisterParam.getUuid());

        // 注册逻辑
//        String username = usernamePasswordEmailRegisterParam.getUsername();
        String password = usernamePasswordEmailRegisterParam.getPassword();
        String email = usernamePasswordEmailRegisterParam.getEmail();

        // 用户名 邮箱 存在校验
        if (sysUserMapper.exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username))) {
            throw new BusinessException("用户名已存在");
        }
        if (sysUserMapper.exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email))) {
            throw new BusinessException("邮箱已存在");
        }

        // 邮箱格式校验
        if (!Validator.isEmail(email)) {
            throw new BusinessException("邮箱格式错误");
        }

        // 加密密码
        String encodePassword = BCrypt.hashpw(password);

        SysUser sysUser = new SysUser();
        sysUser.setUsername(username);
        sysUser.setPassword(encodePassword);
        sysUser.setEmail(email);
        // 默认用户组
        sysUser.setGroupId(SysGroupEnums.DEFAULT_GROUP.getValue());
        // 默认昵称
        sysUser.setNickname(DefaultUserData.USER_DEFAULT_NICKNAME);
        // 默认头像
        sysUser.setAvatar(sysConfigService.getValueByCode("APP_DEFAULT_AVATAR"));
        // 默认背景图片
        sysUser.setBackground(sysConfigService.getValueByCode("APP_DEFAULT_USER_BACKGROUND"));
        // 默认 性别
        sysUser.setGender(DefaultUserData.USER_DEFAULT_GENDER);
        // 默认 签名
        sysUser.setQuote(DefaultUserData.USER_DEFAULT_QUOTE);
        // 默认数据
        sysUser.setDeleted(false);
        sysUser.setLoginTime(new Date());
        sysUserMapper.insert(sysUser);

        // 分配角色
        sysUserRoleService.assignRoles(sysUser.getId(), List.of(DefaultRoleData.DEFAULT_USER_ROLE_ID));

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
        String loginIdAsString = StpUtil.getLoginIdAsString();
        // 获取用户信息
        SysUser sysUser = sysUserMapper.selectById(loginIdAsString);
        transService.transOne(sysUser);
        LoginUser loginUser = new LoginUser();
        BeanUtils.copyProperties(sysUser, loginUser);
        // 手机号，邮箱脱敏
//        loginUser.setTelephone(DesensitizedUtil.mobilePhone(sysUser.getTelephone()));
//        loginUser.setEmail(DesensitizedUtil.email(sysUser.getEmail()));
        // 查询这个用户的角色名
        loginUser.setRoleNames(sysRoleService.getRoleNamesByUserId(sysUser.getId()));
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
