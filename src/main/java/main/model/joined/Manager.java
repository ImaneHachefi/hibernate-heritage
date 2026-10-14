package main.model.joined;

import javax.persistence.Entity;

@Entity
public class Manager extends Employe {

    private int nombreEquipes;

    public Manager() {}

    public Manager(String nom, int nombreEquipes) {
        super(nom);
        this.nombreEquipes = nombreEquipes;
    }

    public int getNombreEquipes() { return nombreEquipes; }
    public void setNombreEquipes(int nombreEquipes) { this.nombreEquipes = nombreEquipes; }

    @Override
    public String toString() {
        return super.toString() + " equipes=" + nombreEquipes;
    }
}