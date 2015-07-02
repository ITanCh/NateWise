package com.nata.wise.strategy.DFS;

public class Link {
	int from;
	int to;

	public Link(int f, int t) {
		from = f;
		to = t;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + from;
		result = prime * result + to;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Link other = (Link) obj;

		if (from != other.from)
			return false;
		if (to != other.to)
			return false;
		return true;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	@Override
	public String toString() {
		return "{ source:" + from + ", target:" + to + "}";
	}
}