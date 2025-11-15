package ua.com.lab.guestbook.listeners;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.com.lab.guestbook.db.DatabaseService;

@WebListener
public class DatabaseContextListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(DatabaseContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Application starting up...");
        try {
            DatabaseService.getInstance().initializeDatabase();
            log.info("Database initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize database!", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Application shutting down...");
    }
}