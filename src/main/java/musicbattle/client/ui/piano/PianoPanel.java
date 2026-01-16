package musicbattle.client.ui.piano;

import musicbattle.client.ui.ConnectPanel;
import musicbattle.common.protocol.Message;
import musicbattle.common.protocol.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class PianoPanel extends JPanel {

    private List<PianoKey> keys = new ArrayList<>();

    private List<int[]> notes = new ArrayList<>(); // [x, y, life]
    private Timer timer;

    public PianoPanel() {
        setPreferredSize(new Dimension(700, 220));
        setBackground(new Color(255, 240, 245)); // Светло-розовый фон

        int x = 20;
        String[] notesArray = {"C", "D", "E", "F", "G", "A", "B"};

        for (String note : notesArray) {
            keys.add(new PianoKey(x, 20, 80, 160, note));
            x += 80;
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                for (PianoKey key : keys) {
                    if (key.contains(e.getX(), e.getY())) {
                        key.setPressed(true);

                        int cx = e.getX();
                        int cy = e.getY();
                        PianoPanel.this.notes.add(new int[]{cx, cy, 30});

                        if (timer == null) {
                            timer = new Timer(30, ev -> {
                                for (int[] n : PianoPanel.this.notes) {
                                    n[1] -= 3; // y вверх
                                    n[2]--;    // life--
                                }
                                PianoPanel.this.notes.removeIf(n -> n[2] <= 0);

                                if (PianoPanel.this.notes.isEmpty()) {
                                    timer.stop();
                                    timer = null;
                                }
                                repaint();
                            });
                            timer.start();
                        }

                        System.out.println("🎹 Нажата клавиша: " + key.getNote());

                        if (ConnectPanel.out != null) {
                            String noteMsg = Message.serialize(MessageType.NOTE_INPUT, key.getNote());
                            ConnectPanel.out.println(noteMsg);
                        }

                        repaint();
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

        // Рисуем клавиши пианино
        for (PianoKey key : keys) {
            key.draw(g);
        }

        // РИСУЕМ ЛЕТЯЩИЕ НОТЫ (требование ТЗ - использование Graphics)
        for (int[] n : PianoPanel.this.notes) {
            // Альфа затухает по мере приближения к смерти
            int alpha = Math.min(255, n[2] * 8); // 30..0 → 240..0
            g.setColor(new Color(255, 105, 180, alpha)); // Розовый с прозрачностью
            g.fillOval(n[0] - 8, n[1] - 8, 16, 16);
        }
    }
}