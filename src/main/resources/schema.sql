create table ticket (
    id bigint not null primary key auto_increment,
    count bigint not null,
    name varchar(255) not null,
--    version integer
) engine=InnoDB;