package com.example.namedmoment.controller;

import com.example.namedmoment.dto.EmotionRecordCreateRequest;
import com.example.namedmoment.dto.EmotionRecordResponse;
import com.example.namedmoment.dto.Result;
import com.example.namedmoment.service.EmotionRecordService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/emotions/records")
public class EmotionRecordController {

    @Resource
    private EmotionRecordService emotionRecordService;

    @PostMapping
    public Result<EmotionRecordResponse> save(
            @Valid @RequestBody EmotionRecordCreateRequest request) {
        return Result.success(emotionRecordService.save(request));
    }

    @GetMapping
    public Result<List<EmotionRecordResponse>> list() {
        return Result.success(emotionRecordService.list());
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Positive Long id) {
        emotionRecordService.delete(id);
        return Result.success(null);
    }
}
