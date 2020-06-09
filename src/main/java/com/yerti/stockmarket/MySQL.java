package com.yerti.stockmarket;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.*;

public class MySQL {

    public static boolean dbstatus = true;
    private Connection con = null;

    public MySQL(Plugin plugin) {

        String mysqlIP = plugin.getConfig().getString("mysql.ip");
        String mysqlPort = plugin.getConfig().getString("mysql.port");
        String mysqlDB = plugin.getConfig().getString("mysql.database");
        String mysqlUser = plugin.getConfig().getString("mysql.username");
        String mysqlPW = plugin.getConfig().getString("mysql.password");



        final String driver = "com.mysql.jdbc.Driver";
        String connection = "jdbc:mysql://" + mysqlIP + ":" + mysqlPort + "/" + mysqlDB;
        final String user = mysqlUser;
        final String password = mysqlPW;

        try {
            Class.forName(driver);
            con = DriverManager.getConnection(connection, user, password);


            setUpTables();

        } catch (SQLException e) {
            try {
                connection = "jdbc:mysql://" + mysqlIP + ":" + mysqlPort;
                con = DriverManager.getConnection(connection, user, password);

                execute("CREATE DATABASE IF NOT EXISTS " + mysqlDB);

                connection = "jdbc:mysql://" + mysqlIP + ":" + mysqlPort + "/" + mysqlDB;
                con = DriverManager.getConnection(connection, user, password);

                setUpTables();
            } catch (SQLException e1) {
                throwSQLException(e1, "Failed to create database during initialisation. Most likely due to incorrect database settings in config file.");
                dbstatus = false;
            }

            //e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("[StockMarket] " + "SQL Drivers not installed. The server and java installation must support JDBC connections.");
            if (StockMarket.debugMode) {
                e.printStackTrace();
            }
            dbstatus = false;

        }


    }

    public void throwSQLException(SQLException e, String msg) {
        if (msg != null) {
            System.out.println("[StockMarket] " + msg);
        }
        if (StockMarket.debugMode) {
            e.printStackTrace();
        }
    }

    private void setUpTables() {
        try {
            execute("CREATE TABLE IF NOT EXISTS transactions (id int NOT NULL AUTO_INCREMENT, PRIMARY KEY(id), name tinytext, stockID tinytext, quantity int, time tinytext");
            execute("CREATE TABLE IF NOT EXISTS stocks (id int NOT NULL AUTO_INCREMENT, PRIMARY KEY(id), name tinytext, stockID tinytext, price decimal(64, 2), basePrice decimal(64, 2), maxPrice decimal(64, 2), minPrice decimal(64, 2), volatility decimal(64, 2), amount int, lastPercent decimal(64, 2), dividend decimal(10, 2))");
            execute("CREATE TABLE IF NOT EXISTS players (id int NOT NULL AUTO_INCREMENT, PRIMARY KEY(id), name tinytext)");
            execute("CREATE TABLE IF NOT EXISTS looptime (looptime int NOT NULL DEFAULT 0, PRIMARY KEY(looptime), looptime2 int NOT NULL DEFAULT 0)");
        } catch (SQLException e) {
            throwSQLException(e, "Could not execute create table statements during initialisation.");
            dbstatus = false;
        }
        ResultSet result = query("SELECT * FROM looptime");

        try {
            boolean found = false;
            while (result.next()) {
                found = true;
            }
            if (!found) {
                try {
                    execute("INSERT INTO looptime (looptime, looptime2) VALUES(0, 0)");
                } catch (SQLException e) {
                    throwSQLException(e, "Database Error - Could not update looptime!");
                    dbstatus = false;
                }
            }
        } catch (SQLException e) {
            throwSQLException(e, "General Database error. Enable debug-mode for more info!");
            dbstatus = false;
        }
    }


    public ResultSet query(PreparedStatement stmt) {
        ResultSet rs = null;

        try {
            rs = stmt.executeQuery();
        } catch (SQLException e4) {
            throwSQLException(e4, "General Database error. Enable debug-mode for more info.");
            dbstatus = false;
        }

        return rs;
    }

    public ResultSet query(String string) {
        ResultSet rs = null;

        try {
            PreparedStatement stmt = prepareStatement(string);
            rs = stmt.executeQuery();
        } catch (SQLException e4) {
            throwSQLException(e4, "General Database error. Enable debug-mode for more info.");
            dbstatus = false;
        }

        return rs;
    }

    public void execute(PreparedStatement stmt) {

        try {
            stmt.execute();
        } catch (SQLException e4) {
            throwSQLException(e4, "General Database error. Enable debug-mode for more info.");
            dbstatus = false;
        }
    }

    public void execute(String s) throws SQLException {

        PreparedStatement stmt = prepareStatement(s);
        stmt.execute();
    }

    public void close() {
        try {
            con.close();
        } catch (SQLException e) {
            throwSQLException(e, "General Database error. Could not close SQL connection after use! Enable debug-mode for more info.");
            dbstatus = false;
        }
    }

    public PreparedStatement prepareStatement(String s) {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(s);
        } catch (SQLException e) {
            throwSQLException(e, "General Database error. Could not convert " + s + " to a prepared database statement! Enable debug-mode for more info.");
            dbstatus = false;
        }

        return stmt;
    }
}