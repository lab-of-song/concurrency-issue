package com.demo.concurrencyissue.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column
    private String email;

    private User(final Long id, final String username, final String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public static User saved(final Long id, final String username, final String email) {
        return new User(id, username, email);
    }

    public static User forSave(final String username, final String email) {
        return new User(null, username, email);
    }
}
