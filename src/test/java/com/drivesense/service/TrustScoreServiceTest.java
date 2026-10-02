package com.drivesense.service;

import com.drivesense.entity.User;
import com.drivesense.repository.TrustEventRepository;
import com.drivesense.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

public class TrustScoreServiceTest {

    private UserRepository userRepository;
    private TrustEventRepository trustEventRepository;
    private TrustScoreService trustScoreService;

    @BeforeEach
    public void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        trustEventRepository = Mockito.mock(TrustEventRepository.class);
        trustScoreService = new TrustScoreService(userRepository, trustEventRepository);
    }

    @Test
    @DisplayName("Trust score adjusts correctly and clamps to upper bound 100")
    public void testScoreUpperClamping() {
        User user = new User("Test User", "test@test.com", "123", "pw", "DL1", LocalDate.of(1995, 1, 1), "ROLE_CUSTOMER");
        user.setTrustScore(98);

        trustScoreService.adjustScore(user, 10, "Great rental");

        assertEquals(100, user.getTrustScore(), "Score should not exceed 100");
        verify(userRepository).save(user);
        verify(trustEventRepository).save(any());
    }

    @Test
    @DisplayName("Trust score clamps to lower bound 0")
    public void testScoreLowerClamping() {
        User user = new User("Test User", "test@test.com", "123", "pw", "DL1", LocalDate.of(1995, 1, 1), "ROLE_CUSTOMER");
        user.setTrustScore(5);

        trustScoreService.adjustScore(user, -15, "Damage reported");

        assertEquals(0, user.getTrustScore(), "Score should not drop below 0");
        verify(userRepository).save(user);
    }
}
