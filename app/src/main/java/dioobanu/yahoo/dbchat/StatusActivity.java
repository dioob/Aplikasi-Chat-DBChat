package dioobanu.yahoo.dbchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputEditText mStatus;
    private Button mSaveBtn;
    //firebase
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;
    //loading progress
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        //firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mToolbar = findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Status Akun");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value = getIntent().getStringExtra("status_value");

        mStatus = findViewById(R.id.status_input);
        mStatus.setText(status_value);
        mSaveBtn = findViewById(R.id.status_save_button);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //progress
                mProgress = new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Simpan Perubahan");
                mProgress.setMessage("Mohon tunggu. Kami sedang menyimpan perubahan status anda.");
                mProgress.show();

                String status = mStatus.getText().toString();
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            mProgress.dismiss();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error. Terjadi kesalahan dalam perubahan status.", Toast.LENGTH_LONG).show();

                        }
                    }
                });

            }
        });
    }
}
