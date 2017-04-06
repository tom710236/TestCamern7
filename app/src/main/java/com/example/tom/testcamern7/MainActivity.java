package com.example.tom.testcamern7;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Environment.DIRECTORY_PICTURES;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_CONTACTS = 0;
    int i=0;
    SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        



        GridView gridView = (GridView)findViewById(R.id.grid);
        File picDir = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES);
        String[] from = {MediaStore.Images.Thumbnails.DATA,MediaStore.Images.Media.DISPLAY_NAME};
        int[] to = new int[] {R.id.imageView};
        adapter = new SimpleCursorAdapter(
                getBaseContext(),
                R.layout.thumb_item,
                null,
                from,
                to,
                0);
        gridView.setAdapter(adapter);
        getSupportLoaderManager().initLoader(0,null,this);
    }
    public void onPicture (View v){
        //拍照權限
        int permission = ActivityCompat.checkSelfPermission(this,
                CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            //若尚未取得權限，則向使用者要求允許聯絡人讀取與寫入的權限，REQUEST_CONTACTS常數未宣告則請按下Alt+Enter自動定義常數值。
            ActivityCompat.requestPermissions(this,
                    new String[]{CAMERA},
                    REQUEST_CONTACTS);
        } else {
            //已有權限，可進行以下方法
            makePicture();
        }
    }
    //呼叫Intent 喚起拍照功能
    private void makePicture() {
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  //建立動作為拍照的意圖
        startActivityForResult(it, 100);   //啟動意圖並要求傳回資料

    }
    //拍照後的預覽畫面設定
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK && requestCode==100) {
            Bundle extras = data.getExtras();         //將 Intent 的附加資料轉為 Bundle 物件
            Bitmap bmp = (Bitmap) extras.get("data"); //由 Bundle 取出名為 "data" 的 Bitmap 資料
            ImageView imv = (ImageView)findViewById(R.id.imageView2);
            imv.setImageBitmap(bmp);    	    	  //將 Bitmap 資料顯示在 ImageView 中

        }
        else {
            Toast.makeText(this, "沒有拍到照片", Toast.LENGTH_LONG).show();
        }
    }
    public void onSave (View v){
        int permission = ActivityCompat.checkSelfPermission(this,
                WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(this,
                READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED) {
            //若尚未取得權限，則向使用者要求允許聯絡人讀取與寫入的權限，REQUEST_CONTACTS常數未宣告則請按下Alt+Enter自動定義常數值。
            ActivityCompat.requestPermissions(this,
                    new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE},
                    REQUEST_CONTACTS);
        } else {
            //已有權限，可進行以下方法
            //i++;
            makeSave();
            File picDir = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES);
            Uri uri2 = Uri.parse(String.valueOf(picDir));
            final Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            final Intent intent = new Intent(Intent.ACTION_PICK, uri2);
            intent.setType("image/*");
            startActivity(intent);
        }
    }

    private void makeSave() {
        if (saveToPictureFolder()) {
            Toast.makeText(MainActivity.this, "儲存成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "儲存失敗", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean saveToPictureFolder() {
        //取得 Pictures 目錄
        File picDir = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES);
        Log.d(">>>", "Pictures Folder path: " + picDir.getAbsolutePath());
        //假如有該目錄
        if (picDir.exists()) {
            //儲存圖片
            ImageView imv = (ImageView)findViewById(R.id.imageView2);
            File pic = new File(picDir, "pic"+System.currentTimeMillis()+".jpg");
            imv.setDrawingCacheEnabled(true);
            imv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
            Bitmap bmp = imv.getDrawingCache();
            return saveBitmap(bmp, pic);


        }
        return false;
    }

    private boolean saveBitmap(Bitmap bmp, File pic) {
        if (bmp == null || pic == null) return false;
        //
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(pic);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            scanGallery(this, pic);
            Log.d(">>>", "bmp path: " + pic.getAbsolutePath());
            return true;
        } catch (Exception e) {
            Log.e(">>>", "save bitmap failed!");
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void scanGallery(Context ctx, File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        ctx.sendBroadcast(mediaScanIntent);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        File picDir = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES);
        Uri uri2 = Uri.parse(String.valueOf(picDir));
        return new CursorLoader(this,uri2,null,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
