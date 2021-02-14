/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.armatiek.xslweb.saxon.debug;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import info.macias.sse.servlet3.ServletEventTarget;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;

@WebServlet(asyncSupported = true)
public class DebugSSEServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!Context.getInstance().getDebugEnable()) {
      super.doGet(req, resp);
      return;
    }
    HttpSession session = req.getSession(false);
    if (session == null) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No active session");
      return;
    }
    ServletEventTarget eventTarget = new ServletEventTarget(req).ok().open();
    DebugClient debugClient = (DebugClient) session.getAttribute(Definitions.ATTRNAME_DEBUGCLIENT);
    if (debugClient == null) {
      debugClient = new DebugClient(req);
      session.setAttribute(Definitions.ATTRNAME_DEBUGCLIENT, debugClient);  
    }
    debugClient.setServletEventTarget(eventTarget);
  }
  
} 