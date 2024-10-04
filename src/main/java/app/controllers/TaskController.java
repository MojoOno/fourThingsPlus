package app.controllers;

import app.entities.Task;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.TaskMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

public class TaskController
{
    public static final String INDEX_HTML = "index.html";
    public static final String TASK_HTML = "task.html";
    public static final String MESSAGE = "message";

    public static void addRoutes(Javalin app, ConnectionPool connectionPool)
    {
        app.post("addtask", ctx -> addtask(ctx, connectionPool));
        app.post("done", ctx -> done(ctx, true, connectionPool));
        app.post("undo", ctx -> done(ctx, false, connectionPool));
        app.post("deletetask", ctx -> deletetask(ctx, connectionPool));
        app.post("edittask", ctx -> edittask(ctx, connectionPool));
        app.post("updatetask", ctx -> updatetask(ctx, connectionPool));
    }

    private static void updatetask(Context ctx, ConnectionPool connectionPool)
    {
        User user = ctx.sessionAttribute("currentUser");
        try {
            int taskId = Integer.parseInt(ctx.formParam("taskId"));
            String taskName = ctx.formParam("taskname");
            TaskMapper.update(taskId, taskName, connectionPool);
            List<Task> taskList = TaskMapper.getAllTasksPerUser(user.getUserId(), connectionPool);
            ctx.attribute("taskList", taskList);
            ctx.render(TASK_HTML);


        } catch (DatabaseException | NumberFormatException e) {
            ctx.attribute(MESSAGE, e.getMessage());
            ctx.render(INDEX_HTML);
        }
    }

    private static void edittask(Context ctx, ConnectionPool connectionPool)
    {

        try {
            int taskId = Integer.parseInt(ctx.formParam("taskId"));
            Task task = TaskMapper.getTaskById(taskId, connectionPool);
            ctx.attribute("task", task);
            ctx.render("edittask.html");

        } catch (DatabaseException | NumberFormatException e) {
            ctx.attribute(MESSAGE, e.getMessage());
            ctx.render(INDEX_HTML);
        }
    }

    private static void deletetask(Context ctx, ConnectionPool connectionPool)
    {
        User user = ctx.sessionAttribute("currentUser");
        try {
            int taskId = Integer.parseInt(ctx.formParam("taskId"));
            TaskMapper.delete(taskId, connectionPool);
            List<Task> taskList = TaskMapper.getAllTasksPerUser(user.getUserId(), connectionPool);
            ctx.attribute("taskList", taskList);
            ctx.render(TASK_HTML);

        } catch (DatabaseException | NumberFormatException e) {
            ctx.attribute(MESSAGE, e.getMessage());
            ctx.render(INDEX_HTML);
        }
    }

    private static void done(Context ctx, boolean done, ConnectionPool connectionPool)
    {
        User user = ctx.sessionAttribute("currentUser");
        try {
            int taskId = Integer.parseInt(ctx.formParam("taskId"));
            TaskMapper.setDoneTo(done, taskId, connectionPool);
            List<Task> taskList = TaskMapper.getAllTasksPerUser(user.getUserId(), connectionPool);
            ctx.attribute("taskList", taskList);
            ctx.render(TASK_HTML);

        } catch (DatabaseException | NumberFormatException e) {
            ctx.attribute(MESSAGE, e.getMessage());
            ctx.render(INDEX_HTML);
        }
    }

    private static void addtask(Context ctx, ConnectionPool connectionPool)
    {
        String taskName = ctx.formParam("taskname");

        User user = ctx.sessionAttribute("currentUser");
        try {
            if (taskName.length() >= 3) {

                Task newTask = TaskMapper.addTask(user, taskName, connectionPool);
            } else {
                ctx.attribute(MESSAGE, "A task must be longer than 3 characters");
            }
            List<Task> taskList = TaskMapper.getAllTasksPerUser(user.getUserId(), connectionPool);
            ctx.attribute("taskList", taskList);
            ctx.render(TASK_HTML);

        } catch (DatabaseException e) {
            ctx.attribute(MESSAGE, "Something went wrong - try again.");
            ctx.render(TASK_HTML);
        }
    }
}

