package icircles.decomposition;

import icircles.abstractDescription.AbstractCurve;
import icircles.abstractDescription.AbstractDescription;
import icircles.util.DEB;

import java.util.ArrayList;

public class DecompositionStrategyUseSortOrder extends DecompositionStrategy {

    boolean m_natural_order;

    DecompositionStrategyUseSortOrder(boolean natural_order) {

        if (DEB.level > 1) {
            System.out.println("recomposition stratgey is alphabetic");
            if (m_natural_order) {
                System.out.println("natural order");
            } else {
                System.out.println("reversed order");
            }
        }
        m_natural_order = natural_order;
    }

    void getContoursToRemove(AbstractDescription ad, ArrayList<AbstractCurve> toRemove) {
        toRemove.clear();
        if (m_natural_order) {
            toRemove.add(ad.getFirstContour());
        } else {
            toRemove.add(ad.getLastContour());
        }
    }
}
