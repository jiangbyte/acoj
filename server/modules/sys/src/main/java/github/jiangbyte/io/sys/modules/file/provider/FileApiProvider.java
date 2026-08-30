package github.jiangbyte.io.sys.modules.file.provider;

import github.jiangbyte.io.sys.file.FileApi;
import github.jiangbyte.io.sys.file.FileInfo;
import github.jiangbyte.io.sys.modules.file.convert.SysFileConvert;
import github.jiangbyte.io.sys.modules.file.entity.SysFile;
import github.jiangbyte.io.sys.modules.file.service.FileService;
import github.jiangbyte.io.sys.modules.file.support.FileAccessUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 跨模块 FileApi 适配器：对外暴露上传与 URL 能力。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class FileApiProvider implements FileApi {

    private final FileService fileService;
    private final FileAccessUrls fileAccessUrls;
    private final SysFileConvert fileConvert;

    @Override
    public FileInfo upload(MultipartFile file, String storageProvider) {
        return fileConvert.toInfo(fileService.upload(file, storageProvider));
    }

    @Override
    public void deleteByObjectName(String objectName) {
        fileService.deleteByObjectName(objectName);
    }

    @Override
    public String normalizeObjectName(String value) {
        return fileAccessUrls.normalizeObjectName(value);
    }

    @Override
    public String resolveUrl(String objectNameOrUrl) {
        return fileService.resolveAccessUrl(objectNameOrUrl);
    }

    @Override
    public Map<String, String> resolveUrls(Collection<String> objectNameOrUrls) {
        Map<String, String> map = new LinkedHashMap<>();
        if (objectNameOrUrls == null || objectNameOrUrls.isEmpty()) {
            return map;
        }
        for (String raw : objectNameOrUrls) {
            if (!StringUtils.hasText(raw) || map.containsKey(raw)) {
                continue;
            }
            String resolved = resolveUrl(raw);
            if (StringUtils.hasText(resolved)) {
                map.put(raw, resolved);
            }
        }
        return map;
    }

    @Override
    public List<FileInfo> listByObjectNames(Collection<String> objectNames) {
        List<FileInfo> list = new ArrayList<>();
        for (SysFile file : fileService.listByObjectNames(objectNames)) {
            list.add(fileConvert.toInfo(file));
        }
        return list;
    }
}
