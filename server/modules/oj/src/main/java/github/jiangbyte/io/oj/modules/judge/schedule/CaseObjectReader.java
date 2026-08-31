package github.jiangbyte.io.oj.modules.judge.schedule;

import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.oss.StorageService;
import github.jiangbyte.io.oj.config.OjProperties;
import github.jiangbyte.io.sys.modules.storage.StorageEngineFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 从默认对象存储读取测例 OBJECT 内容（不查 sys_file）。
 *
 * Author: Charlie
 */
@Component
@RequiredArgsConstructor
public class CaseObjectReader {

    private final StorageEngineFactory storageEngineFactory;
    private final OjProperties ojProperties;

    /** 按 object key 读取 UTF-8 文本，超出限额则拒绝。 */
    public String readText(String objectKey, String caseKey, String side) {
        if (!StringUtils.hasText(objectKey)) {
            throw new BizException("测例 " + caseKey + " 缺少 " + side + " object key");
        }
        int maxBytes = Math.max(ojProperties.getJudge().getInlineCaseMaxBytes(), 4 * 1024 * 1024);
        StorageService storage = storageEngineFactory.getDefault();
        try {
            Resource resource = storage.load(objectKey.trim());
            try (InputStream input = resource.getInputStream()) {
                byte[] data = input.readNBytes(maxBytes + 1);
                if (data.length > maxBytes) {
                    throw new BizException("测例 " + caseKey + " " + side + " 超过最大字节数: " + maxBytes);
                }
                return new String(data, StandardCharsets.UTF_8);
            }
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException("读取测例 " + caseKey + " " + side + " 失败: " + ex.getMessage());
        }
    }
}
