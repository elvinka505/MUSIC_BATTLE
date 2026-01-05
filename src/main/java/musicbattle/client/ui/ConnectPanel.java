package musicbattle.client.ui;

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
            Socket socket = new Socket("localhost", 5050); //

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("CONNECT:" + nameField.getText()); //
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
            String message;
            while ((message = in.readLine()) != null) { //

                final String serverMessage = message;
                System.out.println("From server: " + serverMessage);

                if (serverMessage.equals("WAIT")) {
                    SwingUtilities.invokeLater(() ->
                            statusLabel.setText("⏳ Ожидание других игроков...")
                    );
                }

                else if (serverMessage.equals("START_GAME")) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("🎵 Игра началась!");
                        MainFrame.getInstance().showGamePanel();
                    });
                }

                else if (serverMessage.startsWith("RESULT")) {
                    boolean success = serverMessage.contains("SUCCESS");
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(
                                    this,
                                    success ? "🎉 Правильно!" : "❌ Неправильно",
                                    "Результат",
                                    JOptionPane.INFORMATION_MESSAGE
                            )
                    );
                }

                else if (serverMessage.startsWith("SCORE_UPDATE")) {
                    String[] parts = serverMessage.split(":", 2);
                    if (parts.length == 2 && !parts[1].isEmpty()) {
                        SwingUtilities.invokeLater(() ->
                                GamePanel.updateScore(parts[1])
                        );
                    }
                }

                // ФИНАЛЬНОЕ ОКНО
                else if (serverMessage.startsWith("GAME_OVER")) {

                    String[] parts = serverMessage.split(":", 3);
                    String winner = parts[1];
                    String rating = parts.length == 3 ? parts[2] : "";

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
