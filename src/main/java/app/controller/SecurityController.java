package app.controller;

import app.config.HibernateConfig;
import app.daos.ISecurityDAO;
import app.daos.SecurityDAO;
import app.entities.User;
import app.entities.enums.Role;
import app.exceptions.ApiException;
import app.exceptions.DaoException;
import app.exceptions.NotAuthorizedException;
import app.exceptions.ValidationException;
import app.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.bugelhartmann.ITokenSecurity;
import dk.bugelhartmann.TokenSecurity;
import dk.bugelhartmann.TokenVerificationException;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.*;
import io.javalin.security.RouteRole;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Set;

public class SecurityController implements ISecurityController {
    private final ITokenSecurity tokenSecurity = new TokenSecurity();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ISecurityDAO securityDAO;

    private final Logger logger = LoggerFactory.getLogger(SecurityController.class);

    public SecurityController(EntityManagerFactory emf) {
        this.securityDAO = SecurityDAO.getInstance(emf);
    }
    public SecurityController()
    {
        this.securityDAO = SecurityDAO.getInstance(HibernateConfig.getEntityManagerFactory());
    }

    @Override
    public void login(Context ctx) {
        ObjectNode returnJson = objectMapper.createObjectNode();
        try {
            UserDTO userInput = ctx.bodyAsClass(UserDTO.class);
            UserDTO verifiedUser = securityDAO.getVerifiedUser(userInput.getUsername(), userInput.getPassword());
            String token = createToken(verifiedUser);
            returnJson.put("token", token)
                    .put("username", verifiedUser.getUsername());

            ctx.status(HttpStatus.OK).json(returnJson);
        } catch (EntityNotFoundException | DaoException | ValidationException e) {
            logger.error("Error logging in user", e);
            throw new ApiException(401, "Could not verify user", e);
        }
    }

    @Override
    public void register(Context ctx) {
        ObjectNode returnJson = objectMapper.createObjectNode();
        try {
            UserDTO userInput = ctx.bodyAsClass(UserDTO.class);
            User user = new User(userInput.getUsername(), userInput.getPassword());

            User createdUserAccount = securityDAO.createUser(user);
            String token = createToken(new UserDTO(createdUserAccount.getUsername(), Set.of("USER")));
            returnJson.put("token", token)
                    .put("username", createdUserAccount.getUsername());

            ctx.status(HttpStatus.CREATED).json(returnJson);
        } catch (EntityExistsException e) {
            logger.error("Error registering user", e);
            //throw new APIException(422, "Could not register user: User already exists", e);
            ctx.status(HttpStatus.UNPROCESSABLE_CONTENT).json("User already exists " + e.getMessage());
        }
    }

    public void accessHandler(Context ctx) {
        // This is a preflight request => no need for authentication
        if (ctx.method().toString().equals("OPTIONS")) {
            ctx.status(200);
            return;
        }

        // 1. Check if endpoint is open to all
        // If the endpoint is not protected with roles or is open to ANYONE role, then skip
        Set<RouteRole> permittedRoles = ctx.routeRoles();
        if (permittedRoles.isEmpty() || permittedRoles.contains(Role.ANYONE)) {
            return;
        }

        // Check that token is present and not malformed, and get the User from the token
        UserDTO verifiedTokenUser = getUserFromToken(ctx);
        if (verifiedTokenUser == null) {
            throw new UnauthorizedResponse("Invalid user or token"); // UnauthorizedResponse is javalin 6 specific but response is not json!
//            throw new app.exceptions.APIException(401, "Invalid user or token");
        }
        ctx.attribute("user", verifiedTokenUser);

        if (!userHasAllowedRole(verifiedTokenUser, permittedRoles)) {
            throw new ForbiddenResponse("User does not have the required role to access this endpoint");
            // throw new APIException(403, "User does not have the required role to access this endpoint");
        }
    }

    private static boolean userHasAllowedRole(UserDTO user, Set<RouteRole> allowedRoles) {
        return user.getRoles().stream()
                .map(Role::valueOf)
                .anyMatch(allowedRoles::contains);
    }

    private UserDTO getUserFromToken(Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null) {
            throw new UnauthorizedResponse("Authorization header is missing");
        }
        String token = header.split(" ")[1];
        if (token == null) {
            throw new UnauthorizedResponse("Authorization header is malformed");
        }
        return verifyToken(token);
    }

    private String createToken(UserDTO user) throws ApiException {
        try {
            String ISSUER;
            String TOKEN_EXPIRE_TIME;
            String SECRET_KEY;

            if (System.getenv("DEPLOYED") != null) {
                ISSUER = System.getenv("ISSUER");
                TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
                SECRET_KEY = System.getenv("SECRET_KEY");
            } else {
                ISSUER = Utils.getPropertyValue("ISSUER", "config.properties");
                TOKEN_EXPIRE_TIME = Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties");
                SECRET_KEY = Utils.getPropertyValue("SECRET_KEY", "config.properties");
            }
            return tokenSecurity.createToken(user, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(500, "Could not create token");
        }
    }

    @Override
    public void verify(Context ctx) {
        ObjectNode returnJson = objectMapper.createObjectNode();
        UserDTO verifiedTokenUser = getUserFromToken(ctx);
        if (verifiedTokenUser == null) {
            throw new UnauthorizedResponse("Invalid user or token");
        }
        returnJson.put("msg", "Token is valid");
        ctx.status(HttpStatus.OK).json(returnJson);
    }

    private UserDTO verifyToken(String token) throws ApiException {
        boolean IS_DEPLOYED = (System.getenv("DEPLOYED") != null);
        String SECRET = IS_DEPLOYED ? System.getenv("SECRET_KEY") : Utils.getPropertyValue("SECRET_KEY", "config.properties");

        try {
            if (tokenSecurity.tokenIsValid(token, SECRET) && tokenSecurity.tokenNotExpired(token)) {
                return tokenSecurity.getUserWithRolesFromToken(token);
            } else {
                throw new NotAuthorizedException(403, "Token is not valid");
            }
        } catch (ParseException | NotAuthorizedException | TokenVerificationException e) {
            e.printStackTrace();
            throw new ApiException(HttpStatus.UNAUTHORIZED.getCode(), "Unauthorized. Could not verify token");
        }
    }

    public void healthCheck(@NotNull Context ctx) {
        ctx.status(200).json("{\"msg\": \"API is up and running\"}");
    }
}