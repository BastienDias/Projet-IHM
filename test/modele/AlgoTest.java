/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package modele;

import java.util.ArrayList;
import java.util.List;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author p2501289
 */
public class AlgoTest {
    
    public AlgoTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of filtrageSommet method, of class Algo.
     */
    @Test
    public void testFiltrageSommet() {
        System.out.println("filtrageSommet");
        Graph g = null;
        String typerechecher = "";
        ArrayList<Node> expResult = null;
        ArrayList<Node> result = Algo.filtrageSommet(g, typerechecher);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of tourneeLivraisonTotale method, of class Algo.
     */
    @Test
    public void testTourneeLivraisonTotale() {
        System.out.println("tourneeLivraisonTotale");
        Graph g = null;
        Path expResult = null;
        Path result = Algo.tourneeLivraisonTotale(g);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of tourneeLivraisonFiltrage method, of class Algo.
     */
    @Test
    public void testTourneeLivraisonFiltrage() {
        System.out.println("tourneeLivraisonFiltrage");
        Graph g = null;
        String type = "";
        Path expResult = null;
        Path result = Algo.tourneeLivraisonFiltrage(g, type);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of dureeChemin method, of class Algo.
     */
    @Test
    public void testDureeChemin() {
        System.out.println("dureeChemin");
        Path p = null;
        double expResult = 0.0;
        double result = Algo.dureeChemin(p);
        assertEquals(expResult, result, 0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of exporterResultat1Camion method, of class Algo.
     */
    @Test
    public void testExporterResultat1Camion() {
        System.out.println("exporterResultat1Camion");
        String nomGraphe = "";
        double dureeGlouton = 0.0;
        double meilleureDuree = 0.0;
        List<Node> ordreVisite = null;
        String nomFichier = "";
        Algo.exporterResultat1Camion(nomGraphe, dureeGlouton, meilleureDuree, ordreVisite, nomFichier);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of tournee2Camions method, of class Algo.
     */
    @Test
    public void testTournee2Camions() {
        System.out.println("tournee2Camions");
        Graph g = null;
        String type = "";
        Path[] expResult = null;
        Path[] result = Algo.tournee2Camions(g, type);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
