package saeihm;

import org.graphstream.graph.Graph;

public class Main {

    public static void main(String[] args) {
        Graph carte = GenGraphe.construireGrapheAleatoire(10);
        carte.display();
        FenetrePrincipale fen1 = new FenetrePrincipale();
    }
    
}
