package io.charlie.web.oj.modular.llm.param;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.charlie.galaxy.config.timestamp.DateToTimestampSerializer;
import io.charlie.galaxy.config.timestamp.TimestampToDateDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    private String conversantId;
    private String content;
    private String messageType; // "text", "thinking", "complete"

    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date timestamp;
    
    public ChatResponse(String conversantId, String content, String messageType) {
        this.conversantId = conversantId;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = new Date();
    }
}