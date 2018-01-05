package com.github.maricn.logback;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class HttpURLConnectionReader {
    String readErrorBody(HttpURLConnection conn) throws IOException {
        try (InputStream is = conn.getErrorStream()) {
            final byte[] buffer = read(is, conn.getContentLength());
            if (conn.getContentEncoding() == null) {
                return new String(buffer);
            } else {
                return new String(buffer, conn.getContentEncoding());
            }
        }
    }

    private byte[] read(InputStream is, int contentSizeBytes) throws IOException {
        final byte[] content = new byte[contentSizeBytes];
        final byte[] buffer = new byte[1024];

        int offset = 0;
        do {
            final int read = is.read(buffer);
            if (read == -1) {
                break;
            }

            System.arraycopy(buffer, 0, content, offset, read);
            offset += read;
        } while (true);

        return content;
    }
}
