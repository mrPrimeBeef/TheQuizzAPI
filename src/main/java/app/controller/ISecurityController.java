package app.controller;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public interface ISecurityController {
    void login(Context ctx); // to get a token
    void register(Context ctx); // to get a user
    void verify(Context ctx); // to verify a token
    void accessHandler(Context ctx); // to check if a user has access to a route
    void healthCheck(Context ctx);
}
