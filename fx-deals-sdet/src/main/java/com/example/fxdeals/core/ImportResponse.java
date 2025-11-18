package com.example.fxdeals.core;

import java.util.ArrayList;
import java.util.List;

public class  ImportResponse {
    private final int total;
    private final int accepted;
    private final int duplicates;
    private final int rejected;
    private final List<ErrorRow> errors;

    private ImportResponse(int total, int accepted, int duplicates, int rejected, List<ErrorRow> errors) {
        this.total = total;
        this.accepted = accepted;
        this.duplicates = duplicates;
        this.rejected = rejected;
        this.errors = errors;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        int total;
        int accepted;
        int duplicates;
        int rejected;
        List<ErrorRow> errors = new ArrayList<>();

        public Builder total(int v) { this.total = v; return this; }
        public Builder accepted(int v) { this.accepted = v; return this; }
        public Builder duplicates(int v) { this.duplicates = v; return this; }
        public Builder rejected(int v) { this.rejected = v; return this; }
        public Builder addError(ErrorRow e) { this.errors.add(e); return this; }
        public ImportResponse build() { return new ImportResponse(total, accepted, duplicates, rejected, errors); }
    }

    public int getTotal() { return total; }
    public int getAccepted() { return accepted; }
    public int getDuplicates() { return duplicates; }
    public int getRejected() { return rejected; }
    public List<ErrorRow> getErrors() { return errors; }
    
}
