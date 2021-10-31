package hw7server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private final AuthService authService;

    private final List<ClientHandler> clients;

    public ChatServer() {
        clients = new ArrayList<>();
        authService = new SimpleAuthService();

        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("SERVER: Server start...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("SERVER: Client connected...");
                new ClientHandler(socket, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clients ");
        for (ClientHandler client : clients) {
            sb.append(client.getName() + " ");
        }
        broadcast(sb.toString());
    }

    public void broadcast(String msg) {
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        System.out.println("SERVER: Client " + clientHandler.getName() + " login...");
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        System.out.println("SERVER: Client " + clientHandler.getName() + " logout...");
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isNicknameBusy(String nickname) {
        for (ClientHandler client : clients) {
            if(client.getName().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    public void sendMsgToClient(ClientHandler from, String nickTo, String msg) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nickTo)) {
                o.sendMessage("от " + from.getName() + ": " + msg);
                from.sendMessage("клиенту " + nickTo + ": " + msg);
                return;
            }
        }
        from.sendMessage("Участника с ником " + nickTo + " нет в чат-комнате");
    }
}
