package com.example.liuzhuang.mythreadtext;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private ListView mListView;
    private String URL = "http://www.imooc.com/api/teacher?type=4&num=30";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.lv_main);

        new NewsAsyncTask().execute(URL);

    }

    /**
     * 实现网络的异步访问
     */

    class NewsAsyncTask extends AsyncTask<String, Void, List<NewsBean>> {

        @Override
        protected List<NewsBean> doInBackground(String... strings) {
            return getJsonData(strings[0]);
        }

        @Override
        protected void onPostExecute(List<NewsBean> newsBeen) {
            super.onPostExecute(newsBeen);

            NewsAdapter adapter = new NewsAdapter(MainActivity.this, newsBeen, mListView);
            mListView.setAdapter(adapter);

        }
    }

    /**
     * 将url对应的json格式数据转化成newsbean类型
     * @param url
     * @return
     */
    private List<NewsBean> getJsonData(String url) {
        List<NewsBean> newsBeanList = new ArrayList<>();
        try {
            String jsonString = readStream(new URL(url).openStream());
            JSONObject jsonObject;
            NewsBean newsBean;
            jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
//           int status= jsonObject.getInt("status");
//            Log.e("------->", status + "");
            for (int i=0;i<jsonArray.length();i++) {
                jsonObject = jsonArray.getJSONObject(i);
                newsBean = new NewsBean();
                newsBean.newsIconUrl = jsonObject.getString("picSmall");
                newsBean.newsTitle = jsonObject.getString("name");
                newsBean.newsContent = jsonObject.getString("description");
                newsBeanList.add(newsBean);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return newsBeanList;
    }

    /**
     * 通过is解析网页返回的数据
     * @param is
     * @return
     */
    private String readStream(InputStream is) {
//先传进来字节流，然后通过InputStreamReader变成字符流
        InputStreamReader isr;
        String result = "";
        try {
            String line = "";
            //指定字符集格式
            isr = new InputStreamReader(is, "utf-8");
            //通过BufferedReader来读取出来
            BufferedReader br = new BufferedReader(isr);
            //用while来循环拼接到result里
            while ((line = br.readLine()) != null) {
                result += line;
            }

            is.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
