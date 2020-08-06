package ru.geekbrains.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MessageSocketThread extends Thread {

    private final Socket socket;
    private final MessageSocketThreadListener listener;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean isClosed = false;

    public MessageSocketThread(MessageSocketThreadListener listener, String name, Socket socket) {
        super(name);
        this.socket = socket;
        this.listener = listener;
        start();
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            listener.onSocketReady(this);
            while (!isInterrupted()) {
                if (!isClosed) {
                    listener.onMessageReceived(this, in.readUTF());
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            close();
        }
    }

    public void sendMessage(String message) {
        try {
            if (!socket.isConnected() || socket.isClosed() || isClosed) {
                listener.onException(this, new RuntimeException("Socked closed or not initialized"));
                return;
            }
            if (!isClosed) {
                out.writeUTF(message);
            }
        } catch (IOException e) {
            close();
            listener.onException(this, e);
        }
    }

    public synchronized void close() {
        isClosed = true;
        interrupt();
        try {
            if (out != null) {
                out.close();
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        listener.onSocketClosed(this);
    }
}