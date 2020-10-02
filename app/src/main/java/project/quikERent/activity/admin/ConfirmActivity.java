package project.quikERent.activity.admin;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import project.quikERent.R;
import project.quikERent.models.ConfirmationPowerToolModel;
import project.quikERent.models.PowerToolModel;
import project.quikERent.models.RentedPowerToolModel;
import project.quikERent.models.UserModel;
import project.quikERent.utils.DataStoreUtils;

public class ConfirmActivity extends AppCompatActivity {

    private ListView listView;
    private final List<PowerToolModel> powerTools = new ArrayList<>();
    private final List<UserModel> users = new ArrayList<>();
    private final List<ConfirmationPowerToolModel> confirmations = new ArrayList<>();
    private final List<PowerToolView> confirmationsPowerTools = new ArrayList<>();

    private boolean isConfirmationStored = false;
    private boolean isPowerToolsStored = false;
    private boolean isUsersStored = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_confirmations);
        listView = (ListView) findViewById(R.id.ConfirmationRentPowerToolListView);
        getAllPowerToolsFromDatabase();
    }

    private void filterList() {
        if (!isPowerToolsStored || !isConfirmationStored || !isUsersStored) {
            return;
        }
        confirmationsPowerTools.clear();
        for (ConfirmationPowerToolModel confirmationPowerTool : confirmations) {
            PowerToolView powerToolView = new PowerToolView();
            for (PowerToolModel powerToolModel : powerTools) {
                if (powerToolModel.getId().equals(confirmationPowerTool.getPowerToolId())) {
                    powerToolView.setPowerToolModel(powerToolModel);
                    break;
                }
            }
            for (UserModel userModel : users) {
                if (confirmationPowerTool.getUserId().equalsIgnoreCase(userModel.getUId())) {
                    powerToolView.setUser(userModel);
                    break;
                }
            }
            confirmationsPowerTools.add(powerToolView);
        }
        ConfirmPowerToolAdapter borrowPowerToolAdapter = new ConfirmPowerToolAdapter(ConfirmActivity.this, confirmationsPowerTools);
        listView.setAdapter(borrowPowerToolAdapter);
    }

    void getAllPowerToolsFromDatabase() {
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("power_tools");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                powerTools.clear();
                powerTools.addAll(DataStoreUtils.readPowerTools(dataSnapshot.getValue()));
                isPowerToolsStored = true;
                filterList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofpowerTools:", databaseError.toException());
            }
        });

        final DatabaseReference confirmationsReference = FirebaseDatabase.getInstance().getReference("confirmations");
        confirmationsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                confirmations.clear();
                confirmations.addAll(DataStoreUtils.readConfirmations(dataSnapshot.getValue()));
                isConfirmationStored = true;
                filterList();
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
                filterList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofpowerTools:", databaseError.toException());
            }
        });
    }


    private class ConfirmPowerToolAdapter extends ArrayAdapter<PowerToolView> {

        ConfirmPowerToolAdapter(Context context, List<PowerToolView> powerTools) {
            super(context, 0, powerTools);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final PowerToolView powerTool = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_admin_confirmations_layout, parent, false);
            }
            TextView textView = convertView.findViewById(R.id.confirmRentPowerToolListText);
            Button button = convertView.findViewById(R.id.confirmRentPowerToolButton);
            textView.setText("PowerTool: " + (powerTool.getPowerToolModel() != null ? powerTool.getPowerToolModel().toString() : ""));
            textView.setText(textView.getText() + "\n" + "User: " + powerTool.getUser().getEmail());
            if (!isPowerToolsStored || !isConfirmationStored || !isUsersStored) {
                return convertView;
            }
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final DatabaseReference borrowedPowerToolsRef = FirebaseDatabase.getInstance().getReference("borrowed_powerTools");
                    borrowedPowerToolsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            borrowedPowerToolsRef.removeEventListener(this);
                            RentedPowerToolModel rentedPowerToolModel = new RentedPowerToolModel();
                            rentedPowerToolModel.setPowerToolId(powerTool.getPowerToolModel().getId());
                            rentedPowerToolModel.setRentedDate(new Date());
                            rentedPowerToolModel.setUserId(powerTool.getUser().getUId());
                            borrowedPowerToolsRef.child(powerTool.getPowerToolModel().getId().toString()).setValue(rentedPowerToolModel);
                            Toast.makeText(getApplicationContext(), "PowerTool borrowed", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("err:RentPowerTool:183", databaseError.getMessage());
                        }
                    });

                    final DatabaseReference confirmationsRef = FirebaseDatabase.getInstance().getReference("confirmations");
                    confirmationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            confirmationsRef.removeEventListener(this);
                            confirmationsRef.child(powerTool.getPowerToolModel().getId().toString()).removeValue();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("err:RentPowerTool:183", databaseError.getMessage());
                        }
                    });
                    getAllPowerToolsFromDatabase();
                }
            });
            return convertView;
        }
    }

    private class PowerToolView {

        PowerToolView() {}

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
    }
}
