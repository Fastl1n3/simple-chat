package com.nsu.burym.listeners;

import com.nsu.burym.parser.ParseXml;
import com.nsu.burym.connection.ConnectionToServer;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.event.MouseEvent;

public class ListUsersListener extends MouseInputAdapter {
    private final ConnectionToServer connection;
    private final JMenu menu;

    public ListUsersListener(@NotNull JMenu menu, @NotNull ConnectionToServer connection) {
        this.connection = connection;
        this.menu = menu;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        menu.setSelected(false);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        try {
            Document doc = ParseXml.writeDocument("", "list");
            connection.send(doc);
        }
        catch (ParserConfigurationException ex) {
            System.out.println("Failed to write xml document: " + ex.getMessage());
        }

    }
}
