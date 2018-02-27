package src;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Rule {

	private static boolean isVariable(String term) {
		return Character.isUpperCase(term.charAt(0));
	}

	Expr head;
	List<Expr> body;

	Rule(Expr head, List<Expr> body) {
		this.head = head;
		this.body = body;		
		
        LinkedList<Expr> tempList = new LinkedList<>();
        for (Expr expr : body) {
            Expr tempExpr = new Expr(expr.predicate, expr.terms);
            tempList.add(tempExpr);
        }
        this.body = tempList;
	}

	Rule(Expr head, Expr... body) {
		this(head, Arrays.asList(body));
	}
	
    Rule(Expr new_head) {
        this.head = new_head;
        this.body = new LinkedList<>();
    }

    public void addBodyExpr(Expr expr) {
        this.body.add(expr);
    }

    public void removeBodyExpr(int index) {
        if (index > -1 && index < this.body.size()) {
            this.body.remove(index);
        }
    }

	// Enforce the rule that variables on the LHS must appear on the RHS
	boolean validate() {
		Set<String> variables = new HashSet<String>();
		for (String term : head.terms) {
			if (isVariable(term)) {
				variables.add(term);
			}
		}
		for (Expr expr : body) {
			for (String term : expr.terms) {
				if (isVariable(term)) {
					variables.remove(term);
				}
			}
		}
		return variables.isEmpty();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(head);
		sb.append(" :- ");
		for (int i = 0; i < body.size(); i++) {
			sb.append(body.get(i));
			if (i < body.size() - 1)
				sb.append(", ");
		}
		return sb.toString();
	}
}
