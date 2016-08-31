package flashair;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileListTest {

	private static List<String> fileList1;
	private static List<String> fileList2;
	private static List<String> fileList3;
	
	@BeforeClass
	public static void beforeClass() {
		fileList1 = new ArrayList<>();
		fileList1.add("WLANSD_FILELIST");
		fileList1.add("/DCIM,100__TSB,0,16,18674,39233");
		fileList1.add("/DCIM,test1,0,32,18698,45563");
		
		fileList2 = new ArrayList<>();
		fileList2.add("WLANSD_FILELIST");
		fileList2.add("/DCIM,100__TSB,0,16,18674,39233");
		fileList2.add("/DCIM,test1,0,32,18698,45563");
		fileList2.add("/DCIM,test2,0,32,18698,45608");
		
		fileList3 = new ArrayList<>();
		fileList3.add("WLANSD_FILELIST");
		fileList3.add("/DCIM,100__TSB,0,16,18674,39233");
		fileList3.add("/DCIM,test1,0,32,18698,45563");
		fileList3.add("/DCIM,test2,0,32,18698,45608");
		fileList3.add("/DCIM,test3,0,32,18698,46405");
	}
	
	@Test
	public void parseFileList() {
		List<FATFile> files1 = FlashAirFATFile.parseFATFiles(fileList1);
		Assert.assertTrue(2 == files1.size());
		
		List<FATFile> files2 = FlashAirFATFile.parseFATFiles(fileList2);
		Assert.assertTrue(3 == files2.size());
		
		List<FATFile> files3 = FlashAirFATFile.parseFATFiles(fileList3);
		Assert.assertTrue(4 == files3.size());
	}
	
	@Test
	public void subtractFileLists() {
		List<FATFile> files1 = FlashAirFATFile.parseFATFiles(fileList1);
		List<FATFile> files2 = FlashAirFATFile.parseFATFiles(fileList2);
		List<FATFile> files3 = FlashAirFATFile.parseFATFiles(fileList3);
		
		List<FATFile> resultList1 = FATFile.subtractListAndOrderResultByCreationTimestamp(files2, files1);
		Assert.assertTrue(1 == resultList1.size());
		Assert.assertEquals("test2", resultList1.get(0).getFileName());
		
		List<FATFile> resultList2 = FATFile.subtractListAndOrderResultByCreationTimestamp(files3, files1);
		Assert.assertTrue(2 == resultList2.size());
		Assert.assertEquals("test2", resultList2.get(0).getFileName());
		Assert.assertEquals("test3", resultList2.get(1).getFileName());
	}
}
