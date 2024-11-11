package com.sesac.backend.grade.service;


import com.sesac.backend.assignment.domain.Score;
import com.sesac.backend.assignment.dto.ScoreDto;
import com.sesac.backend.assignment.repository.ScoreRepository;
import com.sesac.backend.assignment.service.ScoreService;
import com.sesac.backend.course.repository.CourseOpeningRepository;
import com.sesac.backend.course.repository.CourseRepository;
import com.sesac.backend.entity.Course;
import com.sesac.backend.entity.CourseOpening;
import com.sesac.backend.entity.Grade;
import com.sesac.backend.grade.dto.*;
import com.sesac.backend.grade.repository.GradeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeService {

    private final GradeRepository gradeRepository;
    private final ScoreService scoreService;
    private final CourseOpeningRepository courseOpeningRepository;


    /**
     * Grade 생성 - 다른 서비스에서 호출하여 사용
     */
    @Transactional
    public GradeDto createGrade(Score score, CourseOpening courseOpening) {
        Grade grade = new Grade();
        grade.setScore(score);
        grade.setCourseOpening(courseOpening);

        Grade savedGrade = gradeRepository.save(grade);
        return GradeDto.from(savedGrade);
    }



    // 학생 ID로 조회
    public GradeDto findById(UUID gradeId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 성적 정보가 없습니다. id=" + gradeId));
        return GradeDto.from(grade);
    }


    // 전체 조회
    public List<GradeDto> findAllByCourseCourseNameAndCourseOpeningSemester(String courseName, String semester) {
        // 1차 필터링: 선택한 강의명, 학기에 해당하는 과목 조회 -> 선택한 강의명, 학기에 해당되는 강의 리스트가 나옴
        List<Grade> grades = gradeRepository.findAllByCourseOpeningSemesterAndCourseOpeningCourseCourseName(semester, courseName);
        // 2차 필터링: 각 과목에 해당하는 성적 조회 -> 각 과목에 해당되는 성적 리스트가 나옴
        // Comparator 사용 해서 정렬
        Collections.sort(grades, Comparator.comparing(
                grade -> -(grade.getAssignScore() +
                        grade.getMidtermScore() +
                        grade.getFinalScore())
        ));

        // Grade 엔티티를 GradeDto로 변환
        return grades.stream()
                .map(GradeDto::from)
                .collect(Collectors.toList());
    }

    //수정
    @Transactional
    public GradeDto updateGradeScores(GradeUpdateRequest request) {
        //1. Grade 엔티티 조회
        Grade grade = gradeRepository.findById(request.getGradeId())
                .orElseThrow(() -> new EntityNotFoundException("해당 성적 정보가 없습니다. id=" + request.getGradeId()));

        //2. Score 정보 업데이트를 위한 DTO 생성
        ScoreDto scoreDto = new ScoreDto(
                grade.getScore().getScoreId(),
                grade.getScore().getAssignment().getAssignId(),
                grade.getScore().getMidtermExam().getMidtermExamId(),
                grade.getScore().getFinalExam().getFinalExamId(),
                grade.getScore().getStudent().getStudentId(),
                request.getAssignScore(),
                request.getMidtermScore(),
                request.getFinalScore(),
                grade.getScore().getVisibility()
        );

        //3. ScoreService를 통해 점수 업데이트
        scoreService.updateScore(scoreDto);

        //4. 업데이트된 Grade 정보 변환
        return GradeDto.from(grade);
    }

    //여러 성적 일괄 수정
    @Transactional
    public List<GradeDto> updateMultipleGradeScores(List<GradeUpdateRequest> requests){
        return requests.stream()
                .map(this::updateGradeScores)
                .collect(Collectors.toList());
    }


//--------------------------------------------------------------------
    // 학점 계산
    public GpaCalculationDto calculateGpa(UUID scoreId) {
        // Score 로 해당 학생의 모든 Grage 조회
        List<Grade> grades = gradeRepository.findByScore_ScoreId(scoreId);

        if (grades.isEmpty()) {
            throw new EntityNotFoundException("해당 성적 정보가 없습니다. scoreId=" + scoreId);
        }

        double totalGradePoints = 0.0;
        int totalCredits = 0;

        for (Grade grade : grades) {
            // getTotalScore() 를 사용하여 가중치가 적용된 총점 계산
            int totalScore = grade.getTotalScore();
            // 총점을 GPA로 변환
            double gradePoint = convertScoreToGradePoint(totalScore);

            int courseCredits = grade.getCourseOpening().getCourse().getCredits();
            totalGradePoints += gradePoint * courseCredits;
            totalCredits += courseCredits;
        }
        double gpa = totalCredits > 0 ? totalGradePoints / totalCredits : 0.0;

        GpaCalculationDto result = new GpaCalculationDto();
        result.setGpa(Math.round(gpa * 100.0) / 100.0);
        result.setTotalCredits(totalCredits);
        result.setGrades(grades.stream()
                .map(GradeDto::from)
                .collect(Collectors.toList()));

        return  result;

    }
    /**
     * 점수를 GPA로 변환
     */
    private double convertScoreToGradePoint(int totalScore) {
        if (totalScore >= 95) return 4.5;      // A+
        else if (totalScore >= 90) return 4.0;  // A
        else if (totalScore >= 85) return 3.5;  // B+
        else if (totalScore >= 80) return 3.0;  // B
        else if (totalScore >= 75) return 2.5;  // C+
        else if (totalScore >= 70) return 2.0;  // C
        else if (totalScore >= 65) return 1.5;  // D+
        else if (totalScore >= 60) return 1.0;  // D
        else return 0.0;                        // F
    }


    /**
     * 성적 리스트 조회 (정렬 기능 포함)
     */
//    public List<GradeListResponseDto> getGradeList(UUID openingID, String sortBy) {
//        List<Grade> grades = gradeRepository.findByCourseOpening_OpeningId(openingID);
//
//        // 정렬 기준에 따라 정렬
//        switch (sortBy) {
//            case "totalScore":
//                grades.sort((g1, g2) -> g2.getTotalScore() - g1.getTotalScore());
//                break;
//            case "studentNumber":
//                grades.sort(Comparator.comparing(Grade::getStudentNumber));
//                break;
//            case "studentName":
//                grades.sort(Comparator.comparing(Grade::getStudentName));
//                break;
//            // 필요한 정렬 기준 추가
//        }
//
//        // 순위 부여하여 DTO 변환
//        List<GradeListResponseDto> result = new ArrayList<>();
//        int rank = 1;
//        int prevScore = -1;
//        int sameRankCount = 0;
//
//        for (Grade grade : grades) {
//            if(prevScore != grade.getTotalScore()) {
//                rank += sameRankCount;
//                sameRankCount = 1;
//            } else {
//                sameRankCount++;
//            }
//            result.add(GradeListResponseDto.from(grade, rank));
//            prevScore = grade.getTotalScore();
//        }
//
//        return result;
//
//
//    }

    /**
     * 강의별 성적 리스트 조회 (정렬 기능 포함)
     */
    public List<GradeListResponseDto> getGradeList(UUID openingId, String sortBy) {
        // 해당 강의를 수강하는 모든 학생의 성적 조회
        List<Grade> grades = gradeRepository.findByCourseOpening_OpeningId(openingId);

        if (grades.isEmpty()) {
            throw new EntityNotFoundException("해당 강의의 성적 정보가 없습니다. openingId=" + openingId);
        }

        // 정렬 로직
        switch (sortBy) {
            case "totalScore":
                grades.sort((g1, g2) -> g2.getTotalScore() - g1.getTotalScore());
                break;
            case "studentNumber":
                grades.sort(Comparator.comparing(Grade::getStudentNumber));
                break;
            case "studentName":
                grades.sort(Comparator.comparing(Grade::getStudentName));
                break;
            case "assignScore":
                grades.sort((g1, g2) -> g2.getAssignScore() - g1.getAssignScore());
                break;
            case "midtermScore":
                grades.sort((g1, g2) -> g2.getMidtermScore() - g1.getMidtermScore());
                break;
            case "finalScore":
                grades.sort((g1, g2) -> g2.getFinalScore() - g1.getFinalScore());
                break;
        }
        // 순위 부여하여 DTO 변환
        List<GradeListResponseDto> result = new ArrayList<>();
        int rank = 1;
        int prevScore = -1;
        int sameRankCount = 0;

        for (Grade grade : grades) {
            if(prevScore != grade.getTotalScore()) {
                rank += sameRankCount;
                sameRankCount = 1;
            } else {
                sameRankCount++;
            }

            GradeListResponseDto dto = GradeListResponseDto.from(grade, rank);
            //강의 정보 추가
            dto.setCourseName(grade.getCourseOpening().getCourse().getCourseName());
            dto.setCourseCode(grade.getCourseOpening().getCourse().getCourseCode());
            dto.setSemester(grade.getCourseOpening().getSemester());
            dto.setYear(grade.getCourseOpening().getYear());

            result.add(dto);
            prevScore = grade.getTotalScore();
        }
        return  result;


    }





}
