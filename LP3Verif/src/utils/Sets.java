package utils;

import java.util.HashSet;
import java.util.Set;

public abstract class Sets {

	public static <T> Set<T> union(Set<T> s1, Set<T> s2) {
		Set<T> result = new HashSet<>(s1);
		result.addAll(s2);
		return result;
	}

	public static <T> Set<T> addElem(Set<T> s, T e) {
		// could throw exception
		Set<T> result = new HashSet<>(s);
		result.add(e);
		return result;
	}

}
