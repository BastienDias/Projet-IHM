package saeihm.modele;

import java.io.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import static saeihm.modele.GenGraphe.plusCourtChemin;

/**
 * Algorithmes de tournee de vehicule pour la livraison des dispensaires (Question 6).
 * La tournee part toujours du sommet d'indice 0 du graphe (centre de depart).
 * L'approche utilisee est gloutonne : a chaque etape, on rejoint le centre non
 * encore visite le plus proche en duree, en empruntant le plus court chemin.
 * Toutes les methodes sont statiques.
 *
 * @author elieb
 */
public class Algo {

    /**
     * Selectionne les sommets a visiter en fonction d'un type recherche.
     * Le sommet "S1" est toujours exclu de la liste (point de depart suppose).
     * Si le type vaut "Tous", tous les sommets (sauf S1) sont retenus.
     * @param g Graphe a parcourir
     * @param typerechecher Type recherche ("M", "O", "N") ou "Tous" pour ne pas filtrer
     * @return Liste des sommets correspondant au filtre, vide si g ou le type est null
     */
    public static ArrayList<Node> filtrageSommet(Graph g, String typerechecher) {

        ArrayList<Node> idpt = new ArrayList<>();
        if (g != null) {
            if (typerechecher != null) {
                for (Node n : g.getEachNode()) {

                    if (!n.getId().equals("S1")) {

                        if (typerechecher.equals(n.getAttribute("type")) || typerechecher.equals("Tous")) {
                            idpt.add(n);
                        }
                    }
                }
            }
        }
        return idpt;
    }

    /**
     * Construit une tournee gloutonne passant par tous les sommets du graphe.
     * Le camion part du sommet d'indice 0, puis rejoint a chaque etape le sommet
     * non encore visite atteignable dans la plus petite duree, jusqu'a ce que tous
     * les sommets aient ete visites. Les sommets traverses en chemin sont consideres
     * comme livres.
     * @param g Graphe a parcourir (non null)
     * @return Le chemin complet de la tournee (Path enracine sur le sommet de depart)
     */
    public static Path tourneeLivraisonTotale(Graph g) {
        List<Node> aVisiter = new ArrayList<>();
        Path tournee = new Path();
        Node courant = g.getNode(0);
        tournee.setRoot(courant);

        // Ajout de tous les sommets du graphe sauf celui de depart
        for (int i = 1; i < g.getNodeCount(); i++) {
            aVisiter.add(g.getNode(i));
        }
        while (!aVisiter.isEmpty()) {
            Node prochainCentre = null;
            Path meilleurChemin = null;
            double dureeMin = Double.MAX_VALUE;
            for (Node prochain : aVisiter) {
                Path p = plusCourtChemin(g, courant.getId(), prochain.getId());
                if (p != null && !p.empty()) {
                    double duree = 0;
                    for (Edge e : p.getEdgePath()) {
                        duree += (double) e.getAttribute("duree");
                    }
                    if (duree < dureeMin) {
                        dureeMin = duree;
                        prochainCentre = prochain;
                        meilleurChemin = p;
                    }
                }
            }
            if (prochainCentre != null) {
                for (Edge e : meilleurChemin.getEdgePath()) {
                    tournee.add(e);
                }
                for (Node nTraverse : meilleurChemin.getNodePath()) {
                    aVisiter.remove(nTraverse);
                }
                courant = prochainCentre;
            }
        }
        return tournee;
    }

    /**
     * Construit une tournee gloutonne passant uniquement par les sommets d'un type donne.
     * Fonctionne comme {@link #tourneeLivraisonTotale(Graph)}, mais la liste des sommets
     * a visiter est restreinte par {@link #filtrageSommet(Graph, String)}.
     * @param g Graphe a parcourir (non null)
     * @param type Type de centre a livrer ("M", "O", "N") ou "Tous"
     * @return Le chemin de la tournee filtree (Path enracine sur le sommet de depart)
     */
    public static Path tourneeLivraisonFiltrage(Graph g, String type) {
        ArrayList<Node> aVisiter = filtrageSommet(g, type);
        Path tournee = new Path();
        Node courant = g.getNode(0);
        tournee.setRoot(courant);

        while (!aVisiter.isEmpty()) {
            Node prochainCentre = null;
            Path meilleurChemin = null;
            double dureeMin = Double.MAX_VALUE;
            for (Node prochain : aVisiter) {
                Path p = plusCourtChemin(g, courant.getId(), prochain.getId());
                if (p != null && !p.empty()) {
                    double duree = 0;
                    for (Edge e : p.getEdgePath()) {
                        duree += (double) e.getAttribute("duree");
                    }
                    if (duree < dureeMin) {
                        dureeMin = duree;
                        prochainCentre = prochain;
                        meilleurChemin = p;
                    }
                }
            }
            if (prochainCentre != null) {
                for (Edge e : meilleurChemin.getEdgePath()) {
                    tournee.add(e);
                }
                for (Node nTraverse : meilleurChemin.getNodePath()) {
                    aVisiter.remove(nTraverse);
                }

                courant = prochainCentre;
            }
        }
        return tournee;
    }

    /**
     * Calcule la duree totale d'un chemin en sommant l'attribut "duree" de ses aretes.
     * @param p Chemin a mesurer (peut etre null)
     * @return Somme des durees des aretes, 0 si le chemin est null ou vide
     */
    public static double dureeChemin(Path p) {
        double total = 0;
        if (p != null) {
            for (Edge e : p.getEdgePath()) {
                total += (double) e.getAttribute("duree");
            }
        }
        return total;
    }
    
    public static void exporterTourneeCSV(Path tournee, String nomFichier) {

        try (PrintWriter writer = new PrintWriter(new FileWriter(nomFichier))) {

            writer.println("type;element,source,cible,duree_cumulee");

            double cumul = 0;

            // Parcours des arêtes
            for (Edge e : tournee.getEdgePath()) {

                String source = e.getNode0().getId();
                String cible = e.getNode1().getId();
                double duree = (double) e.getAttribute("duree");

                cumul += duree;

                writer.println("ARETE," + e.getId() + "," + source + "->" + cible + "," + cumul);
            }

            // Optionnel : liste des noeuds visités
            writer.println();
            writer.println("NODES_VISITED");

            for (Node n : tournee.getNodePath()) {
                writer.println(n.getId());
            }

            System.out.println("CSV genere : " + nomFichier);

        } catch (IOException e) {
            System.err.println("Erreur export CSV : " + e.getMessage());
            e.printStackTrace();
        }
    }
}

