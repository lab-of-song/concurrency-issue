package com.demo.concurrencyissue.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {

    Optional<TicketHistory> findByUserId(final Long userId);
}
