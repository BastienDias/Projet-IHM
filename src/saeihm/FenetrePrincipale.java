package saeihm;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.graphstream.graph.Graph;

public class FenetrePrincipale extends JFrame{
    JButton btGenerer, btCharger;
    JLabel lblBienvenue, lblChoix;
    public FenetrePrincipale(){
        super("Medic'In");
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        initComposants();
        initActions();
        
        this.setResizable(false);
        this.pack();
    }
    
    private void initComposants(){
        JPanel panneauLabel = new JPanel();
        panneauLabel.setLayout(new BorderLayout(5, 5));
        lblBienvenue = new JLabel("Bienvenue sur le menu principale de Medic'In.");
        lblBienvenue.setHorizontalAlignment(SwingConstants.CENTER);
        panneauLabel.add(lblBienvenue, BorderLayout.NORTH);
        lblChoix = new JLabel("Choisissez la méthode pour obtenir votre carte :");
        panneauLabel.add(lblChoix, BorderLayout.CENTER);
        
        JPanel panneauBouton = new JPanel();
        panneauBouton.setLayout(new FlowLayout());
        btGenerer = new JButton("Générer un graphe aléatoire");
        panneauBouton.add(btGenerer);
        btCharger = new JButton("Charger un graphe");
        panneauBouton.add(btCharger);
        
        JPanel panneauGlobal = new JPanel();
        panneauGlobal.setLayout(new BorderLayout());
        panneauGlobal.add(panneauLabel, BorderLayout.NORTH);
        panneauGlobal.add(panneauBouton, BorderLayout.CENTER);
        this.setContentPane(panneauGlobal);
    }
    
    private void initActions() {

        /**
         * Listener associé au bouton "Générer un graphe aléatoire".
         * Génère un graphe de 10 sommets via GenGraphe et l'affiche dans GraphStream.
         */
        btGenerer.addActionListener(e -> {
            Graph carte = GenGraphe.construireGrapheAleatoire(10);
            carte.display();
        });

        /**
         * Listener associé au bouton "Charger un graphe".
         * Ouvre une boîte de dialogue de sélection de fichier filtrée sur les .csv,
         * puis charge et affiche le graphe correspondant via GenGraphe.chargerGraphe().
         */
        btCharger.addActionListener(e -> {
            JFileChooser selecteur = new JFileChooser();
            selecteur.setFileFilter(new FileNameExtensionFilter("Fichiers CSV (*.csv)", "csv"));
            int resultat = selecteur.showOpenDialog(this);
            if (resultat == JFileChooser.APPROVE_OPTION) {
                String chemin = selecteur.getSelectedFile().getAbsolutePath();
                Graph grapheCharge = GenGraphe.chargerGraphe(chemin);
                grapheCharge.display();
            }
        });
    }
}
