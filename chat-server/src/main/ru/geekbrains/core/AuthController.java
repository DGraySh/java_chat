package ru.geekbrains.core;

import java.sql.*;

public class AuthController {

    public void init() {

    }

    public String getNickname(String login, String password) {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:usersDB.db");
             PreparedStatement ps = connection.prepareStatement("SELECT Nickname FROM users WHERE Login = ? and Password = ?")) {
            ps.setString(1, login);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Nickname");
                }
            }
        } catch (SQLException e) {

        }
        return null;
    }

    public static String getNewNickname(String login) {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:usersDB.db");
             PreparedStatement ps = connection.prepareStatement("SELECT Nickname FROM users WHERE Login = ?")) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Nickname");
                }
            }
        } catch (SQLException e) {

        }
        return null;
    }

    public void setNickname(String oldNickname, String login, String newNickname) {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:usersDB.db");
             PreparedStatement ps = connection.prepareStatement("UPDATE users set Nickname = ? WHERE Nickname = ? and Login = ?;")) {
            ps.setString(1, newNickname);
            ps.setString(2, oldNickname);
            ps.setString(3, login);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
