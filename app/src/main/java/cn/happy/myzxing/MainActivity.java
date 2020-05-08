package cn.happy.myzxing;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.WriterException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import cn.happy.myzxing.zxing.CaptureActivity;
import cn.happy.myzxing.zxing.decode.QRCodeDecode;
import cn.happy.myzxing.zxing.encode.QRCodeEncoder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private static final int IMG_REQ_CODE = 10000;

    private Button scanBtn, scanFromAblumBtn, gengrateQRCodeBtn;
    private EditText editQRCode;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 扫一扫
        scanBtn = findViewById(R.id.btn_scan);
        scanBtn.setOnClickListener(this);

        // 扫描相册中的二维码
        scanFromAblumBtn = findViewById(R.id.btn_scan_from_album);
        scanFromAblumBtn.setOnClickListener(this);

        editQRCode = findViewById(R.id.edit_qr_code_value);

        // 生成二维码
        gengrateQRCodeBtn = findViewById(R.id.btn_generate_qr_code);
        gengrateQRCodeBtn.setOnClickListener(this);

        iv = findViewById(R.id.iv);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_scan_from_album:
                // 打开手机相册，识别图片中的二维码
                Intent img = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                img.setType("image/*");
                startActivityForResult(img, IMG_REQ_CODE);
                break;

            case R.id.btn_generate_qr_code:
                // 根据输入内容生成二维码
                String value = editQRCode.getText().toString();

                Log.e(TAG, "用户输入的内容》》》》》" + value);

                if ("".equals(value)) {
                    Toast.makeText(MainActivity.this, "没有输入任何内容，无法生成二维码", Toast.LENGTH_SHORT).show();
                } else {
                    QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(MainActivity.this, 200, value);
                    try {
                        Bitmap bp = qrCodeEncoder.encodeAsBitmap();
                        iv.setImageBitmap(bp);

                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IMG_REQ_CODE:
                try {
                    Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);  //获取照片路径
                    cursor.close();

                    // 图片选择结果回调
//                        List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
//                        picturePath = selectList.get(0).getCompressPath();

                    Log.e("相册中选择 图片路径",picturePath);

                    Bitmap bitmap = compressPicture(picturePath);
                    QRCodeDecode.handleQRCodeFormBitmap(bitmap);

                } catch (Exception e) {
                    Log.e(TAG,"相册中选择 图片路径 没有得到图片");
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    // 申请成功
                } else {
                    // 申请失败
                }
            }
        }
    }

    // 压缩图片
    public static Bitmap compressPicture(String imgPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, options);
        options.inJustDecodeBounds = false;

        int h = options.outWidth;
        int w = options.outHeight;

        Log.e(TAG, "onActivityResult: 未压缩之前图片的宽：" + options.outWidth + "--未压缩之前图片的高："
                + options.outHeight + "--未压缩之前图片大小:" + options.outWidth * options.outHeight * 4 / 1024 / 1024 + "M");

        float hh = 800f;
        float ww = 480f;

        int be=1;
        if(w>h && w>ww){// 如果宽度大 则 根据宽度固定大小缩放
            be = (int) (options.outWidth/ww);
        }else if(w<h && h>hh){//如果高度大 则 根据高度固定大小缩放
            be= (int) (options.outHeight/hh);
        }

        if(be <= 0){
            be =1;
        }
        options.inSampleSize = be; // 设置缩放比例
//        options.inSampleSize = calculateInSampleSize(options, 100, 100);

//        Log.e(TAG, "onActivityResult: inSampleSize:" + options.inSampleSize);
//        options.inJustDecodeBounds = false;
//        Bitmap afterCompressBm = BitmapFactory.decodeFile(imgPath, options);

        bitmap = BitmapFactory.decodeFile(imgPath,options);
        compressImage(bitmap);

//      //默认的图片格式是Bitmap.Config.ARGB_8888
        Log.e(TAG, "onActivityResult: 图片的宽：" + bitmap.getWidth() + "--图片的高："
                + bitmap.getHeight() + "--图片大小:" + bitmap.getWidth() * bitmap.getHeight() * 4 / 1024 / 1024 + "M");
        return bitmap;
    }

    /**
     * 质量压缩方法
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 90;
        while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset(); // 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * 5.采样率压缩（设置图片的采样率，降低图片像素）
     *
     * @param filePath
     * @param file
     */
    public static void samplingRateCompress(String filePath, File file) {
        // 数值越高，图片像素越低
        int inSampleSize = 8;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
//          options.inJustDecodeBounds = true;//为true的时候不会真正加载图片，而是得到图片的宽高信息。
        //采样率
        options.inSampleSize = inSampleSize;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 把压缩后的数据存放到baos中
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        try {
            if (file.exists()) {
                file.delete();
            } else {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
