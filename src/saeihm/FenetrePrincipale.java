package saeihm;
//test
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

import org.graphstream.graph.Graph;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

/**
 * Fenetre principale de l'application Medic'In.
 * Permet de generer ou charger un graphe de district, de le visualiser
 * directement dans la fenetre via GraphStream, et de consulter les statistiques
 * du graphe (compteurs par type de centre, routes risquees au-dela d'un seuil).
 */
public class FenetrePrincipale extends JFrame {

    private JButton btGenerer, btCharger, btRoutesRisquees, btCalculerItineraire;
    private JLabel lblNbMaternite, lblNbBloc, lblNbCentre, lblStatut;
    private JCheckBox cbFiabilite, cbDistance, cbDuree, cbLabelsSommets;
    private JComboBox<String> cbDepart, cbArrivee ;
    private JTextArea zoneResultat;
    private JPanel panneauGraphe;
    private JPanel panneauStats;

    /** Graphe actuellement affiche. Null tant qu'aucun graphe n'a ete charge. */
    private Graph grapheCourant;

    /** Viewer GraphStream actif. Conserve pour pouvoir le fermer avant d'en ouvrir un autre. */
    private Viewer viewerCourant;

    /** Vue active, conservee pour detecter les clics sur les noeuds. */
    private ViewPanel vueCourante;

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
     * A droite : panneau d'informations affichant les compteurs M/O/N
     * et le panneau de preferences d'affichage.
     * En bas : barre de statut.
     * Les panneaux Statistiques et Affichage utilisent GridBagLayout pour
     * empiler verticalement leurs composants en les alignant a gauche.
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

        panneauStats = new JPanel(new GridBagLayout());
        panneauStats.setBorder(BorderFactory.createTitledBorder("Statistiques"));
        GridBagConstraints contraintesStats = new GridBagConstraints();
        contraintesStats.gridx = 0;
        contraintesStats.anchor = GridBagConstraints.WEST;
        contraintesStats.insets = new Insets(2, 5, 2, 5);

        lblNbMaternite = new JLabel("Maternites (M) : -");
        lblNbBloc      = new JLabel("Blocs operatoires (O) : -");
        lblNbCentre    = new JLabel("Centres de nutrition (N) : -");

        contraintesStats.gridy = 0;
        panneauStats.add(lblNbMaternite, contraintesStats);
        contraintesStats.gridy = 1;
        panneauStats.add(lblNbBloc, contraintesStats);
        contraintesStats.gridy = 2;
        panneauStats.add(lblNbCentre, contraintesStats);

        // Panneau Affichage : controle ce qui apparait sur les aretes et sommets.
        // Par defaut : seuls les labels de sommets sont visibles (plus lisible).
        JPanel panneauAffichage = new JPanel(new GridBagLayout());
        panneauAffichage.setBorder(BorderFactory.createTitledBorder("Affichage"));
        GridBagConstraints contraintesAff = new GridBagConstraints();
        contraintesAff.gridx = 0;
        contraintesAff.anchor = GridBagConstraints.WEST;
        contraintesAff.insets = new Insets(2, 5, 2, 5);

        cbFiabilite      = new JCheckBox("Fiabilite sur les routes",     false);
        cbDistance       = new JCheckBox("Distance sur les routes",      false);
        cbDuree          = new JCheckBox("Duree sur les routes",         false);
        cbLabelsSommets  = new JCheckBox("Identifiants des sommets",     true);

        contraintesAff.gridy = 0;
        panneauAffichage.add(cbFiabilite, contraintesAff);
        contraintesAff.gridy = 1;
        panneauAffichage.add(cbDistance, contraintesAff);
        contraintesAff.gridy = 2;
        panneauAffichage.add(cbDuree, contraintesAff);
        contraintesAff.gridy = 3;
        panneauAffichage.add(cbLabelsSommets, contraintesAff);

        // Panneau Itineraire : selection depart/arrivee + bouton + resultats
        JPanel panneauItineraire = new JPanel(new GridBagLayout());
        panneauItineraire.setBorder(BorderFactory.createTitledBorder("Itineraire"));
        GridBagConstraints gciti = new GridBagConstraints() ;
        
        
        JPanel panneauSel = new JPanel(new GridBagLayout());
        GridBagConstraints gcSel = new GridBagConstraints();
        gcSel.insets = new Insets(2, 4, 2, 4);

        cbDepart  = new JComboBox<>();
        cbArrivee = new JComboBox<>();


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

        btCalculerItineraire = new JButton("Calculer les chemins");
        btCalculerItineraire.setEnabled(false);

        zoneResultat = new JTextArea(3,1);
        
        // Ligne 0 : panneauSel (combos depart/arrivee)
        gciti.gridx = 0;
        gciti.gridy = 0;
        gciti.fill = GridBagConstraints.HORIZONTAL;
        gciti.weightx = 1.0;
        gciti.weighty = 0;
        gciti.insets = new Insets(2, 2, 2, 2);
        panneauItineraire.add(panneauSel, gciti);

        // Ligne 1 : bouton centre, taille naturelle
        gciti.gridy = 1;
        gciti.gridx = 0 ;
        gciti.fill = GridBagConstraints.BOTH;
        gciti.anchor = GridBagConstraints.CENTER;
        gciti.weightx = 0;
        panneauItineraire.add(btCalculerItineraire, gciti);

        // Ligne 2 : zone resultats qui prend l'espace restant
        gciti.gridy = 2;
        gciti.fill = GridBagConstraints.BOTH;
        gciti.weightx = 1.0;
        gciti.weighty = 1.0;
        panneauItineraire.add(new JScrollPane(zoneResultat), gciti);

        // Empilement vertical : Statistiques, Affichage, Itineraire
        JPanel panneauDroite = new JPanel(new GridBagLayout());
        GridBagConstraints gbcDroite = new GridBagConstraints();
        gbcDroite.gridx = 0;
        gbcDroite.fill = GridBagConstraints.HORIZONTAL;
        gbcDroite.weightx = 1.0;
        gbcDroite.insets = new Insets(2, 2, 2, 2);

        gbcDroite.gridy = 0;
        panneauDroite.add(panneauStats, gbcDroite);
        gbcDroite.gridy = 1;
        panneauDroite.add(panneauAffichage, gbcDroite);
        gbcDroite.gridy = 2;
        gbcDroite.weighty = 1.0;
        gbcDroite.fill = GridBagConstraints.BOTH;
        panneauDroite.add(panneauItineraire, gbcDroite);
        
        
        // Question 6 IHM
        
        JPanel panneauTournee = new JPanel(new GridBagLayout()) ;
        panneauTournee.setBorder(BorderFactory.createTitledBorder("Tournée de véhicule"));
        GridBagConstraints gct = new GridBagConstraints() ;
        gct.insets = new Insets(5, 2, 5, 2);
        gct.fill = GridBagConstraints.NONE ;
        
        
        
        JComboBox cbCamion = new JComboBox() ;
        cbCamion.addItem("Camion 1") ;
        cbCamion.addItem("Camion 2") ;
        
        gct.anchor = GridBagConstraints.LINE_START ;
        gct.gridx = 0 ;
        gct.gridy = 0 ;
        JLabel jCamion = new JLabel("Choix camion :") ;
        panneauTournee.add(jCamion,gct) ;
        
        gct.anchor = GridBagConstraints.CENTER ;
        gct.gridx = 1 ;
        gct.gridy = 0 ;
        panneauTournee.add(cbCamion, gct) ;
        
        gct.anchor = GridBagConstraints.LINE_START ;
        gct.gridx = 0 ;
        gct.gridy = 1 ;
        JLabel jCentres = new JLabel("Type centres :") ;
        panneauTournee.add(jCentres,gct) ;
        
        gct.anchor = GridBagConstraints.CENTER ;
        gct.gridx = 1 ;
        gct.gridy = 1 ;
        JComboBox cbCentres = new JComboBox() ;
        cbCentres.addItem("Tous");
        cbCentres.addItem("Centres M");
        cbCentres.addItem("Centres O");
        cbCentres.addItem("Centres N");
        panneauTournee.add(cbCentres, gct) ;
        
        gct.fill = GridBagConstraints.BOTH ;
        gct.gridx = 0 ;
        gct.gridwidth = 2 ;
        gct.gridy = 2 ;
        JButton btCalculerTournee = new JButton("Calculer Tournée");
        btCalculerTournee.setEnabled(false);
        panneauTournee.add(btCalculerTournee, gct) ;
        
        gbcDroite.gridy = 3 ;
        panneauDroite.add(panneauTournee, gbcDroite) ;
        

        lblStatut = new JLabel(" ");

        JPanel contenu = new JPanel(new BorderLayout());
        contenu.add(panneauActions, BorderLayout.NORTH);
        contenu.add(panneauGraphe,  BorderLayout.CENTER);
        contenu.add(panneauDroite,  BorderLayout.EAST);
        contenu.add(lblStatut,      BorderLayout.SOUTH);
        this.setContentPane(contenu);
        
        
 
    }

    /**
     * Branche les ecouteurs d'evenements sur les boutons et cases a cocher.
     */
    private void initActions() {
        btGenerer.addActionListener(e -> {
            afficherGraphe(this.demanderNbSommets());
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
        btCalculerItineraire.addActionListener(e -> calculerEtAfficherItineraires());

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
     * @param graphe Graphe a afficher. Si null, la methode sort immediatement.
     */
    private void afficherGraphe(Graph graphe) {
        if (graphe == null) return;
        this.grapheCourant = graphe;

        if (viewerCourant != null) {
            viewerCourant.close();
            viewerCourant = null;
        }

        viewerCourant = new Viewer(graphe, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewerCourant.enableAutoLayout();
        ViewPanel vue = viewerCourant.addDefaultView(false);
        vueCourante = vue;

        vue.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                GraphicElement elem = vueCourante.findNodeOrSpriteAt(e.getX(), e.getY());
                if (elem != null) {
                    String nodeId = elem.getId();
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        ouvrirInfosCentre(nodeId);
                    } else {
                        lblStatut.setText("Centre selectionne : " + nodeId);
                    }
                }
            }
        });

        panneauGraphe.removeAll();
        panneauGraphe.add((Component) vue, BorderLayout.CENTER);
        panneauGraphe.revalidate();
        panneauGraphe.repaint();

        majCompteurs();
        appliquerPreferencesAffichage();
        populerCombos();
        btRoutesRisquees.setEnabled(true);
        lblStatut.setText("Graphe charge : " + graphe.getNodeCount() + " sommets, " + graphe.getEdgeCount() + " routes.");
    }

    /**
     * Met a jour les trois labels de compteurs en interrogeant GenGraphe.
     */
    private void majCompteurs() {
        if (grapheCourant != null) {
            int nbM = GenGraphe.compterSommetsParType(grapheCourant, "maternite");
            int nbO = GenGraphe.compterSommetsParType(grapheCourant, "bloc");
            int nbN = GenGraphe.compterSommetsParType(grapheCourant, "centre");
            lblNbMaternite.setText("Maternites (M) : " + nbM);
            lblNbBloc.setText("Blocs operatoires (O) : " + nbO);
            lblNbCentre.setText("Centres de nutrition (N) : " + nbN);
        }
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
            JOptionPane.showMessageDialog(this, "Valeur invalide : merci de saisir un nombre decimal.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (seuil <= 0 || seuil > 1) {
            JOptionPane.showMessageDialog(this, "Le seuil doit etre dans l'intervalle ]0 ; 1].", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int nbRisquees = GenGraphe.marquerRoutesRisquees(grapheCourant, seuil);
        lblStatut.setText("Routes risquees (fiabilite < " + seuil + ") : " + nbRisquees + " sur " + grapheCourant.getEdgeCount());
    }

    /**
     * Repropage les preferences d'affichage cochees vers le graphe courant.
     * Lue chaque fois qu'une case a cocher du panneau Affichage est modifiee.
     */
    private void appliquerPreferencesAffichage() {
        if (grapheCourant != null) {
            GenGraphe.majAffichage(grapheCourant,
                    cbFiabilite.isSelected(),
                    cbDistance.isSelected(),
                    cbDuree.isSelected(),
                    cbLabelsSommets.isSelected());
        }
    }

    /**
     * Affiche une boite de dialogue demandant le nombre de sommets puis construit
     * un graphe aleatoire de cette taille via GenGraphe.
     * @return Le graphe genere, ou null si l'utilisateur annule ou saisit une valeur invalide
     */
    private Graph demanderNbSommets() {
        String saisie = JOptionPane.showInputDialog(this,
                "Choisir le nombre de centres de la carte\n",
                "Générer le graphe",
                JOptionPane.QUESTION_MESSAGE);
        if (saisie == null) return null;

        int nbSommets;
        try {
            nbSommets = Integer.parseInt(saisie.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valeur invalide : merci de saisir un nombre entier.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (nbSommets < 0) {
            JOptionPane.showMessageDialog(this, "Le nombre de sommets doit etre superieur a 0.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        grapheCourant = GenGraphe.construireGrapheAleatoire(nbSommets);
        lblStatut.setText("Graphe charge : " + nbSommets + " sommets, " + grapheCourant.getEdgeCount() + " routes.");
        return grapheCourant;
    }

    /**
     * Remplit les combo boxes depart/arrivee avec les ids de tous les sommets du graphe courant.
     * Active le bouton de calcul une fois rempli.
     */
    private void populerCombos() {
        cbDepart.removeAllItems();
        cbArrivee.removeAllItems();
        for (Node n : grapheCourant) {
            cbDepart.addItem(n.getId());
            cbArrivee.addItem(n.getId());
        }
        btCalculerItineraire.setEnabled(true);
        zoneResultat.setText("");
    }

    /**
     * Retire le surlignage (ui.style) de toutes les aretes du graphe courant.
     * Appele avant chaque nouveau calcul d'itineraire.
     */
    private void reinitialiserSurlignageChemin() {
        if (grapheCourant != null) {
            for (Edge e : grapheCourant.getEachEdge()) {
                e.setAttribute("ui.style", "fill-color: black; size: 1px;");
            }
        }
    }

    /**
     * Convertit un Path GraphStream en chaine lisible "S1 -> S3 -> S5".
     * @param chemin Path a convertir (non null)
     * @return Chaine representant la sequence de sommets
     */
    private String cheminToString(Path chemin) {
        String sb = "";
        for (Node n : chemin.getNodePath()) {
            if (!sb.equals("")) {
           
                sb = sb + " -> ";
            }
            sb= sb + n.getId();
        }
        return sb;
    }

    /**
     * Lance les deux calculs Dijkstra (duree minimale + chemin sur) entre les centres
     * selectionnes dans les combo boxes, surligne les chemins sur le graphe et
     * affiche les resultats dans zoneResultat.
     * Chemin 1 (duree min) surligne en orange, chemin 2 (sur) en violet.
     */
    private void calculerEtAfficherItineraires() {
        if (grapheCourant != null) {
            String idDepart  = (String) cbDepart.getSelectedItem();
            String idArrivee = (String) cbArrivee.getSelectedItem();

            if (idDepart == null || idArrivee == null || idDepart.equals(idArrivee)) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez selectionner deux centres differents.",
                        "Saisie invalide", JOptionPane.WARNING_MESSAGE);
            } else {
                // Calculer les deux chemins
                Path chemin1 = GenGraphe.plusCourtChemin(grapheCourant, idDepart, idArrivee);
                Path chemin2 = GenGraphe.cheminPlusSur(grapheCourant, idDepart, idArrivee);
                String resultat = "";

                // Remettre a zero PUIS colorier
                reinitialiserSurlignageChemin();

                if (chemin2 != null && !chemin2.empty()) {
                    for (Edge e : chemin2.getEdgeSet()) {
                        e.setAttribute("ui.style", "fill-color: rgb(148,0,211); size: 4px;");
                    }
                }

                if (chemin1 != null && !chemin1.empty()) {
                    for (Edge e : chemin1.getEdgeSet()) {
                        e.setAttribute("ui.style", "fill-color: rgb(255,140,0); size: 4px;");
                    }
                }

                // Texte : chemin1 en premier
                if (chemin1 == null || chemin1.empty()) {
                    resultat = resultat + "=== Chemin court (duree) ===\nAucun chemin\n\n";
                } else {
                    double totalDuree = 0;
                    for (Edge e : chemin1.getEdgeSet()) {
                        totalDuree += (double) e.getAttribute("duree");
                    }
                    resultat = resultat + "=== Chemin court (duree) ===\n";
                    resultat = resultat + cheminToString(chemin1) + "\n";
                    resultat = resultat + "Duree totale : " + String.format("%.0f", totalDuree) + " min\n\n";
                }

                if (chemin2 == null || chemin2.empty()) {
                    resultat = resultat + "=== Chemin sur ===\nAucun chemin (routes peu fiables bloquees)";
                } else {
                    double totalEstimee = 0;
                    for (Edge e : chemin2.getEdgeSet()) {
                        double duree     = (double) e.getAttribute("duree");
                        double fiabilite = (double) e.getAttribute("fiabilite");
                        totalEstimee += duree * (20 - fiabilite * 10) / 10.0;
                    }
                    resultat = resultat + "=== Chemin sur ===\n";
                    resultat = resultat + cheminToString(chemin2) + "\n";
                    resultat = resultat + "Duree estimee : " + String.format("%.0f", totalEstimee) + " min";
                }

                zoneResultat.setText(resultat);
                lblStatut.setText("Itineraire calcule : " + idDepart + " -> " + idArrivee);
            }
        }
    }

    /**
     * Ouvre une boite de dialogue avec les informations du centre clique (clic droit).
     * A completer pour la Q5 : distances, plus proches centres par type, etc.
     * @param nodeId Identifiant du sommet clique (ex: "S1")
     */
    private void ouvrirInfosCentre(String nodeId) {
        String type = (String) grapheCourant.getNode(nodeId).getAttribute("type");
        JOptionPane.showMessageDialog(this,
                "Centre : " + nodeId + "\nType : " + type,
                "Informations sur le centre",
                JOptionPane.INFORMATION_MESSAGE);
    }
}