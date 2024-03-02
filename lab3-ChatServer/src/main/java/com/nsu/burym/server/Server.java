package com.nsu.burym.server;

import com.nsu.burym.parser.ParseXml;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private final int PORT = 8888;
    private final ArrayList<Document> history = new ArrayList<>();
    private final ServerSocketChannel serSockChan;
    private final Selector selector;
    private final ArrayList<String> nicknames = new ArrayList<>();
    private boolean list = false;

    public Server() throws IOException {
        serSockChan = ServerSocketChannel.open();
        serSockChan.socket().bind(new InetSocketAddress("localhost", PORT));
        selector = Selector.open();
        serSockChan.configureBlocking(false);
        serSockChan.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server is started.");
    }

    static class Attachment {
        private boolean reading = false;
        private ByteBuffer atachBuffer;
        private String nickname = "unknown";
    }

    public void start() {
        while (true) {
            try {
                selector.select(); // дожидаемся события возвращается после того как выбран хотя бы один канал
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> i = selectedKeys.iterator();
                while (i.hasNext()) {
                    SelectionKey key = i.next();
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        SocketChannel client = serSockChan.accept();
                        System.out.println("Connect " + client.socket().getInetAddress().getHostAddress() + " " + client.socket().getPort());
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ, new Attachment());
                        loadHistory(client);
                    }
                    else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel)key.channel();
                        try {
                            Document doc = recv(key);
                            Attachment attachment = (Attachment) key.attachment();
                            if (attachment.reading) { // если не все успели считалать
                                i.remove();
                                continue;
                            }
                            doc = handleDoc(doc, key);
                            if (list) {
                                send(client, doc);
                                System.out.println("User list has sent to " + client.socket().getInetAddress().getHostAddress() + " " + client.socket().getPort());
                                list = false;
                            } else {
                                sendMessage(doc);
                            }
                        }
                        catch (IOException e) {
                            removeClient(key);
                        }
                    }
                    i.remove();
                }
                selector.selectedKeys().clear();
            }
            catch (IOException e) {
                System.out.println("ERROR IO from start");
            } catch (ParserConfigurationException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void removeClient(SelectionKey key) {
        Attachment attachment = (Attachment) key.attachment();
        nicknames.remove(attachment.nickname);
        SocketChannel client = (SocketChannel) key.channel();
        System.out.println("Disconnect " + client.socket().getInetAddress().getHostAddress() + " " + client.socket().getPort());
        try {
            key.channel().close();
        } catch (IOException e) {
            System.out.println("Can't close socketChannel");
        }
    }
    private void sendMessage(Document doc) throws IOException {
        addHistory(doc);
        for (SelectionKey key: selector.keys()) {
            if (key.isReadable()) {
                try {
                    send((SocketChannel) key.channel(), doc);
                }
                catch (IOException e) {
                    removeClient(key);
                }
            }
        }
        System.out.println("Message has sent all");
    }

    private Document recv(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        Document doc = null;
        Attachment attachment = (Attachment) key.attachment();
        try {
            if (!attachment.reading) { //создание нового буфера
                ByteBuffer lenBuffer = ByteBuffer.allocate(4);
                while (lenBuffer.hasRemaining()) {
                 client.read(lenBuffer);
                }
                attachment.atachBuffer = ByteBuffer.allocate(lenBuffer.getInt(0));
                key.attach(attachment);
            }
            client.read(attachment.atachBuffer);
            if (attachment.atachBuffer.hasRemaining()) { //если осталось что считать, то выходим
                attachment.reading = true;
                return doc;
            }
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(attachment.atachBuffer.array()));
            doc = (Document) ois.readObject();
            attachment.reading = false;
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR from recv");
        }
        return doc;
    }

    private void send(@NotNull SocketChannel client, @NotNull Document doc) throws IOException {
            ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
            for (int j = 0; j < 4; j++) {
                bufferStream.write(0);
            }
            ObjectOutputStream out = new ObjectOutputStream(bufferStream);
            out.writeObject(doc);
            out.flush();
            out.close();
            ByteBuffer buffer = ByteBuffer.wrap(bufferStream.toByteArray());
            buffer.putInt(0, bufferStream.size() - 4);
            while (buffer.hasRemaining()) {
                client.write(buffer);
            }
            bufferStream.close();
    }

    private Document handleDoc(@NotNull Document doc, SelectionKey key) throws ParserConfigurationException {
        String ans = ParseXml.readAttr(doc);
        String message = "";
        Document document = null;
        if (ans.equals("login")) {
            String nickname = ParseXml.getNickname(doc);
            nicknames.add(nickname);
            ((Attachment) key.attachment()).nickname = nickname;
            document = ParseXml.writeDocument("", "userlogin", nickname);
        }
        if (ans.equals("message")) {
            message = ParseXml.getMessage(doc);
            Attachment attachment = (Attachment) key.attachment();
            document = ParseXml.writeDocument(message, "message", attachment.nickname);
        }
        if (ans.equals("list")) {
            list = true;
            document = ParseXml.writeUserList(nicknames);
        }
        if (ans.equals("logout")) {
            Attachment attachment = (Attachment) key.attachment();
            document = ParseXml.writeDocument(message, "userlogout", attachment.nickname);
        }
        return document;
    }

    private void addHistory(Document doc) {
        if (history.size() >= 20) {
            history.remove(0);
        }
        history.add(doc);
    }

    private void loadHistory(SocketChannel client) {
        try {
            for (Document doc: history) {
                send(client, doc);
            }
        } catch (IOException e) {
            System.out.println("Load history was failed");
        }
    }

    public void closeConnection() {
        try {
            serSockChan.close();
            selector.close();
        } catch (IOException e) {
            System.out.println("CloseConnect: " + e.getMessage());
        }
    }
}
