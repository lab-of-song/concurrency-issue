package com.demo.concurrencyissue.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@Slf4j
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long count;

    @Column
    private final String name;

//    @Version
//    private Integer version;

    private Ticket(final Long id, final Long count, final String name) {
        this.id = id;
        this.count = count;
        this.name = name;
    }

    public static Ticket saved(final Long id, final Long quantity, final String name) {
        return new Ticket(id, quantity, name);
    }

    public static Ticket forSave(final Long quantity, final String name) {
        return new Ticket(null, quantity, name);
    }

    public boolean decreaseTicketCount() {
        if (count == 0) {
//            throw new IllegalArgumentException("티켓이 모두 소진되었습니다.");
            log.error("티켓이 모두 소진되었습니다.");
            return false;
        }

        this.count--;
        return true;
    }

    public Long getId() {
        return id;
    }

    public Long getCount() {
        return count;
    }

    public String getName() {
        return name;
    }
}
