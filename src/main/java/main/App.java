package main;

import main.model.joined.Developpeur;
import main.model.joined.Employe;
import main.model.joined.Manager;
import main.model.singletable.Moto;
import main.model.singletable.Vehicule;
import main.model.singletable.Voiture;
import main.model.tableperclass.Electronique;
import main.model.tableperclass.Livre;
import main.model.tableperclass.Produit;
import org.h2.tools.Server;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class App {

    private static final String UNITE = "hibernate-inheritance";

    private static final String URL_H2 = "jdbc:h2:file:./data/heritage_db;AUTO_SERVER=TRUE";
    private static final String USER_H2 = "sa";
    private static final String PASSWORD_H2 = "";
    private static final String PORT_CONSOLE = "8082";

    public static void main(String[] args) throws Exception {
        afficherBandeauConnexion();

        // La console web H2 reste accessible pendant toute l'exécution
        Server console = Server.createWebServer("-web", "-webPort", PORT_CONSOLE).start();
        System.out.println(">>> Console H2 disponible sur : " + console.getURL());

        Map<String, Object> surcharge = new HashMap<>();
        surcharge.put("javax.persistence.jdbc.url", URL_H2);
        surcharge.put("javax.persistence.jdbc.user", USER_H2);
        surcharge.put("javax.persistence.jdbc.password", PASSWORD_H2);

        EntityManagerFactory fabrique = Persistence.createEntityManagerFactory(UNITE, surcharge);
        try {
            enTransaction(fabrique, App::alimenter);
            titre("ETAT INITIAL");
            enTransaction(fabrique, App::afficher);

            enTransaction(fabrique, App::modifierEtSupprimer);
            titre("ETAT APRES MODIFICATIONS");
            enTransaction(fabrique, App::afficher);

            attendreEntree(console);
        } finally {
            fabrique.close();
            console.stop();
        }
    }

    /* ---------- Affichage / pause ---------- */

    private static void afficherBandeauConnexion() {
        System.out.println("**************************************************");
        System.out.println("*  BASE H2 : " + URL_H2);
        System.out.println("*  USER    : " + USER_H2 + "   PASSWORD : (vide)");
        System.out.println("**************************************************");
    }

    private static void attendreEntree(Server console) throws IOException {
        System.out.println("\n>>> Ouvre " + console.getURL() + " et connecte-toi avec :");
        System.out.println("    URL JDBC : " + URL_H2);
        System.out.println("    User     : " + USER_H2 + " / mot de passe vide");
        System.out.println(">>> Appuie sur Entrée pour arrêter l'application...");
        int touche = System.in.read();
        if (touche == -1) {
            System.out.println(">>> Entrée standard fermée, arrêt de l'application.");
        }
    }

    private static void titre(String texte) {
        System.out.println("\n==================== " + texte + " ====================");
    }

    /* ---------- Gestion de la transaction ---------- */

    private static void enTransaction(EntityManagerFactory fabrique, Consumer<EntityManager> action) {
        EntityManager em = fabrique.createEntityManager();
        try {
            em.getTransaction().begin();
            action.accept(em);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /* ---------- Insertion ---------- */

    private static void alimenter(EntityManager em) {
        Arrays.asList(
                new Voiture("Toyota", 4),
                new Voiture("Renault", 5),
                new Moto("Harley", true),
                new Moto("Yamaha", false)
        ).forEach(em::persist);

        Arrays.asList(
                new Developpeur("Imane", "Java"),
                new Developpeur("Sara", "Python"),
                new Manager("Karim", 3)
        ).forEach(em::persist);

        Arrays.asList(
                new Livre("Clean Code", "Robert C. Martin"),
                new Livre("Effective Java", "Joshua Bloch"),
                new Electronique("Smartphone", 24)
        ).forEach(em::persist);
    }

    /* ---------- Lecture ---------- */

    private static void afficher(EntityManager em) {
        // SINGLE_TABLE
        System.out.println("\n--- SINGLE_TABLE : vehicules ---");
        List<Vehicule> vehicules = em
                .createQuery("select v from Vehicule v order by v.id", Vehicule.class)
                .getResultList();
        for (Vehicule v : vehicules) {
            System.out.println("  " + v);
        }
        Long nbMotos = em
                .createQuery("select count(m) from Moto m", Long.class)
                .getSingleResult();
        System.out.println("  => nombre de motos : " + nbMotos);

        // JOINED
        System.out.println("\n--- JOINED : employes ---");
        List<Employe> employes = em
                .createQuery("select e from Employe e order by e.id", Employe.class)
                .getResultList();
        for (Employe e : employes) {
            System.out.println("  " + e);
        }
        List<Developpeur> devsJava = em
                .createQuery("select d from Developpeur d where d.langage = :langage", Developpeur.class)
                .setParameter("langage", "Java")
                .getResultList();
        for (Developpeur d : devsJava) {
            System.out.println("  => dev Java : " + d.getNom());
        }

        // TABLE_PER_CLASS
        System.out.println("\n--- TABLE_PER_CLASS : produits ---");
        List<Produit> produits = em
                .createQuery("select p from Produit p order by p.id", Produit.class)
                .getResultList();
        for (Produit p : produits) {
            System.out.println("  " + p);
        }
    }

    /* ---------- Mise à jour / suppression ---------- */

    private static void modifierEtSupprimer(EntityManager em) {
        Voiture toyota = em
                .createQuery("select v from Voiture v where v.marque = :marque", Voiture.class)
                .setParameter("marque", "Toyota")
                .getSingleResult();
        toyota.setMarque("Toyota Corolla");

        Manager karim = em
                .createQuery("select m from Manager m where m.nom = :nom", Manager.class)
                .setParameter("nom", "Karim")
                .getSingleResult();
        karim.setNombreEquipes(5);

        Livre aSupprimer = em
                .createQuery("select l from Livre l where l.nom = :nom", Livre.class)
                .setParameter("nom", "Effective Java")
                .getSingleResult();
        em.remove(aSupprimer);
    }
}