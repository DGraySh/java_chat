package ru.geekbrains.net;

public interface MessageSocketThreadListener {

    void onSocketReady(MessageSocketThread thread);
    void onSocketClosed(MessageSocketThread thread);
    void onMessageReceived(MessageSocketThread thread, String msg);
    void onException(MessageSocketThread thread, Throwable throwable);
}