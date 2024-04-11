package variable_manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariableManager {

    private final Map<String, String> localVars = new HashMap<>();
    private final Map<String, List<String>> localArrays = new HashMap<>();
    private final Map<String, String> globalVars = new HashMap<>();
    private final Map<String, List<String>> globalArrays = new HashMap<>();

    public String getVar(String name) {
        return this.getVar(name, null);
    }
    public String getVar(String name, Integer number){

        if (number == null && this.localVars.containsKey(name)) {
            return this.localVars.get(name);
        }

        if (number == null && this.globalVars.containsKey(name)) {
            return this.globalVars.get(name);
        }

        if (number != null && this.localArrays.containsKey(name)) {
            return this.localArrays.get(name).get(number);
        }

        if (number != null && this.globalArrays.containsKey(name)) {
            return this.globalArrays.get(name).get(number);
        }

        throw new RuntimeException("Variable " + name + " does not exist or is nor defined correctly");
    }

    public List<String> getArray(String name){
        List<String> array = this.localArrays.get(name);
        if (array == null) {
            array = this.globalArrays.get(name);
        }
        return array;
    }

    public void putVar(String name, String value, boolean global) {
        if(global) {
                this.globalVars.put(name, value);
            return;
        }
            this.localVars.put(name, value);
    }


    public void putArray(String name, List<String> items, boolean global) {
        if(global) {
            if (!localVars.containsKey(name)
                    && !globalVars.containsKey(name)
                    && !localArrays.containsKey(name)) {

                this.globalArrays.put(name, items);
            }
            return;
        }

        if (!localVars.containsKey(name)
                && !globalVars.containsKey(name)
                && !globalArrays.containsKey(name)) {

            this.localArrays.put(name, items);
        }
    }

    public void clearLocalVars(){
        this.localArrays.clear();
        this.localVars.clear();
    }

}
