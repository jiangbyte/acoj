package github.jiangbyte.io.sys.modules.file.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.sys.modules.file.entity.SysFile;
import github.jiangbyte.io.sys.modules.file.param.SysFileEditParam;
import github.jiangbyte.io.sys.modules.file.param.SysFilePageParam;
import github.jiangbyte.io.sys.modules.file.result.SysFileUrlResult;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

/**
 * 文件服务接口：上传、元数据维护与访问 URL。
 *
 * Author: Charlie
 */
public interface FileService extends IService<SysFile> {

    /** 上传文件。 */
    SysFile upload(MultipartFile file, String storageProvider);

    /** 批量删除。 */
    void delete(IdsParam param);

    /** 更新。 */
    void update(SysFileEditParam param);

    /** 查询详情。 */
    SysFile detail(String id);

    /** 断言当前登录用户为文件上传者（门户防 IDOR）。 */
    void assertOwnedByCurrent(SysFile file);

    /** 按 ID 列表查询。 */
    List<SysFile> listByIds(List<String> ids);

    /** 下载文件资源。 */
    Resource download(String id);

    /** 获取文件访问 URL。 */
    SysFileUrlResult url(String objectName);

    /** 获取预签名访问 URL。 */
    SysFileUrlResult presignedUrl(String objectName);

    /** 分页查询。 */
    Page<SysFile> page(SysFilePageParam param);

    /** 按对象名删除文件。 */
    void deleteByObjectName(String objectName);

    /** 解析可访问 URL。 */
    String resolveAccessUrl(String objectNameOrUrl);

    /** 按对象名列表查询。 */
    List<SysFile> listByObjectNames(Collection<String> objectNames);
}
