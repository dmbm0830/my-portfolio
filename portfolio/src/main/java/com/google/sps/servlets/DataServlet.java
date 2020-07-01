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

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.lang.reflect.Type;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private final ArrayList<String> messageList = new ArrayList<String>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String json = convertToJsonUsingGson(messageList);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // If the user sends another POST request after the game is over, then start a new game.
    String message = getRequestMessage(request);
    if (message != null && !message.isEmpty()){
      messageList.add(message);
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
