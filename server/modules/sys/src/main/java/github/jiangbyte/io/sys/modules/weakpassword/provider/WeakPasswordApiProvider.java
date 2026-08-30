package github.jiangbyte.io.sys.modules.weakpassword.provider;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.sys.modules.weakpassword.entity.SysWeakPassword;
import github.jiangbyte.io.sys.modules.weakpassword.mapper.SysWeakPasswordMapper;
import github.jiangbyte.io.sys.weakpassword.WeakPasswordApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 跨模块 WeakPasswordApi 适配器：对外暴露弱密码校验。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class WeakPasswordApiProvider implements WeakPasswordApi {

    private final SysWeakPasswordMapper weakPasswordMapper;

    @Override
    public void assertNotWeak(String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            return;
        }
        Long count = weakPasswordMapper.selectCount(Wrappers.<SysWeakPassword>lambdaQuery()
                .eq(SysWeakPassword::getPassword, rawPassword.trim()));
        if (count != null && count > 0) {
            throw new BizException("Password is too weak");
        }
    }
}
