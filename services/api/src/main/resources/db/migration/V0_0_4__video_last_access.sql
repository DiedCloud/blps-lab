ALTER TABLE Video_Info ADD COLUMN last_access_time TIMESTAMP not null default now();
ALTER TABLE Comment ADD COLUMN status varchar(32) not null default 'APPROVED';
ALTER TABLE Comment DROP CONSTRAINT fk_comment_video_id;
ALTER TABLE Comment ADD CONSTRAINT fk_comment_video_id FOREIGN KEY (video_id)
REFERENCES Video_Info(id) ON DELETE CASCADE;