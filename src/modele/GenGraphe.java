package modele;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.SingleGraph;
import exception.FormatCsvException;

/**
 * Classe utilitaire de generation et de manipulation du graphe de district Medic'In.
 * Elle regroupe la creation d'un graphe aleatoire, le chargement d'un graphe
 * depuis un fichier CSV, ainsi que des operations d'analyse et d'affichage
 * (comptage par type, marquage des routes risquees, calculs de plus court chemin).
 * Toutes les methodes sont statiques : la classe n'est pas destinee a etre instanciee.
 *
 * <p>Conventions utilisees dans tout le graphe :</p>
 * <ul>
 *   <li>chaque sommet porte un attribut "type" valant "M", "O" ou "N" ;</li>
 *   <li>chaque arete porte les attributs "fiabilite" (0.2 a 1.0), "distance" (km)
 *       et "duree" (min).</li>
 * </ul>
 *
* @author Sinjat, Battah, Dias
 */
public class GenGraphe {

    /** Probabilite qu'un sommet tire au hasard soit une maternite (M). */
    private static final double PROBA_MATERNITE = 0.60;

    /**
     * Probabilite qu'un sommet tire au hasard soit un bloc operatoire (O).
     * Cette meme valeur sert aussi de probabilite d'apparition d'une route.
     * La probabilite d'etre un centre de nutrition (N) vaut
     * 1 - PROBA_MATERNITE - PROBA_AUTRE.
     */
    private static final double PROBA_AUTRE = 0.20;

    /** Feuille de style GraphStream commune : couleurs des sommets par type et aspect des aretes. */
    private static final String STYLESHEET =
            "node.M { fill-color: red; size: 20px; } " +
            "node.O { fill-color: blue; size: 20px; } " +
            "node.N { fill-color: yellow; size: 20px; } " +
            "edge.risquee { fill-color: red; size: 3px; } " +
            "edge { fill-color: black; size: 1px; } " ;

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
     * ou centre de nutrition (0.20). Le graphe est d'abord rendu connexe en reliant
     * chaque nouveau sommet a un sommet deja place, puis des aretes supplementaires
     * sont ajoutees entre deux sommets quelconques avec une probabilite de PROBA_AUTRE.
     * Chaque arete recoit une fiabilite, une distance et une duree aleatoires.
     * @param nbSommets Nombre de sommets souhaite pour la creation du graphe
     * @return carteDistrict : un graphe de nbSommets sommets avec ses routes initialisees
     */
    public static Graph construireGrapheAleatoire(int nbSommets) {
        Graph carteDistrict = new SingleGraph("carte");
        carteDistrict.setAttribute("ui.stylesheet", STYLESHEET);
        Random rand = new Random();
        int nbMaternite = 0, nbBloc = 0, nbCentre = 0, nbRoute = 0;

        for (int i = 0; i < nbSommets; i++) {
            double tirage = rand.nextDouble();
            Node node;
            if (tirage <= PROBA_MATERNITE) {
                nbMaternite++;
                node = carteDistrict.addNode("M" + nbMaternite);
                node.setAttribute("ui.class", "M");
                node.setAttribute("type", "M");
            } else if (tirage <= PROBA_MATERNITE + PROBA_AUTRE) {
                nbBloc++;
                node = carteDistrict.addNode("O" + nbBloc);
                node.setAttribute("ui.class", "O");
                node.setAttribute("type", "O");
            } else {
                nbCentre++;
                node = carteDistrict.addNode("N" + nbCentre);
                node.setAttribute("ui.class", "N");
                node.setAttribute("type", "N");
            }
            node.setAttribute("ui.label", node.getId());
            node.setAttribute("numeroSommet", i + 1);
        }

        // Relie chaque nouveau sommet a un sommet deja place : garantit un graphe connexe
        List<Node> nodesAjoutes = new ArrayList<>();
        nodesAjoutes.add(carteDistrict.getNode(0));
        for (int i = 1; i < carteDistrict.getNodeCount(); i++) {
            Node nodeI = carteDistrict.getNode(i);
            Node nodeAleatoire = nodesAjoutes.get(rand.nextInt(nodesAjoutes.size()));
            nbRoute++;
            Edge e = carteDistrict.addEdge("Route" + nbRoute, nodeI.getId(), nodeAleatoire.getId());
            setFiabiliteRoute(e, rand);
            setDistanceRoute(e, rand);
            setDureeRoute(e, rand);
            majLabelArete(e);
            nodesAjoutes.add(nodeI);
        }

        // Ajoute des routes supplementaires entre sommets non encore relies (proba 1/5)
        for (int j = 0; j < carteDistrict.getNodeCount(); j++) {
            for (int k = j + 1; k < carteDistrict.getNodeCount(); k++) {
                Node nodeJ = carteDistrict.getNode(j);
                Node nodeK = carteDistrict.getNode(k);
                if (!nodeJ.hasEdgeBetween(nodeK) && rand.nextDouble() <= PROBA_AUTRE) {
                    nbRoute++;
                    Edge e = carteDistrict.addEdge("Route" + nbRoute, nodeJ.getId(), nodeK.getId());
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
     * Charge un graphe depuis un fichier CSV au format matrice d'adjacence.
     * Chaque ligne represente un sommet : id;type;cellules.
     * L'identifiant du fichier (S1, S2, ...) est ignore pour l'affichage : chaque
     * sommet est renomme selon son type et un compteur (M1, M2, O1, N1, ...),
     * a la maniere d'un graphe genere aleatoirement.
     * Une cellule vaut "0" (pas de route) ou "fiabilite,distance,duree"
     * (fiabilite entier sur 10, convertie en 0.0-1.0).
     * Le fichier entier est rejete des la premiere anomalie de format.
     * @param cheminFichier Chemin du fichier CSV a lire
     * @return Le graphe construit depuis le fichier
     * @throws FormatCsvException si une ligne ou une cellule est mal formee
     * @throws IOException si le fichier est introuvable ou illisible
     */
    public static Graph chargerGraphe(String cheminFichier) throws FormatCsvException, IOException {
        Graph carteDistrict = new SingleGraph("carte");
        carteDistrict.setAttribute("ui.stylesheet", STYLESHEET);

        // Memorise le nouvel identifiant (M1, O1, ...) de chaque sommet, dans l'ordre du fichier
        List<String> idsSommets = new ArrayList<>();
        int nbMaternite = 0;
        int nbBloc = 0;
        int nbCentre = 0;

        try (BufferedReader lecteur = new BufferedReader(new FileReader(cheminFichier))) {
            String ligne;
            int numeroLigne = 0;
            int numLigneFichier = 0;

            while ((ligne = lecteur.readLine()) != null) {
                numLigneFichier++;
                ligne = ligne.trim();
                if (ligne.isEmpty()) continue;
                if (ligne.startsWith("//")) continue;

                String[] colonnes = ligne.split(";", -1);

                if (colonnes.length < 2) {
                    throw new FormatCsvException("Ligne " + numLigneFichier + " : il faut au moins un identifiant et un type (id;type;...).");
                }

                String typeSommet = colonnes[1].trim();

                // Construction du nouvel identifiant selon le type
                String nouvelId;
                switch (typeSommet) {
                    case "M":
                        nbMaternite++;
                        nouvelId = "M" + nbMaternite;
                        break;
                    case "O":
                        nbBloc++;
                        nouvelId = "O" + nbBloc;
                        break;
                    case "N":
                        nbCentre++;
                        nouvelId = "N" + nbCentre;
                        break;
                    default:
                        throw new FormatCsvException("Ligne " + numLigneFichier + " : type de sommet inconnu (" + typeSommet + "). Attendu : M, O ou N.");
                }

                Node noeud = carteDistrict.addNode(nouvelId);
                noeud.setAttribute("ui.class", typeSommet);
                noeud.setAttribute("type", typeSommet);
                noeud.setAttribute("ui.label", nouvelId);
                noeud.setAttribute("numeroSommet", numeroLigne + 1); // S1 -> 1, S2 -> 2, ... (ordre du fichier)
                
                idsSommets.add(nouvelId);

                for (int j = 0; j < numeroLigne; j++) {
                    int indexColonne = 2 + j;
                    if (indexColonne >= colonnes.length) continue;
                    String cellule = colonnes[indexColonne].trim();
                    if (cellule.isEmpty() || cellule.equals("0")) continue;
                    
                    String[] attributs = cellule.split(",");
                    if (attributs.length != 3) {
                        throw new FormatCsvException("Ligne " + numLigneFichier + ", colonne " + (indexColonne + 1) + " : cellule mal formee (\"" + cellule + "\"). Attendu : fiabilite,distance,duree.");
                    }

                    try {
                        double fiabilite = Integer.parseInt(attributs[0].trim()) / 10.0;
                        double distance = Double.parseDouble(attributs[1].trim());
                        double duree = Double.parseDouble(attributs[2].trim());

                        String idArete = nouvelId + "--" + idsSommets.get(j);
                        Edge arete = carteDistrict.addEdge(idArete, nouvelId, idsSommets.get(j));
                        arete.setAttribute("fiabilite", fiabilite);
                        arete.setAttribute("distance", distance);
                        arete.setAttribute("duree", duree);
                        majLabelArete(arete);

                    } catch (NumberFormatException e) {
                        throw new FormatCsvException("Ligne " + numLigneFichier + ", colonne " + (indexColonne + 1) + " : valeur numerique invalide (\"" + cellule + "\").");
                    }
                }
                numeroLigne++;
            }

            if (carteDistrict.getNodeCount() == 0) {
                throw new FormatCsvException("Le fichier ne contient aucun sommet valide.");
            }
        }
        return carteDistrict;
    }
    
    /**
     * Compte le nombre de sommets d'un type donne dans le graphe.
     * Le type est lu depuis l'attribut "type" pose a la construction du graphe.
     * @param graphe Graphe a analyser (ne doit pas etre null)
     * @param type Type recherche : "M" (maternite), "O" (bloc operatoire) ou "N" (centre de nutrition)
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
     * les autres voient leur ui.class retiree.
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
     * Une arete dont aucune info n'est cochee voit son ui.label vide ("").
     * @param graphe Graphe a mettre a jour (non null)
     * @param afficherFiabilite Si vrai, la fiabilite apparait sur chaque arete
     * @param afficherDistance  Si vrai, la distance apparait sur chaque arete
     * @param afficherDuree     Si vrai, la duree apparait sur chaque arete
     * @param afficherLabelsSommets Si vrai, les sommets affichent leur identifiant, sinon rien
     */
    public static void majAffichage(Graph graphe, boolean afficherFiabilite, boolean afficherDistance, boolean afficherDuree, boolean afficherLabelsSommets) {
        // Sommets : on affiche l'identifiant ou une chaine vide
        for (Node n : graphe) {
            if (afficherLabelsSommets == true) {
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

    /**
     * Calcule le plus court chemin en duree entre deux sommets via l'algorithme de Dijkstra.
     * Le cout minimise est l'attribut "duree" des aretes.
     * @param graphe Graphe sur lequel calculer (non null)
     * @param idDebut Identifiant du sommet de depart
     * @param idFin Identifiant du sommet d'arrivee
     * @return Le chemin trouve ; un Path vide si aucun chemin n'existe entre les deux sommets
     */
    public static Path plusCourtChemin(Graph graphe, String idDebut, String idFin) {
        Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "duree");
        dijkstra.init(graphe);
        dijkstra.setSource(graphe.getNode(idDebut));
        dijkstra.compute();
        Path chemin = dijkstra.getPath(graphe.getNode(idFin));
        return chemin;
    }

    /**
     * Calcule le chemin le plus sur entre deux sommets en ponderant la duree par la fiabilite.
     * Chaque arete recoit un attribut temporaire "dureeEstimee" = duree * (20 - fiabilite*10) / 10.
     * Les routes dont la fiabilite est inferieure a 0.25 recoivent une duree estimee tres elevee
     * afin d'etre evitees par Dijkstra. Le cout minimise est ensuite "dureeEstimee".
     * @param graphe Graphe sur lequel calculer (non null)
     * @param idDebut Identifiant du sommet de depart
     * @param idFin Identifiant du sommet d'arrivee
     * @return Le chemin le plus sur ; un Path vide si aucun chemin praticable n'existe
     */
    public static Path cheminPlusSur(Graph graphe, String idDebut, String idFin) {
        for (Edge e : graphe.getEachEdge()) {
            double fiabilite = (double) e.getAttribute("fiabilite");
            double duree     = (double) e.getAttribute("duree");
            double dureeEstimee;
            if (fiabilite < 0.25) {
                dureeEstimee = Double.MAX_VALUE / 2; // route bloquee, Dijkstra l'evite
            } else {
                dureeEstimee = duree * (20 - fiabilite * 10) / 10; // fiabilite stockee en 0.0-1.0, formule sur echelle /10
            }
            e.setAttribute("dureeEstimee", dureeEstimee);
        }
        Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "dureeEstimee");
        dijkstra.init(graphe);
        dijkstra.setSource(graphe.getNode(idDebut));
        dijkstra.compute();
        return dijkstra.getPath(graphe.getNode(idFin));
    }
}