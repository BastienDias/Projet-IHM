package vue;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
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

    /** Combo choix de l'algorithme (Glouton ou Optimise 2-opt), uniquement pour 1 camion. */
    private final JComboBox<String> cbAlgo;

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

        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 2;
        this.add(new JLabel("Algorithme :"), gbc);

        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 1;
        gbc.gridy = 2;
        cbAlgo = new JComboBox<>();
        cbAlgo.addItem("Glouton");
        cbAlgo.addItem("Optimise (2-opt)");
        cbAlgo.setSelectedIndex(1);
        cbAlgo.setToolTipText("Choix de l'algorithme : glouton de base ou version optimisee par 2-opt sur matrice");
        this.add(cbAlgo, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.gridy = 3;
        btCalculerTournee = new JButton("Calculer Tournee");
        btCalculerTournee.setEnabled(false);
        this.add(btCalculerTournee, gbc);

        btCalculerTournee.addActionListener(e -> calculerTournee());

        // Masque "Glouton" en mode 2 camions (seule la version optimisee y est disponible)
        cbCamion.addActionListener(e -> majAlgoDispoSelonCamion());
        majAlgoDispoSelonCamion();
    }

    /**
     * Ajuste les choix d'algorithme disponibles selon le nombre de camions selectionne.
     * En mode 2 camions, l'option "Glouton" est retiree puisque l'ancien algorithme a
     * ete supprime. En mode 1 camion, "Glouton" est restaure pour comparaison.
     */
    private void majAlgoDispoSelonCamion() {
        if ("2".equals(cbCamion.getSelectedItem())) {
            cbAlgo.removeItem("Glouton");
        } else {
            boolean dejaPresent = false;
            for (int i = 0; i < cbAlgo.getItemCount(); i++) {
                if ("Glouton".equals(cbAlgo.getItemAt(i))) {
                    dejaPresent = true;
                    break;
                }
            }
            if (!dejaPresent) {
                cbAlgo.insertItemAt("Glouton", 0);
            }
        }
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
    
/**
     * Calcule et exporte la tournee de livraison selon le nombre de camions choisi.
     * En mode 1 camion, l'algorithme choisi dans la combobox (glouton ou 2-opt) est
     * utilise pour l'ordre exporte ; la duree gloutonne est toujours calculee pour
     * remplir la colonne de comparaison du fichier CSV. En mode 2 camions, la version
     * optimisee est utilisee. Les sommets visites sont affiches en console avec leur
     * numero, et le resultat est affiche dans la barre de statut.
     */
    private void calculerTournee() {
        Graph g = parent.getGrapheCourant();
        if (g == null) {
            return;
        }
        String type = (String) cbCentres.getSelectedItem();

        if ("1".equals(cbCamion.getSelectedItem())) {
            String algo = (String) cbAlgo.getSelectedItem();

            // On calcule toujours la tournee gloutonne (sert de reference pour la
            // colonne de comparaison du CSV).
            Path pGlouton = Algo.tourneeLivraisonFiltrage(g, type);
            double dureeGlouton = Algo.dureeChemin(pGlouton);

            // Le chemin reellement exporte depend de l'algorithme choisi.
            Path pChoisi;
            if ("Optimise (2-opt)".equals(algo)) {
                pChoisi = Algo.tourneeOptimisee(g, type);
            } else {
                pChoisi = pGlouton;
            }
            double dureeChoisie = Algo.dureeChemin(pChoisi);

            ArrayList<Node> ordre = Algo.ordreCentresVisites(g, pChoisi);

            Algo.exporterResultat1Camion("grapheCourant.csv", dureeGlouton, dureeChoisie, ordre, "resultatsCamion1Equipe3GroupeG2S2");

            // Affichage console des sommets visites avec leur numero
            System.out.println("Ordre (" + algo + ") :");
            for (Node n : ordre) {
                System.out.println(n.getId() + " (" + Algo.numeroSommet(n) + ")");
            }

            String msg = "Tournee " + algo + " : " + String.format("%.1f", dureeChoisie) + " min"
                    + " (Glouton : " + String.format("%.1f", dureeGlouton) + " min)";
            System.out.println(msg);
            parent.setStatut(msg);

        } else {
            // Cas 2 camions : seule la version optimisee est disponible.
            Path[] tournees = Algo.tournee2CamionsOptimisee(g, type);

            Path p1 = tournees[0];
            Path p2 = tournees[1];

            double duree1 = Algo.dureeChemin(p1);
            double duree2 = Algo.dureeChemin(p2);

            ArrayList<Node> ordre1 = Algo.ordreCentresVisites(g, p1);
            ArrayList<Node> ordre2 = Algo.ordreCentresVisites(g, p2);

            Algo.exporterResultat2Camions("grapheCourant.csv", duree1, duree2, ordre1, ordre2, "resultatsCamions2Equipe3GroupeG2S2");

            // Affichage console des sommets visites avec leur numero
            System.out.println("Camion 1 :");
            for (Node n : ordre1) {
                System.out.println(n.getId() + " (" + Algo.numeroSommet(n) + ")");
            }
            System.out.println("Camion 2 :");
            for (Node n : ordre2) {
                System.out.println(n.getId() + " (" + Algo.numeroSommet(n) + ")");
            }

            String msg1 = "Camion 1 : " + String.format("%.1f", duree1) + " min";
            String msg2 = "Camion 2 : " + String.format("%.1f", duree2) + " min";
            System.out.println(msg1);
            System.out.println(msg2);
            parent.setStatut(msg1 + "  |  " + msg2);
        }
    }
}
