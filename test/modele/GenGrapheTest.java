package modele;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Classe de tests unitaires pour la classe GenGraphe.
 * Verifie la generation aleatoire, le comptage par type, le marquage des
 * routes risquees et les calculs de plus court chemin (Dijkstra duree et
 * chemin le plus sur). Les methodes lisant un fichier ne sont pas testees ici.
* @author Sinjat, Battah, Dias
 */
public class GenGrapheTest {

    public GenGrapheTest() {
    }

    /**
     * Construit a la main un petit graphe de 4 sommets servant de reference.
     * Le sommet d'indice 0 (M1) est le depot. Les attributs type, numeroSommet,
     * fiabilite, distance et duree sont poses manuellement pour que les resultats
     * des calculs soient previsibles.
     * Aretes (fiabilite ; distance ; duree) :
     *   M1--O1 : 0.8 ; 20 ; 30
     *   O1--N1 : 0.6 ; 15 ; 25
     *   M1--N1 : 0.9 ; 50 ; 80   (chemin direct plus long en duree)
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

    /**
     * Test : la generation aleatoire produit exactement le nombre de sommets demande.
     */
    @Test
    public void testConstruireGrapheNbSommets() {
        Graph g = GenGraphe.construireGrapheAleatoire(10);
        assertEquals(10, g.getNodeCount());
    }

    /**
     * Test : chaque sommet genere possede un type valide (M, O ou N).
     */
    @Test
    public void testConstruireGrapheTypesValides() {
        Graph g = GenGraphe.construireGrapheAleatoire(15);
        for (Node n : g) {
            String type = (String) n.getAttribute("type");
            assertTrue(type.equals("M") || type.equals("O") || type.equals("N"));
        }
    }

    /**
     * Test : le graphe genere est connexe (au moins nbSommets - 1 aretes,
     * car chaque nouveau sommet est relie a un sommet deja place).
     */
    @Test
    public void testConstruireGrapheConnexe() {
        Graph g = GenGraphe.construireGrapheAleatoire(8);
        assertTrue(g.getEdgeCount() >= g.getNodeCount() - 1);
    }

    /**
     * Test : chaque arete generee porte bien les trois attributs metier.
     */
    @Test
    public void testConstruireGrapheAttributsAretes() {
        Graph g = GenGraphe.construireGrapheAleatoire(8);
        for (Edge e : g.getEachEdge()) {
            assertTrue(e.hasAttribute("fiabilite"));
            assertTrue(e.hasAttribute("distance"));
            assertTrue(e.hasAttribute("duree"));
        }
    }

    /**
     * Test : la fiabilite generee reste dans l'intervalle [0.2 ; 1.0].
     */
    @Test
    public void testConstruireGrapheFiabiliteDansIntervalle() {
        Graph g = GenGraphe.construireGrapheAleatoire(20);
        for (Edge e : g.getEachEdge()) {
            double f = (double) e.getAttribute("fiabilite");
            assertTrue(f >= 0.2);
            assertTrue(f <= 1.0);
        }
    }

    /**
     * Test : le comptage par type renvoie le bon nombre pour chaque categorie.
     */
    @Test
    public void testCompterSommetsParType() {
        Graph g = creerGrapheTest();
        assertEquals(2, GenGraphe.compterSommetsParType(g, "M"));
        assertEquals(1, GenGraphe.compterSommetsParType(g, "O"));
        assertEquals(1, GenGraphe.compterSommetsParType(g, "N"));
    }

    /**
     * Test : le comptage d'un type absent renvoie 0.
     */
    @Test
    public void testCompterSommetsParTypeAbsent() {
        Graph g = creerGrapheTest();
        assertEquals(0, GenGraphe.compterSommetsParType(g, "Z"));
    }

    /**
     * Test : les routes sous le seuil sont comptees et marquees "risquee",
     * les autres ne le sont pas.
     */
    @Test
    public void testMarquerRoutesRisquees() {
        Graph g = creerGrapheTest();
        // Seuil 0.7 : seule O1--N1 (0.6) est strictement inferieure.
        int nb = GenGraphe.marquerRoutesRisquees(g, 0.7);
        assertEquals(1, nb);
        assertEquals("risquee", g.getEdge("O1--N1").getAttribute("ui.class"));
        assertFalse(g.getEdge("M1--O1").hasAttribute("ui.class"));
    }

    /**
     * Test : avec un seuil tres bas, aucune route n'est risquee.
     */
    @Test
    public void testMarquerRoutesRisqueesAucune() {
        Graph g = creerGrapheTest();
        int nb = GenGraphe.marquerRoutesRisquees(g, 0.1);
        assertEquals(0, nb);
    }

    /**
     * Test : Dijkstra en duree prefere le chemin indirect quand il est plus court.
     * M1 -> N1 direct = 80 min ; via O1 = 30 + 25 = 55 min (choisi).
     */
    @Test
    public void testPlusCourtCheminDuree() {
        Graph g = creerGrapheTest();
        org.graphstream.graph.Path chemin = GenGraphe.plusCourtChemin(g, "M1", "N1");
        double duree = 0;
        for (Edge e : chemin.getEdgePath()) {
            duree += (double) e.getAttribute("duree");
        }
        assertEquals(55.0, duree, 0.001);
        assertEquals(3, chemin.getNodeCount());
    }

    /**
     * Test : le plus court chemin d'un sommet vers lui-meme est vide (aucune arete).
     */
    @Test
    public void testPlusCourtCheminVersSoiMeme() {
        Graph g = creerGrapheTest();
        org.graphstream.graph.Path chemin = GenGraphe.plusCourtChemin(g, "M1", "M1");
        assertEquals(0, chemin.getEdgeCount());
    }

    /**
     * Test : le chemin le plus sur evite une route de fiabilite < 0.25.
     * On rend M1--N1 impraticable (0.2) : le chemin doit alors passer par O1.
     */
    @Test
    public void testCheminPlusSurEviteRouteRisquee() {
        Graph g = creerGrapheTest();
        g.getEdge("M1--N1").setAttribute("fiabilite", 0.2); // route bloquee

        org.graphstream.graph.Path chemin = GenGraphe.cheminPlusSur(g, "M1", "N1");
        assertFalse(chemin.getEdgePath().contains(g.getEdge("M1--N1")));
        assertEquals(3, chemin.getNodeCount());
    }

    /**
     * Test : cheminPlusSur pose bien l'attribut temporaire dureeEstimee sur les aretes.
     */
    @Test
    public void testCheminPlusSurPoseDureeEstimee() {
        Graph g = creerGrapheTest();
        GenGraphe.cheminPlusSur(g, "M1", "M2");
        for (Edge e : g.getEachEdge()) {
            assertTrue(e.hasAttribute("dureeEstimee"));
        }
    }
}