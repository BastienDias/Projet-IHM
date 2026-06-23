package vue;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.graphstream.graph.Graph;

import modele.GenGraphe;

/**
 * Panneau lateral : affiche les compteurs de sommets par type (M / O / N).
 * @author Sinjat, Battah, Dias
 */
public class PanneauStats extends JPanel {

    /** Labels des trois compteurs, mis a jour via majStats. */
    private final JLabel lblNbMaternite, lblNbBloc, lblNbCentre;

    /**
     * Construit le panneau de statistiques.
     * Le parametre parent n'est pas utilise mais conserve par coherence avec les autres panneaux.
     * @param parent Fenetre orchestratrice
     */
    public PanneauStats(FenetrePrincipale parent) {
        super(new GridBagLayout());
        this.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Statistiques",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5);

        lblNbMaternite = new JLabel("Maternites (M) : -");
        lblNbBloc = new JLabel("Blocs operatoires (O) : -");
        lblNbCentre = new JLabel("Centres de nutrition (N) : -");
        lblNbMaternite.setForeground(Color.RED);
        lblNbBloc.setForeground(Color.BLUE);
        lblNbCentre.setForeground(Color.ORANGE);

        gbc.gridy = 0;
        this.add(lblNbMaternite, gbc);
        gbc.gridy = 1;
        this.add(lblNbBloc, gbc);
        gbc.gridy = 2;
        this.add(lblNbCentre, gbc);
    }

    /**
     * Met a jour les trois compteurs en interrogeant le graphe passe en parametre.
     * @param g Graphe a analyser (si null, les compteurs sont remis a "-")
     */
    public void majStats(Graph g) {
        if (g == null) {
            lblNbMaternite.setText("Maternites (M) : -");
            lblNbBloc.setText("Blocs operatoires (O) : -");
            lblNbCentre.setText("Centres de nutrition (N) : -");
            return;
        }
        lblNbMaternite.setText("Maternites (M) : " + GenGraphe.compterSommetsParType(g, "M"));
        lblNbBloc.setText("Blocs operatoires (O) : " + GenGraphe.compterSommetsParType(g, "O"));
        lblNbCentre.setText("Centres de nutrition (N) : " + GenGraphe.compterSommetsParType(g, "N"));
    }
    
    /**
     * Supprime les infos des labels lorsque qu'on supprime un graphe
     */
    public void reinitialiser() {
    lblNbMaternite.setText("Maternites (M) : -");
    lblNbBloc.setText("Blocs operatoires (O) : -");
    lblNbCentre.setText("Centres de nutrition (N) : -");
    }
}