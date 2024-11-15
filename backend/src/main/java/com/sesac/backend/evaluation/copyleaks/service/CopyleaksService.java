package com.sesac.backend.evaluation.copyleaks.service;

import com.sesac.backend.evaluation.assignment.domain.Assignment;
import com.sesac.backend.evaluation.assignment.repository.AssignmentRepository;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CopyleaksService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private CopyleaksAuthService authService;

    @Autowired
    private CopyleaksScanService scanService;

    public void checkPlagiarism(UUID assignId) throws IOException {
        // 1. 파일 데이터 가져오기
        Assignment assignment = assignmentRepository.findById(assignId).orElseThrow(RuntimeException::new);
        byte[] fileData = assignment.getFile();
        String fileName = assignment.getFileName();

        // 2. Copyleaks 인증 토큰 획득
        String accessToken = authService.getAccessToken();

        // 3. 스캔 ID 생성
        UUID scanId = UUID.randomUUID();
        assignment.setScanId(scanId);
        assignmentRepository.save(assignment);

        // 4. 파일 제출 및 표절 검사 요청
        scanService.submitFileForScan(accessToken, scanId, fileData, fileName);

        // 5. 결과는 웹훅을 통해 수신됨
        log.info("Scan submitted. Scan ID: {}", scanId);
    }
}
