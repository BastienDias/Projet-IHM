package saeihm.vue;

import java.awt.*;
import javax.swing.*;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import saeihm.modele.GenGraphe;

/**
 * Fenetre modale d'informations sur un centre clique (Question 5).
 */
public class FenetreInfosCentre extends JDialog {

    /** Graphe sur lequel portent tous les calculs. */
    private Graph graphe;

    /** Identifiant du centre dont on affiche les informations. */
    private String idCentre;

    /**
     * Construit et affiche la fenetre d'infos pour un centre.
     * @param parent Fenetre parente
     * @param graphe Graphe courant non null
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
     * Pose l'attribut "dureeEstimee" sur chaque arete a partir de la duree
     * et de la fiabilite. Une route trop peu fiable (< 0.25) recoit une
     * valeur tres elevee pour etre evitee par Dijkstra.
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
     * Met en place les composants : panneau du haut avec id, type et les
     * trois centres les plus proches par type, puis un JTabbedPane avec
     * les six tables (distances et durees par type de centre).
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
        panneauHaut.add(new JLabel("Type du centre : " + type), gc);
        gc.gridy = 2;
        panneauHaut.add(new JLabel("Maternite la plus proche : " + textePlusProche("maternite")), gc);
        gc.gridy = 3;
        panneauHaut.add(new JLabel("Bloc operatoire le plus proche : " + textePlusProche("bloc")), gc);
        gc.gridy = 4;
        panneauHaut.add(new JLabel("Centre de nutrition le plus proche : " + textePlusProche("centre")), gc);

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
     * Construit une JTable listant les centres d'un type avec le cout du
     * chemin le plus court vers eux (un seul Dijkstra par table grace a getPathLength).
     * @param type Type des centres a lister ("maternite", "bloc", "centre")
     * @param attribut Attribut a minimiser ("distance" ou "duree")
     * @param intitule Titre de la deuxieme colonne
     * @return JTable a deux colonnes : Id et le cout
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
     * Cherche le centre d'un type donne le plus proche en duree, et renvoie
     * son id avec sa duree minimale et sa duree estimee.
     * @param type Type recherche
     * @return Phrase descriptive du plus proche centre du type, ou "aucun"
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
