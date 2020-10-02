package project.quikERent.activity.common;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.quikERent.R;
import project.quikERent.activity.admin.AddPowerToolActivity;
import project.quikERent.activity.admin.CheckRequestPowerToolActivity;
import project.quikERent.activity.admin.ConfirmActivity;
import project.quikERent.activity.admin.RemovePowerToolActivity;
import project.quikERent.activity.customer.RentPowerToolActivity;
import project.quikERent.activity.customer.SuggestPowerToolActivity;
import project.quikERent.activity.customer.ReturnPowerToolActivity;
import project.quikERent.models.AdminUserModel;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_main);
        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
        setMenuVisibility();
        addOnClickListenersOnButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    private void addOnClickListenersOnButtons() {
        Integer[] buttonNames = new Integer[]{
                R.id.listOfPowerToolsButton,
                R.id.addPowerToolButton,
                R.id.removePowerToolButton,
                R.id.rentPowerToolButton,
                R.id.returnPowerToolButton,
                R.id.suggestPowerToolButton,
                R.id.adminReturnPowerToolButton,
                R.id.adminRentPowerToolButton,
                R.id.adminConfirmRentPowerToolButton,
                R.id.checkSuggestPowerToolButton,
                R.id.checkStatePowerToolButton,
                R.id.aboutButton,
                R.id.sign_out};
        for (int i = 0; i < buttonNames.length; i++) {
            Button button = (Button) findViewById(buttonNames[i]);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.listOfPowerToolsButton:
                            startActivity(new Intent(MainActivity.this, ListOfPowerToolsActivity.class));
                            break;
                        case R.id.addPowerToolButton:
                            startActivity(new Intent(MainActivity.this, AddPowerToolActivity.class));
                            break;
                        case R.id.removePowerToolButton:
                            startActivity(new Intent(MainActivity.this, RemovePowerToolActivity.class));
                            break;
                        case R.id.checkStatePowerToolButton:
                            startActivity(new Intent(MainActivity.this, CheckStatePowerToolActivity.class));
                            break;
                        case R.id.rentPowerToolButton:
                            startActivity(new Intent(MainActivity.this, RentPowerToolActivity.class));
                            break;
                        case R.id.returnPowerToolButton:
                            startActivity(new Intent(MainActivity.this, ReturnPowerToolActivity.class));
                            break;
                        case R.id.suggestPowerToolButton:
                            startActivity(new Intent(MainActivity.this, SuggestPowerToolActivity.class));
                            break;
                        case R.id.adminReturnPowerToolButton:
                            startActivity(new Intent(MainActivity.this, project.quikERent.activity.admin.ReturnPowerToolActivity.class));
                            break;
                        case R.id.adminConfirmRentPowerToolButton:
                            startActivity(new Intent(MainActivity.this, ConfirmActivity.class));
                            break;
                        case R.id.adminRentPowerToolButton:
                            startActivity(new Intent(MainActivity.this, project.quikERent.activity.admin.RentPowerToolActivity.class));
                            break;
                        case R.id.checkSuggestPowerToolButton:
                            startActivity(new Intent(MainActivity.this, CheckRequestPowerToolActivity.class));
                            break;
                        case R.id.aboutButton:
                            startActivity(new Intent(MainActivity.this, AboutActivity.class));
                        case R.id.sign_out:
                            findViewById(R.id.sign_out).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    auth.signOut();
                                }
                            });
                    }
                }
            });
        }
    }

    private void setMenuVisibility() {
        final DatabaseReference users = FirebaseDatabase.getInstance().getReference("admin_users");
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                boolean isAdmin = false;

                /*if (!CollectionUtils.isEmpty((List) dataSnapshot.getValue())) {
                    for (Object field : (List) dataSnapshot.getValue()) {
                        if (field != null) {
                            final HashMap<String, Object> fields = (HashMap<String, Object>) field;
                            final String email = (String) fields.get("email");
                            if (FirebaseAuth.getInstance().getCurrentUser().getEmail().equalsIgnoreCase(email)) {
                                isAdmin = true;
                                break;
                            }
                        }
                    }
                }*/

                for (DataSnapshot adminUserSnapshot: dataSnapshot.getChildren()) {
                    final String email = (String) adminUserSnapshot.getValue();
                    AdminUserModel adminUserModel = new AdminUserModel(email);

                    if (FirebaseAuth.getInstance().getCurrentUser().getEmail().equalsIgnoreCase(adminUserModel.getEmail())) {
                        isAdmin = true;
                        break;
                    }

                }

                LinearLayout menu = (LinearLayout) findViewById(R.id.menu);
                if (isAdmin) {
                    menu.removeView(findViewById(R.id.rentPowerToolButton));
                    menu.removeView(findViewById(R.id.returnPowerToolButton));
                    menu.removeView(findViewById(R.id.suggestPowerToolButton));
                } else {
                    menu.removeView(findViewById(R.id.adminConfirmRentPowerToolButton));
                    menu.removeView(findViewById(R.id.adminRentPowerToolButton));
                    menu.removeView(findViewById(R.id.adminReturnPowerToolButton));
                    menu.removeView(findViewById(R.id.addPowerToolButton));
                    menu.removeView(findViewById(R.id.removePowerToolButton));
                    menu.removeView(findViewById(R.id.checkSuggestPowerToolButton));
                }
                menu.setVisibility(View.VISIBLE);
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("err:AddPowerToolListene:93", databaseError.getMessage());
            }
        });
    }


}