package com.example.mynotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.core.FirestoreClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class mynotesActivity extends AppCompatActivity {
    FloatingActionButton mcreatenotefab;
    private FirebaseAuth firebaseAuth;

    RecyclerView mrecyclerview;
    StaggeredGridLayoutManager staggeredGridLayoutManager;

    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;

    FirestoreRecyclerAdapter<firebasemodel,NoteViewHolder> noteadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mynotes);

        // Find the FloatingActionButton by its ID and assign it to mcreatenotefab
        mcreatenotefab = findViewById(R.id.createnotefab);

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore=FirebaseFirestore.getInstance();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("All Notes");
        }

        mcreatenotefab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mynotesActivity.this, createnote.class));
            }
        });


        Query query=firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("mynotes").orderBy("title",Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<firebasemodel> allusernotes=new FirestoreRecyclerOptions.Builder<firebasemodel>().setQuery(query,firebasemodel.class).build();

        noteadapter=new FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder>(allusernotes) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull firebasemodel firebasemodel) {
                ImageView popupbutton=noteViewHolder.itemView.findViewById(R.id.menupopupbutton);


                int colourcode=getrandomcolor();
                noteViewHolder.mnote.setBackgroundColor(noteViewHolder.itemView.getResources().getColor(colourcode,null));
                noteViewHolder.notetitle.setText(firebasemodel.getTitle());
                noteViewHolder.notecontent.setText(firebasemodel.getContent());

                String docId=noteadapter.getSnapshots().getSnapshot(i).getId();
                noteViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //we have to open note detail activity
                        Intent intent=new Intent(v.getContext(),notedetails.class);
                        intent.putExtra("title",firebasemodel.getTitle());
                        intent.putExtra("content",firebasemodel.getContent());
                        intent.putExtra("noteId",docId);
                        v.getContext().startActivity(intent);

                    }
                });

                popupbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popupMenu=new PopupMenu(v.getContext(),v);
                        popupMenu.setGravity(Gravity.END);
                        popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(@NonNull MenuItem item) {
                                Intent intent=new Intent(v.getContext(),editnoteactivity.class);
                                intent.putExtra("title",firebasemodel.getTitle());
                                intent.putExtra("content",firebasemodel.getContent());
                                intent.putExtra("noteId",docId);
                                v.getContext().startActivity(intent);
                                return false;
                            }
                        });
                        popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                //Toast.makeText(getApplicationContext(),"Note deleted successfully",Toast.LENGTH_SHORT).show();
                                DocumentReference documentReference=firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("mynotes").document(docId);
                                documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(v.getContext(),"Note deleted successfully",Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(v.getContext(),"Failed to delete",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return false;
                            }
                        });
                        popupMenu.show();
                    }
                });

            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout,parent,false);
                return new NoteViewHolder(view);
            }
        };


        mrecyclerview=findViewById(R.id.recyclerview);
        mrecyclerview.setHasFixedSize(true);
        staggeredGridLayoutManager=new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        mrecyclerview.setLayoutManager(staggeredGridLayoutManager);
        mrecyclerview.setAdapter(noteadapter);
    }



    public class NoteViewHolder extends RecyclerView.ViewHolder{

        private TextView notetitle;
        private TextView notecontent;
        LinearLayout mnote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            notetitle=itemView.findViewById(R.id.notetitle);
            notecontent=itemView.findViewById(R.id.notecontent);
            mnote=itemView.findViewById(R.id.note);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(mynotesActivity.this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStart(){
        super.onStart();
        noteadapter.startListening();
    }
    @Override
    protected void onStop(){
        super.onStop();
        if(noteadapter!=null){
            noteadapter.stopListening();
        }
    }
    private int getrandomcolor(){
        List<Integer> colorcode=new ArrayList<>();
        colorcode.add(R.color.gray);
        colorcode.add(R.color.skyblue);
        colorcode.add(R.color.lightgreen);
        colorcode.add(R.color.green);
        colorcode.add(R.color.teal_200);
        colorcode.add(R.color.teal_700);
        colorcode.add(R.color.color1);
        colorcode.add(R.color.color2);
        colorcode.add(R.color.color3);
        colorcode.add(R.color.color4);
        colorcode.add(R.color.color5);
        colorcode.add(R.color.pink);
        colorcode.add(R.color.purple_200);
        colorcode.add(R.color.purple_500);
        colorcode.add(R.color.purple_700);

        Random random=new Random();
        int number=random.nextInt(colorcode.size());
        return colorcode.get(number);
    }
}

