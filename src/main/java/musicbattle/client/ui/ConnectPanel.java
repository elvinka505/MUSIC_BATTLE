package musicbattle.client.ui;

import musicbattle.common.protocol.Message;
import musicbattle.common.protocol.MessageType;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectPanel extends JPanel {

    public static PrintWriter out;
    private BufferedReader in;

    private final JTextField nameField = new JTextField(10);
    private final JButton connectButton = new JButton("Подключиться 💗");
    private final JLabel statusLabel = new JLabel(" ");

    public ConnectPanel() {
        add(new JLabel("Имя:"));
        add(nameField);
        add(connectButton);
        add(statusLabel);

        connectButton.addActionListener(e -> connect());
    }

    private void connect() {
        try {
            Socket socket = new Socket("localhost", 5050);

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String connectMsg = Message.serialize(MessageType.CONNECT, nameField.getText());
            out.println(connectMsg);

            statusLabel.setText("⏳ Подключение...");
            connectButton.setEnabled(false);

            new Thread(this::listenServer).start();

        } catch (Exception ex) {
            statusLabel.setText("❌ Ошибка подключения");
            ex.printStackTrace();
        }
    }

    private void listenServer() {
        try {
            String rawMessage;
            while ((rawMessage = in.readLine()) != null) {

                Message message = Message.deserialize(rawMessage);
                if (message == null) {
                    System.err.println("Ошибка парсинга сообщения: " + rawMessage);
                    continue;
                }

                final MessageType messageType = message.getType();
                final String payload = message.getPayload();

                System.out.println("From server: " + messageType + " | payload: " + payload);

                if (messageType == MessageType.WAIT) {
                    SwingUtilities.invokeLater(() ->
                            statusLabel.setText("⏳ Ожидание других игроков...")
                    );
                }

                else if (messageType == MessageType.START_GAME) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("🎵 Игра началась!");
                        MainFrame.getInstance().showGamePanel();
                    });
                }

                else if (messageType == MessageType.RESULT) {
                    boolean success = payload.contains("SUCCESS");
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(
                                    this,
                                    success ? "🎉 Правильно!" : "❌ Неправильно",
                                    "Результат",
                                    JOptionPane.INFORMATION_MESSAGE
                            )
                    );
                }

                else if (messageType == MessageType.SCORE_UPDATE) {
                    SwingUtilities.invokeLater(() ->
                            GamePanel.updateScore(payload)
                    );
                }

                else if (messageType == MessageType.GAME_OVER) {
                    String[] parts = payload.split(":", 2);
                    String winner = parts.length > 0 ? parts[0] : "неизвестно";
                    String rating = parts.length > 1 ? parts[1] : "";

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                                this,
                                "🏆 Победитель: " + winner + "\n\nРейтинг:\n" +
                                        rating.replace(";", "\n"),
                                "Игра окончена",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        statusLabel.setText("🏁 Игра окончена");
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}