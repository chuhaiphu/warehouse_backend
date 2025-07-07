package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.Account;
import capstonesu25.warehouse.entity.ExportRequest;
import capstonesu25.warehouse.entity.ImportOrder;
import capstonesu25.warehouse.entity.StaffPerformance;
import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.enums.AccountStatus;
import capstonesu25.warehouse.model.staffperformance.StaffPerformanceResponse;
import capstonesu25.warehouse.model.staffperformance.TaskInDateOfStaff;
import capstonesu25.warehouse.repository.AccountRepository;
import capstonesu25.warehouse.repository.ExportRequestRepository;
import capstonesu25.warehouse.repository.ImportOrderRepository;
import capstonesu25.warehouse.repository.StaffPerformanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffPerformanceService {
    private final StaffPerformanceRepository staffPerformanceRepository;
    private final AccountRepository accountRepository;
    private final ImportOrderRepository importOrderRepository;
    private final ExportRequestRepository exportRequestRepository;

    public List<StaffPerformanceResponse> getAllActive(LocalDate date) {
        List<StaffPerformance> staffPerformanceList = staffPerformanceRepository.findAll();
        List<StaffPerformanceResponse> responseList = new ArrayList<>();

        for (StaffPerformance staffPerformance : staffPerformanceList) {
            if (staffPerformance.getAssignedStaff().getStatus().equals(AccountStatus.ACTIVE)) {
                List<StaffPerformance> performance = staffPerformanceRepository.findByAssignedStaff_IdAndDate(
                        staffPerformance.getAssignedStaff().getId(),
                        date
                );

                LocalTime totalActualWorkingTimeOfRequestInDay = LocalTime.of(0, 0);
                LocalTime totalExpectedWorkingTimeOfRequestInDay = LocalTime.of(0, 0);

                for (StaffPerformance p : performance) {
                    totalExpectedWorkingTimeOfRequestInDay = totalExpectedWorkingTimeOfRequestInDay.plusMinutes(p.getExpectedWorkingTime().toSecondOfDay() / 60);

                    if (p.getActualWorkingTime() != null) {
                        totalActualWorkingTimeOfRequestInDay = totalActualWorkingTimeOfRequestInDay.plusMinutes(p.getActualWorkingTime().toSecondOfDay() / 60);
                    }
                }

                StaffPerformanceResponse response = createStaffPerformanceResponse(staffPerformance, performance.size(),
                        totalExpectedWorkingTimeOfRequestInDay, totalActualWorkingTimeOfRequestInDay);

                responseList.add(response);
            }
        }
        return responseList;
    }

    public List<StaffPerformanceResponse> getAllByAccountIdAndDate(Long accountId, LocalDate date) {
        List<StaffPerformance> staffPerformanceList = staffPerformanceRepository.findByAssignedStaff_IdAndDate(accountId, date);
        List<StaffPerformanceResponse> responseList = new ArrayList<>();

        for (StaffPerformance staffPerformance : staffPerformanceList) {
            if (staffPerformance.getAssignedStaff().getStatus().equals(AccountStatus.ACTIVE)) {
                List<StaffPerformance> performance = staffPerformanceRepository.findByAssignedStaff_IdAndDate(
                        staffPerformance.getAssignedStaff().getId(),
                        date
                );

                LocalTime totalActualWorkingTimeOfRequestInDay = LocalTime.of(0, 0);
                LocalTime totalExpectedWorkingTimeOfRequestInDay = LocalTime.of(0, 0);

                for (StaffPerformance p : performance) {
                    totalExpectedWorkingTimeOfRequestInDay = totalExpectedWorkingTimeOfRequestInDay.plusMinutes(p.getExpectedWorkingTime().toSecondOfDay() / 60);

                    if (p.getActualWorkingTime() != null) {
                        totalActualWorkingTimeOfRequestInDay = totalActualWorkingTimeOfRequestInDay.plusMinutes(p.getActualWorkingTime().toSecondOfDay() / 60);
                    }
                }

                StaffPerformanceResponse response = createStaffPerformanceResponse(staffPerformance, performance.size(),
                        totalExpectedWorkingTimeOfRequestInDay, totalActualWorkingTimeOfRequestInDay);
                responseList.add(response);
            }
        }
        return responseList;
    }

    public TaskInDateOfStaff getListImportAndExportOfStaffInDate(Long accountId, LocalDate date) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + accountId));
        if(!account.getRole().equals(AccountRole.STAFF)) {
            throw new IllegalArgumentException("Account with ID " + accountId + " is not a staff member.");
        }
        List<ImportOrder> importOrders = importOrderRepository.findByAssignedStaff_IdAndDateReceived(accountId, date);
        if(!importOrders.isEmpty()) {
            importOrders.sort(Comparator.comparing(ImportOrder::getCreatedDate));
        }
        List<ExportRequest> exportRequestOfCounting = exportRequestRepository.findByCountingDateAndCountingStaffId(date, accountId);
        if(!exportRequestOfCounting.isEmpty()) {
            exportRequestOfCounting.sort(Comparator.comparing(ExportRequest::getCreatedDate));
        }

        List<ExportRequest> exportRequestOfAssigned = exportRequestRepository.findAllByAssignedStaff_IdAndExportDate(accountId, date);
        if(!exportRequestOfAssigned.isEmpty()) {
            exportRequestOfAssigned.sort(Comparator.comparing(ExportRequest::getCreatedDate));
        }
        List<String> importOrderIds = Optional.ofNullable(importOrders)
                .orElse(Collections.emptyList())
                .stream()
                .map(ImportOrder::getId)
                .toList();

        List<String> exportRequestIdsOfCounting = Optional.ofNullable(exportRequestOfCounting)
                .orElse(Collections.emptyList())
                .stream()
                .map(ExportRequest::getId)
                .toList();

        List<String> exportRequestIdsOfConfirmation = Optional.ofNullable(exportRequestOfAssigned)
                .orElse(Collections.emptyList())
                .stream()
                .map(ExportRequest::getId)
                .toList();

        return new TaskInDateOfStaff(importOrderIds, exportRequestIdsOfCounting, exportRequestIdsOfConfirmation);
    }

    private StaffPerformanceResponse createStaffPerformanceResponse(StaffPerformance performance, int requestCount,
                                                                    LocalTime totalExpected, LocalTime totalActual) {
        StaffPerformanceResponse response = new StaffPerformanceResponse();
        response.setId(performance.getId());
        response.setAccountId(performance.getAssignedStaff().getId());
        response.setDate(performance.getDate());
        response.setExpectedWorkingTimeOfRequest(performance.getExpectedWorkingTime());
        response.setActualWorkingTimeOfRequest(performance.getActualWorkingTime());
        response.setNumberOfRequestInDay(requestCount);
        response.setTotalExpectedWorkingTimeOfRequestInDay(totalExpected);
        response.setTotalActualWorkingTimeOfRequestInDay(totalActual);
        return response;
    }
}
