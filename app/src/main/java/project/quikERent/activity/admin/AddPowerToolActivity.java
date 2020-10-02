package project.quikERent.activity.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import project.quikERent.R;
import project.quikERent.models.PowerToolModel;


public class AddPowerToolActivity extends AppCompatActivity {

    private EditText brandEditText, powerToolNameEditText, yearEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_powertool);
        brandEditText = (EditText) findViewById(R.id.AddPowerToolBrandEditText);
        powerToolNameEditText = (EditText) findViewById(R.id.AddPowerToolPowerToolNameEditText);
        yearEditText = (EditText) findViewById(R.id.AddPowerToolYearEditText);
    }

    public void submitListener(View view) {
        Map<String,String> validationErrors = new HashMap<>();
        String brand = brandEditText.getText().toString();
        String powerToolName = powerToolNameEditText.getText().toString();
        String rawYear = yearEditText.getText().toString();
        if(brand.isEmpty()){
            validationErrors.put("brand","Brand field cannot be empty!");
        } /*else if(brand.split(" ").length != 2 || brand.matches(".*\\d+.*")) {
            validationErrors.put("brand","Invalid format on brand field! Expected \"[name]\"");
        }*/
        if(powerToolName.isEmpty()) {
            validationErrors.put("powerToolName","PowerToolName field cannot be empty!");
        }
        if(rawYear.isEmpty()) {
            validationErrors.put("year", "Year field cannot be empty!");
        } else {
            try {
                Integer year = Integer.parseInt(rawYear);
                int currentYear = new Date().getYear() + 1900;
                if (year < 1000 || year > currentYear) {
                    validationErrors.put("year", "Expected year from range 1000 <= year <= " + currentYear);
                } else if (validationErrors.size() == 0) {
                    final PowerToolModel powerTool = new PowerToolModel(brand, powerToolName, year);
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference myRef = database.getReference("power_tools");
                    final DatabaseReference counter = database.getReference("counter");
                    counter.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            counter.removeEventListener(this);
                            Long id = dataSnapshot.getValue(Long.class);
                            powerTool.setId(id);
                            myRef.child(String.valueOf(id)).setValue(powerTool);
                            Toast.makeText(getApplicationContext(), "PowerTool added to database!", Toast.LENGTH_SHORT).show();
                            counter.setValue(++id);
                            finish();
                        }

                        @SuppressLint("LongLogTag")
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("err:AddPowerToolListene:93", databaseError.getMessage());
                        }
                    });
                }
            } catch (NumberFormatException nfe) {
                validationErrors.put("year", "Invalid format of year!");
            }
        }
        if(validationErrors.size()!=0){
            for(Map.Entry<String,String> entry : validationErrors.entrySet()){
                Toast.makeText(getApplicationContext(), entry.getValue(), Toast.LENGTH_LONG).show();
            }
        }
    }
};