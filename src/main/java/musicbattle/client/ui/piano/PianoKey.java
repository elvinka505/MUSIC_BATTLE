package musicbattle.client.ui.piano;

import java.awt.*;

public class PianoKey {

    private Rectangle bounds;
    private String note;
    private boolean pressed = false;

    public PianoKey(int x, int y, int width, int height, String note) {
        this.bounds = new Rectangle(x, y, width, height);
        this.note = note;
    }

    public void draw(Graphics g) {
        if (pressed) {
            g.setColor(new Color(255, 182, 193)); // розовый при нажатии
        } else {
            g.setColor(Color.WHITE);
        }

        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.setColor(Color.BLACK);
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

        g.drawString(note,
                bounds.x + bounds.width / 2 - 5,
                bounds.y + bounds.height - 10);
    }

    public boolean contains(int x, int y) {
        return bounds.contains(x, y);
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    public String getNote() {
        return note;
    }
}
