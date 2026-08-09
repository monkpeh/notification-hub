package com.notifyhub.editrequest;

import com.notifyhub.security.CurrentUser;
import com.notifyhub.template.TemplateEntity;
import com.notifyhub.template.UpdateTemplateRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class EditRequestService {

    private final EditRequestRepository editRequestRepository;

    public EditRequestService(EditRequestRepository editRequestRepository) {
        this.editRequestRepository = editRequestRepository;
    }

    public List<EditRequestEntity> createPendingTemplateEdits(TemplateEntity current, UpdateTemplateRequest request, CurrentUser requestedBy) {
        List<EditRequestEntity> created = new ArrayList<>();

        addIfChanged(created, current.getId(), "templateName", current.getTemplateName(), request.templateName(), requestedBy, request.reason());
        addIfChanged(created, current.getId(), "templateDescription", current.getTemplateDescription(), request.templateDescription(), requestedBy, request.reason());
        addIfChanged(created, current.getId(), "customerType", current.getCustomerType(), request.customerType(), requestedBy, request.reason());
        addIfChanged(created, current.getId(), "language", current.getLanguage(), request.language(), requestedBy, request.reason());
        addIfChanged(created, current.getId(), "priority", current.getPriority(), request.priority(), requestedBy, request.reason());
        addIfChanged(created, current.getId(), "status", current.getStatus(), request.status(), requestedBy, request.reason());

        return created;
    }

    private void addIfChanged(List<EditRequestEntity> created, Long recordId, String fieldName,
                               Object oldValue, Object newValue, CurrentUser requestedBy, String reason) {
        if (Objects.equals(oldValue, newValue)) {
            return;
        }

        EditRequestEntity entity = new EditRequestEntity(
            "template",
            recordId,
            fieldName,
            oldValue == null ? null : oldValue.toString(),
            newValue == null ? null : newValue.toString(),
            requestedBy.userId(),
            reason,
            "public"
        );

        created.add(editRequestRepository.save(entity));
    }
}
