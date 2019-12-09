import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

public class Interpreter {

    Parser parser;
    HashMap<String, Integer> globalScope; //имена переменных и их значения


    public Interpreter(Parser parser) {
        this.parser = parser;
        globalScope = new HashMap<>();
    }

    //здесь описывается алгоритм обхода дерева
    public Object visit(ASTNode node) {

        if (node instanceof ExprNode) {
            return visitOp((ExprNode)node); //двигаемся дальше вниз по дереву
        } else if (node instanceof NumNode) {
            return ((NumNode) node).value; //возвращаем значение листа дерева
        } else if (node instanceof StatementNode) {
            visitStatement((StatementNode) node);
        } else if (node instanceof AssignNode) {
            visitAssign((AssignNode)node);
        } else if (node instanceof IDNode) {
            return visitIdentifier((IDNode)node);
        } else if (node instanceof LogicOpNode) {
            return visitLogicOp((LogicOpNode) node);
        } else if (node instanceof PrintNode) {
            visitPrintStmt((PrintNode) node);
        } else if (node instanceof ScanNode) {
            visitScanStmt((ScanNode)node);
        } else if (node instanceof StringNode) {
            return visitStringNode((StringNode)node);
        }

        return null;

    }

    private Integer visitOp(ExprNode node) {

        if (node.token.type == Token.TokenType.PLUS) {
            return (Integer) visit(node.left) + (Integer) visit(node.right);
        } else if (node.token.type == Token.TokenType.MINUS) {
            return (Integer)visit(node.left) - (Integer)visit(node.right);
        } else if (node.token.type == Token.TokenType.MUL) {
            return (Integer)visit(node.left) * (Integer)visit(node.right);
        } else if (node.token.type == Token.TokenType.DIV) {
            return (Integer)visit(node.left) / (Integer)visit(node.right);
        }

        return 0;

    }

    private void visitScanStmt(ScanNode node) {

        //ожидаем ввода значения переменной
        Scanner scanner = new Scanner(System.in);
        boolean inputSuccess = false;
        Integer value = null;
        while (!inputSuccess) {
            try {
                value = scanner.nextInt();
                inputSuccess = true;
            } catch (InputMismatchException e) {
                System.out.println("Введите целое число");
                scanner.nextLine();
            }
        }
        //Получаем имя переменной
        String name = ((IDNode)node.idNode).value;
        globalScope.put(name, value);//запоминаем в таблицу переменных

    }

    private void visitPrintStmt(PrintNode node) {

        for (ASTNode child : node.nodesToPrint) {
            //TODO неопределенное значение переменной
            System.out.print(visit(child));
        }
        System.out.println(); //для перехода на следующую строку

    }

    private String visitStringNode(StringNode node) {
        return node.value;
    }

    private Boolean visitLogicOp(LogicOpNode node) {

        if (node.token.type == Token.TokenType.GREATER) {
            return (Integer) visit(node.left) > (Integer) visit(node.right);
        } else if (node.token.type == Token.TokenType.GREATER_EQ) {
            return (Integer) visit(node.left) >= (Integer) visit(node.right);
        } else if (node.token.type == Token.TokenType.LESS) {
            return (Integer) visit(node.left) < (Integer) visit(node.right);
        } else if (node.token.type == Token.TokenType.LESS_EQ) {
            return (Integer) visit(node.left) <= (Integer) visit(node.right);
        } else if (node.token.type == Token.TokenType.EQUAL) {
            return visit(node.left).equals(visit(node.right));
        } else if (node.token.type == Token.TokenType.NOT_EQUAL) {
            return !visit(node.left).equals(visit(node.right));
        }

        return null;

    }

    private void visitStatement(StatementNode node) {
        //for (ASTNode child : node.children) { //посещаем все дочерние элементы
        for (int i = 0; i < node.children.size(); ++i) {
            ASTNode child = node.children.get(i);

            if (child instanceof IfNode) { //если это if

                //if, statement, и else идут друг за другом, поэтому берем их последовательно
                StatementNode statementNode = (StatementNode) node.children.get(++i);
                ElseNode elseNode = null;
                if (node.children.get(i + 1) instanceof ElseNode) { //elseNode всегда есть, но он может быть null
                    elseNode = (ElseNode)node.children.get(++i);
                }
                visitIfStatement((IfNode) child, statementNode, elseNode);

            } else if (child instanceof ForNode) {

                //берем тело цикла, которое следует за узлом for
                StatementNode bodyNode = (StatementNode) node.children.get(++i);
                visitForStatement((ForNode) child, bodyNode);

            } else {
                visit(child);
            }

        }
    }

    private void visitIfStatement(IfNode ifNode, StatementNode thenNode, ElseNode elseNode) {

        LogicOpNode logicOpNode = (LogicOpNode) ifNode.boolExprNode; //получаем выражение внутри if
        if ((Boolean) visit(logicOpNode)) { //если оно true
            visit(thenNode); //посещаем ветвь then
        } else {
            if (elseNode != null) { //если есть else
                visit(elseNode.statementNode);//посещаем statement внутри else
            }
        }

    }

    private void visitForStatement(ForNode forNode, StatementNode bodyNode) {

        Integer valueFrom = (Integer)visit(forNode.exprFrom);
        Integer valueTo = (Integer)visit(forNode.exprTo);
        ASTNode counter = forNode.identifier;//переменная счетчик
        //запоминаем новую переменную, она тоже становится глобальной
        globalScope.put(((IDNode)counter).value, valueFrom);

        Integer currValue = valueFrom;
        while (currValue < valueTo) {
            visit(bodyNode); //выполняем тело цикла
            globalScope.put(((IDNode)counter).value, ++currValue);//увеличиваем счетчик
        }

    }

    private void visitAssign(AssignNode node) {
        String varName = ((IDNode)node.left).value; //слева берем имя переменной
        globalScope.put(varName, (Integer)visit(node.right)); //запоминаем значение переменной
        int a = 0;
    }

    private Integer visitIdentifier(IDNode node) {
        String varName = node.value; //получаем имя переменной
        Integer value = globalScope.get(varName);
        if (value == null) {
            globalScope.put(varName, 0);
            return 0;
        }
        else
            return value;
        //return null;
    }

    private void error() {}

    public void interpret() {
        ASTNode root = parser.parse();
        if (root != null)
            visit(root);
    }

    public void printGlobalScope() {
       for (Map.Entry<String, Integer> entry : globalScope.entrySet()) {
           String varName = entry.getKey();
           int varValue = entry.getValue();
           System.out.println(varName + ": " + varValue);
       }
    }

}
