package musicbattle.server.network;

import musicbattle.server.ServerMain;
import musicbattle.server.game.ScoreBoard;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler extends Thread {

    private static final List<String> MELODY = List.of("C", "D", "E");

    private final Socket socket;
    private final ScoreBoard scoreBoard;

    private PrintWriter out;
    private String playerName;
    private final List<String> inputNotes = new ArrayList<>();

    public ClientHandler(Socket socket, ScoreBoard scoreBoard) {
        this.socket = socket;
        this.scoreBoard = scoreBoard;
    }

    public PrintWriter getOut() {
        return out;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            out = new PrintWriter(socket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {

                if (message.startsWith("CONNECT")) {
                    playerName = message.split(":", 2)[1];
                    scoreBoard.addPlayer(playerName);

                    int connected = ServerMain.getClients().size();
                    if (connected < ServerMain.MAX_PLAYERS) {
                        out.println("WAIT");
                    }

                    ServerMain.tryStartGame(); // проверка старта
                }

                if (message.startsWith("NOTE_INPUT")) { //
                    String note = message.split(":", 2)[1];
                    inputNotes.add(note);

                    if (inputNotes.size() == MELODY.size()) {
                        checkMelody();
                        inputNotes.clear();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkMelody() {

        if (inputNotes.equals(MELODY)) {
            scoreBoard.addPoint(playerName);
            out.println("RESULT:SUCCESS");
        } else {
            out.println("RESULT:FAIL");
        }

        ServerMain.broadcastScore();

        if (scoreBoard.hasWinner()) {
            String winner = scoreBoard.getWinner();
            String rating = scoreBoard.toProtocolString();
            ServerMain.broadcast("GAME_OVER:" + winner + ":" + rating);
        }
    }
}
