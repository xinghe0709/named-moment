package com.example.namedmoment.service;

import com.example.namedmoment.dto.EmotionRecordCreateRequest;
import com.example.namedmoment.dto.EmotionRecordResponse;
import com.example.namedmoment.entity.EmotionConcept;
import com.example.namedmoment.entity.EmotionRecord;
import com.example.namedmoment.enums.ErrorCode;
import com.example.namedmoment.exception.BusinessException;
import com.example.namedmoment.mapper.EmotionConceptMapper;
import com.example.namedmoment.mapper.EmotionRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class EmotionRecordService {

    @Resource
    private EmotionConceptMapper emotionConceptMapper;

    @Resource
    private EmotionRecordMapper emotionRecordMapper;

    @Transactional
    public EmotionRecordResponse save(Long userId, EmotionRecordCreateRequest request) {
        EmotionConcept concept = emotionConceptMapper.selectById(request.getConceptId());
        if (concept == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        OffsetDateTime createdAt = OffsetDateTime.now();
        EmotionRecord record = EmotionRecord.builder()
                .userId(userId)
                .inputText(request.getInputText().trim())
                .conceptId(request.getConceptId())
                .matchScore(request.getMatchScore())
                .explanation(request.getExplanation().trim())
                .createdAt(createdAt)
                .build();
        emotionRecordMapper.insert(record);

        return EmotionRecordResponse.builder()
                .id(record.getId())
                .inputText(record.getInputText())
                .conceptId(concept.getId())
                .name(concept.getName())
                .language(concept.getLanguage())
                .meaning(concept.getMeaning())
                .description(concept.getDescription())
                .sourceUrl(concept.getSourceUrl())
                .matchScore(record.getMatchScore())
                .explanation(record.getExplanation())
                .createdAt(createdAt)
                .build();
    }

    public List<EmotionRecordResponse> list(Long userId) {
        return emotionRecordMapper.selectAllResponses(userId);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        if (emotionRecordMapper.deleteById(userId, id) != 1) {
            throw new BusinessException(ErrorCode.RECORD_NOT_FOUND);
        }
    }
}
