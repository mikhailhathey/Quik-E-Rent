package project.quikERent.activity.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import project.quikERent.R;
import project.quikERent.models.PowerToolModel;
import project.quikERent.models.RentedPowerToolModel;
import project.quikERent.models.UserModel;
import project.quikERent.utils.DataStoreUtils;

public class RentPowerToolActivity extends AppCompatActivity {

    private EditText powerToolNameEditText, emailEditText;
    private ListView listView;
    private final List<PowerToolModel> powerTools = new ArrayList<>();
    private final List<UserModel> users = new ArrayList<>();
    private final List<RentedPowerToolModel> listOfRentedPowerTools = new ArrayList<>();

    private boolean isPowerToolsStored = false;
    private boolean isRentedPowerToolsStored = false;
    private boolean isUsersStored = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_rented_powertools);
        powerToolNameEditText = (EditText) findViewById(R.id.adminRentPowerToolNameEditText);
        powerToolNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString(), emailEditText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        emailEditText = (EditText) findViewById(R.id.adminRentEmailEditText);
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(powerToolNameEditText.getText().toString(), s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        listView = (ListView) findViewById(R.id.AdminListOfRentedPowerToolsListView);
        getAllPowerToolsFromDatabase();
    }

    private void filterList(String powerToolName, String email) {
        if (!isPowerToolsStored || !isRentedPowerToolsStored || !isUsersStored) {
            return;
        }

        List<String> powerToolViews = new ArrayList<>();
        for (RentedPowerToolModel rentedPowerToolModel : listOfRentedPowerTools) {
            UserModel userModel = null;
            PowerToolModel powerToolModel = null;
            for (UserModel user : users) {
                if (rentedPowerToolModel.getUserId().equalsIgnoreCase(user.getUId())) {
                    userModel = user;
                    break;
                }
            }
            for (PowerToolModel powerTool : powerTools) {
                if (Objects.equals(rentedPowerToolModel.getPowerToolId(), powerTool.getId())) {
                    powerToolModel = powerTool;
                    break;
                }
            }
            if (powerToolModel != null && powerToolModel.getPowerToolName().contains(powerToolName)
                    && userModel != null && userModel.getEmail().contains(email)) {
                powerToolViews.add(new PowerToolView(powerToolModel, userModel).toString());
            }
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(RentPowerToolActivity.this,
                R.layout.activity_admin_rented_powertools_layout,
                R.id.adminRentListText,
                powerToolViews);
        listView.setAdapter(arrayAdapter);
    }

    void getAllPowerToolsFromDatabase() {
        final DatabaseReference powerToolsRef = FirebaseDatabase.getInstance().getReference("powerTools");
        powerToolsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                powerTools.clear();
                powerTools.addAll(DataStoreUtils.readPowerTools(dataSnapshot.getValue()));
                isPowerToolsStored = true;
                filterList(powerToolNameEditText.getText().toString(), emailEditText.getText().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofpowerTools:", databaseError.toException());
            }
        });

        final DatabaseReference borrowedPowerToolsRef = FirebaseDatabase.getInstance().getReference("borrowed_powerTools");
        borrowedPowerToolsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                listOfRentedPowerTools.clear();
                listOfRentedPowerTools.addAll(DataStoreUtils.readRentedPowerTools(dataSnapshot.getValue()));
                isRentedPowerToolsStored = true;
                filterList(powerToolNameEditText.getText().toString(), emailEditText.getText().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofpowerTools:", databaseError.toException());
            }
        });

        final DatabaseReference usersdRef = FirebaseDatabase.getInstance().getReference().child("users");
        usersdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                users.clear();
                users.addAll(DataStoreUtils.readUsers(dataSnapshot.getValue()));
                isUsersStored = true;
                filterList(powerToolNameEditText.getText().toString(), emailEditText.getText().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofpowerTools:", databaseError.toException());
            }
        });
    }

    private class PowerToolView {

        PowerToolView(PowerToolModel powerToolModel, UserModel user) {
            this.powerToolModel = powerToolModel;
            this.user = user;
        }

        PowerToolModel powerToolModel;
        UserModel user;

        PowerToolModel getPowerToolModel() {
            return powerToolModel;
        }

        void setPowerToolModel(PowerToolModel powerToolModel) {
            this.powerToolModel = powerToolModel;
        }

        UserModel getUser() {
            return user;
        }

        void setUser(UserModel user) {
            this.user = user;
        }

        @Override
        public String toString() {
            return powerToolModel.getPowerToolName() + ", " + user.getEmail();
        }
    }


}