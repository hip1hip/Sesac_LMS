package com.sesac.backend.grade.controller;

import java.util.List;

import java.util.UUID;  // 이 import 추가


import com.sesac.backend.grade.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.sesac.backend.grade.service.GradeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;



@Tag(name = "성적 관리", description = "성적 조회 및 관리 관련 API")
@Slf4j
@RestController
@RequestMapping("/grades")
@RequiredArgsConstructor
public class GradeController {
    private final GradeService gradeService;

    @Operation(summary = "단일 성적 조회", description = "특정 학생의 성적 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "성적 정보를 찾을 수 없음")
    @GetMapping("/{gradeId}")
    public ResponseEntity<GradeDto> getGrade(
            @Parameter(description = "성적 ID") @PathVariable UUID gradeId) {
        return ResponseEntity.ok(gradeService.findById(gradeId));
    }

    @Operation(summary = "강의별 성적 조회", description = "강의명과 학기로 전체 성적을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/course")
    public ResponseEntity<List<GradeDto>> getAllGradesByCourseAndSemester(
            @Parameter(description = "강의명") @RequestParam String courseName,
            @Parameter(description = "학기 (예: Fall, Spring)") @RequestParam String semester ,
            @Parameter(description = "년도") @RequestParam int year) {
        return ResponseEntity.ok(gradeService.findAllByCourseCourseNameAndCourseOpeningSemesterAndCourseOpeningYear(courseName, semester, year));
    }

//     @GetMapping("/course")
// public ResponseEntity<List<GradeDto>> getAllGradesByCourseAndSemester(
//         @Parameter(description = "강의 코드") @RequestParam String courseCode,
//         @Parameter(description = "학기 (예: Fall, Spring)") @RequestParam String semester,
//         @Parameter(description = "년도") @RequestParam int year) {
//     return ResponseEntity.ok(gradeService.findAllByCourseCodeAndSemesterAndYear(courseCode, semester, year));
// }

    @Operation(summary = "성적 일괄 수정", description = "여러 학생의 성적을 한 번에 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @PutMapping("/scores/batch")
    public ResponseEntity<List<GradeDto>> updateMultipleGradeScores(
            @Parameter(description = "성적 수정 요청 목록") @RequestBody List<GradeUpdateRequest> updateRequests) {
        return ResponseEntity.ok(gradeService.updateMultipleGradeScores(updateRequests));
    }

    @Operation(summary = "학점 계산", description = "성적을 기반으로 학점을 계산합니다.")
    @ApiResponse(responseCode = "200", description = "계산 성공")
    @ApiResponse(responseCode = "404", description = "성적 정보를 찾을 수 없음")
    @GetMapping("/gpa/{scoreId}")
    public ResponseEntity<GpaCalculationDto> calculateGpa(
            @Parameter(description = "점수 ID") @PathVariable UUID scoreId) {
        return ResponseEntity.ok(gradeService.calculateGpa(scoreId));
    }

    @Operation(summary = "성적 목록 조회", description = "강의 개설 ID로 성적 목록을 조회하고 정렬합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/list/{openingId}")
    public ResponseEntity<List<GradeListResponseDto>> getGradeList(
            @Parameter(description = "강의 개설 ID") @PathVariable UUID openingId,
            @Parameter(description = "정렬 기준 (기본값: totalScore)") @RequestParam(defaultValue = "totalScore") String sortBy) {
        return ResponseEntity.ok(gradeService.getGradeList(openingId, sortBy));
    }

    @Operation(summary = "성적 통계 조회", description = "강의별 성적 통계를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/statistics/{openingId}")
    public ResponseEntity<GradeStatisticsDto> getGradeStatistics(
            @Parameter(description = "강의 개설 ID") @PathVariable UUID openingId) {
        return ResponseEntity.ok(gradeService.getGradeStatistics(openingId));
    }

    @Operation(summary = "성적 공개 설정", description = "성적 공개 기간을 설정합니다.")
    @ApiResponse(responseCode = "200", description = "설정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @PutMapping("/visibility")
    public ResponseEntity<Void> updateGradeVisibility(
            @Parameter(description = "성적 공개 설정 정보") @RequestBody GradeVisibilityRequest request) {
        gradeService.updateGradeVisibility(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "성적 공개 여부 확인", description = "특정 성적의 공개 여부를 확인합니다.")
    @ApiResponse(responseCode = "200", description = "확인 성공")
    @GetMapping("/visibility/{gradeId}")
    public ResponseEntity<Boolean> checkGradeVisibility(
            @Parameter(description = "성적 ID") @PathVariable UUID gradeId) {
        return ResponseEntity.ok(gradeService.isGradeVisible(gradeId));
    }
}













