package ru.geekbrains.core;

import ru.geekbrains.data.DB;
import ru.geekbrains.data.User;

import java.util.HashMap;

public class AuthController {

    HashMap<String, User> users = new HashMap<>();

    public void init() {
//        new User("admin", "admin", "sysroot");
//        new User("alex", "123", "alex-st");
        for (User user : DB.getUsersFromDB()) {
            users.put(user.getLogin(), user);
        }
    }

    public String getNickname(String login, String password) {
        User user = users.get(login);
        if (user != null && user.isPasswordCorrect(password)) {
            return user.getNickname();
        }
        return null;
    }

//    private ArrayList<User> receiveUsers() {
//        ArrayList<User> usersArr = new ArrayList<>();
//        usersArr.add(new User("admin", "admin", "sysroot"));
//        usersArr.add(new User("alex", "123", "alex-st"));
//        return usersArr;
//    }

}