package project.quikERent.activity.common;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import project.quikERent.R;
import project.quikERent.models.AdminUserModel;
import project.quikERent.models.RentedPowerToolModel;
import project.quikERent.models.ConfirmationPowerToolModel;
import project.quikERent.models.ConfirmationType;
import project.quikERent.models.UserModel;

public class CheckStatePowerToolActivity extends AppCompatActivity {
    public final static String SOUL = "vMv48nFoBWvfREAFBjKVvQWAZkEIRhLV9TBYKS2A";
    public boolean isAdmin = false;
    public HashMap<String, UserModel> usersMap = new HashMap<>();
    public Activity activity;

    private IntentIntegrator qrScan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_common_check_state_powertool);
            qrScan = new IntentIntegrator(this);
            checkIfLoggedUserIsAdmin();
            updateUsersMap();
            qrScan.initiateScan();
            activity = this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else if(result.getContents().startsWith(SOUL)){
                final String powerToolId = result.getContents().replace(SOUL,"");
                final DatabaseReference powerToolsRef = FirebaseDatabase.getInstance().getReference("borrowed_powerTools");
                powerToolsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        powerToolsRef.removeEventListener(this);
                        if (dataSnapshot.getValue() == null) {
                            return;
                        }
                        HashMap<String,Object> mapEntries = (HashMap<String,Object>) dataSnapshot.getValue();
                        HashMap<String,Object> powerTool = (HashMap<String,Object>) mapEntries.get(powerToolId);
                        if(powerTool!=null){
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            Date date = dataSnapshot.child(powerToolId).getValue(RentedPowerToolModel.class).getRentedDate();
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.add(Calendar.DAY_OF_YEAR,1);
                            Date untilDate = calendar.getTime();
                            builder.setTitle("PowerTool already borrowed!")
                                    .setMessage("This powerTool is already borrowed by" + usersMap.get(powerTool.get("userId")).getEmail() +" until " + untilDate)
                                    .setPositiveButton("Continue scanning!", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            qrScan.initiateScan();
                                        }
                                    })
                                    .setNegativeButton("End scanning!", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    }).show();
                        } else {
                            final DatabaseReference powerToolsRef = FirebaseDatabase.getInstance().getReference("confirmations");
                            powerToolsRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    powerToolsRef.removeEventListener(this);
                                    if (dataSnapshot.getValue() == null) {
                                        return;
                                    }
                                    HashMap<String,Object> mapEntries = (HashMap<String,Object>) dataSnapshot.getValue();
                                    HashMap<String,Object> powerTool = (HashMap<String,Object>) mapEntries.get(powerToolId);
                                    if(powerTool!=null){
                                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                        Date date = dataSnapshot.child(powerToolId).getValue(ConfirmationPowerToolModel.class).getDatetime();
                                        builder.setTitle("PowerTool already reserved!")
                                                .setMessage("This powerTool is already reserved by" + usersMap.get(powerTool.get("userId")).getEmail() +" until " + date)
                                                .setPositiveButton("Continue scanning!", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        qrScan.initiateScan();
                                                    }
                                                })
                                                .setNegativeButton("End scanning!", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        finish();
                                                    }
                                                }).show();
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                        if(isAdmin) {
                                            builder.setTitle("PowerTool is available!")
                                                    .setMessage("PowerTool is available.")
                                                    .setPositiveButton("Continue scanning!", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            qrScan.initiateScan();
                                                        }
                                                    })
                                                    .setNegativeButton("End scanning!", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            finish();
                                                        }
                                                    }).show();
                                        } else {
                                            builder.setTitle("PowerTool is available!")
                                                    .setMessage("PowerTool is available. Do you want reserve this powerTool??")
                                                    .setPositiveButton("Continue scanning!", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            qrScan.initiateScan();
                                                        }
                                                    })
                                                    .setNeutralButton("Reserve!", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            final ConfirmationPowerToolModel confirmationPowerToolModel = new ConfirmationPowerToolModel();
                                                            confirmationPowerToolModel.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                            confirmationPowerToolModel.setPowerToolId(Long.valueOf(powerToolId));
                                                            confirmationPowerToolModel.setType(ConfirmationType.RENT);
                                                            Date now = new Date();
                                                            now.setTime(now.getTime() + 24 * 60 * 60 * 1000);
                                                            confirmationPowerToolModel.setDatetime(now);
                                                            powerToolsRef.child(powerToolId).setValue(confirmationPowerToolModel);
                                                            Toast.makeText(getApplicationContext(), "PowerTool reserved", Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        }
                                                    })
                                                    .setNegativeButton("End scanning!", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            finish();
                                                        }
                                                    }).show();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w("Err:listofpowerTools:", databaseError.toException());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("Err:listofpowerTools:", databaseError.toException());
                    }
                });
            }
        } else {
            Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
        }
    }

    private void checkIfLoggedUserIsAdmin() {
        final DatabaseReference users = FirebaseDatabase.getInstance().getReference("admin_users");
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.removeEventListener(this);
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                for (DataSnapshot adminUserSnapshot: dataSnapshot.getChildren()) {
                    final String email = (String) adminUserSnapshot.getValue();
                    AdminUserModel adminUserModel = new AdminUserModel(email);
                    //Log.d("Failed", (String) adminUserSnapshot.getValue());

                    if (FirebaseAuth.getInstance().getCurrentUser().getEmail().equalsIgnoreCase(adminUserModel.getEmail())) {
                        isAdmin = true;
                        break;
                    }

                }

                Toast.makeText(getApplicationContext(),"isAdmin: "+isAdmin,Toast.LENGTH_SHORT);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("err:CheckState:184", databaseError.getMessage());
            }
        });
    }

    private void updateUsersMap() {
        final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usersRef.removeEventListener(this);
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserModel user = snapshot.getValue(UserModel.class);
                    usersMap.put(user.getUId(), user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
