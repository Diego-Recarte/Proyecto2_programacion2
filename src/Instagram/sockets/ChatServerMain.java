package Instagram.sockets;

/** Entrada compatible que inicia ahora todos los servicios de INSTA+. */
public final class ChatServerMain {
    private ChatServerMain() { }
    public static void main(String[] args) throws Exception {
        if (args.length > 0) System.setProperty("instagram.chat.port", args[0]);
        Instagram.sockets.InstaServer.main(new String[0]);
    }
}
