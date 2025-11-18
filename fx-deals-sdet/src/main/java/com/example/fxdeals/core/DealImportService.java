package com.example.fxdeals.core;

import com.example.fxdeals.domain.Deal;
import com.example.fxdeals.repository.DealRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DealImportService {

    private final DealRepository repo;
    private final DealParser parser = new DealParser();

    public DealImportService(DealRepository repo) {
        this.repo = repo;
    }

    public ImportResponse importBatch(List<DealRequest> rows) {
        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger duplicates = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        ImportResponse.Builder builder = ImportResponse.builder().total(rows == null ? 0 : rows.size());

        if (rows == null || rows.isEmpty()) {
            return builder.accepted(0).duplicates(0).rejected(0).build();
        }

        for (int i = 0; i < rows.size(); i++) {
            DealRequest r = rows.get(i);
            DealParser.ParseResult parsed = parser.parse(r);
            if (!parsed.isOk()) {
                rejected.incrementAndGet();
                builder.addError(new ErrorRow(i, parsed.errorCode, parsed.errorMessage));
                continue;
            }
            RowStatus status = importOne(parsed.deal);
            if (status == RowStatus.ACCEPTED) {
                accepted.incrementAndGet();
            } else if (status == RowStatus.DUPLICATE) {
                duplicates.incrementAndGet();
            } else {
                rejected.incrementAndGet();
                builder.addError(new ErrorRow(i, "PERSISTENCE_ERROR", "Unexpected DB error"));
            }
        }
        return builder.accepted(accepted.get()).duplicates(duplicates.get()).rejected(rejected.get()).build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RowStatus importOne(Deal d) {
        try {
            repo.save(d);
            return RowStatus.ACCEPTED;
        } catch (DataIntegrityViolationException e) {
            // Primary key conflict -> duplicate
            return RowStatus.DUPLICATE;
        } catch (RuntimeException e) {
            return RowStatus.REJECTED;
        }
    }
}
