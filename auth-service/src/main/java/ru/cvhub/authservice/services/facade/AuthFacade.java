package ru.cvhub.authservice.services.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.cvhub.authservice.grpc.AS;
import ru.cvhub.authservice.services.UserService;
import ru.cvhub.authservice.util.mapping.Mapper;

@Service
@RequiredArgsConstructor
public class AuthFacade {
    private final UserService userService;

    public AS.TokenResponse register(AS.RegisterRequest request) {
        AS.TokenResponse response = Mapper.toResponse(
                userService.createUser(Mapper.toDto(request))
        );

        /*
         * generate and return token
         * mfa etc.
         */
        return response;
    }
}
