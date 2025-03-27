package PatternEnumerationBit;

import java.util.HashSet;
import java.util.Set;

public class UniqueSet extends HashSet<Long> {

    public UniqueSet(Set<Long> h) {
        super(h);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Set)) return false;
        Set<?> that = (Set<?>) o;
        return this.size() == that.size() && this.containsAll(that);
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (Long element : this) {
            result += element != null ? element.hashCode() : 0;
        }
        return result;
    }
}