package dataStructures;

import java.util.ArrayList;
import java.util.List;

public class CharacterBuffer implements ICharacterBuffer {

	protected List<Integer> characters;

	public CharacterBuffer() {
		this(100);
	}

	public CharacterBuffer(int initialCapacity) {
		characters = new ArrayList<>(initialCapacity);
	}

	@Override
	public int[] get(int start, int end) {
		int[] characters = new int[end - start];

		for (int i = start; i < end; i++) {
			characters[i] = this.characters.get(i);
		}

		return characters;
	}

	@Override
	public char[] getChar(int start, int end) {
		char[] characters = new char[end - start];

		for (int i = start; i < end; i++) {
			characters[i] = (char) (int) this.characters.get(i);
		}

		return characters;
	}

	@Override
	public int get(int index) {
		return characters.get(index);
	}

	@Override
	public char getChar(int index) {
		return (char) (int) characters.get(index);
	}

	@Override
	public int[] get(SQFToken token) {
		return get(token.start, token.stop());
	}

	@Override
	public void append(char c) {
		append((int) c);
	}

	@Override
	public void append(int c) {
		characters.add((int) c);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(characters.size());

		for (int c : characters) {
			builder.append((char) c);
		}

		return builder.toString();
	}

	@Override
	public String getText(int start, int length) {
		StringBuilder builder = new StringBuilder(length);

		for (int i = start; i < start + length; i++) {
			builder.append((char) (int) this.characters.get(i));
		}

		return builder.toString();
	}
}
