package dioobanu.yahoo.dbchat;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private EditText mTulisCari;
    private ImageButton mTombolCari;

    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = findViewById(R.id.usersAppBar);
        mUsersList = findViewById(R.id.users_list);
        mTulisCari = findViewById(R.id.cariUser);
        mTombolCari = findViewById(R.id.tombolCari);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Semua User");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mTombolCari.setOnClickListener(new View.OnClickListener() { //set click untuk tombol cari
            @Override
            public void onClick(View v) {

                String searchText = mTulisCari.getText().toString();

                if (searchText.equals(null)){

                    startListening();
                }else
                searchFirebase(searchText);
            }
        });
    }


    @Override
    protected void onStart() {

        super.onStart();
        startListening();
    }
    public void searchFirebase(String searchText){

        Query firebaseSearchQuery = mUsersDatabase.orderByChild("nama").startAt(searchText).endAt(searchText + "\uf8ff");
        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(firebaseSearchQuery, Users.class).build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(options)
        {
            @Override
            public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);
                return new UserViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(UserViewHolder holder, int position, Users model) {
                // Bind the Chat object to the ChatHolder
                holder.setNama(model.nama); //masukan nama user ke dalam method setName
                holder.setStatus(model. status); //masukan nama user ke dalam method setStatus
                holder.setThumb_image(model. thumb_image);

                final String user_id = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileActivity = new Intent(UsersActivity.this, ProfilActivity.class);
                        profileActivity.putExtra("user_id", user_id);
                        startActivity(profileActivity);
                    }
                });
                // ...
            }
        };

        mUsersList.setAdapter(adapter);
        adapter.startListening();


    }

    public void startListening() {

        Query query = FirebaseDatabase.getInstance().getReference().child("Users").limitToLast(50);
        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(query, Users.class).build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(options)
        {
            @Override
            public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);
                return new UserViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(UserViewHolder holder, int position, Users model) {
                // Bind the Chat object to the ChatHolder
                holder.setNama(model.nama); //masukan nama user ke dalam method setName
                holder.setStatus(model. status); //masukan nama user ke dalam method setStatus
                holder.setThumb_image(model. thumb_image);

                final String user_id = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileActivity = new Intent(UsersActivity.this, ProfilActivity.class);
                        profileActivity.putExtra("user_id", user_id);
                        startActivity(profileActivity);
                    }
                });
                // ...
                }
        };

        mUsersList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UserViewHolder(View itemView)
        {
        super(itemView);
        mView = itemView;
        }

        public void setNama(String nama)
        {
            TextView userNamaView = mView.findViewById(R.id.user_single_name);
            userNamaView.setText(nama);
        }

        public void setStatus(String status)
        {
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }

        public void setThumb_image(String thumb_image) {
            CircleImageView userThumbImage = mView.findViewById(R.id.user_single_image);
            Picasso.with(mView.getContext()).load(thumb_image).placeholder(R.drawable.defaultavatar).into(userThumbImage);
        }
    }
}
