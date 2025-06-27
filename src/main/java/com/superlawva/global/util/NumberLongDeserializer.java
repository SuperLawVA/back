package com.superlawva.global.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

public class NumberLongDeserializer extends JsonDeserializer<Long> {
    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.START_OBJECT) {
            JsonNode node = p.getCodec().readTree(p);
            if (node.has("$numberLong")) {
                return Long.parseLong(node.get("$numberLong").asText());
            }
            // fallback: try first field text
            return node.fields().hasNext() ? Long.parseLong(node.fields().next().getValue().asText()) : null;
        } else if (token.isNumeric()) {
            return p.getLongValue();
        } else if (token == JsonToken.VALUE_STRING) {
            String text = p.getValueAsString();
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
} 