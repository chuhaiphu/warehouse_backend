package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.Department;
import capstonesu25.warehouse.model.department.DepartmentResponse;
import capstonesu25.warehouse.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(DepartmentService.class);

    public Page<DepartmentResponse> getAllDepartments(int page, int limit) {
        logger.info("Fetching all departments");
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Department> departments = departmentRepository.findAll(pageable);
        return departments.map(this::convertToResponse);
    }

    public DepartmentResponse getDepartmentById(Long id) {
        logger.info("Fetching department with ID: {}", id);
        return departmentRepository.findById(id)
                .map(this::convertToResponse)
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + id));
    }

    private DepartmentResponse convertToResponse(capstonesu25.warehouse.entity.Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getDepartmentName(),
                department.getDepartmentResponsible(),
                department.getLocation(),
                department.getPhone()
        );
    }

}
