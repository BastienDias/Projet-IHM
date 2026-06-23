package vue;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.graphstream.graph.Graph;

import modele.GenGraphe;

/**
 * Panneau lateral : cases a cocher pour les preferences d'affichage des routes
 * (fiabilite, distance, duree) et des sommets (identifiants).
 * @author Sinjat, Battah, Dias
 */
public class PanneauAffichage extends JPanel {

    /** Reference vers la fenetre orchestratrice. */
    private final FenetrePrincipale parent;

    /** Cases a cocher. */
    private final JCheckBox cbFiabilite, cbDistance, cbDuree, cbLabelsSommets;

    /**
     * Construit le panneau d'affichage.
     * @param parent Fenetre orchestratrice (non null)
     */
    public PanneauAffichage(FenetrePrincipale parent) {
        super(new GridBagLayout());
        this.parent = parent;
        this.setBorder(BorderFactory.createTitledBorder("Affichage"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5);

        cbFiabilite = new JCheckBox("Fiabilite sur les routes", false);
        cbDistance = new JCheckBox("Distance sur les routes", false);
        cbDuree = new JCheckBox("Duree sur les routes", false);
        cbLabelsSommets = new JCheckBox("Identifiants des sommets", true);

        gbc.gridy = 0;
        this.add(cbFiabilite, gbc);
        gbc.gridy = 1;
        this.add(cbDistance, gbc);
        gbc.gridy = 2;
        this.add(cbDuree, gbc);
        gbc.gridy = 3;
        this.add(cbLabelsSommets, gbc);

        // Chaque clic propage l'etat des cases au graphe courant
        ActionListener maj = e -> appliquer(parent.getGrapheCourant());
        cbFiabilite.addActionListener(maj);
        cbDistance.addActionListener(maj);
        cbDuree.addActionListener(maj);
        cbLabelsSommets.addActionListener(maj);
    }

    /**
     * Repropage l'etat des cases sur le graphe passe en parametre.
     * @param g Graphe a mettre a jour (peut etre null, alors la methode ne fait rien)
     */
    public void appliquer(Graph g) {
        if (g != null) {
            GenGraphe.majAffichage(g,
                    cbFiabilite.isSelected(),
                    cbDistance.isSelected(),
                    cbDuree.isSelected(),
                    cbLabelsSommets.isSelected());
        }
    }
}
