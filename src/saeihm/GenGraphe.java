package saeihm;

import java.util.Random;
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
     * Cette fonction initialise la distance entre deux centres comprise entre 10 et 120
     * @param e Une arrête du graphe
     */
    private static void setDureeRoute(Edge e){
        Random r = new Random();
        double range = 120 - 10 + 1;
        double n =  r.nextDouble(range) + 10;
        e.addAttribute("duree", n);
    }
    
    /**
     * Cette fonction génère un graphe de nbSommets voulu
     * @param nbSommets Nombre de sommets souhaité pour la création du graphe
     * @return carteDistrict : Un graphe de nbSommets avec des routes initialisé avec la fiablilité, la distance et la durée
     */
    public static Graph construireGrapheAleatoire(int nbSommets){
        Graph carteDistrict = new SingleGraph("carte");
        carteDistrict.setAttribute("ui.stylesheet", "node.maternite { fill-color: red; size: 20px; } " +"node.bloc { fill-color: blue; size: 20px; } "+"node.centre { fill-color: green; size: 20px; }");
        Random rand = new Random();
        double tirage;
        for(int i = 0; i<nbSommets ; i++){
            tirage = rand.nextDouble();
            if (tirage <= probaMaternite){
                nbMaternite++;
                Node node = carteDistrict.addNode("M"+nbMaternite);
                node.setAttribute("ui.class", "maternite");
                node.setAttribute("ui.label", node.getId());
            }
            else if (tirage <= probaMaternite + autreProba){
                nbBloc++;
                Node node = carteDistrict.addNode("BO"+nbBloc);
                node.setAttribute("ui.class", "bloc");
                node.setAttribute("ui.label", node.getId());
            }
            else {
                nbCentre++;
                Node node = carteDistrict.addNode("CN"+nbCentre);
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
                    Edge e = carteDistrict.addEdge("Route"+nbRoute, nodeJ.getId(), nodeK.getId());
                    setFiabiliteRoute(e);
                    e.setAttribute("ui.label","Fiabilité: "+String.format("%.2f",(double) e.getAttribute("fiabilite")));
                    setDistanceRoute(e);
                    e.setAttribute("ui.label","Distance: "+String.format("%.2f",(double) e.getAttribute("distance"))+"km");
                    setDureeRoute(e);
                    }
                }
        }  
        return carteDistrict;
    }
}
