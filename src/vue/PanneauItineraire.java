package vue;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

import modele.GenGraphe;

/**
 * Panneau lateral : selection d'un sommet de depart et d'arrivee, puis calcul
 * du chemin le plus court en duree ou du chemin le plus sur (penalise par fiabilite).
 * @author Sinjat, Battah, Dias
 */
public class PanneauItineraire extends JPanel {

    /** Reference vers la fenetre orchestratrice. */
    private final FenetrePrincipale parent;

    /** Listes deroulantes des extremites. */
    private final JComboBox<String> cbDepart, cbArrivee;

    /** Boutons de calcul, desactives tant qu'aucun graphe n'est charge. */
    private final JButton btCheminLePlusCourt, btCheminLePlusSur;

    /**
     * Construit le panneau d'itineraire.
     * @param parent Fenetre orchestratrice (non null)
     */
    public PanneauItineraire(FenetrePrincipale parent) {
        super(new GridBagLayout());
        this.parent = parent;
        this.setBorder(BorderFactory.createTitledBorder("Itineraire"));

        cbDepart = new JComboBox<>();
        cbArrivee = new JComboBox<>();

        // Sous-panneau "Depart : ... / Arrivee : ..."
        JPanel panneauSel = new JPanel(new GridBagLayout());
        GridBagConstraints gcSel = new GridBagConstraints();
        gcSel.insets = new Insets(2, 4, 2, 4);

        gcSel.gridx = 0;
        gcSel.weightx = 0;
        gcSel.fill = GridBagConstraints.NONE;
        gcSel.anchor = GridBagConstraints.LINE_END;
        gcSel.gridy = 0;
        panneauSel.add(new JLabel("Depart :"), gcSel);
        gcSel.gridy = 1;
        panneauSel.add(new JLabel("Arrivee :"), gcSel);

        gcSel.gridx = 1;
        gcSel.weightx = 1;
        gcSel.fill = GridBagConstraints.HORIZONTAL;
        gcSel.anchor = GridBagConstraints.LINE_START;
        gcSel.gridy = 0;
        panneauSel.add(cbDepart, gcSel);
        gcSel.gridy = 1;
        panneauSel.add(cbArrivee, gcSel);

        btCheminLePlusCourt = new JButton("Calculer le chemin le plus court");
        btCheminLePlusCourt.setEnabled(false);
        btCheminLePlusCourt.setToolTipText("Plus court trajet en duree (Dijkstra)");

        btCheminLePlusSur = new JButton("Calculer le chemin le plus sur");
        btCheminLePlusSur.setEnabled(false);
        btCheminLePlusSur.setToolTipText("Trajet le plus fiable (penalise par fiabilite)");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 2, 2, 2);

        gbc.gridx = 0;
        gbc.gridy = 0;
        this.add(panneauSel, gbc);
        gbc.gridy = 1;
        this.add(btCheminLePlusCourt, gbc);
        gbc.gridy = 2;
        this.add(btCheminLePlusSur, gbc);

        btCheminLePlusCourt.addActionListener(e -> calculerEtAfficherCheminPlusCourt());
        btCheminLePlusSur.addActionListener(e -> calculerEtAfficherCheminPlusSur());
    }

    /**
     * Remplit les deux combos avec les identifiants de tous les sommets du graphe,
     * puis active les boutons de calcul.
     * @param g Graphe a partir duquel peupler les listes (non null)
     */
    public void populerCombos(Graph g) {
        cbDepart.removeAllItems();
        cbArrivee.removeAllItems();
        if (g == null) return;
        for (Node n : g) {
            cbDepart.addItem(n.getId());
            cbArrivee.addItem(n.getId());
        }
        btCheminLePlusCourt.setEnabled(true);
        btCheminLePlusSur.setEnabled(true);
    }

    /** Plus court chemin en duree, surligne en vert. */
    private void calculerEtAfficherCheminPlusCourt() {
        Graph g = parent.getGrapheCourant();
        if (g == null) return;

        String idDepart = (String) cbDepart.getSelectedItem();
        String idArrivee = (String) cbArrivee.getSelectedItem();
        parent.reinitialiserSurlignage();

        if (idDepart == null || idArrivee == null || idDepart.equals(idArrivee)) {
            JOptionPane.showMessageDialog(parent,
                    "Veuillez selectionner deux centres differents.",
                    "Saisie invalide", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Path chemin = GenGraphe.plusCourtChemin(g, idDepart, idArrivee);
        String resultat;
        if (chemin == null || chemin.empty()) {
            resultat = " Aucun chemin";
        } else {
            for (Edge e : chemin.getEdgePath()) {
                e.setAttribute("ui.style", "fill-color: rgb(0,255,0); size: 4px;");
            }
            double totalDuree = 0;
            for (Edge e : chemin.getEdgePath()) {
                totalDuree += (double) e.getAttribute("duree");
            }
            resultat = "Le chemin le plus court a une duree totale de : "
                    + String.format("%.0f", totalDuree) + " min";
        }
        parent.setStatut(" Itineraire calcule : " + resultat);
    }

    /** Chemin le plus sur, surligne en violet. */
    private void calculerEtAfficherCheminPlusSur() {
        Graph g = parent.getGrapheCourant();
        if (g == null) return;

        String idDepart = (String) cbDepart.getSelectedItem();
        String idArrivee = (String) cbArrivee.getSelectedItem();
        parent.reinitialiserSurlignage();

        if (idDepart == null || idArrivee == null || idDepart.equals(idArrivee)) {
            JOptionPane.showMessageDialog(parent,
                    "Veuillez selectionner deux centres differents.",
                    "Saisie invalide", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Path chemin = GenGraphe.cheminPlusSur(g, idDepart, idArrivee);
        String resultat;
        if (chemin == null || chemin.empty()) {
            resultat = "Aucun chemin (routes peu fiables bloquees)";
        } else {
            for (Edge e : chemin.getEdgePath()) {
                e.setAttribute("ui.style", "fill-color: rgb(148,0,211); size: 4px;");
            }
            double totalEstimee = 0;
            for (Edge e : chemin.getEdgePath()) {
                double duree = (double) e.getAttribute("duree");
                double fiabilite = (double) e.getAttribute("fiabilite");
                totalEstimee += duree * (20 - fiabilite * 10) / 10.0;
            }
            resultat = "Le chemin le plus sur a une duree estimee de : "
                    + String.format("%.0f", totalEstimee) + " min";
        }
        parent.setStatut(" Itineraire calcule : " + resultat);
    }
}
