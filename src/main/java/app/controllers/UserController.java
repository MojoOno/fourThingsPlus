package app.controllers;

import app.entities.Task;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.TaskMapper;
import app.persistence.UserMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.List;

public class UserController
{

    private static final String CREATEUSER_HTML = "createuser.html";
    private static final String INDEX_HTML = "index.html";
    private static final String MESSAGE = "message";

    public static void addRoutes(Javalin app, ConnectionPool connectionPool)
    {
        app.post("/login", ctx -> login(ctx, connectionPool));
        app.get("/logout", ctx -> logout(ctx));
        app.get("/createuser", ctx -> ctx.render(CREATEUSER_HTML));
        app.post("/createuser", ctx -> createUser(ctx, connectionPool));
        app.get("/task", ctx -> getTasks(ctx, connectionPool));

    }

    private static void getTasks(Context ctx, ConnectionPool connectionPool)
    {
        // Get the current user from the session
        User currentUser = ctx.sessionAttribute("currentUser");

        try {
            // Retrieve tasks for the logged-in user
            List<Task> taskList = TaskMapper.getAllTasksPerUser(currentUser.getUserId(), connectionPool);
            ctx.attribute("taskList", taskList);
            ctx.render("task.html");
        } catch (DatabaseException e) {
            // If any database exception occurs, show error and redirect to the login page
            ctx.attribute(MESSAGE, "Unable to load tasks: " + e.getMessage());
            ctx.render(INDEX_HTML);
        }
    }

    private static void createUser(Context ctx, ConnectionPool connectionPool)
    {
        //Hent form parametre
        String username = ctx.formParam("username");
        String password1 = ctx.formParam("password1");
        String password2 = ctx.formParam("password2");

        if(password1.equals(password2)){
            try {
                UserMapper.createuser(username, password1, connectionPool);
                ctx.attribute(MESSAGE, "Account with username: " + username + " created successfully");
                ctx.render(INDEX_HTML);

            } catch (DatabaseException e) {
                ctx.attribute(MESSAGE, "The username already exists. Try again, or log in");
                ctx.render(CREATEUSER_HTML);
            }

        }else {
            ctx.attribute(MESSAGE, "Passwords do not match - please try again");
            ctx.render(CREATEUSER_HTML);
        }

    }

    private static void logout(Context ctx)
    {
        ctx.req().getSession().invalidate();
        ctx.redirect("/");
    }

    public static void login(Context ctx, ConnectionPool connectionPool)
    {
        //Hent form parametre
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");


        //Check om bruger findes i DB med de angivne username + password
        try {
            User user = UserMapper.login(username, password, connectionPool);
            ctx.sessionAttribute("currentUser", user);

            //Hvis ja, Send videre til task side
            List<Task> taskList = TaskMapper.getAllTasksPerUser(user.getUserId(), connectionPool);
            ctx.attribute("taskList", taskList);
            ctx.render("task.html");

        } catch (DatabaseException e) {
            //Hvis nej, send tilbage til login side med fejl kode
            ctx.attribute(MESSAGE, e.getMessage());
            ctx.render(INDEX_HTML);
        }
    }
}
