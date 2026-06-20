package vue;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Fenetre principale de l'application Medic'In.
 * Orchestre les sept panneaux : detient le graphe courant et la barre de statut,
 * expose les operations transverses (afficher un graphe, mettre a jour la statut,
 * reinitialiser le surlignage, repropager les preferences d'affichage) et delegue
 * tout le reste a chaque panneau specialise.
 * @author Sinjat, Battah, Dias
 */
public class FenetrePrincipale extends JFrame {

    /** Graphe actuellement affiche. Null tant qu'aucun graphe n'a ete charge. */
    private Graph grapheCourant;

    /** Barre de statut en bas de fenetre. */
    private final JLabel lblStatut;

    /** Panneaux specialises (voir leurs classes respectives). */
    private final PanneauActions panneauActions;
    private final PanneauGraphe panneauGraphe;
    private final PanneauStats panneauStats;
    private final PanneauAffichage panneauAffichage;
    private final PanneauItineraire panneauItineraire;
    private final PanneauTournee panneauTournee;

    /**
     * Construit et affiche la fenetre principale.
     * Le viewer GraphStream est utilise en mode integre : la propriete systeme
     * du renderer J2D est positionnee ici car elle est necessaire pour que la
     * detection des clics sur les sommets fonctionne.
     */
    public FenetrePrincipale() {
        super("Medic'In");
        System.setProperty("org.graphstream.ui.renderer",
                "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        // Instanciation de chaque panneau (chacun recoit "this" comme reference)
        panneauActions    = new PanneauActions(this);
        panneauGraphe     = new PanneauGraphe(this);
        panneauStats      = new PanneauStats(this);
        panneauAffichage  = new PanneauAffichage(this);
        panneauItineraire = new PanneauItineraire(this);
        panneauTournee    = new PanneauTournee(this);
        lblStatut         = new JLabel(" ");

        // Empilement vertical du panneau de droite
        JPanel panneauDroite = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 2, 2, 2);

        gbc.gridy = 0;
        panneauDroite.add(panneauStats, gbc);
        gbc.gridy = 1;
        panneauDroite.add(panneauAffichage, gbc);
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panneauDroite.add(panneauItineraire, gbc);
        gbc.gridy = 3;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panneauDroite.add(panneauTournee, gbc);

        // Assemblage final
        JPanel contenu = new JPanel(new BorderLayout());
        contenu.add(panneauActions, BorderLayout.NORTH);
        contenu.add(panneauGraphe, BorderLayout.CENTER);
        contenu.add(panneauDroite, BorderLayout.EAST);
        contenu.add(lblStatut, BorderLayout.SOUTH);
        this.setContentPane(contenu);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(900, 650);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }


    /** @return Le graphe courant (peut etre null tant qu'aucun n'est charge). */
    public Graph getGrapheCourant() {
        return grapheCourant;
    }

    /**
     * Remplace le graphe courant et met a jour tous les panneaux.
     * Methode appelee par les panneaux Actions (apres generation/chargement).
     * @param graphe Nouveau graphe a afficher (ignore si null)
     */
    public void afficherGraphe(Graph graphe) {
        if (graphe == null) return;
        this.grapheCourant = graphe;

        panneauGraphe.afficher(graphe);
        panneauStats.majStats(graphe);
        panneauAffichage.appliquer(graphe);
        panneauItineraire.populerCombos(graphe);
        panneauActions.setBoutonsApresChargement(true);
        panneauTournee.setBoutonActif(true);

        setStatut("Graphe charge : " + graphe.getNodeCount()
                + " sommets, " + graphe.getEdgeCount() + " routes.");
    }

    /**
     * Met a jour la barre de statut.
     * @param texte Texte a afficher (non null)
     */
    public void setStatut(String texte) {
        lblStatut.setText(texte);
    }

    /**
     * Reinitialise l'apparence de tous les sommets et de toutes les aretes du
     * graphe courant : efface le surlignage d'un itineraire, les routes risquees
     * et la selection d'un sommet precedemment affiches.
     */
    public void reinitialiserSurlignage() {
        if (grapheCourant == null) return;
        for (Edge e : grapheCourant.getEachEdge()) {
            e.removeAttribute("ui.class");
            e.setAttribute("ui.style", "fill-color: black; size: 1px;");
        }
        for (Node n : grapheCourant) {
            n.removeAttribute("ui.clicked");
            n.removeAttribute("ui.style");
        }
    }

    /**
     * Repropage l'etat des cases d'affichage sur le graphe courant.
     * Methode appelee notamment par le bouton "Recharger" du PanneauActions.
     */
    public void appliquerPreferencesAffichage() {
        panneauAffichage.appliquer(grapheCourant);
    }
}
