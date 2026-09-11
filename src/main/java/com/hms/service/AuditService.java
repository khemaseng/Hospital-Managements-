package com.hms.service;

import com.hms.model.AuditLog;
import com.hms.model.User;
import com.hms.repository.AuditLogRepository;
import com.hms.util.SessionManager;

import java.util.List;

/**
 * Thin facade so any service can log an action in one line without touching
 * the repository or figuring out "who's the current user" itself.
 */
public class AuditService {

    private final AuditLogRepository auditLogRepository = new AuditLogRepository();

    public void log(String action, String details) {
        User current = SessionManager.getInstance().getCurrentUser();
        Integer userId = current != null ? current.getId() : null;
        String username = current != null ? current.getUsername() : "system";
        auditLogRepository.record(userId, username, action, details);
    }

    /** Overload for events (like a failed login) where there's no session user yet. */
    public void log(String username, String action, String details) {
        auditLogRepository.record(null, username, action, details);
    }

    public List<AuditLog> getRecent(int limit) {
        return auditLogRepository.findRecent(limit);
    }
}
