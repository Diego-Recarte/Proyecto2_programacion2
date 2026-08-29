package Instagram.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/** Inicia un servidor embebido cuando la aplicación trabaja en localhost. */
public final class LocalChatServer {

    private static ChatServer server;

    private LocalChatServer() {
    }

    public static synchronized void ensureAvailable(String host, int port) throws IOException {
        if (!isLocal(host) || canConnect(host, port)) {
            return;
        }

        if (server == null || !server.isRunning()) {
            server = new ChatServer(port);
            server.start();
        }
    }

    private static boolean canConnect(String host, int port) {
        try (Socket probe = new Socket()) {
            probe.connect(new InetSocketAddress(host, port), 300);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private static boolean isLocal(String host) {
        return "localhost".equalsIgnoreCase(host)
                || "127.0.0.1".equals(host)
                || "::1".equals(host);
    }
}
