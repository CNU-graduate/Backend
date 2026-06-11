-- US-11: RecordSession 메모 필드 추가
-- 적용 대상: dev (ddl-auto=update가 자동 처리하나 명시 목적), prod (validate 모드라 수동 적용 필수)
-- 적용 시점: 애플리케이션 배포 직전

ALTER TABLE record_sessions
    ADD COLUMN memo VARCHAR(2000);