package project.quikERent.activity.customer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import project.quikERent.R;
import project.quikERent.activity.common.LoginActivity;
import project.quikERent.models.PowerToolModel;
import project.quikERent.models.RentedPowerToolModel;
import project.quikERent.models.ConfirmationPowerToolModel;
import project.quikERent.models.ConfirmationType;
import project.quikERent.utils.DataStoreUtils;


public class RentPowerToolActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText brandEditText, powerToolNameEditText, yearEditText;
    private ListView listView;
    private ProgressBar progressBar;
    private final List<PowerToolModel> powerTools = new ArrayList<>();
    private final List<PowerToolModel> filteredRentPowerTool = new ArrayList<>();
    private final List<ConfirmationPowerToolModel> confirmationPowerToolModels = new ArrayList<>();
    private final List<RentedPowerToolModel> borrowedPowerTools = new ArrayList<>();
    private final List<Long> confirmationPowerToolIds = new ArrayList<>();

    private boolean isConfirmationStored = false;
    private boolean isPowerToolsStored = false;
    private boolean isRentedPowerToolsStored = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(RentPowerToolActivity.this, LoginActivity.class));
            finish();
        }

        setContentView(R.layout.activity_customer_rent_powertools);
        brandEditText = (EditText) findViewById(R.id.RentPowerToolBrandEditText);
        setBrandEditTestListener();
        powerToolNameEditText = (EditText) findViewById(R.id.RentPowerToolPowerToolNameEditText);
        setPowerToolNameEditTextListener();
        yearEditText = (EditText) findViewById(R.id.RentPowerToolYearEditText);
        setYearEditTextListener();
        listView = (ListView) findViewById(R.id.RentPowerToolListView);
        setListViewItemClickListener();
        progressBar = (ProgressBar) findViewById(R.id.RentPowerToolProgressBar);
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

    private void setYearEditTextListener() {
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
    }

    private void setPowerToolNameEditTextListener() {
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
    }

    private void setBrandEditTestListener() {
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
    }

    private void filterList(String brand, String powerToolName, String year) {
        if (!isPowerToolsStored || !isConfirmationStored || !isRentedPowerToolsStored) {
            return;
        }
        filteredRentPowerTool.clear();
        filteredRentPowerTool.addAll(powerTools);
        Iterator<PowerToolModel> it = filteredRentPowerTool.iterator();
        while (it.hasNext()) {
            final PowerToolModel model = it.next();
            if (!model.getBrand().contains(brand) || !model.getPowerToolName().contains(powerToolName) && !model.getYear().toString().contains(year)) {
                it.remove();
                continue;
            }
            boolean isConfirm = false;
            for (ConfirmationPowerToolModel confirmationPowerToolModel : confirmationPowerToolModels) {
                if (confirmationPowerToolModel.getPowerToolId().equals(model.getId())) {
                    if (!confirmationPowerToolModel.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        isConfirm = true;
                    break;
                }
            }
            if (isConfirm) {
                it.remove();
                continue;
            }
            boolean isRented = false;
            for (RentedPowerToolModel rentedPowerToolModel : borrowedPowerTools) {
                if (rentedPowerToolModel.getPowerToolId().equals(model.getId())) {
                    isRented = true;
                    break;
                }
            }
            if (isRented) {
                it.remove();
            }
        }
        RentPowerToolAdapter borrowPowerToolAdapter = new RentPowerToolAdapter(RentPowerToolActivity.this, filteredRentPowerTool);
        listView.setAdapter(borrowPowerToolAdapter);
    }

    void getAllPowerToolsFromDatabase() {
        final DatabaseReference powerToolsRepository = FirebaseDatabase.getInstance().getReference("powerTools");
        powerToolsRepository.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                powerTools.clear();
                powerTools.addAll(DataStoreUtils.readPowerTools(dataSnapshot.getValue()));
                isPowerToolsStored = true;
                filterList(brandEditText.getText().toString(), powerToolNameEditText.getText().toString(), yearEditText.getText().toString());
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:borrowedPowerTools:", databaseError.toException());
            }
        });

        final DatabaseReference confirmations = FirebaseDatabase.getInstance().getReference("confirmations");
        confirmations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    isConfirmationStored = true;
                    return;
                }
                confirmationPowerToolModels.clear();
                confirmationPowerToolModels.addAll(DataStoreUtils.readConfirmations(dataSnapshot.getValue()));
                confirmationPowerToolIds.clear();
                for (ConfirmationPowerToolModel conf : confirmationPowerToolModels) {
                    confirmationPowerToolIds.add(conf.getPowerToolId());
                }
                isConfirmationStored = true;
                filterList(brandEditText.getText().toString(), powerToolNameEditText.getText().toString(), yearEditText.getText().toString());
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofpowerTools:", databaseError.toException());
            }
        });

        final DatabaseReference borrowedPowerToolsRepository = FirebaseDatabase.getInstance().getReference("borrowed_powerTools");
        borrowedPowerToolsRepository.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    isRentedPowerToolsStored = true;
                    return;
                }
                borrowedPowerTools.clear();
                borrowedPowerTools.addAll(DataStoreUtils.readRentedPowerTools(dataSnapshot.getValue()));
                isRentedPowerToolsStored = true;
                filterList(brandEditText.getText().toString(), powerToolNameEditText.getText().toString(), yearEditText.getText().toString());
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:borrowedPowerTools:", databaseError.toException());
            }
        });
    }

    private class RentPowerToolAdapter extends ArrayAdapter<PowerToolModel> {

        RentPowerToolAdapter(Context context, List<PowerToolModel> powerTools) {
            super(context, 0, powerTools);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final PowerToolModel powerTool = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_customer_rent_powertools_layout, parent, false);
            }
            TextView textView = convertView.findViewById(R.id.borrowPowerToolListText);
            final Button button = convertView.findViewById(R.id.reservePowerToolButton);
            textView.setText(powerTool != null ? powerTool.toString() : "");
            if (!isPowerToolsStored || !isConfirmationStored || !isRentedPowerToolsStored) {
                return convertView;
            }
            if (confirmationPowerToolIds.contains(powerTool.getId()))
                button.setText(R.string.ReservePowerToolCancel);
            else button.setText(R.string.ReservePowerToolSubmit);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    final ConfirmationPowerToolModel model = new ConfirmationPowerToolModel();
                    model.setUserId(uid);
                    model.setPowerToolId(powerTool.getId());
                    model.setType(ConfirmationType.RENT);
                    Date now = new Date();
                    if (!confirmationPowerToolIds.contains(powerTool.getId()))
                        now.setTime(now.getTime() + 24 * 60 * 60 * 1000);
                    model.setDatetime(now);
                    final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("confirmations");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            myRef.removeEventListener(this);
                            myRef.child(String.valueOf(model.getPowerToolId())).setValue(model);
                            if (!confirmationPowerToolIds.contains(powerTool.getId())) {
                                confirmationPowerToolIds.add(powerTool.getId());
                                button.setText(R.string.ReservePowerToolCancel);
                                Toast.makeText(getApplicationContext(), "PowerTool reserved! It will expire after 24 hours! Get powerTool from library!", Toast.LENGTH_SHORT).show();
                            } else {
                                confirmationPowerToolIds.remove(powerTool.getId());
                                button.setText(R.string.ReservePowerToolSubmit);
                                Toast.makeText(getApplicationContext(), "PowerTool reservation cancelled!", Toast.LENGTH_SHORT).show();
                            }
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

}