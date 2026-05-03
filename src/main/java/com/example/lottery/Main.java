package com.example.lottery;

import com.example.lottery.config.DatabaseConfig;
import com.example.lottery.controller.AuthController;
import com.example.lottery.controller.DrawController;
import com.example.lottery.controller.TicketController;
import com.example.lottery.security.JwtAuthHandler;
import com.example.lottery.service.AuthService;
import com.example.lottery.service.DrawService;
import com.example.lottery.service.TicketService;
import com.example.lottery.util.JwtUtil;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws Exception {
        Properties props = loadProperties();
        int port = Integer.parseInt(props.getProperty("server.port", "8080"));

        DataSource dataSource = DatabaseConfig.getDataSource();

        String jwtSecret = System.getenv().getOrDefault("JWT_SECRET",
                props.getProperty("jwt.secret"));
        long jwtExpiration = Long.parseLong(props.getProperty("jwt.expiration.ms"));
        JwtUtil jwtUtil = new JwtUtil(jwtSecret, jwtExpiration);

        AuthService authService = new AuthService(dataSource, jwtUtil);
        DrawService drawService = new DrawService(dataSource);
        TicketService ticketService = new TicketService(dataSource);

        AuthController authController = new AuthController(authService);
        DrawController drawController = new DrawController(drawService);
        TicketController ticketController = new TicketController(ticketService);

        Server server = new Server(port);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setWelcomeFiles(new String[]{"swagger.html"});

        URL staticUrl = Main.class.getClassLoader().getResource("static");
        if (staticUrl == null) {
            throw new RuntimeException("Static resources not found");
        }
        resourceHandler.setResourceBase(staticUrl.toExternalForm());

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{
                resourceHandler,
                authController,
                drawController,
                ticketController,
                new DefaultHandler()
        });

        JwtAuthHandler jwtHandler = new JwtAuthHandler(jwtUtil);
        jwtHandler.setHandler(handlers);

        server.setHandler(jwtHandler);
        server.start();
        System.out.println("Server started on port " + port);
        server.join();
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = Main.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
        return props;
    }
}