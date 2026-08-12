package com.seaman.service;

import com.seaman.entity.MessageCodeEntity;
import com.seaman.repository.MessageCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageCodeServiceTest {

    @Mock MessageCodeRepository messageCodeRepository;

    private MessageCodeService service;

    @BeforeEach
    void setUp() {
        service = new MessageCodeService(messageCodeRepository);
    }

    @Test
    void returnsNotMatchingMessageWhenCodeNotFound() {
        when(messageCodeRepository.findByCode("MA00000")).thenReturn(null);

        String result = service.getMessageDescription("MA00000", "EN");

        assertEquals("Message code : MA00000 is not matching in master.", result);
    }

    @Test
    void returnsEnglishDescriptionWhenLanguageIsEn() {
        MessageCodeEntity entity = new MessageCodeEntity();
        entity.setMessageDescriptionEn("Success");
        entity.setMessageDescriptionTh("สำเร็จ");
        when(messageCodeRepository.findByCode("MA00000")).thenReturn(entity);

        String result = service.getMessageDescription("MA00000", "EN");

        assertEquals("Success", result);
    }

    @Test
    void returnsThaiDescriptionWhenLanguageNotEn() {
        MessageCodeEntity entity = new MessageCodeEntity();
        entity.setMessageDescriptionEn("Success");
        entity.setMessageDescriptionTh("สำเร็จ");
        when(messageCodeRepository.findByCode("MA00000")).thenReturn(entity);

        String result = service.getMessageDescription("MA00000", "TH");

        assertEquals("สำเร็จ", result);
    }

    @Test
    void returnsNotMatchingMessageWhenFoundDescriptionIsEmpty() {
        MessageCodeEntity entity = new MessageCodeEntity();
        entity.setMessageDescriptionEn("");
        entity.setMessageDescriptionTh("");
        when(messageCodeRepository.findByCode("MA00000")).thenReturn(entity);

        String result = service.getMessageDescription("MA00000", "EN");

        assertEquals("Message code : MA00000 is not matching in master.", result);
    }
}
