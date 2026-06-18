package vue;

import java.awt.*;
import javax.swing.*;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import modele.GenGraphe;

/**
 * Fenetre modale d'informations sur un centre (Question 5).
 * Ouverte lors d'un clic droit sur un sommet, elle affiche l'identifiant et le
 * type du centre, le centre le plus proche de chaque type (en duree et duree estimee),
 * puis six tables : les distances et les durees minimales vers les centres de chaque type.
 */
public class FenetreInfosCentre extends JDialog {

    /** Graphe sur lequel portent tous les calculs. */
    private Graph graphe;

    /** Identifiant du centre dont on affiche les informations. */
    private String idCentre;

    /**
     * Construit et affiche la fenetre d'infos pour un centre.
     * @param parent Fenetre parente, sur laquelle la boite de dialogue est centree
     * @param graphe Graphe courant (non null)
     * @param idCentre Identifiant du sommet clique
     */
    public FenetreInfosCentre(JFrame parent, Graph graphe, String idCentre) {
        super(parent, "Informations sur le centre " + idCentre, true);
        this.graphe = graphe;
        this.idCentre = idCentre;

        poserDureesEstimees();
        initComposants();

        this.setSize(650, 500);
        this.setLocationRelativeTo(parent);
        this.pack();
        this.setVisible(true);
    }

    /**
     * Pose l'attribut "dureeEstimee" sur chaque arete a partir de la duree et de la fiabilite.
     * La formule est duree * (20 - fiabilite*10) / 10 ; une route trop peu fiable (fiabilite < 0.25)
     * recoit une valeur tres elevee afin d'etre evitee par Dijkstra.
     */
    private void poserDureesEstimees() {
        for (Edge e : graphe.getEachEdge()) {
            double fiabilite = (double) e.getAttribute("fiabilite");
            double duree = (double) e.getAttribute("duree");
            double dureeEstimee;
            if (fiabilite < 0.25) {
                dureeEstimee = Double.MAX_VALUE / 2;
            } else {
                dureeEstimee = duree * (20 - fiabilite * 10) / 10.0;
            }
            e.setAttribute("dureeEstimee", dureeEstimee);
        }
    }

    /**
     * Met en place les composants : un panneau haut recapitulant l'id, le type et
     * le centre le plus proche de chaque type, puis un JTabbedPane contenant les six
     * tables (distances et durees vers les centres de type M, O et N).
     */
    private void initComposants() {
        Node centre = graphe.getNode(idCentre);

        JPanel panneauPrincipal = new JPanel(new BorderLayout(5, 5));
        panneauPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panneauHaut = new JPanel(new GridBagLayout());
        panneauHaut.setBorder(BorderFactory.createTitledBorder("Centre selectionne"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(3, 5, 3, 5);

        String type = (String) centre.getAttribute("type");
        gc.gridy = 0;
        panneauHaut.add(new JLabel("Id du centre : " + idCentre), gc);
        gc.gridy = 1;
        panneauHaut.add(new JLabel("Type du centre : " + libelleType(type)), gc);
        gc.gridy = 2;
        panneauHaut.add(new JLabel("Maternite la plus proche : " + textePlusProche("M")), gc);
        gc.gridy = 3;
        panneauHaut.add(new JLabel("Bloc operatoire le plus proche : " + textePlusProche("O")), gc);
        gc.gridy = 4;
        panneauHaut.add(new JLabel("Centre de nutrition le plus proche : " + textePlusProche("N")), gc);

        JTabbedPane onglets = new JTabbedPane();
        onglets.add("Distances maternites", new JScrollPane(creerTable("M", "distance", "Distance (km)")));
        onglets.add("Distances blocs", new JScrollPane(creerTable("O", "distance", "Distance (km)")));
        onglets.add("Distances nutrition", new JScrollPane(creerTable("N", "distance", "Distance (km)")));
        onglets.add("Durees maternites", new JScrollPane(creerTable("M", "duree", "Duree (min)")));
        onglets.add("Durees blocs", new JScrollPane(creerTable("O", "duree", "Duree (min)")));
        onglets.add("Durees nutrition", new JScrollPane(creerTable("N", "duree", "Duree (min)")));

        panneauPrincipal.add(panneauHaut, BorderLayout.NORTH);
        panneauPrincipal.add(onglets, BorderLayout.CENTER);
        this.setContentPane(panneauPrincipal);
    }

    /**
    * Convertit un code de type ("M", "O", "N") en libelle lisible.
    * @param type Code du type lu sur le sommet
    * @return Le code suivi de son intitule, ou le code seul si inconnu
    */
    private String libelleType(String type) {
        if (type == null) {
            return "inconnu";
        }
        
        switch (type) {
            case "M": return "M - Maternite";
            case "O": return "O - Bloc operatoire";
            case "N": return "N - Centre de nutrition";
            default:  return type;
        }
    }

    /**
     * Construit une JTable listant les centres d'un type avec le cout du plus court
     * chemin depuis le centre courant vers chacun d'eux. Un seul Dijkstra est calcule
     * par table : la lecture du cout vers chaque sommet se fait via getPathLength.
     * @param type Type des centres a lister ("M", "O" ou "N")
     * @param attribut Attribut a minimiser ("distance" ou "duree")
     * @param intitule Titre de la deuxieme colonne du tableau
     * @return JTable a deux colonnes (Id du centre, cout) ; "Aucun chemin" si non atteignable
     */
    private JTable creerTable(String type, String attribut, String intitule) {
        int nb = GenGraphe.compterSommetsParType(graphe, type);
        Object[][] donnees = new Object[nb][2];

        Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, attribut);
        dijkstra.init(graphe);
        dijkstra.setSource(graphe.getNode(idCentre));
        dijkstra.compute();

        int ligne = 0;
        for (Node n : graphe) {
            String typeNoeud = (String) n.getAttribute("type");
            if (type.equals(typeNoeud)) {
                donnees[ligne][0] = n.getId();
                double cout = dijkstra.getPathLength(n);
                if (cout == Double.POSITIVE_INFINITY) {
                    donnees[ligne][1] = "Aucun chemin";
                } else {
                    donnees[ligne][1] = String.format("%.0f", cout);
                }
                ligne++;
            }
        }

        String[] colonnes = {"Id", intitule};
        return new JTable(donnees, colonnes);
    }

    /**
     * Cherche le centre d'un type donne le plus proche en duree depuis le centre courant.
     * Deux calculs de Dijkstra sont effectues (sur "duree" et sur "dureeEstimee") afin
     * de renvoyer a la fois la duree minimale et la duree estimee correspondante.
     * @param type Type recherche ("M", "O" ou "N")
     * @return Phrase decrivant le centre le plus proche (id, duree, duree estimee), "aucun" si introuvable
     */
    private String textePlusProche(String type) {
        Dijkstra dijkstraDuree = new Dijkstra(Dijkstra.Element.EDGE, null, "duree");
        dijkstraDuree.init(graphe);
        dijkstraDuree.setSource(graphe.getNode(idCentre));
        dijkstraDuree.compute();

        Dijkstra dijkstraEstimee = new Dijkstra(Dijkstra.Element.EDGE, null, "dureeEstimee");
        dijkstraEstimee.init(graphe);
        dijkstraEstimee.setSource(graphe.getNode(idCentre));
        dijkstraEstimee.compute();

        String idPlusProche = "";
        double meilleureDuree = Double.POSITIVE_INFINITY;
        double dureeEstimee = Double.POSITIVE_INFINITY;

        for (Node n : graphe) {
            String typeNoeud = (String) n.getAttribute("type");
            if (type.equals(typeNoeud) && !n.getId().equals(idCentre)) {
                double duree = dijkstraDuree.getPathLength(n);
                if (duree < meilleureDuree) {
                    meilleureDuree = duree;
                    idPlusProche = n.getId();
                    dureeEstimee = dijkstraEstimee.getPathLength(n);
                }
            }
        }

        if (idPlusProche.equals("")) {
            return "aucun";
        }
        return idPlusProche
                + " | duree : " + String.format("%.0f", meilleureDuree) + " min"
                + " | duree estimee : " + String.format("%.0f", dureeEstimee) + " min";
    }
}