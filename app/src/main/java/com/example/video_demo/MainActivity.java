package com.example.video_demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.graphics.SurfaceTexture;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener{

    //这里使用的是camera1，使用camera2可以参考：https://blog.csdn.net/qq_21898059/article/details/50986290#comments
    private static final String TAG = "MainActivity";

    private TextureView textureView;
    private Camera mCamera;

    final RxPermissions rxPermissions = new RxPermissions(this); // where this is an Activity or Fragment instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = (TextureView) findViewById(R.id.texture_view);

        //要是用这个方法旋转图像的话，图像就会被拉伸，感觉这里是旋转了这个控件，而后面那个方法是旋转了图像
//        textureView.setRotation(90); //设置预览角度，并不改变获取到的原始数据方向(与Camera.setDisplayOrientation(0)相同)

        textureView.setSurfaceTextureListener(this);
        initData();

    }


    private void initData() {
        int numberOfCameras = Camera.getNumberOfCameras();// 获取摄像头个数
        if (numberOfCameras < 1) {
            Toast.makeText(this, "没有相机", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @SuppressLint("CheckResult")
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        // 打开相机 0后置 1前置
        // Must be done during an initialization phase like onCreate
        rxPermissions
                .request(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        mCamera = Camera.open(0);
                        if (mCamera != null) {
                            // 设置相机预览宽高，此处设置为TextureView宽高
                            Camera.Parameters params = mCamera.getParameters();
                            params.setPreviewFormat(ImageFormat.NV21); //设置返回的格式
                            Camera.Size size = params.getPreviewSize();
                            Log.d(TAG, "onSurfaceTextureAvailable: width:" + size.width);
                            Log.d(TAG, "onSurfaceTextureAvailable: height:" + size.height);
                            params.setPreviewSize(1920, 1080);  //设置预览图长款
                            // 设置自动对焦模式
                            List<String> focusModes = params.getSupportedFocusModes();
                            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO )) {
                                Log.d(TAG, "onSurfaceTextureAvailable: 111");
                                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO );
                                mCamera.setParameters(params);
                            }
                            try {
                                mCamera.setDisplayOrientation(90);// 设置预览角度，并不改变获取到的原始数据方向
                                // 绑定相机和预览的View
                                mCamera.setPreviewTexture(surface);
                                // 开始预览
                                mCamera.startPreview();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        // Oups permission denied
                        Toast.makeText(this, "请授予相机权限！", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

}
