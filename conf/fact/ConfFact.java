package conf.fact;

import pascal.taie.analysis.dataflow.fact.MapFact;
import pascal.taie.ir.exp.Var;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConfFact extends MapFact<String, List<Var>> {
    public ConfFact(){
        this(Collections.emptyMap());
    }
    public ConfFact(Map<String, List<Var>> map) {
        super(map);
    }

    public ConfFact copy() {
        return new ConfFact(this.map);
    }
}
