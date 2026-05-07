package saeihm;

import java.util.Random;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class GenGraphe {
    private static double probaMaternite = 0.60;
    private static double autreProba = 0.20;
    private static int nbMaternite = 0;
    private static int nbBloc = 0;
    private static int nbCentre = 0;
    private static int nbRoute = 0;
    
    private static void setFiabiliteRoute(Edge e){
        Random r = new Random();
        double range = 1.0 - 0.2 + 1.0;
        double n =  r.nextDouble(range) + 0.2;
        e.addAttribute("fiabilité", n);
    }
    
    private static void setDistanceRoute(Edge e){
        Random r = new Random();
        double range = 50 - 10 + 1;
        double n =  r.nextDouble(range) + 10;
        e.addAttribute("distance", n);
    }
    
    private static Edge setDureeRoute(Edge e){
        Random r = new Random();
        double range = 120 - 10 + 1;
        double n =  r.nextDouble(range) + 10;
        e.addAttribute("durée", n);
        return e;
    }
    
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
                    e.setAttribute("ui.label","Fiabilité: "+String.format("%.2f",(double) e.getAttribute("fiabilité")));
                    setDistanceRoute(e);
                    setDureeRoute(e);
                    }
                }
        }  
        return carteDistrict;
    }
}
