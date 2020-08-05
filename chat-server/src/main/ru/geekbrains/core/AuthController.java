package ru.geekbrains.core;

import java.sql.*;

public class AuthController {

    public void init() {
//        new User("admin", "admin", "sysroot");
//        new User("alex", "123", "alex-st");
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

    public void setNickname(String oldNickname, String newNickname) {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:usersDB.db");
             PreparedStatement ps = connection.prepareStatement("UPDATE users set Nickname = ? WHERE Nickname = ?")) {
            ps.setString(1, newNickname);
            ps.setString(2, oldNickname);
            ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}