package src;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class NaiveEvaluation {

    public List<Map<String, String>> queryResults(List<Expr> query, Collection<Expr> edbList, Collection<Rule> idbList, String magicSetsUsed) {
        if (query.isEmpty()) {
            return new ArrayList<Map<String, String>>();
        }
        if (magicSetsUsed.equals("Y")) {
            //call function to return new rules.
            MagicSets ms = new MagicSets();

            idbList = ms.evaluateMagicSets(edbList, idbList);

            // from here
            for (Rule rule : idbList) {
                if (rule.head.predicate.contains("query_")) {
                    query.add(rule.body.get(0));
                }
                if (rule.body.isEmpty()) {
                    edbList.add(rule.head);
                }
            }

            query.remove(0);
            // till here
        }
        Collection<Expr> dataset = buildDatabase(new HashSet<Expr>(edbList), idbList);

        LoggerClass.logger("To answer the given query we need to evaluate " + AuxilaryMethods.toString(dataset));
        return evalQuery(dataset, query);
    }

    private Collection<Expr> buildDatabase(Set<Expr> facts, Collection<Rule> rules) {
        int factSize = 0;
        // Repeat until there are no more facts
        do {
            factSize = facts.size();
            final Set<Expr> factsSet = facts;

            // Match each rule to the facts
            Stream<Expr> stream = rules.stream().flatMap(rule -> matchRule(factsSet, rule));

            // Combine all the facts.
            facts.addAll(stream.collect(Collectors.toSet()));
        } while (facts.size() != factSize);

        return facts;
    }

    private Stream<Expr> matchRule(final Collection<Expr> facts, Rule rule) {
        // Find if there is a match between the body of the rule with the fact.
        if (rule.body.isEmpty()) {
            return Stream.of();
        }
        return matchGoals(facts, rule.body, null).map(answer -> rule.head.substituteExpression(answer));
    }

    private Stream<StackMap<String, String>> matchGoals(final Collection<Expr> facts, List<Expr> goals, StackMap<String, String> bindings) {
        // Get the first goal
        final Expr firstGoalInList = goals.get(0);

        // Create list of all the other goals.
        final List<Expr> remainingGoals = goals.subList(1, goals.size());

        // Match each fact to the first goal.
        return facts.stream().filter(fact -> fact.predicate.equals(firstGoalInList.predicate)).flatMap(fact -> {

            LoggerClass.logger("  - Comparing goal -->> " + firstGoalInList + " against fact-->> " + fact);

            StackMap<String, String> newBindings = new StackMap<String, String>(bindings);
            //System.out.println(newBindings.toString(String, value));
            if (fact.unify(firstGoalInList, newBindings)) {
                if (remainingGoals.isEmpty()) {
                    return Stream.of(newBindings);
                } else {
                    // Check if the fact matches with the other remaining goals in the list.
                    return matchGoals(facts, remainingGoals, newBindings);
                }
            }
            //No matches found.
            return Stream.of();
        });
    }

    private List<Map<String, String>> evalQuery(Collection<Expr> database, List<Expr> goals) {
        List<Map<String, String>> answers = matchGoals(database, goals, null).map(StackMap::flatten).collect(Collectors.toList());
        return answers;
    }

}
