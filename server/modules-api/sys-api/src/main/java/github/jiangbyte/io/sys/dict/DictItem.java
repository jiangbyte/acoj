package github.jiangbyte.io.sys.dict;

import lombok.Data;

/**
 * 跨模块字典项快照：编码、展示文案、取值与排序等只读字段。
 * 非 HTTP 结果，亦非持久化实体。
 *
 * Author: Charlie
 */
@Data
public class DictItem {
    private String id;
    private String code;
    private String label;
    private String value;
    private String color;
    private String parentId;
    private Integer sort;
}
