package icircles.concreteDiagram;

import icircles.recomposition.RecompData;

import java.util.ArrayList;

public class BuildStep {

    public static enum Piercing {UNKNOWN, NESTED_PIERCING, ONE_PIERCING, TWO_PIERCING};

    public ArrayList<RecompData> recomp_data;
    public BuildStep next = null;

    BuildStep(RecompData rd) {
        recomp_data = new ArrayList<RecompData>();
        recomp_data.add(rd);
    }

    Piercing getType() {
        switch (recomp_data.get(0).split_zones.size()) {
        case 1: return Piercing.NESTED_PIERCING;
        case 2: return Piercing.ONE_PIERCING;
        }
        return Piercing.UNKNOWN;
    }
}
