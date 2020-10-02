package project.quikERent.activity.admin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import project.quikERent.R;
import project.quikERent.activity.common.LoginActivity;
import project.quikERent.models.PowerToolModel;
import project.quikERent.models.RentedPowerToolModel;
import project.quikERent.models.UserModel;
import project.quikERent.utils.DataStoreUtils;

public class ReturnPowerToolActivity extends AppCompatActivity {

    public static final int MILLIS_IN_SECOND = 1000;
    public static final int SECONDS_IN_MINUTE = 60;
    public static final int MINUTES_IN_HOUR = 60;
    public static final int HOURS_IN_DAY = 24;
    private FirebaseAuth auth;
    private EditText adminReturnPowerToolFilter;
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
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent( ReturnPowerToolActivity.this, LoginActivity.class));
            finish();
        }

        setContentView(R.layout.activity_admin_return_powertools);
        adminReturnPowerToolFilter = (EditText) findViewById(R.id.AdminReturnPowerToolFilter);
        setAdminReturnPowerToolListener();
        listView = (ListView) findViewById(R.id.AdminReturnPowerToolListView);
        setListViewItemClickListener();
        getAllPowerToolsFromDatabase();
    }

    private void setListViewItemClickListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("Hello!", "Y u no see me?");
            }
        });
    }

    private void setAdminReturnPowerToolListener() {
        adminReturnPowerToolFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterList(String email) {
        if (!isPowerToolsStored || !isRentedPowerToolsStored || !isUsersStored) {
            return;
        }
        List<ReturnPowerToolView> returnPowerToolViews = new ArrayList<>();

        for (UserModel userModel : users) {
            if (userModel.getEmail().contains(email)) {
                for (RentedPowerToolModel powerToolModel : listOfRentedPowerTools) {
                    if (powerToolModel.getUserId().equalsIgnoreCase(userModel.getUId())) {
                        for (PowerToolModel powerTool : powerTools) {
                            if (Objects.equals(powerToolModel.getPowerToolId(), powerTool.getId())) {
                                returnPowerToolViews.add(new ReturnPowerToolView(powerTool, userModel));
                                break;
                            }
                        }
                    }
                }
            }
        }

        ReturnPowerToolAdapter borrowPowerToolAdapter = new ReturnPowerToolAdapter(ReturnPowerToolActivity.this, returnPowerToolViews);
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
                filterList(adminReturnPowerToolFilter.getText().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofpowerTools:", databaseError.toException());
            }
        });

        final DatabaseReference confirmations = FirebaseDatabase.getInstance().getReference("borrowed_powerTools");
        confirmations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                listOfRentedPowerTools.clear();
                listOfRentedPowerTools.addAll(DataStoreUtils.readRentedPowerTools(dataSnapshot.getValue()));
                isRentedPowerToolsStored = true;
                filterList(adminReturnPowerToolFilter.getText().toString());
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
                filterList(adminReturnPowerToolFilter.getText().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofpowerTools:", databaseError.toException());
            }
        });
    }

    private class ReturnPowerToolAdapter extends ArrayAdapter<ReturnPowerToolView> {

        ReturnPowerToolAdapter(Context context, List<ReturnPowerToolView> powerTools) {
            super(context, 0, powerTools);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ReturnPowerToolView powerTool = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_admin_return_powertools_layout, parent, false);
            }
            TextView textView = convertView.findViewById(R.id.adminReturnPowerToolListText);
            Button button = convertView.findViewById(R.id.adminReturnPowerToolButton);
            textView.setText(powerTool.getPowerToolModel() != null ? powerTool.getPowerToolModel().toString() : "");
            textView.setText(textView.getText() + "\n" + "User: " + powerTool.getUser().getEmail());
            if (!isPowerToolsStored || !isRentedPowerToolsStored || !isUsersStored) {
                return convertView;
            }
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("borrowed_powerTools");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            myRef.removeEventListener(this);
                            final DatabaseReference powerToolRef = myRef.child(powerTool.getPowerToolModel().getId().toString());
                            powerToolRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    powerToolRef.removeEventListener(this);
                                    HashMap<String, Object> map = (HashMap<String,Object>) dataSnapshot.getValue();
                                    HashMap<String,Object> borrowDateMap = (HashMap <String,Object>) map.get("borrowDate");
                                    Long time = (Long) borrowDateMap.get("time");
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(new Date(time));
                                    calendar.add(Calendar.DAY_OF_YEAR, 14);
                                    calendar.getTime();
                                    if(new Date().getTime() > calendar.getTime().getTime()) {
                                        long latenessInMillis = Math.abs(new Date().getTime() - calendar.getTime().getTime());
                                        int latenessInDays = (int)Math.ceil((float)latenessInMillis/ MILLIS_IN_SECOND / SECONDS_IN_MINUTE / MINUTES_IN_HOUR / HOURS_IN_DAY);
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("Lateness!")
                                                .setMessage("Lateness in returning powerTool: "+latenessInDays+" DAYS!\nPenalty to pay: "+latenessInDays*5+"zl")
                                                .setNeutralButton("OK!", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        powerToolRef.removeValue();
                                                    }
                                                }).show();
                                    } else {
                                        powerToolRef.removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w("Err:returnPowerTool:", databaseError.toException());
                                }
                            });
                            Toast.makeText(getApplicationContext(), "PowerTool returned", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("err:RentPowerTool:183", databaseError.getMessage());
                        }
                    });
                }
            });
            return convertView;
        }
    }

    private class ReturnPowerToolView {

        ReturnPowerToolView(PowerToolModel powerToolModel, UserModel user) {
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
