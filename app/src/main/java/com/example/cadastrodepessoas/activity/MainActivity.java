package com.example.cadastrodepessoas.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cadastrodepessoas.R;
import com.example.cadastrodepessoas.RecyclerItemClickListener;
import com.example.cadastrodepessoas.adapter.AdaptadorListaPessoa;
import com.example.cadastrodepessoas.helper.DbHelper;
import com.example.cadastrodepessoas.helper.PersonDAO;
import com.example.cadastrodepessoas.model.Person;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("people");
    ConstraintLayout consLayout;
    RecyclerView recyclerView;
    List<Person> people = new ArrayList<>();
    PersonDAO personDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        consLayout = findViewById(R.id.consLayout);

        //Criando Banco de Dados
        createDatabase();

        //Listeners
        recyclerViewListener();
    }

    public void updateRecylcerView() {
        recoverDataDataBase();
        createRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateRecylcerView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {

            Person personTmp = (Person) Objects.requireNonNull(data).getSerializableExtra("person");
            if(insertDataDb(personTmp)) {
                snackbarMessage(Objects.requireNonNull(personTmp).getName() + " salvo com sucesso", "OK");
            }
            else {
                snackbarMessage("Erro ao salvar", "OK");
            }
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
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.itemClearAll:
                if(people.size() != 0) {
                    createDialogBox("Isso apagará toda a lista e não poderá ser revertido!");
                }

                else {
                    snackbarMessage("Lista já se encontra vazia", "OK");

                }
                break;

            case R.id.itemSync:
                saveOnFirebase();
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }

        return super.onOptionsItemSelected(item);
    }

    public void createDialogBox(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Tem Certeza?");
        dialog.setMessage(message);
        dialog.setIcon(R.drawable.ic_warning_24dp);

        dialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DbHelper dbHelper = new DbHelper(getApplicationContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                dbHelper.resetTable(db, DbHelper.TABLE_PESSOAS);
                if(people.size() > 1) {
                    snackbarMessage("Os " + people.size() + " itens foram Apagados", "OK");
                }
                else {
                    snackbarMessage("O item " + people.get(0).getName() + " foi Apagado", "OK");
                }
                updateRecylcerView();
            }
        });

        dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        dialog.create();
        dialog.show();
    }

    public void createDialogBox(final Person person) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Tem Certeza?");
        dialog.setMessage("Deseja excluir " + person.getName() + " da lista?");

        dialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteDataDataBase(person);
            }
        });

        dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        dialog.create();
        dialog.show();
    }

    public void recyclerViewListener() {
        recyclerView.addOnItemTouchListener( new RecyclerItemClickListener(getApplicationContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Snackbar.make(recyclerView, people.get(position).getName(), BaseTransientBottomBar.LENGTH_SHORT).show();
            }

            @Override
            public void onLongItemClick(View view, int position) {
                createDialogBox(people.get(position));
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

    public void saveOnFirebase() {
        DatabaseReference users;

        for(int i = 0; i < people.size(); i++) {
            users = databaseReference.child(people.get(i).getName());
            users.setValue(people.get(i));
        }
    }

    public boolean insertDataDb(Person person) {
        try {
            personDAO.save(person);
        }

        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void createDatabase() {
        try {
            personDAO = new PersonDAO(getApplicationContext());

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void deleteDataDataBase(Person person) {
        if(!personDAO.delete(person)) {
            snackbarMessage("Erro ao excluir", "OK");
        }
        else {
            updateRecylcerView();
            snackbarMessage(person.getName() + " excluído", "Desfazer");
        }
    }

    public void recoverDataDataBase() {

        try {
            people = personDAO.list();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void snackbarMessage(String message, String btnMessage) {
     Snackbar.make(recyclerView,message,BaseTransientBottomBar.LENGTH_LONG).setAction(btnMessage, new View.OnClickListener() {
         @Override
         public void onClick(View v) {

         }
     }).show();
    }
}


