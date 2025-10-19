package ru.cvhub.authservice.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.cvhub.authservice.security.JwtUtil;
import ru.cvhub.authservice.services.dto.TokenDto;
import ru.cvhub.authservice.store.entity.Session;
import ru.cvhub.authservice.store.entity.User;
import ru.cvhub.authservice.store.repository.SessionRepository;
import ru.cvhub.authservice.store.repository.UserRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {
    private static final UUID TEST_USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final String TEST_EMAIL = "tester@domain.com";
    private static final String TEST_PASSWORD = "realLy_Str0ng#password(no...)";
    private static final String VALID_ACCESS_TOKEN = "valid_access_token";
    private static final String VALID_REFRESH_TOKEN = "123e4567-e89b-12d3-a456-426614174001";

    private final User user = new User(
            TEST_USER_ID,
            TEST_EMAIL,
            TEST_PASSWORD,
            Instant.now(),
            Instant.now(),
            true
    );
    private final TokenDto tokenDto = new TokenDto(
            VALID_ACCESS_TOKEN,
            VALID_REFRESH_TOKEN
    );
    private final Session session = new Session(
            UUID.fromString(VALID_REFRESH_TOKEN),
            TEST_USER_ID,
            Instant.now(),
            Instant.MAX,
            true
    );


    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SessionServiceImpl sessionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sessionService, "sessionTtl", Duration.ofHours(1));
        when(
                jwtUtil.generateToken(new JwtUtil.JwtUserDetails(
                        TEST_USER_ID, TEST_EMAIL, false
                ))
        ).thenReturn(VALID_ACCESS_TOKEN);
    }

    @Test
    void createSessionToken_withActiveSession() {
        when(sessionRepository.findByUserId(TEST_USER_ID)).thenReturn(List.of(session));

        assertThat(sessionService.createSessionToken(user)).isEqualTo(tokenDto);
    }

    @Test
    void createSessionToken_noActiveSession() {
        when(sessionRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.emptyList());
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> {
            Session newSession = invocation.getArgument(0);
            newSession.refreshToken(UUID.fromString(VALID_REFRESH_TOKEN));
            return newSession;
        });

        assertThat(sessionService.createSessionToken(user)).isEqualTo(tokenDto);
    }

    @Test
    void refreshSessionToken() {
        when(sessionRepository.findByRefreshToken(UUID.fromString(VALID_REFRESH_TOKEN)))
                .thenReturn(Optional.of(session));
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));

        assertThat(sessionService.refreshSessionToken(tokenDto)).isEqualTo(tokenDto);
    }
}