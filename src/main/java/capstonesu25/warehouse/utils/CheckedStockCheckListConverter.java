package capstonesu25.warehouse.utils;

import capstonesu25.warehouse.model.stockcheck.detail.CheckedStockCheck;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;

@Converter
public class CheckedStockCheckListConverter implements AttributeConverter<List<CheckedStockCheck>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(List<CheckedStockCheck> attribute) {
        try {
            return attribute == null ? "[]" : objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Error converting list of CheckedStockCheck to JSON", e);
        }
    }

    @Override
    public List<CheckedStockCheck> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null || dbData.isEmpty()
                    ? List.of()
                    : objectMapper.readValue(dbData, new TypeReference<List<CheckedStockCheck>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to list of CheckedStockCheck", e);
        }
    }
}
