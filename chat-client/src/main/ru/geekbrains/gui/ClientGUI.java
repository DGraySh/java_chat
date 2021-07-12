package ru.geekbrains.gui;

import org.apache.commons.io.input.ReversedLinesFileReader;
import ru.geekbrains.chat.common.MessageLibrary;
import ru.geekbrains.core.AuthController;
import ru.geekbrains.net.MessageSocketThread;
import ru.geekbrains.net.MessageSocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class ClientGUI extends JFrame implements ActionListener, UncaughtExceptionHandler, MessageSocketThreadListener {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;
    private static final String WINDOW_TITLE = "Chat Client";
    private final JTextArea chatArea = new JTextArea();
    private final JPanel panelTop = new JPanel(new GridLayout(2, 3));
    private final JTextField ipAddressField = new JTextField("127.0.0.1");
    private final JTextField portField = new JTextField("8181");
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Always on top", true);
    private final JTextField loginField = new JTextField("login");
    private final JPasswordField passwordField = new JPasswordField("123");
    private final JButton buttonLogin = new JButton("Login");
    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JButton buttonDisconnect = new JButton("<html><b>Disconnect</b></html>");
    private final JScrollPane scrollPaneChatArea;
    private final JTextField messageField = new JTextField();
    private final JButton buttonSend = new JButton("Send");
    private final JList<String> listUsers = new JList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private final String chatHistoryFile = "_" + "history.txt";
    private MessageSocketThread socketThread;
    private String nickname;
    private String login;

    ClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle(WINDOW_TITLE);

        setTitle("Chat");
        setSize(WIDTH, HEIGHT);
        setAlwaysOnTop(true);

        JScrollPane scrollPaneUsers = new JScrollPane(listUsers);
        scrollPaneChatArea = new JScrollPane(chatArea);
        scrollPaneUsers.setPreferredSize(new Dimension(100, 0));

        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setEditable(false);
        chatArea.setText("");

        panelTop.add(ipAddressField);
        panelTop.add(portField);
        panelTop.add(cbAlwaysOnTop);
        panelTop.add(loginField);
        panelTop.add(passwordField);
        panelTop.add(buttonLogin);
        panelBottom.add(buttonDisconnect, BorderLayout.WEST);
        panelBottom.add(messageField, BorderLayout.CENTER);
        panelBottom.add(buttonSend, BorderLayout.EAST);

        add(scrollPaneChatArea, BorderLayout.CENTER);
        add(scrollPaneUsers, BorderLayout.EAST);
        add(panelTop, BorderLayout.NORTH);
        add(panelBottom, BorderLayout.SOUTH);
        panelBottom.setVisible(false);

        cbAlwaysOnTop.addActionListener(this);
        buttonLogin.addActionListener(e -> {
            Socket socket = null;
            try {
                socket = new Socket(ipAddressField.getText(), Integer.parseInt(portField.getText()));
                socketThread = new MessageSocketThread(this, "Client" + loginField.getText(), socket);
                login = loginField.getText();
            } catch (IOException ioException) {
                showError(ioException.getMessage());
            }
        });

        buttonDisconnect.addActionListener(e -> socketThread.close());
        messageField.addActionListener(e -> sendMessage(messageField.getText()));
        buttonSend.addActionListener(e -> {
            sendMessage(messageField.getText());
            messageField.grabFocus();
        });
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }

    public void sendMessage(String msg) {
        if (msg.isEmpty()) {
            return;
        }
        if (MessageLibrary.getMessageType(msg) == MessageLibrary.MESSAGE_TYPE.CHANGE_NICKNAME) {
            putMessageInChatArea(nickname, msg);
            messageField.setText("");
            messageField.grabFocus();
            socketThread.sendMessage(msg);
            this.nickname = AuthController.getNewNickname(login);
            setTitle(WINDOW_TITLE + " authorized with nickname: " + this.nickname);
        } else {
            putMessageInChatArea(nickname, msg);
            messageField.setText("");
            messageField.grabFocus();
            socketThread.sendMessage(MessageLibrary.getTypeBroadcastClient(nickname, msg));
        }
    }

    private void scrollDownChatArea() {
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void putIntoFileHistory(String msg, String fileName) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(login + fileName, true))) {
            pw.print(msg);
        } catch (FileNotFoundException e) {
            showError(msg);
        }
    }

    private void showError(String errorMsg) {
        JOptionPane.showMessageDialog(this, errorMsg, "Exception!", JOptionPane.ERROR_MESSAGE);
    }

    public void putMessageInChatArea(String user, String msg) {
        String messageToChat = String.format("%s <%s>: %s%n", sdf.format(Calendar.getInstance().getTime()), user, msg);
        chatArea.append(messageToChat);
        putIntoFileHistory(messageToChat, chatHistoryFile);
    }

    public void putHistoryInChatArea(String history) {
        chatArea.setText("");
        chatArea.append(history);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == cbAlwaysOnTop) {
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        } else {
            throw new RuntimeException("Unsupported action: " + src);
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        StackTraceElement[] ste = e.getStackTrace();
        String msg = String.format("Exception in \"%s\": %s %s%n\t %s",
                t.getName(), e.getClass().getCanonicalName(), e.getMessage(), ste[0]);
        JOptionPane.showMessageDialog(this, msg, "Exception!", JOptionPane.ERROR_MESSAGE);
    }

    private String readHistory(String fileName) {
        StringBuilder history = new StringBuilder();
        if ((new File(fileName)).exists()) {
            try (ReversedLinesFileReader file = new ReversedLinesFileReader(new File(fileName), Charset.defaultCharset())) {
                int counter = 0;
                String line = file.readLine();
                while (counter < 99 && line != null) {
                    history.insert(0, line + "\n");
                    line = file.readLine();
                    counter++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return history.toString();
    }

    @Override
    public void onSocketReady(MessageSocketThread thread) {
        panelTop.setVisible(false);
        panelBottom.setVisible(true);
        socketThread.sendMessage(MessageLibrary.getAuthRequestMessage(loginField.getText(), new String(passwordField.getPassword())));
    }

    @Override
    public void onSocketClosed(MessageSocketThread thread) {
        panelTop.setVisible(true);
        panelBottom.setVisible(false);
        setTitle(WINDOW_TITLE);
        listUsers.setListData(new String[0]);
        chatArea.setText("");
    }

    @Override
    public void onMessageReceived(MessageSocketThread thread, String msg) {
        handleMessage(msg);
        scrollDownChatArea();
    }

    @Override
    public void onException(MessageSocketThread thread, Throwable throwable) {
        throwable.printStackTrace();
        showError(throwable.getMessage());
    }

    private void handleMessage(String msg) {
        String[] values = msg.split(MessageLibrary.DELIMITER);
        switch (MessageLibrary.getMessageType(msg)) {
            case AUTH_ACCEPT:
                this.nickname = values[2];
                setTitle(WINDOW_TITLE + " authorized with nickname: " + this.nickname);
                putHistoryInChatArea(readHistory(login + chatHistoryFile));
                break;
            case AUTH_DENIED:
                putMessageInChatArea("server", msg);
                socketThread.close();
                break;
            case TYPE_BROADCAST:
                putMessageInChatArea(values[2], values[3]);
                break;
            case MSG_FORMAT_ERROR:
                putMessageInChatArea("server", msg);
                break;
            case USER_LIST:
                String users = msg.substring(MessageLibrary.USER_LIST.length() +
                        MessageLibrary.DELIMITER.length());
                String[] userArray = users.split(MessageLibrary.DELIMITER);
                Arrays.sort(userArray);
                listUsers.setListData(userArray);
                break;
            case TYPE_BROADCAST_CLIENT:
                String srcNickname = values[1];
                if (srcNickname.equals(nickname)) {
                    return;
                }
                putMessageInChatArea(srcNickname, values[2]);
                break;
            case CHANGE_NICKNAME:
                putMessageInChatArea("server", "User " + values[1] + " changed nickname on: " + values[2]);
                break;
            default:
                throw new RuntimeException("Unknown message: " + msg);
        }
    }
}


