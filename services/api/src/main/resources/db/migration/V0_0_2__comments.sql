ALTER TABLE "comment"
ADD COLUMN video_id bigint not null default 0,
ADD CONSTRAINT fk_comment_video_id
FOREIGN KEY (video_id) REFERENCES video_info (id);
