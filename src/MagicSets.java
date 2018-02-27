package src;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class MagicSets {

    HashSet<String> edbPredicateNames = new HashSet<>();
    HashSet<String> idbPredicateNames = new HashSet<>();

    public Collection<Rule> evaluateMagicSets(Collection<Expr> edbList, Collection<Rule> idbList) {
        for (Expr expr : edbList) {
            edbPredicateNames.add(expr.predicate);
        }

        for (Rule rule : idbList) {
            idbPredicateNames.add(rule.head.predicate);
        }

        // adorned sets
        // distinguished predicates
        // local distinguished arguments
        // if head contains distinguisehd predicate, mark every body idb predicate as
        // distinguised. take no action if its a edb predicate
        // make 2 loops, outer controls if any change happened, innner iterates over all
        // idb rules
        System.out.print("");

        Collection<Rule> adornedSet = new LinkedList<>();
        HashSet<String> distinguishedPredicateNames = new HashSet<>();
        HashMap<String, String> distinguishedPredicateMap = new HashMap<>();

        // work on query
        for (Rule rule : idbList) {
            if (rule.head.predicate.equals("query")) {

                Rule tempRule = new Rule(new Expr(rule.head.predicate, rule.head.terms), new ArrayList<>(rule.body));

                ArrayList<String> distinguishedArgument = new ArrayList<String>();

                for (Expr expr : tempRule.body) {
                    if (idbPredicateNames.contains(expr.predicate)) {
                        distinguishedPredicateNames.add(expr.predicate);
                        String tempPredicate = expr.predicate;
                        expr.predicate = expr.predicate + "_";

                        for (String argument : expr.terms) {
                            if (Character.isUpperCase(argument.charAt(0))) {
                                expr.predicate = expr.predicate + "f";
                            } else {
                                distinguishedArgument.add(argument);
                                expr.predicate = expr.predicate + "b";
                            }
                        }

                        distinguishedPredicateMap.put(tempPredicate, expr.predicate);
                    }
                }
                tempRule.head.predicate = tempRule.head.predicate + "_";
                for (String argument : tempRule.head.terms) {
                    if (distinguishedArgument.contains(argument)) {
                        tempRule.head.predicate = tempRule.head.predicate + "b";
                    } else {
                        tempRule.head.predicate = tempRule.head.predicate + "f";
                    }
                }

                adornedSet.add(tempRule);

                break;

            }
        }

        HashSet<Integer> doneRules = new HashSet<>();
        boolean outer = true;
        while (outer) {
            outer = false;
            for (int i = 0; i < idbList.size(); i++) {

                if (!doneRules.contains(i)) {
                    if (distinguishedPredicateNames.contains(((LinkedList<Rule>) idbList).get(i).head.predicate)) {

                        Rule tempRule = new Rule(new Expr(((LinkedList<Rule>) idbList).get(i).head.predicate, ((LinkedList<Rule>) idbList).get(i).head.terms),
                                new ArrayList<>(((LinkedList<Rule>) idbList).get(i).body));
                        HashSet<String> distinguishedArgument = new HashSet<>();

                        tempRule.head.predicate = distinguishedPredicateMap.get(tempRule.head.predicate);

                        String tempPredName = tempRule.head.predicate
                                .substring(tempRule.head.predicate.lastIndexOf("_") + 1);
                        for (int j = 0; j < tempPredName.length(); j++) {
                            if (tempPredName.charAt(j) == 'b') {
                                distinguishedArgument.add(tempRule.head.terms.get(j));
                            }
                        }

                        // trying to make it loop if any chnage happened
                        // edb - one of if needs to be inverted ?? to make sure that if distArg already
                        // contains all args, count as no change
                        // edb - count adding arg as change
                        // idb - if contains distinguished arg, add to distinguished predicates(rename),
                        // count as change
                        // idb -
                        // on any chnage break TO loop local - might dec amt of looping
                        // boolean loopLocal = true;
                        // while (loopLocal) {
                        // loopLocal = false;
                        for (Expr exprBody : tempRule.body) {
                            if (edbPredicateNames.contains(exprBody.predicate)) {
                                if (edbContainsArgument(exprBody, distinguishedArgument)) { // if atleast one argument
                                    // is distinguished
                                    // add all arguments to distinguished argument list
                                    for (String arg : exprBody.terms) {
                                        distinguishedArgument.add(arg);
                                    }

                                }
                            }
                        }

                        for (Expr exprBody : tempRule.body) {
                            if (idbPredicateNames.contains(exprBody.predicate)) {
                                if (edbContainsArgument(exprBody, distinguishedArgument)) {

                                    distinguishedPredicateNames.add(exprBody.predicate);
                                    String tempPredicate = exprBody.predicate;
                                    exprBody.predicate = exprBody.predicate + "_";

                                    for (String argument : exprBody.terms) {
                                        if (!distinguishedArgument.contains(argument)) {
                                            exprBody.predicate = exprBody.predicate + "f";
                                        } else {
                                            // distinguishedArgument.add(argument);
                                            exprBody.predicate = exprBody.predicate + "b";
                                        }
                                    }

                                    distinguishedPredicateMap.put(tempPredicate, exprBody.predicate);

                                }

                            }
                        }

                        // }
                        doneRules.add(i);
                        outer = true;
                    }

                }
            }
        }

        for (Rule rule : idbList) {
            if (distinguishedPredicateMap.containsKey(rule.head.predicate)) {
                Rule tempRule = new Rule(new Expr(distinguishedPredicateMap.get(rule.head.predicate), rule.head.terms));

                for (Expr bodyExpr : rule.body) {
                    String nameNew;
                    if (distinguishedPredicateMap.containsKey(bodyExpr.predicate)) {
                        nameNew = distinguishedPredicateMap.get(bodyExpr.predicate);
                    } else {
                        nameNew = bodyExpr.predicate;
                    }

                    tempRule.addBodyExpr(new Expr(nameNew, bodyExpr.terms));

                }

                adornedSet.add(tempRule);

            }
        }
        // STEP-1 DONE

        System.out.println("Adorned Set:");
        for (Rule rule : adornedSet) {
            System.out.println(rule);
        }
        System.out.println("");

        // remove normal names from distinguisehd predicate names and replace with k,v
        // mapping
        HashSet<String> distinguishedPredicateNamesTemp = new HashSet<>();
        for (String s : distinguishedPredicateNames) {
            String tempS = distinguishedPredicateMap.get(s);
            distinguishedPredicateNamesTemp.add(tempS);
            // distinguishedPredicateNames.remove(s);
        }
        distinguishedPredicateNames = distinguishedPredicateNamesTemp;

        Collection<Rule> magicRules = new LinkedList<>();

        for (Rule rule : adornedSet) {

            // RuleMS currRule = new RuleMS(rule.head);
            Rule currRule = new Rule(new Expr(rule.head.predicate, rule.head.terms));
            for (Expr bodyExpr : rule.body) {
                if (distinguishedPredicateNames.contains(bodyExpr.predicate)) {
                    Expr current = new Expr(bodyExpr.predicate, bodyExpr.terms);

                    // create distinguished arguments list
                    HashSet<String> distinguishedArgument = new HashSet<>();

                    String tempPredName = currRule.head.predicate.substring(currRule.head.predicate.lastIndexOf("_") + 1);
                    for (int j = 0; j < tempPredName.length(); j++) {
                        if (tempPredName.charAt(j) == 'b') {
                            distinguishedArgument.add(rule.head.terms.get(j));
                        }
                    }
                    for (Expr exprBody : rule.body) {
                        if (edbPredicateNames.contains(exprBody.predicate)) {
                            if (edbContainsArgument(exprBody, distinguishedArgument)) { // if atleast one argument is
                                // distinguished
                                // add all arguments to distinguished argument list
                                for (String arg : exprBody.terms) {
                                    distinguishedArgument.add(arg);
                                }

                            }
                        }
                    }
                    // done creating distinguished argument list

                    for (Expr bodyExpr1 : rule.body) {
                        if (edbPredicateNames.contains(bodyExpr1.predicate)) {
                            if (edbContainsArgument(bodyExpr1, distinguishedArgument)) {
                                Expr current1 = new Expr(bodyExpr1.predicate, bodyExpr1.terms);
                                List<String> new_terms = new LinkedList<>();

                                for (String str : current1.terms) {
                                    if (distinguishedArgument.contains(str)) {
                                        new_terms.add(str);
                                    } else if (!Character.isUpperCase(str.charAt(0))) {
                                        new_terms.add(str);
                                    } else {
                                    }
                                }
                                currRule.addBodyExpr(bodyExpr1);
                            }
                        }
                    }
                    // (d) done

                    current.predicate = "magic_" + current.predicate;
                    // (b) done

                    // remove non-distinguished arguments
                    List<String> new_terms = new LinkedList<>();
                    for (String str : current.terms) {
                        if (distinguishedArgument.contains(str)) {
                            new_terms.add(str);
                        } else if (!Character.isUpperCase(str.charAt(0))) {
                            new_terms.add(str);
                        } else {
                        }
                    }
                    current.terms = new_terms;
                    // (c) done

                    currRule.head.predicate = "magic_" + currRule.head.predicate;
                    // (e) done

                    new_terms = new LinkedList<>();
                    for (String str : currRule.head.terms) {
                        if (distinguishedArgument.contains(str)) {
                            new_terms.add(str);
                        } else if (!Character.isUpperCase(str.charAt(0))) {
                            new_terms.add(str);
                        } else {
                        }
                    }
                    currRule.head.terms = new_terms;
                    // (f) done

                    Expr copyExpr = new Expr(current.predicate, current.terms);

                    currRule.addBodyExpr(currRule.head);

                    currRule.head = copyExpr;
                    // (a) & (g) done

                    if (currRule.body.get(currRule.body.size() - 1).terms.isEmpty()) {
                        currRule.removeBodyExpr(currRule.body.size() - 1);
                    }

                    magicRules.add(currRule);
                }
            }
        }
        // STEP-2 done

        System.out.println("Magic Rules:");
        for (Rule rule : magicRules) {
            System.out.println(rule);
        }
        System.out.println("");

        Collection<Rule> modifiedAdornedSet = new LinkedList<>();

        for (Rule rule : adornedSet) {
            Expr temp = new Expr(rule.head.predicate, rule.head.terms);

            Rule tempRule = new Rule(rule.head, rule.body);

            List<String> new_terms = new LinkedList<>();
            String tempPredName = rule.head.predicate.substring(tempRule.head.predicate.lastIndexOf("_") + 1);
            for (int j = 0; j < tempPredName.length(); j++) {
                if (tempPredName.charAt(j) == 'b') {
                    new_terms.add(rule.head.terms.get(j));
                }
            }
            temp.terms = new_terms;

            temp.predicate = "magic_" + temp.predicate;

            if (!temp.terms.isEmpty()) {
                tempRule.body.add(temp);
            }

            modifiedAdornedSet.add(tempRule);

        }
        // STEP-3 DONE

        System.out.println("Modified Adorned Rules:");
        for (Rule rule : modifiedAdornedSet) {
            System.out.println(rule);
        }
        System.out.println("");
        System.out.println("break here");

        modifiedAdornedSet.addAll(magicRules);
        return modifiedAdornedSet;
    }

    public static boolean edbContainsArgument(Expr expr, HashSet<String> argumentsDistinguished) {

        for (String arg : expr.terms) {
            if (argumentsDistinguished.contains(arg)) {
                return true;
            }
        }
        return false;
    }
}
