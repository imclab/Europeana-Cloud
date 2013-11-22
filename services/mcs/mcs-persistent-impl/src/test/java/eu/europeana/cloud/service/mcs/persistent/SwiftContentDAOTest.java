package eu.europeana.cloud.service.mcs.persistent;

import eu.europeana.cloud.common.model.File;
import eu.europeana.cloud.service.mcs.exception.FileNotExistsException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 
 * @author olanowak
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = { "classpath:/swiftTestContext.xml" })
public class SwiftContentDAOTest
{

	@Autowired
	private SwiftContentDAO instance;


	@Test
	public void shouldPutAndGetContent()
		throws Exception
	{
		String fileName = "someFileName";
		byte[] content = ("This is a test content").getBytes("UTF-8");
		InputStream is = new ByteArrayInputStream(content);

		File file = new File();
		instance.putContent(fileName, file, is);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		instance.getContent(fileName, -1, -1, os);
		assertTrue(Arrays.equals(content, os.toByteArray()));

		String md5Hex = DigestUtils.md5Hex(content);
		//check if file md5 got updated
		assertNotNull(file.getMd5());
		//check if md5 in file is correct
		assertEquals(file.getMd5(), md5Hex);
		//check if md5 in file is correct
		assertEquals(md5Hex, DigestUtils.md5Hex(os.toByteArray()));
	}


	public void shouldRetrieveRangeOfBytes()
		throws Exception
	{
		String fileName = "rangeFile";
		String content = "This is a test content";
		InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));

		File file = new File();
		instance.putContent(fileName, file, is);

		int from = -1;
		int to = -1;
		checkRange(from, to, content.getBytes(), fileName);

		from = -1;
		to = 3;
		checkRange(from, to, content.getBytes(), fileName);

		from = 3;
		to = -1;
		checkRange(from, to, content.getBytes(), fileName);

		from = 2;
		to = 2;
		checkRange(from, to, content.getBytes(), fileName);

		from = 3;
		to = 6;
		checkRange(from, to, content.getBytes(), fileName);
	}


	private void checkRange(int from, int to, byte[] expected, String fileName)
		throws Exception
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		instance.getContent(fileName, from, to, os);

		int rangeStart = from;
		int rangeEnd = to + 1;
		if (from == -1) {
			rangeStart = 0;
		}
		if (to == -1) {
			rangeEnd = expected.length;
		}
		byte[] rangeOfContent = Arrays.copyOfRange(expected, rangeStart, rangeEnd);
		assertTrue(String.format("Ranges not equal %d-%d", from, to), Arrays.equals(rangeOfContent, os.toByteArray()));
	}


	@Test(expected = FileNotExistsException.class)
	public void testDeleteContent()
		throws Exception
	{
		String objectId = "to_delete";
		File file = new File();
		String content = "This is a test content";
		InputStream is = new ByteArrayInputStream(content.getBytes());
		instance.putContent(objectId, file, is);
		instance.deleteContent(objectId);
		instance.getContent(objectId, -1, -1, null);
	}


	@Test(expected = FileNotExistsException.class)
	public void shouldThrowNotFoundExpWhenGettingNotExistingFile()
		throws Exception
	{
		String objectId = "not_exist";
		instance.getContent(objectId, -1, -1, null);
	}


	@Test(expected = FileNotExistsException.class)
	public void shouldThrowNotFoundExpWhenDeletingNotExistingFile()
		throws Exception
	{
		String objectId = "not_exist";
		instance.deleteContent(objectId);
	}


	@Test(expected = FileNotExistsException.class)
	public void shouldThrowNotFoundExpWhenCopingNotExistingFile()
		throws Exception
	{
		String objectId = "not_exist";
		String trg = "trg_name";
		instance.copyContent(objectId, trg);
	}


	@Test
	public void testCopyContent()
		throws Exception
	{
		String sourceObjectId = "sourceObjectId";
		String trgObjectId = "trgObjectId";
		String content = "This is a test content";
		InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));

		File file = new File();
		//input source object
		instance.putContent(sourceObjectId, file, is);
		//copy object
		instance.copyContent(sourceObjectId, trgObjectId);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		instance.getContent(trgObjectId, -1, -1, os);
		String result = os.toString("utf-8");
		assertEquals(content, result);
	}
}