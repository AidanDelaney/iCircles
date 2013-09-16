package icircles.concreteDiagram;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import icircles.abstractDescription.AbstractBasicRegion;
import icircles.abstractDescription.AbstractCurve;
import icircles.abstractDescription.AbstractDescription;
import icircles.concreteDiagram.*;
import icircles.input.AbstractDiagram;
import icircles.util.CannotDrawException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(value = Parameterized.class)
public class TestDiagramCreator {

    // Our TestData is converted into a TestDatum, then this is used to store 
    // the current TestDatum parameter.
    private TestDatum datum;
    private static final int CANVAS_SIZE = 200;
    // How accurate should positioning be?  4th decimal place is reasonable.
    private static final double EPSILON  = 0.0001;

    public TestDiagramCreator(TestDatum datum) {
        this.datum = datum;
    }
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
            assertEquals(expected[i], it.nextAngle(), EPSILON);
        }
    }

    /* TODO: put foot count metadata in TestDatum
    @Test
    public void testGetABRFootCount() {
        ObjectMapper        m  = new ObjectMapper();
        m.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        AbstractDiagram ad = null;
        try {
            ad = m.readValue(datum.description, AbstractDiagram.class);
        } catch (IOException e) { // JsonParseException | JsonMappingException
            e.printStackTrace();
            assertTrue(false);
        }
        DiagramCreator      dc = new DiagramCreator(ad.toAbstractDescription());

        HashMap<AbstractBasicRegion, Integer> hm = dc.getABRFootCount();
        assertEquals(hm.size(), 0);
    }*/

    @Parameters
    public static Collection<TestDatum[]> data() {
        Vector<TestDatum[]> v = new Vector<TestDatum[]>();

        for(TestDatum td : TestData.test_data) {
            v.add(new TestDatum[]{ new TestDatum(td.toJSON(), td.expected_checksum, td.expected_circles)});
        }
      return v;
    }

    @Test
    public void testAllDiagrams() {
        ObjectMapper        m  = new ObjectMapper();
        m.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        AbstractDiagram ad = null;
        try {
            ad = m.readValue(datum.description, AbstractDiagram.class);
        } catch (IOException e) { // JsonParseException | JsonMappingException
            e.printStackTrace();
            assertTrue(false);
        }
        DiagramCreator      dc = new DiagramCreator(ad.toAbstractDescription());
        try {
            ConcreteDiagram cd = dc.createDiagram(CANVAS_SIZE);

            // foreach circle in the TestDatum, compare it with the
            // corresponding circle in the ConcreteDiagram;
            for(int i = 0; i < cd.circles.size(); i++) {
                if(null == datum.expected_circles) // TODO: remove this null check when all tests have a LiteCircleContour component.
                    break;
                LiteCircleContour lcc = datum.expected_circles[i];
                CircleContour      cc = cd.circles.get(i);
                assertEquals(lcc.cx, cc.cx, EPSILON);
                assertEquals(lcc.cy, cc.cy, EPSILON);
                assertEquals(lcc.r,  cc.radius, EPSILON);
            }
        } catch (CannotDrawException cde) {
            
        }
    }
}
