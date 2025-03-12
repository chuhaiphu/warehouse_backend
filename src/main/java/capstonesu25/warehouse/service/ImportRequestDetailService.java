package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.entity.ImportRequestDetail;
import capstonesu25.warehouse.enums.DetailStatus;
import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestDetailRequest;
import capstonesu25.warehouse.repository.ImportRequestDetailRepository;
import capstonesu25.warehouse.repository.ImportRequestRepository;
import capstonesu25.warehouse.repository.ItemRepository;
import capstonesu25.warehouse.utils.ExcelUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportRequestDetailService {
    private final ImportRequestRepository importRequestRepository;
    private final ImportRequestDetailRepository importRequestDetailRepository;
    private final ItemRepository itemRepository;

    public void createImportRequestDetail(MultipartFile file, Long importRequestId) {
        ImportRequest importRequest = importRequestRepository.findById(importRequestId).orElseThrow();
        List<ImportRequestDetailRequest> list =  ExcelUtil.processExcelFile(file,
                ImportRequestDetailRequest.class);

        for(ImportRequestDetailRequest request : list) {
            ImportRequestDetail importRequestDetail = new ImportRequestDetail();
            importRequestDetail.setImportRequest(importRequest);
            importRequestDetail.setExpectQuantity(request.getQuantity());
            importRequestDetail.setItem(itemRepository.findById(request.getItemId()).orElseThrow());
            importRequestDetail.setStatus(DetailStatus.EXCESS);
            importRequestDetail.setActualQuantity(0);
            importRequestDetailRepository.save(importRequestDetail);
        }

    }
}
