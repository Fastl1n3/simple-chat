package com.nsu.burym.parser;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class ParseXml {
    public static Optional<String> read(@NotNull Document document) {
        StringBuilder string = new StringBuilder();
        SimpleDateFormat formatter = new SimpleDateFormat("(dd-MMM HH:mm) ");
        String date = formatter.format(new Date());
        string.append(date);
        Element root = document.getDocumentElement();
        if (root.getTagName().equals("event")) {
            String event = root.getAttribute("name");
            if (event.equals("message")) { //распечатка сообщения в чате
                NodeList parametrs = root.getChildNodes();
                string.append(parametrs.item(1).getTextContent());
                string.append(": ");
                string.append(parametrs.item(0).getTextContent()).append("\n");
            }
            if (event.equals("userlogin")) { //новый клиент
                string.append("SERVER: ");
                string.append(root.getChildNodes().item(0).getTextContent());
                string.append(" has joined.\n");
            }
            if (event.equals("userlogout")) {
                string.append("SERVER: ");
                string.append(root.getChildNodes().item(0).getTextContent());
                string.append(" has left.\n");
            }
        }
        if (root.getTagName().equals("success")) {
            return Optional.empty();
        }
        return Optional.of(string.toString());
    }

    public static Document writeDocument(@NotNull String mes, @NotNull String attr) throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("command");
        root.setAttribute("name", attr);
        if (attr.equals("message")) { // написание сообщения в чат
            Element value = doc.createElement("message");
            value.setTextContent(mes);
            root.appendChild(value);
        }
        if (attr.equals("login")) {
            Element value = doc.createElement("name");
            value.setTextContent(mes);
            root.appendChild(value);
        }
        doc.appendChild(root);
        return doc;
    }

    public static String[] readUserList(@NotNull Document document) {
        Node root = document.getDocumentElement();
        Node listUsers = root.getChildNodes().item(0);
        NodeList users = listUsers.getChildNodes();
        String[] nicknames = new String[users.getLength()];
        for (int i = 0; i < users.getLength(); i++) {
            Node user = users.item(i);
            nicknames[i] = user.getFirstChild().getTextContent();
        }
        return nicknames;
    }
}
