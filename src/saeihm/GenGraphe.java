package saeihm;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

/**
 * Cette classe permet d'initialiser un graphe aléatoire ou par lecture de fichier csv
 * @author Bastien Dias
 */
public class GenGraphe {
    private static double probaMaternite = 0.60;
    private static double autreProba = 0.20;
    private static int nbMaternite = 0;
    private static int nbBloc = 0;
    private static int nbCentre = 0;
    private static int nbRoute = 0;
    
    /**
     * Cette fonction initialise la fiabilité de la route comprise entre 0.2 et 1
     * @param e Une arrête du graphe
     */
    private static void setFiabiliteRoute(Edge e){
        Random r = new Random();
        double range = 1.0 - 0.2 + 1.0;
        double n =  r.nextDouble(range) + 0.2;
        e.addAttribute("fiabilite", n);
    }
    
    /**
     * Cette fonction initialise la distance entre deux centres comprise entre 10 et 50
     * @param e Une arrête du graphe
     */
    private static void setDistanceRoute(Edge e){
        Random r = new Random();
        double range = 50 - 10 + 1;
        double n =  r.nextDouble(range) + 10;
        e.addAttribute("distance", n);
    }
    
    /**
     * Cette fonction initialise la durée entre deux centres comprise entre 10 et 120
     * @param e Une arrête du graphe
     */
    private static void setDureeRoute(Edge e){
        Random r = new Random();
        double range = 120 - 10 + 1;
        double n =  r.nextDouble(range) + 10;
        e.addAttribute("duree", n);
    }
    
    /**
     * Cette fonction génère un graphe aléatoire de nbSommets voulu
     * @param nbSommets Nombre de sommets souhaité pour la création du graphe
     * @return carteDistrict : Un graphe de nbSommets avec des routes initialisé avec la fiabilité, la distance et la durée
     */
    public static Graph construireGrapheAleatoire(int nbSommets){
        Graph carteDistrict = new SingleGraph("carte");
        carteDistrict.setAttribute("ui.stylesheet",
            "node.maternite { fill-color: red; size: 20px; } " +
            "node.bloc { fill-color: blue; size: 20px; } " +
            "node.centre { fill-color: green; size: 20px; }");
        Random rand = new Random();
        double tirage;
        for(int i = 0; i < nbSommets; i++){
            tirage = rand.nextDouble();
            if (tirage <= probaMaternite){
                nbMaternite++;
                Node node = carteDistrict.addNode("M" + nbMaternite);
                node.setAttribute("ui.class", "maternite");
                node.setAttribute("ui.label", node.getId());
            }
            else if (tirage <= probaMaternite + autreProba){
                nbBloc++;
                Node node = carteDistrict.addNode("BO" + nbBloc);
                node.setAttribute("ui.class", "bloc");
                node.setAttribute("ui.label", node.getId());
            }
            else {
                nbCentre++;
                Node node = carteDistrict.addNode("CN" + nbCentre);
                node.setAttribute("ui.class", "centre");
                node.setAttribute("ui.label", node.getId());
            }
        }
        for (int j = 0; j < carteDistrict.getNodeCount(); j++){
            for (int k = j+1; k < carteDistrict.getNodeCount(); k++){
                Node nodeJ = carteDistrict.getNode(j);
                Node nodeK = carteDistrict.getNode(k);
                if (rand.nextDouble() <= autreProba){
                    nbRoute++;
                    Edge e = carteDistrict.addEdge("Route" + nbRoute, nodeJ.getId(), nodeK.getId());
                    setFiabiliteRoute(e);
                    e.setAttribute("ui.label", "Fiabilité: " + String.format("%.2f", (double) e.getAttribute("fiabilite")));
                    setDistanceRoute(e);
                    e.setAttribute("ui.label", "Distance: " + String.format("%.2f", (double) e.getAttribute("distance")) + "km");
                    setDureeRoute(e);
                }
            }
        }
        return carteDistrict;
    }
    
    /**
     * Cette fonction charge un graphe depuis un fichier CSV au format matrice d'adjacence.
     * Chaque ligne du fichier représente un sommet et ses arêtes vers les sommets précédents.
     * Les lignes commençant par "//" sont des commentaires et sont ignorées.
     * Une cellule à "0" indique l'absence d'arête, sinon elle contient "fiabilité,distance,durée".
     * @param cheminFichier Le chemin absolu vers le fichier CSV à lire
     * @return carteDistrict : Un graphe construit depuis le fichier CSV, avec les attributs
     *                         fiabilité, distance et durée initialisés sur chaque arête
     */
    public static Graph chargerGraphe(String cheminFichier) {

        Graph carteDistrict = new SingleGraph("carte");
        carteDistrict.setAttribute("ui.stylesheet",
            "node.maternite { fill-color: red; size: 20px; } " +
            "node.bloc { fill-color: blue; size: 20px; } " +
            "node.centre { fill-color: green; size: 20px; }");

        List<String> idsSommets = new ArrayList<>();

        try (BufferedReader lecteur = new BufferedReader(new FileReader(cheminFichier))) {

            String ligne;
            int numeroLigne = 0; 

            while ((ligne = lecteur.readLine()) != null) {

                ligne = ligne.trim();
                if (ligne.isEmpty()) continue;
                if (ligne.startsWith("//")) continue;
                String[] colonnes = ligne.split(";", -1);

                String idSommet = colonnes[0].trim(); //ID du Sommet

                String typeSommet = colonnes[1].trim(); //TYPE du Sommet

                Node noeud = carteDistrict.addNode(idSommet);
                noeud.setAttribute("ui.label", idSommet);

                switch (typeSommet) {
                    case "M":
                        noeud.setAttribute("ui.class", "maternite");
                        noeud.setAttribute("type", "maternite");
                        break;
                    case "O":
                        noeud.setAttribute("ui.class", "bloc");
                        noeud.setAttribute("type", "bloc");
                        break;
                    case "N":
                        noeud.setAttribute("ui.class", "centre");
                        noeud.setAttribute("type", "centre");
                        break;
                    default:
                        noeud.setAttribute("type", "inconnu");
                        break;
                }

                idsSommets.add(idSommet);

                for (int j = 0; j < numeroLigne; j++) {

                    int indexColonne = 2 + j; 

                    if (indexColonne >= colonnes.length) continue;

                    String cellule = colonnes[indexColonne].trim();
                    if (cellule.equals("0")) continue;

                    String[] attributs = cellule.split(",");

                    if (attributs.length != 3) continue;

                    try {
                        double fiabilite = Integer.parseInt(attributs[0].trim()) / 10.0;

                        double distance = Double.parseDouble(attributs[1].trim());

                        double duree = Double.parseDouble(attributs[2].trim());

                        String idArete = idSommet + "--" + idsSommets.get(j);

                        String idCible = idsSommets.get(j);

                        Edge arete = carteDistrict.addEdge(idArete, idSommet, idCible);

                        arete.setAttribute("fiabilite", fiabilite);
                        arete.setAttribute("distance", distance);
                        arete.setAttribute("duree", duree);

                        arete.setAttribute("ui.label",
                            "Fiabilité :" + String.format("%.1f", fiabilite) +
                            " Distance :" + String.format("%.0f", distance) + "km" +
                            " Temps :" + String.format("%.0f", duree) + "min");

                    } catch (NumberFormatException e) {
                        System.err.println("Valeur non parsable dans la cellule \"" + cellule +
                            "\" ligne " + idSommet + " col " + j + " : " + e.getMessage());
                    }
                }

                numeroLigne++;
            }

        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier : " + cheminFichier);
        }

        return carteDistrict;
    }
}