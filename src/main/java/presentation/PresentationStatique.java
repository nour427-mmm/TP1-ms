package presentation;

import dao.DaoImpl;
import metier.MetierImpl;

public class PresentationStatique {
    public static void main(String[] args) {
        DaoImpl dao = new DaoImpl();
        MetierImpl metier = new MetierImpl();


        metier.setDao(dao);


        System.out.println("Résultat du calcul = " + metier.calcul());
    }
}