package com.example.carcare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NoteList_RecyclerViewAdapter extends RecyclerView.Adapter<NoteList_RecyclerViewAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Note> carNotes;

    public NoteList_RecyclerViewAdapter(Context context, ArrayList<Note> carNotes){
        this.context = context;
        this.carNotes = carNotes;
    }

    @NonNull
    @Override
    public NoteList_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.notes_card_layout, parent,false);
        return new NoteList_RecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteList_RecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.noteTitle.setText(carNotes.get(position).getTitle());
        holder.notePrice.setText(carNotes.get(position).getPrice());
    }

    @Override
    public int getItemCount() {
        return carNotes.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView noteTitle, notePrice;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            noteTitle = itemView.findViewById(R.id.note_title);
            notePrice = itemView.findViewById(R.id.note_price);
        }
    }
}
