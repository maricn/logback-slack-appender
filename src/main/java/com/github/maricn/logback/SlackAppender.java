package com.github.maricn.logback;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class SlackAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private final static String API_URL = "https://slack.com/api/chat.postMessage";
    private final static Layout<ILoggingEvent> defaultLayout = new LayoutBase<ILoggingEvent>() {
        public String doLayout(ILoggingEvent event) {
            return "-- [" + event.getLevel() + "]" +
                    event.getLoggerName() + " - " +
                    event.getFormattedMessage().replaceAll("\n", "\n\t");
        }
    };

    private final HttpURLConnectionReader httpURLConnectionReader = new HttpURLConnectionReader();

    private String webhookUri;
    private String token;
    private String channel;
    private String username;
    private String iconEmoji;
    private String iconUrl;
    private Boolean colorCoding = false;
    private Layout<ILoggingEvent> layout = defaultLayout;

    private int timeout = 30_000;

    @Override
    protected void append(final ILoggingEvent evt) {
        try {
            if (webhookUri != null && !webhookUri.isEmpty()) {
                sendMessageWithWebhookUri(evt);
            } else if (token != null && !token.isEmpty()) {
                sendMessageWithToken(evt);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            addError("Error posting log to Slack.com (" + channel + "): " + evt, ex);
        }
    }

    private void sendMessageWithWebhookUri(final ILoggingEvent evt) throws IOException {
        String[] parts = layout.doLayout(evt).split("\n", 2);

        Map<String, Object> message = new HashMap<>();
        message.put("channel", channel);
        message.put("username", username);
        message.put("icon_emoji", iconEmoji);
        message.put("icon_url", iconUrl);
        message.put("text", parts[0]);

        // Send the lines below the first line as an attachment.
        if (parts.length > 1 && parts[1].length() > 0) {
            Map<String, String> attachment = new HashMap<>();
            attachment.put("text", parts[1]);
            if (colorCoding) {
                attachment.put("color", colorByEvent(evt));
            }

            message.put("attachments", Collections.singletonList(attachment));
        }

        ObjectMapper objectMapper = new ObjectMapper();
        final byte[] bytes = objectMapper.writeValueAsBytes(message);

        postMessage(webhookUri, "application/json", bytes);
    }

    private void sendMessageWithToken(final ILoggingEvent evt) throws IOException {
        final StringWriter requestParams = new StringWriter();
        requestParams.append("token=").append(token).append("&");

        String[] parts = layout.doLayout(evt).split("\n", 2);
        requestParams.append("text=").append(URLEncoder.encode(parts[0], "UTF-8")).append('&');

        // Send the lines below the first line as an attachment.
        if (parts.length > 1 && parts[1].length() > 0) {
            Map<String, String> attachment = new HashMap<>();
            attachment.put("text", parts[1]);
            if (colorCoding) {
                attachment.put("color", colorByEvent(evt));
            }

            List<Map<String, String>> attachments = Collections.singletonList(attachment);
            String json = new ObjectMapper().writeValueAsString(attachments);
            requestParams.append("attachments=").append(URLEncoder.encode(json, "UTF-8")).append('&');
        }
        if (channel != null) {
            requestParams.append("channel=").append(URLEncoder.encode(channel, "UTF-8")).append('&');
        }
        if (username != null) {
            requestParams.append("username=").append(URLEncoder.encode(username, "UTF-8")).append('&');
        }
        if (iconEmoji != null) {
            requestParams.append("icon_emoji=").append(URLEncoder.encode(iconEmoji, "UTF-8"));
        }
        if (iconUrl != null) {
            requestParams.append("icon_url=").append(URLEncoder.encode(iconUrl, "UTF-8"));
        }

        final byte[] bytes = requestParams.toString().getBytes("UTF-8");

        postMessage(API_URL, "application/x-www-form-urlencoded", bytes);
    }

    private String colorByEvent(ILoggingEvent evt) {
        if (Level.ERROR.equals(evt.getLevel())) {
            return "danger";
        } else if (Level.WARN.equals(evt.getLevel())) {
            return "warning";
        } else if (Level.INFO.equals(evt.getLevel())) {
            return "good";
        }

        return "";
    }

    private void postMessage(String uri, String contentType, byte[] bytes) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) new URL(uri).openConnection();
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setFixedLengthStreamingMode(bytes.length);
        conn.setRequestProperty("Content-Type", contentType);
        conn.setInstanceFollowRedirects(false);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(bytes);
            os.flush();
        }

        // Read Slack response. This is necessary for Slack to accept the message. When a message
        // is logged immediately before the JVM exits, Slack will fail to send the "OK" response
        // (because the client closed the connection) and thus will discard the received message.
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            final String content = httpURLConnectionReader.readErrorBody(conn);
            addError("Slack POST request failed: " + responseCode + " " + content);
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

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrlArg) {
        this.iconUrl = iconUrlArg;
    }

    public Layout<ILoggingEvent> getLayout() {
        return layout;
    }

    public void setLayout(final Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getWebhookUri() {
        return webhookUri;
    }

    public void setWebhookUri(String webhookUri) {
        this.webhookUri = webhookUri;
    }

    public Boolean getColorCoding() {
        return colorCoding;
    }

    public void setColorCoding(Boolean colorCoding) {
        this.colorCoding = colorCoding;
    }
}
