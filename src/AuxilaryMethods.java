package CoDalogFinal;

import java.util.Collection;
import java.util.Map;

public class AuxilaryMethods {

    static String toString(Collection<?> collection) {
        StringBuilder sb = new StringBuilder("[");
        for (Object o : collection) {
            sb.append(o.toString()).append(". ");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String toString(Map<String, String> bindings) {
        StringBuilder sb = new StringBuilder("{");
        int s = bindings.size(), i = 0;
        for (String k : bindings.keySet()) {
            String v = bindings.get(k);
            sb.append(k).append(": ");
            if (Expr.quotedTerm.matcher(v).find()) {
                sb.append('"').append(v.replaceAll("\"", "\\\\\"")).append("\"");
            } else {
                sb.append(v);
            }
            if (++i < s) {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
