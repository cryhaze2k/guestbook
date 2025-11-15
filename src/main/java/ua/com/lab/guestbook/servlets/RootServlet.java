package ua.com.lab.guestbook.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ua.com.lab.guestbook.db.Comment;
import ua.com.lab.guestbook.db.DatabaseService;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

public class RootServlet extends HttpServlet {

    private DatabaseService dbService;

    @Override
    public void init() throws ServletException {
        this.dbService = DatabaseService.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        try (PrintWriter out = resp.getWriter()) {
            out.println("<html>");
            out.println("<head><title>Guestbook</title>");
            out.println("<style>");
            out.println("body { font-family: sans-serif; margin: 2em; }");
            out.println("form { margin-bottom: 2em; padding: 1em; border: 1px solid #ccc; border-radius: 5px; }");
            out.println("div { margin-bottom: 0.5em; }");
            out.println("label { display: inline-block; width: 60px; }");
            out.println("input[type='text'], textarea { width: 300px; padding: 5px; }");
            out.println("textarea { height: 100px; }");
            out.println(".comment { border: 1px solid #eee; padding: 1em; margin-bottom: 1em; border-radius: 5px; }");
            out.println(".comment strong { display: block; }");
            out.println(".comment span { color: #888; font-size: 0.9em; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");

            // --- 1. Форма ---
            // Форма відправляє POST-запит на "comments" (який обробить інший сервлет)
            out.println("<h1>Guestbook</h1>");
            out.println("<form method='POST' action='comments'>");
            out.println("<div><label for='author'>Author:</label>");
            out.println("<input type='text' id='author' name='author' maxlength='64'></div>");
            out.println("<div><label for='text'>Text:</label>");
            out.println("<textarea id='text' name='text' maxlength='1000'></textarea></div>");
            out.println("<div><button type='submit'>Submit</button></div>");
            out.println("</form>");

            // --- 2. Список відгуків ---
            out.println("<h2>Comments</h2>");
            List<Comment> comments = dbService.getComments();
            if (comments.isEmpty()) {
                out.println("<p>No comments yet. Be the first!</p>");
            } else {
                for (Comment c : comments) {
                    out.println("<div class='comment'>");
                    out.println("<strong>" + htmlEscape(c.author()) + "</strong>");
                    out.println("<span>" + c.createdAt() + "</span>");
                    out.println("<p>" + htmlEscape(c.text()).replace("\n", "<br>") + "</p>");
                    out.println("</div>");
                }
            }

            out.println("</body></html>");

        } catch (SQLException e) {
            // Помилка 500 (збій БД)
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve comments from database");
        }
    }

    // Простий хелпер для уникнення XSS
    private String htmlEscape(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}