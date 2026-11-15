package com.civicbuild.service;

import com.civicbuild.dto.request.ApplicationCreateRequest;
import com.civicbuild.dto.response.ApplicationDetailResponse;
import com.civicbuild.dto.response.ApplicationSummaryResponse;
import com.civicbuild.entity.Document;
import com.civicbuild.entity.User;
import com.civicbuild.entity.WorkflowHistory;
import com.civicbuild.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ApplicationService {
    ApplicationDetailResponse createDraft(ApplicationCreateRequest request, User applicant);
    ApplicationDetailResponse updateDraft(Long id, ApplicationCreateRequest request, User applicant);
    Document uploadDocument(Long applicationId, MultipartFile file, DocumentType documentType, User user);
    ApplicationDetailResponse submitApplication(Long id, User applicant);
    ApplicationDetailResponse getApplicationById(Long id, User user);
    List<ApplicationSummaryResponse> getMyApplications(User applicant);
    List<ApplicationSummaryResponse> getAllApplications();
    Map<String, Object> getApplicationStatus(Long id, User user);
    List<WorkflowHistory> getApplicationHistory(Long id, User user);
}
