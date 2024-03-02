package com.nsu.burym.parser;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;

public class ParseXml {
    public static String readAttr(@NotNull Document document) {
        Element root = document.getDocumentElement();
        String attr = null;
        if (root.getTagName().equals("command")) {
            attr = root.getAttribute("name");
        }
        return attr;
    }

    public static Document writeDocument(@NotNull String mes, @NotNull String attr, @NotNull String nick) throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("event");
        root.setAttribute("name", attr);
        if (attr.equals("message")) {
            Element value = doc.createElement("message");
            value.setTextContent(mes);
            Element nickname = doc.createElement("name");
            nickname.setTextContent(nick);
            root.appendChild(value);
            root.appendChild(nickname);
        }
        if (attr.equals("userlogin")) {
            Element value = doc.createElement("name");
            value.setTextContent(nick);
            root.appendChild(value);
        }
        if (attr.equals("userlogout")) {
            Element value = doc.createElement("name");
            value.setTextContent(nick);
            root.appendChild(value);
        }
        doc.appendChild(root);
        return doc;
    }

    public static String getNickname(@NotNull Document doc) {
        Element root = doc.getDocumentElement();
        return root.getChildNodes().item(0).getTextContent();
    }

    public static String getMessage(@NotNull Document doc) {
        Element root = doc.getDocumentElement();
        return root.getChildNodes().item(0).getTextContent();
    }

    public static Document writeUserList(@NotNull ArrayList<String> nicknames) throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("success");
        Element listUsers = doc.createElement("listusers");
        for (String nickname : nicknames) {
            Element user = doc.createElement("user");
            Element name = doc.createElement("name");
            name.setTextContent(nickname);
            user.appendChild(name);
            listUsers.appendChild(user);
        }
        root.appendChild(listUsers);
        doc.appendChild(root);
        return doc;
    }
}
