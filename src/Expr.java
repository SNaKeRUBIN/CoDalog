package CoDalogFinal;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Expr {

    String predicate;
    List<String> terms;

    private static boolean variableFound(String term) {
        return Character.isUpperCase(term.charAt(0));
    }

    Expr(String predicate, List<String> terms) {
        this.predicate = predicate;
        this.terms = terms;
    }

    Expr(String predicate, String... terms) {
        this(predicate, Arrays.asList(terms));
    }

    int domainSize() {
        return terms.size();
    }

    public boolean isGroundQuery() {
        for (String term : terms) {
            if (variableFound(term)) {
                return false;
            }
        }
        return true;
    }

    boolean unify(Expr expression, StackMap<String, String> bindings) {
        if (!this.predicate.equals(expression.predicate) || this.domainSize() != expression.domainSize()) {
            return false;
        }
        for (int i = 0; i < this.domainSize(); i++) {
            String term1 = this.terms.get(i);
            String term2 = expression.terms.get(i);
            if (variableFound(term1)) {
                if (!term1.equals(term2)) {
                    if (!bindings.containsKey(term1)) {
                        bindings.put(term1, term2);
                    } else if (!bindings.get(term1).equals(term2)) {
                        return false;
                    }
                }
            } else if (variableFound(term2)) {
                if (!bindings.containsKey(term2)) {
                    bindings.put(term2, term1);
                } else if (!bindings.get(term2).equals(term1)) {
                    return false;
                }
            } else if (!term1.equals(term2)) {
                return false;
            }
        }
        return true;
    }

    Expr substituteExpression(StackMap<String, String> answer) {
        Expr expression = new Expr(this.predicate, new ArrayList<String>());
        for (String term : this.terms) {
            String value;
            if (variableFound(term)) {
                value = answer.get(term);
                if (value == null) {
                    value = term;
                }
            } else {
                value = term;
            }
            expression.terms.add(value);
        }
        return expression;
    }

    static Expr parse(StreamTokenizer scan, boolean nextToken) throws ParseException {
        try {
            if (nextToken) {
                scan.nextToken();
            }
            if (scan.ttype != StreamTokenizer.TT_WORD) {
                throw new ParseException("A predicate is expected");
            }
            String predicate = scan.sval;
            if (scan.nextToken() != '(') {
                throw new ParseException("Syntax error: expected '('");
            }
            List<String> terms = new ArrayList<String>();
            if (scan.nextToken() != ')') {
                scan.pushBack();
                do {
                    if (scan.nextToken() == StreamTokenizer.TT_WORD) {
                        terms.add(scan.sval);
                    } else if (scan.ttype == '"' || scan.ttype == '\'') {
                        terms.add(scan.sval);
                    } else if (scan.ttype == StreamTokenizer.TT_NUMBER) {
                        String value;
                        if (scan.nval == (long) scan.nval) {
                            value = String.format("%d", (long) scan.nval);
                        } else {
                            value = String.format("%s", scan.nval);
                        }
                        terms.add(value);
                    } else {
                        throw new ParseException("A term is expected");
                    }
                } while (scan.nextToken() == ',');
                if (scan.ttype != ')') {
                    throw new ParseException("Syntax error: expected ')'");
                }
            }
            return new Expr(predicate, terms);
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Expr)) {
            return false;
        }
        Expr that = ((Expr) obj);
        if (!this.predicate.equals(that.predicate)) {
            return false;
        }
        if (domainSize() != that.domainSize()) {
            return false;
        }
        for (int i = 0; i < terms.size(); i++) {
            if (!terms.get(i).equals(that.terms.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = predicate.hashCode();
        for (String term : terms) {
            hash += term.hashCode();
        }
        return hash;
    }

    // Regex for terms that contain non-alphanumeric characters and should be
    // quoted in toString()
    static final Pattern quotedTerm = Pattern.compile("[^a-zA-Z0-9._]");

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(predicate).append('(');
        for (int i = 0; i < terms.size(); i++) {
            String term = terms.get(i);
            if (quotedTerm.matcher(term).find()) {
                sb.append('"').append(term.replaceAll("\"", "\\\\\"")).append('"');
            } else {
                sb.append(term);
            }
            if (i < terms.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(')');
        return sb.toString();
    }
}
