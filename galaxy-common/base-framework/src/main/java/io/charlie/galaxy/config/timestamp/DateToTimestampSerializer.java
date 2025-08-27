package io.charlie.galaxy.config.timestamp;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;
import java.util.Date;

//public class DateToTimestampSerializer extends JsonSerializer<Date> {
//    @Override
//    public void serialize(Date date, JsonGenerator gen, SerializerProvider serializers) throws IOException {
//        if (date == null) {
//            gen.writeNull();
//        } else {
//            gen.writeNumber(date.getTime());
//        }
//    }
//}

public class DateToTimestampSerializer extends JsonSerializer<Date> {
    @Override
    public void serialize(Date date, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (date == null) {
            gen.writeNull();
        } else {
            gen.writeNumber(date.getTime());
        }
    }

    @Override
    public void serializeWithType(Date value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        typeSer.writeTypePrefix(gen, typeSer.typeId(value, JsonToken.VALUE_NUMBER_INT));
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffix(gen, typeSer.typeId(value, JsonToken.VALUE_NUMBER_INT));
    }
}