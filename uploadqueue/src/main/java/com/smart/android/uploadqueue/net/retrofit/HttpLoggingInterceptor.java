package com.smart.android.uploadqueue.net.retrofit;

import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * @author wangxin
 * @since 20190721
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class HttpLoggingInterceptor implements Interceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static final int HTTP_CONTINUE = 100;
    /**
     * HTTP Status-Code 204: No Content.
     */
    public static final int HTTP_NO_CONTENT = 204;
    /**
     * HTTP Status-Code 304: Not Modified.
     */
    public static final int HTTP_NOT_MODIFIED = 304;

    public enum Level {
        /**
         * No logs.
         */
        NONE,

        /**
         * Logs request and response lines.
         * <p>
         * <p>Example:
         * <pre>{@code
         * ╔══════════╤════════════════════════════════════════════════════════════════════════════
         * ║Request   │
         * ║url       │GET http://www.example.com/ HTTP/1.1
         * ║header    │<set level as Level.HEADERS or Level.BODY to watch...>
         * ║body      │<no request body...>
         * ╟──────────┼────────────────────────────────────────────────────────────────────────────
         * ║Response  │<Spent on network(ms): 116>
         * ║http.code │200 OK
         * ║header    │Content-Type: text/html; charset=utf-8
         * ║body      │<set level as Level.BODY to watch...>
         * ╚══════════╧════════════════════════════════════════════════════════════════════════════
         * }</pre>
         */
        BASIC,

        /**
         * Logs request and response lines and their respective headers.
         * <p>
         * <p>Example:
         * <pre>{@code
         * ╔══════════╤════════════════════════════════════════════════════════════════════════════
         * ║Request   │
         * ║url       │GET http://www.example.com/ HTTP/1.1
         * ║header    │Host: www.example.com
         * ║          │Connection: Keep-Alive
         * ║          │Accept-Encoding: gzip
         * ║          │User-Agent: okhttp/3.2.0
         * ║body      │<no request body...>
         * ╟──────────┼────────────────────────────────────────────────────────────────────────────
         * ║Response  │<Spent on network(ms): 58>
         * ║http.code │200 OK
         * ║header    │Date: Fri, 29 Jul 2016 09:47:20 GMT
         * ║          │Content-Type: text/html; charset=utf-8
         * ║          │Expires: Fri, 29 Jul 2016 09:46:46 GMT
         * ║          │Content-Encoding: gzip
         * ║          │Transfer-Encoding: chunked
         * ║body      │<set level as Level.BODY to watch...>
         * ╚══════════╧════════════════════════════════════════════════════════════════════════════
         * }</pre>
         */
        HEADERS,

        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         * <p>
         * <p>Example:
         * <pre>{@code
         * ╔══════════╤════════════════════════════════════════════════════════════════════════════
         * ║Request   │
         * ║url       │GET http://www.example.com/ HTTP/1.1
         * ║header    │Host: www.example.com
         * ║          │Connection: Keep-Alive
         * ║          │Accept-Encoding: gzip
         * ║          │User-Agent: okhttp/3.2.0
         * ║body      │<no request body...>
         * ╟──────────┼────────────────────────────────────────────────────────────────────────────
         * ║Response  │<Spent on network(ms): 58>
         * ║http.code │200 OK
         * ║header    │Date: Fri, 29 Jul 2016 09:47:20 GMT
         * ║          │Content-Type: text/html; charset=utf-8
         * ║          │Expires: Fri, 29 Jul 2016 09:46:46 GMT
         * ║          │Content-Encoding: gzip
         * ║          │Transfer-Encoding: chunked
         * ║body      │<html>Hello everyone</html>
         * ╚══════════╧════════════════════════════════════════════════════════════════════════════
         * }</pre>
         */
        BODY
    }

    public interface Logger {
        void log(String message);

        /**
         * A {@link Logger} defaults output appropriate for the current platform.
         */
        Logger DEFAULT = new Logger() {

            private final String tag = HttpLoggingInterceptor.class.getSimpleName();

            @Override
            public void log(String message) {
                Log.d(tag, message);
            }
        };
    }

    private static String prettyPrintString(String text) {
        try {
            if (text.startsWith("{") && text.trim().endsWith("}")) {
                JSONObject jsonObject = new JSONObject(text);
                return jsonObject.toString(2);
            }
        } catch (Throwable ignore) {
        }
        return text;
    }

    private static String urlToString(String url) {
        int index = url.indexOf('?');
        if (index < 0) return url;
        if (index >= url.length() - 1) return url.substring(0, index);

        String query = url.substring(index + 1);
        query = query.replace("&", " &\n");
        try {
            return url.substring(0, index) + "?\n" + URLDecoder.decode(query, UTF8.name()); // RFC
        } catch (Throwable t) {
            return "<error occur in decoding url query: " + t + "...>"; // should never
        }
    }

    private static String headersToString(
            Headers headers, Set<String> skippedHeaders, boolean logHeaders) {
        if (logHeaders) {
            StringBuilder sb = new StringBuilder(533);
            for (int i = 0, size = headers.size(); i < size; i++) {
                if (!skippedHeaders.contains(headers.name(i))) {
                    sb.append(headers.name(i)).append(": ").append(headers.value(i)).append("\n");
                }
            }
            if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder(233);
            final String contentType = headers.get("Content-Type");
            if (contentType != null) {
                sb.append("Content-Type: ").append(contentType).append("\n");
            }
            final String contentLength = headers.get("Content-Length");
            if (contentLength != null) {
                sb.append("Content-Length: ").append(contentLength).append("\n");
            }

            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
                return sb.toString();
            } else {
                return "<set level as Level.HEADERS or Level.BODY to watch...>";
            }
        }
    }

    /**
     * Returns true if the response must have a (possibly 0-length) body. See RFC 2616 section 4.3.
     */
    private static boolean hasBody(Response response) {
        // HEAD requests never yield a body regardless of the response headers.
        if ("HEAD".equals(response.request().method())) {
            return false;
        }

        int responseCode = response.code();
        if ((responseCode < HTTP_CONTINUE || responseCode >= 200)
                && responseCode != HTTP_NO_CONTENT
                && responseCode != HTTP_NOT_MODIFIED) {
            return true;
        }

        // If the Content-Length or Transfer-Encoding headers disagree with the
        // response code, the response is malformed. For best compatibility, we
        // honor the headers.
        //noinspection RedundantIfStatement: for more clear logic
        if (Long.parseLong(response.header("Content-Length", "-1")) != -1
                || "chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) {
            return true;
        }

        return false;
    }

    private static boolean bodyEncoded(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null && !"identity".equalsIgnoreCase(contentEncoding);
    }

    public static <T extends OutputStream> T copy(InputStream in, T out) throws IOException {
        byte[] buffer = new byte[4096];
        int n;
        while (-1 != (n = in.read(buffer))) {
            out.write(buffer, 0, n);
        }
        out.flush();
        return out;
    }

    private static String readBodyAsString(Request request, int maxBytes) throws IOException {
        final RequestBody requestBody = request.body();
        final boolean hasRequestBody = requestBody != null;
        if (!hasRequestBody) return "<no request body...>";
        if (maxBytes <= 0) return "<set level as Level.BODY to watch...>";
        if (requestBody.contentLength() > maxBytes)
            return "<response body is larger than " + maxBytes + " bytes...>";

        Charset charset = UTF8;
        MediaType contentType = requestBody.contentType();
        if (contentType != null) {
            charset = contentType.charset(charset);
        }

        Buffer buffer = new Buffer();
        try {
            requestBody.writeTo(buffer);
        } catch (Throwable t) {
            return "<error occur in buffering request body: " + t + "...>";
        }

        String requestBodyString;
        try {
            requestBodyString = buffer.readString(charset);
        } catch (Throwable t) {
            return "<error occur in reading request body: " + t + "...>";
        }

        if ("application/x-www-form-urlencoded".equals(String.valueOf(contentType))) {
            requestBodyString = requestBodyString.replace("&", " &\n");
            try {
                requestBodyString = URLDecoder.decode(requestBodyString, charset.name());
            } catch (Throwable t) {
                return "<error occur in decoding requestBodyString: " + t + "...>";
            }
        }

        return requestBodyString;
    }

    private static String readBodyAsString(Response response, int maxBytes) throws IOException {
        final ResponseBody responseBody = response.body();
        if (!hasBody(response)) return "<no response body...>";
        if (maxBytes <= 0) return "<set level as Level.BODY to watch...>";

        BufferedSource source = responseBody.source();
        source.request(maxBytes == Integer.MAX_VALUE ? Integer.MAX_VALUE : (maxBytes + 1));
        if (source.buffer().size() > maxBytes)
            return "<response body is larger than " + maxBytes + " bytes...>";

        Charset charset = UTF8;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            charset = contentType.charset(charset);
        }

        Buffer buffer = source.buffer();
        if (!bodyEncoded(response.headers())) {
            try {
                return buffer.clone().readString(charset);
            } catch (Throwable t) {
                return "<error occur in reading response body: " + t + "...>";
            }
        }

        final String contentEncoding = response.header("Content-Encoding");
        if ("gzip".equalsIgnoreCase(contentEncoding)) {
            final Buffer encodedBuffer = buffer.clone();
            try {
                return copy(new GZIPInputStream(encodedBuffer.inputStream()),
                        new ByteArrayOutputStream((int) encodedBuffer.size()))
                        .toString(charset.name());
            } catch (Throwable t) {
                return "<error occur in reading encoded response body: " + t + "...>";
            }
        }

        return "<not supported Content-Encoding of body: " + contentEncoding + "...>";
    }


    private final Logger mLogger;

    private volatile Level mLevel = Level.BASIC;
    private volatile int mRequestBodyMaxLogBytes = 3 * 1024;
    private volatile int mResponseBodyMaxLogBytes = 8 * 1024;
    private volatile Set<String> mSkippedHeaders = new HashSet<>();
    private volatile boolean mLineAutoWrap = false;

    public HttpLoggingInterceptor() {
        this(Logger.DEFAULT);
    }

    public HttpLoggingInterceptor(Logger logger) {
        mLogger = logger;
    }

    public Level getLevel() {
        return mLevel;
    }

    public HttpLoggingInterceptor setLevel(Level level) {
        if (level == null) throw new NullPointerException("level == null. Use Level.NONE instead.");
        mLevel = level;
        return this;
    }

    public boolean isLineAutoWrap() {
        return mLineAutoWrap;
    }

    public HttpLoggingInterceptor setLineAutoWrap(boolean lineAutoWrap) {
        mLineAutoWrap = lineAutoWrap;
        return this;
    }

    public int getRequestBodyMaxLogBytes() {
        return mRequestBodyMaxLogBytes;
    }

    public HttpLoggingInterceptor setRequestBodyMaxLogBytes(int requestBodyMaxLogBytes) {
        mRequestBodyMaxLogBytes = requestBodyMaxLogBytes;
        return this;
    }

    public int getResponseBodyMaxLogBytes() {
        return mResponseBodyMaxLogBytes;
    }

    public HttpLoggingInterceptor setResponseBodyMaxLogBytes(int responseBodyMaxLogBytes) {
        mResponseBodyMaxLogBytes = responseBodyMaxLogBytes;
        return this;
    }

    public Set<String> getSkippedHeaders() {
        return Collections.unmodifiableSet(mSkippedHeaders);
    }

    public HttpLoggingInterceptor setSkippedHeaders(Collection<String> skippedHeaders) {
        mSkippedHeaders = new HashSet<>(skippedHeaders);
        return this;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Level level = mLevel;
        final Request request = chain.request();
        if (level == Level.NONE) return chain.proceed(request);

        final boolean logBody = level == Level.BODY;
        final boolean logHeaders = logBody || level == Level.HEADERS;

        final Connection connection = chain.connection();
        final Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;
        final String protocolStr = protocol == Protocol.HTTP_1_0 ? "HTTP/1.0" : "HTTP/1.1";

        final long startNs = System.nanoTime();
        final Response response;
        try {
            response = chain.proceed(request);
        } catch (Throwable t) {
            final long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(533);
            PrintStream ps = new PrintStream(byteArrayOutputStream);
            t.printStackTrace(ps);
            synchronized (HttpLoggingInterceptor.class) {
                new SimpleTableStringPrinter(mLogger, mLineAutoWrap)
                        .printRow("Request", request.method() + ' ' + protocolStr)
                        .printRow("url", urlToString(request.url().toString()))
                        .printRow("header", headersToString(request.headers(), mSkippedHeaders, logHeaders))
                        .printRow("body", prettyPrintString(
                                readBodyAsString(request, logBody ? mRequestBodyMaxLogBytes : 0)))
                        .printRow()
                        .printRow("Response", "<Spent on network(ms): " + tookMs + ">")
                        .printRow("throwable", byteArrayOutputStream.toString(UTF8.name()))
                        .end();
            }
            throw t;  // rethrow
        }
        final long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        synchronized (HttpLoggingInterceptor.class) {
            new SimpleTableStringPrinter(mLogger, mLineAutoWrap)
                    .printRow("Request", request.method() + ' ' + protocolStr)
                    .printRow("url", urlToString(request.url().toString()))
                    .printRow("header", headersToString(request.headers(), mSkippedHeaders, logHeaders))
                    .printRow("body", prettyPrintString(
                            readBodyAsString(request, logBody ? mRequestBodyMaxLogBytes : 0)))
                    .printRow()
                    .printRow("Response", "<Spent on network(ms): " + tookMs + ">")
                    .printRow("http.code", response.code() + " " + response.message())
                    .printRow("header", headersToString(response.headers(), mSkippedHeaders, logHeaders))
                    .printRow("body", prettyPrintString(
                            readBodyAsString(response, logBody ? mResponseBodyMaxLogBytes : 0)))
                    .end();
        }
        return response;
    }


    private static final class SimpleTableStringPrinter {

        private static int sum(int... values) {
            int sum = 0;
            for (int a : values) sum += a;
            return sum;
        }

        private static void fillRepeatChar(StringBuilder sb, char ch, int count) {
            if (count <= 0) return;
            for (int i = 0; i < count; ++i) {
                sb.append(ch);
            }
        }

        private static int fillText(StringBuilder sb, String tableChars, String[] texts, int[] ems) {
            if (tableChars.length() != 4) throw new IllegalArgumentException("example: ╟─┼╢");
            if (texts.length > ems.length) throw new IllegalArgumentException();
            final int originalSize = sb.length();
            final char line = tableChars.charAt(1);
            final char mid = tableChars.charAt(2);
            final char right = tableChars.charAt(3);

            sb.append(tableChars.charAt(0));
            for (int i = 0, columnCount = ems.length; i < columnCount; ++i) {
                sb.append(i < texts.length ? texts[i] : "");
                fillRepeatChar(sb, line, ems[i] - (i < texts.length ? texts[i].length() : 0));
                sb.append(i == columnCount - 1 ? right : mid);
            }
            return sb.length() - originalSize;
        }

        private static final int[] SEGMENTS_LENGTH = {10, 88};
        private static final String[] TABLE_CHARS = {
                "╔═╤═",
                "╟─┼─",
                "║ │ ",
                "╚═╧═"
        };
        private final Logger mLogger;
        private final boolean mLineAutoWrap;

        /* package */ SimpleTableStringPrinter(Logger logger, boolean lineAutoWrap) {
            mLogger = logger;
            mLineAutoWrap = lineAutoWrap;

            StringBuilder sb = new StringBuilder(sum(SEGMENTS_LENGTH) + SEGMENTS_LENGTH.length - 1);
            fillText(sb, TABLE_CHARS[0], new String[0], SEGMENTS_LENGTH);
            mLogger.log(sb.toString());
        }

        /* package */ SimpleTableStringPrinter printRow(String... columns) {
            if (columns.length > SEGMENTS_LENGTH.length) throw new IllegalArgumentException();
            if (columns.length == 0) {
                StringBuilder sb = new StringBuilder(sum(SEGMENTS_LENGTH) + SEGMENTS_LENGTH.length - 1);
                fillText(sb, TABLE_CHARS[1], new String[0], SEGMENTS_LENGTH);
                mLogger.log(sb.toString());
                return this;
            }

            boolean flag = true;
            while (flag) {
                flag = false;
                StringBuilder sb = new StringBuilder(533);

                // support multi lines
                String[] singleLine = new String[columns.length];
                for (int i = 0; i < columns.length; ++i) {
                    final int index = columns[i].indexOf('\n');
                    if (index >= 0) {
                        if (mLineAutoWrap && index > SEGMENTS_LENGTH[i]) {
                            singleLine[i] = columns[i].substring(0, SEGMENTS_LENGTH[i]);
                            columns[i] = columns[i].substring(SEGMENTS_LENGTH[i]);
                        } else {
                            singleLine[i] = columns[i].substring(0, index);
                            columns[i] = columns[i].substring(index + 1);
                        }
                        flag = true;
                    } else {
                        if (mLineAutoWrap && columns[i].length() > SEGMENTS_LENGTH[i]) {
                            singleLine[i] = columns[i].substring(0, SEGMENTS_LENGTH[i]);
                            columns[i] = columns[i].substring(SEGMENTS_LENGTH[i]);
                            flag = true;
                        } else {
                            singleLine[i] = columns[i];
                            columns[i] = "";
                        }
                    }
                }

                fillText(sb, TABLE_CHARS[2], singleLine, SEGMENTS_LENGTH);
                mLogger.log(sb.toString().trim());
            }
            return this;
        }

        /* package */ void end() {
            StringBuilder sb = new StringBuilder(sum(SEGMENTS_LENGTH) + SEGMENTS_LENGTH.length - 1);
            fillText(sb, TABLE_CHARS[TABLE_CHARS.length - 1], new String[0], SEGMENTS_LENGTH);
            mLogger.log(sb.toString());
        }
    }
}
