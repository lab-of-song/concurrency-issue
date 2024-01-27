package com.demo.concurrencyissue.domain;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    default Ticket getById(final Long id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 티켓입니다."));
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t from Ticket t where t.id = :id")
    Ticket getByIdForUpdate(@Param("id") final Long id);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT t from Ticket t where t.id = :id")
    Ticket getByIdWithVersion(@Param("id") final Long id);

    @Query("UPDATE Ticket t SET t.count = t.count - 1 WHERE t.id = :id")
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void decreaseCountById(@Param("id") final Long id);

    @Query("SELECT t.count FROM Ticket t WHERE t.id = :id")
    long getTicketCountById(@Param("id") final Long id);
}
