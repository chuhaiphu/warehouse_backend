package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.StaffPerformance;
import capstonesu25.warehouse.enums.AccountStatus;
import capstonesu25.warehouse.model.staffperformance.StaffPerformanceResponse;
import capstonesu25.warehouse.repository.StaffPerformanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffPerformanceService {
    private final StaffPerformanceRepository staffPerformanceRepository;

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
