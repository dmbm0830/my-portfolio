package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that displays login and logout information, and prints out comments if logged in */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();

    /** Prints out all comments with email of commenter only if user is logged in */
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      out.println("<ul>");
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
      PreparedQuery results = datastore.prepare(query);
      for (Entity entity : results.asIterable()) {
        String message = (String) entity.getProperty("message");
        String email = (String) entity.getProperty("email");
        out.println("<li>" + email + ": " + message + "</li>");
      }
      out.println("</ul>");
      out.println("");
      String logoutUrl = userService.createLogoutURL("/index.html");
      response.getWriter().println("<p>Logout <a href=\"" + logoutUrl + "\">here</a>.</p>");
    } else {
      /** Otherwise, the user is prompted to login */
      String loginUrl = userService.createLoginURL("/index.html");
      response.getWriter().println("<p>Login <a href=\"" + loginUrl + "\">here</a>.</p>");
    }
  }
}
