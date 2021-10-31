package hw7server;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {
    private final Socket socket;
    private final ChatServer server;
    private final DataInputStream in;
    private final DataOutputStream out;

    private String name;

    public ClientHandler(Socket socket, ChatServer server) {
        try {
            this.name = "";
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    authenticate();
                    readMessages();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Не могу создать обработчик для клиента", e);
        }
    }

    private void authenticate() {
        while (true) {
            try {
                final String str = in.readUTF();
                if (str.startsWith("/auth")) { // /auth login1 pass1

                    final String[] split = str.split("\\s");
                    final String login = split[1];
                    final String pass = split[2];
                    final String nickname = server.getAuthService().getNicknameByLoginAndPassword(login, pass);
                    if (nickname != null) {
                        if (!server.isNicknameBusy(nickname)) {
                            sendMessage("/authok " + nickname);
                            this.name = nickname;
                            server.broadcast("SERVER: Пользователь " + nickname + " зашел в чат");
                            server.subscribe(this);
                            break;
                        } else {
                            sendMessage("SERVER: Уже произведен вход в учетную запись");
                        }
                    } else {
                        sendMessage("SERVER: Неверные логин / пароль");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    private void closeConnection() {
        try {
            server.unsubscribe(this);
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String msg) {
        try {
            System.out.println("SERVER: Send message to " + name + ": " + msg);
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void readMessages() {
        try {
            while (true) {
                final String strFromClient = in.readUTF();
                if (strFromClient.startsWith("/")) {
                    if (strFromClient.equals("/end")) {
                        break;
                    }
                    if (strFromClient.startsWith("/w ")) {
                        String[] tokens = strFromClient.split("\\s");
                        String nick = tokens[1];
                        String msg = strFromClient.substring(4 + nick.length());
                        server.sendMsgToClient(this, nick, msg);
                    }
                    continue;
                }
                server.broadcast(name + ": " + strFromClient);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

}
