package ru.cvhub.authservice.services.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.cvhub.authservice.grpc.AS;
import ru.cvhub.authservice.services.UserService;
import ru.cvhub.authservice.util.exception.PreconditionException;

@Service
@RequiredArgsConstructor
public class AuthFacade {
    private final UserService userService;

    public AS.TokenResponse register(AS.RegisterRequest request) {
        AS.TokenResponse token = userService.registerUser(request);


        /*
         * generate and return token
         * mfa etc.
         */
        return null;
    }
}
