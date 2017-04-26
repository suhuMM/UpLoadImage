package com.example.suhu.uploadimage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//http://blog.csdn.net/builder_taoge/article/details/70170590
public class MainActivity extends AppCompatActivity implements View.OnClickListener ,PopupWindow.OnDismissListener{
    private static final String IMAGE_FILE_PATH = Environment.getExternalStorageDirectory().toString() + "/android/data/"+"/cache/";
    private static final String IMAGE_FILE_NAME = "faceImage.jpg";
    private static final String FACE_PATH = IMAGE_FILE_PATH +IMAGE_FILE_NAME;

    private static final int IMAGE_REQUEST_CODE = 0;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int RESULT_REQUEST_CODE = 2;

    private BitmapDrawable drawable;

    private RoundedImage imageView;
    private PopupWindow photoPopWindow;
    private RelativeLayout pop_menu_background;

    private boolean isfacechanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (RoundedImage) findViewById(R.id.imageView);
        pop_menu_background = (RelativeLayout) findViewById(R.id.send_menu_background);
        imageView.setOnClickListener(this);
        showImage(imageView,FACE_PATH);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageView:
                shotSelectImages();
                break;
            case R.id.btn_paizhao:
                openCamera();
                break;
            case R.id.btn_select_pic:
                openPhoto();
                break;
        }
    }


    /**
     *@method 弹出框
     *@author suhu
     *@time 2017/4/25 15:51
     *
    */
    public void shotSelectImages() {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout myView = (LinearLayout) inflater.inflate(
                R.layout.select_images_from_local1, null);
        myView.findViewById(R.id.btn_paizhao).setOnClickListener(this);
        myView.findViewById(R.id.btn_select_pic).setOnClickListener(this);
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        photoPopWindow = new PopupWindow(myView, width - dip2px(40),
                RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        photoPopWindow.setOutsideTouchable(true);
        photoPopWindow.setBackgroundDrawable(new BitmapDrawable());
        photoPopWindow.showAtLocation(imageView, Gravity.CENTER, 0, 0);
        photoPopWindow.setOnDismissListener(this);
        pop_menu_background.setVisibility(View.VISIBLE);
    }



    @Override
    public void onDismiss() {
        // TODO Auto-generated method stub
        pop_menu_background.setVisibility(View.GONE);
    }

    /**
     *@method 打开相机
     *@author suhu
     *@time 2017/4/25 15:50
     *
    */
    private void openCamera(){
        Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (hasSdcard()) {
            intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME)));
        }
        startActivityForResult(intentFromCapture, CAMERA_REQUEST_CODE);
        pop_menu_background.setVisibility(View.GONE);
        photoPopWindow.dismiss();
    }

    /**
     *@method 打开相册
     *@author suhu
     *@time 2017/4/25 15:51
     *
    */
    private void openPhoto(){
        Intent intentFromGallery = new Intent(Intent.ACTION_PICK, null);
        intentFromGallery.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intentFromGallery, IMAGE_REQUEST_CODE);
        pop_menu_background.setVisibility(View.GONE);
        photoPopWindow.dismiss();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case IMAGE_REQUEST_CODE:
                    startPhotoZoom(data.getData());
                    break;
                case CAMERA_REQUEST_CODE:
                    if (hasSdcard()) {
                        File tempFile = new File(Environment.getExternalStorageDirectory() + File.separator + IMAGE_FILE_NAME);
                        startPhotoZoom(Uri.fromFile(tempFile));
                    } else {
                        Toast.makeText(MainActivity.this, "未找到存储卡，无法存储照片！", Toast.LENGTH_LONG).show();
                    }
                    break;
                case RESULT_REQUEST_CODE:
                    if (data != null) {
                        getImageToView(data);
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     *@method 显示本地图片
     *@param imageView
     *@param file_path
     *
    */
    private void showImage(ImageView imageView,String file_path){
        Bitmap bm = BitmapFactory.decodeFile(file_path);
        if (bm!=null){
            imageView.setImageBitmap(bm);
        }
    }


    /********************************************工具方法**************************************************/

    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public int dip2px(float dpValue) {
        final float scale = this.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     *@method 裁剪图片方法实现
     *@param uri
     *
    */
    public void startPhotoZoom(Uri uri) {

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 320);
        intent.putExtra("outputY", 320);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 2);
    }


    /**
     *@method 保存裁剪之后的图片数据
     *@param data
     *
    */
    private void getImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            drawable = new BitmapDrawable(photo);
            imageView.setImageDrawable(drawable);
            isfacechanged = true;
            SaveBitmapAsFile(FACE_PATH, photo);
        }
    }

    /**
     *@method 保存图片到内存卡
     *@param filepath
     *@param bitmap
     *
    */
    public boolean SaveBitmapAsFile(String filepath, Bitmap bitmap) {
        boolean flag = false;
        String dir = filepath.substring(0, filepath.lastIndexOf("/"));
        Log.e("", filepath);
        Log.e("", dir);
        File mDownloadDir = new File(dir);
        if (!mDownloadDir.exists()) {
            mDownloadDir.mkdirs();
        }
        File newFile = new File(filepath);
        FileOutputStream os = null;
        if (newFile.exists()) {
            newFile.delete();
        }
        try {
            newFile.createNewFile();
            os = new FileOutputStream(newFile);
            Matrix matrix = new Matrix();
            matrix.setScale(0.6f, 0.6f);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            flag = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }
}
