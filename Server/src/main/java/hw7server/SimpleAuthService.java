package hw7server;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {

    private static class UserData {
        private final String login;
        private final String password;
        private final String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }

    }

    private List<UserData> users;

    public SimpleAuthService() {
        users = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            users.add(new UserData("login" + i, "pass" + i, "nick" + i));
        }
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        for (UserData user : users) {
            if (login.equals(user.login) && password.equals(user.password)) {
                return user.nickname;
            }
        }
        return null;
    }
}
