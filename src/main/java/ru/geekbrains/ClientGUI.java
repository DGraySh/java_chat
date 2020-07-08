package ru.geekbrains;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;

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
    private final JTextField messageField = new JTextField();
    private final JButton buttonSend = new JButton("Send");

    private final JList<String> listUsers = new JList<>();



    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI();
            }
        });
    }


    ClientGUI () {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chat");
        setSize(WIDTH, HEIGHT);
        setAlwaysOnTop(true);

        listUsers.setListData(new String[]{"user1", "user2", "user3", "user4",
                "user5", "user6", "user7", "user8", "user9", "user-with-too-long-name-in-this-chat"});
        JScrollPane scrollPaneUsers = new JScrollPane(listUsers);
        JScrollPane scrollPaneChatArea = new JScrollPane(chatArea);
        scrollPaneUsers.setPreferredSize(new Dimension(100, 0));

        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setEditable(false);

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

        cbAlwaysOnTop.addActionListener(this);

        setVisible(true);
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
}
