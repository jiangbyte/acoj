package io.charlie.web.oj.modular.sys.user.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 07/10/2025
 */
@Data
@AllArgsConstructor
public class ACRecord {
    private String date;
    private Long count;
}
