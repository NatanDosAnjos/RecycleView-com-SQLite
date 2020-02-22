package com.example.cadastrodepessoas.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.cadastrodepessoas.R;
import com.example.cadastrodepessoas.model.Person;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class CadastroDePessoas extends AppCompatActivity {

    ConstraintLayout layout;
    EditText inputNome;
    EditText inputAge;
    Person person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_de_pessoas);

        layout = findViewById(R.id.layout);
        inputAge = findViewById(R.id.inputIdade);
        inputNome = findViewById(R.id.inputNome);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_ok, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.ok) {
            person = createPerson();
            if (intentFunction(person) == true) {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public Person createPerson() {
        String name, ageTmp;
        int age;

        name = inputNome.getText().toString();
        ageTmp = inputAge.getText().toString();

        if (name.length() == 0 && ageTmp.length() == 0) {

            makeSnackBar("Preencha os campos ou volte para cancelar", "OK", BaseTransientBottomBar.LENGTH_LONG);

        } else if (ageTmp.length() == 0) {
            makeSnackBar("Preencha o campo idade", "OK", BaseTransientBottomBar.LENGTH_LONG, inputAge);


        } else if (name.length() == 0) {
            makeSnackBar("Preencha o campo nome", "OK", BaseTransientBottomBar.LENGTH_LONG, inputNome);

        } else {
            age = Integer.parseInt(ageTmp);
            Person person = new Person(name, age);

            return person;
        }

        return null;
    }


    public boolean intentFunction(Person person) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        if (person != null) {
            intent.putExtra("person", person);
            setResult(RESULT_OK, intent);
            return true;
        }
        return false;
    }

    public void makeSnackBar(String message, String btnMessage, int duration) {
        hideSoftKeyboard();
        Snackbar.make(layout, message, duration).setAction(btnMessage, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }
        ).show();
    }

    //Make Snackbar with focus on
    public void makeSnackBar(String message, String btnMessage, int duration, final View focusView) {
        hideSoftKeyboard();
        Snackbar.make(focusView, message, duration).setAction(btnMessage, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        focusView.requestFocus();
                        showSoftKeyboard(focusView);
                    }
                }
        ).show();
    }

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void showSoftKeyboard(View focus) {
        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        imm.showSoftInput(focus, InputMethodManager.SHOW_IMPLICIT);
    }
}

