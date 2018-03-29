package dataStructures;

public class SQFTestTokenFactory extends AbstractSQFTokenFactory {
	
	public SQFTestTokenFactory(CharacterBuffer buffer) {
		super(buffer);
	}
	
	public SQFTestTokenFactory() {
		super();
	}

	@Override
	public void doInitialize() {
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
		lookupTable.put("setpos", BINARY);
		lookupTable.put("then", BINARY);
		lookupTable.put("setpos", BINARY);
		lookupTable.put("call", BINARY);
		lookupTable.put("params", BINARY);
		lookupTable.put("spawn", BINARY);
		lookupTable.put("isequalto", BINARY);
		lookupTable.put("execvm", BINARY);
		lookupTable.put("select", BINARY);
		lookupTable.put("random", BINARY);
		lookupTable.put("foreach", BINARY);
		lookupTable.put("do", BINARY);
		lookupTable.put(":", BINARY);
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
		
		lookupTable.put("hint", UNARY);
		lookupTable.put("str", UNARY);
		lookupTable.put("cos", UNARY);
		lookupTable.put("sin", UNARY);
		lookupTable.put("not", UNARY);
		lookupTable.put("!", UNARY);
		lookupTable.put("isnull", UNARY);
		lookupTable.put("isnil", UNARY);
		lookupTable.put("servercommandavailable", UNARY);
		lookupTable.put("if", UNARY);
		lookupTable.put("compile", UNARY);
		lookupTable.put("preprocessfilelinenumbers", UNARY);
		lookupTable.put("round", UNARY);
		lookupTable.put("switch", UNARY);
		lookupTable.put("case", UNARY);
		lookupTable.put("default", UNARY);
		lookupTable.put("private", UNARY);
	}

}
