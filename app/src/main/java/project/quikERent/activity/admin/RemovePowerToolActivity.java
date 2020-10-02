package project.quikERent.activity.admin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import project.quikERent.R;
import project.quikERent.models.PowerToolModel;
import project.quikERent.utils.DataStoreUtils;


public class RemovePowerToolActivity extends AppCompatActivity {

    private EditText brandEditText, powerToolNameEditText, yearEditText;
    private ListView listView;
    private ProgressBar progressBar;
    private final List<PowerToolModel> listOfPowerTools = new ArrayList<>();
    private final List<PowerToolModel> filteredListOfPowerTools = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_remove_powertool);
        brandEditText = (EditText) findViewById(R.id.RemovePowerToolBrandEditText);
        brandEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString(), powerToolNameEditText.getText().toString(), yearEditText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        powerToolNameEditText = (EditText) findViewById(R.id.RemovePowerToolPowerToolNameEditText);
        powerToolNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(brandEditText.getText().toString(), s.toString(), yearEditText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        yearEditText = (EditText) findViewById(R.id.RemovePowerToolYearEditText);
        yearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(brandEditText.getText().toString(), powerToolNameEditText.getText().toString(), s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        listView = (ListView) findViewById(R.id.RemovePowerToolListView);
        progressBar = (ProgressBar) findViewById(R.id.RemovePowerToolProgressBar);
        getAllPowerToolsFromDatabase();

    }

    private void filterList(String brand, String powerToolName, String year) {
        filteredListOfPowerTools.clear();
        for (PowerToolModel powerTool : listOfPowerTools) {
            if (powerTool.getBrand().contains(brand) && powerTool.getPowerToolName().contains(powerToolName) && powerTool.getYear().toString().contains(year)) {
                filteredListOfPowerTools.add(powerTool);
            }
        }
        PowerToolAdapter powerToolAdapter = new PowerToolAdapter(RemovePowerToolActivity.this, filteredListOfPowerTools);
        listView.setAdapter(powerToolAdapter);
    }

    void getAllPowerToolsFromDatabase() {
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("power_tools");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                listOfPowerTools.clear();
                listOfPowerTools.addAll(DataStoreUtils.readPowerTools(dataSnapshot.getValue()));
                filterList(brandEditText.getText().toString(), powerToolNameEditText.getText().toString(), yearEditText.getText().toString());
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofpowerTools:", databaseError.toException());
            }
        });
    }

    private class PowerToolAdapter extends ArrayAdapter<PowerToolModel> {
        PowerToolAdapter(Context context, List<PowerToolModel> powerTools) {
            super(context, 0, powerTools);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final PowerToolModel powerTool = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_admin_remove_row_layout, parent, false);
            }
            TextView textView = convertView.findViewById(R.id.listText);
            Button button = convertView.findViewById(R.id.removePowerToolButton);
            textView.setText(powerTool != null ? powerTool.toString() : "");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final DatabaseReference powerToolsRef = FirebaseDatabase.getInstance().getReference("powerTools");
                    powerToolsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            powerToolsRef.removeEventListener(this);
                            powerToolsRef.child(String.valueOf(powerTool.getId())).removeValue();
                            Toast.makeText(getApplicationContext(), "PowerTool removed from database!", Toast.LENGTH_SHORT).show();
                        }

                        @SuppressLint("LongLogTag")
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("err:RemPowerToolListene:183", databaseError.getMessage());
                        }
                    });

                    final DatabaseReference confirmationsRef = FirebaseDatabase.getInstance().getReference("confirmations");
                    confirmationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            confirmationsRef.removeEventListener(this);
                            confirmationsRef.child(String.valueOf(powerTool.getId())).removeValue();
                            Toast.makeText(getApplicationContext(), "PowerTool removed from database!", Toast.LENGTH_SHORT).show();
                        }

                        @SuppressLint("LongLogTag")
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("err:RemPowerToolListene:183", databaseError.getMessage());
                        }
                    });

                    final DatabaseReference borrowedPowerToolsRef = FirebaseDatabase.getInstance().getReference("borrowed_powerTools");
                    borrowedPowerToolsRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            borrowedPowerToolsRef.removeEventListener(this);
                            borrowedPowerToolsRef.child(String.valueOf(powerTool.getId())).removeValue();
                            Toast.makeText(getApplicationContext(), "PowerTool removed from database!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w("Err:listofpowerTools:", databaseError.toException());
                        }
                    });
                }
            });
            return convertView;
        }
    }
};