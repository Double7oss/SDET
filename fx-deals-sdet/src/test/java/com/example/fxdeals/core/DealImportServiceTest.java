package com.example.fxdeals.core;

import com.example.fxdeals.domain.Deal;
import com.example.fxdeals.repository.DealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DealImportServiceTest {

    DealRepository repo;
    DealImportService service;

    @BeforeEach
    void setup() {
        repo = Mockito.mock(DealRepository.class);
        service = new DealImportService(repo);
    }

    @Test
    void importOneAccepted() {
        Deal d = new Deal(UUID.randomUUID(), "USD", "EUR", Instant.parse("2025-01-01T00:00:00Z"), new BigDecimal("1"));
        RowStatus status = service.importOne(d);
        assertEquals(RowStatus.ACCEPTED, status);
        verify(repo).save(d);
    }

    @Test
    void importOneDuplicate() {
        Deal d = new Deal(UUID.randomUUID(), "USD", "EUR", Instant.now(), new BigDecimal("1"));
        when(repo.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));
        RowStatus status = service.importOne(d);
        assertEquals(RowStatus.DUPLICATE, status);
    }

    @Test
    void importOneRejectedOnRuntimeFailure() {
        Deal d = new Deal(UUID.randomUUID(), "USD", "EUR", Instant.now(), new BigDecimal("1"));
        when(repo.save(any())).thenThrow(new RuntimeException("boom"));
        RowStatus status = service.importOne(d);
        assertEquals(RowStatus.REJECTED, status);
    }

    @Test
    void importBatchPartialSuccess() {
        DealRequest ok = new DealRequest(UUID.randomUUID().toString(), "USD", "EUR", "2025-01-01T00:00:00Z", new BigDecimal("1"));
        DealRequest bad = new DealRequest("not-uuid", "USD", "EUR", "2025-01-01T00:00:00Z", new BigDecimal("1"));
        ImportResponse res = service.importBatch(List.of(ok, bad));
        assertEquals(2, res.getTotal());
        assertEquals(1, res.getAccepted());
        assertEquals(0, res.getDuplicates());
        assertEquals(1, res.getRejected());
        verify(repo, times(1)).save(any());
    }

    @Test
    void importBatchHandlesNullInput() {
        ImportResponse res = service.importBatch(null);
        assertEquals(0, res.getTotal());
        assertEquals(0, res.getAccepted());
        assertEquals(0, res.getDuplicates());
        assertEquals(0, res.getRejected());
        verifyNoInteractions(repo);
    }

    @Test
    void importBatchHandlesEmptyList() {
        ImportResponse res = service.importBatch(List.of());
        assertEquals(0, res.getTotal());
        assertEquals(0, res.getAccepted());
        assertEquals(0, res.getDuplicates());
        assertEquals(0, res.getRejected());
        verifyNoInteractions(repo);
    }

    @Test
    void importBatchCountsDuplicates() {
        DealRequest first = new DealRequest(UUID.randomUUID().toString(), "USD", "EUR", "2025-01-01T00:00:00Z", new BigDecimal("1"));
        DealRequest second = new DealRequest(first.dealId, "USD", "EUR", "2025-01-01T00:00:00Z", new BigDecimal("1"));
        when(repo.save(any()))
                .thenAnswer(invocation -> null)
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        ImportResponse res = service.importBatch(List.of(first, second));

        assertEquals(2, res.getTotal());
        assertEquals(1, res.getAccepted());
        assertEquals(1, res.getDuplicates());
        assertEquals(0, res.getRejected());
        verify(repo, times(2)).save(any());
    }

    @Test
    void importBatchRecordsPersistenceErrors() {
        DealRequest req = new DealRequest(UUID.randomUUID().toString(), "USD", "EUR", "2025-01-01T00:00:00Z", new BigDecimal("1"));
        when(repo.save(any())).thenThrow(new RuntimeException("db down"));

        ImportResponse res = service.importBatch(List.of(req));

        assertEquals(1, res.getTotal());
        assertEquals(0, res.getAccepted());
        assertEquals(0, res.getDuplicates());
        assertEquals(1, res.getRejected());
        assertEquals(1, res.getErrors().size());
        assertEquals("PERSISTENCE_ERROR", res.getErrors().get(0).getCode());
    }
}
