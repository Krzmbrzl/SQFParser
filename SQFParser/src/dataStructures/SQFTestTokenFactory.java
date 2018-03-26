package dataStructures;

public class SQFTestTokenFactory extends AbstractSQFTokenFactory {
	
	public SQFTestTokenFactory(CharacterBuffer buffer) {
		super(buffer);
	}

	@Override
	public void initialize() {
		// TODO: https://gist.github.com/commy2/016676126737a9a4389c85925b45a68e
		// assignment
		lookupTable.put("=", new LookupTableEntry(10, ESQFOperatorType.BINARY));
		// logic
		lookupTable.put("or", new LookupTableEntry(8, ESQFOperatorType.BINARY));
		lookupTable.put("||", new LookupTableEntry(8, ESQFOperatorType.BINARY));
		lookupTable.put("and", new LookupTableEntry(7, ESQFOperatorType.BINARY));
		lookupTable.put("&&", new LookupTableEntry(7, ESQFOperatorType.BINARY));
		// comparison
		lookupTable.put("!=", new LookupTableEntry(6, ESQFOperatorType.BINARY));
		lookupTable.put("<", new LookupTableEntry(6, ESQFOperatorType.BINARY));
		lookupTable.put(">", new LookupTableEntry(6, ESQFOperatorType.BINARY));
		lookupTable.put("<=", new LookupTableEntry(6, ESQFOperatorType.BINARY));
		lookupTable.put(">=", new LookupTableEntry(6, ESQFOperatorType.BINARY));
		lookupTable.put("==", new LookupTableEntry(6, ESQFOperatorType.BINARY));
		lookupTable.put(">>", new LookupTableEntry(6, ESQFOperatorType.BINARY));
		// binary commands
		lookupTable.put("setpos", new LookupTableEntry(5, ESQFOperatorType.BINARY));
		lookupTable.put("then", new LookupTableEntry(5, ESQFOperatorType.BINARY));
		lookupTable.put("setpos", new LookupTableEntry(5, ESQFOperatorType.BINARY));
		lookupTable.put("call", new LookupTableEntry(5, ESQFOperatorType.BINARY));
		lookupTable.put("params", new LookupTableEntry(5, ESQFOperatorType.BINARY));
		lookupTable.put("spawn", new LookupTableEntry(5, ESQFOperatorType.BINARY));
		lookupTable.put("isequalto", new LookupTableEntry(5, ESQFOperatorType.BINARY));
		lookupTable.put("execvm", new LookupTableEntry(5, ESQFOperatorType.BINARY));
		lookupTable.put("select", new LookupTableEntry(5, ESQFOperatorType.BINARY));
		lookupTable.put("random", new LookupTableEntry(5, ESQFOperatorType.BINARY));
		lookupTable.put("foreach", new LookupTableEntry(5, ESQFOperatorType.BINARY));
		lookupTable.put("do", new LookupTableEntry(5, ESQFOperatorType.BINARY));
		lookupTable.put(":", new LookupTableEntry(5, ESQFOperatorType.BINARY));
		// else
		lookupTable.put("else", new LookupTableEntry(4, ESQFOperatorType.BINARY));
		// binary math operators
		lookupTable.put("+", new LookupTableEntry(3, ESQFOperatorType.BINARY));
		lookupTable.put("-", new LookupTableEntry(3, ESQFOperatorType.BINARY));
		lookupTable.put("min", new LookupTableEntry(3, ESQFOperatorType.BINARY));
		lookupTable.put("max", new LookupTableEntry(3, ESQFOperatorType.BINARY));
		lookupTable.put("*", new LookupTableEntry(2, ESQFOperatorType.BINARY));
		lookupTable.put("/", new LookupTableEntry(2, ESQFOperatorType.BINARY));
		lookupTable.put("%", new LookupTableEntry(2, ESQFOperatorType.BINARY));
		lookupTable.put("mod", new LookupTableEntry(2, ESQFOperatorType.BINARY));
		lookupTable.put("atan2", new LookupTableEntry(2, ESQFOperatorType.BINARY));
		lookupTable.put("^", new LookupTableEntry(1, ESQFOperatorType.BINARY));
		
		lookupTable.put("hint", new LookupTableEntry(0, ESQFOperatorType.UNARY));
		lookupTable.put("str", new LookupTableEntry(0, ESQFOperatorType.UNARY));
		lookupTable.put("cos", new LookupTableEntry(0, ESQFOperatorType.UNARY));
		lookupTable.put("sin", new LookupTableEntry(0, ESQFOperatorType.UNARY));
		lookupTable.put("not", new LookupTableEntry(0, ESQFOperatorType.UNARY));
		lookupTable.put("!", new LookupTableEntry(0, ESQFOperatorType.UNARY));
		lookupTable.put("isnull", new LookupTableEntry(0, ESQFOperatorType.UNARY));
		lookupTable.put("isnil", new LookupTableEntry(0, ESQFOperatorType.UNARY));
		lookupTable.put("servercommandavailable", new LookupTableEntry(0, ESQFOperatorType.UNARY));
		lookupTable.put("if", new LookupTableEntry(0, ESQFOperatorType.UNARY));
		lookupTable.put("compile", new LookupTableEntry(0, ESQFOperatorType.UNARY));
		lookupTable.put("preprocessfilelinenumbers", new LookupTableEntry(0, ESQFOperatorType.UNARY));
		lookupTable.put("round", new LookupTableEntry(0, ESQFOperatorType.UNARY));
		lookupTable.put("switch", new LookupTableEntry(0, ESQFOperatorType.UNARY));
		lookupTable.put("case", new LookupTableEntry(0, ESQFOperatorType.UNARY));
		lookupTable.put("default", new LookupTableEntry(0, ESQFOperatorType.UNARY));
		lookupTable.put("private", new LookupTableEntry(0, ESQFOperatorType.UNARY));
	}

}
