package modele;

import java.io.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import static modele.GenGraphe.plusCourtChemin;

/**
 * Algorithmes de tournee de vehicule pour la livraison des dispensaires (Question 6).
 * La tournee part toujours du sommet d'indice 0 du graphe (centre de depart).
 * L'approche utilisee est gloutonne : a chaque etape, on rejoint le centre non
 * encore visite le plus proche en duree, en empruntant le plus court chemin.
 * Toutes les methodes sont statiques.
 *
* @author Sinjat, Battah, Dias
 */

public class Algo {

/**
     * Selectionne les sommets a visiter en fonction d'un type recherche.
     * Le sommet de depart (sommet d'indice 0 du graphe) est toujours exclu de la liste.
     * Si le type vaut "Tous", tous les sommets sont retenus sauf le depart.
     * @param g Graphe a parcourir (non null)
     * @param typerechecher Type recherche ("M", "O", "N") ou "Tous" pour ne pas filtrer
     * @return Liste des sommets correspondant au filtre, vide si typerechecher est null
     */
    public static ArrayList<Node> filtrageSommet(Graph g, String typerechecher) {
        
    String idDepart = g.getNode(0).getId();

        ArrayList<Node> idpt = new ArrayList<>();
        if (g != null) {
            if (typerechecher != null) {
                for (Node n : g.getEachNode()) {

                    if (!n.getId().equals(idDepart)) {

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
    
    /**
     * Extrait l'ordre des centres reellement visites dans une tournee, sans doublon,
     * le sommet de depart (indice 0) place en premier. Un centre est considere visite
     * s'il apparait dans le chemin de la tournee.
     * @param g Graphe a parcourir (non null)
     * @param tournee Chemin de la tournee a analyser
     * @return Liste ordonnee des centres visites, depart en tete
     */
    public static ArrayList<Node> ordreCentresVisites(Graph g, Path tournee) {
        ArrayList<Node> ordre = new ArrayList<>();
        Node depart = g.getNode(0);
        ordre.add(depart);
        for (Node n : tournee.getNodePath()) {
            if (!ordre.contains(n)) {
                ordre.add(n);
            }
        }
        return ordre;
    }
    
    /**
     * Ajoute dans un fichier CSV le resultat d'evaluation d'un graphe pour le cas
     * "1 camion, tous les dispensaires". Le fichier respecte le format impose : une
     * ligne d'en-tete ecrite une seule fois, puis une ligne par graphe contenant le
     * nom du graphe, la duree de l'algorithme glouton, la meilleure duree trouvee,
     * puis les numeros des centres dans l'ordre de visite. Le fichier est complete a
     * chaque appel (mode ajout). L'extension ".csv" est ajoutee automatiquement.
     * En cas d'erreur d'ecriture, un message est affiche sur la sortie d'erreur et
     * aucune exception n'est propagee.
     * @param nomGraphe Nom du fichier graphe evalue (ex. "grapheEval1.csv")
     * @param dureeGlouton Duree de la tournee obtenue par l'algorithme glouton
     * @param meilleureDuree Meilleure duree obtenue (ex. apres 2-opt)
     * @param ordreVisite Liste des centres dans l'ordre de livraison de la meilleure tournee (chaque centre une fois, depart en premier)
     * @param nomFichier Nom du fichier de sortie sans extension (l'extension ".csv" est ajoutee)
     */
    public static void exporterResultat1Camion(String nomGraphe, double dureeGlouton,
            double meilleureDuree, List<Node> ordreVisite, String nomFichier) {

        File fichier = new File(nomFichier + ".csv");
        boolean fichierVide = (!fichier.exists()) || (fichier.length() == 0);

        try (PrintWriter writer = new PrintWriter(new FileWriter(fichier, true))) {

            if (fichierVide) {
                writer.println("nomFichier;dureeAlgoGlouton1camion;meilleureDuree1camion;ordreMeilleureDuree");
            }

            String ligne = nomGraphe + ";" + Math.round(dureeGlouton) + ";" + Math.round(meilleureDuree);

            for (Node n : ordreVisite) {
                int numero = (int) n.getAttribute("numeroSommet");
                ligne = ligne + ";" + numero;
            }

            writer.println(ligne);
            System.out.println("Resultat ajoute pour : " + nomGraphe);

        } catch (IOException e) {
            System.err.println("Erreur export CSV : " + e.getMessage());
        }
    }    
    
    /**
     * Ajoute dans un fichier CSV le resultat d'evaluation d'un graphe pour le cas
     * "2 camions". Le format respecte celui impose : une ligne d'en-tete ecrite une
     * seule fois, puis une ligne par graphe contenant le nom du graphe, la duree du
     * camion 1, la duree du camion 2, puis les centres du camion 1 puis ceux du camion 2
     * (chaque tournee commencant par le sommet de depart). Le fichier est complete a
     * chaque appel (mode ajout). En cas d'erreur d'ecriture, un message est affiche sur
     * la sortie d'erreur et aucune exception n'est propagee.
     * @param nomGraphe Nom du fichier graphe evalue (ex. "grapheEval1.csv")
     * @param dureeCamion1 Duree de la tournee du camion 1
     * @param dureeCamion2 Duree de la tournee du camion 2
     * @param ordreCamion1 Centres du camion 1 dans l'ordre de visite (depart en premier)
     * @param ordreCamion2 Centres du camion 2 dans l'ordre de visite (depart en premier)
     * @param nomFichier Nom du fichier de sortie sans extension (l'extension ".csv" est ajoutee)
     */
    public static void exporterResultat2Camions(String nomGraphe, double dureeCamion1, double dureeCamion2, List<Node> ordreCamion1, List<Node> ordreCamion2, String nomFichier) {

        File fichier = new File(nomFichier + ".csv");
        boolean fichierVide = (!fichier.exists()) || (fichier.length() == 0);

        try (PrintWriter writer = new PrintWriter(new FileWriter(fichier, true))) {

            if (fichierVide) {
                writer.println("nomFichier;DureeCamion1;DureeCamion2;‎ordreDureeCamion1Camion2");
            }

            String ligne = nomGraphe + ";" + Math.round(dureeCamion1) + ";" + Math.round(dureeCamion2);

            for (Node n : ordreCamion1) {
                int numero = (int) n.getAttribute("numeroSommet");
                ligne = ligne + ";" + numero;
            }
            for (Node n : ordreCamion2) {
                int numero = (int) n.getAttribute("numeroSommet");
                ligne = ligne + ";" + numero;
            }

            writer.println(ligne);
            System.out.println("Resultat 2 camions ajoute pour : " + nomGraphe);

        } catch (IOException e) {
            System.err.println("Erreur export CSV : " + e.getMessage());
        }
    }
    
    /**
     * Construit une tournee gloutonne passant par une liste de sommets donnee.
     * Le camion part du sommet d'indice 0, puis rejoint a chaque etape le sommet
     * de la liste non encore visite atteignable dans la plus petite duree.
     * Cette methode est le moteur commun reutilise par la tournee a deux camions.
     * @param g Graphe a parcourir (non null)
     * @param aLivrer Liste des sommets a livrer par ce camion (ne contient pas le depart)
     * @return Le chemin de la tournee (Path enracine sur le sommet de depart)
     */
    private static Path tourneeSurListe(Graph g, ArrayList<Node> aLivrer) {
        ArrayList<Node> aVisiter = new ArrayList<>();
        for (int i = 0; i < aLivrer.size(); i++) {
            aVisiter.add(aLivrer.get(i));
        }

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
                    double duree = dureeChemin(p);
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
     * Trie une liste de sommets par duree de plus court chemin depuis le depart,
     * du plus proche au plus loin, par un tri a bulles.
     * @param g Graphe a parcourir (non null)
     * @param aTrier Liste des sommets a trier
     * @return Une nouvelle liste contenant les memes sommets, tries par duree croissante
     */
    private static ArrayList<Node> trierParDureeDepart(Graph g, ArrayList<Node> aTrier) {
        Node depart = g.getNode(0);

        ArrayList<Node> tries = new ArrayList<>();
        ArrayList<Double> durees = new ArrayList<>();
        for (int i = 0; i < aTrier.size(); i++) {
            Node n = aTrier.get(i);
            Path p = plusCourtChemin(g, depart.getId(), n.getId());
            tries.add(n);
            durees.add(dureeChemin(p));
        }

        for (int i = 0; i < tries.size(); i++) {
            for (int j = 0; j < tries.size() - 1 - i; j++) {
                if (durees.get(j) > durees.get(j + 1)) {
                    double tmpD = durees.get(j);
                    durees.set(j, durees.get(j + 1));
                    durees.set(j + 1, tmpD);

                    Node tmpN = tries.get(j);
                    tries.set(j, tries.get(j + 1));
                    tries.set(j + 1, tmpN);
                }
            }
        }
        return tries;
    }

    /**
     * Construit la tournee de deux camions partant tous deux du sommet de depart (indice 0).
     * Les dispensaires sont d'abord tries par duree depuis le depart, puis repartis en
     * deux groupes par distribution alternee. Une phase d'amelioration tente ensuite de
     * deplacer chaque dispensaire vers l'autre camion et conserve le changement seulement
     * s'il diminue la duree du camion le plus lent.
     * @param g Graphe a parcourir (non null)
     * @param type Type de centre a livrer ("M", "O", "N") ou "Tous"
     * @return Un tableau de deux Path : la tournee du camion 1 et celle du camion 2
     */
    public static Path[] tournee2Camions(Graph g, String type) {
        // Etape 1 : recuperer puis trier les dispensaires a livrer
        ArrayList<Node> aLivrer = filtrageSommet(g, type);
        ArrayList<Node> tries = trierParDureeDepart(g, aLivrer);

        // Etape 2 : distribution alternee en deux groupes
        ArrayList<Node> groupe1 = new ArrayList<>();
        ArrayList<Node> groupe2 = new ArrayList<>();
        for (int i = 0; i < tries.size(); i++) {
            if (i % 2 == 0) {
                groupe1.add(tries.get(i));
            } else {
                groupe2.add(tries.get(i));
            }
        }

        // Etape 3 : duree initiale des deux tournees
        double meilleurMax = Math.max(
                dureeChemin(tourneeSurListe(g, groupe1)),
                dureeChemin(tourneeSurListe(g, groupe2)));

        // Etape 4 : amelioration par deplacement d'un dispensaire vers l'autre groupe
        boolean amelioration = true;
        while (amelioration) {
            amelioration = false;

            // Deplacement d'un dispensaire de groupe1 vers groupe2
            for (int i = 0; i < groupe1.size(); i++) {
                Node centre = groupe1.get(i);
                groupe1.remove(i);
                groupe2.add(centre);

                double nouveauMax = Math.max(
                        dureeChemin(tourneeSurListe(g, groupe1)),
                        dureeChemin(tourneeSurListe(g, groupe2)));

                if (nouveauMax < meilleurMax) {
                    meilleurMax = nouveauMax;
                    amelioration = true;
                    i = -1;
                } else {
                    groupe2.remove(groupe2.size() - 1);
                    groupe1.add(i, centre);
                }
            }

            // Deplacement d'un dispensaire de groupe2 vers groupe1
            for (int i = 0; i < groupe2.size(); i++) {
                Node centre = groupe2.get(i);
                groupe2.remove(i);
                groupe1.add(centre);

                double nouveauMax = Math.max(
                        dureeChemin(tourneeSurListe(g, groupe1)),
                        dureeChemin(tourneeSurListe(g, groupe2)));

                if (nouveauMax < meilleurMax) {
                    meilleurMax = nouveauMax;
                    amelioration = true;
                    i = -1;
                } else {
                    groupe1.remove(groupe1.size() - 1);
                    groupe2.add(i, centre);
                }
            }
        }

        // Etape 5 : tournees finales
        Path[] resultat = new Path[2];
        resultat[0] = tourneeSurListe(g, groupe1);
        resultat[1] = tourneeSurListe(g, groupe2);
        return resultat;
    }
    }


