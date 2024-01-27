package com.demo.concurrencyissue.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "ticket_id", foreignKey = @ForeignKey(name = "none"))
    private Ticket ticket;

    public TicketHistory(final Long userId, final Ticket ticket) {
        this.userId = userId;
        this.ticket = ticket;
    }
}
