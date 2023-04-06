package project.stn991614740.grocerymanagerapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FridAct extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Food> foodArrayList;
    MyAdapter myAdapter;
    FirebaseFirestore db;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fridge);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Data...");
        progressDialog.show();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        foodArrayList = new ArrayList<>();
        myAdapter = new MyAdapter(this, foodArrayList);
        recyclerView.setAdapter(myAdapter);

        EventChangeListener();
    }

    private void EventChangeListener(){
        db.collection("food").orderBy("ExpirationDate", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if(progressDialog.isShowing()){
                            progressDialog.dismiss();
                            Log.e("Firebase error", error.getMessage());
                            return;
                        }

                        for (DocumentChange dc : value.getDocumentChanges()){
                            if (dc.getType() == DocumentChange.Type.ADDED){
                                foodArrayList.add(dc.getDocument().toObject(Food.class));
                            }
                        }

                        // Check if adapter is null or empty
                        if (myAdapter == null) {
                            myAdapter = new MyAdapter(FridAct.this, foodArrayList);
                            recyclerView.setAdapter(myAdapter);
                        } else if (foodArrayList.size() > 0) {
                            myAdapter.notifyDataSetChanged();
                        }

                        if(progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                });
    }

}
