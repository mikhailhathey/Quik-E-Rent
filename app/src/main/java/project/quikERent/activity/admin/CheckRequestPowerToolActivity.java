package project.quikERent.activity.admin;

import android.annotation.SuppressLint;
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
import java.util.List;

import project.quikERent.R;
import project.quikERent.models.SuggestPowerToolModel;
import project.quikERent.utils.DataStoreUtils;

public class CheckRequestPowerToolActivity extends AppCompatActivity {

    private ListView listView;
    private final List<SuggestPowerToolModel> listOfRequests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_check_suggest_powertool);
        listView = (ListView) findViewById(R.id.CheckRequestPowerToolListView);
        getAllPowerToolsFromDatabase();
    }

    void getAllPowerToolsFromDatabase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("request_powerTools");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                listOfRequests.clear();
                listOfRequests.addAll(DataStoreUtils.readRequest(dataSnapshot.getValue()));
                RequestPowerToolAdapter requestPowerToolAdapter = new RequestPowerToolAdapter(CheckRequestPowerToolActivity.this, listOfRequests);
                listView.setAdapter(requestPowerToolAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofpowerTools:", databaseError.toException());
            }
        });
    }

    private class RequestPowerToolAdapter extends ArrayAdapter<SuggestPowerToolModel> {

        RequestPowerToolAdapter(Context context, List<SuggestPowerToolModel> powerTools) {
            super(context, 0, powerTools);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final SuggestPowerToolModel powerTool = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_admin_check_suggest_powertool_layout, parent, false);
            }
            TextView textView = convertView.findViewById(R.id.listText);
            Button button = convertView.findViewById(R.id.checkSuggestPowerToolButton);
            textView.setText(powerTool != null ? powerTool.toString() : "");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("request_powerTools");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            myRef.removeEventListener(this);
                            myRef.child(String.valueOf(powerTool.getId())).removeValue();
                            Toast.makeText(getApplicationContext(), "Request removed!", Toast.LENGTH_SHORT).show();
                        }

                        @SuppressLint("LongLogTag")
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("err:RemPowerToolListene:183", databaseError.getMessage());
                        }
                    });
                }
            });
            return convertView;
        }
    }
}