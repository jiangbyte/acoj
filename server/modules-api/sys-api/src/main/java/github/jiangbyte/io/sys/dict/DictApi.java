package github.jiangbyte.io.sys.dict;

import java.util.List;

/**
 * 跨模块字典门面：按字典类型列出启用项，供其他模块做下拉/翻译等只读消费。
 * HTTP 类型留在 {@code module/sys}；实现为 {@code DictApiProvider}。
 *
 * Author: Charlie
 */
public interface DictApi {

    /** 按字典类型列出字典项。 */
    List<DictItem> listByType(String dictType);
}
