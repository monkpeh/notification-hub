package com.notifyhub.editrequest;

import com.notifyhub.security.CurrentUser;
import com.notifyhub.security.CurrentUserContext;
import com.notifyhub.security.RequireRole;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/edit-requests")
@RequireRole({"APPROVER", "ADMIN", "SUPER_ADMIN"})
public class EditRequestController {

    private final EditRequestService editRequestService;

    public EditRequestController(EditRequestService editRequestService) {
        this.editRequestService = editRequestService;
    }

    @GetMapping
    public List<EditRequestResponse> list(@RequestParam(required = false) String status,
                                           @RequestParam(required = false) Long templateId,
                                           @RequestParam(required = false) OffsetDateTime dateFrom,
                                           @RequestParam(required = false) OffsetDateTime dateTo) {
        return editRequestService.findFiltered(status, templateId, dateFrom, dateTo).stream()
            .map(EditRequestResponse::from)
            .toList();
    }

    @PostMapping("/{id}/approve")
    public EditRequestResponse approve(@PathVariable Long id) {
        CurrentUser currentUser = CurrentUserContext.get();
        return EditRequestResponse.from(editRequestService.approve(id, currentUser));
    }

    @PostMapping("/{id}/reject")
    public EditRequestResponse reject(@PathVariable Long id) {
        CurrentUser currentUser = CurrentUserContext.get();
        return EditRequestResponse.from(editRequestService.reject(id, currentUser));
    }
}
