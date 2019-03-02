package textspeech.thezaxis.speechtext;


import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import textspeech.thezaxis.speechtext.Helper.VolleyCallBack;
import textspeech.thezaxis.speechtext.Helper.VolleyRequest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MainActivity extends Activity implements AIListener{

    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    Button logoutButton;
    FirebaseUser mUser;
    List<Chat> chatList = new ArrayList<>();
    ArrayList<String> actionList = new ArrayList<>();
    String name;
    boolean flag;
    Result result;


    //private Button listenButton;
    Button listenButton;
    private RecyclerView recyclerView;
    private TextView resultTextView, messageText, queryText;
    private AIService aiService;
    private ChatAdapter mAdapter;
    private final int REQ_CODE_SPEECH_INPUT = 100;


    private static String TAG = "PermissionDemo";
    private static final int RECORD_REQUEST_CODE = 101;


    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                RECORD_REQUEST_CODE);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            //Log.i(TAG, "Permission to record denied");
            makeRequest();
        }
        flag = false;
        listenButton = findViewById(R.id.listenButton);
        recyclerView = findViewById(R.id.recycler_view);
        logoutButton = findViewById(R.id.logout_button);
        initializeActionList();

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mAdapter = new ChatAdapter(chatList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });


        final AIConfiguration config = new AIConfiguration("5987e904ba4b4a699b296c548e336fc7",
                AIConfiguration.SupportedLanguages.DEFAULT,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(this, config);
        aiService.setListener(this);



        listenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenButtonOnClick();
            }
        });



        /*txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        // hide the action bar
        //getActionBar().hide();

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });*/

    }

    private void initializeActionList() {
        actionList.add("query.name");
        actionList.add("query.phone");
    }

    public void listenButtonOnClick() {
        aiService.startListening();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));

//                    String str = data.getStringExtra(RecognizerIntent.EXTRA_RESULTS);
//                    txtSpeechInput.setText(str);
                }
                break;
            }

        }
    }

    @Override
    public void onResult(AIResponse response) {

        result = response.getResult();
        // Get parameters
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }
        String receivedMessage = result.getFulfillment().getSpeech();
        String sentMessage = result.getResolvedQuery();
        String action = result.getAction();
        Chat chat = new Chat(sentMessage, "me");
        chatList.add(chat);
        changeRecyclerView();
        resolveQuery(action);
        if (!actionList.contains(action)){
            chat = new Chat(receivedMessage, "him");
            chatList.add(chat);
            changeRecyclerView();
        }
    }
    public void resolveQuery(String action){
        if (action.equals("query.phone")){
            if (mUser!=null){
                String phone = mUser.getPhoneNumber().toString();
                String receivedMessage = result.getFulfillment().getSpeech();
                receivedMessage = receivedMessage+ " " +phone;
                Chat chat = new Chat(receivedMessage, "him");
                chatList.add(chat);
                changeRecyclerView();
            }
        }
        if (action.equals("query.name")){
            //String name;
            if(mUser!=null){
                String phone = mUser.getPhoneNumber();
                if (phone.contains("+91")){
                    phone = phone.substring(3, phone.length());
                }
                VolleyRequest request1 = new VolleyRequest();
                String tableName = "Customer";
                String query = "Select CustomerName from " +tableName +" where CustomerContact = "+phone;
                //Toast.makeText(this, ""+query, Toast.LENGTH_SHORT).show();
                request1.fetchData(new VolleyCallBack(){
                    @Override
                    public void onSuccess(JSONArray resultArray){
                        //Toast.makeText(MainActivity.this, ""+result, Toast.LENGTH_SHORT).show();
                        try{
                            JSONArray json =resultArray;
                            JSONObject obj;
                            //int size=json.length();
                            obj = json.getJSONObject(0);
                            name = obj.getString("CustomerName");
                            String receivedMessage = result.getFulfillment().getSpeech();
                            receivedMessage = receivedMessage+ " " +name;
                            Chat chat = new Chat(receivedMessage, "him");
                            chatList.add(chat);
                            changeRecyclerView();

                            //return name;
                            //Toast.makeText(MainActivity.this, ""+name, Toast.LENGTH_LONG).show();
                        }catch(JSONException e) {
                            //Toast.makeText(MainActivity.this,"No records present..",Toast.LENGTH_SHORT).show();
                        }catch(Exception e){
                            //Toast.makeText(MainActivity.this,"An error has occured.."+e, Toast.LENGTH_SHORT).show();
                        }
                        //return name;
                    }
                }, this, query);



                /*
                String response = request1.func(query, this);
                Toast.makeText(this, ""+response, Toast.LENGTH_SHORT).show();
                /*try {
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String name = jsonObject.getString("CustomerName");
                    return name;
                    //String chatMessage = name;
                    //Chat chat = new chat
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
            }
        }
    }

    @Override
    public void onError(AIError error) {
        //resultTextView.setText(error.toString());
        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/


    private void changeRecyclerView(){
        mAdapter.notifyDataSetChanged();
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                // Call smooth scroll
                recyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            }
        });
    }


}