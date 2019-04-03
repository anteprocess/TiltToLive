package ninja.anteprocess.com.hakaishin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import anteprocess.com.zombie.R;

/**new LoadAllProducts().execute("http://anteprocess.versus.jp/Tester/getScores.php");
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ScoreActivity extends Activity {

    ArrayList<DataModel> dataModels;
    ListView listView;
    private static CustomAdapter adapter;
    private long mHighscore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_score);
        mHighscore =  getIntent().getLongExtra("score", 0);
        listView=(ListView)findViewById(R.id.list);

        dataModels= new ArrayList<>();
        new LoadAllProducts().execute("http://anteprocess.versus.jp/Tester/getScores.php");
        //dataModels.add(new DataModel("Apple Pie", "Android 1.0", "1","September 23, 2008"));
        //dataModels.add(new DataModel("Banana Bread", "Android 1.1", "2","February 9, 2009"));

       // adapter= new CustomAdapter(dataModels,getApplicationContext());
       // listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //DataModel dataModel= dataModels.get(position);


            }
        });

    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllProducts extends AsyncTask<String, String, String> {
        private String name = "";
        private String score = "";
        private String[] names;
        private String[] scores;
        private JSONArray result = null;
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(args[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(file_url);
                result= jsonObject .getJSONArray("result");
                scores = new String[result.length()];
                names = new String[result.length()];
                for(int i = 0; i < result.length(); i++) {
                    JSONObject json = result.getJSONObject(i);
                     name = json.getString("name");
                     score = json.getString("score");
                    names[i] = name;
                    scores[i] = score;
                    android.util.Log.d("Test",name+"|"+score);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    int rank = 1;
                    if (result != null) {
                        for(int i = 0; i < result.length(); i++) {
                            dataModels.add(new DataModel(names[i], scores[i],"Rank "+rank));
                            rank++;
                        }
                    }
                    adapter= new CustomAdapter(dataModels,getApplicationContext());
                    listView.setAdapter(adapter);
                    View head = getLayoutInflater().inflate(R.layout.header_layout, null);
                    //TODO: add the footer that directs to the website
                    if (result != null) {
                        View footerView = ((LayoutInflater) getApplicationContext().
                                getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                                R.layout.footer_layout, null, false);
                        TextView website = (TextView)footerView.findViewById(R.id.website);
                        TextView privacy = (TextView)footerView.findViewById(R.id.privacy);

                        website.setOnClickListener(new ConnectToWebsite(getBaseContext(),
                                "https://play.google.com/store/apps/developer?id=Anteprocess%20Enterprise"));
                        privacy.setOnClickListener(new ConnectToWebsite(getBaseContext(),
                                "http://anteprocess.versus.jp/privacy-policy/"));

                        listView.addFooterView(footerView);
                    } else {
                        head = getLayoutInflater().inflate(R.layout.header_no_data, null);
                    }
                    TextView currentScoreTextId = (TextView)head.findViewById(R.id.currentScoreTextId);
                    if (currentScoreTextId != null) {
                        currentScoreTextId.setText("My current highscore is "+mHighscore);
                    }
                    listView.addHeaderView(head);

                }
            });

        }

    }

}

 class ConnectToWebsite implements View.OnClickListener{
     public String mUrl;
     public Context mContext;
    ConnectToWebsite(Context context, String url) {
        mUrl = url;
        mContext = context;
    }
     @Override
     public void onClick(View v) {
         Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
         browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         mContext.startActivity(browserIntent);
     }
 }
