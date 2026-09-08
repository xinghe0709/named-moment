package com.example.namedmoment.service;

import com.example.namedmoment.dto.EmotionRecordCreateRequest;
import com.example.namedmoment.dto.EmotionRecordResponse;
import com.example.namedmoment.entity.EmotionConcept;
import com.example.namedmoment.entity.EmotionRecord;
import com.example.namedmoment.enums.ErrorCode;
import com.example.namedmoment.exception.BusinessException;
import com.example.namedmoment.mapper.EmotionConceptMapper;
import com.example.namedmoment.mapper.EmotionRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmotionRecordServiceTest {

    @Mock
    private EmotionConceptMapper emotionConceptMapper;

    @Mock
    private EmotionRecordMapper emotionRecordMapper;

    private EmotionRecordService service;

    @BeforeEach
    void setUp() {
        service = new EmotionRecordService();
        ReflectionTestUtils.setField(service, "emotionConceptMapper", emotionConceptMapper);
        ReflectionTestUtils.setField(service, "emotionRecordMapper", emotionRecordMapper);
    }

    @Test
    void shouldRejectUnknownConcept() {
        when(emotionConceptMapper.selectById(42L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(requestFor(42L)));

        assertEquals(ErrorCode.PARAM_ERROR, exception.getErrorCode());
        verify(emotionRecordMapper, never()).insert(any(EmotionRecord.class));
    }

    @Test
    void shouldSaveSelectedConcept() {
        EmotionConcept concept = concept(3L, "Sehnsucht");
        when(emotionConceptMapper.selectById(3L)).thenReturn(concept);
        when(emotionRecordMapper.insert(any(EmotionRecord.class))).thenAnswer(invocation -> {
            EmotionRecord record = invocation.getArgument(0);
            record.setId(9L);
            return 1;
        });

        EmotionRecordResponse response = service.save(requestFor(3L));

        ArgumentCaptor<EmotionRecord> captor = ArgumentCaptor.forClass(EmotionRecord.class);
        verify(emotionRecordMapper).insert(captor.capture());
        assertEquals("一段足够长的情感文字", captor.getValue().getInputText());
        assertEquals(3L, captor.getValue().getConceptId());
        assertEquals(9L, response.getId());
        assertEquals("Sehnsucht", response.getName());
    }

    @Test
    void shouldReturnMapperArchiveOrder() {
        List<EmotionRecordResponse> expected = Arrays.asList(
                EmotionRecordResponse.builder().id(2L).build(),
                EmotionRecordResponse.builder().id(1L).build());
        when(emotionRecordMapper.selectAllResponses()).thenReturn(expected);

        List<EmotionRecordResponse> actual = service.list();

        assertEquals(Arrays.asList(2L, 1L),
                Arrays.asList(actual.get(0).getId(), actual.get(1).getId()));
    }

    @Test
    void shouldDeleteExistingRecord() {
        when(emotionRecordMapper.deleteById(7L)).thenReturn(1);

        service.delete(7L);

        verify(emotionRecordMapper).deleteById(7L);
    }

    @Test
    void shouldRejectDeletingMissingRecord() {
        when(emotionRecordMapper.deleteById(7L)).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.delete(7L));

        assertEquals(ErrorCode.RECORD_NOT_FOUND, exception.getErrorCode());
    }

    private EmotionRecordCreateRequest requestFor(Long conceptId) {
        return EmotionRecordCreateRequest.builder()
                .inputText("一段足够长的情感文字")
                .conceptId(conceptId)
                .matchScore(90)
                .explanation("这个概念最接近当时的感受")
                .build();
    }

    private EmotionConcept concept(Long id, String name) {
        return EmotionConcept.builder()
                .id(id)
                .name(name)
                .language("德语")
                .meaning("对遥远之物的深切渴望")
                .description("渴望与无法抵达同时存在")
                .sourceUrl("https://example.test/source")
                .createdAt(OffsetDateTime.now())
                .build();
    }
}
