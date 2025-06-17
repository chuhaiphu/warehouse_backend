package capstonesu25.warehouse.annotation.transactionLog;

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

    @AfterReturning(pointcut = "@annotation(transactionLoggable)", returning = "result")
    public void logTransaction(JoinPoint joinPoint, TransactionLoggable transactionLoggable, Object result)
            throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String fullName = accountService.findAccountByUsername(username).getFullName();
        String type = transactionLoggable.type();
        String action = transactionLoggable.action();

        // Convert response to JSON
        String responseData = "null";
        Object contentToLog = extractContentFromResponse(result);
        if (contentToLog != null) {
            responseData = objectMapper.writeValueAsString(contentToLog);
        }

        TransactionLog transactionLog = new TransactionLog();
        transactionLog.setExecutorUsername(username);
        transactionLog.setExecutorFullName(fullName);
        transactionLog.setType(type);
        transactionLog.setAction(action);
        transactionLog.setResponseData(responseData);
        transactionLogRepository.save(transactionLog);
    }

    private Object extractContentFromResponse(Object result) {
        try {
            // Check if result is ResponseEntity
            if (result instanceof org.springframework.http.ResponseEntity) {
                org.springframework.http.ResponseEntity<?> responseEntity = (org.springframework.http.ResponseEntity<?>) result;
                Object body = responseEntity.getBody();
                
                // Check if body is ResponseDTO and extract content field
                if (body != null && body.getClass().getSimpleName().equals("ResponseDTO")) {
                    // Use reflection to get the content field
                    java.lang.reflect.Field contentField = body.getClass().getDeclaredField("content");
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
}
