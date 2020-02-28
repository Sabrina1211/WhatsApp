package com.example.whatsapp;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View PrivateChatsView;
    private RecyclerView chatsList;

    private DatabaseReference ChatsRef, UserRef;
    private FirebaseAuth mAuth;
    private String  currentUserID;






    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");


        chatsList = (RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));


        return PrivateChatsView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacs>options =
                new FirebaseRecyclerOptions.Builder<Contacs>()
                .setQuery(ChatsRef, Contacs.class)
                .build();

        FirebaseRecyclerAdapter<Contacs,ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacs, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacs model)
                    {
                        final  String userIDs = getRef(position).getKey();
                        final String[] retImage = {"default_image"};

                        UserRef.child(userIDs).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                               if (dataSnapshot.exists())
                               {
                                   if (dataSnapshot.hasChild("image"))
                                   {
                                        retImage[0] = dataSnapshot.child("image").getValue().toString();
                                       Picasso.get().load(retImage[0]).into(holder.profileImage);

                                   }

                                   final String retName = dataSnapshot.child("name").getValue().toString();
                                   final String retStatus = dataSnapshot.child("status").getValue().toString();


                                   holder.userName.setText(retName);


                                   if(dataSnapshot.child("userState").hasChild("state"))
                                   {
                                       String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                       String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                       String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                       if(state.equals("online"))
                                       {
                                           holder.userStatus.setText("online");
                                       }

                                       else if (state.equals("offline"))
                                       {
                                           holder.userStatus.setText("Last Seen: " + date + " " + time);

                                       }


                                   }

                                   else
                                   {
                                       holder.userStatus.setText("offline");
                                   }


                                   holder.itemView.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View view)
                                       {
                                           Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                           chatIntent.putExtra("visit_user_id", userIDs);
                                           chatIntent.putExtra("visit_user_name", retName);
                                           chatIntent.putExtra("visit_user_image", retImage[0]);


                                           startActivity(chatIntent);


                                       }
                                   });



                               }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {

                            }
                        });



                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout, viewGroup, false);
                        ChatsViewHolder viewHolder = new ChatsViewHolder(view);
                        return viewHolder;


                    }
                };

        chatsList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class  ChatsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImage;
        TextView userStatus, userName;


        public ChatsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            profileImage = itemView.findViewById(R.id.users_profile_image);
            userStatus = itemView.findViewById(R.id.user_status);
            userName = itemView.findViewById(R.id.user_profile_name);
        }
    }
}



