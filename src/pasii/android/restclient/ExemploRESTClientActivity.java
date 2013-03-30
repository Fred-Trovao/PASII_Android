package pasii.android.restclient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ExemploRESTClientActivity extends Activity {

	private ProgressDialog dialog;
	private TextView txtResposta;
	private boolean isGet = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        txtResposta = (TextView)findViewById(R.id.txtResposta);
    }
    
	public void btnWSClick(View v) {
		this.isGet = true;
		new TarefaWS().execute();
	}
	
	public void btnWSClickPost(View v){
		this.isGet = false;		
		new TarefaWS().execute();
	}
	
	public byte[] getBytes(InputStream is) {
		try {
			int bytesLidos;
			ByteArrayOutputStream bigBuffer = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];

			while ((bytesLidos = is.read(buffer)) > 0) {
				bigBuffer.write(buffer, 0, bytesLidos);
			}

			return bigBuffer.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getRESTFileContent(String url) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);

		try {
			HttpResponse response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream instream = entity.getContent();
				String result = new String(getBytes(instream));

				instream.close();
				return result;
			}
		} catch (Exception e) {
			Log.e("PASII", "Falha ao acessar Web service", e);
		}
		return null;
	}
	
	class TarefaWS extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			txtResposta.setText("");
			dialog = ProgressDialog.show(ExemploRESTClientActivity.this, "Aguarde",
					"Acessando WS...");
		}

		@Override
		protected String doInBackground(Void... params) {
			if (isGet)
				return lerTodosClubes();
			else
				return inserirClube();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (txtResposta == null){
				txtResposta.setText("Erro ao acessar web service");
			} else {
				txtResposta.setText(result);
			}
			dialog.dismiss();
		}
		
		private String lerTodosClubes() {
			String result = getRESTFileContent("http://bolaoshow.herokuapp.com/service/clubes");
			if (result == null) {
				Log.e("PASII", "Falha ao acessar WS");
				return null;
			}

			try {
				JSONObject json = new JSONObject(result);
				JSONArray clubesArray = json.getJSONArray("clube");

				JSONObject clubeJson;

				StringBuffer sb = new StringBuffer();
				
				for (int i = 0; i < clubesArray.length(); i++) {
					clubeJson = new JSONObject(clubesArray.getString(i));

					sb.append("clube=" + clubeJson.getInt("clube"));
					sb.append("|nome=" + clubeJson.getString("nome"));
					sb.append('\n');
				}
				
				return sb.toString();
				
			} catch (JSONException e) {
				Log.e("PASII", "Erro no parsing do JSON", e);
			}
			return null;
		}
		
		private String inserirClube() {  
			EditText editText = (EditText) findViewById(R.id.edit_clube);  
			try {  
			    URL url = new URL("http://bolaoshow.herokuapp.com/service/clubes");  
			    HttpURLConnection conexao =   
			      (HttpURLConnection)url.openConnection();  
			  
			    conexao.setRequestMethod("POST");  
			    conexao.addRequestProperty(  
			      "Content-type", "application/json");  
			  
			    conexao.setDoOutput(true);  
			      
			    conexao.connect();  
			    
			    String msgJson = "{\"nome\":\"" + editText.getText().toString() + "\"}"; 
			    
			    OutputStream os = conexao.getOutputStream();  
			    os.write(msgJson.getBytes());  
			    os.flush();  
			      
			    InputStream is = conexao.getInputStream();  
			    return is.toString();  
			      
			  } catch (Exception e) {  
			    e.printStackTrace();  
			    return "ERRO!";  
			  }  
		}
	}
}