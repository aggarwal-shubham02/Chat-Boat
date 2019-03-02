package textspeech.thezaxis.speechtext.Helper;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VolleyRequest {
    private RequestQueue requestQueue;
    JSONArray jsonArray;
    String response1;


    public void fetchData(final VolleyCallBack callback, Context context, String query){

        //JSONArray jsonArray;
        //final Context context2= context;
        //String query = "Select CustomerName from Customer where CustomerContact = 9958146938";
        String url="http://www.skyline69.co.nf/request.php?query="+query;
        requestQueue= Volley.newRequestQueue(context);
        StringRequest request =new StringRequest(Request.Method.GET,url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONArray json =new JSONArray(response);
                            JSONObject obj;
                            //int size=json.length();
                            obj = json.getJSONObject(0);
                            //Toast.makeText(MainActivity.this, ""+name, Toast.LENGTH_LONG).show();
                            callback.onSuccess(json);
                        }catch(JSONException e) {
                            //Toast.makeText(MainActivity.this,"No records present..",Toast.LENGTH_SHORT).show();
                        }catch(Exception e){
                            //Toast.makeText(MainActivity.this,"An error has occured.."+e, Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(MainActivity.this,"Cannot connect to the server, check your Internet conection and try again...",Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> param=new HashMap<>();
                //param.put("query","select * from Customer");
                return param;
            }
        };
        requestQueue.add(request);
        //return jsonArray;
    }
    /*public String func(String query, Context context){
        //fetchData(query, context);
        return response1;
    }*/
    /*public void getString(final VolleyCallBack callback) {
        String query = "Select CustomerName from Customer where CustomerContact = 9958146938";
        String url = "http://www.skyline69.co.nf/request.php?query=" + query;
        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // (optionally) some manipulation of the response
                callback.onSuccess(response);
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(MainActivity.this,"Cannot connect to the server, check your Internet conection and try again...",Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> param=new HashMap<>();
                //param.put("query","select * from Customer");
                return param;
            }
        };
        requestQueue.add(strReq);
    }*/
}
