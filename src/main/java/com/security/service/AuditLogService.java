package com.security.service;

import com.security.entity.AuditLog;
import com.security.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String username, String action, String ipAddress, boolean success) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action);
        log.setIpAddress(ipAddress);
        log.setSuccess(success);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsByUsername(String username) {
        return auditLogRepository.findByUsername(username);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }
}