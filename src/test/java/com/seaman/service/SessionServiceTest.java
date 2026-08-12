package com.seaman.service;

import com.seaman.entity.SessionEntity;
import com.seaman.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock SessionRepository sessionRepository;

    private SessionService service;

    @BeforeEach
    void setUp() {
        service = new SessionService(sessionRepository);
    }

    @Test
    void insertSessionDelegatesToRepository() {
        SessionEntity entity = new SessionEntity();
        when(sessionRepository.insert(entity)).thenReturn(true);

        assertTrue(service.insertSession(entity));
        verify(sessionRepository).insert(entity);
    }

    @Test
    void insertSessionReturnsFalseWhenRepositoryFails() {
        SessionEntity entity = new SessionEntity();
        when(sessionRepository.insert(entity)).thenReturn(false);

        assertFalse(service.insertSession(entity));
    }

    @Test
    void findByIdDelegatesToRepository() {
        SessionEntity entity = new SessionEntity();
        when(sessionRepository.findById("session-1")).thenReturn(entity);

        assertSame(entity, service.findById("session-1"));
    }

    @Test
    void updateStatusDelegatesToRepository() {
        SessionEntity entity = new SessionEntity();
        when(sessionRepository.updateStatus(entity)).thenReturn(true);

        assertTrue(service.updateStatus(entity));
        verify(sessionRepository).updateStatus(entity);
    }
}
