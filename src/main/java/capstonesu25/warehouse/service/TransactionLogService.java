package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.TransactionLog;
import capstonesu25.warehouse.repository.TransactionLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionLogService {
    private final TransactionLogRepository transactionLogRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionLogService.class);

    public List<TransactionLog> getAllTransactionLogs() {
        LOGGER.info("Get all transaction logs");
        return transactionLogRepository.findAll();
    }
} 