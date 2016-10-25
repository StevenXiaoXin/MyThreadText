package com.example.liuzhuang.mythreadtext;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by liuzhuang on 2016/10/18.
 */
public class ImageLoader {
    private ImageView mImageView;
    private String mUrl;
    private LruCache<String, Bitmap> mCaches;
    private ListView mListView;
    private Set<NewsAsyncTask> mTask;

    /**
     *构造方法来初始缓存的可用大小
     */
    public ImageLoader(ListView listView) {
        mListView = listView;
        mTask = new HashSet<>();
        //获取最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        //设置可用缓存大小
        int cachesSize = maxMemory / 4;
        mCaches = new LruCache<String, Bitmap>(cachesSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //在每次存入缓存的时候调用,告知传入东西得大小
                return value.getByteCount();
            }
        };
    }

    /**
     * 增加到缓存
     * @param url
     * @param bitmap
     */
    public void addBitmapToCaches(String url, Bitmap bitmap) {
        if (getBitmapFromCaches(url) == null) {
            mCaches.put(url, bitmap);
        }
    }

    /**
     * 从缓存中获取数据
     * @param url
     * @return
     */
    public Bitmap getBitmapFromCaches(String url) {
        return mCaches.get(url);
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (mImageView.getTag().equals(mUrl)) {

                mImageView.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };

    /**
     * 停止异步的加载任务
     */
    public void cancelAllTask() {
        if (mTask != null) {
            for (NewsAsyncTask task:mTask ) {
                task.cancel(true);

            }
        }
    }

    /**
     * 加载指定范围内的图片
     * @param start
     * @param end
     */
    public void loadImages(final int start, final int end) {

        for (int i=start;i<end;i++) {
            String url = NewsAdapter.URLS[i];
            //从缓存中取出对应的图片，如果没有，再去网络加载
            Bitmap bitmap = getBitmapFromCaches(url);
            if (bitmap == null) {

                NewsAsyncTask task = new NewsAsyncTask(url);
                task.execute(url);
                mTask.add(task);
//                new NewsAsyncTask(url).execute(url);
            } else {
                ImageView imageView = (ImageView) mListView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }

        }
    }

    /**
     * 利用多线程和handler来对imageView进行设置
     * @param imageView
     * @param url
     */

    public void showImageByThread(final ImageView imageView, final String url) {
        mImageView = imageView;
        mUrl = url;
        new Thread() {
            @Override
            public void run() {
                super.run();
                Bitmap bitmap = null;
                if (getBitmapFromCaches(url) == null) {
//                    bitmap = getBitmapFromUrl(url);
//                    imageView.setImageResource(R.mipmap.ic_launcher);
                     bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
//                    //将不在缓存中的图片加到缓存中
//                    addBitmapToCaches(url, bitmap);
                } else {
                    bitmap = getBitmapFromCaches(url);
                }
                Message message = Message.obtain();
                message.obj = bitmap;
                handler.sendMessage(message);
            }
        }.start();
    }

    /**
     * 通过url来从网络读取图片bitmap
     * @param urlString
     * @return
     */
    public Bitmap getBitmapFromUrl(String urlString) {
        Bitmap bitmap;
        InputStream is = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(connection.getInputStream());
            bitmap = BitmapFactory.decodeStream(is);
            connection.disconnect();
            return bitmap;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 利用AsyncTask进行显示图片
     * @param imageView
     * @param url
     */
    public void showImageByAsyncTask(ImageView imageView, String url) {

        //从缓存中取出对应的图片，如果没有，再去网络加载
        Bitmap bitmap = getBitmapFromCaches(url);
        if (bitmap == null) {
//            new NewsAsyncTask(url).execute(url);
            imageView.setImageResource(R.mipmap.ic_launcher);
        } else {
            imageView.setImageBitmap(bitmap);
        }

    }

    private class NewsAsyncTask extends AsyncTask<String, Void, Bitmap> {

//        private ImageView mImageView;
        private String mUrl;

        public NewsAsyncTask(String url) {
//            mImageView = imageView;
            mUrl = url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            Bitmap bitmap=getBitmapFromUrl(url);
            if (bitmap != null) {
                //将不在缓存中的图片存到缓存中
                addBitmapToCaches(url,bitmap);
            }
            return bitmap;
        }


        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView imageView = (ImageView) mListView.findViewWithTag(mUrl);
            if (imageView!=null&&bitmap!=null) {
                imageView.setImageBitmap(bitmap);
            }
            mTask.remove(this);
        }

    }


}
