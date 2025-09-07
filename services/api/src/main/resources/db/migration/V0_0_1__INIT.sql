create table Client
(
    id       bigserial primary key,
    login    varchar(255) not null unique,
    name     varchar(255) not null,
    password varchar(255) not null
);

create table Comment
(
    id        bigserial primary key,
    author_id bigint    not null,
    content   text      not null,
    published timestamp not null,
    foreign key (author_id) REFERENCES Client (id) ON DELETE cascade ON UPDATE cascade
);

create type MonetizationStatus as enum (
    'PROCESSING',
    'PENDING_MODERATION',
    'MONETIZED',
    'REJECTED',
    'APPEAL_SUBMITTED'
);

create table Video_Info
(
    id                bigserial primary key,
    title             varchar(511) not null,
    description       text         not null,
    storage_key       varchar(128) not null,
    transcription_key varchar(128) not null,
    published         timestamp    not null,
    author_id         bigint       not null,
    status            varchar(32)  not null,
    foreign key (author_id) REFERENCES Client (id) ON DELETE cascade ON UPDATE cascade
);

create table Monetization_Info
(
    video_id  bigint primary key,
    percent   float not null,
    is_agreed bool  not null,
    foreign key (video_id) REFERENCES Video_Info (id) ON DELETE cascade ON UPDATE cascade
);

create table Appeal
(
    id        bigserial primary key,
    video_id  bigint not null,
    reason    varchar(128),
    processed bool   not null,
    foreign key (video_id) REFERENCES Video_Info (id) ON DELETE cascade ON UPDATE cascade
);
