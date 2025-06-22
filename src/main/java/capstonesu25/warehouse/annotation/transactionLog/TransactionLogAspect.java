package capstonesu25.warehouse.annotation.transactionLog;

import java.util.Collection;
import java.lang.reflect.Field;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import capstonesu25.warehouse.entity.TransactionLog;
import capstonesu25.warehouse.repository.TransactionLogRepository;
import capstonesu25.warehouse.service.AccountService;

@Aspect
@Component
public class TransactionLogAspect {
    @Autowired
    private TransactionLogRepository transactionLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountService accountService;

    @AfterReturning(pointcut = "@annotation(transactionLoggable)", returning = "responseContent")
    public void logTransaction(JoinPoint joinPoint, TransactionLoggable transactionLoggable, Object responseContent)
            throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String fullName = accountService.findAccountByUsername(username).getFullName();
        String type = transactionLoggable.type();
        String action = transactionLoggable.action();
        String objectIdSource = transactionLoggable.objectIdSource();

        // Check if response content is a List/Collection
        // If yes - create multiple transaction log records
        // Else - create single transaction log
        if (responseContent instanceof Collection) {
            Collection<?> responseCollection = (Collection<?>) responseContent;
            for (Object responseItem : responseCollection) {
                createTransactionLog(username, fullName, type, action, objectIdSource, responseItem);
            }
        } else {
            createTransactionLog(username, fullName, type, action, objectIdSource, responseContent);
        }
    }
    
    private void createTransactionLog(String username, String fullName, String type, String action, 
                                    String objectIdSource, Object responseItem) throws Exception {
        // Convert individual response item to JSON
        String responseContent = "null";
        if (responseItem != null) {
            responseContent = objectMapper.writeValueAsString(responseItem);
        }

        // Extract objectId from individual response item
        String objectId = null;
        if (objectIdSource != null && !objectIdSource.trim().isEmpty()) {
            objectId = extractObjectIdFromItem(responseItem, objectIdSource);
        }

        TransactionLog transactionLog = new TransactionLog();
        transactionLog.setExecutorUsername(username);
        transactionLog.setExecutorFullName(fullName);
        transactionLog.setType(type);
        transactionLog.setAction(action);
        transactionLog.setObjectId(objectId);
        transactionLog.setResponseContent(responseContent);
        transactionLogRepository.save(transactionLog);
    }

    private String extractObjectIdFromItem(Object responseItem, String objectIdSource) throws Exception {
        // If responseItem is null, return null
        if (responseItem == null) {
            return null;
        }
        
        // Use reflection technique - Get the class of the response item
        // Example: targetClass = class ImportRequestResponse
        Class<?> targetClass = responseItem.getClass();
        Field field = null; // Hold the field (objectIdSource field) we're looking for
        
        // Try to find the field in the class hierarchy
        // Looks in current class first, then parent classes
        while (field == null && targetClass != null) {
            try {
                field = targetClass.getDeclaredField(objectIdSource);
            } catch (NoSuchFieldException e) {
                // Field not found in current class, move to parent class
                targetClass = targetClass.getSuperclass();
            }
        }
        
        // If we found the field, extract its value
        if (field != null) {
            field.setAccessible(true);
            // Get the actual value of the field from the response object
            // Example: value = "PN-20241201-001" (the actual ID value from the response)
            Object value = field.get(responseItem);
            return value != null ? value.toString() : null;
        }
        
        return null;
    }
}
