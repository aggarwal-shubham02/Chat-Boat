package textspeech.thezaxis.speechtext.Helper;

import textspeech.thezaxis.speechtext.Chat;
import textspeech.thezaxis.speechtext.R;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CustomDialogClass extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    String phone, customer;
    public Dialog d;
    private ArrayList<String> productList = new ArrayList<>();
    public Button submitButton, cancelButton;
    private Spinner spinnerProducts;
    EditText quantityText;

    public CustomDialogClass(Activity a, String customerID) {
        super(a);
        customer = customerID;
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog_order);
        submitButton = (Button) findViewById(R.id.submit_button);
        cancelButton = (Button) findViewById(R.id.cancel_button);
        spinnerProducts = findViewById(R.id.spinner_products);
        initializeArrayList();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (!mUser.equals(null)){
            phone = mUser.getPhoneNumber();
        }





        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                c, R.array.products, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProducts.setAdapter(adapter);










        quantityText = findViewById(R.id.quantity_text);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phone.contains("+91")){
                    phone = phone.substring(3, phone.length());
                }
                String quantity = quantityText.getText().toString();
                String PID = getPID(spinnerProducts.getSelectedItem().toString());
                VolleyRequest request1 = new VolleyRequest();
                String tableName = "Orders";
                String query = "Insert into " +tableName +"(QuantityofItems, ProductID, CustomerID, OrderStatus, ProductName) values( "+quantity + ", " +PID + ", " +customer + ", \'Order Placed\'" +", \'"+spinnerProducts.getSelectedItem().toString() +"\')";
                Toast.makeText(c, ""+query, Toast.LENGTH_SHORT).show();
                request1.fetchData(new VolleyCallBack(){
                    @Override
                    public void onSuccess(JSONArray resultArray){
                        try{
                            JSONObject obj = resultArray.getJSONObject(0);
                            String str = obj.getString("flag") +"";
                            Toast.makeText(c, "" +str, Toast.LENGTH_SHORT).show();
                            //return name;
                            //Toast.makeText(MainActivity.this, ""+name, Toast.LENGTH_LONG).show();
                        }catch(Exception e){
                            //Toast.makeText(MainActivity.this,"An error has occured.."+e, Toast.LENGTH_SHORT).show();
                        }
                        //return name;
                    }
                },"http://www.skyline69.co.nf/write_request.php?query=",  c, query);
                //dismiss();
            }

        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private String getPID(String str) {
        switch (str){
            case "Dalmia FBC Cement": {
                return "1";
            }
            case "Dalmia 53 Grade Superoof":{
                return "2";
            }
            case "Dalmia PPC":{
                return "3";
            }
            case "Dalmia DSP Cement":{
                return "4";
            }
            case "Dalmia SRPC":{
                return "5";
            }
            default: return "0";
        }
    }

    @Override
    public void onClick(View v) {
        /*switch (v.getId()) {
            case R.id.btn_yes:
                c.finish();
                break;
            case R.id.btn_no:
                dismiss();
                break;
            default:
                break;
        }*/
        dismiss();
    }
    void initializeArrayList(){
        productList.add("Dalmia FBC Cement");
        productList.add("Dalmia 53 Grade Superoof");
        productList.add("Dalmia PPC");
        productList.add("Dalmia DSP Cement");
        productList.add("Dalmia SRPC");
    }
}