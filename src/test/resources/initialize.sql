 drop table if exists user;
 drop table if exists ticket_history;
 drop table if exists ticket;

create table ticket (
    count bigint,
    id bigint not null auto_increment,
    name varchar(255),
--    version int,
    primary key (id)
) engine=InnoDB;

create table ticket_history (
    id bigint not null auto_increment,
    ticket_id bigint,
    user_id bigint,
    primary key (id)
) engine=InnoDB;

create table user (
    id bigint not null auto_increment,
    email varchar(255),
    username varchar(255),
    primary key (id)
) engine=InnoDB;

insert into ticket (id, count, name) values (1, 1, 'ticket1');
--insert into ticket (id, count, name, version) values (1, 1, 'ticket1', 0);
insert into user(id, email, username) values (1, "first", "first");
insert into user(id, email, username) values (2, "second", "second");
