package musicbattle.client.ui.piano;

import musicbattle.client.ui.ConnectPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class PianoPanel extends JPanel {

    private List<PianoKey> keys = new ArrayList<>();

    public PianoPanel() {
        setPreferredSize(new Dimension(700, 220));
        setBackground(new Color(255, 240, 245));

        int x = 20;
        String[] notes = {"C", "D", "E", "F", "G", "A", "B"};

        for (String note : notes) {
            keys.add(new PianoKey(x, 20, 80, 160, note));
            x += 80;
        }

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                for (PianoKey key : keys) {
                    if (key.contains(e.getX(), e.getY())) {
                        key.setPressed(true);
                        repaint();

                        System.out.println("Pressed note: " + key.getNote());

                        // ОТПРАВКА НА СЕРВЕР
                        if (ConnectPanel.out != null) {
                            ConnectPanel.out.println("NOTE_INPUT:" + key.getNote());
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                for (PianoKey key : keys) {
                    key.setPressed(false);
                }
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (PianoKey key : keys) {
            key.draw(g);
        }
    }
}
