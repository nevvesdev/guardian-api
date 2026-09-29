package com.nevvesdev.guardianapi.controller;

import com.nevvesdev.guardianapi.dto.response.ResourceHistoryResponse;
import com.nevvesdev.guardianapi.service.ResourceHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
@Slf4j
public class ResourceHistoryController {

    private final ResourceHistoryService resourceHistoryService;

    @GetMapping("/{id}/history")
    public ResponseEntity<List<ResourceHistoryResponse>> findHistory(@PathVariable String id) {
        return ResponseEntity.ok(resourceHistoryService.findByResourceId(id));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable String id) {
        resourceHistoryService.restore(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history/user/{email}")
    public ResponseEntity<List<ResourceHistoryResponse>> findByChangedBy(@PathVariable String email) {
        return ResponseEntity.ok(resourceHistoryService.findByChangedBy(email));
    }
}