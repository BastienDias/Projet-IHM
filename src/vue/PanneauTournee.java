package vue;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

import modele.Algo;

/**
 * Panneau lateral : Question 6. Choix du nombre de camions (1 ou 2), du type
 * de centres a livrer, et calcul de la tournee correspondante.
 * @author Sinjat, Battah, Dias
 */

public class PanneauTournee extends JPanel {

    /** Reference vers la fenetre orchestratrice. */
    private final FenetrePrincipale parent;

    /** Combo nombre de camions (1 ou 2). */
    private final JComboBox<String> cbCamion;

    /** Combo type de centres a livrer (Tous, M, O, N). */
    private final JComboBox<String> cbCentres;

    /** Bouton de calcul, desactive tant qu'aucun graphe n'est charge. */
    private final JButton btCalculerTournee;

    /**
     * Construit le panneau de tournee.
     * @param parent Fenetre orchestratrice (non null)
     */
    public PanneauTournee(FenetrePrincipale parent) {
        super(new GridBagLayout());
        this.parent = parent;
        this.setBorder(BorderFactory.createTitledBorder("Tournee de vehicule"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 2, 5, 2);
        gbc.fill = GridBagConstraints.NONE;

        cbCamion = new JComboBox<>();
        cbCamion.addItem("1");
        cbCamion.addItem("2");

        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 0;
        this.add(new JLabel("Choix du nombre de camion(s) :"), gbc);

        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 1;
        gbc.gridy = 0;
        this.add(cbCamion, gbc);

        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 1;
        this.add(new JLabel("Type centres :"), gbc);

        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 1;
        gbc.gridy = 1;
        cbCentres = new JComboBox<>();
        cbCentres.addItem("Tous");
        cbCentres.addItem("M");
        cbCentres.addItem("O");
        cbCentres.addItem("N");
        this.add(cbCentres, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.gridy = 2;
        btCalculerTournee = new JButton("Calculer Tournee");
        btCalculerTournee.setEnabled(false);
        this.add(btCalculerTournee, gbc);

        btCalculerTournee.addActionListener(e -> calculerTournee());
    }

    /**
     * Active ou desactive le bouton de calcul.
     * @param actif true pour activer, false pour desactiver
     */
    public void setBoutonActif(boolean actif) {
        btCalculerTournee.setEnabled(actif);
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
    
    /** Lance le calcul d'une tournee (1 ou 2 camions) selon les combos. */
    private void calculerTournee() {
        Graph g = parent.getGrapheCourant();
        if (g == null) return;
        String type = (String) cbCentres.getSelectedItem();

        if ("1".equals(cbCamion.getSelectedItem())) {
            // Cas 1 camion
            Path p1 = Algo.tourneeLivraisonFiltrage(g, type);
            double duree = Algo.dureeChemin(p1);
            ArrayList<Node> ordre = Algo.ordreCentresVisites(g, p1);

            Algo.exporterResultat1Camion("grapheCourant.csv", duree, duree, ordre, "resultats1camion1Equipe3GroupeG2S2");

            for (Node n : ordre) {
                System.out.println(n.getId());
            }
            String msg = "La tournee a ete effectuee en " + String.format("%.1f", duree) + " minutes.";
            System.out.println(msg);
            parent.setStatut(msg);

        } else {
            // Cas 2 camions
            Path[] tournees = Algo.tournee2Camions(g, type);
            Path p1 = tournees[0];
            Path p2 = tournees[1];

            double duree1 = Algo.dureeChemin(p1);
            double duree2 = Algo.dureeChemin(p2);

            ArrayList<Node> ordre1 = Algo.ordreCentresVisites(g, p1);
            ArrayList<Node> ordre2 = Algo.ordreCentresVisites(g, p2);

            Algo.exporterResultat2Camions("grapheCourant.csv", duree1, duree2, ordre1, ordre2, "resultats1camion2Equipe3GroupeG2S2");

            for (Node n : ordre1) {
                System.out.println(n.getId());
            }
            String msg1 = "Camion 1 : " + String.format("%.1f", duree1) + " min";
            System.out.println(msg1);

            for (Node n : ordre2) {
                System.out.println(n.getId());
            }
            String msg2 = "Camion 2 : " + String.format("%.1f", duree2) + " min";
            System.out.println(msg2);

            parent.setStatut(msg1 + "  |  " + msg2);
        }
    }
}
