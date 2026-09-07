package com.example.namedmoment.mapper;

import com.example.namedmoment.dto.EmotionRecordResponse;
import com.example.namedmoment.entity.EmotionRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EmotionRecordMapper {

    int insert(EmotionRecord record);

    List<EmotionRecordResponse> selectAllResponses();

    int deleteById(@Param("id") Long id);
}
