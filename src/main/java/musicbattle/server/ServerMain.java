package musicbattle.server;

import musicbattle.server.game.ScoreBoard;
import musicbattle.server.network.ClientHandler;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMain {

    public static final int PORT = 5050;
    public static final int MAX_PLAYERS = 2;

    private static final List<ClientHandler> clients = new ArrayList<>();
    private static final ScoreBoard scoreBoard = new ScoreBoard();

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) { //
            System.out.println("MusicBattle server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept(); //
                ClientHandler handler = new ClientHandler(socket, scoreBoard);
                clients.add(handler);
                handler.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<ClientHandler> getClients() {
        return clients;
    }

    public static synchronized void tryStartGame() {
        if (clients.size() == MAX_PLAYERS) {
            scoreBoard.reset(); // СБРОС СЧЁТА МАТЧА
            broadcast("START_GAME");
        }
    }

    public static void broadcastScore() {
        String data = scoreBoard.toProtocolString();
        if (!data.isEmpty()) {
            broadcast("SCORE_UPDATE:" + data);
        }
    }

    public static void broadcast(String msg) {
        for (ClientHandler client : clients) {
            if (client.getOut() != null) {
                client.getOut().println(msg);
            }
        }
    }

    public static ScoreBoard getScoreBoard() {
        return scoreBoard;
    }
}
