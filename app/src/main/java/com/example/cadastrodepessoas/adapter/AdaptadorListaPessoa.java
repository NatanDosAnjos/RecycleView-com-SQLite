package com.example.cadastrodepessoas.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cadastrodepessoas.R;
import com.example.cadastrodepessoas.model.Person;

import java.util.List;

public class AdaptadorListaPessoa extends RecyclerView.Adapter<AdaptadorListaPessoa.ViewHolderPessoa> {

    private List<Person> peopleList;

    public AdaptadorListaPessoa(List<Person> peopleList) {
        this.peopleList = peopleList;
    }

    @NonNull
    @Override
    public ViewHolderPessoa onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View viewLista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_lista, parent, false);

        return new ViewHolderPessoa(viewLista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderPessoa holder, int position) {
        Person person;
        person = peopleList.get(position);

        holder.nome.setText(person.getName());
        if (person.getAge() != 0) {
            holder.idade.setText(String.valueOf(person.getAge()));
        }

    }

    @Override
    public int getItemCount() {
        return peopleList.size();
    }


    //ViewHolder
    public class ViewHolderPessoa extends RecyclerView.ViewHolder {
        TextView nome;
        TextView idade;

        ViewHolderPessoa(@NonNull View itemView) {
            super(itemView);
            this.nome   = itemView.findViewById(R.id.name);
            this.idade  = itemView.findViewById(R.id.age);
        }
    }
}
