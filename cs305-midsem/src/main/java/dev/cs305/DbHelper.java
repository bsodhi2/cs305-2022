package dev.cs305;

import java.sql.*;
import java.util.Date;

/**
  CREATE TABLE RoutingTable (
  	id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  	sender TEXT NOT NULL,
  	messageType TEXT NOT NULL,
  	destination TEXT NOT NULL,
  	CONSTRAINT RoutingTable_UN UNIQUE (sender,messageType,destination)
  );

  CREATE TABLE MessageLog (
  	id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  	routeId INTEGER NOT NULL,
  	eventType TEXT NOT NULL,
  	eventTime TEXT NOT NULL
  );

  DELETE FROM RoutingTable;
  INSERT INTO RoutingTable
  (sender, messageType, destination)
  VALUES('http://127.0.0.1:8201/foo1', 'COMPRESS', 'http://127.0.0.1:8202/bar1'),
  ('http://127.0.0.1:8201/foo2', 'GET_STOCK_QUOTE', 'http://127.0.0.1:8202/bar2'),
  ('http://127.0.0.1:8202/bar1', 'REPLY_FOO1', 'http://127.0.0.1:8201/foo1'),
  ('http://127.0.0.1:8202/bar1', 'REPLY_FOO2', 'http://127.0.0.1:8201/foo2'),
  ('http://127.0.0.1:8202/bar2', 'REPLY_FOO1', 'http://127.0.0.1:8201/foo1'),
  ('http://127.0.0.1:8202/bar2', 'REPLY_FOO2', 'http://127.0.0.1:8201/foo2')
  ;

 */
public class DbHelper {
    public static class Route {
        public int id;
        public String destination;
        public String messageType;

        public Route(int id, String destination, String messageType) {
            this.id = id;
            this.destination = destination;
            this.messageType = messageType;
        }

        @Override
        public String toString() {
            return "Route{" +
                    "id=" + id +
                    ", destination='" + destination + '\'' +
                    ", messageType='" + messageType + '\'' +
                    '}';
        }
    }

    private String dbUrl;

    public DbHelper(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    private Connection getConn() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(dbUrl);
    }

    public Route getDestinationForMessage(String sender, String msgType)
            throws SQLException, ClassNotFoundException {
        try (PreparedStatement ps = getConn().prepareStatement(
                new StringBuilder().append("SELECT id, destination FROM RoutingTable r ")
                        .append("WHERE r.sender=? AND r.messageType=?;").toString())) {
            ps.setString(1, sender);
            ps.setString(2, msgType);
            ResultSet rs = ps.executeQuery();
            int id = 0;
            String dest = null;
            if (rs.next()) {
                id = rs.getInt(1);
                dest = rs.getString(2);
            } else {
                System.out.println("!!!!Query didn't return any results!");
            }
            return new Route(id, dest, msgType);
        }
    }

    public int saveEvent(int routeId, String eventType)
            throws SQLException, ClassNotFoundException {

        try (PreparedStatement ps = getConn().prepareStatement(new StringBuilder()
                .append("INSERT INTO MessageLog(routeId, eventType, eventTime) ")
                .append("VALUES(?,?,?);").toString())) {
            ps.setInt(1, routeId);
            ps.setString(2, eventType);
            ps.setString(3, new Date().toString());
            return ps.executeUpdate();
        }
    }
}
