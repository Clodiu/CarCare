package com.example.carcare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class CarList_RecyclerViewAdapter extends RecyclerView.Adapter<CarList_RecyclerViewAdapter.MyViewHolder> {

    private final CarRecyclerViewInterface recyclerViewInterface;
    private Context context;
    private ArrayList<Car> userCars;

    public CarList_RecyclerViewAdapter(Context context, ArrayList<Car> userCars, CarRecyclerViewInterface recyclerViewInterface){
        this.context = context;
        this.userCars = userCars;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    public void setCars(ArrayList<Car> cars) {
        this.userCars = cars;
    }

    @NonNull
    @Override
    public CarList_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //This is where you inflate the layout (Ginving a look to our rows)
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.car_card_layout, parent,false);
        return new CarList_RecyclerViewAdapter.MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull CarList_RecyclerViewAdapter.MyViewHolder holder, int position) {
        // assigning values to the views we created in the car_card_layout
        // based on the position of the recycler view
        holder.carManufacturer.setText(userCars.get(position).getManufacturer());
        holder.carModel.setText(userCars.get(position).getModel());
        holder.carPlate.setText(userCars.get(position).getRegisterPlate());
        holder.carLastServiced.setText(userCars.get(position).getLastServiced());
    }

    @Override
    public int getItemCount() {
        //the recycler view just wants to know the number of items you want displayed
        return userCars.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        //grabbing the views from our car_card_layout
        //kinda like in the onCreate method

        TextView carManufacturer, carModel, carPlate, carLastServiced;

        public MyViewHolder(@NonNull View itemView, CarRecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            carManufacturer = itemView.findViewById(R.id.car_manufacturer);
            carModel = itemView.findViewById(R.id.car_model);
            carPlate = itemView.findViewById(R.id.car_register_plate);
            carLastServiced = itemView.findViewById(R.id.car_last_service);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recyclerViewInterface != null){
                        int pos = getAdapterPosition();

                        if(pos != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemClick(pos);
                        }
                    }

                }
            });
        }
    }
}
