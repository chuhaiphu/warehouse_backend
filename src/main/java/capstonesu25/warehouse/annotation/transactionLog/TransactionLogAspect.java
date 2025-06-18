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
import org.springframework.http.ResponseEntity;

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

    @AfterReturning(pointcut = "@annotation(transactionLoggable)", returning = "result")
    public void logTransaction(JoinPoint joinPoint, TransactionLoggable transactionLoggable, Object result)
            throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String fullName = accountService.findAccountByUsername(username).getFullName();
        String type = transactionLoggable.type();
        String action = transactionLoggable.action();
        String objectIdSource = transactionLoggable.objectIdSource();

        // Convert response to JSON
        String responseContent = "null";
        Object contentToLog = extractContentFromResponse(result);
        if (contentToLog != null) {
            responseContent = objectMapper.writeValueAsString(contentToLog);
        }

        // Extract objectId if objectIdSource is specified
        String objectId = null;
        if (objectIdSource != null && !objectIdSource.trim().isEmpty()) {
            objectId = extractObjectId(result, objectIdSource);
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

    private Object extractContentFromResponse(Object result) {
        try {
            // Check if result is ResponseEntity
            if (result instanceof ResponseEntity) {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
                Object body = responseEntity.getBody();
                
                // Check if body is ResponseDTO and extract content field
                if (body != null && body.getClass().getSimpleName().equals("ResponseDTO")) {
                    // Use reflection to get the content field
                    Field contentField = body.getClass().getDeclaredField("content");
                    contentField.setAccessible(true);
                    return contentField.get(body);
                }
            }
            
            // If it's not the expected structure, return the original result
            return result;
        } catch (Exception e) {
            // If extraction fails, return the original result
            return result;
        }
    }

    private String extractObjectId(Object result, String objectIdSource) {
        try {
            Object targetObject = result;
            
            // Check if result is ResponseEntity and extract body
            if (result instanceof ResponseEntity) {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
                Object body = responseEntity.getBody();
                
                // Check if body is ResponseDTO and extract content field
                if (body != null && body.getClass().getSimpleName().equals("ResponseDTO")) {
                    Field contentField = body.getClass().getDeclaredField("content");
                    contentField.setAccessible(true);
                    targetObject = contentField.get(body);
                }
            }
            
            // If targetObject is null, return null
            if (targetObject == null) {
                return null;
            }
            
            // Handle List/Collection case - get first element
            if (targetObject instanceof Collection) {
                Collection<?> collection = (Collection<?>) targetObject;
                if (!collection.isEmpty()) {
                    targetObject = collection.iterator().next();
                } else {
                    return null;
                }
            }
            
            // Use reflection to get the field value
            Class<?> targetClass = targetObject.getClass();
            Field field = null;
            
            // Try to find the field in the class hierarchy
            while (field == null && targetClass != null) {
                try {
                    field = targetClass.getDeclaredField(objectIdSource);
                } catch (NoSuchFieldException e) {
                    targetClass = targetClass.getSuperclass();
                }
            }
            
            if (field != null) {
                field.setAccessible(true);
                Object value = field.get(targetObject);
                return value != null ? value.toString() : null;
            }
            
            return null;
        } catch (Exception e) {
            // Log the exception but don't fail the transaction
            System.err.println("Error extracting objectId: " + e.getMessage());
            return null;
        }
    }
}
