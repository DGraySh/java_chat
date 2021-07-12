package ru.geekbrains.net;

import java.net.Socket;

public interface ServerSocketThreadListener {

    void onClientConnected();
    void onSocketAccepted(Socket socket);
    void onException(Throwable throwable);
    void onClientTimeout(Throwable throwable);

}
