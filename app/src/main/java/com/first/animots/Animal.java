package com.first.animots;

import android.content.Context;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

public class Animal {
    private final String nom;
    private String pronoun;

    public Animal(String nom, String pronoun) {
        this.nom = nom;
        this.pronoun = pronoun;
    }

    public String getNom() {
        return nom;
    }

    public String getPronoun() {
        return pronoun;
    }

    public static List<Animal> loadAnimalData(Context context) {
        List<Animal> animals = new ArrayList<>();

        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.animal_data);
            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            String jsonStr = scanner.hasNext() ? scanner.next() : "";

            JSONArray jsonArray = new JSONArray(jsonStr);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String name = obj.getString("name");
                String pronoun = obj.getString("pronoun");
                animals.add(new Animal(name, pronoun));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return animals;
    }

}
