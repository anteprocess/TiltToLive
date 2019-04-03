package backend;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Created by michael on 2017/02/13.
 */

public class DBUtil {

    private static DBUtil instance = null;
    public static String mId;
    public static String mName;
    public static String mScore;

    protected DBUtil() {

    }
    public static DBUtil getInstance() {
        if(instance == null) {
            instance = new DBUtil();
        }
        android.util.Log.d("Test","getInstance");
        return instance;
    }

    public static void  saveScoreValues(String id, String name, String score) {
        mId = id;
        mName = name;
        mScore = score;
        android.util.Log.d("Test","saveScoreValues");
         new saveScores().execute();
    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    private static class saveScores extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            android.util.Log.d("Test","onPreExecute");
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            android.util.Log.d("Test","doInBackground");
            try {
                exec_post();

            } finally {
                android.util.Log.d("Test","finally");
                return null;
            }

        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products

        }

    }


    // POST通信を実行（AsyncTaskによる非同期処理を使わないバージョン）
    private static void exec_post() {

        Log.d("posttest", "postします");
        String ret = "";

        // URL
        URI url = null;
        try {
            url = new URI( "http://anteprocess.versus.jp/Tester/saveScores.php" );
            Log.d("posttest", "URLはOK");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            ret = e.toString();
        }

        // POSTパラメータ付きでPOSTリクエストを構築
        HttpPost request = new HttpPost( url );
        List<NameValuePair> post_params = new ArrayList<NameValuePair>();
        post_params.add(new BasicNameValuePair("id", mId));
        post_params.add(new BasicNameValuePair("name", mName));
        post_params.add(new BasicNameValuePair("score", mScore));
        try {
            // 送信パラメータのエンコードを指定
            request.setEntity(new UrlEncodedFormEntity(post_params, "UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        // POSTリクエストを実行
        DefaultHttpClient httpClient = new DefaultHttpClient();
        try {
            Log.d("posttest", "POST開始");
            ret = httpClient.execute(request, new ResponseHandler<String>() {

                @Override
                public String handleResponse(HttpResponse response) throws IOException
                {
                    Log.d(
                            "posttest",
                            "レスポンスコード：" + response.getStatusLine().getStatusCode()
                    );

                    // 正常に受信できた場合は200
                    switch (response.getStatusLine().getStatusCode()) {
                        case HttpStatus.SC_OK:
                            Log.d("posttest", "レスポンス取得に成功");

                            // レスポンスデータをエンコード済みの文字列として取得する
                            return EntityUtils.toString(response.getEntity(), "UTF-8");

                        case HttpStatus.SC_NOT_FOUND:
                            Log.d("posttest", "データが存在しない");
                            return null;

                        default:
                            Log.d("posttest", "通信エラー");
                            return null;
                    }

                }

            });

        } catch (IOException e) {
            Log.d("posttest", "通信に失敗：" + e.toString());
        } finally {
            // shutdownすると通信できなくなる
            httpClient.getConnectionManager().shutdown();
        }

        // 受信結果をUIに表示


    }


}
