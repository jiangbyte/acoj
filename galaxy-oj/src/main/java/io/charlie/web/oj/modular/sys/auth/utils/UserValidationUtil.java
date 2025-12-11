package io.charlie.web.oj.modular.sys.auth.utils;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.charlie.galaxy.exception.BusinessException;
import lombok.Getter;

import java.util.regex.Pattern;

/**
 * 用户名校验工具类
 */
public class UserValidationUtil {

    // 用户名规则：2-15位，字母开头，只能包含字母、数字、下划线
    private static final String USERNAME_PATTERN = "^[a-zA-Z][a-zA-Z0-9_]*$";
    private static final Pattern USERNAME_REGEX = Pattern.compile(USERNAME_PATTERN);

    // 用户名最小长度
    private static final int USERNAME_MIN_LENGTH = 2;
    // 用户名最大长度
    private static final int USERNAME_MAX_LENGTH = 15;

    /**
     * 校验用户名格式
     *
     * @param username 用户名
     * @return 校验结果
     */
    public static ValidationResult validateUsername(String username) {
        if (ObjectUtil.isEmpty(username)) {
            return ValidationResult.failure("用户名不能为空");
        }

        // 校验长度
        if (username.length() < USERNAME_MIN_LENGTH || username.length() > USERNAME_MAX_LENGTH) {
            return ValidationResult.failure("用户名长度必须在2-15位之间");
        }

        // 校验必须以字母开头
        if (!Character.isLetter(username.charAt(0))) {
            return ValidationResult.failure("用户名必须以字母开头");
        }

        // 校验只能包含字母、数字、下划线
        if (!USERNAME_REGEX.matcher(username).matches()) {
            return ValidationResult.failure("用户名只能包含字母、数字和下划线");
        }

        // 不能包含中文
        if (Validator.hasChinese(username)) {
            return ValidationResult.failure("用户名不能包含中文");
        }

        return ValidationResult.success();
    }

    /**
     * 校验邮箱格式
     *
     * @param email 邮箱地址
     * @return 校验结果
     */
    public static ValidationResult validateEmail(String email) {
        if (ObjectUtil.isEmpty(email)) {
            return ValidationResult.failure("邮箱不能为空");
        }

        if (!Validator.isEmail(email)) {
            return ValidationResult.failure("邮箱格式错误");
        }

        return ValidationResult.success();
    }

    public static ValidationResult validatePhone(String phone) {
        if (!Validator.isMobile(phone)) {
            return ValidationResult.failure("手机号格式错误");
        }

        return ValidationResult.success();
    }

    /**
     * 校验密码强度（可根据需求扩展）
     *
     * @param password 密码
     * @return 校验结果
     */
    public static ValidationResult validatePassword(String password) {
        if (ObjectUtil.isEmpty(password)) {
            return ValidationResult.failure("密码不能为空");
        }

        // 这里可以添加密码强度校验规则
        // 例如：最小长度、必须包含数字和字母等
        if (password.length() < 6) {
            return ValidationResult.failure("密码长度不能少于6位");
        }

        if (password.length() > 20) {
            return ValidationResult.failure("密码长度不能大于20位");
        }

        // 不能含有中文
        if (Validator.hasChinese(password)) {
            return ValidationResult.failure("密码不能包含中文");
        }

        return ValidationResult.success();
    }

    /**
     * 综合校验注册信息
     *
     * @param username 用户名
     * @param email    邮箱
     * @param password 密码
     * @return 校验结果
     */
    public static ValidationResult validateRegisterInfo(String username, String email, String password) {
        // 校验用户名
        ValidationResult usernameResult = validateUsername(username);
        if (!usernameResult.isSuccess()) {
            return usernameResult;
        }

        // 校验邮箱
        ValidationResult emailResult = validateEmail(email);
        if (!emailResult.isSuccess()) {
            return emailResult;
        }

        // 校验密码
        ValidationResult passwordResult = validatePassword(password);
        if (!passwordResult.isSuccess()) {
            return passwordResult;
        }

        return ValidationResult.success();
    }

    /**
     * 校验结果封装类
     */
    @Getter
    public static class ValidationResult {
        private final boolean success;
        private final String message;

        private ValidationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, "校验成功");
        }

        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message);
        }

        /**
         * 如果校验失败，抛出业务异常
         */
        public void throwIfFailed() {
            if (!success) {
                throw new BusinessException(message);
            }
        }
    }
}