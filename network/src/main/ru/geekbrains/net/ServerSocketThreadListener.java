package ru.geekbrains.net;

import java.net.Socket;

public interface ServerSocketThreadListener {

    //  void onSocketStarted();
//  void onSocketClosed();
    void onClientConnected();
    void onSocketAccepted(Socket socket);
    void onException(Throwable throwable);
    void onClientTimeout(Throwable throwable);

}