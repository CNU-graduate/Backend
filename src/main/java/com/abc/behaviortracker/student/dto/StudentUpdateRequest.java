package com.abc.behaviortracker.student.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Map;

public record StudentUpdateRequest(
        @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다")
        @Pattern(regexp = ".*\\S.*", message = "이름은 공백일 수 없습니다")
        String name,

        @Min(value = 1, message = "학년은 1 이상이어야 합니다")
        @Max(value = 12, message = "학년은 12 이하여야 합니다")
        Integer grade,

        @PastOrPresent(message = "생년월일은 미래일 수 없습니다")
        LocalDate birthDate,

        @Size(max = 1000, message = "IEP 요약은 1000자를 초과할 수 없습니다")
        String iepSummary,

        Map<String, Object> metadata
) {}
