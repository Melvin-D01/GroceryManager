package project.stn991614740.grocerymanagerapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    ArrayList<Food> foodArrayList;

    public MyAdapter(Context context, ArrayList<Food> foodArrayList) {
        this.context = context;
        this.foodArrayList = foodArrayList;
    }

    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.item,parent,false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {

        Food food = foodArrayList.get(position);
        holder.itemName.setText(food.Description);
        holder.expiryDate.setText(food.ExpirationDate);

    }

    @Override
    public int getItemCount() {
        return foodArrayList.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView expiryDate, itemName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.foodName);
            expiryDate = itemView.findViewById(R.id.expiryDate);
        }
    }
}