package src;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SemiNaiveEvaluation {

    public List<Map<String, String>> query(List<Expr> goals, Collection<Expr> edbList, Collection<Rule> idbList, String magicSetsUsed) {
        if (goals.isEmpty()) {
            return new ArrayList<Map<String, String>>();
        }

        if (magicSetsUsed.equals("Y")) {
            //call function to return new rules.
            MagicSets ms = new MagicSets();
            idbList = ms.evaluateMagicSets(edbList, idbList);

            // from here
            for (Rule rule : idbList) {
                if (rule.head.predicate.contains("query_")) {
                    goals.add(rule.body.get(0));
                }
                if (rule.body.isEmpty()) {
                    edbList.add(rule.head);
                }
            }

            goals.remove(0);
            // till here

        }
        Collection<Rule> rules = getRelevantRules(goals, edbList, idbList);
        LoggerClass.logger("To answer the given query we need to evaluate " + AuxilaryMethods.toString(rules));

        Collection<Expr> dataset = buildDatabase(new HashSet<Expr>(edbList), rules);

        return matchAnswers(dataset, goals);
    }

    /* Returns a list of rules that are relevant to the query.
		If for example you're quering employment status, you don't care about family relationships, etc.
		The advantages of this of this optimization becomes bigger the complexer the rules get. */
    private Collection<Rule> getRelevantRules(List<Expr> goals, Collection<Expr> edbList, Collection<Rule> idbList) {
        Set<Rule> relevant = new HashSet<Rule>();
        goals = new LinkedList<Expr>(goals);
        while (!goals.isEmpty()) {
            Expr expr = goals.remove(0);
            for (Rule rule : idbList) {
                if (rule.head.predicate.equals(expr.predicate) && !relevant.contains(rule)) {
                    relevant.add(rule);
                    goals.addAll(rule.body);
                }
            }
        }
        return relevant;
    }

    private Collection<Expr> buildDatabase(Set<Expr> facts, Collection<Rule> rules) {
        int o = 0;
        // Repeat until there no more facts added
        do {
            o = facts.size();
            final Set<Expr> lambdaFacts = facts;

            // Match each rule to the facts
            Stream<Expr> stream = rules.stream().flatMap(rule -> matchRule(lambdaFacts, rule));
            //stream.forEach(System.out::print);

            // Combine all the facts. A Set ensures that the facts are unique
            facts.addAll(stream.collect(Collectors.toSet()));
        } while (facts.size() != o);

        return facts;
    }

    private Stream<Expr> matchRule(final Collection<Expr> facts, Rule rule) {
        if (rule.body.isEmpty()) // If this happens, you're using the API worng.
        {
            return Stream.of();
        }
        // Match the rule body to the facts.
        // For each match found, substitute the bindings into the head to create a new fact.
        return matchGoals(rule.body, facts, null).map(answer -> rule.head.substituteExpression(answer));
    }

    private Stream<StackMap<String, String>> matchGoals(List<Expr> goals, final Collection<Expr> facts, StackMap<String, String> bindings) {
        // First goal
        final Expr goal = goals.get(0); // Assumes goals won't be empty

        // Remaining goals - needs to be final for the lambda
        final List<Expr> nextGoals = goals.subList(1, goals.size());

        // Match each fact to the first goal.
        // If the fact matches: If it is the last/only goal then we can return the bindings
        // as an answer, otherwise we recursively check the remaining goals.
        // Rough profiling says the filter() on fact.predicate makes a big difference as
        // the complexity of the database increases.
        return facts.stream().filter(fact -> fact.predicate.equals(goal.predicate)).flatMap(fact -> {
            LoggerClass.logger("  - Comparing goal -->> " + goal + " against fact-->> " + fact);
            StackMap<String, String> newBindings = new StackMap<String, String>(bindings);
            if (fact.unify(goal, newBindings)) {
                if (nextGoals.isEmpty()) {
                    // Last goal in the list of goals matched!
                    return Stream.of(newBindings);
                } else {
                    // More goals to match. Recurse with the remaining goals.
                    return matchGoals(nextGoals, facts, newBindings);
                }
            }
            return Stream.of(); // No match; return empty stream.
        });
    }

    private List<Map<String, String>> matchAnswers(Collection<Expr> database, List<Expr> goals) {
        // We have to call StackMap::flatten on each element answers because the StackMap doesn't implement all the
        // methods of the Map interface; we convert it to a proper array first.
        List<Map<String, String>> answers = matchGoals(goals, database, null).map(StackMap::flatten).collect(Collectors.toList());
        return answers;
    }
}
