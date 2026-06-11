package com.abc.behaviortracker.record.session.weather.domain;

import com.abc.behaviortracker.global.common.BaseEntity;
import com.abc.behaviortracker.record.session.domain.RecordSession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Entity
@Table(name = "weather_snapshots",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_weather_snapshot_session",
                columnNames = "session_id"
        ))
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeatherSnapshot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weather_snapshot_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private RecordSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_main", nullable = false, length = 20)
    private WeatherCondition condition;

    @Column(name = "condition_description", length = 100)
    private String conditionDescription;

    @Column(name = "icon_code", nullable = false, length = 10)
    private String iconCode;

    @Column(name = "temperature_celsius", nullable = false, precision = 5, scale = 2)
    private BigDecimal temperatureCelsius;

    @Column(name = "humidity_percent", nullable = false)
    private Integer humidityPercent;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "location_name", length = 100)
    private String locationName;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    @Builder
    private WeatherSnapshot(RecordSession session,
                            WeatherCondition condition,
                            String conditionDescription,
                            String iconCode,
                            BigDecimal temperatureCelsius,
                            Integer humidityPercent,
                            BigDecimal latitude,
                            BigDecimal longitude,
                            String locationName,
                            Instant observedAt) {
        this.session = session;
        this.condition = condition;
        this.conditionDescription = conditionDescription;
        this.iconCode = iconCode;
        this.temperatureCelsius = temperatureCelsius;
        this.humidityPercent = humidityPercent;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.observedAt = observedAt;
    }

    public void delete() {
        markDeleted();
    }

    public boolean isOwnedBy(Long teacherId) {
        return this.session != null && this.session.isOwnedBy(teacherId);
    }
}
