package com.example.cadastrodepessoas.activity;

import  android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import com.mooveit.library.Fakeit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("people");
    ConstraintLayout consLayout;
    RecyclerView recyclerView;
    List<Person> people = new ArrayList<>();
    PersonDAO personDAO;
    AdaptadorListaPessoa adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        consLayout = findViewById(R.id.consLayout);
        sharedPreferencesListener();
        //Iniciando API fakeIt
        Fakeit.init();

        //Criando Banco de Dados
        createDatabase();

        //Listeners
        recyclerViewListener();

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHandler(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT));

        helper.attachToRecyclerView(recyclerView);
    }


    private class ItemTouchHandler extends ItemTouchHelper.SimpleCallback {

        public ItemTouchHandler(int dragDirs, int swipeDirs) {
            super(dragDirs, swipeDirs);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int from = viewHolder.getAdapterPosition();
            int to = target.getAdapterPosition();

            Collections.swap(people,from,to);
            adapter.notifyItemMoved(from, to);

            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            adapter.notifyItemRemoved(position);
            people.remove(position);
        }
    }

    private void addFakePerson(int qtdOfPerson) {
        Person person;

        for(int cont = 0; cont < qtdOfPerson; cont++) {
            person = new Person();
            person.setName(Fakeit.name().firstName());
            person.setAge(randomNumberGenerate(100));
            people.add(person);
            personDAO.save(person);
        }
        createRecyclerView();

    }

    public int randomNumberGenerate(int maxNumber) {
        return (int)(Math.random()*maxNumber);
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
            if (insertDataDb(personTmp)) {
                snackbarMessage(Objects.requireNonNull(personTmp).getName() + " salvo com sucesso", "OK");
            } else {
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
                if (people.size() != 0) {
                    createDialogBox("Isso apagará toda a lista e não poderá ser revertido!");
                } else {
                    snackbarMessage("Lista já se encontra vazia", "OK");

                }
                break;

            case R.id.itemSync:
                createSyncDialogBox("Essa opção irá salvar sua lista na núvem");

                break;

            case R.id.addFake:
                addFakePerson(1);
                break;

            case R.id.upL:
                if (people.size()!=0) {
                    createDialogBoxUpdateList("Deseja reorganizar sua lista?");

                }
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }

        return super.onOptionsItemSelected(item);
    }

    public void createSyncDialogBox(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Sincronizar com a Núvem");
        dialog.setMessage(message);
        dialog.setIcon(R.drawable.ic_info_outline_24dp);

        dialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (people.size() >= 1) {
                    saveOnFirebase();
                    snackbarMessage(people.size() + " itens foram Sincronizados", "OK");

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
                if (people.size() > 1) {
                    snackbarMessage("Os " + people.size() + " itens foram Apagados", "OK");
                } else {
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

    public void createDialogBoxUpdateList(String message) {
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
                personDAO.saveList(people);
                if (people.size() > 1) {
                    snackbarMessage("Lista reorganizada", "OK");
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
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                createDialogBox(people.get(position));
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        }));
    }

    public void createRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        adapter = new AdaptadorListaPessoa(people);

        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayout.VERTICAL));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
    }

    //Adicionando listener ao SharedPreferences
    public void sharedPreferencesListener() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (sharedPreferences.getBoolean(key, false)) {
                    saveOnFirebase();
                }
            }
        });
    }

    public void saveOnFirebase() {
        DatabaseReference users;

        for (int i = 0; i < people.size(); i++) {
            users = databaseReference.child(people.get(i).getName());
            users.setValue(people.get(i));
        }
    }

    public boolean insertDataDb(Person person) {
        try {
            personDAO.save(person);
        } catch (Exception e) {
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
        if (!personDAO.delete(person)) {
            snackbarMessage("Erro ao excluir", "OK");
        } else {
            updateRecylcerView();
            snackbarMessage(person.getName() + " excluído", "Desfazer");
        }
    }

    public void recoverDataDataBase() {

        try {
            people = personDAO.list();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void snackbarMessage(String message, String btnMessage) {
        Snackbar.make(recyclerView, message, BaseTransientBottomBar.LENGTH_LONG).setAction(btnMessage, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
    }

    public void listarSensores() {

    }
}


