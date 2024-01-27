package com.demo.concurrencyissue.application;

import com.demo.concurrencyissue.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketHistoryRepository historyRepository;

    public Ticket save(final long ticketCount, final String name) {
        final Ticket ticket = Ticket.forSave(ticketCount, name);
        return ticketRepository.save(ticket);
    }

    public Optional<TicketHistory> findByUser(final User user) {
        return historyRepository.findByUserId(user.getId());
    }

    public synchronized void sellTicketBySynchronized(final User user, final long ticketId) {
        final Ticket ticket = ticketRepository.getById(ticketId);
        decreaseTicketCount(ticket, user);
    }

    private void decreaseTicketCount(final Ticket ticket, final User user) {
        if (ticket.decreaseTicketCount()) {
            historyRepository.save(new TicketHistory(user.getId(), ticket));
        }

//        ticket.decreaseTicketCount();
//        if (!logIfLeftTicketCountMinus(ticket.getId())) {
//            historyRepository.save(new TicketHistory(user.getId(), ticket));
//        }
    }

    // JPA 더티체킹
    public void sellTicketByDirtyChecking(final User user, final long ticketId) {
        final Ticket ticket = ticketRepository.getById(ticketId);
        decreaseTicketCount(ticket, user);
    }

    // select for update -> 조회 시점부터 트랜잭션이 끝날 때 까지 해당 자원에 대해 X-lock을 점유한다.
    // 따라서, 한 트랜잭션이 자원을 읽는 시점부터 다른 트랜잭션에서는 해당 자원에 접근하지 못한다. => 동시성 해결
    public void sellTicketBySelectForUpdate(final User user, final long ticketId) {
        final Ticket ticket = ticketRepository.getByIdForUpdate(ticketId);
        decreaseTicketCount(ticket, user);
    }

    public void sellTicketByOptimisticLock(final User user, final long ticketId) {
        final Ticket ticket = ticketRepository.getByIdWithVersion(ticketId);
        decreaseTicketCount(ticket, user);
    }

    public void sellTicketByReadAndUpdate(final User user, final long ticketId) {
        final Ticket ticket = ticketRepository.getById(ticketId); // 티켓 존재여부만 확인

        ticketRepository.decreaseCountById(ticketId);
        final long ticketCount = ticketRepository.getTicketCountById(ticketId);
        if (ticketCount < 0) {
//            throw new IllegalArgumentException("티켓이 모두 소진되었습니다.");
            log.error("티켓이 모두 소진되었습니다.");
            return;
        }
        historyRepository.save(new TicketHistory(user.getId(), ticket));
    }

    private void saveTicketHistory(final User user, final Ticket ticket) {
        historyRepository.save(new TicketHistory(user.getId(), ticket));
    }
}
