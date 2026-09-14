package Instagram;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Vista manual de INSTA+ con autenticacion en el servidor. */
public final class InstaFeedPreview {

    private InstaFeedPreview() {
    }

    public static void main(String[] args) {
        String username = args.length > 0 ? args[0] : "RED";
        SwingUtilities.invokeLater(() -> {
            try {
                instaManager manager = new instaManager();
                instaController.getInstance().setInsta(manager);


                JFrame frame = new JFrame("INSTA+ Feed Preview — " + username);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setResizable(false);
                frame.setContentPane(new InstaLoginUI());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}
