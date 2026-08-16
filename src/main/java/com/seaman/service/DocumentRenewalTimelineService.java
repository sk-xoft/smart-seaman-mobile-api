package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.DocumentRenewalTransactionEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRenewalTimelineItemResponse;
import com.seaman.model.response.DocumentRenewalTimelineResponse;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DocumentRenewalTimelineService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Bangkok");
    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DocumentRenewalFoundationRepository repository;
    private final HttpServletRequest httpServletRequest;

    public DocumentRenewalTimelineResponse timeline(String requestNoInput) {
        String requestNo = normalizeRequestNo(requestNoInput);
        String userUuid = currentUserUuid();
        DocumentRenewalRequestEntity request =
                repository.findOwnedRequestByNo(requestNo, userUuid);
        List<DocumentRenewalTransactionEntity> rows =
                repository.findOwnedTransactions(request.getId(), userUuid);
        List<DocumentRenewalTimelineItemResponse> items = new ArrayList<>();
        for (DocumentRenewalTransactionEntity row : rows) {
            items.add(map(row));
        }
        DocumentRenewalTimelineResponse response = new DocumentRenewalTimelineResponse();
        response.setRequestNo(requestNo);
        response.setItems(items);
        return response;
    }

    private DocumentRenewalTimelineItemResponse map(DocumentRenewalTransactionEntity row) {
        DocumentRenewalTimelineItemResponse response =
                new DocumentRenewalTimelineItemResponse();
        boolean english = isEnglish();
        response.setAction(row.getAction());
        response.setFromStatus(mobileStatusName(row.getFromStatusNameTh(), row.getFromStatusNameEn(), english));
        response.setToStatus(mobileStatusName(row.getToStatusNameTh(), row.getToStatusNameEn(), english));
        if (row.getActionedAt() != null) {
            response.setActionedAt(row.getActionedAt().toInstant()
                    .atZone(BUSINESS_ZONE).format(DISPLAY_DATE));
        }
        response.setDetail(displayDetail(row.getAction(), english));
        return response;
    }

    private String mobileStatusName(String nameTh, String nameEn, boolean english) {
        return english ? nameEn : nameTh;
    }

    private String displayDetail(String action, boolean english) {
        if (english) {
            if ("CREATE".equals(action)) return "Renewal request created";
            if ("SEND_BACK".equals(action)) return "Documents returned for correction";
            if ("RESUBMIT".equals(action)) return "Corrected documents resubmitted";
            if ("CHECK_DOCS".equals(action)) return "Document review completed";
            if ("SUBMIT_TO_DEPT".equals(action)) return "Submitted to the Marine Department";
            if ("RECORD_DEPT_RESULT".equals(action)) return "Marine Department result recorded";
            if ("RECEIVE_FROM_DEPT".equals(action)) return "Document received from the Marine Department";
            if ("RECORD_DELIVERY".equals(action)) return "Document delivery started";
            if ("DELIVERY_COMPLETE".equals(action)) return "Document delivered";
            if ("CANCEL".equals(action)) return "Renewal request cancelled";
            return "Renewal status updated";
        }
        if ("CREATE".equals(action)) return "สร้างคำขอต่ออายุเอกสารแล้ว";
        if ("SEND_BACK".equals(action)) return "ส่งเอกสารกลับเพื่อแก้ไข";
        if ("RESUBMIT".equals(action)) return "ส่งเอกสารแก้ไขเพื่อตรวจสอบอีกครั้งแล้ว";
        if ("CHECK_DOCS".equals(action)) return "ตรวจเอกสารเรียบร้อยแล้ว";
        if ("SUBMIT_TO_DEPT".equals(action)) return "ยื่นคำขอต่อกรมเจ้าท่าแล้ว";
        if ("RECORD_DEPT_RESULT".equals(action)) return "บันทึกผลจากกรมเจ้าท่าแล้ว";
        if ("RECEIVE_FROM_DEPT".equals(action)) return "รับเอกสารจากกรมเจ้าท่าแล้ว";
        if ("RECORD_DELIVERY".equals(action)) return "เริ่มจัดส่งเอกสารแล้ว";
        if ("DELIVERY_COMPLETE".equals(action)) return "จัดส่งเอกสารสำเร็จแล้ว";
        if ("CANCEL".equals(action)) return "ยกเลิกคำขอต่ออายุแล้ว";
        return "อัปเดตสถานะคำขอต่ออายุแล้ว";
    }

    private boolean isEnglish() {
        return "EN".equalsIgnoreCase(
                (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
    }

    private String currentUserUuid() {
        UsersEntity user = (UsersEntity) httpServletRequest.getAttribute("userObject");
        if (user == null || user.getMobileUuid() == null || user.getMobileUuid().trim().isEmpty()) {
            throw new BusinessException(AppStatus.USERNAME_IS_NOT_FOUND_SECURITY_CONTEXT, "userObject");
        }
        return user.getMobileUuid();
    }

    private String normalizeRequestNo(String value) {
        if (value == null || value.trim().isEmpty() || value.trim().length() > 20
                || !value.trim().matches("[A-Za-z0-9_-]+")) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "requestNo");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
