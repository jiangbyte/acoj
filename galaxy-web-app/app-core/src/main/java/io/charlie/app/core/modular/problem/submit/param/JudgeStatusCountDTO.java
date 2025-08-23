package io.charlie.app.core.modular.problem.submit.param;

import lombok.Data;

@Data
public class JudgeStatusCountDTO {
    private String status;
    private Long count;
    
    public JudgeStatusCountDTO() {}
    
    public JudgeStatusCountDTO(String status, Long count) {
        this.status = status;
        this.count = count;
    }
}