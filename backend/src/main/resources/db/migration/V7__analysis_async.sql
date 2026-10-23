-- 비동기 분석. 실행기가 분석을 가져가면(pending → processing) 시작 시각을 기록한다.
-- 서버가 재시작되거나 실행기가 멈춰 processing이 오래 남으면, 이 시각을 보고 다시 대기로 돌리거나 실패 처리한다.
alter table image_analyses add column processing_started_at timestamp with time zone;

create index idx_image_analyses_status on image_analyses (status, processing_started_at);
