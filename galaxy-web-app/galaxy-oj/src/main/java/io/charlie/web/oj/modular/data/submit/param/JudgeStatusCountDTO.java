package io.charlie.web.oj.modular.data.submit.param;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class JudgeStatusCountDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String status;
    private Long count;
    
    public JudgeStatusCountDTO() {}
    
    public JudgeStatusCountDTO(String status, Long count) {
        this.status = status;
        this.count = count;
    }
}