package cn.happy.myzxing.zxing.decode;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;

import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.Map;

/**
 * Author: created by ghappy on 2020-01-13 18:38
 * <p>
 * Description: 解析二维码图片
 */
public class QRCodeDecode {

    private static final String TAG = "QRCodeDecode";

    public static class DecodeAsyncTask extends AsyncTask<Bitmap, Integer, Result> {

        private WeakReference<Context> mContext;
        private Result result;

        public DecodeAsyncTask(Context mContext) {
            this.mContext = new WeakReference<>(mContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Result doInBackground(Bitmap... bitmaps) {
            result = handleQRCodeFormBitmap(bitmaps[0]);
            return result;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            if (result != null) {
                String text = result.getText();
                if (!TextUtils.isEmpty(text)) {
                    Log.e("识别图片中的二维码", String.valueOf(result));
                }
            } else {
                Toast.makeText(mContext.get(), "解码失败", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public static Result handleQRCodeFormBitmap(Bitmap bitmap) {

        if(bitmap == null){
            return null;
        }

        //得到图片的宽高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //得到图片的像素
        int[] pixels = new int[width * height];

        RGBLuminanceSource source = new RGBLuminanceSource(width,height,pixels);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        // 解码设置编码方式utf-8
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        //优化精度
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        //复制模式
        hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
        MultiFormatReader reader = new MultiFormatReader();

//        QRCodeReader reader = new QRCodeReader();
        Result result = null;
        try {
            Log.e("识别图片中的二维码","尝试第一次解析");
            Log.e("识别图片中的二维码", String.valueOf(reader));
            Log.e("识别图片中的二维码",String.valueOf(bitmap1));
            Log.e("识别图片中的二维码",String.valueOf(hints));
            result = reader.decode(bitmap1, hints);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("识别图片中的二维码","尝试第二次解析" + e.toString());

            // 尝试再次解析
            BinaryBitmap bitmap2 = new BinaryBitmap(new GlobalHistogramBinarizer(source));
            try {
                result = reader.decode(bitmap2, hints);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        Log.e("处理图片中的二维码",String.valueOf(result));
        return result;
    }

}
