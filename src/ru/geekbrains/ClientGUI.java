package ru.geekbrains;

import org.apache.commons.io.input.ReversedLinesFileReader;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;


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

    private static void addTextToFile(String fileName, String text) {
        try (FileWriter outFile = new FileWriter(fileName, true);
             BufferedWriter fileWriter = new BufferedWriter(outFile)) {
            fileWriter.newLine();
            fileWriter.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI();
            }
        });
    }

    ClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chat");
        setSize(WIDTH, HEIGHT);
        setAlwaysOnTop(true);
        String user = "user"; //temporary username

        listUsers.setListData(new String[]{"user1", "user2", "user3", "user4",
                "user5", "user6", "user7", "user8", "user9", "user-with-too-long-name-in-this-chat"});
        JScrollPane scrollPaneUsers = new JScrollPane(listUsers);
        JScrollPane scrollPaneChatArea = new JScrollPane(chatArea);
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

        cbAlwaysOnTop.addActionListener(this);

        //readHistory(); //при запуске вывести в chatArea историю последней переписки, нужно допилить

        addTextToFile("log.txt", "\n ------------- " +  //пишем в лог разрыв с датой
                java.time.LocalDateTime.now() + "-------------\n");   //при отрисовке главного окна

        KeyListener enterIsPressed = new KeyListener() { //создаем слушателя нажатия клавиш клавиатуры
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) { //переопределяю метод слушателя нажатия кнопки enter
                if (e.getKeyCode() == 10) { //если нажат именно enter, сверяем по коду клавиши
                    sendText(user);
                }
            }
        };

        messageField.addKeyListener(enterIsPressed);
        buttonSend.addActionListener(e -> sendText(user));

        chatArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                addTextToFile("log.txt", user + ": " + messageField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        setVisible(true);
    }

    private void sendText(String user) { //отправка сообщения с аргументом имя пользователя
        if (!messageField.getText().isEmpty()) { //если сообщение не пустое, тогда печатаем его
            chatArea.setText(chatArea.getText() + user + ": " + messageField.getText() + "\n");
            messageField.setText(""); //и заменяем поле ввода пустой строкой
        }
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

    private void readHistory() { //нужно загнать в массив и перевернуть строки в обратном порядке
        StringBuilder history = new StringBuilder(); // еще надо добавить проверку на наличие строк в файле
        try (ReversedLinesFileReader file = new ReversedLinesFileReader(new File("log.txt"), Charset.defaultCharset())) {
            int counter = 0;
            while (counter < 5) {
                history.append(file.readLine() + "\n");
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(history);
    }
}
