package textspeech.thezaxis.speechtext;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.JsonElement;
import java.util.Map;

public class MainActivity extends Activity implements AIListener{

    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    FirebaseUser mUser;
    List<Chat> chatList = new ArrayList<>();


    //private Button listenButton;
    Button listenButton;
    private RecyclerView recyclerView;
    private TextView resultTextView, messageText, queryText;
    private AIService aiService;
    private ChatAdapter mAdapter;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listenButton = findViewById(R.id.listenButton);
        recyclerView = findViewById(R.id.recycler_view);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mAdapter = new ChatAdapter(chatList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);











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

        Result result = response.getResult();

        // Get parameters
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }
        String receivedMessage = result.getFulfillment().getSpeech();
        String sentMessage = result.getResolvedQuery();


        Chat chat = new Chat(sentMessage, "me");
        chatList.add(chat);
        changeRecyclerView();
        chat = new Chat(receivedMessage, "him");
        chatList.add(chat);
        changeRecyclerView();

        // Show results in TextView.
        /*resultTextView.setText("Query:" + result.getResolvedQuery() +
                "\nAction: " + result.getAction() +
                "\nParameters: " + parameterString);
        messageText.setText(""+result.getFulfillment().getSpeech());
        queryText.setText("");
        resolveQuery(result.getAction().toString());*/

    }
    public void resolveQuery(String action){
        if (action.equals("query.phone")){
            if (mUser!=null){
                String phone = mUser.getPhoneNumber().toString();
                queryText.setText("Your Phone no: " + phone);
            }
            else{
                queryText.setText("Error retrieving User!");
                queryText.setTextColor(Color.RED);
            }
            //queryText.setText("Your phone no: " );
        }
    }

    @Override
    public void onError(AIError error) {
        resultTextView.setText(error.toString());
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