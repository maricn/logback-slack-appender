package com.github.maricn.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SlackAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private final static String API_URL = "https://slack.com/api/chat.postMessage";
    private static Layout<ILoggingEvent> defaultLayout = new LayoutBase<ILoggingEvent>() {
        public String doLayout(ILoggingEvent event) {
            StringBuffer sbuf = new StringBuffer(128);
            sbuf.append("-- [");
            sbuf.append(event.getLevel());
            sbuf.append("]");
            sbuf.append(event.getLoggerName());
            sbuf.append(" - ");
            sbuf.append(event.getFormattedMessage().replaceAll("\n", "\n\t"));
            return sbuf.toString();
        }
    };

    private String token;
    private String channel;
    private String username;
    private String iconEmoji;
    private Layout<ILoggingEvent> layout = defaultLayout;

    @Override
    protected void append(final ILoggingEvent evt) {
        try {
            final URL url = new URL(API_URL);

            final StringWriter w = new StringWriter();
            w.append("token=").append(token).append("&");
            w.append("text=").append(URLEncoder.encode(layout.doLayout(evt), "UTF-8")).append('&');
            if (channel != null) {
                w.append("channel=").append(URLEncoder.encode(channel, "UTF-8")).append('&');
            }
            if (username != null) {
                w.append("username=").append(URLEncoder.encode(username, "UTF-8")).append('&');
            }
            if (iconEmoji != null) {
                w.append("icon_emoji=").append(URLEncoder.encode(iconEmoji, "UTF-8"));
            }

            final byte[] bytes = w.toString().getBytes("UTF-8");

            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            final OutputStream os = conn.getOutputStream();
            os.write(bytes);

            os.flush();
            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            addError("Error posting log to Slack.com (" + channel + "): " + evt, ex);
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(final String channel) {
        this.channel = channel;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIconEmoji() {
        return iconEmoji;
    }

    public void setIconEmoji(String iconEmojiArg) {
        this.iconEmoji = iconEmojiArg;
        if (iconEmoji != null && !iconEmoji.isEmpty() && iconEmoji.startsWith(":") && !iconEmoji.endsWith(":")) {
            iconEmoji += ":";
        }
    }

    public Layout<ILoggingEvent> getLayout() {
        return layout;
    }

    public void setLayout(final Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }
}
