package com.example.fxdeals.repository;

import com.example.fxdeals.domain.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DealRepository extends JpaRepository<Deal, UUID> {
}
