package io.charlie.web.oj.modular.data.library.entity;

import lombok.Data;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 02/11/2025
 * @description TODO
 */
@Data
public class LibraryBatchCount {
    private String totalCount; // 有效记录代码数量
    // 实际检测数量
    private String checkCount;

    // 预计处理时间
    private String expectTime;

    // 对比组合数
    private String compareCount;
}
