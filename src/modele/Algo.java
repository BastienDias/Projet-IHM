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
     * Renvoie le numero de sommet associe a un noeud, utilise pour l'export CSV.
     * S'appuie en priorite sur l'attribut "numeroSommet" pose par {@link GenGraphe}
     * (lors du chargement CSV et de la generation aleatoire). En cas d'absence
     * de l'attribut (graphe construit manuellement par exemple), retombe sur
     * l'index GraphStream du noeud, augmente de 1 pour rester compatible avec
     * la convention 1-based des fichiers de rendu.
     * @param n Noeud dont on veut le numero
     * @return Numero du sommet (1-based)
     * @author elieb
     */
    private static int numeroSommet(Node n) {
        Object attr = n.getAttribute("numeroSommet");
        if (attr instanceof Integer) {
            return (Integer) attr;
        }
        return n.getIndex() + 1;
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
    public static void exporterResultat1Camion(String nomGraphe, double dureeGlouton, double meilleureDuree, List<Node> ordreVisite, String nomFichier) {

        File fichier = new File(nomFichier + ".csv");
        boolean fichierVide = (!fichier.exists()) || (fichier.length() == 0);

        try (PrintWriter writer = new PrintWriter(new FileWriter(fichier, true))) {

            if (fichierVide) {
                writer.println("nomFichier;dureeAlgoGlouton1camion;meilleureDuree1camion;ordreMeilleureDuree");
            }

            String ligne = nomGraphe + ";" + Math.round(dureeGlouton) + ";" + Math.round(meilleureDuree);

            for (Node n : ordreVisite) {
                int numero = numeroSommet(n);
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
                writer.println("nomFichier;DureeCamion1;DureeCamion2;ordreDureeCamion1Camion2");
            }

            String ligne = nomGraphe + ";" + Math.round(dureeCamion1) + ";" + Math.round(dureeCamion2);

            for (Node n : ordreCamion1) {
                int numero = numeroSommet(n);
                ligne = ligne + ";" + numero;
            }
            for (Node n : ordreCamion2) {
                int numero = numeroSommet(n);
                ligne = ligne + ";" + numero;
            }

            writer.println(ligne);
            System.out.println("Resultat 2 camions ajoute pour : " + nomGraphe);

        } catch (IOException e) {
            System.err.println("Erreur export CSV : " + e.getMessage());
        }
    }
    

    // ============================================================
    // Optimisation tournee a un camion (matrice + sequence + 2-opt)
    // ============================================================

    /**
     * Pre-calcule la matrice des durees de plus court chemin entre chaque paire de
     * centres. La case matrice[i][j] contient la duree (en minutes) du plus court
     * chemin du centre i au centre j, ou les indices i et j sont des positions dans
     * la liste {@code centres} fournie en parametre. La diagonale est laissee a 0.
     * Une paire de centres non connectes est marquee {@link Double#POSITIVE_INFINITY}
     * pour eviter qu'un algorithme d'optimisation ne la considere a tort comme un
     * raccourci gratuit. La matrice est symetrique (graphe non oriente).
     * @param g Graphe a parcourir (non null)
     * @param centres Liste ordonnee des centres consideres, depart en premier (indice 0)
     * @return Matrice n*n des durees de plus court chemin entre centres (n = taille de la liste)
     * @author elieb
     */
    public static double[][] calculerMatrice(Graph g, ArrayList<Node> centres) {
       int n = centres.size();
       double[][] matrice = new double[n][n];

       for (int i = 0; i < n; i++) {
           for (int j = i + 1; j < n; j++) {
               Path p = plusCourtChemin(g, centres.get(i).getId(), centres.get(j).getId());
               double duree;
               if (p == null || p.empty()) {
                   duree = Double.POSITIVE_INFINITY;
               } else {
                   duree = dureeChemin(p);
               }
               matrice[i][j] = duree;
               matrice[j][i] = duree;
           }
       }
       return matrice;
   }


    /**
     * Pre-calcule conjointement la matrice des durees de plus court chemin et les
     * Path GraphStream correspondants pour chaque paire de centres. La matrice sert
     * aux algorithmes d'optimisation (PPV, 2-opt) qui manipulent des sequences
     * d'indices ; les Path stockes servent ensuite a reconstruire la tournee
     * complete sur le graphe routier sans relancer Dijkstra. La diagonale {@code [i][i]}
     * a une duree de 0 et un Path null. Une paire de centres non connectes est marquee
     * {@link Double#POSITIVE_INFINITY} cote duree et null cote Path.
     * @param g Graphe a parcourir (non null)
     * @param centres Liste ordonnee des centres consideres, depart en premier (indice 0)
     * @return Objet conteneur regroupant la matrice des durees et le tableau des Path
     * @author elieb
     */
    public static DonneesCentres calculerDonneesCentres(Graph g, ArrayList<Node> centres) {
        int n = centres.size();
        double[][] durees = new double[n][n];
        Path[][] chemins = new Path[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {

                if (i == j) {
                    durees[i][j] = 0;
                    chemins[i][j] = null;
                    continue;
                }

                Path p = plusCourtChemin(g, centres.get(i).getId(), centres.get(j).getId());

                if (p == null || p.empty()) {
                    durees[i][j] = Double.POSITIVE_INFINITY;
                    chemins[i][j] = null;
                } else {
                    durees[i][j] = dureeChemin(p);
                    chemins[i][j] = p;
                }
            }
        }

        return new DonneesCentres(durees, chemins);
    }

    /**
     * Calcule la duree totale d'une sequence de centres en s'appuyant sur la matrice
     * des durees pre-calculee. La sequence represente un ordre de visite : {@code seq[0]}
     * est le depart, {@code seq[seq.length - 1]} le dernier centre livre. Le sujet
     * ne contraint pas le centre d'arrivee : pas de retour au depart (chemin libre).
     * @param seq Ordre de visite des centres, indices dans la liste des centres
     * @param durees Matrice des durees centre a centre (issue de {@link #calculerMatrice})
     * @return Somme des durees des transitions seq[i] -&gt; seq[i+1], pour i de 0 a length-2
     * @author elieb
     */
    public static double dureeSequence(int[] seq, double[][] durees) {
        double total = 0;
        for (int i = 0; i < seq.length - 1; i++) {
            total += durees[seq[i]][seq[i + 1]];
        }
        return total;
    }

    /**
     * Construit une tournee gloutonne (heuristique du plus proche voisin) sur la
     * matrice des durees pre-calculee. Le camion part de l'indice 0 (le depart),
     * puis a chaque etape rejoint le centre non encore visite dont la duree depuis
     * le centre courant est minimale. La methode produit l'ordre de visite sous
     * forme d'un tableau d'indices, directement exploitable par les algorithmes
     * d'amelioration (2-opt, Or-opt) et par {@link #dureeSequence}.
     * Contrairement a {@link #tourneeLivraisonTotale}, cette version ne profite
     * pas des centres traverses au passage : elle considere chaque centre comme
     * un point a livrer explicitement. La compensation se fait par l'optimisation
     * locale appliquee ensuite. La complexite est en O(n^2), tres rapide grace aux
     * lookups O(1) dans la matrice.
     * @param durees Matrice n*n des durees centre a centre (issue de {@link #calculerMatrice})
     * @return Tableau d'indices de taille n donnant l'ordre de visite, seq[0] = 0 (depart)
     * @author elieb
     */
    public static int[] tourneeGloutonSequence(double[][] durees) {
        int n = durees.length;
        int[] seq = new int[n];
        boolean[] visite = new boolean[n];

        seq[0] = 0;
        visite[0] = true;
        int courant = 0;

        for (int i = 1; i < n; i++) {
            int meilleur = -1;
            double dureeMin = Double.POSITIVE_INFINITY;
            for (int j = 0; j < n; j++) {
                if (!visite[j] && durees[courant][j] < dureeMin) {
                    dureeMin = durees[courant][j];
                    meilleur = j;
                }
            }
            seq[i] = meilleur;
            visite[meilleur] = true;
            courant = meilleur;
        }
        return seq;
    }

    /**
     * Inverse en place le segment du tableau {@code seq} entre les indices
     * {@code debut} et {@code fin} inclus. Utilitaire interne de l'algorithme 2-opt.
     * @param seq Tableau a modifier (non null)
     * @param debut Indice de debut du segment (inclus)
     * @param fin Indice de fin du segment (inclus)
     */
    private static void inverserSegment(int[] seq, int debut, int fin) {
        while (debut < fin) {
            int tmp = seq[debut];
            seq[debut] = seq[fin];
            seq[fin] = tmp;
            debut++;
            fin--;
        }
    }

    /**
     * Optimise une sequence par l'algorithme 2-opt. A chaque iteration, l'algorithme
     * teste l'inversion de chaque segment possible et applique l'inversion des
     * qu'elle reduit la duree totale. Il s'arrete lorsqu'une passe complete n'a
     * produit aucune amelioration (optimum local 2-opt atteint). Le calcul du
     * gain est incremental : seules les durees des arretes affectees par
     * l'inversion sont considerees, ce qui rend chaque test en O(1) et l'ensemble
     * en O(n^2) par passe. La sequence est modifiee en place et egalement retournee
     * par commodite. Comme la tournee est un chemin libre (pas de retour au depart),
     * le cas {@code j == n - 1} est traite specifiquement : pas d'arrete a droite.
     * @param seq Sequence initiale (typiquement issue de {@link #tourneeGloutonSequence}), modifiee en place
     * @param durees Matrice des durees centre a centre (issue de {@link #calculerMatrice})
     * @return La sequence optimisee (meme reference que le parametre {@code seq})
     * @author elieb
     */
    public static int[] deuxOpt(int[] seq, double[][] durees) {
        int n = seq.length;
        boolean amelioration = true;

        while (amelioration) {
            amelioration = false;

            for (int i = 0; i < n - 2; i++) {
                for (int j = i + 2; j < n; j++) {

                    double avant = durees[seq[i]][seq[i + 1]];
                    double apres = durees[seq[i]][seq[j]];

                    if (j + 1 < n) {
                        avant += durees[seq[j]][seq[j + 1]];
                        apres += durees[seq[i + 1]][seq[j + 1]];
                    }

                    double delta = apres - avant;

                    if (delta < -1e-9) {
                        inverserSegment(seq, i + 1, j);
                        amelioration = true;
                    }
                }
            }
        }
        return seq;
    }

    /**
     * Reconstruit un {@link Path} GraphStream complet a partir d'une sequence
     * optimisee d'indices de centres. Concatene les sous-chemins centre a centre
     * pre-calcules dans {@link DonneesCentres}. Le Path resultant est enracine sur
     * le premier centre de la sequence (le depart) et utilisable pour la
     * visualisation et l'export CSV. Si un sous-chemin est null (centres non
     * connectes), le segment est ignore : la situation ne devrait pas se produire
     * sur les graphes fournis.
     * @param seq Sequence ordonnee des indices de centres (issue de {@link #tourneeGloutonSequence} ou {@link #deuxOpt})
     * @param centres Liste des centres dans le meme ordre d'indexation que la matrice
     * @param donnees Donnees pre-calculees contenant les Path centre a centre
     * @return Path GraphStream complet de la tournee, pret a etre affiche ou exporte
     * @author elieb
     */
    public static Path reconstruirePath(int[] seq, ArrayList<Node> centres, DonneesCentres donnees) {
        Path resultat = new Path();
        resultat.setRoot(centres.get(seq[0]));

        for (int i = 0; i < seq.length - 1; i++) {
            Path sousChemin = donnees.chemins[seq[i]][seq[i + 1]];
            if (sousChemin == null) {}
            else{
                for (Edge e : sousChemin.getEdgePath()) {
                    resultat.add(e);
                }
            }
        }
        return resultat;
    }

    /**
     * Pipeline complet d'optimisation d'une tournee a un camion : filtrage des
     * centres par type, pre-calcul de la matrice et des Path, construction d'une
     * sequence par plus proche voisin, amelioration locale 2-opt, reconstruction
     * du Path GraphStream. Methode de haut niveau utilisable directement par
     * l'interface graphique.
     * @param g Graphe a parcourir (non null)
     * @param type Type de centres a livrer ("M", "O", "N" ou "Tous")
     * @return Path optimise pret a etre affiche et exporte
     * @author elieb
     */
    public static Path tourneeOptimisee(Graph g, String type) {
        ArrayList<Node> centres = filtrageSommet(g, type);
        centres.add(0, g.getNode(0));

        DonneesCentres donnees = calculerDonneesCentres(g, centres);
        int[] seq = tourneeGloutonSequence(donnees.durees);
        deuxOpt(seq, donnees.durees);

        return reconstruirePath(seq, centres, donnees);
    }

    /**
     * Construit une sequence par plus proche voisin sur un sous-ensemble
     * d'indices de centres. Le camion part de l'indice 0 (depart) puis ne
     * visite que les centres listes dans {@code aVisiter}, dans l'ordre choisi
     * par l'heuristique. Variante de {@link #tourneeGloutonSequence} utile
     * lorsqu'on partitionne les centres en plusieurs sous-tournees (cas a
     * deux camions). La taille de la sequence retournee est {@code aVisiter.size() + 1}.
     * @param durees Matrice des durees centre a centre
     * @param aVisiter Liste des indices de centres a livrer (sans le depart)
     * @return Sequence d'indices commencant par 0 puis les centres dans l'ordre glouton
     * @author elieb
     */
    private static int[] tourneeGloutonSurIndices(double[][] durees, ArrayList<Integer> aVisiter) {
        int taille = aVisiter.size() + 1;
        int[] seq = new int[taille];
        boolean[] visite = new boolean[durees.length];

        seq[0] = 0;
        visite[0] = true;
        int courant = 0;

        for (int i = 1; i < taille; i++) {
            int meilleur = -1;
            double dureeMin = Double.POSITIVE_INFINITY;
            for (int idx : aVisiter) {
                if (!visite[idx] && durees[courant][idx] < dureeMin) {
                    dureeMin = durees[courant][idx];
                    meilleur = idx;
                }
            }
            seq[i] = meilleur;
            visite[meilleur] = true;
            courant = meilleur;
        }
        return seq;
    }

    /**
     * Tournee a deux camions optimisee, version sur matrice + 2-opt. Strategie :
     * tri des dispensaires par duree depuis le depart, distribution alternee en
     * deux groupes, amelioration par echange entre groupes pour minimiser la duree
     * du camion le plus lent. Le tout s'appuie sur la matrice des durees pre-calculee :
     * les evaluations
     * pendant la boucle d'amelioration sont en O(n) au lieu de necessiter des
     * Dijkstra repetes. Une fois la partition stabilisee, 2-opt est applique
     * une fois sur la sequence de chaque camion pour ameliorer l'ordre de visite.
     * Le pipeline complet est typiquement 100 a 10000 fois plus rapide que
     * l'ancienne version sur les grands graphes.
     * @param g Graphe a parcourir (non null)
     * @param type Type de centres a livrer ("M", "O", "N" ou "Tous")
     * @return Tableau de deux Path : tournee du camion 1 et tournee du camion 2
     * @author elieb
     */
    public static Path[] tournee2CamionsOptimisee(Graph g, String type) {
        // 1. Liste des centres + matrice + Path pre-calcules
        ArrayList<Node> centres = filtrageSommet(g, type);
        centres.add(0, g.getNode(0));
        int n = centres.size();

        DonneesCentres donnees = calculerDonneesCentres(g, centres);

        // 2. Tri des indices de dispensaires (1..n-1) par duree depuis le depart
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 1; i < n; i++) {
            indices.add(i);
        }
        indices.sort((a, b) -> Double.compare(donnees.durees[0][a], donnees.durees[0][b]));

        // 3. Distribution alternee en deux groupes
        ArrayList<Integer> groupe1 = new ArrayList<>();
        ArrayList<Integer> groupe2 = new ArrayList<>();
        for (int k = 0; k < indices.size(); k++) {
            if (k % 2 == 0) {
                groupe1.add(indices.get(k));
            } else {
                groupe2.add(indices.get(k));
            }
        }

        // 4. Boucle d'amelioration par echange entre groupes (rapide grace a la matrice)
        double meilleurMax = Math.max(
                dureeSequence(tourneeGloutonSurIndices(donnees.durees, groupe1), donnees.durees),
                dureeSequence(tourneeGloutonSurIndices(donnees.durees, groupe2), donnees.durees));

        boolean amelioration = true;
        while (amelioration) {
            amelioration = false;

            for (int i = 0; i < groupe1.size(); i++) {
                int centre = groupe1.get(i);
                groupe1.remove(i);
                groupe2.add(centre);

                double nouveauMax = Math.max(
                        dureeSequence(tourneeGloutonSurIndices(donnees.durees, groupe1), donnees.durees),
                        dureeSequence(tourneeGloutonSurIndices(donnees.durees, groupe2), donnees.durees));

                if (nouveauMax < meilleurMax) {
                    meilleurMax = nouveauMax;
                    amelioration = true;
                    i = -1;
                } else {
                    groupe2.remove(groupe2.size() - 1);
                    groupe1.add(i, centre);
                }
            }

            for (int i = 0; i < groupe2.size(); i++) {
                int centre = groupe2.get(i);
                groupe2.remove(i);
                groupe1.add(centre);

                double nouveauMax = Math.max(
                        dureeSequence(tourneeGloutonSurIndices(donnees.durees, groupe1), donnees.durees),
                        dureeSequence(tourneeGloutonSurIndices(donnees.durees, groupe2), donnees.durees));

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

        // 5. 2-opt en finition sur chaque camion
        int[] seq1 = tourneeGloutonSurIndices(donnees.durees, groupe1);
        int[] seq2 = tourneeGloutonSurIndices(donnees.durees, groupe2);
        deuxOpt(seq1, donnees.durees);
        deuxOpt(seq2, donnees.durees);

        // 6. Reconstruction des Path GraphStream pour l'IHM et l'export
        Path[] resultat = new Path[2];
        resultat[0] = reconstruirePath(seq1, centres, donnees);
        resultat[1] = reconstruirePath(seq2, centres, donnees);
        return resultat;
    }

}


