package com.tencent.xlab.infinixcamera2.utils;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.DngCreator;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @author v_ccxinchen on 2019/7/29
 * @introduce
 */
public class ImageSaver implements Runnable {
    private static final String TAG = "ImageSaver";
    /**
     * The image to save.
     */
    private final Image mImage;
    /**
     * The file we save the image into.
     */
    private final File mFile;

    /**
     * The CaptureResult for this image capture.
     */
    private final CaptureResult mCaptureResult;

    /**
     * The CameraCharacteristics for this camera device.
     */
    private final CameraCharacteristics mCharacteristics;

    /**
     * The Context to use when updating MediaStore with the saved images.
     */
    private final Context mContext;

    /**
     * A reference counted wrapper for the ImageReader that owns the given image.
     */
    private final RefCountedAutoCloseable<ImageReader> mReader;

    private ImageSaver(Image image, File file, CaptureResult result,
                       CameraCharacteristics characteristics, Context context,
                       RefCountedAutoCloseable<ImageReader> reader) {
        mImage = image;
        mFile = file;
        mCaptureResult = result;
        mCharacteristics = characteristics;
        mContext = context;
        mReader = reader;
    }

    @Override
    public void run() {
        boolean success = false;
        int format = mImage.getFormat();
        switch (format) {
            case ImageFormat.JPEG: {
                ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                FileOutputStream output = null;
                try {
                    output = new FileOutputStream(mFile);
                    output.write(bytes);
                    success = true;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mImage.close();
                    closeOutput(output);
                }
                break;
            }
            case ImageFormat.RAW_SENSOR: {
                DngCreator dngCreator = new DngCreator(mCharacteristics, mCaptureResult);
                FileOutputStream output = null;
                try {
                    output = new FileOutputStream(mFile);
                    dngCreator.writeImage(output, mImage);
                    success = true;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mImage.close();
                    closeOutput(output);
                }
                break;
            }
            default: {
                Log.e(TAG, "Cannot save image, unexpected image format:" + format);
                break;
            }
        }

        // Decrement reference count to allow ImageReader to be closed to free up resources.
        mReader.close();

        // If saving the file succeeded, update MediaStore.
        if (success) {
            MediaScannerConnection.scanFile(mContext, new String[]{mFile.getPath()},
                    /*mimeTypes*/null, new MediaScannerConnection.MediaScannerConnectionClient() {
                        @Override
                        public void onMediaScannerConnected() {
                            // Do nothing
                        }

                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i(TAG, "Scanned " + path + ":");
                            Log.i(TAG, "-> uri=" + uri);
                        }
                    });
        }
    }

    /**
     * Cleanup the given {@link OutputStream}.
     *
     * @param outputStream the stream to close.
     */
    private static void closeOutput(OutputStream outputStream) {
        if (null != outputStream) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A wrapper for an {@link AutoCloseable} object that implements reference counting to allow
     * for resource management.
     */
    public static class RefCountedAutoCloseable<T extends AutoCloseable> implements AutoCloseable {
        private T mObject;
        private long mRefCount = 0;

        /**
         * Wrap the given object.
         *
         * @param object an object to wrap.
         */
        public RefCountedAutoCloseable(T object) {
            if (object == null) throw new NullPointerException();
            mObject = object;
        }

        /**
         * Increment the reference count and return the wrapped object.
         *
         * @return the wrapped object, or null if the object has been released.
         */
        public synchronized T getAndRetain() {
            if (mRefCount < 0) {
                return null;
            }
            mRefCount++;
            return mObject;
        }

        /**
         * Return the wrapped object.
         *
         * @return the wrapped object, or null if the object has been released.
         */
        public synchronized T get() {
            return mObject;
        }

        /**
         * Decrement the reference count and release the wrapped object if there are no other
         * users retaining this object.
         */
        @Override
        public synchronized void close() {
            if (mRefCount >= 0) {
                mRefCount--;
                if (mRefCount < 0) {
                    try {
                        mObject.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        mObject = null;
                    }
                }
            }
        }
    }

    public static class ImageSaverBuilder {
        private Image mImage;
        private File mFile;
        private CaptureResult mCaptureResult;
        private CameraCharacteristics mCharacteristics;
        private Context mContext;
        private RefCountedAutoCloseable<ImageReader> mReader;

        /**
         * Construct a new ImageSaverBuilder using the given {@link Context}.
         *
         * @param context a {@link Context} to for accessing the
         *                {@link android.provider.MediaStore}.
         */
        public ImageSaverBuilder(final Context context) {
            mContext = context;
        }

        public synchronized ImageSaverBuilder setRefCountedReader(
                RefCountedAutoCloseable<ImageReader> reader) {
            if (reader == null) throw new NullPointerException();

            mReader = reader;
            return this;
        }

        public synchronized ImageSaverBuilder setImage(final Image image) {
            if (image == null) throw new NullPointerException();
            mImage = image;
            return this;
        }

        public synchronized ImageSaverBuilder setFile(final File file) {
            if (file == null) throw new NullPointerException();
            mFile = file;
            return this;
        }

        public synchronized ImageSaverBuilder setResult(final CaptureResult result) {
            if (result == null) throw new NullPointerException();
            mCaptureResult = result;
            return this;
        }

        public synchronized ImageSaverBuilder setCharacteristics(
                final CameraCharacteristics characteristics) {
            if (characteristics == null) throw new NullPointerException();
            mCharacteristics = characteristics;
            return this;
        }

        public synchronized ImageSaver buildIfComplete() {
            if (!isComplete()) {
                return null;
            }
            return new ImageSaver(mImage, mFile, mCaptureResult, mCharacteristics, mContext,
                    mReader);
        }

        public synchronized String getSaveLocation() {
            return (mFile == null) ? "Unknown" : mFile.toString();
        }

        private boolean isComplete() {
            return mImage != null && mFile != null && mCaptureResult != null
                    && mCharacteristics != null;
        }
    }
}
