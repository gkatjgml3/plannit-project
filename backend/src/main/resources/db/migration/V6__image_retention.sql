-- 원본 이미지 보관 정책. 분석이 확정되면 바로, 확정되지 않았으면 업로드 후 일정 기간(기본 7일)이 지나면
-- 저장소에서 이미지를 지운다. 분석 기록과 후보는 남기고 image_deleted_at으로 이미지가 지워졌다는 것만 표시한다.
-- 저장소 삭제에 실패하면 값이 null로 남아 다음 정리 때 다시 시도한다.
alter table image_analyses add column image_deleted_at timestamp with time zone;

create index idx_image_analyses_image_cleanup on image_analyses (image_deleted_at, status, created_at);
