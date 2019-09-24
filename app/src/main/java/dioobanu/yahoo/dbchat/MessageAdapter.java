package dioobanu.yahoo.dbchat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.graphics.Color;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> mMessageList){

        this.mMessageList=mMessageList;
    }


    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout,parent,false);

        return new MessageViewHolder(v);


    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        public CircleImageView profilImage;
        public TextView displayName;
        public TextView lokasiAlamat;
        public TextView waktudikirim;

        public MessageViewHolder(View view){
            super(view);

            messageText = view.findViewById(R.id.message_text_layout);
            profilImage = view.findViewById(R.id.message_profil_layout);
            displayName = view.findViewById(R.id.name_text_layout);
            waktudikirim = view.findViewById(R.id.time_text_layout);

            lokasiAlamat = view.findViewById(R.id.message_lokasi);//lokasi

        }

    }

    @Override
    public void onBindViewHolder(final MessageAdapter.MessageViewHolder viewHolder, int i) {

        mAuth = FirebaseAuth.getInstance();
        Messages c = mMessageList.get(i);

        String from_user = c.getFrom();
        //String message_type = c.getType();


        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("nama").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                viewHolder.displayName.setText(name);

                Picasso.with(viewHolder.profilImage.getContext()).load(image)
                        .placeholder(R.drawable.defaultavatar).into(viewHolder.profilImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (mAuth.getCurrentUser() != null) {
            String current_user_id = mAuth.getCurrentUser().getUid();
        if(from_user.equals(current_user_id)){

            viewHolder.messageText.setBackgroundResource(R.drawable.message_text_other_background);
            viewHolder.messageText.setTextColor(Color.BLACK);
        }else {
            viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
            viewHolder.messageText.setTextColor(Color.WHITE);
        }}

        viewHolder.messageText.setText(c.getMessage());
        viewHolder.lokasiAlamat.setText(c.getTempat());
        viewHolder.waktudikirim.setText(c.getWaktu());

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


}