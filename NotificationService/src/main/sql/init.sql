create table client (
    login varchar primary key,
    rating double precision default 1
);

create table topic (
    id integer primary key,
    views integer default 0,
    fame integer default 0,
    owner varchar references client on delete cascade
);