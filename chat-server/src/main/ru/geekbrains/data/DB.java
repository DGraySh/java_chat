package ru.geekbrains.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DB { //всю работу с БД вынес в отдельный класс

    private static Connection connection;
    private static Statement statement;

    public static void connect() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:usersDB.db");
        statement = connection.createStatement();
    }

    public static void disconnect(){
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

/*    public static void addUserToDB(User user){ //метод для добавления пользователей в ДБ, метод getPassword плохая
        try {                                   //идея, но как его обойти не додумался
            connect();
            statement.executeUpdate("INSERT OR REPLACE INTO users (Login, Nickname, Password) VALUES ('" +
                    user.getLogin() + "', '" +
                    user.getNickname() + "', '" +
                    user.getPassword() + "');");
        } catch (SQLException|ClassNotFoundException throwables) {
            throwables.printStackTrace();
        } finally {
            disconnect();
        }
    }*/

    public static List<User> getUsersFromDB(){ //получить юзеров из ДБ и отдать в листе
        ArrayList<User> usersArr = new ArrayList<>();
        try {
            connect();
            ResultSet rs = statement.executeQuery("SELECT * FROM users");
            while (rs.next()){
                usersArr.add(new User(
                        rs.getString("Login"),
                        rs.getString("Password"),
                        rs.getString("Nickname")));
            }
        } catch (SQLException|ClassNotFoundException throwables) {
            throwables.printStackTrace();
        } finally {
        disconnect();
        }
        return usersArr;
    }

    /*public static void changeNickname(String login, String nickname){ //нужно доделать, затупил с реализацией
        try {
            statement.executeQuery("UPDATE INTO users set Nickname = " +
                    nickname + " WHERE Login = " + login);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }*/

   /* public static void changeUserData(String login, String field, String nickname){ //нужно доделать, затупил с реализацией
        try {
            connect();
            PreparedStatement ps = connection.prepareStatement("UPDATE INTO users set ? = ? WHERE Login = ?;");
            ps.setString(1, field);
            ps.setString(1, nickname);
            ps.setString(2, login);
        } catch (SQLException|ClassNotFoundException throwables) {
            throwables.printStackTrace();
        } finally {
            disconnect();
        }
    }*/

}
