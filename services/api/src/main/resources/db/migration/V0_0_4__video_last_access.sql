ALTER TABLE Video_Info ADD COLUMN last_access_time TIMESTAMP not null;
ALTER TABLE Comment ADD COLUMN status varchar(32) not null;