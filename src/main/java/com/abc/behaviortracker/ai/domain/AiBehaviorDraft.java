package com.abc.behaviortracker.ai.domain;

import com.abc.behaviortracker.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * AI 분석 서버(ai-server)가 생성한 행동 기록 초안.
 *
 * <p>외부 AI 응답(behavior/confidence/draftText)을 교사 확인 전 {@link DraftStatus#DRAFT}
 * 상태로 보관한다. 좌석 매핑 전일 수 있어 studentId 는 nullable 이다.
 *
 * <p>※ ai-server 스펙 미확정. 세션/미디어와의 연결(예: record_session_id, media_id)은
 * 스펙 확정 후 추가 가능. 현재는 trackId/studentId 만 보관한다.
 */
@Getter
@Entity
@Table(name = "ai_behavior_drafts",
        indexes = {
                @Index(name = "idx_ai_drafts_student", columnList = "student_id"),
                @Index(name = "idx_ai_drafts_track", columnList = "track_id")
        })
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiBehaviorDraft extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_behavior_draft_id")
    private Long id;

    @Column(name = "track_id")
    private Long trackId;

    /** 좌석 매핑 전일 수 있어 nullable. */
    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "behavior", nullable = false, length = 50)
    private String behavior;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "draft_text", columnDefinition = "TEXT")
    private String draftText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DraftStatus status;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Builder
    private AiBehaviorDraft(Long trackId, Long studentId, String behavior,
                           Double confidence, String draftText, Long createdBy) {
        this.trackId = trackId;
        this.studentId = studentId;
        this.behavior = behavior;
        this.confidence = confidence;
        this.draftText = draftText;
        this.status = DraftStatus.DRAFT;
        this.createdBy = createdBy;
    }

    /** 교사가 초안을 확정한다. */
    public void confirm() {
        this.status = DraftStatus.CONFIRMED;
    }

    public void delete() {
        markDeleted();
    }

    public boolean isOwnedBy(Long teacherId) {
        return this.createdBy != null && this.createdBy.equals(teacherId);
    }
}
