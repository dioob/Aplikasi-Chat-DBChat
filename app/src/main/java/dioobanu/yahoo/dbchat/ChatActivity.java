package dioobanu.yahoo.dbchat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {


    //untuk lokasi
    private boolean gps_enabled=false;
    private boolean network_enabled=false;

    Location location;
    Double MyLat, MyLong;
    String CityName="";
    String StateName="";
    String CountryName="";
    String Address="";
    String NomorRumah="";
    String Kel="";
    String Kec="";
    String mLokasi="";
    //untuk lokasi

    String mWaktu=""; // jam dikirimnya pesan

    private String mChatUser;
    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;

    private TextView mTitleView;
    private TextView mLastSeenView;

    //private CircleImageView mProfilImage;
    private String mCurrentUserId;

    private FirebaseAuth mAuth;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatPesanView;
    private RecyclerView mPesanList;

    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    public static final int TOTAL_TIMES_UNTUK_LOAD = 10;
    private int mCurrentPage=1;
    private SwipeRefreshLayout mRefreshLayout;

    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        mChatToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        //------- currentuserid adalah pengirim pesan
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        //------ mchatuser adalah pengguna yang kita kirimi chat/ penerima
        mChatUser = getIntent().getStringExtra("user_id");
        String namaUser = getIntent().getStringExtra("user_name");
        getSupportActionBar().setTitle(namaUser);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        //----- Item dari action custom bar ------
        mTitleView = findViewById(R.id.custom_bar_title);
        mLastSeenView = findViewById(R.id.custom_bar_seen);
        //mProfilImage = findViewById(R.id.custom_bar_image);



        // ----- variabel dari komponen desain xml nya-----
        mChatAddBtn = findViewById(R.id.chat_add_btn);
        mChatSendBtn = findViewById(R.id.chat_send_btn);
        mChatPesanView = findViewById(R.id.chat_message_view);

        mAdapter = new MessageAdapter(messageList);
        mPesanList = findViewById(R.id.pesan_list);
        mRefreshLayout = findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);

        mPesanList.setHasFixedSize(true);
        mPesanList.setLayoutManager(mLinearLayout);
        mPesanList.setAdapter(mAdapter);

        loadMessages();

        mTitleView.setText(namaUser);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String dataTime = dataSnapshot.child("time_stamp").getValue().toString();
                //String image = dataSnapshot.child("thumb_image").toString();

                if (online.equals("true")){
                    mLastSeenView.setText("Online");
                }else {
                    GetTimeTadi getTimeTadi = new GetTimeTadi();

                    long lastTime = Long.parseLong(dataTime);

                    String terakhirDilihat = getTimeTadi.getTimeTadi(lastTime, getApplicationContext());
                    mLastSeenView.setText(terakhirDilihat);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mChatUser)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"+ mCurrentUserId +"/"+ mChatUser, chatAddMap);
                    chatUserMap.put("Chat/"+ mChatUser +"/"+ mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError != null){

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //turnGPSOn();
                getMyCurrentLocation();
                getCurrentTimeUsingDate();
                kirimPesan();
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;
                itemPos=0;
                loadMoreMessages();
            }
        });
    }

    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();


                if(!mPrevKey.equals(messageKey)){
                    messageList.add(itemPos++, message);
                }else{
                    mPrevKey = mLastKey;
                }
                if (itemPos==1){
                    mLastKey = messageKey;
                }
                Log.d("TOTALKEYS", "Last Key :"+mLastKey+" | Prev Key :"+mPrevKey+" | Message Key :"+messageKey);
                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(itemPos,0);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage = TOTAL_TIMES_UNTUK_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                itemPos++;
                if (itemPos==1){

                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }

                messageList.add(message);
                mAdapter.notifyDataSetChanged();

                mPesanList.scrollToPosition(messageList.size() -1); //setiap kali masuk untuk mengirim pesan akan menampilkan pesan paling baru
                mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void kirimPesan() {

        String pesan = mChatPesanView.getText().toString();

        if (!TextUtils.isEmpty(pesan)){

            String current_user_ref = "messages/"+mCurrentUserId+"/"+mChatUser;
            String chat_user_ref = "messages/"+mChatUser+"/"+mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();

            String pushId = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", pesan);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);
            messageMap.put("tempat", mLokasi);
            messageMap.put("waktu", mWaktu);


            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref+"/"+ pushId, messageMap);
            messageUserMap.put(chat_user_ref+"/"+ pushId, messageMap);

            mChatPesanView.setText(""); //mengembalikan edit text menjadi null ketika mengirim pesan / menekan tombol send

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    if (databaseError != null){

                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }
                }
            });


        }

    }


    //--------------------------------Jam------------------------
    public void getCurrentTimeUsingDate() {

        Date date = new Date();

        String mFormatJam = "hh:mm a, dd MMM yyyy";

        DateFormat formatJam = new SimpleDateFormat(mFormatJam);

        String jamTerformat= formatJam.format(date);

        mWaktu = " "+ jamTerformat;

    }

    //---------------------------------program untuk lokasi dimulai dari sini
    public void turnGPSOn(){
        try
        {

            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);


            if(!provider.contains("gps")){ //if gps is disabled
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                sendBroadcast(poke);
            }
        }
        catch (Exception e) {

        }
    }

    public void turnGPSOff(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(provider.contains("gps")){ //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }

    public void onStart() {
        super.onStart();
        turnGPSOn();
    }

    protected void onPause() {
        super.onPause();
        turnGPSOff();
    }

    @SuppressLint({"SetTextI18n", "MissingPermission"})
    void getMyCurrentLocation() {


        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locListener = new ChatActivity.MyLocationListener();

        try{gps_enabled=locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
        try{network_enabled=locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}

        //tidak memulai listener jika tidak ada provider yang enabled
        //if(!gps_enabled && !network_enabled)
        //return false;

        if(gps_enabled){
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
        }


        if(gps_enabled){
            location=locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }


        if(network_enabled && location==null){
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
        }


        if(network_enabled && location==null)    {
            location=locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (location != null) {

            MyLat = location.getLatitude();
            MyLong = location.getLongitude();

        //mendapatkan lokasi terakhir bilamana gps tidak dapat atau lemot untuk mendapatkan lokasi terkini
        } else {
            Location loc= getLastKnownLocation(this);
            if (loc != null) {

                MyLat = loc.getLatitude();
                MyLong = loc.getLongitude();
            }
        }
        locManager.removeUpdates(locListener); // menghapus periodik update dari location listener untuk

        // menghindari boros baterai. If you want to get location at the periodic intervals call this method using
        // pending intent.

        try
        {
            // Mendapat alamat dari locations.
            Geocoder geocoder;

            List<android.location.Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            addresses = geocoder.getFromLocation(MyLat, MyLong, 1);

            StateName= addresses.get(0).getAdminArea();
            CityName = addresses.get(0).getSubAdminArea();
            Address = addresses.get(0).getThoroughfare();
            NomorRumah = addresses.get(0).getSubThoroughfare();
            Kel = addresses.get(0).getSubLocality();
            Kec = addresses.get(0).getLocality();
            CountryName = addresses.get(0).getCountryName();
            // you can get more details other than this . like country code, state code, etc.

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (gps_enabled && network_enabled && location!=null){
        mLokasi = "Dikirim : "+Address+" "+NomorRumah+", "+CityName;
        }
        else if (gps_enabled && network_enabled && location == null){
            mLokasi = "GPS sedang OFF";
        }else {mLokasi = "GPS sedang OFF";}

        //lokasiAlamat.setText("Dikirim dari: "+ Address+" "+NomorRumah+", "+CityName);

    }

    // Location listener class. to get location.
    public class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            if (location != null) {
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    // below method to get the last remembered location. because we don't get locations all the times .At some instances we are unable to get the location from GPS. so at that moment it will show us the last stored location.

    public static Location getLastKnownLocation(Context context)
    {
        Location location = null;
        @SuppressLint("WrongConstant") LocationManager locationmanager = (LocationManager)context.getSystemService("location");
        List list = locationmanager.getAllProviders();
        boolean i = false;
        Iterator iterator = list.iterator();
        do
        {
            //System.out.println("---------------------------------------------------------------------");
            if(!iterator.hasNext())
                break;
            String s = (String)iterator.next();
            //if(i != 0 && !locationmanager.isProviderEnabled(s))
            if(i != false && !locationmanager.isProviderEnabled(s))
                continue;
            // System.out.println("provider ===> "+s);
            @SuppressLint("MissingPermission") Location location1 = locationmanager.getLastKnownLocation(s);
            if(location1 == null)
                continue;
            if(location != null)
            {
                //System.out.println("location ===> "+location);
                //System.out.println("location1 ===> "+location);
                float f = location.getAccuracy();
                float f1 = location1.getAccuracy();
                if(f >= f1)
                {
                    long l = location1.getTime();
                    long l1 = location.getTime();
                    if(l - l1 <= 600000L)
                        continue;
                }
            }
            location = location1;
            // System.out.println("location  out ===> "+location);
            //System.out.println("location1 out===> "+location);
            i = locationmanager.isProviderEnabled(s);
            // System.out.println("---------------------------------------------------------------------");
        } while(true);
        return location;
    }

}
