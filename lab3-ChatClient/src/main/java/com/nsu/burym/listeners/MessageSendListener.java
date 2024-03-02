package com.nsu.burym.listeners;

import com.nsu.burym.parser.ParseXml;
import com.nsu.burym.connection.ConnectionToServer;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MessageSendListener implements ActionListener {
    private final JTextField fieldInput;
    private final ConnectionToServer connection;

    public MessageSendListener(@NotNull JTextField fieldInput, @NotNull ConnectionToServer connection) {
        this.fieldInput = fieldInput;
        this.connection = connection;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String mes = fieldInput.getText();
        if (mes.equals("")) {
            return;
        }
        try {
            Document doc = ParseXml.writeDocument(mes, "message");
            connection.send(doc);
            fieldInput.setText(null);
        }
        catch (ParserConfigurationException ex) {
            System.out.println("Failed to write xml document: " + ex.getMessage());
        }
    }
}
