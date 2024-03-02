package com.nsu.burym.listeners;

import com.nsu.burym.parser.ParseXml;
import com.nsu.burym.connection.ConnectionToServer;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CloseWindowListener extends WindowAdapter {
    private final ConnectionToServer connection;

    public CloseWindowListener(@NotNull ConnectionToServer connection) {
        this.connection = connection;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        try {
            Document doc = ParseXml.writeDocument("", "logout");
            connection.send(doc);
        } catch (ParserConfigurationException ex) {
            System.out.println("Failed to write xml document: " + ex.getMessage());
        }
        connection.setStop();

    }
}
