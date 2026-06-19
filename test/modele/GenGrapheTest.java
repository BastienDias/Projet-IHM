/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package modele;

import org.graphstream.graph.Graph;
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
public class GenGrapheTest {
    
    public GenGrapheTest() {
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
     * Test of construireGrapheAleatoire method, of class GenGraphe.
     */
    @Test
    public void testConstruireGrapheAleatoire() {
        System.out.println("construireGrapheAleatoire");
        int nbSommets = 0;
        Graph expResult = null;
        Graph result = GenGraphe.construireGrapheAleatoire(nbSommets);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of chargerGraphe method, of class GenGraphe.
     */
    @Test
    public void testChargerGraphe() throws Exception {
        System.out.println("chargerGraphe");
        String cheminFichier = "";
        Graph expResult = null;
        Graph result = GenGraphe.chargerGraphe(cheminFichier);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of compterSommetsParType method, of class GenGraphe.
     */
    @Test
    public void testCompterSommetsParType() {
        System.out.println("compterSommetsParType");
        Graph graphe = null;
        String type = "";
        int expResult = 0;
        int result = GenGraphe.compterSommetsParType(graphe, type);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of marquerRoutesRisquees method, of class GenGraphe.
     */
    @Test
    public void testMarquerRoutesRisquees() {
        System.out.println("marquerRoutesRisquees");
        Graph graphe = null;
        double seuil = 0.0;
        int expResult = 0;
        int result = GenGraphe.marquerRoutesRisquees(graphe, seuil);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of majAffichage method, of class GenGraphe.
     */
    @Test
    public void testMajAffichage() {
        System.out.println("majAffichage");
        Graph graphe = null;
        boolean afficherFiabilite = false;
        boolean afficherDistance = false;
        boolean afficherDuree = false;
        boolean afficherLabelsSommets = false;
        GenGraphe.majAffichage(graphe, afficherFiabilite, afficherDistance, afficherDuree, afficherLabelsSommets);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of plusCourtChemin method, of class GenGraphe.
     */
    @Test
    public void testPlusCourtChemin() {
        System.out.println("plusCourtChemin");
        Graph graphe = null;
        String idDebut = "";
        String idFin = "";
        Path expResult = null;
        Path result = GenGraphe.plusCourtChemin(graphe, idDebut, idFin);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of cheminPlusSur method, of class GenGraphe.
     */
    @Test
    public void testCheminPlusSur() {
        System.out.println("cheminPlusSur");
        Graph graphe = null;
        String idDebut = "";
        String idFin = "";
        Path expResult = null;
        Path result = GenGraphe.cheminPlusSur(graphe, idDebut, idFin);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
