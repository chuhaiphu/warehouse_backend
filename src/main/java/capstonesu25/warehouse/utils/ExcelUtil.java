package capstonesu25.warehouse.utils;


import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ExcelUtil {
    private ExcelUtil() {
    }
    private static final Logger log = LoggerFactory.getLogger(ExcelUtil.class);

    public static <T> List<T> processExcelFile(MultipartFile file, Class<T> clazz) {
        List<T> dataList = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            boolean firstRow = true;
            List<String> fieldNames = new ArrayList<>();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (firstRow) {
                    // Read header row to map fields
                    for (Cell cell : row) {
                        fieldNames.add(cell.getStringCellValue());
                    }
                    firstRow = false;
                    continue;
                }

                T instance = clazz.getDeclaredConstructor().newInstance();
                for (int i = 0; i < fieldNames.size(); i++) {
                    Cell cell = row.getCell(i);
                    String fieldName = fieldNames.get(i);

                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);

                    Object cellValue = getCellValue(cell, field.getType());
                    field.set(instance, cellValue);
                }

                // Only add if at least itemId or quantity is not null
                Object itemIdValue = null;
                Object quantityValue = null;
                try {
                    Field itemIdField = clazz.getDeclaredField("itemId");
                    itemIdField.setAccessible(true);
                    itemIdValue = itemIdField.get(instance);
                    Field quantityField = clazz.getDeclaredField("quantity");
                    quantityField.setAccessible(true);
                    quantityValue = quantityField.get(instance);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    log.warn("Could not check itemId/quantity for row: {}", instance, e);
                }
                if (itemIdValue == null && quantityValue == null) {
                    log.info("Skipped empty Excel row: {}", instance);
                    continue;
                }
                log.info("Parsed Excel row: {}", instance);
                dataList.add(instance);
            }
        } catch (Exception e) {
            log.error("Error processing Excel file", e);
        }

        return dataList;
    }

    private static Object getCellValue(Cell cell, Class<?> fieldType) {
        if (cell == null) {
            return null;
        }

        if (fieldType == LocalDate.class && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            if (date == null) return null;
            return date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        if (fieldType == LocalTime.class && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            if (date == null) return null;
            return date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime();
        }

        return switch (cell.getCellType()) {
            case STRING -> {
                if (fieldType == Integer.class || fieldType == int.class) {
                    yield Integer.parseInt(cell.getStringCellValue());
                } else if (fieldType == Long.class) {
                    yield Long.parseLong(cell.getStringCellValue());
                } else if (fieldType == Double.class) {
                    yield Double.parseDouble(cell.getStringCellValue());
                }
                yield cell.getStringCellValue();
            }
            case NUMERIC -> {
                double numericValue = cell.getNumericCellValue();
                if (fieldType == Long.class) {
                    yield (long) numericValue;
                } else if (fieldType == Integer.class || fieldType == int.class) {
                    yield (int) numericValue;
                } else if (fieldType == Double.class) {
                    yield numericValue;
                } else {
                    yield numericValue; // fallback
                }
            }
            case BOOLEAN -> cell.getBooleanCellValue();
            default -> null;
        };
    }


}
