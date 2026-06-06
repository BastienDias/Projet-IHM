/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package saeihm.modele;

import java.util.ArrayList;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 *
 * @author elieb
 */
public class Algo {
    
        public static ArrayList<String> filtrageSommet(Graph g, String typerechecher){

        ArrayList<String> idpt = new ArrayList<>() ;
        if (g != null) {
            if (typerechecher != null) {
                for(Node n :g.getEachNode()){

                    if(!n.getId().equals("S1")){

                        if(typerechecher.equals(n.getAttribute("type")) || typerechecher.equals("Tous")){
                            idpt.add(n.getId()) ;
                        }
                    }
                }
            }
        }
        return idpt ;
    }
    
}
