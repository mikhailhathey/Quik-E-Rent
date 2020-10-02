package project.quikERent.activity.customer;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import project.quikERent.R;
import project.quikERent.models.PowerToolModel;
import project.quikERent.models.RentedPowerToolModel;
import project.quikERent.models.ConfirmationPowerToolModel;
import project.quikERent.models.UserModel;
import project.quikERent.utils.DataStoreUtils;

public class ReturnPowerToolActivity extends AppCompatActivity {

    private ListView listView;

    private final List<PowerToolModel> powerTools = new ArrayList<>();
    private final List<PowerToolModel> filteredPowerTools = new ArrayList<>();
    private final List<UserModel> users = new ArrayList<>();
    private final List<ConfirmationPowerToolModel> confirmations = new ArrayList<>();
    private final List<RentedPowerToolModel> borrowedPowerTools = new ArrayList<>();

    private boolean isConfirmationStored = false;
    private boolean isPowerToolsStored = false;
    private boolean isRentedPowerToolsStored = false;
    private boolean isUsersStored = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_return_powertool);
        listView = (ListView) findViewById(R.id.ReturnPowerToolListView);
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

    private void filterList() {
        if (!isRentedPowerToolsStored || !isPowerToolsStored || !isConfirmationStored || !isUsersStored) {
            return;
        }
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        filteredPowerTools.clear();
        filteredPowerTools.addAll(powerTools);
        Iterator<PowerToolModel> it = filteredPowerTools.iterator();
        while (it.hasNext()) {
            final PowerToolModel model = it.next();
            boolean found = false;
            for (ConfirmationPowerToolModel confirmationPowerToolModel : confirmations) {
                if (Objects.equals(confirmationPowerToolModel.getUserId(), currentUser.getUid()) &&
                        Objects.equals(confirmationPowerToolModel.getPowerToolId(), model.getId())) {
                    found = true;
                    break;
                }
            }
            for (RentedPowerToolModel rentedPowerToolModel : borrowedPowerTools) {
                if (Objects.equals(rentedPowerToolModel.getUserId(), currentUser.getUid()) &&
                        Objects.equals(rentedPowerToolModel.getPowerToolId(), model.getId())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                it.remove();
            }
        }

        ReturnPowerToolAdapter borrowPowerToolAdapter = new ReturnPowerToolAdapter(ReturnPowerToolActivity.this, filteredPowerTools);
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

        final DatabaseReference confirmations = FirebaseDatabase.getInstance().getReference("borrowed_powerTools");
        confirmations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                borrowedPowerTools.clear();
                borrowedPowerTools.addAll(DataStoreUtils.readRentedPowerTools(dataSnapshot.getValue()));
                isRentedPowerToolsStored = true;
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

    private class ReturnPowerToolAdapter extends ArrayAdapter<PowerToolModel> {

        ReturnPowerToolAdapter(Context context, List<PowerToolModel> powerTools) {
            super(context, 0, powerTools);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final PowerToolModel powerTool = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_customer_return_powertool_layout, parent, false);
            }
            final View view = convertView;
            TextView textView = view.findViewById(R.id.returnPowerToolListText);
            textView.setText(powerTool != null ? powerTool.toString() : "");

            if (!isRentedPowerToolsStored || !isPowerToolsStored || !isConfirmationStored || !isUsersStored) {
                return view;
            }
            for (ConfirmationPowerToolModel confirmationPowerToolModel : confirmations) {
                if (powerTool.getId().equals(confirmationPowerToolModel.getPowerToolId())) {
                    view.findViewById(R.id.studentReceivedPowerToolCheckbox).setVisibility(View.GONE);
                    break;
                }
            }

            return view;
        }
    }

}