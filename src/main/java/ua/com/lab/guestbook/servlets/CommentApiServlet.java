package ua.com.lab.guestbook.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.com.lab.guestbook.db.Comment;
import ua.com.lab.guestbook.db.DatabaseService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class CommentApiServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(CommentApiServlet.class);

    private DatabaseService dbService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        this.dbService = DatabaseService.getInstance();
        this.objectMapper = new ObjectMapper();
        // Реєструємо модуль для коректної серіалізації Instant/Timestamp
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * GET /comments — JSON-список (останні зверху).
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<Comment> comments = dbService.getComments();
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            objectMapper.writeValue(resp.getWriter(), comments);

        } catch (SQLException e) {
            log.error("Failed to get comments from DB", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database failure");
        }
    }

    /**
     * POST /comments — додає запис; без тіла у відповіді.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String author = req.getParameter("author");
            String text = req.getParameter("text");

            // --- Валідація ---
            if (author == null || author.isBlank() || author.length() > 64) {
                log.warn("Validation failed: Invalid author '{}'", author);
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Author is required and must be <= 64 chars.");
                return;
            }
            if (text == null || text.isBlank() || text.length() > 1000) {
                log.warn("Validation failed: Invalid text (length: {})", text == null ? 0 : text.length());
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Text is required and must be <= 1000 chars.");
                return;
            }

            // --- Збереження в БД ---
            Comment newComment = dbService.addComment(author, text);

            // --- Логування (обов'язково) ---
            log.info("New comment added! id={}, author='{}', length={}",
                    newComment.id(), newComment.author(), newComment.text().length());

            // --- Успіх: 204 ---
            // Примітка: HTML форма очікує перенаправлення, але API вимагає 204.
            // При 204 користувач побачить "білу сторінку" і має повернутись назад
            // та оновити сторінку. Це відповідає вимогам ТЗ.
            // Якби ми хотіли кращий UX для HTML, ми б зробили redirect:
            // resp.sendRedirect(req.getContextPath() + "/");

            resp.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204

        } catch (SQLException e) {
            log.error("Failed to add comment to DB", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database failure");
        }
    }
}