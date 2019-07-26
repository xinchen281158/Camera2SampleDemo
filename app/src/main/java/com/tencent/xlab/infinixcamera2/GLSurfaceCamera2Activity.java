package com.tencent.xlab.infinixcamera2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


import com.tencent.xlab.infinixcamera2.camera.Camera2Proxy;
import com.tencent.xlab.infinixcamera2.utils.ImageUtils;
import com.tencent.xlab.infinixcamera2.view.ASeekBar;
import com.tencent.xlab.infinixcamera2.view.Camera2GLSurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tencent.xlab.infinixcamera2.utils.ImageUtils.GALLERY_PATH;


public class GLSurfaceCamera2Activity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "GLSurfaceCamera2Act";

    private ImageView mCloseIv;
    private ImageView mSwitchCameraIv;
    private ImageView mTakePictureIv;
    private ImageView mPictureIv;
    private ImageView mSetFlashIv;

    private ASeekBar iso_aSeekBar;
    private ASeekBar exp_aSeekBar;
    private EditText et_count;
    public static TextView tv_iso;

    private Camera2GLSurfaceView mCameraView;

    private Camera2Proxy mCameraProxy;
    private int flashState = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glsurface_camera2);
        initView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCameraProxy.stopPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraProxy.startPreview();
    }

    private void initView() {
        mCloseIv = findViewById(R.id.toolbar_close_iv);
        mCloseIv.setOnClickListener(this);
        mSwitchCameraIv = findViewById(R.id.toolbar_switch_iv);
        mSwitchCameraIv.setOnClickListener(this);
        mTakePictureIv = findViewById(R.id.take_picture_iv);
        mTakePictureIv.setOnClickListener(this);
        mPictureIv = findViewById(R.id.picture_iv);
        mPictureIv.setOnClickListener(this);
        mSetFlashIv = findViewById(R.id.toolbar_flash_iv);
        mSetFlashIv.setOnClickListener(this);
        mPictureIv.setImageBitmap(ImageUtils.getLatestThumbBitmap());
        mCameraView = findViewById(R.id.camera_view);
        iso_aSeekBar = findViewById(R.id.iso_seekBar);
        exp_aSeekBar = findViewById(R.id.exp_seekBar);

        et_count = findViewById(R.id.et_count);
        tv_iso = findViewById(R.id.tv_iso);

        mCameraProxy = mCameraView.getCameraProxy();

        iso_aSeekBar.setOnASeekBarListener(new IsoSeekBarListener());
        exp_aSeekBar.setOnASeekBarListener(new ExpTimeSeekBarListener());
    }

    private class ExpTimeSeekBarListener implements ASeekBar.ASeekBarListener{

        @Override
        public void SeekBarChange(int stall) {
            mCameraProxy.setExpChange(stall);
        }
    }

    private class IsoSeekBarListener implements ASeekBar.ASeekBarListener{
        @Override
        public void SeekBarChange(int stall) {
            mCameraProxy.setIsoChange(stall);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_close_iv:
                finish();
                break;
            case R.id.toolbar_switch_iv:
                mCameraProxy.switchCamera(mCameraView.getWidth(), mCameraView.getHeight());
                mCameraProxy.startPreview();
                break;
            case R.id.take_picture_iv:
                if (!TextUtils.isEmpty(et_count.getText())) {
                    //连拍
                    Log.e(TAG, et_count.getText().toString());
                    String text = et_count.getText().toString();

                    Pattern p = Pattern.compile("[0-9]*");
                    Matcher m = p.matcher(text);
                    if (m.matches()) {
                        mCameraProxy.setImageAvailableListener(mOnBurstImageAvailableListener);
                        mCameraProxy.captureImageBurst(Integer.parseInt(text));
                    }
                    p = Pattern.compile("[a-zA-Z]");
                    m = p.matcher(text);
                    if (m.matches()) {
                        //输入的是字母
                    }
                    p = Pattern.compile("[\u4e00-\u9fa5]");
                    m = p.matcher(text);
                    if (m.matches()) {
                        //输入的是汉字
                    }

                } else {
                    mCameraProxy.setImageAvailableListener(mOnImageAvailableListener);
                    mCameraProxy.captureStillPicture(); // 拍照
                }
                break;
            case R.id.picture_iv:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivity(intent);
                break;
            case R.id.toolbar_flash_iv:
                mCameraProxy.setFlash(flashState);//0:开启；1:关闭
                if (flashState == 0) {
                    flashState = 1;
                } else {
                    flashState = 0;
                }
                break;
        }
    }

    private ImageReader.OnImageAvailableListener mOnBurstImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireNextImage();
            try {
                new ImageSaver(image, mCameraProxy).run();
                CaptureResult captureResult = Camera2Proxy.captureRequests.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            image.close();
        }
    };

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener
            () {
        @Override
        public void onImageAvailable(ImageReader reader) {
            new ImageSaveTask().execute(reader.acquireLatestImage()); // 保存图片
        }
    };

    private ByteBuffer imageToByteBuffer(final Image image) {
        final Rect crop = image.getCropRect();
        final int width = crop.width();
        final int height = crop.height();

        final Image.Plane[] planes = image.getPlanes();
        final byte[] rowData = new byte[planes[0].getRowStride()];
        final int bufferSize = width * height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8;
        final ByteBuffer output = ByteBuffer.allocateDirect(bufferSize);

        int channelOffset = 0;
        int outputStride = 0;

        for (int planeIndex = 0; planeIndex < 3; planeIndex++) {
            if (planeIndex == 0) {
                channelOffset = 0;
                outputStride = 1;
            } else if (planeIndex == 1) {
                channelOffset = width * height + 1;
                outputStride = 2;
            } else if (planeIndex == 2) {
                channelOffset = width * height;
                outputStride = 2;
            }

            final ByteBuffer buffer = planes[planeIndex].getBuffer();
            final int rowStride = planes[planeIndex].getRowStride();
            final int pixelStride = planes[planeIndex].getPixelStride();

            final int shift = (planeIndex == 0) ? 0 : 1;
            final int widthShifted = width >> shift;
            final int heightShifted = height >> shift;

            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));

            for (int row = 0; row < heightShifted; row++) {
                final int length;

                if (pixelStride == 1 && outputStride == 1) {
                    length = widthShifted;
                    buffer.get(output.array(), channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (widthShifted - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);

                    for (int col = 0; col < widthShifted; col++) {
                        output.array()[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }

                if (row < heightShifted - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }

        return output;
    }

    private class ImageSaveTask extends AsyncTask<Image, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Image... images) {
            ByteBuffer buffer = images[0].getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            saveYUVData(images[0]);//存储YUV数据
//            final Image image = reader.acquireLatestImage();
            final ByteBuffer yuvBytes = imageToByteBuffer(images[0]);
            // Convert YUV to RGB
            final RenderScript rs = RenderScript.create(getBaseContext());
            final Bitmap bitmap = Bitmap.createBitmap(images[0].getWidth(), images[0].getHeight(), Bitmap.Config.ARGB_8888);
            final Allocation allocationRgb = Allocation.createFromBitmap(rs, bitmap);
            final Allocation allocationYuv = Allocation.createSized(rs, Element.U8(rs), yuvBytes.array().length);
            allocationYuv.copyFrom(yuvBytes.array());
            ScriptIntrinsicYuvToRGB scriptYuvToRgb = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
            scriptYuvToRgb.setInput(allocationYuv);
            scriptYuvToRgb.forEach(allocationRgb);
            allocationRgb.copyTo(bitmap);

            // Release

//            long time = System.currentTimeMillis();
//            if (mCameraProxy.isFrontCamera()) {
////                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                Log.d(TAG, "BitmapFactory.decodeByteArray time: " + (System.currentTimeMillis() - time));
//                time = System.currentTimeMillis();
//                // 前置摄像头需要左右镜像
//                Bitmap rotateBitmap = ImageUtils.rotateBitmap(bitmap, 270, true, true);
//                Log.d(TAG, "rotateBitmap time: " + (System.currentTimeMillis() - time));
//                time = System.currentTimeMillis();
//                ImageUtils.saveBitmap(rotateBitmap);
//                Log.d(TAG, "saveBitmap time: " + (System.currentTimeMillis() - time));
//                rotateBitmap.recycle();
//            } else {
////                ImageUtils.saveImage(bytes);
////                Matrix m = new Matrix();
////                m.postScale(-1, 1); // 镜像水平翻转
//                Bitmap rotateBitmap = ImageUtils.rotateBitmap(bitmap, 90, true, true);
////                Bitmap rotateBitmap = Bitmap.createBitmap(bitmap,0, 0, matrixBitmap.getWidth(), matrixBitmap.getHeight(), m, true);
//                ImageUtils.saveBitmap(rotateBitmap);
//                Log.d(TAG, "saveBitmap time: " + (System.currentTimeMillis() - time));
//                rotateBitmap.recycle();
//            }
            ImageUtils.saveBitmap(bitmap);
            images[0].close();
            bitmap.recycle();

            allocationYuv.destroy();
            allocationRgb.destroy();
            rs.destroy();
            return ImageUtils.getLatestThumbBitmap();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mPictureIv.setImageBitmap(bitmap);
        }

    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

    private void saveYUVData(Image image) {
        String fileName = DATE_FORMAT.format(new Date(System.currentTimeMillis())) + ".yuv";
        File file = new File(GALLERY_PATH, fileName);
        FileOutputStream output = null;
        ByteBuffer buffer;
        byte[] bytes;
        boolean success = false;
        switch (image.getFormat()) {
            case ImageFormat.YUV_420_888:
                // "prebuffer" simply contains the meta information about the following planes.
                ByteBuffer prebuffer = ByteBuffer.allocate(16);
                prebuffer.putInt(image.getWidth())
                        .putInt(image.getHeight())
                        .putInt(image.getPlanes()[1].getPixelStride())
                        .putInt(image.getPlanes()[1].getRowStride());

                try {
                    output = new FileOutputStream(file);
                    output.write(prebuffer.array()); // write meta information to file
                    // Now write the actual planes.
                    for (int i = 0; i < 3; i++) {
                        buffer = image.getPlanes()[i].getBuffer();
                        bytes = new byte[buffer.remaining()]; // makes byte array large enough to hold image
                        buffer.get(bytes); // copies image from buffer to byte array
                        output.write(bytes);    // write the byte array to file
                    }
                    Log.d(TAG, "saveYUV. filepath: " + file.getAbsolutePath());
                    success = true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
//                    image.close(); // close this to free up buffer for other images
                    if (null != output) {
                        try {
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }

    private class ImageSaver implements Runnable {
        private final Image mImage;
        private Camera2Proxy mCameraProxy;

        public ImageSaver(Image image, Camera2Proxy cameraProxy) {
            mImage = image;
            mCameraProxy = cameraProxy;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            saveYUVData(mImage);//存储YUV数据
//            final Image image = reader.acquireLatestImage();
            final ByteBuffer yuvBytes = imageToByteBuffer(mImage);
            // Convert YUV to RGB
            final RenderScript rs = RenderScript.create(getBaseContext());
            final Bitmap bitmap = Bitmap.createBitmap(mImage.getWidth(), mImage.getHeight(), Bitmap.Config.ARGB_8888);
            final Allocation allocationRgb = Allocation.createFromBitmap(rs, bitmap);
            final Allocation allocationYuv = Allocation.createSized(rs, Element.U8(rs), yuvBytes.array().length);
            allocationYuv.copyFrom(yuvBytes.array());
            ScriptIntrinsicYuvToRGB scriptYuvToRgb = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
            scriptYuvToRgb.setInput(allocationYuv);
            scriptYuvToRgb.forEach(allocationRgb);
            allocationRgb.copyTo(bitmap);

            // Release
            long time = System.currentTimeMillis();
            if (mCameraProxy.isFrontCamera()) {
//                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Log.d(TAG, "BitmapFactory.decodeByteArray time: " + (System.currentTimeMillis() - time));
                time = System.currentTimeMillis();
                // 前置摄像头需要左右镜像
                Bitmap rotateBitmap = ImageUtils.rotateBitmap(bitmap, 270, true, true);
                Log.d(TAG, "rotateBitmap time: " + (System.currentTimeMillis() - time));
                time = System.currentTimeMillis();
                ImageUtils.saveBitmap(rotateBitmap);
                Log.d(TAG, "saveBitmap time: " + (System.currentTimeMillis() - time));
                rotateBitmap.recycle();
            } else {
//                ImageUtils.saveImage(bytes);
                Bitmap rotateBitmap = ImageUtils.rotateBitmap(bitmap, 90, true, true);
                ImageUtils.saveBitmap(rotateBitmap);
                Log.d(TAG, "saveBitmap time: " + (System.currentTimeMillis() - time));
                rotateBitmap.recycle();
            }
            mImage.close();
            bitmap.recycle();
            allocationYuv.destroy();
            allocationRgb.destroy();
            rs.destroy();
        }
    }

}
