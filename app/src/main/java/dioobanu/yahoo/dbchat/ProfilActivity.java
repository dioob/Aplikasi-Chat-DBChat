package dioobanu.yahoo.dbchat;

import android.app.ProgressDialog;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfilActivity extends AppCompatActivity {

    private ImageView mProfilImage;
    private TextView mProfilNama, mProfilStatus;
    private Button mProfilSendReqBtn;
    private Button mProfilDeclineBtn;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendsReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotifikasiDatabase;
    private DatabaseReference mRootRef;

    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;
    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        final String user_id;
        String data = getIntent().getStringExtra("user_id"); //mengambil intent dari class UsersActivity
        if(data == null){
            user_id= getIntent().getStringExtra("from_user_id");
        }else{
            user_id = getIntent().getStringExtra("user_id");
        }
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendsReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotifikasiDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfilImage = findViewById(R.id.profil_image);
        mProfilNama = findViewById(R.id.display_profile_name);
        mProfilStatus = findViewById(R.id.display_profil_status);
        //mProfilFriendsCount = findViewById(R.id.display_totalfriends);
        mProfilSendReqBtn = findViewById(R.id.send_req_btn);

        mProfilDeclineBtn = findViewById(R.id.send_decline_btn);
        mProfilDeclineBtn.setVisibility(View.INVISIBLE);
        mProfilDeclineBtn.setEnabled(false);

        mCurrent_state = "bukan_teman";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading..");
        mProgressDialog.setMessage("Mohon tunggu, sedang memuat profil pengguna.");
        mProgressDialog.setCanceledOnTouchOutside(false);

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String nama = dataSnapshot.child("nama").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfilNama.setText(nama);
                mProfilStatus.setText(status);

                if (!image.equals("default")) {
                    Picasso.with(ProfilActivity.this).load(image).placeholder(R.drawable.defaultavatar).into(mProfilImage);
                }

                //------------------------------ Daftar Teman / Fitur Permintaan ---------------------
                mFriendsReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)){

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (req_type.equals("received")){

                                mCurrent_state = "req_received";
                                mProfilSendReqBtn.setText("TERIMA PERMINTAAN PERTEMANAN");

                                mProfilDeclineBtn.setVisibility(View.VISIBLE);
                                mProfilDeclineBtn.setEnabled(true);

                            }else if (req_type.equals("sent")){

                                mCurrent_state = "req_sent";
                                mProfilSendReqBtn.setText("BATALKAN PERMINTAAN PERTEMANAN");

                                mProfilDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfilDeclineBtn.setEnabled(false);
                            }

                            mProgressDialog.dismiss();
                        }else {

                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)){

                                        mCurrent_state = "teman";
                                        mProfilSendReqBtn.setText("BATALKAN PERTEMANAN");

                                        mProfilDeclineBtn.setVisibility(View.INVISIBLE);
                                        mProfilDeclineBtn.setEnabled(false);
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });


        mProfilSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfilSendReqBtn.setEnabled(false); //tombol tidak akan bisa ditekan lagi setelah mengirim permintaan pertemanan


                //---------------------------- JIKA BUKAN TEMAN ---------------------------------------------/
                if (mCurrent_state.equals("bukan_teman")){

                    DatabaseReference newNotificationref = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String,String> notifikasiData = new HashMap<>();
                    notifikasiData.put("from", mCurrentUser.getUid());
                    notifikasiData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_Req/" + mCurrentUser.getUid() + "/" + user_id + "/request_type","sent");
                    requestMap.put("Friend_Req/" + user_id + "/" + mCurrentUser.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + user_id + "/" + newNotificationId, notifikasiData);



                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            //----------------------------------
                            if (databaseError != null){

                                Toast.makeText(ProfilActivity.this, "Terjadi Error saat mengirim permintaan", Toast.LENGTH_SHORT).show();
                            }
                            //--------------------------------------
                            if (mCurrent_state.equals("teman")){
                                mProfilSendReqBtn.setEnabled(true);
                                mCurrent_state = "req_received";
                                mProfilSendReqBtn.setText("BATALKAN PERTEMANAN");

                            }else{
                            mProfilSendReqBtn.setEnabled(true);
                            mCurrent_state = "req_sent";
                            mProfilSendReqBtn.setText("BATALKAN PERMINTAAN PERTEMANAN");
                            }
                        }
                    });
                }

                //---------------------------- MEMBATALKAN PERMINTAAN PERTEMANAN ---------------------------------/
                if (mCurrent_state.equals("req_sent")){

                    //final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friend_Req/"+mCurrentUser.getUid()+"/"+user_id, null);
                    unfriendMap.put("Friend_Req/"+user_id+"/"+mCurrentUser.getUid(),null);

                    unfriendMap.put("notifications/"+user_id,null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError == null){
                                mCurrent_state = "bukan_teman";
                                mProfilSendReqBtn.setText("KIRIM PERMINTAAN PERTEMANAN");

                                mProfilDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfilDeclineBtn.setEnabled(false);
                            }else {

                                String error = databaseError.getMessage();
                                Toast.makeText(ProfilActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                            mProfilSendReqBtn.setEnabled(true);

                        }
                    });
                }

                //------------------------- Kondisi Saat Permintaan Diterima ------------------------------------
                if (mCurrent_state.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendMap = new HashMap();
                    friendMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date",currentDate);
                    friendMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/date",currentDate);

                    friendMap.put("Friend_Req/"+mCurrentUser.getUid()+"/"+user_id, null);
                    friendMap.put("Friend_Req/"+user_id+"/"+mCurrentUser.getUid(),null);

                    friendMap.put("notifications/"+mCurrentUser.getUid(),null);

                    mRootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError == null){

                                mProfilSendReqBtn.setEnabled(true);
                                mCurrent_state = "teman";
                                mProfilSendReqBtn.setText("BATALKAN PERTEMANAN");

                                mProfilDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfilDeclineBtn.setEnabled(false);
                            }else {

                                String error = databaseError.getMessage();
                                Toast.makeText(ProfilActivity.this, error, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }

                //---------- UNFRIEND/Menghapus Pertemanan ----------------
                if (mCurrent_state.equals("teman")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/"+mCurrentUser.getUid()+"/"+user_id, null);
                    unfriendMap.put("Friends/"+user_id+"/"+mCurrentUser.getUid(),null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError == null){
                                mCurrent_state = "bukan_teman";
                                mProfilSendReqBtn.setText("KIRIM PERMINTAAN PERTEMANAN");

                                mProfilDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfilDeclineBtn.setEnabled(false);
                            }else {

                                String error = databaseError.getMessage();
                                Toast.makeText(ProfilActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                            mProfilSendReqBtn.setEnabled(true);
                        }
                    });
                }

            }
        });

        mProfilDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friend_Req/"+mCurrentUser.getUid()+"/"+user_id, null);
                    unfriendMap.put("Friend_Req/"+user_id+"/"+mCurrentUser.getUid(),null);

                    //unfriendMap.put("notifications/"+user_id,null);
                    unfriendMap.put("notifications/"+mCurrentUser.getUid(),null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError == null) {
                                mCurrent_state = "bukan_teman";
                                mProfilSendReqBtn.setText("KIRIM PERMINTAAN PERTEMANAN");

                                mProfilDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfilDeclineBtn.setEnabled(false);
                            } else {

                                String error = databaseError.getMessage();
                                Toast.makeText(ProfilActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                            mProfilSendReqBtn.setEnabled(true);

                        }
                    });


            }
        });

    }
}
