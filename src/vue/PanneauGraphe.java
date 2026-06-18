package vue;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

/**
 * Panneau central : accueille la vue GraphStream du graphe courant.
 * Gere la creation/fermeture du Viewer et la detection des clics sur les sommets
 * (clic gauche : selection ; clic droit : ouverture de la fenetre d'infos).
 */
public class PanneauGraphe extends JPanel {

    /** Reference vers la fenetre orchestratrice. */
    private final FenetrePrincipale parent;

    /** Viewer GraphStream actif (null tant qu'aucun graphe n'a ete affiche). */
    private Viewer viewerCourant;

    /** Vue active utilisee pour la detection des clics. */
    private ViewPanel vueCourante;

    /**
     * Construit le panneau et y affiche un placeholder texte.
     * @param parent Fenetre orchestratrice (non null)
     */
    public PanneauGraphe(FenetrePrincipale parent) {
        super(new BorderLayout());
        this.parent = parent;
        this.setBorder(BorderFactory.createTitledBorder("Carte du district"));
        this.add(new JLabel("Aucun graphe charge.", SwingConstants.CENTER), BorderLayout.CENTER);
    }

    /**
     * Remplace l'affichage courant par la vue du graphe passe en parametre.
     * Ferme le viewer precedent s'il y en avait un.
     * @param graphe Graphe a afficher (non null)
     */
    public void afficher(Graph graphe) {
        if (graphe == null) return;

        if (viewerCourant != null) {
            viewerCourant.close();
            viewerCourant = null;
        }

        viewerCourant = new Viewer(graphe, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewerCourant.enableAutoLayout();
        ViewPanel vue = viewerCourant.addDefaultView(false);
        vueCourante = vue;

        vue.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Graph g = parent.getGrapheCourant();
                if (g == null) return;
                GraphicElement element = vueCourante.findNodeOrSpriteAt(e.getX(), e.getY());
                if (element == null) return;

                Node noeud = g.getNode(element.getId());
                if (noeud == null) return;

                for (Node autre : g) {
                    autre.removeAttribute("ui.clicked");
                }
                noeud.setAttribute("ui.clicked");

                if (SwingUtilities.isRightMouseButton(e)) {
                    new FenetreInfosCentre(parent, g, noeud.getId());
                } else {
                    parent.setStatut("Centre selectionne : " + noeud.getId());
                }
            }
        });

        this.removeAll();
        this.add((Component) vue, BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }
}
