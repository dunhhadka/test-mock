package com.example.demo.jwt;

import com.example.demo.entity.ForgotPasswordEntity;
import com.example.demo.repository.ForgotPasswordRepository;
import com.example.demo.schedule.ConfigSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

public class ConfigScheduleTest {

    private ForgotPasswordRepository forgotPasswordRepository;
    private ConfigSchedule configSchedule;

    @BeforeEach
    void setUp() {
        forgotPasswordRepository = mock(ForgotPasswordRepository.class);
        configSchedule = new ConfigSchedule(forgotPasswordRepository);
    }

    @Test
    void testPerformScan_deletesExpiredEntries() {
        // Arrange
        ForgotPasswordEntity entity1 = new ForgotPasswordEntity();
        ForgotPasswordEntity entity2 = new ForgotPasswordEntity();
        List<ForgotPasswordEntity> expiredList = Arrays.asList(entity1, entity2);

        when(forgotPasswordRepository.getListByExpiredTime()).thenReturn(expiredList);

        // Act
        configSchedule.performScan();

        // Assert
        verify(forgotPasswordRepository, times(1)).getListByExpiredTime();
        verify(forgotPasswordRepository, times(1)).deleteAll(expiredList);
    }

    @Test
    void testPerformScan_whenNoExpiredEntities() {
        // Arrange
        when(forgotPasswordRepository.getListByExpiredTime()).thenReturn(List.of());

        // Act
        configSchedule.performScan();

        // Assert
        verify(forgotPasswordRepository, times(1)).getListByExpiredTime();
        verify(forgotPasswordRepository, times(1)).deleteAll(List.of());
    }
}
