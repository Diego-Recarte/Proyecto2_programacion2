package Instagram.sockets;

/** Punto de entrada para ejecutar el servidor en otra computadora de la red. */
public final class ChatServerMain {

    private ChatServerMain() {
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : ChatServer.DEFAULT_PORT;
        ChatServer server = new ChatServer(port);
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
        System.out.println("Servidor de chat escuchando en el puerto " + port);
        Thread.currentThread().join();
    }
}
