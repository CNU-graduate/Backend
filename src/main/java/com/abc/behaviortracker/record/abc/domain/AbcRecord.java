package com.abc.behaviortracker.record.abc.domain;

import com.abc.behaviortracker.global.common.BaseEntity;
import com.abc.behaviortracker.record.session.domain.RecordSession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Table(name = "abc_records",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_abc_records_session",
                        columnNames = "record_session_id"
                )
        })
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AbcRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "abc_record_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_session_id", nullable = false)
    private RecordSession session;

    @Column(name = "content_a", columnDefinition = "TEXT")
    private String contentA;

    @Column(name = "content_b", columnDefinition = "TEXT")
    private String contentB;

    @Column(name = "content_c", columnDefinition = "TEXT")
    private String contentC;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Builder
    private AbcRecord(RecordSession session, String contentA, String contentB, String contentC, Long createdBy) {
        this.session = session;
        this.contentA = contentA;
        this.contentB = contentB;
        this.contentC = contentC;
        this.createdBy = createdBy;
    }

    public void updateContent(String contentA, String contentB, String contentC) {
        if (contentA != null) this.contentA = contentA;
        if (contentB != null) this.contentB = contentB;
        if (contentC != null) this.contentC = contentC;
    }

    public boolean isComplete() {
        return hasText(contentA) && hasText(contentB) && hasText(contentC);
    }

    public void delete() {
        markDeleted();
    }

    public boolean isOwnedBy(Long teacherId) {
        return this.createdBy != null && this.createdBy.equals(teacherId);
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
