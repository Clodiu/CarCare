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

    private final NoteRecyclerViewInterface recyclerViewInterface;

    private Context context;
    private ArrayList<Note> carNotes;

    public NoteList_RecyclerViewAdapter(Context context, ArrayList<Note> carNotes, NoteRecyclerViewInterface recyclerViewInterface){
        this.context = context;
        this.carNotes = carNotes;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public NoteList_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.notes_card_layout, parent,false);
        return new NoteList_RecyclerViewAdapter.MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteList_RecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.noteTitle.setText(carNotes.get(position).getTitle());
        holder.noteKm.setText("" + carNotes.get(position).getKm());
        holder.noteDate.setText(carNotes.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        return carNotes.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView noteTitle, noteKm, noteDate;

        public MyViewHolder(@NonNull View itemView, NoteRecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            noteTitle = itemView.findViewById(R.id.note_title);
            noteKm = itemView.findViewById(R.id.note_km);
            noteDate = itemView.findViewById(R.id.note_date);

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
