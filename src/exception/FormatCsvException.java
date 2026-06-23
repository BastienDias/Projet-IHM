package exception;

/**
 * Exception levee lorsqu'un fichier CSV de graphe ne respecte pas le format attendu.
 * Le format attendu par ligne est : id;type;cellule;cellule;...
 * ou chaque cellule vaut "0" (pas de route) ou "fiabilite,distance,duree".
 * @author Sinjat, Battah, Dias
 */
public class FormatCsvException extends Exception {

    /**
     * @param message Description de l'anomalie (numero de ligne et cause)
     */
    public FormatCsvException(String message) {
        super(message);
    }
}