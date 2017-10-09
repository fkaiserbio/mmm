package de.bioforscher.mmm.model.configurations;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;

/**
 * An interface providing the default methods for serialization and deserialization from Json.
 *
 * @author fk
 */
public interface Jsonizable<ConfigurationType extends JsonConfiguration> extends JsonConfiguration {

    default String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
        return mapper.writeValueAsString(this);
    }

    default ConfigurationType fromJson(InputStream inputStream) throws IOException {
        TypeReference<ConfigurationType> typeReference = new TypeReference<ConfigurationType>() {
        };
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        return mapper.readValue(inputStream, typeReference);
    }

    default ConfigurationType fromJson(String json) throws IOException {
        TypeReference<ConfigurationType> typeReference = new TypeReference<ConfigurationType>() {
        };
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        return mapper.readValue(json, typeReference);
    }
}
