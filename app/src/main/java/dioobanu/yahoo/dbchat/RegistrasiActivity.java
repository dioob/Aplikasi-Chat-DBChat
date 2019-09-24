package dioobanu.yahoo.dbchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrasiActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    //firebase auth
    private FirebaseAuth mAuth;

    private ProgressDialog mRegProgress;

    private TextInputEditText mDisplayName;
    private TextInputEditText mEmail;
    private TextInputEditText mPassword;
    private Button mCreateBtn;

    //untuk validasi bentuk email yang proper
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private Matcher matcher;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi);

        mToolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Membuat Akun Baru");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //firebase auth
        mAuth = FirebaseAuth.getInstance();
        //progress dialog / loading
        mRegProgress =  new ProgressDialog(this);

        //android fields
        mDisplayName = findViewById(R.id.reg_display_name);
        mEmail = findViewById(R.id.reg_email);
        mPassword = findViewById(R.id.reg_password);
        mCreateBtn = findViewById(R.id.reg_create_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String display_nama = mDisplayName.getText().toString();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();


                if (!TextUtils.isEmpty(display_nama) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password))
                {
                    mRegProgress.setTitle("Membuat Akun");
                    mRegProgress.setMessage("Mohon tunggu, kami sedang membuatkan akun baru anda.");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    register_user(display_nama, email, password);
                }if(!validasiEmail(email)){
                    Toast.makeText(RegistrasiActivity.this, "Email yang anda masukan tidak valid.", Toast.LENGTH_LONG).show();
                }if (!validasiPassword(password)){
                    Toast.makeText(RegistrasiActivity.this, "Password yang anda masukkan harus minimal 6 karakter.", Toast.LENGTH_LONG).show();
                }
                else {
                    //Toast.makeText(RegistrasiActivity.this, "Gagal daftar. Mohon cek kembali dan coba lagi.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void register_user(final String display_nama, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful())
                {
                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    String device_token = FirebaseInstanceId.getInstance().getToken();

                    HashMap<String, String> usermap = new HashMap<>();
                    usermap.put("nama", display_nama);
                    usermap.put("status", "Hai! Aku juga memakai aplikasi DBChat.");
                    usermap.put("image", "default");
                    usermap.put("thumb_image", "default");
                    usermap.put("device_token", device_token);

                    mDatabase.setValue(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful())
                            {
                                mRegProgress.dismiss();
                                Intent mainIntent = new Intent(RegistrasiActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });

                }else
                    {
                        mRegProgress.hide();
                        //Toast.makeText(RegistrasiActivity.this, "Akun anda tidak dapat didaftarkan.", Toast.LENGTH_LONG).show();
                    }


            }
        });
    }

    //---- method validasi bentuk email yang benar
    public boolean validasiEmail(String email) {
        matcher = pattern.matcher(email);
        return matcher.matches();
    }
    //---- method validasi banyak karakter untuk password harus lebih dari 5 atau minimal 6 karakter
    public boolean validasiPassword(String password) {
        return password.length() > 5;
    }
}
