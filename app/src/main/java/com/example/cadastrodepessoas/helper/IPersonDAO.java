package com.example.cadastrodepessoas.helper;

import com.example.cadastrodepessoas.model.Person;

import java.util.List;

public interface IPersonDAO {

    public boolean save(Person person);
    public boolean update(Person person);
    public boolean delete(Person person);
    public List<Person> list();
}
