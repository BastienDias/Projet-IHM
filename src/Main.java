
import modele.GenGraphe;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import vue.FenetrePrincipale;

/**
 * Point d'entree de l'application Medic'In.
 * Cette classe ne fait qu'instancier et afficher la fenetre principale ;
 * toute la logique applicative est deleguee aux paquets {@code modele} et {@code vue}.
 *
 * <p>Remarque : une seconde classe {@code vue.Main} joue le meme role. Selon la
 * configuration de lancement du projet (classe principale choisie dans l'IDE),
 * l'un ou l'autre point d'entree est utilise ; les deux sont equivalents.</p>
 *
 * @author Sinjat, Battah, Dias
 */
public class Main {

    /**
     * Lance la fenetre principale de l'application.
     * @param args Arguments de la ligne de commande (non utilises)
     */
    public static void main(String[] args) {
        FenetrePrincipale fp = new FenetrePrincipale();
    }
}
