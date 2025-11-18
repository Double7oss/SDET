package com.example.fxdeals.web;

import com.example.fxdeals.core.DealImportService;
import com.example.fxdeals.core.DealRequest;
import com.example.fxdeals.core.ImportResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deals")
public class DealsController {

    private final DealImportService service;

    public DealsController(DealImportService service) {
        this.service = service;
    }

    /** Import endpoint that accepts a JSON array. Returns summary with partial success semantics. */
    @PostMapping("/import")
    public ResponseEntity<ImportResponse> importDeals(@RequestBody List<DealRequest> rows) {
        ImportResponse result = service.importBatch(rows);
        return ResponseEntity.ok(result);
    }
}
