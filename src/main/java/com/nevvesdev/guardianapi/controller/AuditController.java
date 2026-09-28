package com.nevvesdev.guardianapi.controller;

import com.nevvesdev.guardianapi.dto.response.AuditLogResponse;
import com.nevvesdev.guardianapi.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> findAll() {
        return ResponseEntity.ok(auditService.findAll());
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<AuditLogResponse>> findByUserEmail(@PathVariable String email) {
        return ResponseEntity.ok(auditService.findByUserEmail(email));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLogResponse>> findByEntity(@PathVariable String entityType,
                                                               @PathVariable String entityId) {
        return ResponseEntity.ok(auditService.findByEntity(entityType, entityId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<AuditLogResponse>> findByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(auditService.findByDateRange(start, end));
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLogResponse>> findByAction(@PathVariable String action) {
        return ResponseEntity.ok(auditService.findByAction(action));
    }
}