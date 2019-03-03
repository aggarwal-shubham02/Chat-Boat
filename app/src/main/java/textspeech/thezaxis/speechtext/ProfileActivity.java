package textspeech.thezaxis.speechtext;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import textspeech.thezaxis.speechtext.Helper.VolleyCallBack;
import textspeech.thezaxis.speechtext.Helper.VolleyRequest;
public class ProfileActivity extends AppCompatActivity {
    FirebaseUser mUser;
    EditText name, phoneNo, address;
    ImageView txtChangePhoto;
    String phone2;
    ImageButton btnSave, btnClose;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        name = findViewById(R.id.profile_name);
        phoneNo =  findViewById(R.id.profile_phone);
        address =  findViewById(R.id.profile_address);

        txtChangePhoto = findViewById(R.id.profile_change_img);
        btnSave = (ImageButton) findViewById(R.id.profile_button_save);
        btnClose = (ImageButton) findViewById(R.id.profile_button_close);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        String phone = mUser.getPhoneNumber();
        initializeCustomer(phone);
    }

    private void initializeCustomer(final String phone) {
        VolleyRequest request1 = new VolleyRequest();
        String tableName = "Customer";
        if (phone.contains("+91")){
            phone2 = phone.substring(3, phone.length());
        }
        String query = "Select * from " + tableName + " where CustomerContact = " + phone2;
        //Toast.makeText(this, ""+query, Toast.LENGTH_SHORT).show();
        request1.fetchData(new VolleyCallBack() {
            @Override
            public void onSuccess(JSONArray resultArray) {
                //Toast.makeText(ProfileActivity.this, "" + resultArray, Toast.LENGTH_SHORT).show();
                try {
                    JSONArray json = resultArray;
                    JSONObject obj;
                    //int size=json.length();
                    obj = json.getJSONObject(0);
                    String custName = obj.getString("CustomerName");
                    String custAddress = obj.getString("CustomerAddress");
                    String contact = obj.getString("CustomerContact");

                    name.setText(custName);
                    phoneNo.setText(contact);
                    address.setText(custAddress);
                    //customerID = obj.getString("CustomerID");
                    //return name;
                    //Toast.makeText(MainActivity.this, ""+name, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    //Toast.makeText(MainActivity.this,"No records present..",Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    //Toast.makeText(MainActivity.this,"An error has occured.."+e, Toast.LENGTH_SHORT).show();
                }
                //return name;
            }
        },"http://www.skyline69.co.nf/request.php?query=", this, query);
    }
}
