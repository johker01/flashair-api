package flashair;

import java.util.Comparator;

public class FATFileCreationDateComparator implements Comparator<FATFile> {

	@Override
	public int compare(FATFile o1, FATFile o2) {
		return o1.getCreationTimestamp().compareTo(o2.getCreationTimestamp());
	}

}
