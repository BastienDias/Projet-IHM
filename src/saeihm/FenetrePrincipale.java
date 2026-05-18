package saeihm;

import java.awt.*;
import javax.swing.*;
import org.graphstream.graph.Graph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Fenetre principale de l'application Medic'In.
 * Permet de generer ou charger un graphe de district, de le visualiser
 * directement dans la fenetre via GraphStream, et de consulter les statistiques
 * du graphe (compteurs par type de centre, routes risquees au-dela d'un seuil).
 */

public class FenetrePrincipale extends JFrame {

    private JButton btGenerer, btCharger, btRoutesRisquees;
    private JLabel lblNbMaternite, lblNbBloc, lblNbCentre, lblStatut;
    private JCheckBox cbFiabilite, cbDistance, cbDuree, cbLabelsSommets;
    private JPanel panneauGraphe;
    private JPanel panneauStats;

    /** Graphe actuellement affiche. Null tant qu'aucun graphe n'a ete charge. */
    private Graph grapheCourant;

    /** Viewer GraphStream actif. Conserve pour pouvoir le fermer avant d'en ouvrir un autre. */
    private Viewer viewerCourant;

    /**
     * Construit et affiche la fenetre principale.
     * Le viewer GraphStream est utilise en mode integre pour afficher le rendu
     * directement dans un JPanel plutot que dans une fenetre separee.
     */
    public FenetrePrincipale() {
        super("Medic'In");

        initComposants();
        initActions();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(900, 650);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /**
     * Initialise la disposition des composants.
     * En haut : bandeau d'actions (generer / charger / routes risquees).
     * Au centre : panneau qui accueillera la vue GraphStream.
     * A droite : panneau d'informations affichant les compteurs M/O/N.
     * En bas : barre de statut.
     */
    private void initComposants() {
        JPanel panneauActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btGenerer = new JButton("Generer un graphe aleatoire");
        btCharger = new JButton("Charger un graphe (.csv)");
        btRoutesRisquees = new JButton("Afficher les routes risquees");
        btRoutesRisquees.setEnabled(false);
        panneauActions.add(btGenerer);
        panneauActions.add(btCharger);
        panneauActions.add(btRoutesRisquees);

        panneauGraphe = new JPanel(new BorderLayout());
        panneauGraphe.setBorder(BorderFactory.createTitledBorder("Carte du district"));
        JLabel lblVide = new JLabel("Aucun graphe charge.", SwingConstants.CENTER);
        panneauGraphe.add(lblVide, BorderLayout.CENTER);

        panneauStats = new JPanel(new GridLayout(0, 1, 5, 5));
        panneauStats.setBorder(BorderFactory.createTitledBorder("Statistiques"));
        lblNbMaternite = new JLabel("Maternites (M) : -");
        lblNbBloc      = new JLabel("Blocs operatoires (O) : -");
        lblNbCentre    = new JLabel("Centres de nutrition (N) : -");
        panneauStats.add(lblNbMaternite);
        panneauStats.add(lblNbBloc);
        panneauStats.add(lblNbCentre);

        // Panneau Affichage : controle ce qui apparait sur les aretes et sommets.
        // Par defaut : seuls les labels de sommets sont visibles (plus lisible).
        JPanel panneauAffichage = new JPanel(new GridLayout(0, 1, 5, 5));
        panneauAffichage.setBorder(BorderFactory.createTitledBorder("Affichage"));
        cbFiabilite      = new JCheckBox("Fiabilite sur les routes",     false);
        cbDistance       = new JCheckBox("Distance sur les routes",      false);
        cbDuree          = new JCheckBox("Duree sur les routes",         false);
        cbLabelsSommets  = new JCheckBox("Identifiants des sommets",     true);
        panneauAffichage.add(cbFiabilite);
        panneauAffichage.add(cbDistance);
        panneauAffichage.add(cbDuree);
        panneauAffichage.add(cbLabelsSommets);

        // Empilement vertical : Statistiques au-dessus, Affichage en-dessous
        JPanel panneauDroite = new JPanel(new BorderLayout(5, 5));
        panneauDroite.add(panneauStats,     BorderLayout.NORTH);
        panneauDroite.add(panneauAffichage, BorderLayout.CENTER);

        lblStatut = new JLabel(" ");
        lblStatut.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        JPanel contenu = new JPanel(new BorderLayout(5, 5));
        contenu.add(panneauActions, BorderLayout.NORTH);
        contenu.add(panneauGraphe,  BorderLayout.CENTER);
        contenu.add(panneauDroite,  BorderLayout.EAST);
        contenu.add(lblStatut,      BorderLayout.SOUTH);
        this.setContentPane(contenu);
    }

    /**
     * Branche les ecouteurs d'evenements sur les boutons.
     */
    private void initActions() {
        btGenerer.addActionListener(e -> {
            Graph carte = GenGraphe.construireGrapheAleatoire(10);
            afficherGraphe(carte);
        });

        btCharger.addActionListener(e -> {
            JFileChooser selecteur = new JFileChooser();
            selecteur.setFileFilter(new FileNameExtensionFilter("Fichiers CSV (*.csv)", "csv"));
            int resultat = selecteur.showOpenDialog(this);
            if (resultat == JFileChooser.APPROVE_OPTION) {
                String chemin = selecteur.getSelectedFile().getAbsolutePath();
                Graph carte = GenGraphe.chargerGraphe(chemin);
                afficherGraphe(carte);
            }
        });

        btRoutesRisquees.addActionListener(e -> demanderEtAfficherRoutesRisquees());
        
        java.awt.event.ActionListener majAffichage = e -> appliquerPreferencesAffichage();
        cbFiabilite.addActionListener(majAffichage);
        cbDistance.addActionListener(majAffichage);
        cbDuree.addActionListener(majAffichage);
        cbLabelsSommets.addActionListener(majAffichage);

    }

    /**
     * Affiche le graphe passe en parametre dans le panneau central.
     * Sous gs-core 1.3, on instancie un Viewer puis on recupere une ViewPanel via
     * addDefaultView(false) qui empeche GraphStream d'ouvrir sa propre JFrame.
     * Si un viewer etait deja actif, il est ferme proprement avant d'en creer un nouveau
     * pour eviter une fuite de threads.
     * @param graphe Graphe a afficher (non null)
     */
    private void afficherGraphe(Graph graphe) {
        this.grapheCourant = graphe;

        if (viewerCourant != null) {
            viewerCourant.close();
            viewerCourant = null;
        }

        viewerCourant = new Viewer(graphe, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewerCourant.enableAutoLayout();
        ViewPanel vue = viewerCourant.addDefaultView(false);

        panneauGraphe.removeAll();
        panneauGraphe.add((Component) vue, BorderLayout.CENTER);
        panneauGraphe.revalidate();
        panneauGraphe.repaint();

        majCompteurs();
        appliquerPreferencesAffichage();
        btRoutesRisquees.setEnabled(true);
        lblStatut.setText("Graphe charge : " + graphe.getNodeCount() + " sommets, "
                + graphe.getEdgeCount() + " routes.");
    }

    /**
     * Met a jour les trois labels de compteurs en interrogeant GenGraphe.
     */
    private void majCompteurs() {
        if (grapheCourant == null) return;
        int nbM = GenGraphe.compterSommetsParType(grapheCourant, "maternite");
        int nbO = GenGraphe.compterSommetsParType(grapheCourant, "bloc");
        int nbN = GenGraphe.compterSommetsParType(grapheCourant, "centre");
        lblNbMaternite.setText("Maternites (M) : " + nbM);
        lblNbBloc.setText("Blocs operatoires (O) : " + nbO);
        lblNbCentre.setText("Centres de nutrition (N) : " + nbN);
    }

    /**
     * Demande un seuil de fiabilite a l'utilisateur via une boite de dialogue,
     * puis colore en rouge les aretes du graphe dont la fiabilite est strictement inferieure.
     * En cas de saisie invalide (non numerique ou hors plage), une boite d'erreur s'affiche.
     */
    private void demanderEtAfficherRoutesRisquees() {
        if (grapheCourant == null) return;

        String saisie = JOptionPane.showInputDialog(this,
                "Seuil de fiabilite (entre 0 et 1)\nLes routes en dessous seront affichees en rouge :",
                "Routes risquees",
                JOptionPane.QUESTION_MESSAGE);

        if (saisie == null) return;

        double seuil;
        try {
            seuil = Double.parseDouble(saisie.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Valeur invalide : merci de saisir un nombre decimal.",
                    "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (seuil <= 0 || seuil > 1) {
            JOptionPane.showMessageDialog(this,
                    "Le seuil doit etre dans l'intervalle ]0 ; 1].",
                    "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int nbRisquees = GenGraphe.marquerRoutesRisquees(grapheCourant, seuil);
        lblStatut.setText("Routes risquees (fiabilite < " + seuil + ") : "
                + nbRisquees + " sur " + grapheCourant.getEdgeCount());
    }
    private void appliquerPreferencesAffichage() {
        if (grapheCourant == null) return;
        GenGraphe.majAffichage(grapheCourant,
                cbFiabilite.isSelected(),
                cbDistance.isSelected(),
                cbDuree.isSelected(),
                cbLabelsSommets.isSelected());
    }
}


