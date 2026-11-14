package com.example.barista_app.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ImageUploader {

    public interface UploadCallback {
        void onSuccess(String url);

        void onError(Exception exception);
    }

    private static final int MAX_SIDE_PX = 1024;
    private static final int JPEG_QUALITY = 80;
    private static final String BOUNDARY = "BaristaAppBoundary";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ImageUploader() {
    }

    public static void upload(Context context, Uri uri, UploadCallback callback) {
        Context appContext = context.getApplicationContext();
        executor.execute(() -> {
            try {
                byte[] jpeg = compress(appContext, uri);
                String url = send(jpeg);
                android.util.Log.d("ImageUploader", "url=" + url);
                mainHandler.post(() -> callback.onSuccess(url));
            } catch (Exception exception) {
                android.util.Log.e("ImageUploader", "upload failed", exception);
                mainHandler.post(() -> callback.onError(exception));
            }
        });
    }

    private static byte[] compress(Context context, Uri uri) throws Exception {
        Bitmap original;
        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            original = BitmapFactory.decodeStream(input);
        }
        if (original == null) {
            throw new IllegalStateException("No se pudo leer la imagen");
        }
        float scale = Math.min(1f, (float) MAX_SIDE_PX / Math.max(original.getWidth(), original.getHeight()));
        Bitmap scaled = Bitmap.createScaledBitmap(
                original,
                Math.round(original.getWidth() * scale),
                Math.round(original.getHeight() * scale),
                true);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output);
        return output.toByteArray();
    }

    private static String send(byte[] jpeg) throws Exception {
        URL endpoint = new URL("https://api.cloudinary.com/v1_1/" + CloudinaryConfig.CLOUD_NAME + "/image/upload");
        HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

        try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
            writeTextField(out, "upload_preset", CloudinaryConfig.UPLOAD_PRESET);
            out.writeBytes("--" + BOUNDARY + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"recipe.jpg\"\r\n");
            out.writeBytes("Content-Type: image/jpeg\r\n\r\n");

            out.write(jpeg);
            out.writeBytes("\r\n--" + BOUNDARY + "--\r\n");
        }

        int code = connection.getResponseCode();
        InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
        String body = readAll(stream);
        if (code >= 400) {
            throw new IllegalStateException(body);
        }
        return new JSONObject(body).getString("secure_url");
    }

    private static void writeTextField(DataOutputStream out, String name, String value) throws Exception {
        out.writeBytes("--" + BOUNDARY + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
        out.writeBytes(value + "\r\n");
    }

    private static String readAll(InputStream stream) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        stream.close();
        return output.toString("UTF-8");
    }
}
