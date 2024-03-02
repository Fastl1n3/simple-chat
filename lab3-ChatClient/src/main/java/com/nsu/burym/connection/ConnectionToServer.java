package com.nsu.burym.connection;

import com.nsu.burym.view.ChatWindow;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ConnectionToServer {
    private final int PORT = 8888;
    private final SocketChannel client;
    private final ClientReader clientReader;
    private final ChatWindow chatWindow;

    public ConnectionToServer(@NotNull ChatWindow chatWindow) throws IOException {
        client = SocketChannel.open(new InetSocketAddress("localhost", PORT));
        this.chatWindow = chatWindow;
        clientReader = new ClientReader();
        clientReader.start();
    }

    private class ClientReader extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    ByteBuffer lenBuffer = ByteBuffer.allocate(4);
                    client.read(lenBuffer);
                    ByteBuffer buffer = ByteBuffer.allocate(lenBuffer.getInt(0));
                    client.read(buffer);
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer.array()));
                    Document doc = (Document) ois.readObject();
                    chatWindow.addMessage(doc);
                }
            } catch (IOException | ClassNotFoundException e) {
                closeConnection();
            }
        }
    }

    public void send(@NotNull Document mes) {
        try {
            ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
            for (int i = 0; i < 4; i++) {
                bufferStream.write(0);
            }
            ObjectOutputStream out = new ObjectOutputStream(bufferStream);
            out.writeObject(mes);
            out.flush();
            out.close();
            ByteBuffer buffer = ByteBuffer.wrap(bufferStream.toByteArray());
            buffer.putInt(0, bufferStream.size() - 4);
            client.write(buffer);
            bufferStream.close();
        }
        catch (IOException e) {
            System.out.println("Can't send message: " + e.getMessage());
            clientReader.interrupt();
            closeConnection();
        }
    }

    public void setStop() {
        clientReader.interrupt();
        closeConnection();
    }

    public void closeConnection() {
        try {
            client.close();
        } catch (IOException e) {
            System.out.println("CloseConnectException: " + e.getMessage());
        }
    }
}
