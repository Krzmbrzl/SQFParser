package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import dataStructures.CharacterInputStream1;
import dataStructures.ESQFOperatorType;
import dataStructures.SQFToken;
import dataStructures.TokenBuffer;
import lexer.SQFLexer;

class TokenTest {

	@Test
	void arithmetics() throws IOException {
		SQFLexer lexer = new SQFLexer(new CharacterInputStream1(new ByteArrayInputStream("2+3*4^2/5-7".getBytes())));

		TokenBuffer<SQFToken> buffer = lexer.getTokens();

		int[] precedences = new int[] { 0, 3, 0, 2, 0, 1, 0, 2, 0, 3, 0 };
		ESQFOperatorType[] types = new ESQFOperatorType[] { ESQFOperatorType.NULAR, ESQFOperatorType.BINARY,
				ESQFOperatorType.NULAR, ESQFOperatorType.BINARY, ESQFOperatorType.NULAR, ESQFOperatorType.BINARY,
				ESQFOperatorType.NULAR, ESQFOperatorType.BINARY, ESQFOperatorType.NULAR, ESQFOperatorType.BINARY,
				ESQFOperatorType.NULAR };

		for (int i = 0; i < buffer.size(); i++) {
			SQFToken token = buffer.get(i);

			assertEquals(precedences[i], token.precendence(), "Wrong precedence!");
			assertEquals(types[i], token.operatorType(), "Wrong operator type!");
		}
	}

}
