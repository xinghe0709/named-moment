package com.example.namedmoment.controller;

import com.example.namedmoment.dto.EmotionMatchRequest;
import com.example.namedmoment.dto.EmotionMatchResponse;
import com.example.namedmoment.dto.Result;
import com.example.namedmoment.service.EmotionMatchService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/emotions")
public class EmotionController {

    @Resource
    private EmotionMatchService emotionMatchService;

    @PostMapping("/match")
    public Result<EmotionMatchResponse> match(
            @Valid @RequestBody EmotionMatchRequest request) {
        return Result.success(emotionMatchService.match(request.getText().trim()));
    }
}
