package com.example.kuetcentrallibrary.Auxilaries;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;

public class PDFSaver {

    public static void save(Context context, View content, Uri fileUri) {
        if(ExternalStorageVerify.isExternalStorageWritable()) {
            // open a new document
            PdfDocument document = new PdfDocument();

            if( content.getWidth() < 0 || content.getHeight() < 0 ) {
                Toast.makeText(context,"There was a internal problem please try again",Toast.LENGTH_LONG).show();
                return;
            }
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo
                    .Builder(content.getWidth(), content.getHeight()+30, 1).create();
            // start a page
            PdfDocument.Page page = document.startPage(pageInfo);
            // draw something on the page
            content.draw(page.getCanvas());
            // finish the page
            document.finishPage(page);
            // open the file
            ParcelFileDescriptor pfd;
            try {
                pfd = context.getContentResolver().openFileDescriptor(fileUri, "w");
                // write the document content
                FileOutputStream outputStream;
                if (pfd != null) {
                    outputStream = new FileOutputStream(pfd.getFileDescriptor());
                }
                else {
                    Toast.makeText(context,"There was a internal problem please try again",Toast.LENGTH_LONG).show();
                    return;
                }
                document.writeTo(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //close the document
            document.close();
        }
        else {
            Toast.makeText(context,"Sorry, Problem with writing file to SDCard",Toast.LENGTH_LONG).show();
        }
    }

    public static void save(Context context, View content, ListView listView, Uri fileUri) {
        if(ExternalStorageVerify.isExternalStorageWritable()) {
            // open a new document
            PdfDocument document = new PdfDocument();

            int totalCount = listView.getCount();
            Adapter adapter = listView.getAdapter();

            //calculating the margin and total number of page
            View dummyView = adapter.getView(0,null,listView);
            dummyView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            dummyView.layout(0, 0, dummyView.getMeasuredWidth(), dummyView.getMeasuredHeight());

            int margin = Math.max( content.getWidth() - dummyView.getWidth(), 0 ) / 2;

            int viewNum = 0;

            if( content.getWidth() < 0 || content.getHeight() < 0 ) {
                Toast.makeText(context,"There was a internal problem please try again",Toast.LENGTH_LONG).show();
                return;
            }

            for(int pageNum = 1; ; pageNum++) {
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(content.getWidth(), content.getHeight(), pageNum)
                        .create();

                // start a page
                PdfDocument.Page page = document.startPage(pageInfo);

                // draw something on the page
                int height = 0;
                boolean ended = false;
                for (int contentHeight = 2*margin; ;  ) {
                    View view = adapter.getView(viewNum,null,listView);

                    view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
                    Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                            Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    view.draw(canvas);

                    contentHeight += bitmap.getHeight();
                    if(contentHeight > content.getHeight()) break;

                    page.getCanvas().drawBitmap(bitmap,margin,margin + height,null);
                    height += bitmap.getHeight();

                    viewNum++;
                    if(viewNum >= totalCount) {
                        ended = true;
                        break;
                    }
                }

                // finish the page
                document.finishPage(page);

                if(ended) break;
            }

            // open the file
            ParcelFileDescriptor pfd;
            try {
                pfd = context.getContentResolver().
                        openFileDescriptor(fileUri, "w");

                // write the document content
                FileOutputStream outputStream = null;
                if (pfd != null) {
                    outputStream = new FileOutputStream(pfd.getFileDescriptor());
                }
                else {
                    Toast.makeText(context,"There was a internal problem please try again",Toast.LENGTH_LONG).show();
                    return;
                }

                document.writeTo(outputStream);

            } catch (IOException e) {
                e.printStackTrace();
            }

            //close the document
            document.close();
        }
        else {
            Toast.makeText(context,"Sorry, Problem with writing file to SDCard",Toast.LENGTH_LONG).show();
        }
    }

}
