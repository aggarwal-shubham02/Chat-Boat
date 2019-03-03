package textspeech.thezaxis.speechtext;


import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import textspeech.thezaxis.speechtext.Helper.CustomDialogClass;
import textspeech.thezaxis.speechtext.Helper.VolleyCallBack;
import textspeech.thezaxis.speechtext.Helper.VolleyRequest;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.JsonElement;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
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

    ImageButton listenButton;
    ImageButton keyboardSwitchButton,  profileButton;
    Button sendButton;
    private EditText editQuery;
    private RecyclerView recyclerView;
    private TextView resultTextView, messageText, queryText;
    private AIService aiService;
    private ChatAdapter mAdapter;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private String customerID;
    boolean keyboardSwitch = false;
    private static String TAG = "PermissionDemo";
    private static final int RECORD_REQUEST_CODE = 101;
    TextToSpeech tts;


    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                RECORD_REQUEST_CODE);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        String phone = mUser.getPhoneNumber();
        if (phone.contains("+91")){
            phone = phone.substring(3, phone.length());
        }
        initializeCustomer(phone);
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int ttsLang = tts.setLanguage(new Locale("en", "IN"));

                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        //Log.e("TTS", "The Language is not supported!");
                    } else {
                        //Log.i("TTS", "Language Supported.");
                    }
                    //Log.i("TTS", "Initialization success.");
                } else {
                    Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });



        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            //Log.i(TAG, "Permission to record denied");
            makeRequest();
        }
        flag = false;
        listenButton = findViewById(R.id.listenButton);
        profileButton = findViewById(R.id.profile_Button);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });

        Glide.with(this).load(R.drawable.init).into(listenButton);

        /*textQueryButton = findViewById(R.id.send_query);
        editQuery = findViewById(R.id.edit_query);*/
        keyboardSwitchButton = findViewById(R.id.switch_keyboard_button);
        sendButton = findViewById(R.id.send_button);
        editQuery = findViewById(R.id.text_query);
        sendButton.setVisibility(View.INVISIBLE);
        editQuery.setVisibility(View.INVISIBLE);
        keyboardSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!keyboardSwitch){
                    keyboardSwitch=true;
                    keyboardSwitchButton.setBackground(getResources().getDrawable(R.drawable.keyboard_hide));
                    listenButton.setVisibility(View.GONE);
                    sendButton.setVisibility(View.VISIBLE);
                    editQuery.setVisibility(View.VISIBLE);
                    editQuery.requestFocus();
                }
                else {
                    keyboardSwitchButton.setBackground(getResources().getDrawable(R.drawable.keyboard));
                    listenButton.setVisibility(View.VISIBLE);
                    sendButton.setVisibility(View.GONE);
                    editQuery.setVisibility(View.GONE);
                    editQuery.clearFocus();
                    keyboardSwitch=false;
                }
            }
        });




        recyclerView = findViewById(R.id.recycler_view);
        //logoutButton = findViewById(R.id.logout_button);
        initializeActionList();

        mAdapter = new ChatAdapter(chatList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        /*logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });*/


        final AIConfiguration config = new AIConfiguration("5987e904ba4b4a699b296c548e336fc7",
                AIConfiguration.SupportedLanguages.DEFAULT,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(this, config);
        aiService.setListener(this);
        //code to send queries from text
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AIDataService aiDataService = new AIDataService(config);
                final AIRequest aiRequest = new AIRequest();
                aiRequest.setQuery(editQuery.getText().toString());
                new AsyncTask<AIRequest, Void, AIResponse>() {
                    @Override
                    protected AIResponse doInBackground(AIRequest... requests) {
                        final AIRequest request = requests[0];
                        try {
                            final AIResponse response = aiDataService.request(aiRequest);
                            return response;
                        } catch (AIServiceException e) {
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(AIResponse aiResponse) {
                        if (aiResponse != null) {
                            // process aiResponse here
                            String sentMessage = editQuery.getText().toString();
                            result = aiResponse.getResult();
                            String receivedMessage = result.getFulfillment().getSpeech();
                            Chat chat = new Chat(sentMessage, "me");
                            chatList.add(chat);
                            changeRecyclerView();
                            chat = new Chat(receivedMessage, "him");
                            chatList.add(chat);
                            changeRecyclerView();
                            tts.speak(receivedMessage, TextToSpeech.QUEUE_FLUSH, null);
                            editQuery.setText("");
                        }
                    }
                }.execute(aiRequest);
            }
        });



        /*new AsyncTask<AIRequest, Void, AIResponse>() {
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                final AIRequest request = requests[0];
                try {
                    final AIResponse response = aiDataService.request(aiRequest);
                    return response;
                } catch (AIServiceException e) {
                }
                return null;
            }
            @Override
            protected void onPostExecute(AIResponse aiResponse) {
                if (aiResponse != null) {
                    // process aiResponse here
                    result = aiResponse.getResult();
                    String ss = result.getFulfillment().getSpeech();
                    Toast.makeText(MainActivity.this, ""+ss, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(aiRequest);*/




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

    private void initializeCustomer(String phone) {
        VolleyRequest request1 = new VolleyRequest();
        String tableName = "Customer";
        String query = "Select * from " +tableName +" where CustomerContact = "+phone;
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
                    customerID = obj.getString("CustomerID");
                    //return name;
                    //Toast.makeText(MainActivity.this, ""+name, Toast.LENGTH_LONG).show();
                }catch(JSONException e) {
                    //Toast.makeText(MainActivity.this,"No records present..",Toast.LENGTH_SHORT).show();
                }catch(Exception e){
                    //Toast.makeText(MainActivity.this,"An error has occured.."+e, Toast.LENGTH_SHORT).show();
                }
                //return name;
            }
        },"http://www.skyline69.co.nf/request.php?query=",  this, query);
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
        Glide.with(this).load(R.drawable.init).into(listenButton);
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
            tts.speak(receivedMessage, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    public void resolveQuery(String action){
        if (action.equals("query.phone")){
            if (mUser!=null){
                String phone = mUser.getPhoneNumber().toString();
                //Toast.makeText(this, ""+customerID, Toast.LENGTH_SHORT).show();
                String receivedMessage = result.getFulfillment().getSpeech();
                receivedMessage = receivedMessage+ " " +phone;
                Chat chat = new Chat(receivedMessage, "him");
                chatList.add(chat);
                changeRecyclerView();
            }
        }
        else if (action.equals("query.name")){
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
                },"http://www.skyline69.co.nf/request.php?query=",  this, query);
            }
        }
        else if (action.equals("query.order.status")){

            VolleyRequest request1 = new VolleyRequest();
            String tableName = "Orders";
            String query = "Select * from " +tableName +" where CustomerID = "+customerID;
            request1.fetchData(new VolleyCallBack(){
                @Override
                public void onSuccess(JSONArray resultArray){
                    //Toast.makeText(MainActivity.this, ""+result, Toast.LENGTH_SHORT).show();
                    try{
                        JSONArray json =resultArray;
                        JSONObject obj;
                        int size=json.length()-1;
                        obj = json.getJSONObject(0);
                        String receivedMessage = "you have "+ size + " order(s)";
                        Chat chat = new Chat(receivedMessage, "him");
                        chatList.add(chat);
                        changeRecyclerView();
                        for (int i = 0 ; i<size ; i++){
                            obj = json.getJSONObject(i);
                            String orderID = obj.getString("OrderID");
                            String orderStatus = obj.getString("OrderStatus");
                            String dateOfDelivery = obj.getString("ScheduledDeliveryDate");
                            String quantity = obj.getString("QuantityofItems");
                            String productName = obj.getString("ProductName");
                            receivedMessage = "Order ID: " +orderID + "\nOrder Status: " +orderStatus +"\nExpected Delivery date: " +dateOfDelivery + "\nQuantity: " +quantity + "\nProduct Name: "+productName;
                            chat = new Chat(receivedMessage, "him");
                            chatList.add(chat);
                            changeRecyclerView();

                        }
                        //customerID = obj.getString("CustomerID");
                        //Toast.makeText(MainActivity.this, ""+orderID, Toast.LENGTH_SHORT).show();
                        /*String receivedMessage = result.getFulfillment().getSpeech();
                        receivedMessage = receivedMessage+ " " +name;
                        Chat chat = new Chat(receivedMessage, "him");
                        chatList.add(chat);
                        changeRecyclerView();*/

                        //return name;
                        //Toast.makeText(MainActivity.this, ""+name, Toast.LENGTH_LONG).show();
                    }catch(JSONException e) {
                        //Toast.makeText(MainActivity.this,"No records present..",Toast.LENGTH_SHORT).show();
                    }catch(Exception e){
                        //Toast.makeText(MainActivity.this,"An error has occured.."+e, Toast.LENGTH_SHORT).show();ddddd
                    }
                    //return name;
                }
            },"http://www.skyline69.co.nf/request.php?query=",  this, query);
        }
        else if(action.equals("action.signout")){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }, 800);

        }
        else if (action.equals("action.order")){
            CustomDialogClass cdd=new CustomDialogClass(this, customerID);
            cdd.show();
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Chat chat = new Chat("Order Placed", "him");
                    chatList.add(chat);
                    changeRecyclerView();
                }
            }, 5000);
        }
    }

    @Override
    public void onError(AIError error) {
        //resultTextView.setText(error.toString());
        Glide.with(this).load(R.drawable.error).into(listenButton);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Glide.with(getApplicationContext()).load(R.drawable.init).into(listenButton);
            }
        }, 2000);
        //Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {
        Glide.with(this).load(R.drawable.listening).into(listenButton);
    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {
        Glide.with(this).load(R.drawable.processing).into(listenButton);
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