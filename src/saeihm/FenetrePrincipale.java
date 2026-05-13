package saeihm;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class FenetrePrincipale extends JFrame{
    JButton btGenerer, btCharger;
    JLabel lblBienvenue, lblChoix;
    public FenetrePrincipale(){
        super("Medic'In");
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.init_composants();
        this.setResizable(false);
        this.pack();
    }
    
    private void init_composants(){
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
}
