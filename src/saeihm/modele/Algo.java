/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package saeihm.modele;

import java.util.ArrayList;
import java.util.List;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import static saeihm.modele.GenGraphe.plusCourtChemin;

/**
 *
 * @author elieb
 */
public class Algo {
    
    public static ArrayList<Node> filtrageSommet(Graph g, String typerechecher){
        
        ArrayList<Node> idpt = new ArrayList<>() ;
        if (g != null) {
            if (typerechecher != null) {
                for(Node n :g.getEachNode()){

                    if(!n.getId().equals("S1")){

                        if(typerechecher.equals(n.getAttribute("type")) || typerechecher.equals("Tous")){
                            idpt.add(n) ;
                        }
                    }
                }
            }
        }
        return idpt ;
    }
    
    public static Path tourneeLivraisonTotale(Graph g){
        List<Node> aVisiter = new ArrayList<>();
        Path tournee = new Path();
        Node courant = g.getNode(0);
        tournee.setRoot(courant);
        
        //Ajout de tous les sommets du graphe sauf celui de départ
        for (int i = 1; i < g.getNodeCount(); i++) {
            aVisiter.add(g.getNode(i));
        }    
        while(!aVisiter.isEmpty()){
            Node prochainCentre = null;
            Path meilleurChemin = null;
            double dureeMin = Double.MAX_VALUE;
            for (Node prochain : aVisiter) {
                Path p = plusCourtChemin(g, courant.getId(), prochain.getId());
                if(p!=null && !p.empty()){
                    double duree = 0;
                    for (Edge e : p.getEdgePath()) {
                        duree += (double) e.getAttribute("duree");
                    }
                    if(duree < dureeMin){
                        dureeMin = duree;
                        prochainCentre = prochain;
                        meilleurChemin = p;
                    }
                }
            }
            if(prochainCentre != null) {
                for (Edge e : meilleurChemin.getEdgePath()){
                        tournee.add(e); 
                }
                aVisiter.remove(prochainCentre);
                courant = prochainCentre;
            }
        }
        return tournee;
    }
    
    
    public static Path tourneeLivraisonFiltrage(Graph g, String type){
        ArrayList<Node> aVisiter = filtrageSommet(g, type);
        Path tournee = new Path();
        Node courant = g.getNode(0);
        tournee.setRoot(courant);
        
        while(!aVisiter.isEmpty()){
            Node prochainCentre = null;
            Path meilleurChemin = null;
            double dureeMin = Double.MAX_VALUE;
            for (Node prochain : aVisiter) {
                Path p = plusCourtChemin(g, courant.getId(), prochain.getId());
                if(p!=null && !p.empty()){
                    double duree = 0;
                    for (Edge e : p.getEdgePath()) {
                        duree += (double) e.getAttribute("duree");
                    }
                    if(duree < dureeMin){
                        dureeMin = duree;
                        prochainCentre = prochain;
                        meilleurChemin = p;
                    }
                }
            }
            if(prochainCentre != null) {
                for (Edge e : meilleurChemin.getEdgePath()){
                        tournee.add(e); 
                }
                aVisiter.remove(prochainCentre);
                courant = prochainCentre;
            }
        }
        return tournee;
    }
}
   
