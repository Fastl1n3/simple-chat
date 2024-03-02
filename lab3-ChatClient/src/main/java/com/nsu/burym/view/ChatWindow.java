package com.nsu.burym.view;

import com.nsu.burym.parser.ParseXml;
import com.nsu.burym.connection.ConnectionToServer;
import com.nsu.burym.listeners.CloseWindowListener;
import com.nsu.burym.listeners.ListUsersListener;
import com.nsu.burym.listeners.MessageSendListener;
import com.nsu.burym.listeners.NicknameListener;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Optional;

public class ChatWindow extends JFrame {
    private JTextArea chat;
    private JMenu userList;

    public ChatWindow(double displayCoeffX, double displayCoeffY) {
        try {
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize((int) (700 * displayCoeffX), (int) (700 * displayCoeffY));
            JTextField fieldInput = new JTextField(40);
            ConnectionToServer connection = new ConnectionToServer(this);
            addWindowListener(new CloseWindowListener(connection));

            fieldInput.addActionListener(new MessageSendListener(fieldInput, connection));
            fieldInput.setEnabled(false);

            JPanel bottomPanel = new JPanel();
            JLabel label = new JLabel("Введите текст");
            JButton sendButton = new JButton("Отправить");
            sendButton.addActionListener(new MessageSendListener(fieldInput, connection));
            bottomPanel.add(label);
            bottomPanel.add(fieldInput);
            bottomPanel.add(sendButton);

            //панель сверху с ником
            JPanel panelTop = new JPanel();
            JTextField nicknameField = new JTextField(10);
            JLabel label2 = new JLabel("Никнейм");
            nicknameField.addActionListener(new NicknameListener(fieldInput, nicknameField, connection));
            panelTop.add(label2);
            panelTop.add(nicknameField);

            JMenuBar mb = new JMenuBar();
            userList = new JMenu("Список пользователей");
            userList.addMouseListener(new ListUsersListener(userList, connection));
            mb.add(userList);
            setJMenuBar(mb);

            chat = new JTextArea();
            chat.setEditable(false);
            chat.setLineWrap(true);

            JScrollPane scroll = new JScrollPane(chat);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

            getContentPane().add(BorderLayout.SOUTH, bottomPanel);
            getContentPane().add(BorderLayout.CENTER, scroll);
            getContentPane().add(BorderLayout.BEFORE_FIRST_LINE, panelTop);
            setVisible(true);
        }
        catch (IOException e) {
            System.out.println("Server doesn't response.");
        }
    }

    public void addMessage(@NotNull Document document) {
        Optional<String> str = ParseXml.read(document);
        if (str.isPresent()) {
            chat.append(str.get());
            chat.setCaretPosition(chat.getDocument().getLength());
        }
        else {
            String[] nicknames = ParseXml.readUserList(document);
            addUserList(nicknames);
        }
    }

    private void addUserList(@NotNull String[] nicknames) {
        userList.removeAll();
        for (String nickname : nicknames) {
            if (nickname.equals("")) {
                continue;
            }
            userList.add(new JMenuItem(nickname));
        }
    }
}
