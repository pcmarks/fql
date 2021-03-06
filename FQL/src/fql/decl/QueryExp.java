package fql.decl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fql.Pair;
import fql.Triple;



public abstract class QueryExp {
	
	public static class Var extends QueryExp {

		String v;
		
		public Var(String v) {
			super();
			this.v = v;
		}

		@Override
		public <R, E> R accept(E env, QueryExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Var other = (Var) obj;
			if (v == null) {
				if (other.v != null)
					return false;
			} else if (!v.equals(other.v))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((v == null) ? 0 : v.hashCode());
			return result;
		}

		@Override
		public String toString() {
			return v;
		}
		
	}
	
	public interface QueryExpVisitor<R,E> {
		public R visit (E env, Const e);
		public R visit (E env, Comp e);
		public R visit (E env, Var e);
	}
	
	public abstract <R, E> R accept(E env, QueryExpVisitor<R, E> v);
	
	@Override
	public abstract boolean equals(Object o);
	
	@Override
	public abstract int hashCode();
	
	public static class Comp extends QueryExp {
		QueryExp l, r;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((l == null) ? 0 : l.hashCode());
			result = prime * result + ((r == null) ? 0 : r.hashCode());
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
			Comp other = (Comp) obj;
			if (l == null) {
				if (other.l != null)
					return false;
			} else if (!l.equals(other.l))
				return false;
			if (r == null) {
				if (other.r != null)
					return false;
			} else if (!r.equals(other.r))
				return false;
			return true;
		}

		public Comp(QueryExp l, QueryExp r) {
			super();
			this.l = l;
			this.r = r;
		}
		
		public String toString() {
			return (l + " then " + r);
		}

		@Override
		public <R, E> R accept(E env, QueryExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

				
	}
	
	
	public static class Const extends QueryExp {
		MapExp delta, sigma, pi;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((delta == null) ? 0 : delta.hashCode());
			result = prime * result + ((pi == null) ? 0 : pi.hashCode());
			result = prime * result + ((sigma == null) ? 0 : sigma.hashCode());
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
			Const other = (Const) obj;
			if (delta == null) {
				if (other.delta != null)
					return false;
			} else if (!delta.equals(other.delta))
				return false;
			if (pi == null) {
				if (other.pi != null)
					return false;
			} else if (!pi.equals(other.pi))
				return false;
			if (sigma == null) {
				if (other.sigma != null)
					return false;
			} else if (!sigma.equals(other.sigma))
				return false;
			return true;
		}

		public Const(MapExp delta, MapExp pi, MapExp sigma) {
			super();
			this.delta = delta;
			this.sigma = sigma;
			this.pi = pi;
		}
		
		@Override
		public String toString() {
			return "delta " + delta + " pi " + pi + " sigma " + sigma;
		}
		
		@Override
		public <R, E> R accept(E env, QueryExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public final Pair<SigExp, SigExp> type(Map<String, SigExp> sigs,
			Map<String, MapExp> maps, Map<String, QueryExp> qs) {
		return accept(new Triple<>(sigs, maps, qs), new QueryExpChecker());
	}
	
	public static class QueryExpChecker implements QueryExpVisitor<Pair<SigExp, SigExp>, Triple<Map<String, SigExp>,
			Map<String, MapExp>, Map<String, QueryExp>>> {
		
		List<String> seen = new LinkedList<>();

		@Override
		public Pair<SigExp, SigExp> visit(
				Triple<Map<String, SigExp>, Map<String, MapExp>, Map<String, QueryExp>> env,
				Const e) {
			Pair<SigExp, SigExp> d = e.delta.type(env.first, env.second);
			Pair<SigExp, SigExp> p = e.pi.type(env.first, env.second);
			Pair<SigExp, SigExp> s = e.sigma.type(env.first, env.second);
			
			if (!d.first.equals(p.first)) {
				throw new RuntimeException("Mismatch: " + d.first + " and " + p.first);
			}
			if (!p.second.equals(s.first)) {
				throw new RuntimeException("Mismatch: " + p.second + " and " + s.first);
			}
			
			return new Pair<>(d.second, s.second);
		}

		@Override
		public Pair<SigExp, SigExp> visit(
				Triple<Map<String, SigExp>, Map<String, MapExp>, Map<String, QueryExp>> env,
				Comp e) {
			List<String> x = new LinkedList<String>(seen);
			Pair<SigExp, SigExp> lt = e.l.accept(env, this);
			seen = x;
			Pair<SigExp, SigExp> rt = e.r.accept(env, this);
			seen = x;
			if (!lt.second.equals(rt.first)) {
				throw new RuntimeException("Mismatch: " + lt.second + " and " + rt.first);
			}
			return new Pair<>(lt.first, rt.second);
		}

		@Override
		public Pair<SigExp, SigExp> visit(
				Triple<Map<String, SigExp>, Map<String, MapExp>, Map<String, QueryExp>> env,
				Var e) {
			if (seen.contains(e.v)) {
				throw new RuntimeException("Circular: " + e.v);
			}
			seen.add(e.v);
			QueryExp q = env.third.get(e.v);
			if (q == null) {
				throw new RuntimeException("Unknown query: " + e.v);
			}
			return q.accept(env, this);
		}
		
	}

}
