package com.nsu.burym;

import com.nsu.burym.view.ChatWindow;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ParserConfigurationException {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        double displayCoeffX = (double) gd.getDisplayMode().getWidth() / 1920;
        double displayCoeffY = (double) gd.getDisplayMode().getHeight() / 1080;
        new ChatWindow(displayCoeffX, displayCoeffY);
    }
}
