// Copyright 2008 Google Inc. All Rights Reserved.
package com.google.appengine.helloorm;

import javax.jdo.PersistenceManager;
import javax.persistence.EntityManager;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class GetFlights extends HttpServlet {

  private static final String DEFAULT_QUERY = "select from " + Flight.class.getName();

  private ServletConfig config;
  @Override
  public void init(ServletConfig config) {
    this.config = config;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    doGet(req, resp);
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    long start = System.currentTimeMillis();
    PersistenceManager pm = null;
    EntityManager em = null;
    List<Flight> flights;
    String query = DEFAULT_QUERY;
    String customQuery = req.getParameter("q");
    if (customQuery != null) {
      query = customQuery;
    }
    try {
      if (PersistenceStandard.get() == PersistenceStandard.JPA) {
        em = EMF.emf.createEntityManager();
        flights = em.createQuery(query).getResultList();
      } else {
        pm = PMF.pmf.getPersistenceManager();
        flights = (List<Flight>) pm.newQuery(query).execute();
      }
      printQueryFlightForm(query, resp);
      printTableHeader(resp);

      resp.getWriter().println("<table>");

      printFlights(resp, flights);
      resp.getWriter().println("</table>");
      printAddFlightForm(query, resp);
      resp.getWriter().println("Request time in millis: " + (System.currentTimeMillis() - start));
      resp.getWriter().println("<br>");
      printPersistenceStandardForm(query, resp);
    } finally {
      if (pm != null) {
        pm.close();
      }

      if (em != null) {
        em.close();
      }
    }
  }

  private void printQueryFlightForm(String query, HttpServletResponse resp) throws IOException {
    PersistenceStandard ps = PersistenceStandard.get();
    resp.getWriter().println("<form action=\"/\" method=\"post\">");
    resp.getWriter().println(ps.name() + " Query for flights: "
        + "<input name=\"q\" type=\"text\" size=\"100\" "
        + "value=\"" + (query == null ? "" : query) + "\"/>");
    resp.getWriter().println("<input type=\"submit\" value=\"Run Query\">");
    resp.getWriter().println("</form>");
  }

  private void printPersistenceStandardForm(String query, HttpServletResponse resp) throws IOException {
    PersistenceStandard ps = PersistenceStandard.get();
    resp.getWriter().println("Persistence standard is " + ps.name());

    resp.getWriter().println("<form action=\"updatePersistenceStandard\" method=\"post\">");
    resp.getWriter().println("<input type=\"submit\" value=\"Switch to " + ps.getAlternate() + "\">");
    resp.getWriter().println(
        "<input name=\"persistenceStandard\" type=\"hidden\" value=\"" + ps.getAlternate() + "\"/>");
    resp.getWriter().println("<input name=\"q\" type=\"hidden\" value=\"" + query + "\"/>");
    resp.getWriter().println("</form>");
  }

  private void printAddFlightForm(String query, HttpServletResponse resp) throws IOException {
    resp.getWriter().println("<form action=\"addFlight\" method=\"post\">");
    resp.getWriter().println("<table>");
    resp.getWriter().println("<tr>");
    resp.getWriter().println("<th>Origin</th><td><input name=\"orig\" type=\"text\"/></td>");
    resp.getWriter().println("<th>Destination</th><td><input name=\"dest\" type=\"text\"/></td>");
    resp.getWriter().println("</tr>");
    resp.getWriter().println("<tr><td><input type=\"submit\" value=\"Add Flight\"></td></tr>");
    resp.getWriter().println("<input name=\"q\" type=\"hidden\" value=\"" + query + "\"/>");
    resp.getWriter().println("</table>");
    resp.getWriter().println("</form>");
  }

  private void printFlights(HttpServletResponse resp, List<Flight> flights) throws IOException {
    if (flights.isEmpty()) {
      resp.getWriter().println("<br>No Flights!");
    }
    int index = 1;
    for (Flight f : flights) {
      resp.getWriter().println("<tr>");
      resp.getWriter().println("<td>");
      resp.getWriter().println("<a href=\"getFlight?key=" + f.getId() + "\"> " + index++ + "</a>");
      resp.getWriter().println("</td>");
      resp.getWriter().println("<td>");
      resp.getWriter().println(f.getOrig());
      resp.getWriter().println("</td>");
      resp.getWriter().println("<td>");
      resp.getWriter().println(f.getDest());
      resp.getWriter().println("</td>");
      resp.getWriter().println("</tr>");
    }
  }

  private void printTableHeader(HttpServletResponse resp) throws IOException {
    resp.getWriter().println("<tr>");
    resp.getWriter().println("<td>");
    resp.getWriter().println("</td>");
    resp.getWriter().println("<td>");
    resp.getWriter().println("Origin");
    resp.getWriter().println("</td>");
    resp.getWriter().println("<td>");
    resp.getWriter().println("Dest");
    resp.getWriter().println("</td>");
    resp.getWriter().println("</tr>");
  }

}