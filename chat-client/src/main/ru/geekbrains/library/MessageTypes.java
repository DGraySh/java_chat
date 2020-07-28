package ru.geekbrains.library;

import ru.geekbrains.chat.common.MessageLibrary;

public class MessageTypes { // класс проверяет типы входящих сообщений на клиенте

    private MessageTypes() {
    }

    public static boolean isBroadcast (String msg) {
        String[] arr = msg.split(MessageLibrary.DELIMITER);
        return  (arr.length == 4
                && arr[0].equals(MessageLibrary.TYPE_BROADCAST));
    }

    public static boolean isAuthAccept (String msg) {
        String[] arr = msg.split(MessageLibrary.DELIMITER);
        return  (arr.length == 3
                && arr[0].equals(MessageLibrary.AUTH_METHOD)
                && arr[1].equals(MessageLibrary.AUTH_ACCEPT));
    }

    public static boolean isAuthDeny (String msg) {
        String[] arr = msg.split(MessageLibrary.DELIMITER);
        return  (arr.length == 2
                && arr[0].equals(MessageLibrary.AUTH_METHOD)
                && arr[1].equals(MessageLibrary.AUTH_DENIED));
    }

}
