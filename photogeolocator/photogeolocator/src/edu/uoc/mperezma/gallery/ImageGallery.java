/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uoc.mperezma.gallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import edu.uoc.mperezma.main.R;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author mperezma
 */
public class ImageGallery extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.gallery);

        GridView g = (GridView) findViewById(R.id.myGrid);
        g.setAdapter(new ImageAdapter(this, getIntent().getExtras().getStringArrayList("fileNames")));
        g.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Intent result = new Intent();
                String fileName = ImageGallery.this.getIntent().getExtras().getStringArrayList("fileNames").get(position);
                result.putExtra("fileName", fileName);
                ImageGallery.this.setResult(RESULT_OK, result);
                finish();
            }
        });
    }

    public class ImageAdapter extends BaseAdapter {

        HashMap<String, Bitmap> bitmapCache = new HashMap<String, Bitmap>();

        public ImageAdapter(Context c, List<String> fileNames) {
            this.fileNames = fileNames;
            mContext = c;
        }

        public int getCount() {
            return fileNames.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                    imageView = new ImageView(mContext);
                    imageView.setLayoutParams(new GridView.LayoutParams(145, 145));
                    imageView.setAdjustViewBounds(false);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

                try {
                    Bitmap bm = bitmapCache.get(fileNames.get(position));
                    if (bm == null) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 16;
                        bm = BitmapFactory.decodeFile(fileNames.get(position), options);
                        bitmapCache.put(fileNames.get(position), bm);
                    }
                    imageView.setImageBitmap(bm);
                } catch (Exception e) {
                }

            return imageView;
        }
        private Context mContext;
        private List<String> fileNames;
    }

}
