package stringprop.fact;


import pascal.taie.analysis.dataflow.fact.MapFact;
import pascal.taie.ir.exp.Var;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SPFact extends MapFact<Var, Set<String>> {

    public SPFact() {
        this(Collections.emptyMap());
    }

    public SPFact(Map<Var, Set<String>> map) {
        super(map);
    }

    @Override
    public boolean update(Var var, Set<String> value) {
        /*
         * value cannot be null
         */
        if (value != null) {

            /*
             * whether the var data fact is empty
             */
            Set<String> valueSet = this.get(var);
            if (valueSet == null) {
                valueSet = new HashSet<>();
            }
            int oldSize = valueSet.size();
            boolean haschanged = false;
            valueSet.addAll(value);
            int newSize = valueSet.size();
            /*
             * if the set has changed, it means there is new data
             */
            if (oldSize != newSize) {
                haschanged = true;
            }
            super.update(var, valueSet);
            return haschanged;
        }
        /*
         * it is not necessary to update when value is null
         */
        else return false;

    }

    @Override
    public SPFact copy() {
        return new SPFact(this.map);
    }
}
