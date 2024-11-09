package com.sesac.backend.assignment.domain;

import com.sesac.backend.entity.BaseEntity;
import com.sesac.backend.entity.Course;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

/**
 * @author dongjin
 * 기말고사 도메인
 * FinalExam 테이블 컬럼 정의
 */
@Getter
@Setter
@ToString(exclude = {"course"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class FinalExam extends BaseEntity {

    /**
     * finalExamId:     PK
     * course:          기말고사 생성한 강의
     * startTime:       시작시간
     * endTime:         종료시간
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID finalExamId;
    @OneToOne
    @JoinColumn(name = "courseId", nullable = false)
    private Course course;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
