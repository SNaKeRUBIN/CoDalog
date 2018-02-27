package src;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Parser {

    private Collection<Expr> edbList = null;
    private Collection<Rule> idbList = null;

    NaiveEvaluation naive = null;
    SemiNaiveEvaluation semiNaive = null;

    private String evaluationMethod = null;
    private String usingMagicSets = null;

    boolean nextToken = true;

    public Parser(String evaluationMethod, Reader reader, String magicSetsUsed) {
        this.edbList = new LinkedList<Expr>();
        this.idbList = new LinkedList<Rule>();
        this.usingMagicSets = magicSetsUsed;

        naive = new NaiveEvaluation();
        semiNaive = new SemiNaiveEvaluation();
        this.evaluationMethod = evaluationMethod;

        try {
            execute(reader);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, String>> execute(Reader reader) throws ParseException {
        try {
            //Scan through each line to see if it is a fact, query or a rule.
            StreamTokenizer scan = new StreamTokenizer(reader);
            scan.ordinaryChar('.');
            scan.commentChar('%');
            scan.quoteChar('"');
            scan.quoteChar('\'');
            scan.wordChars('_', '_');
            scan.nextToken();
            List<Map<String, String>> queryResults = null;
            while (scan.ttype != StreamTokenizer.TT_EOF) {
                scan.pushBack();
                queryResults = parseStatement(scan, true);
                if (queryResults != null) {
                    if (!queryResults.isEmpty()) {
                        if (queryResults.get(0).isEmpty()) {
                            System.out.println("Yes.");
                        } else {
                            for (Map<String, String> result : queryResults) {
                                System.out.println(" " + AuxilaryMethods.toString(result));
                                LoggerClass.logger(" " + AuxilaryMethods.toString(result));
                            }
                        }
                    } else {
                        LoggerClass.logger("No.");
                    }
                }
                scan.nextToken();
            }
            return queryResults;
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }

    public List<Map<String, String>> parseStatement(StreamTokenizer scan, boolean extractNextToken) throws ParseException {
        try {
            Expr head = Expr.parse(scan, extractNextToken);
            if (scan.nextToken() == ':') {
                // This is a rule.
                if (scan.nextToken() != '-') {
                    LoggerClass.logger("Syntax error: expected ':-'");
                    head = null;
                    parseStatement(scan, false);
                    return null;
                }
                List<Expr> body = new ArrayList<Expr>();
                do {
                    Expr arg = Expr.parse(scan, true);
                    body.add(arg);
                } while (scan.nextToken() == ',');

                if (scan.ttype != '.') {
                    LoggerClass.logger("Syntax error: '.' expected after rule.");
                    head = null;
                    parseStatement(scan, false);
                    return null;
                }
                Rule rule = new Rule(head, body);
                if (!rule.validate()) {
                    LoggerClass.logger("The rule is invalid: " + rule);
                    head = null;
                    parseStatement(scan, false);
                    return null;
                }
                idbList.add(rule);
            } else {
                if (scan.ttype == '.') {
                    // This is a fact.
                    if (!head.isGroundQuery()) {
                        LoggerClass.logger("The fact needs to be grounded: " + head);
                        head = null;
                        parseStatement(scan, false);
                        return null;
                    }
                    edbList.add(head);
                } else {
                    if (scan.ttype != '.' && scan.ttype != '?' && scan.ttype != ',') {
                        LoggerClass.logger("Incorrect syntax: Expect one of '.', ',' or '?' after fact/query expression");
                        head = null;
                        parseStatement(scan, false);
                        return null;
                    }
                    // This is a query
                    List<Expr> query = new ArrayList<Expr>();
                    query.add(head);

                    if (usingMagicSets.equals("Y")) {
                        Expr queryHead = new Expr("query", "X");
                        Rule queryRule = new Rule(queryHead);
                        queryRule.addBodyExpr(head);
                        idbList.add(queryRule);
                    }

                    while (scan.ttype == ',') {
                        nextToken = true;
                        query.add(Expr.parse(scan, true));
                        scan.nextToken();
                    }

                    if (scan.ttype == '?') {
                        if (evaluationMethod.equals("Naive")) {
                            return naive.queryResults(query, edbList, idbList, usingMagicSets);
                        } else if (evaluationMethod.equals("Seminaive")) {
                            return semiNaive.query(query, edbList, idbList, usingMagicSets);
                        }
                    } else {
                        LoggerClass.logger("Syntax error: Expected '?' after query");
                        head = null;
                        parseStatement(scan, false);
                        return null;
                    }
                    if (query.isEmpty()) {
                        if (evaluationMethod.equals("Naive")) {
                            return naive.queryResults(query, edbList, idbList, usingMagicSets);
                        } else if (evaluationMethod.equals("Seminaive")) {
                            return semiNaive.query(query, edbList, idbList, usingMagicSets);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ParseException(e);
        }
        return null;
    }

}
