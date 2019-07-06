package okhttp3.internal.platform;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * @author Relish Wang
 * @since 2019/03/19
 */
public class PlatformHack {

    public static X509TrustManager trustManager(SSLSocketFactory sslSocketFactory) {
        return Platform.get().trustManager(sslSocketFactory);
    }
}
