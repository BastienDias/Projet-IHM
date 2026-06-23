package modele;

import org.graphstream.graph.Path;

/**
 * Conteneur regroupant les deux structures pre-calculees utiles a
 * l'optimisation d'une tournee : la matrice des durees de plus court chemin
 * entre chaque paire de centres, et les Path GraphStream correspondants.
 * @author Sinjat, Battah, Dias
 */
public class DonneesCentres {

    /** Matrice n*n des durees de plus court chemin centre a centre. */
    public final double[][] durees;

    /** Tableau n*n des Path GraphStream centre a centre (null si non connecte). */
    public final Path[][] chemins;

    /**
     * Construit un conteneur a partir des deux tableaux pre-calcules.
     * @param durees Matrice des durees de plus court chemin
     * @param chemins Tableau des Path GraphStream correspondants
     */
    public DonneesCentres(double[][] durees, Path[][] chemins) {
        this.durees = durees;
        this.chemins = chemins;
    }
}