package com.nihaocloud.sesamedisk.httputils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.nihaocloud.sesamedisk.account.Account;
import com.nihaocloud.sesamedisk.data.ProgressMonitor;
import com.nihaocloud.sesamedisk.ssl.SSLTrustManager;
import com.nihaocloud.sesamedisk.transfer.MirroredSource;
import com.nihaocloud.sesamedisk.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import okio.Source;


public class RequestManager {

    private OkHttpClient client;
    private static final long TIMEOUT_COUNT = 5;
    private static RequestManager mRequestManager;

    private RequestManager(Account account) {
        SSLSocketFactory sslSocketFactory = SSLTrustManager.instance().getSSLSocketFactory(account);
        X509TrustManager defaultTrustManager = SSLTrustManager.instance().getDefaultTrustManager();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslSocketFactory, defaultTrustManager);
        builder.addInterceptor(new LoggingInterceptor()); //add okhttp log
        client = builder.hostnameVerifier((hostname, session) -> true)
                .retryOnConnectionFailure(true)
                .connectTimeout(TIMEOUT_COUNT, TimeUnit.MINUTES)
                .readTimeout(TIMEOUT_COUNT, TimeUnit.MINUTES)
                .writeTimeout(TIMEOUT_COUNT, TimeUnit.MINUTES)
                .build();
    }

    public OkHttpClient getClient() {
        return client;
    }


    public static RequestManager getInstance(Account account) {
        if (mRequestManager == null) {
            synchronized (RequestManager.class) {
                if (mRequestManager == null) {
                    mRequestManager = new RequestManager(account);
                }
            }
        }
        return mRequestManager;
    }


    /**
     * output log interceptor
     */
    private class LoggingInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            String content = response.body().string();
            Log.i("LoggingInterceptor", content);
            return response.newBuilder()
                    .body(okhttp3.ResponseBody.create(response.body().contentType(), content))
                    .build();
        }
    }


    /**
     * Create progress RequestBody
     *
     * @param monitor
     * @param file
     * @param <T>
     * @return
     */
    public <T> RequestBody createProgressRequestBody(ProgressMonitor monitor, final File file) {
        return new RequestBody() {

            public long temp = System.currentTimeMillis();

            @Override
            public MediaType contentType() {
                return MediaType.parse("application/octet-stream");
            }

            @Override
            public long contentLength() {
                return file.length();
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source;
                try {
                    source = Okio.source(file);
                    Buffer buf = new Buffer();
                    // long remaining = contentLength();
                    long current = 0;
                    for (long readCount; (readCount = source.read(buf, 2048)) != -1; ) {
                        sink.write(buf, readCount);
                        current += readCount;
                        long nowt = System.currentTimeMillis();
                        // 1s refresh progress
                        if (nowt - temp >= 1000) {
                            temp = nowt;
                            monitor.onProgressNotify(current, false);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }


    public <T> RequestBody createProgressRequestBody(Context context, ProgressMonitor monitor, final Uri uri, final File cacheFile) {
        return new RequestBody() {

            public long temp = System.currentTimeMillis();

            @Override
            public MediaType contentType() {
                return MediaType.parse("application/octet-stream");
            }

            @Override
            public long contentLength() {
                return Utils.getFilSizeFromUri(context, uri);
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                try {
                    final Sink sinkFile = Okio.sink(cacheFile);
                    final Source source = Okio.source(context.getContentResolver().openInputStream(uri));
                    final MirroredSource mirroredSource = new MirroredSource(source);
                    final Source httpSource = mirroredSource.original();
                    final Source fileSource = mirroredSource.mirror();
                    Buffer httpBuffer = new Buffer();
                    Buffer fileBuffer = new Buffer();
                    long current = 0;
                    for (long readCount; (readCount = httpSource.read(httpBuffer, 2048)) != -1; ) {
                        sink.write(httpBuffer, readCount);
                        current += readCount;
                        long nowt = System.currentTimeMillis();
                        long fileCount = fileSource.read(fileBuffer, readCount);
                        sinkFile.write(fileBuffer, fileCount);
                        if (nowt - temp >= 1000) {
                            temp = nowt;
                            monitor.onProgressNotify(current, false);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
