package cn.edu.zju.myopencv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.InputStream;
import java.text.DecimalFormat;

public class JavaActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private double max_size = 1024;
    private int PICK_IMAGE_REQUEST = 1;// 请求码
    private ImageView myImageView;
    private Bitmap selectbp;
    private TextView textView_h;
    private TextView textView_s;
    private TextView textView_i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java);

        staticLoadCVLibraries();

        myImageView = (ImageView)findViewById(R.id.imageView);
        // 图片填充控件，不让图片变形，图片居中显示
        myImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        textView_h = findViewById(R.id.H);
        textView_s = findViewById(R.id.S);
        textView_i = findViewById(R.id.L);
        Button selectImageBtn = (Button)findViewById(R.id.select_btn);
        selectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // makeText(MainActivity.this.getApplicationContext(), "start to browser image", Toast.LENGTH_SHORT).show();
                selectImage();
            }
            private void selectImage() {
                Intent intent = new Intent();
                intent.setType("image/*");// 设置intent filter
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"请选择图片..."), PICK_IMAGE_REQUEST);
            }
        });

        Button processBtn = (Button)findViewById(R.id.process_btn);
        processBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // makeText(MainActivity.this.getApplicationContext(), "hello, image process", Toast.LENGTH_SHORT).show();
                //convertGray();
                Intent intent = new Intent(getApplicationContext(),RealtimeActivity.class);
                startActivity(intent);
            }
        });

    }

    private void staticLoadCVLibraries() {

        // Returns true is initialization of OpenCV was successful
        boolean load = OpenCVLoader.initDebug();
        if(load) {
            Log.i("CV", "Open CV Libraries loaded...");
        }

    }

    private void convertGray() {
        DecimalFormat df = new DecimalFormat( "0.00");
        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(selectbp, src);
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_BGRA2BGR);
        //Log.i("CV", "image type:" + (temp.type() == CvType.CV_8UC3));
        Imgproc.cvtColor(temp, dst, Imgproc.COLOR_BGR2HLS);
        double lightest = 0.0;
        double pix[] = new double[10];
        for(int i = 0 ; i < dst.rows() ; i++)
        {
            for(int j = 0 ; j < dst.cols() ;j++)
            {
                for(int k = 0 ; k < dst.get(i,j).length ; k++)
                {
                    pix[k] = dst.get(i,j)[k];
                    if( pix[1] > lightest)
                    {
                        lightest = pix[1];
                    }
                }
            }
        }
        Log.d("nikolajohn", String.valueOf(lightest));
        textView_i.setText(String.valueOf(df.format(lightest/255)));
        Utils.matToBitmap(dst, selectbp);
        myImageView.setImageBitmap(selectbp);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {//检查请求码和返回码
            // 一个data主要包括了一个URI和mimeType
            Uri uri = data.getData();
            try {
                Log.d("image-tag", "start to decode selected image now...");
                InputStream input = getContentResolver().openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(input, null, options);

                int raw_width = options.outWidth;
                int raw_height = options.outHeight;
                int max = Math.max(raw_width, raw_height);
                int newWidth = raw_width;
                int newHeight = raw_height;
                int inSampleSize = 1;
                if(max > max_size) {
                    newWidth = raw_width / 2;
                    newHeight = raw_height / 2;
                    while((newWidth/inSampleSize) > max_size || (newHeight/inSampleSize) > max_size) {
                        inSampleSize *=2;
                    }
                }

                options.inSampleSize = inSampleSize;
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                selectbp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);

                myImageView.setImageBitmap(selectbp);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


