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

    /** Probabilite qu'un sommet tire au hasard soit une maternite (M). */
    private static final double PROBA_MATERNITE = 0.60;

    /** Probabilite qu'un sommet tire au hasard soit un bloc operatoire (O).
     *  La proba d'etre un centre de nutrition est 1 - PROBA_MATERNITE - PROBA_AUTRE. */
    private static final double PROBA_AUTRE = 0.20;

    /** Stylesheet commun pour la coloration des sommets et des aretes risquees. */
    private static final String STYLESHEET =
            "node.maternite { fill-color: red; size: 20px; } " +
            "node.bloc { fill-color: blue; size: 20px; } " +
            "node.centre { fill-color: green; size: 20px; } " +
            "edge.risquee { fill-color: red; size: 3px; } " +
            "edge { size: 1px; }";

    /**
     * Constructeur prive : classe utilitaire non instanciable.
     */

    /**
     * Initialise la fiabilite d'une route avec une valeur aleatoire dans [0.2 ; 1.0].
     * @param e Arete du graphe a valuer
     * @param r Generateur aleatoire reutilise (evite de creer un nouveau Random a chaque appel)
     */
    private static void setFiabiliteRoute(Edge e, Random r) {
        double fiabilite = 0.2 + r.nextDouble() * (1.0 - 0.2);
        e.setAttribute("fiabilite", fiabilite);
    }

    /**
     * Initialise la distance d'une route avec une valeur aleatoire dans [10 ; 50] km.
     * @param e Arete du graphe a valuer
     * @param r Generateur aleatoire reutilise
     */
    private static void setDistanceRoute(Edge e, Random r) {
        double distance = 10.0 + r.nextDouble() * (50.0 - 10.0);
        e.setAttribute("distance", distance);
    }

    /**
     * Initialise la duree d'une route avec une valeur aleatoire dans [10 ; 120] minutes.
     * @param e Arete du graphe a valuer
     * @param r Generateur aleatoire reutilise
     */
    private static void setDureeRoute(Edge e, Random r) {
        double duree = 10.0 + r.nextDouble() * (120.0 - 10.0);
        e.setAttribute("duree", duree);
    }

    /**
     * Construit l'etiquette ui.label d'une arete a partir de ses trois attributs.
     * Centralise le formatage pour eviter d'ecraser l'etiquette par inadvertance.
     * @param e Arete dont les attributs fiabilite, distance et duree sont deja positionnes
     */
    private static void majLabelArete(Edge e) {
        double fiabilite = (double) e.getAttribute("fiabilite");
        double distance  = (double) e.getAttribute("distance");
        double duree     = (double) e.getAttribute("duree");
        e.setAttribute("ui.label",
                "Fiab:" + String.format("%.2f", fiabilite)
                + " Dist:" + String.format("%.0f", distance) + "km"
                + " Tps:"  + String.format("%.0f", duree)    + "min");
    }

    /**
     * Genere un graphe aleatoire de nbSommets sommets.
     * Chaque sommet est tire au sort comme maternite (proba 0.60), bloc operatoire (0.20)
     * ou centre de nutrition (0.20). Une arete est ajoutee entre deux sommets avec
     * une probabilite egale a PROBA_AUTRE. Chaque arete recoit une fiabilite,
     * une distance et une duree aleatoires.
     * @param nbSommets Nombre de sommets souhaite pour la creation du graphe
     * @return carteDistrict : Un graphe de nbSommets avec ses routes initialisees
     */
    public static Graph construireGrapheAleatoire(int nbSommets) {
        Graph carteDistrict = new SingleGraph("carte");
        carteDistrict.setAttribute("ui.stylesheet", STYLESHEET);

        Random rand = new Random();

        // Compteurs LOCAUX : repartent a zero a chaque appel (corrige le bug
        // des compteurs static qui s'accumulaient entre deux generations).
        int nbMaternite = 0;
        int nbBloc = 0;
        int nbCentre = 0;
        int nbRoute = 0;

        for (int i = 0; i < nbSommets; i++) {
            double tirage = rand.nextDouble();
            Node node;
            if (tirage <= PROBA_MATERNITE) {
                nbMaternite++;
                node = carteDistrict.addNode("M" + nbMaternite);
                node.setAttribute("ui.class", "maternite");
                node.setAttribute("type", "maternite");
            } else if (tirage <= PROBA_MATERNITE + PROBA_AUTRE) {
                nbBloc++;
                node = carteDistrict.addNode("BO" + nbBloc);
                node.setAttribute("ui.class", "bloc");
                node.setAttribute("type", "bloc");
            } else {
                nbCentre++;
                node = carteDistrict.addNode("CN" + nbCentre);
                node.setAttribute("ui.class", "centre");
                node.setAttribute("type", "centre");
            }
            node.setAttribute("ui.label", node.getId());
        }

        for (int j = 0; j < carteDistrict.getNodeCount(); j++) {
            for (int k = j + 1; k < carteDistrict.getNodeCount(); k++) {
                if (rand.nextDouble() <= PROBA_AUTRE) {
                    nbRoute++;
                    Node nodeJ = carteDistrict.getNode(j);
                    Node nodeK = carteDistrict.getNode(k);
                    Edge e = carteDistrict.addEdge("Route" + nbRoute,
                            nodeJ.getId(), nodeK.getId());
                    setFiabiliteRoute(e, rand);
                    setDistanceRoute(e, rand);
                    setDureeRoute(e, rand);
                    majLabelArete(e);
                }
            }
        }
        return carteDistrict;
    }

    /**
     * Charge un graphe depuis un fichier CSV au format matrice d'adjacence triangulaire.
     * Chaque ligne represente un sommet et ses aretes vers les sommets precedents.
     * Les lignes commencant par "//" sont des commentaires et sont ignorees.
     * Une cellule a "0" indique l'absence d'arete, sinon elle contient "fiabilite,distance,duree".
     * @param cheminFichier Chemin absolu vers le fichier CSV a lire
     * @return carteDistrict : Graphe construit depuis le fichier CSV
     */
    public static Graph chargerGraphe(String cheminFichier) {
        Graph carteDistrict = new SingleGraph("carte");
        carteDistrict.setAttribute("ui.stylesheet", STYLESHEET);

        List<String> idsSommets = new ArrayList<>();

        try (BufferedReader lecteur = new BufferedReader(new FileReader(cheminFichier))) {
            String ligne;
            int numeroLigne = 0;

            while ((ligne = lecteur.readLine()) != null) {
                ligne = ligne.trim();
                if (ligne.isEmpty()) continue;
                if (ligne.startsWith("//")) continue;

                String[] colonnes = ligne.split(";", -1);
                String idSommet = colonnes[0].trim();
                String typeSommet = colonnes[1].trim();

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
                        double distance  = Double.parseDouble(attributs[1].trim());
                        double duree     = Double.parseDouble(attributs[2].trim());

                        String idArete = idSommet + "--" + idsSommets.get(j);
                        Edge arete = carteDistrict.addEdge(idArete, idSommet, idsSommets.get(j));
                        arete.setAttribute("fiabilite", fiabilite);
                        arete.setAttribute("distance", distance);
                        arete.setAttribute("duree", duree);
                        majLabelArete(arete);

                    } catch (NumberFormatException e) {
                        System.err.println("Valeur non parsable \"" + cellule
                                + "\" ligne " + idSommet + " col " + j + " : " + e.getMessage());
                    }
                }
                numeroLigne++;
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier : " + cheminFichier);
        }
        return carteDistrict;
    }

    /**
     * Compte le nombre de sommets d'un type donne dans le graphe.
     * Le type est lu depuis l'attribut "type" pose au moment de la construction.
     * @param graphe Graphe a analyser (ne doit pas etre null)
     * @param type Type recherche : "maternite", "bloc" ou "centre"
     * @return Nombre de sommets correspondant au type, 0 si aucun
     */
    public static int compterSommetsParType(Graph graphe, String type) {
        int compteur = 0;
        for (Node n : graphe) {
            Object t = n.getAttribute("type");
            if (t != null && t.equals(type)) {
                compteur++;
            }
        }
        return compteur;
    }

    /**
     * Met en evidence les routes risquees du graphe.
     * Une route est consideree risquee si sa fiabilite est strictement inferieure au seuil.
     * Les aretes risquees recoivent la ui.class "risquee" (affichage rouge epais),
     * les autres voient leur ui.class reinitialisee.
     * @param graphe Graphe a analyser
     * @param seuil  Seuil de fiabilite dans ]0 ; 1] : toute route en-dessous est risquee
     * @return Nombre de routes identifiees comme risquees
     */
    public static int marquerRoutesRisquees(Graph graphe, double seuil) {
        int nbRisquees = 0;
        for (Edge e : graphe.getEachEdge()) {
            Object f = e.getAttribute("fiabilite");
            if (f == null) continue;
            double fiabilite = (double) f;
            if (fiabilite < seuil) {
                e.setAttribute("ui.class", "risquee");
                nbRisquees++;
            } else {
                e.removeAttribute("ui.class");
            }
        }
        return nbRisquees;
    }
    
        /**
     * Met a jour l'affichage des labels du graphe selon les preferences utilisateur.
     * Les attributs metier (fiabilite, distance, duree, type) ne sont jamais modifies :
     * seuls les ui.label de chaque sommet et de chaque arete sont reconstruits.
     * Une arete sans aucune info cochee voit son ui.label vide ("").
     * @param graphe Graphe a mettre a jour (non null)
     * @param afficherFiabilite Si vrai, la fiabilite apparait sur chaque arete
     * @param afficherDistance  Si vrai, la distance apparait sur chaque arete
     * @param afficherDuree     Si vrai, la duree apparait sur chaque arete
     * @param afficherLabelsSommets Si vrai, les sommets affichent leur identifiant, sinon rien
     */
    public static void majAffichage(Graph graphe, boolean afficherFiabilite, boolean afficherDistance, boolean afficherDuree, boolean afficherLabelsSommets) {
        // Sommets : on affiche l'identifiant ou une chaine vide
        for (Node n : graphe) {
            if(afficherLabelsSommets == true ) {
                n.setAttribute("ui.label", n.getId());
            } else {
                n.setAttribute("ui.label", "");
            }
        } 
 
        // Aretes : on reconstruit le label en concatenant uniquement ce qui est coche
        for (Edge e : graphe.getEachEdge()) {
            String label = "";
            if (afficherFiabilite && e.hasAttribute("fiabilite")) {
                double f = (double) e.getAttribute("fiabilite");
                label = label + "Fiabilité :" + String.format("%.2f", f);
            }
            if (afficherDistance && e.hasAttribute("distance")) {
                if (!label.equals("")) label = label + " ";
                double d = (double) e.getAttribute("distance");
                label = label + "Distance :" + String.format("%.0f", d) + "km";
            }
            if (afficherDuree && e.hasAttribute("duree")) {
                if (!label.equals("")) label = label + " ";
                double t = (double) e.getAttribute("duree");
                label = label + "Temps :" + String.format("%.0f", t) + "min";
            }
            e.setAttribute("ui.label", label);
        }
    }
}