package modele;

import java.util.ArrayList;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Classe de tests unitaires pour la classe Algo.
 * Verifie le filtrage des sommets, les tournees gloutonnes (totale, filtree,
 * deux camions), le calcul de duree d'un chemin et l'ordre des centres visites.
 * Les methodes d'export CSV ne sont pas testees ici.
* @author Sinjat, Battah, Dias
 */
public class AlgoTest {

    public AlgoTest() {
    }

    /**
     * Construit a la main un petit graphe de 4 sommets servant de reference.
     * Le sommet d'indice 0 (M1) est le depot, exclu des tournees.
     * Aretes (fiabilite ; distance ; duree) :
     *   M1--O1 : 0.8 ; 20 ; 30
     *   O1--N1 : 0.6 ; 15 ; 25
     *   M1--N1 : 0.9 ; 50 ; 80
     *   N1--M2 : 0.7 ; 10 ; 40
     * @return Le graphe de test pret a l'emploi
     */
    private Graph creerGrapheTest() {
        Graph g = new SingleGraph("test");

        Node m1 = g.addNode("M1");
        m1.setAttribute("type", "M");
        m1.setAttribute("numeroSommet", 1);

        Node o1 = g.addNode("O1");
        o1.setAttribute("type", "O");
        o1.setAttribute("numeroSommet", 2);

        Node n1 = g.addNode("N1");
        n1.setAttribute("type", "N");
        n1.setAttribute("numeroSommet", 3);

        Node m2 = g.addNode("M2");
        m2.setAttribute("type", "M");
        m2.setAttribute("numeroSommet", 4);

        ajouterArete(g, "M1--O1", "M1", "O1", 0.8, 20, 30);
        ajouterArete(g, "O1--N1", "O1", "N1", 0.6, 15, 25);
        ajouterArete(g, "M1--N1", "M1", "N1", 0.9, 50, 80);
        ajouterArete(g, "N1--M2", "N1", "M2", 0.7, 10, 40);

        return g;
    }

    /**
     * Ajoute une arete valuee au graphe de test.
     * @param g Graphe a completer
     * @param id Identifiant de l'arete
     * @param a Identifiant du premier sommet
     * @param b Identifiant du second sommet
     * @param fiabilite Fiabilite de la route (0.0 a 1.0)
     * @param distance Distance de la route en km
     * @param duree Duree de la route en minutes
     */
    private void ajouterArete(Graph g, String id, String a, String b, double fiabilite, double distance, double duree) {
        Edge e = g.addEdge(id, a, b);
        e.setAttribute("fiabilite", fiabilite);
        e.setAttribute("distance", distance);
        e.setAttribute("duree", duree);
    }

    // ---------- filtrageSommet ----------

    /**
     * Test : le filtrage par type "M" exclut le depot (M1 a l'indice 0)
     * et ne garde que les autres maternites (M2).
     */
    @Test
    public void testFiltrageSommetTypeM() {
        Graph g = creerGrapheTest();
        ArrayList<Node> resultat = Algo.filtrageSommet(g, "M");
        assertEquals(1, resultat.size());
        assertEquals("M2", resultat.get(0).getId());
    }

    /**
     * Test : le filtrage "Tous" retient tous les sommets sauf le depot.
     */
    @Test
    public void testFiltrageSommetTous() {
        Graph g = creerGrapheTest();
        ArrayList<Node> resultat = Algo.filtrageSommet(g, "Tous");
        assertEquals(3, resultat.size());
        assertFalse(resultat.contains(g.getNode(0)));
    }

    /**
     * Test : le filtrage avec un type null renvoie une liste vide.
     */
    @Test
    public void testFiltrageSommetNull() {
        Graph g = creerGrapheTest();
        ArrayList<Node> resultat = Algo.filtrageSommet(g, null);
        assertTrue(resultat.isEmpty());
    }

    /**
     * Test : le filtrage par un type absent (hors depot) renvoie une liste vide.
     */
    @Test
    public void testFiltrageSommetTypeAbsent() {
        Graph g = creerGrapheTest();
        ArrayList<Node> resultat = Algo.filtrageSommet(g, "Z");
        assertTrue(resultat.isEmpty());
    }

    // ---------- dureeChemin ----------

    /**
     * Test : la duree d'un chemin est la somme des durees de ses aretes.
     * M1 -> O1 -> N1 : 30 + 25 = 55.
     */
    @Test
    public void testDureeChemin() {
        Graph g = creerGrapheTest();
        org.graphstream.graph.Path p = GenGraphe.plusCourtChemin(g, "M1", "N1");
        assertEquals(55.0, Algo.dureeChemin(p), 0.001);
    }

    /**
     * Test : la duree d'un chemin null vaut 0.
     */
    @Test
    public void testDureeCheminNull() {
        assertEquals(0.0, Algo.dureeChemin(null), 0.001);
    }

    /**
     * Test : la duree d'un chemin vide (un seul sommet) vaut 0.
     */
    @Test
    public void testDureeCheminVide() {
        Graph g = creerGrapheTest();
        org.graphstream.graph.Path p = GenGraphe.plusCourtChemin(g, "M1", "M1");
        assertEquals(0.0, Algo.dureeChemin(p), 0.001);
    }
    
    /**
     * Test : la tournee totale part du depot et visite tous les autres sommets.
     */
    @Test
    public void testTourneeLivraisonTotale() {
        Graph g = creerGrapheTest();
        org.graphstream.graph.Path tournee = Algo.tourneeLivraisonTotale(g);
        assertEquals(g.getNode(0), tournee.getRoot());
        assertTrue(tournee.getNodePath().contains(g.getNode("O1")));
        assertTrue(tournee.getNodePath().contains(g.getNode("N1")));
        assertTrue(tournee.getNodePath().contains(g.getNode("M2")));
    }

    /**
     * Test : la tournee filtree sur "M" visite la maternite cible (M2)
     * en partant du depot.
     */
    @Test
    public void testTourneeLivraisonFiltrage() {
        Graph g = creerGrapheTest();
        org.graphstream.graph.Path tournee = Algo.tourneeLivraisonFiltrage(g, "M");
        assertEquals(g.getNode(0), tournee.getRoot());
        assertTrue(tournee.getNodePath().contains(g.getNode("M2")));
    }

    /**
     * Test : l'ordre des centres visites commence par le depot et ne contient
     * aucun doublon.
     */
    @Test
    public void testOrdreCentresVisites() {
        Graph g = creerGrapheTest();
        org.graphstream.graph.Path tournee = Algo.tourneeLivraisonTotale(g);
        ArrayList<Node> ordre = Algo.ordreCentresVisites(g, tournee);

        assertEquals(g.getNode(0), ordre.get(0));
        for (int i = 0; i < ordre.size(); i++) {
            for (int j = i + 1; j < ordre.size(); j++) {
                assertNotEquals(ordre.get(i), ordre.get(j));
            }
        }
    }

    /**
     * Test : la tournee a deux camions renvoie bien deux chemins,
     * chacun enracine sur le depot.
     */
    @Test
    public void testTournee2CamionsDeuxChemins() {
        Graph g = creerGrapheTest();
        org.graphstream.graph.Path[] resultat = Algo.tournee2Camions(g, "Tous");
        assertEquals(2, resultat.length);
        assertEquals(g.getNode(0), resultat[0].getRoot());
        assertEquals(g.getNode(0), resultat[1].getRoot());
    }

    /**
     * Test : entre les deux camions, tous les dispensaires a livrer sont couverts.
     */
    @Test
    public void testTournee2CamionsCouvreTousLesCentres() {
        Graph g = creerGrapheTest();
        org.graphstream.graph.Path[] resultat = Algo.tournee2Camions(g, "Tous");

        ArrayList<Node> vus = new ArrayList<>();
        for (Node n : resultat[0].getNodePath()) {
            if (!vus.contains(n)) vus.add(n);
        }
        for (Node n : resultat[1].getNodePath()) {
            if (!vus.contains(n)) vus.add(n);
        }
        assertTrue(vus.contains(g.getNode("O1")));
        assertTrue(vus.contains(g.getNode("N1")));
        assertTrue(vus.contains(g.getNode("M2")));
    }
}