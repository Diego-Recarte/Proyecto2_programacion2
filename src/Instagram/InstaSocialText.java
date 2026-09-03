package Instagram;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;

/** Renderiza descripciones con hashtags y menciones reconocibles y navegables. */
final class InstaSocialText {

    static final Pattern TOKEN_PATTERN = Pattern.compile(
            "(?<![\\p{L}\\p{N}_])([#@])([\\p{L}\\p{N}_]+)");

    private InstaSocialText() {
    }

    static JEditorPane createCaption(String author, String caption, int width,
            Consumer<String> hashtagAction, Consumer<String> mentionAction) {
        JEditorPane pane = new JEditorPane();
        pane.setContentType("text/html");
        pane.setEditable(false);
        pane.setFocusable(false);
        pane.setOpaque(false);
        pane.setBackground(Color.BLACK);
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        pane.setText(toHtml(author, caption));
        pane.setCaretPosition(0);
        pane.addHyperlinkListener(event -> {
            if (event.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                pane.setCursor(new Cursor(Cursor.HAND_CURSOR));
                pane.setToolTipText("Abrir " + event.getDescription());
                return;
            }
            if (event.getEventType() == HyperlinkEvent.EventType.EXITED) {
                pane.setCursor(new Cursor(Cursor.TEXT_CURSOR));
                pane.setToolTipText(null);
                return;
            }
            if (event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
                return;
            }
            String link = event.getDescription();
            if (link == null) {
                return;
            }
            if (link.startsWith("hashtag:") && hashtagAction != null) {
                hashtagAction.accept(link.substring("hashtag:".length()));
            } else if (link.startsWith("mention:") && mentionAction != null) {
                mentionAction.accept(link.substring("mention:".length()));
            }
        });
        pane.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        pane.setSize(new Dimension(width, Short.MAX_VALUE));
        int height = Math.max(26, pane.getPreferredSize().height);
        pane.setPreferredSize(new Dimension(width, height));
        pane.setMaximumSize(new Dimension(width, height));
        return pane;
    }

    static int countHashtags(String text) {
        return countTokens(text, '#');
    }

    static int countMentions(String text) {
        return countTokens(text, '@');
    }

    private static int countTokens(String text, char prefix) {
        int count = 0;
        Matcher matcher = TOKEN_PATTERN.matcher(text == null ? "" : text);
        while (matcher.find()) {
            if (matcher.group(1).charAt(0) == prefix) {
                count++;
            }
        }
        return count;
    }

    private static String toHtml(String author, String caption) {
        StringBuilder html = new StringBuilder(256);
        html.append("<html><head><style>")
                .append("body{font-family:'Segoe UI';font-size:13px;color:#f3f3f3;margin:0;padding:0;}")
                .append("a{text-decoration:none;font-weight:bold;}")
                .append(".author{color:#ff5a1f;}.hashtag{color:#ff8b61;}.mention{color:#62b5ff;}")
                .append("</style></head><body>");

        String safeAuthor = author == null ? "" : author;
        html.append("<a class='author' href='mention:")
                .append(attribute(safeAuthor)).append("'>@")
                .append(escape(safeAuthor)).append("</a> ");

        String text = caption == null ? "" : caption;
        Matcher matcher = TOKEN_PATTERN.matcher(text);
        int previous = 0;
        while (matcher.find()) {
            html.append(escape(text.substring(previous, matcher.start())));
            boolean hashtag = "#".equals(matcher.group(1));
            String token = matcher.group(2);
            html.append("<a class='").append(hashtag ? "hashtag" : "mention")
                    .append("' href='").append(hashtag ? "hashtag:" : "mention:")
                    .append(attribute(token)).append("'>")
                    .append(hashtag ? "#" : "@").append(escape(token)).append("</a>");
            previous = matcher.end();
        }
        html.append(escape(text.substring(previous))).append("</body></html>");
        return html.toString();
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;")
                .replace("\r\n", "<br>").replace("\n", "<br>").replace("\r", "<br>");
    }

    private static String attribute(String value) {
        return escape(value).replace("<br>", "");
    }
}
