create table Permission
(
    id   bigint primary key,
    name varchar(255) not null unique
);

create table Role
(
    id   bigint primary key,
    name varchar(255) not null unique
);

create table role_permissions
(
    role_id       bigint,
    permission_id bigint,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES Role (id) ON DELETE cascade ON UPDATE cascade,
    FOREIGN KEY (permission_id) REFERENCES Permission (id) ON DELETE cascade ON UPDATE cascade
);

create table user_roles
(
    user_id      bigint,
    role_id      bigint,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES Client (id) ON DELETE cascade ON UPDATE cascade,
    FOREIGN KEY (role_id) REFERENCES Role (id) ON DELETE cascade ON UPDATE cascade
);

insert into Permission (id, name) values
(1, 'request_monetization_on_any_video'), (2, 'moderate_monetization_request'), (3, 'appeal_monetization_on_any_video'),
(4, 'edit_any_comment'), (5, 'delete_any_comment'),
(6, 'edit_any_video'), (7, 'delete_any_video');

insert into Role (id, name) values (1, 'ADMIN'), (2, 'MODERATOR'), (3, 'ADVERTISER'), (4, 'CLIENT');

insert into role_permissions (role_id, permission_id) values
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(1, 6),
(1, 7),
(2, 4),
(2, 5),
(2, 6),
(2, 7),
(3, 1),
(3, 2),
(3, 3);
