package vue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

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
    
    /** Indique si le mode sombre est actuellement actif (true) ou le mode clair (false). */
    boolean modeSombre = false;

    /** Couleur de fond utilisee par le mode sombre. */
    private static final Color FOND_SOMBRE = new ColorUIResource(43, 43, 43);
    /** Couleur de texte utilisee par le mode sombre. */
    private static final Color TEXTE_SOMBRE = new ColorUIResource(220, 220, 220);

    /** Couleur de fond utilisee par le mode clair. */
    private static final Color FOND_CLAIR = new ColorUIResource(238, 238, 238);
    /** Couleur de texte utilisee par le mode clair. */
    private static final Color TEXTE_CLAIR = new ColorUIResource(Color.BLACK);

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
        // Largeur figee : la zone EAST respecte la largeur preferee. La hauteur
        // est ignoree par BorderLayout.EAST (la zone est etiree verticalement).
        panneauDroite.setPreferredSize(new Dimension(360, 0));
        panneauDroite.setMinimumSize(new Dimension(360, 0));
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
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panneauDroite.add(panneauItineraire, gbc);
        gbc.gridy = 3;
        panneauDroite.add(panneauTournee, gbc);
        
        // Assemblage final
        JPanel contenu = new JPanel(new BorderLayout());
        contenu.add(panneauActions, BorderLayout.NORTH);
        contenu.add(panneauGraphe, BorderLayout.CENTER);
        contenu.add(panneauDroite, BorderLayout.EAST);
        contenu.add(lblStatut, BorderLayout.SOUTH);
        initMenu();
        this.setContentPane(contenu);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(950, 800);
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
        if (modeSombre) {
            grapheCourant.setAttribute("ui.stylesheet", "graph { fill-color: #2B2B2B; } edge { text-color: white; }");
        } 
        else {
            grapheCourant.setAttribute("ui.stylesheet", "graph { fill-color: #FFFFFF; } edge { text-color: black; }");
        }
    }
    
    /**
     * Construit et installe la barre de menus de la fenetre.
     * Deux menus sont proposes :
     * <ul>
     *   <li><b>Affichage</b> : bascule entre mode sombre et mode clair ;</li>
     *   <li><b>Parametres de la carte</b> : suppression du graphe courant et
     *       suppression du surlignage (itineraires, routes risquees).</li>
     * </ul>
     */
    private void initMenu(){
        JMenuBar menu = new JMenuBar();
        this.setJMenuBar(menu);
        JMenu affichage = new JMenu("Affichage");
        JMenu carte = new JMenu("Paramètres de la carte");
        menu.add(affichage);
        menu.add(carte);
        
        JMenuItem sombre = new JMenuItem("Mode sombre");
        sombre.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                appliquerModeSombre();
            }
        });
        JMenuItem clair = new JMenuItem("Mode clair");
        clair.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                appliquerModeNormal();
            }
        });
        affichage.add(sombre);
        affichage.add(clair);
        
        JMenuItem supprimer = new JMenuItem("Supprimer la carte");
        supprimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                grapheCourant = null;
                panneauGraphe.supprimer();
                panneauStats.reinitialiser();
                panneauItineraire.viderCombos();
                panneauActions.setBoutonsApresChargement(false);
                panneauItineraire.boutonCheminDesactiver();
                panneauTournee.setBoutonActif(false);
                lblStatut.setText(" ");
            }
        });
        JMenuItem surlignage = new JMenuItem("Supprimer le surlignage");
        surlignage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {    
                reinitialiserSurlignage();
                lblStatut.setText("Graphe charge : " + grapheCourant.getNodeCount() + " sommets, " + grapheCourant.getEdgeCount() + " routes.");
            }
        }); 
        carte.add(supprimer);
        carte.add(surlignage);
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
    * Applique les couleurs à la fenêtre pour avoir un mode sombre
    */
    private void appliquerModeSombre(){
        modeSombre = true;
        
        UIManager.put("Panel.background", FOND_SOMBRE);
        UIManager.put("Label.foreground", TEXTE_SOMBRE);
        UIManager.put("CheckBox.background", FOND_SOMBRE);
        UIManager.put("CheckBox.foreground", TEXTE_SOMBRE);
        UIManager.put("ComboBox.background", FOND_SOMBRE);
        UIManager.put("ComboBox.foreground", TEXTE_SOMBRE);
        UIManager.put("TitledBorder.titleColor", TEXTE_SOMBRE);
        UIManager.put("OptionPane.background", FOND_SOMBRE);
        UIManager.put("OptionPane.messageForeground", TEXTE_SOMBRE);
        UIManager.put("MenuBar.background", FOND_SOMBRE);
        UIManager.put("MenuBar.foreground", TEXTE_SOMBRE);
        UIManager.put("MenuItem.background", FOND_SOMBRE);
        UIManager.put("MenuItem.foreground", TEXTE_SOMBRE);
        UIManager.put("Table.background", FOND_SOMBRE);
        UIManager.put("Table.foreground", TEXTE_SOMBRE);
        UIManager.put("TableHeader.background", FOND_SOMBRE);
        UIManager.put("TableHeader.foreground", TEXTE_SOMBRE);
        UIManager.put("Viewport.background", FOND_SOMBRE);
        SwingUtilities.updateComponentTreeUI(this);
        
        if (grapheCourant != null) 
             grapheCourant.setAttribute("ui.stylesheet", "graph { fill-color: #2B2B2B; } edge { text-color: white; }");
    }    
    
    /**
     * Applique les couleurs à la fenêtre pour avoir un mode clair
    */
    private void appliquerModeNormal(){
        modeSombre = false;
        
        UIManager.put("Panel.background", FOND_CLAIR);
        UIManager.put("Label.foreground", TEXTE_CLAIR);
        UIManager.put("CheckBox.background", FOND_CLAIR);
        UIManager.put("CheckBox.foreground", TEXTE_CLAIR);
        UIManager.put("ComboBox.background", FOND_CLAIR);
        UIManager.put("ComboBox.foreground", TEXTE_CLAIR);
        UIManager.put("TitledBorder.titleColor", TEXTE_CLAIR);
        UIManager.put("OptionPane.background", FOND_CLAIR);
        UIManager.put("OptionPane.messageForeground", TEXTE_CLAIR);
        UIManager.put("MenuBar.background", FOND_CLAIR);
        UIManager.put("MenuBar.foreground", TEXTE_CLAIR);
        UIManager.put("Menu.background", FOND_CLAIR);
        UIManager.put("Menu.foreground", TEXTE_CLAIR);
        UIManager.put("MenuItem.background", FOND_CLAIR);
        UIManager.put("MenuItem.foreground", TEXTE_CLAIR);
        UIManager.put("Table.background", FOND_CLAIR);
        UIManager.put("Table.foreground", TEXTE_CLAIR);
        UIManager.put("TableHeader.background", FOND_CLAIR);
        UIManager.put("TableHeader.foreground", TEXTE_CLAIR);
        UIManager.put("Viewport.background", FOND_CLAIR);
        SwingUtilities.updateComponentTreeUI(this);
        
        if (grapheCourant != null) {
            grapheCourant.setAttribute("ui.stylesheet", "graph { fill-color: #FFFFFF; } edge { text-color: black; }");
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
