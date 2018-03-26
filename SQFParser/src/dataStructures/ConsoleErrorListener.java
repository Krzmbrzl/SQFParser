package dataStructures;

public class ConsoleErrorListener implements IErrorListener {

	@Override
	public void error(String msg, SQFToken token) {
		System.err.println(msg + " - (start: " + token.start + " end: " + token.end + ")");
	}

}
