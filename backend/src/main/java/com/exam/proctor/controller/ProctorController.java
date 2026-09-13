package com.exam.proctor.controller;

import com.exam.proctor.service.ProctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.Map;

@RestController
@RequestMapping("/api/proctor")
@CrossOrigin(origins = "*")
public class ProctorController {

    @Autowired
    private ProctorService proctorService;

    @PostMapping("/violation")
    public ResponseEntity<?> recordViolation(@NonNull @RequestBody Map<String, Object> body) {
        Long studentId = body.get("studentId") != null ? Long.parseLong(body.get("studentId").toString()) : 0L;
        Long examId = body.get("examId") != null ? Long.parseLong(body.get("examId").toString()) : 1L;
        Long sessionId = body.get("sessionId") != null ? Long.parseLong(body.get("sessionId").toString()) : studentId;
        String violationType = body.get("violationType") != null ? body.get("violationType").toString() : "UNKNOWN";

        try {
            proctorService.recordViolation(sessionId, studentId, examId, violationType);
        } catch (Exception e) {
            System.err.println("Proctor violation record error: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of(
                "status", "recorded",
                "violationType", violationType
        ));
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(@NonNull @RequestBody Map<String, Object> body) {
        Long studentId = body.get("studentId") != null ? Long.parseLong(body.get("studentId").toString()) : 0L;
        Long examId = body.get("examId") != null ? Long.parseLong(body.get("examId").toString()) : 1L;

        return ResponseEntity.ok(Map.of(
                "status", "alive",
                "studentId", studentId,
                "examId", examId,
                "timestamp", System.currentTimeMillis()
        ));
    }
}
