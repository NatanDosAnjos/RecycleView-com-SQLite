package com.example.cadastrodepessoas.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.example.cadastrodepessoas.R;
import com.example.cadastrodepessoas.RecyclerItemClickListener;
import com.example.cadastrodepessoas.adapter.AdaptadorListaPessoa;
import com.example.cadastrodepessoas.helper.DbHelper;
import com.example.cadastrodepessoas.model.Person;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import static android.app.PendingIntent.getActivity;

public class MainActivity extends AppCompatActivity {

    DbHelper dataBase = new DbHelper(getApplicationContext());
    SQLiteDatabase myDataBase;
    ConstraintLayout consLayout;
    RecyclerView recyclerView;
    List<Person> people = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        consLayout = findViewById(R.id.consLayout);

        createDatabase("usarios.db");

        recyclerViewListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        people = recoverDataDataBase();
        createRecyclerView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK) {

            Person personTmp = (Person) data.getSerializableExtra("person");
            insertDataDb(personTmp.getName(), personTmp.getAge());
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_voltar, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.itemAdd:
                Intent intent;
                intent = new Intent(getApplicationContext(), CadastroDePessoas.class);

                startActivityForResult(intent, 1);
                break;

            case R.id.itemConfiguracoes:
                Snackbar.make(consLayout, "Configurado", BaseTransientBottomBar.LENGTH_SHORT).show();
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }

        return super.onOptionsItemSelected(item);
    }

    public void recyclerViewListener() {
        recyclerView.addOnItemTouchListener( new RecyclerItemClickListener(getApplicationContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Snackbar.make(recyclerView, people.get(position).getName(), BaseTransientBottomBar.LENGTH_LONG).show();
            }

            @Override
            public void onLongItemClick(View view, int position) {
                if(deleteDataDataBase(myDataBase, people.get(position).getName())) {
                    Snackbar.make(recyclerView, people.get(position).getName() + " excluído", BaseTransientBottomBar.LENGTH_LONG).show();
                    people.remove(position);
                    createRecyclerView();
                } else {
                    Snackbar.make(recyclerView, "Erro ao excluir " + people.get(position).getName(), BaseTransientBottomBar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        }));
    }

    public void createRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        AdaptadorListaPessoa adapter = new AdaptadorListaPessoa(people);

        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayout.VERTICAL));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
    }

    public void insertDataDb(String name, int age) {

        String valuesInsert = "'" + name + "', " + age;
        myDataBase.execSQL( "INSERT INTO tabelaPessoas(nome, idade) VALUES("+valuesInsert+") ");

    }
    public void createDatabase(String nomeDb) {
        try {
            //criando o banco de dados
            myDataBase = openOrCreateDatabase(nomeDb,MODE_ENABLE_WRITE_AHEAD_LOGGING, null);

            //criando uma tabela
            myDataBase.execSQL("CREATE TABLE IF NOT EXISTS tabelaPessoas ( id INTEGER PRIMARY KEY AUTOINCREMENT, nome VARCHAR(20), idade INT(3) )");

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public boolean deleteDataDataBase(SQLiteDatabase db, String nameOnDataBase) {
        String comando = "DELETE FROM tabelaPessoas WHERE nome = '" +  nameOnDataBase + "'";
        System.out.println("Este é o comando: " + comando);
        try {
            db.execSQL(comando);
        } catch (Exception e) {
            Log.i("SQL - ", "Erro ao apagar registro");
            return false;
        }
        return true;
    }

    public List<Person> recoverDataDataBase() {
        Cursor cursor;
        int indiceNome;
        int indiceIdade;
        int indiceId;
        Person personTmp;
        List<Person> peopleTmp = new ArrayList<>();
        cursor = myDataBase.rawQuery("SELECT id, nome, idade FROM tabelaPessoas", null);

        try {

            indiceNome = cursor.getColumnIndex("nome");
            indiceIdade = cursor.getColumnIndex("idade");
            indiceId = cursor.getColumnIndex("id");

            cursor.moveToFirst();
            while (cursor != null) {

                personTmp = (instanciatePerson(cursor.getString(indiceNome), cursor.getInt(indiceIdade)));
                personTmp.setId(cursor.getInt(indiceId));
                peopleTmp.add(personTmp);
                System.out.println(personTmp.getId());

                cursor.moveToNext();
            }
            cursor.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return peopleTmp;
    }

    public Person instanciatePerson(String name, int age) {
        return new Person(name, age);
    }
}


