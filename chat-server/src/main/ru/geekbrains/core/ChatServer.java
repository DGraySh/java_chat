package ru.geekbrains.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.chat.common.MessageLibrary;
import ru.geekbrains.net.MessageSocketThread;
import ru.geekbrains.net.MessageSocketThreadListener;
import ru.geekbrains.net.ServerSocketThread;
import ru.geekbrains.net.ServerSocketThreadListener;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListener, MessageSocketThreadListener {

    private ServerSocketThread serverSocketThread;
    private final ChatServerListener listener;
    private AuthController authController;
    private final Vector<ClientSessionThread> clients = new Vector<>();
    private final Logger logger = LogManager.getLogger(ChatServer.class);


    public ChatServer(ChatServerListener listener) {
        this.listener = listener;
    }

    public void start(int port) {
        if (serverSocketThread != null && serverSocketThread.isAlive()) {
            return;
        }
        serverSocketThread = new ServerSocketThread(this,"Chat-Server-Socket-Thread", port, 2000);
        serverSocketThread.start();
        authController = new AuthController();
        authController.init();
        logger.info("Server started");
    }

    public void stop() {
        if (serverSocketThread == null || !serverSocketThread.isAlive()) {
            return;
        }
        serverSocketThread.interrupt();
        disconnectAll();
        logger.info("Server stopped");
    }

    @Override
    public void onClientConnected() {
        logger.info("Client connected");
    }

    @Override
    public void onSocketAccepted(Socket socket) {
        clients.add(new ClientSessionThread(this, "ClientSessionThread", socket));
    }

    @Override
    public void onException(MessageSocketThread thread, Throwable throwable) {
        throwable.printStackTrace();
        logger.warn("Error: ", throwable);
    }

    @Override
    public void onException(Throwable throwable) {
        throwable.printStackTrace();
        logger.warn("Error: ", throwable);
    }

    @Override
    public void onClientTimeout(Throwable throwable) {

    }

    @Override
    public void onSocketReady(MessageSocketThread thread) {
        logger.info("Socket ready");
    }

    @Override
    public void onSocketClosed(MessageSocketThread thread) {
        ClientSessionThread clientSession = (ClientSessionThread) thread;
        String nick = clientSession.getNickname();
        logger.info("Socket for {} closed", nick);
        clients.remove(thread);
        if (clientSession.isAuthorized() && !clientSession.isReconnected()) {
            sendToAllAuthorizedClients(MessageLibrary.getBroadcastMessage("server", "User " + clientSession.getNickname() + " disconnected"));
        }
        sendToAllAuthorizedClients(MessageLibrary.getUserList(getUsersList()));
    }

    @Override
    public void onMessageReceived(MessageSocketThread thread, String msg) {
        ClientSessionThread clientSession = (ClientSessionThread) thread;
        if (checkChangeNickMsg(msg) && clientSession.isAuthorized()) { //смена ника
            changeNickname(clientSession, msg);
        } else if (clientSession.isAuthorized()) {
            processAuthorizedUserMessage(msg);
        } else {
            processUnauthorizedUserMessage(clientSession, msg);
        }
    }

    //проверка на поступившее сообщение о смене ника
    private boolean checkChangeNickMsg(String msg) {
        return MessageLibrary.getMessageType(msg) == MessageLibrary.MESSAGE_TYPE.CHANGE_NICKNAME;
    }

    private void processAuthorizedUserMessage(String msg) {
        logger.debug(msg);
        for (ClientSessionThread client : clients) {
            if (!client.isAuthorized()) {
                continue;
            }
            client.sendMessage(msg);
        }
    }

    // обработка служебного сообщения о смене ника в формате "/change_nick##login##new_nickname"
    private void changeNickname(ClientSessionThread clientSession, String msg) {
        String[] arr = msg.split(MessageLibrary.DELIMITER);
        String oldNickname = clientSession.getNickname();
        if (arr.length > 2 && arr[0].equals(MessageLibrary.CHANGE_NICKNAME)) {
            authController.setNickname(oldNickname, arr[1], arr[2]); // замена ника в БД
            clientSession.setNickname(AuthController.getNewNickname(arr[1])); // сменить ник в листе сессий на текущий ник из БД
            logger.info("User {} changed nick to {}", oldNickname, arr[2]);
            sendToAllAuthorizedClients(MessageLibrary.getUserList(getUsersList())); //рассылка юзерлиста
        }
    }

    private void sendToAllAuthorizedClients(String msg) {
        for (ClientSessionThread client : clients) {
            if (!client.isAuthorized()) {
                continue;
            }
            client.sendMessage(msg);
        }
    }

    private void processUnauthorizedUserMessage(ClientSessionThread clientSession, String msg) {
        String[] arr = msg.split(MessageLibrary.DELIMITER);
        if (arr.length < 4 ||
                !arr[0].equals(MessageLibrary.AUTH_METHOD) ||
                !arr[1].equals(MessageLibrary.AUTH_REQUEST)) {
            clientSession.authError("Incorrect request: " + msg);
            return;
        }
        String login = arr[2];
        String password = arr[3];
        String nickname = authController.getNickname(login, password);
        if (nickname == null) {
            clientSession.authDeny();
            return;
        } else {
            ClientSessionThread oldClientSession = findClientSessionByNickname(nickname);
            clientSession.authAccept(nickname);
            if (oldClientSession == null) {
                sendToAllAuthorizedClients(MessageLibrary.getBroadcastMessage("Server", nickname + " connected"));
            } else {
                oldClientSession.setReconnected(true);
                clients.remove(oldClientSession);
            }
        }
        sendToAllAuthorizedClients(MessageLibrary.getUserList(getUsersList()));
    }

    public void disconnectAll() {
        ArrayList<ClientSessionThread> currentClients = new ArrayList<>(clients);
        for (ClientSessionThread client : currentClients) {
            client.close();
            clients.remove(client);
            logger.info("All users disconnected");
        }
    }

    public String getUsersList() {
        StringBuilder sb = new StringBuilder();
        for (ClientSessionThread client : clients) {
            if (!client.isAuthorized()) {
                continue;
            }
            sb.append(client.getNickname()).append(MessageLibrary.DELIMITER);
        }
        return sb.toString();
    }

    private ClientSessionThread findClientSessionByNickname(String nickname) {
        for (ClientSessionThread client : clients) {
            if (!client.isAuthorized()) {
                continue;
            }
            if (client.getNickname().equals(nickname)) {
                return client;
            }
        }
        return null;
    }
}