package com.example.kuetcentrallibrary.Auxilaries;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageSaveRetrieve {

    public static void saveImage(Context context, String fileName, Bitmap img){

//        File dir = context.getDir(dirName,Context.MODE_PRIVATE);
//        File newFile = new File(dir,fileName);

        try {
//            if(!newFile.exists()) newFile.createNewFile();

            FileOutputStream ostream = context.openFileOutput(fileName,Context.MODE_PRIVATE);
//            ostream.write(fileName.getBytes());
            img.compress(Bitmap.CompressFormat.PNG,100,ostream);
            ostream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context,"Sorry there was a problem\n"+e,Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,"Sorry there was a problem\n"+e,Toast.LENGTH_LONG).show();
        }
    }

    public static Bitmap retrieveImage(Context context, String fileName){
        Bitmap bitmap = null;

        try {
            FileInputStream istream = context.openFileInput(fileName);
            bitmap = BitmapFactory.decodeStream(istream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context,"Sorry there was a problem\n"+e,Toast.LENGTH_LONG).show();
        }

        return bitmap;
    }

}
