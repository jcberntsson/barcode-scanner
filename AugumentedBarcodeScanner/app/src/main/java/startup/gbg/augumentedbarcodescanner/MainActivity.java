package startup.gbg.augumentedbarcodescanner;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.view.View.INVISIBLE;

public class MainActivity extends Activity {
    private String TAG = "MainActivity";

    private TextureView textureView;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private HashMap<String, Long> registeredEANs = new HashMap<>();
    private Size imageDimension;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private BarcodeDetector detector;
    private API service;
    private Thread mDecodeThread;

    private ConcurrentLinkedDeque<Bitmap> bitmapsToProcess = new ConcurrentLinkedDeque<>();
    private boolean mIsPaused = false;

    private ProductLayer productLayer;

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        long surfaceUpdates = 0;

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (!mIsPaused)
                openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            //if (++surfaceUpdates % 10 == 0)
            //    bitmapsToProcess.add(textureView.getBitmap());
        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NotNull CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "Camera onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NotNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NotNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);

        productLayer = (ProductLayer) findViewById(R.id.productLayer);

        //
        detector = new BarcodeDetector.Builder(getApplicationContext())
                        .setBarcodeFormats(Barcode.EAN_13 | Barcode.EAN_8)
                        .build();
        if(!detector.isOperational()){
            Toast.makeText(MainActivity.this, "Could not set up barcode detector!", Toast.LENGTH_SHORT).show();
            finish();
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://johanssonjohn.com:5000/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(API.class);

        productLayer.setBackendService(service);


        findViewById(R.id.economicScore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                productLayer.togglePrices();
            }
        });
        findViewById(R.id.pricesLayer).setClickable(true);
        findViewById(R.id.pricesLayer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "HEJOO", Toast.LENGTH_SHORT).show();
                productLayer.hidePricesLayer();
            }
        });
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NotNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NotNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void getPricesForProduct(final Product p, final Callback<LinkedList<PriceData>> callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Call<LinkedList<PriceData>> priceCall =  service.getPrices(p.id);
                try {
                    Response<LinkedList<PriceData>> res = priceCall.execute();
                    callback.onResponse(priceCall,res);
                    Log.d(TAG, "Hittade : "+res.body().size() + " produkter yooo!");
                }catch (IOException e){
                    Log.d(TAG, "N[got gick fel yoo: "+e.getMessage());
                }
            }
        }).start();
    }

    public void takeCareOfEAN(String ean){
        while(ean.length() < 14){
            ean = "0" + ean;
        }



        if(registeredEANs.containsKey(ean)){
            long lastTime = registeredEANs.get(ean);
            if(System.currentTimeMillis()-lastTime < 10000){
                return;
            }else{
                registeredEANs.remove(ean);
            }
        }
        registeredEANs.put(ean,System.currentTimeMillis());

        final String coolEAN = ean;

        new Thread(new Runnable() {
            @Override
            public void run() {
                Call<LinkedList<Product>> productCall =  service.listByGTIN(coolEAN);
                try {
                    LinkedList<Product> products = productCall.execute().body();

                    if(products.size()>0){
                        Log.d(TAG, "Hittade : "+products.get(0).name);
                        final Product product = products.get(0);
                        productLayer.post(new Runnable() {
                            @Override
                            public void run() {
                                productLayer.hidePricesLayer();
                                productLayer.setVisibility(View.VISIBLE);
                                productLayer.setProduct(product);
                            }
                        });

                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                productLayer.setVisibility(INVISIBLE);
                                Toast.makeText(getBaseContext(), "Yo, this product doesn't exists man!!", Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                }catch (IOException e){
                    Log.d(TAG, "N[got gick fel yoo: "+e.getMessage());
                }
            }
        }).start();
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "openCamera");
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);

            startDecodeThread();
        } catch (CameraAccessException e) {
            Log.e(TAG, "openCamera error");
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(MainActivity.this, "This app can not be used without camera permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    protected void startDecodeThread() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {
                        break;
                    }
                    Bitmap bitmap = textureView.getBitmap();
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<Barcode> barcodes = detector.detect(frame);
                    for(int i = 0; i < barcodes.size(); i++)
                    {
                        Barcode b = barcodes.valueAt(i);
                        takeCareOfEAN(b.rawValue);
                        Log.d(TAG, "Barcode found with gtin: " + b.rawValue);
                    }

                }
            }
        };
        mDecodeThread = new Thread(runnable);
        mDecodeThread.start();
    }

    protected void stopDecodeThread() {
        if (mDecodeThread != null)
        {
            mDecodeThread.interrupt();
            try {
                mDecodeThread.join();
                mDecodeThread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsPaused = false;
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }

        // Enable immersive mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        mIsPaused = true;
        stopBackgroundThread();
        stopDecodeThread();
        closeCamera();
        super.onPause();
    }
}


