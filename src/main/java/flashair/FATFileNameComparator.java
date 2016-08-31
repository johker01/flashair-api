package flashair;

import java.util.Comparator;

public class FATFileNameComparator implements Comparator<FATFile> {

	@Override
	public int compare(FATFile o1, FATFile o2) {
		return o1.getFileName().compareTo(o2.getFileName());
	}

}
