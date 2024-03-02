package com.nsu.burym.listeners;

import com.nsu.burym.parser.ParseXml;
import com.nsu.burym.connection.ConnectionToServer;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NicknameListener implements ActionListener {
    private final JTextField nickname;
    private final ConnectionToServer connection;
    private final JTextField fieldInput;
    public NicknameListener(@NotNull JTextField fieldInput, @NotNull JTextField nickname, @NotNull ConnectionToServer connection) {
        this.nickname = nickname;
        this.connection = connection;
        this.fieldInput = fieldInput;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String mes = nickname.getText();
        if (mes.equals("")) {
            return;
        }
        try {
            Document doc = ParseXml.writeDocument(mes, "login");
            connection.send(doc);
            nickname.setEnabled(false);
            fieldInput.setEnabled(true);
        }
        catch (ParserConfigurationException ex) {
            System.out.println("Failed to write xml document: " + ex.getMessage());
        }
    }
}
