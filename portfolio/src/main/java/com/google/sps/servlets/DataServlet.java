// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;


import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {


  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    ArrayList<String> messageList = new ArrayList<String>();

    for (Entity entity : results.asIterable()) {
      String message = (String) entity.getProperty("message");
      messageList.add(message);
    }

    String json = convertToJsonUsingGson(messageList);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String message = getRequestMessage(request);
    long timestamp = System.currentTimeMillis();
    UserService userService = UserServiceFactory.getUserService();

    /* If user is not logged in, then no comment is added. Instead, the user
     * is sent to a login screen.
     */
    if (!userService.isUserLoggedIn()) {
        response.sendRedirect("/login");
        return;
    }

    if (message != null && !message.isEmpty()){
      Entity commentEntity = new Entity("Comment");
      String email = userService.getCurrentUser().getEmail();
      commentEntity.setProperty("message", message);
      commentEntity.setProperty("email", email);
      commentEntity.setProperty("timestamp", timestamp);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(commentEntity);
    }

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  private String convertToJsonUsingGson(ArrayList<String> list) {
    Gson gson = new Gson();
    Type typeOfList = new TypeToken<List<String>>(){}.getType();
    String json = gson.toJson(list, typeOfList);
    return json;
  }

  private String getRequestMessage(HttpServletRequest request){
    String messageString = request.getParameter("comment");
    return messageString;
  }
}