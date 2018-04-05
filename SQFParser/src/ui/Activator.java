package ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import dataStructures.CharacterInputStream;
import dataStructures.SQFTestTokenFactory;
import lexer.SQFLexer;
import parser.SQFParser;

public class Activator {
	public static void main(String[] args) throws IOException {
		SQFLexer lexer = new SQFLexer();
		lexer.setTokenFactory(new SQFTestTokenFactory());
		SQFParser parser = new SQFParser();
		
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("leader group player setPos getPos vehicle player; hint 'Test';{2+3;4-5;}".getBytes())));
		parser.parse(lexer);
		
		Display display = new Display();
		
		// open TreeUI
		Shell treeUIShell = new Shell(display);
		treeUIShell.setLayout(new FillLayout());
		
		new TreeUI(treeUIShell, SWT.BORDER, parser.tree(), lexer);
		
		treeUIShell.open();

		// run the event loop as long as the window is open
		while (!treeUIShell.isDisposed()) {
			// read the next OS event queue and transfer it to a SWT event
			if (!display.readAndDispatch()) {
				// if there are currently no other OS event to process
				// sleep until the next OS event is available
				display.sleep();
			}
		}
	}

}
