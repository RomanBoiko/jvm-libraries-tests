package vu.collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntProcedure;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class TroveCollections {

	@Test public void mapImplementations() {
		TIntIntHashMap intIntMap = new TIntIntHashMap();
		intIntMap.put(1, 2);

		assertThat(intIntMap.get(1), is(2));

		checkOutForeachOperator(intIntMap);
	}

	@Test public void setImplementations() {
		TIntHashSet intSet = new TIntHashSet();
		intSet.add(1);
		intSet.add(2);

		assertThat(intSet.size(), is(2));
		assertThat(intSet.contains(1), is(true));
		assertThat(intSet.contains(3), is(false));
	}

	private void checkOutForeachOperator(TIntIntHashMap intIntMap) {
		final AtomicInteger entriesCount = new AtomicInteger(0);
		intIntMap.forEachEntry(new TIntIntProcedure() {
			@Override
			public boolean execute(int key, int value) {
				assertThat(key, is(1));
				assertThat(value, is(2));
				entriesCount.incrementAndGet();
				return true;
			}
		});
		assertThat(entriesCount.get(), is(1));
	}

}
