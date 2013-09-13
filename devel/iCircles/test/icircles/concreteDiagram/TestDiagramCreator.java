package icircles.concreteDiagram;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestDiagramCreator {

    @Test
    public void testAngleIterator() {
        // I don't know why these values from AngleIterator are in this order,
        // but this is what they evaluate to.
        double [] expected = {0.0,
                3.141592653589793,
                1.5707963267948966,
                4.71238898038469,
                0.7853981633974483,
                2.356194490192345,
                3.9269908169872414,
                5.497787143782138,
                0.39269908169872414,
                1.1780972450961724,
                1.9634954084936207,
                2.748893571891069,
                3.5342917352885173,
                4.319689898685965,
                5.105088062083414,
                5.890486225480862};
        AngleIterator it = new AngleIterator();
        for(int i =0; (i < expected.length) && it.hasNext(); i++) {
            assertEquals(expected[i], it.nextAngle(), 0.000001);
        }
    }

}
