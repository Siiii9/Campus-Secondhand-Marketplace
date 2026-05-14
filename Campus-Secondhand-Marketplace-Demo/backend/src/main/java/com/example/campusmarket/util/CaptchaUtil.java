package com.example.campusmarket.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class CaptchaUtil {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_COUNT = 4;
    private static final int LINE_COUNT = 10;
    private static final String CODE_CHAR = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final Random random = new Random();

    public static BufferedImage generateCaptcha(StringBuilder code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(getRandomColor(200, 250));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setFont(new Font("Arial", Font.BOLD, 28));

        for (int i = 0; i < LINE_COUNT; i++) {
            g.setColor(getRandomColor(160, 200));
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        for (int i = 0; i < CODE_COUNT; i++) {
            char c = CODE_CHAR.charAt(random.nextInt(CODE_CHAR.length()));
            code.append(c);
            g.setColor(getRandomColor(20, 130));
            g.drawString(String.valueOf(c), 25 * i + 10, 30);
        }

        for (int i = 0; i < 30; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            g.setColor(getRandomColor(100, 200));
            g.drawOval(x, y, 1, 1);
        }

        g.dispose();
        return image;
    }

    private static Color getRandomColor(int min, int max) {
        int r = min + random.nextInt(max - min);
        int g = min + random.nextInt(max - min);
        int b = min + random.nextInt(max - min);
        return new Color(r, g, b);
    }
}