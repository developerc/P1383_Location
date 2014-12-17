package com.hmkcode.android;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
 
public class MainActivity extends Activity implements OnClickListener {
 
    EditText etResponse;
    TextView tvIsConnected;
    Button btnPost;
    String resultPOST;
    String matchtemper = "";
    String authtok = "";    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
 
        // get reference to the views
        etResponse = (EditText) findViewById(R.id.etResponse);
        tvIsConnected = (TextView) findViewById(R.id.tvIsConnected);
        btnPost = (Button) findViewById(R.id.btnPost);
 
        // check if you are connected or not
        if(isConnected()){
            tvIsConnected.setBackgroundColor(0xFF00CC00);
            tvIsConnected.setText("You are connected");
        }
        else{
            tvIsConnected.setText("You are NOT conncted");
        }
 
        // show response on the EditText etResponse 
       // etResponse.setText("http://hmkcode.com/examples/index.php");
        new HttpAsyncTask().execute("http://192.168.1.34:3000/coordinates");
     // add click listener to Button "POST"
        btnPost.setOnClickListener(this);
    }
 
    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {
 
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
 
            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
 
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
 
            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";
 
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
 
        return result;
    }
 
    // convert inputstream to String
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
 
        inputStream.close();
        return result;
 
    }
 
    // check network connection
    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) 
                return true;
            else
                return false;   
    }
    
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
 
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
         // работаем с регулярками первый проход
           Pattern pattern = Pattern.compile("(meta content)(.*?)(csrf-token\")");
           Matcher matcher = pattern.matcher(result);
          matcher.find();
        	   matchtemper = matcher.group();       
        	  // etResponse.setText(matchtemper);     	   
                     
        // второй проход
            pattern = Pattern.compile("(\")(.*?)(\")");
            matcher = pattern.matcher(matchtemper);
            matcher.find();
            matcher.find();
            matcher.find();
            authtok = matcher.group(2);
            etResponse.setText(authtok);
   
       }
    }   
    
    @Override
    public void onClick(View view) {
 
        switch(view.getId()){
            case R.id.btnPost:
              //  if(!validate())
                    Toast.makeText(getBaseContext(), "Enter some data!", Toast.LENGTH_LONG).show();
                // call AsynTask to perform network operation on separate thread
                new HttpAsyncTaskPost().execute("http://192.168.1.34:3000/coordinates");
            break;
        }
    }
    
    private class HttpAsyncTaskPost extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
 
           HttpClient client = 
  	              new DefaultHttpClient();
  	          HttpPost post = 
  	             new HttpPost(urls[0]);
  	          //**************************
  	        List<BasicNameValuePair> coordinate = new ArrayList<BasicNameValuePair>(); 
  	        coordinate.add(new BasicNameValuePair("coordinate[userid]", "9"));
  	        coordinate.add(new BasicNameValuePair("coordinate[lon]", "50"));
  	        coordinate.add(new BasicNameValuePair("coordinate[lat]", "50"));
  	          //**************************
  	          List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
  	        pairs.add(new BasicNameValuePair("utf8=", "(byte)0xE0"));
  	        pairs.add(new BasicNameValuePair("authenticity_token", authtok));
  	       // pairs.add(new BasicNameValuePair("userid", "7"));
  	        //pairs.add(new BasicNameValuePair("lon", "50"));
  	        //pairs.add(new BasicNameValuePair("lat", "50"));
  	        pairs.addAll(coordinate);
  	        pairs.add(new BasicNameValuePair("commit", "Create Coordinate"));
  	        
  	          try {
				post.setEntity(new UrlEncodedFormEntity(pairs));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	          try {
				HttpResponse response = client.execute(post);
				 BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"windows-1251"));
		          StringBuilder sb = new StringBuilder();
		          String line = null;
		          while ((line = reader.readLine()) != null) {
		          sb.append(line + System.getProperty("line.separator"));
		          } 
		          
		                 resultPOST = sb.toString(); //получили ответ
		               //  etResponse.setText(resultPOST);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	          
        	return null;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
       }
    }    
 
}