package vue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.graphstream.graph.Graph;

import exception.FormatCsvException;
import modele.GenGraphe;

/**
 * Bandeau Nord : logo + boutons Generer / Charger / Recharger / Routes risquees.
 * Delegue toute la mise a jour de l'affichage du graphe a FenetrePrincipale.
 */
public class PanneauActions extends JPanel {

    /** Reference vers la fenetre orchestratrice (graphe courant, statut, etc.). */
    private final FenetrePrincipale parent;

    /** Boutons du bandeau. */
    private final JButton btGenerer, btCharger, btRecharger, btRoutesRisquees;

    /**
     * Construit le bandeau d'actions.
     * @param parent Fenetre orchestratrice (non null) sur laquelle les actions s'appliquent
     */
    public PanneauActions(FenetrePrincipale parent) {
        super(new BorderLayout());
        this.parent = parent;

        JPanel panneauBoutons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btGenerer = new JButton("Generer un graphe aleatoire");
        btCharger = new JButton("Charger un graphe (.csv)");
        btRoutesRisquees = new JButton("Afficher les routes risquees");
        btRoutesRisquees.setEnabled(false);
        btRecharger = new JButton("Recharger");
        btRecharger.setEnabled(false);

        panneauBoutons.add(btGenerer);
        panneauBoutons.add(btCharger);
        panneauBoutons.add(btRecharger);
        panneauBoutons.add(btRoutesRisquees);

        JLabel lblLogo = new JLabel();
        URL urlLogo = getClass().getResource("logo.png");
        if (urlLogo != null) {
            ImageIcon icone = new ImageIcon(urlLogo);
            parent.setIconImage(icone.getImage());
            Image img = icone.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(img));
        }
        lblLogo.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 10));

        this.add(lblLogo, BorderLayout.WEST);
        this.add(panneauBoutons, BorderLayout.CENTER);

        // Mise en forme
        btGenerer.setBackground(Color.GREEN);
        btGenerer.setForeground(Color.BLACK);
        btGenerer.setFocusPainted(false);
        btCharger.setBackground(Color.CYAN);
        btCharger.setForeground(Color.BLACK);
        btCharger.setFocusPainted(false);
        btRoutesRisquees.setBackground(Color.LIGHT_GRAY);
        btRoutesRisquees.setForeground(Color.BLACK);
        btRoutesRisquees.setFocusPainted(false);
        btRecharger.setBackground(Color.LIGHT_GRAY);
        btRecharger.setForeground(Color.BLACK);
        btRecharger.setFocusPainted(false);

        btGenerer.setToolTipText("Genere un graphe avec un nombre de centres choisi");
        btCharger.setToolTipText("Charge un graphe depuis un fichier CSV");
        btRoutesRisquees.setToolTipText("Met en evidence les routes trop peu fiables");
        btRecharger.setToolTipText("Reinitialise l'affichage (efface chemins et routes risquees)");

        brancherEcouteurs();
    }

    /** Branche les listeners sur les quatre boutons. */
    private void brancherEcouteurs() {
        btGenerer.addActionListener(e -> {
            Graph g = demanderEtGenerer();
            if (g != null) parent.afficherGraphe(g);
        });

        btCharger.addActionListener(e -> {
            Graph g = chargerDepuisFichier();
            if (g != null) parent.afficherGraphe(g);
        });

        btRecharger.addActionListener(e -> {
            if (parent.getGrapheCourant() != null) {
                parent.reinitialiserSurlignage();
                parent.appliquerPreferencesAffichage();
            }
        });

        btRoutesRisquees.addActionListener(e -> demanderEtAfficherRoutesRisquees());
    }

    /**
     * Active les boutons qui n'ont de sens qu'apres chargement d'un graphe.
     * @param actif true pour activer, false pour desactiver
     */
    public void setBoutonsApresChargement(boolean actif) {
        btRoutesRisquees.setEnabled(actif);
        btRecharger.setEnabled(actif);
    }

    /** Demande la taille du graphe puis le genere aleatoirement. */
    private Graph demanderEtGenerer() {
        String saisie = JOptionPane.showInputDialog(parent,
                "Choisir le nombre de centres de la carte\n",
                "Generer le graphe",
                JOptionPane.QUESTION_MESSAGE);
        if (saisie == null) return null;

        int nbSommets;
        try {
            nbSommets = Integer.parseInt(saisie.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(parent, "Valeur invalide : merci de saisir un nombre entier.",
                    "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (nbSommets <= 0) {
            JOptionPane.showMessageDialog(parent, "Le nombre de sommets doit etre superieur a 0.",
                    "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return GenGraphe.construireGrapheAleatoire(nbSommets);
    }

    /** Demande un fichier CSV puis charge le graphe. */
    private Graph chargerDepuisFichier() {
        JFileChooser selecteur = new JFileChooser();
        selecteur.setFileFilter(new FileNameExtensionFilter("Fichiers CSV (*.csv)", "csv"));
        int resultat = selecteur.showOpenDialog(parent);
        if (resultat != JFileChooser.APPROVE_OPTION) return null;

        String chemin = selecteur.getSelectedFile().getAbsolutePath();
        try {
            return GenGraphe.chargerGraphe(chemin);
        } catch (FormatCsvException ex) {
            JOptionPane.showMessageDialog(parent, "Fichier CSV invalide :\n" + ex.getMessage(),
                    "Erreur de format", JOptionPane.ERROR_MESSAGE);
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(parent, "Impossible de lire le fichier :\n" + chemin,
                    "Erreur de lecture", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    /** Demande un seuil et met en evidence les routes en dessous. */
    private void demanderEtAfficherRoutesRisquees() {
        Graph g = parent.getGrapheCourant();
        if (g == null) return;
        parent.reinitialiserSurlignage();

        String saisie = JOptionPane.showInputDialog(parent,
                "Seuil de fiabilite (entre 0 et 1)\nLes routes en dessous seront affichees en rouge :",
                "Routes risquees",
                JOptionPane.QUESTION_MESSAGE);
        if (saisie == null) return;

        double seuil;
        try {
            seuil = Double.parseDouble(saisie.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(parent, "Valeur invalide : merci de saisir un nombre decimal.",
                    "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (seuil <= 0 || seuil > 1) {
            JOptionPane.showMessageDialog(parent, "Le seuil doit etre dans l'intervalle ]0 ; 1].",
                    "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int nbRisquees = GenGraphe.marquerRoutesRisquees(g, seuil);
        parent.setStatut("Routes risquees (fiabilite < " + seuil + ") : "
                + nbRisquees + " sur " + g.getEdgeCount());
    }
}
