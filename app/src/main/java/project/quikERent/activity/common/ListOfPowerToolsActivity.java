package project.quikERent.activity.common;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.quikERent.R;
import project.quikERent.models.AdminUserModel;
import project.quikERent.models.PowerToolModel;
import project.quikERent.utils.DataStoreUtils;
import project.quikERent.utils.FileUtils;


public class ListOfPowerToolsActivity extends AppCompatActivity {

    public final static String SOUL = "vMv48nFoBWvfREAFBjKVvQWAZkEIRhLV9TBYKS2A";

    private EditText brandEditText, powerToolNameEditText, yearEditText;
    private ListView listView;
    private ProgressBar progressBar;
    private final List<PowerToolModel> listOfPowerTools = new ArrayList<>();
    private final List<PowerToolModel> filteredListOfPowerTools = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_list_of_powertools);
        brandEditText = (EditText) findViewById(R.id.ListOfPowerToolsBrandEditText);
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
        powerToolNameEditText = (EditText) findViewById(R.id.ListOfPowerToolsPowerToolNameEditText);
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
        yearEditText = (EditText) findViewById(R.id.ListOfPowerToolsYearEditText);
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
        listView = (ListView) findViewById(R.id.ListOfPowerToolsListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PowerToolModel powerToolModel = filteredListOfPowerTools.get(position);
                Intent intent = new Intent(ListOfPowerToolsActivity.this, BitMapActivity.class);
                intent.putExtra("key", SOUL + powerToolModel.getId());
                startActivity(intent);
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.ListOfPowerToolsProgressBar);

//        final FileUtils fileUtils = new FileUtils();
//        final List<FileUtils.PowerTool> powerTools;
//        try {
//            powerTools = fileUtils.readPowerToolFromFile();
//            readDatabaseFromFileAndSaveInFirebase(powerTools);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        getAllPowerToolsFromDatabase();
    }

    private void filterList(String brand, String powerToolName, String year) {
        filteredListOfPowerTools.clear();
        for (PowerToolModel powerTool : listOfPowerTools) {
            if (powerTool.getBrand().contains(brand) && powerTool.getPowerToolName().contains(powerToolName) && powerTool.getYear().toString().contains(year)) {
                filteredListOfPowerTools.add(powerTool);
            }
        }
        List<String> listForAdapter = new ArrayList<>();
        for (PowerToolModel powerTool : filteredListOfPowerTools) {
            listForAdapter.add(powerTool.toString());
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ListOfPowerToolsActivity.this, R.layout.activity_common_list_of_powertools_layout, R.id.listText, listForAdapter);
        listView.setAdapter(arrayAdapter);
    }

    void getAllPowerToolsFromDatabase() {
        final DatabaseReference powerToolsRef = FirebaseDatabase.getInstance().getReference("power_tools");
        powerToolsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                listOfPowerTools.clear();

                //for (DataSnapshot powerToolsSnapshot: dataSnapshot.getChildren()) {
                    /*Map<String, Object> powertool = (HashMap<String, Object>) dataSnapshot.getValue();
                    Map<String, Object> powertoolfields = (Map<String, Object>) powertool.values().toArray()[0];

                    if (powertool.isEmpty()){
                        return;
                    }

                    final Long id = (Long) powertoolfields.get("id");
                    final String brand = (String) powertoolfields.get("brand");
                    final String powerToolName = (String) powertoolfields.get("powerToolName");
                    final Integer year = ((Long) powertoolfields.get("year")).intValue();

                    listOfPowerTools.add(new PowerToolModel(id, brand, powerToolName, year));*/
                    //Log.d("Failed", (String) powerToolsSnapshot.getValue());

                    /*PowerToolModel newPowerTool = dataSnapshot.getValue(PowerToolModel.class);
                    listOfPowerTools.add(newPowerTool);*/

                    listOfPowerTools.addAll(DataStoreUtils.readPowerTools(dataSnapshot.getValue()));


                //}

                //listOfPowerTools.addAll(DataStoreUtils.readPowerTools(dataSnapshot.getValue()));
                filteredListOfPowerTools.addAll(listOfPowerTools);
                List<String> listForAdapter = new ArrayList<>();
                for (PowerToolModel powerTool : filteredListOfPowerTools) {
                    listForAdapter.add(powerTool.toString());
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ListOfPowerToolsActivity.this, R.layout.activity_common_list_of_powertools_layout, R.id.listText, listForAdapter);
                listView.setAdapter(arrayAdapter);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofpowerTools:", databaseError.toException());
            }
        });
    }

    void readDatabaseFromFileAndSaveInFirebase(List<FileUtils.PowerTool> listOfPowerTools) {
        final List<FileUtils.PowerTool> powerTools = listOfPowerTools;
        final DatabaseReference powerToolsRef = FirebaseDatabase.getInstance().getReference("powerTools");
        powerToolsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                long couinter = 10;
                for (FileUtils.PowerTool b : powerTools) {
                    try {
                        final PowerToolModel model = new PowerToolModel(b.getBrand(), b.getPowerToolName(), Integer.parseInt(b.getYear()));
                        model.setId(couinter);
                        powerToolsRef.child(String.valueOf(model.getId())).setValue(model);
                        couinter++;
                    } catch (RuntimeException e) {

                    }
                    if (couinter > 2000) {
                        break;
                    }
                }
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofpowerTools:", databaseError.toException());
            }
        });
    }
}