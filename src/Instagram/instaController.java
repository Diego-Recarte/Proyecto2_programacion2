package Instagram;

/** Clientes autenticados por usuario para no mezclar ventanas de INSTA+. */
public final class instaController {
    private static final instaController INSTANCE = new instaController();
    private volatile instaManager insta;
    private final java.util.Map<String, instaManager> sessions = new java.util.concurrent.ConcurrentHashMap<>();
    private instaController() { }
    public static instaController getInstance() { return INSTANCE; }
    public void setInsta(instaManager manager) { insta = manager; }
    public instaManager getInsta() { return insta; }
    public instaManager getInsta(String user) { return sessions.getOrDefault(user, insta); }
    void remember(String user, instaManager manager) { sessions.put(user, manager); }
    void forget(String user, instaManager manager) { if (user != null) sessions.remove(user, manager); }
}
