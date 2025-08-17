create table Permission
(
    id   bigserial primary key,
    name varchar(255) not null unique
);

create table Role
(
    id   bigserial primary key,
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
