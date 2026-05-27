package saeihm;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

public class Main {

    public static void main(String[] args) {
        FenetrePrincipale fp = new FenetrePrincipale();
        //Graph carte = GenGraphe.chargerGraphe("./graphes/graphe1.csv");
        //carte.display();
        //Path chemin = GenGraphe.plusCourtChemin(carte, "S1", "S6");
        //System.out.println("Sommets du chemin :");
        //chemin.getNodePath().forEach(n -> System.out.println("  → " + n.getId()));

        //System.out.println("Routes empruntées :");
        //chemin.getEdgePath().forEach(e
        //-> System.out.println("  → " + e.getId()
        // + " | Durée : " + String.format("%.0f", (double) e.getAttribute("duree")) + " min")
        //);
        Graph graphe = GenGraphe.construireGrapheAleatoire(6);

        String idDebut = graphe.getNode(0).getId();
        String idFin = graphe.getNode(graphe.getNodeCount() - 1).getId();

        System.out.println("Recherche du chemin le plus sûr de " + idDebut + " vers " + idFin);

        Path chemin = GenGraphe.cheminPlusSur(graphe, idDebut, idFin);

        if (chemin == null || chemin.empty()) {
            System.out.println("Aucun chemin trouvé.");
        } else {
            // Construction de l'affichage des sommets
            StringBuilder sb = new StringBuilder();
            chemin.getNodePath().forEach(n -> {
                if (sb.length() > 0) {
                    sb.append("-");
                }
                sb.append(n.getId());
            });

            // Calcul de la durée estimée totale
            double dureeEstimee = 0;
            for (Edge e : chemin.getEdgePath()) {
                dureeEstimee += (double) e.getAttribute("dureeEstimee");
            }

            System.out.println(sb.toString() + " : durée estimée : " + String.format("%.0f", dureeEstimee) + " min");
        }
        //FenetrePrincipale fen1 = new FenetrePrincipale();

    }

}
