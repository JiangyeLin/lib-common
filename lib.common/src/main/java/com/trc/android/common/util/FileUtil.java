package com.trc.android.common.util;

import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;

import com.trc.android.common.exception.ExceptionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author HanTuo on 2017/2/8.
 */

public class FileUtil extends FileProvider {

    public static final String FILE_PROVIDER_SUFFIX = ".fileProvider";
    public static final String FILE_PROVIDER_SCHEME = "content://";

    public static File getShareFile(String fileName) {
        return new File(getShareFileDir(), fileName);
    }

    public static Uri getShareFileUri(File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(Contexts.getInstance(), Contexts.getInstance().getPackageName() + FILE_PROVIDER_SUFFIX, file);
        } else {
            return Uri.fromFile(file);
        }
    }

    public static Uri getShareFileUri(String fileName) {
        return getShareFileUri(getShareFile(fileName));
    }

    //content://com.tairanchina.taiheapp.fileprovider/open/1489217533007.jpeg
    public static File getRealFile(Uri uri) {
        String path = uri.toString().replace(FILE_PROVIDER_SCHEME + Contexts.getInstance().getPackageName() + FILE_PROVIDER_SUFFIX, Contexts.getInstance().getExternalCacheDir().getAbsolutePath());
        return new File(path);
    }

    public static File getShareFileDir() {
        File externalCacheDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {//部分手机没有外部存储卡，获得的目录是"/"
            File[] externalCacheDirs = ContextCompat.getExternalCacheDirs(Contexts.getInstance());
            if (externalCacheDirs.length > 0) {
                externalCacheDir = externalCacheDirs[0];
            } else {
                externalCacheDir = Contexts.getInstance().getExternalCacheDir();
            }
        } else {
            externalCacheDir = Contexts.getInstance().getCacheDir();
        }
        return externalCacheDir;
    }

    public static void download(final String url, final Map<String, String> headers, final File targetFile, final DownloadListener listener) {
        new Thread(new Runnable() {
            private Handler handler = new Handler(Looper.getMainLooper());
            DownloadListener downloadListener = listener;


            @Override
            public void run() {
                synchronized (targetFile.getPath().intern()) {
                    if (targetFile.exists()) {
                        onSuccess();
                        return;
                    }
                    FileOutputStream fos = null;
                    InputStream is;
                    HttpURLConnection connection = null;
                    boolean isDownloadSuccessful = false;
                    File tmpCacheFile = new File(targetFile.getParentFile(), targetFile.getName() + ".tmp");
                    try {
                        if (tmpCacheFile.exists()) tmpCacheFile.delete();
                        URL u = new URL(url);
                        connection = (HttpURLConnection) u.openConnection();
                        if (null != headers && !headers.isEmpty()) {
                            for (Map.Entry<String, String> entry : headers.entrySet()) {
                                connection.setRequestProperty(entry.getKey(), entry.getValue());
                            }
                        }
                        connection.connect();
                        int responseCode = connection.getResponseCode();
                        if (responseCode > 300 && responseCode < 400) {
                            String location = connection.getHeaderField("Location");
                            if (null != location) {
                                download(location, targetFile, downloadListener);
                                return;
                            }
                        }
                        if (responseCode == HttpURLConnection.HTTP_OK || responseCode < 400) {
                            fos = new FileOutputStream(tmpCacheFile, false);
                            is = connection.getInputStream();
                            byte[] buffer = new byte[20480];
                            int n = 0;
                            long sum = 0;
                            final ProDownloadListener[] proDownloadListener = new ProDownloadListener[1];
                            if (downloadListener instanceof ProDownloadListener) {
                                proDownloadListener[0] = (ProDownloadListener) downloadListener;
                            }
                            final long total = getLong(connection.getHeaderField("content-length"));
                            do {
                                fos.write(buffer, 0, n);
                                n = is.read(buffer);
                                sum += n;
                                if (null != proDownloadListener[0]) {
                                    handler.removeCallbacks(null);
                                    final long percentage = sum * 100 / connection.getContentLength();
                                    onProgress(proDownloadListener, total, (int) percentage);
                                }
                            } while (n != -1);
                            fos.flush();
                            isDownloadSuccessful = true;
                        } else {
                            onFail();
                        }
                    } catch (Throwable e) {
                        onFail();
                    } finally {
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                ExceptionManager.handle(e);
                            }
                        }
                        if (null != connection) {
                            connection.disconnect();
                        }
                        if (isDownloadSuccessful) {
                            if (tmpCacheFile.renameTo(targetFile)) {
                                onSuccess();
                            } else {
                                tmpCacheFile.delete();
                                onFail();
                            }
                        }
                    }
                }
            }

            private void onFail() {
                handler.post(() -> {
                    try {
                        downloadListener.onFail();
                    } catch (Exception e) {
                        ExceptionManager.handle(e);
                    }
                });
            }

            private void onSuccess() {
                handler.post(() -> {
                    try {
                        downloadListener.onSuccess();
                    } catch (Exception e) {
                        ExceptionManager.handle(e);
                    }
                });
            }

            private void onProgress(final ProDownloadListener[] proDownloadListener, final long total, final int percentage) {
                handler.post(() -> {
                    try {
                        proDownloadListener[0].onProgress(percentage, total);
                    } catch (Exception e) {
                        ExceptionManager.handle(e);
                    }
                });
            }
        }).start();
    }

    private static long getLong(String longStr) {
        try {
            return Long.parseLong(longStr);
        } catch (Throwable e) {
            ExceptionManager.handle(e);
        }
        return -1;
    }


    public static void download(final String url, final File targetFile, final DownloadListener listener) {
        download(url, null, targetFile, listener);
    }

    public interface DownloadListener {

        void onSuccess();

        void onFail();
    }

    public interface ProDownloadListener extends DownloadListener {
        void onProgress(int progress, long total);
    }

    public static long getFileSize(File file) {
        try {
            if (file.isFile()) {
                return file.length();
            } else {
                long size = 0;
                File[] files = file.listFiles();
                for (File f : files) {
                    size += getFileSize(f);
                }
                return size;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean deleteFile(File file) {
        try {
            if (!file.exists()) {
                return true;
            } else if (file.isFile()) {
                return file.delete();
            } else {
                File[] files = file.listFiles();
                for (File f : files) {
                    if (!deleteFile(f)) {
                        return false;
                    }
                }
                return file.delete();
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean unzip(File zipSrcFile, File targetDir) {
        try {
            if (zipSrcFile.exists()) {
                ZipFile zipFile = new ZipFile(zipSrcFile);
                Enumeration e = zipFile.entries();
                while (e.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    if (entry.isDirectory()) continue;
                    InputStream is = zipFile.getInputStream(entry);
                    File dstFile = new File(targetDir + "/" + entry.getName());
                    File parentFile = dstFile.getParentFile();
                    if (!parentFile.exists()) parentFile.mkdirs();
                    FileOutputStream fos = new FileOutputStream(dstFile);
                    byte[] buffer = new byte[8192];
                    int count = 0;
                    while ((count = is.read(buffer, 0, buffer.length)) != -1) {
                        fos.write(buffer, 0, count);
                    }
                }
                return true;
            }
        } catch (Throwable t) {
            deleteFile(targetDir);
            ExceptionManager.handle(t);
        }
        return false;
    }
}
