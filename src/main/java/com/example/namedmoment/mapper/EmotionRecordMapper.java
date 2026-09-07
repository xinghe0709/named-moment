package com.example.namedmoment.mapper;

import com.example.namedmoment.entity.EmotionRecord;
import org.apache.ibatis.annotations.Param;

public interface EmotionRecordMapper {

    int insert(EmotionRecord record);

    int deleteById(@Param("id") Long id);
}
