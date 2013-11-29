package eu.europeana.cloud.service.mcs.persistent;

import eu.europeana.cloud.common.model.File;
import eu.europeana.cloud.common.model.Representation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.solr.client.solrj.SolrServerException;
import static org.hamcrest.Matchers.is;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = {"classpath:/solrTestContext.xml"})
public class SolrDAOSearchTest {

	@Autowired
	private SolrDAO solrDAO;

	@Autowired
	private SolrConnectionProvider connectionProvider;


	@After
	public void deleteData()
			throws IOException, SolrServerException {
		connectionProvider.getSolrServer().deleteByQuery("*:*");
	}


	@Test
	public void searchBySchema()
			throws IOException, SolrServerException {
		Representation r1 = insertRepresentation("c1", "dc", "v1", "dp", true, new Date());
		Representation r2 = insertRepresentation("c1", "dc", "v2", "dp", true, new Date());
		Representation r3 = insertRepresentation("c1", "jpg", "v3", "dp", true, new Date());
		Representation r4 = insertRepresentation("c1", "jpg", "v4", "dp", true, new Date());

		List<Representation> foundRepresentations
				= solrDAO.search(RepresentationSearchParams.builder().setSchema("dc").build(), 0, 10);
		assertSameContent(Arrays.asList(r1, r2), foundRepresentations);
	}


	@Test
	public void searchByProvider()
			throws IOException, SolrServerException {
		Representation r1 = insertRepresentation("c1", "dc", "v1", "dp1", true, new Date());
		Representation r2 = insertRepresentation("c1", "dc", "v2", "dp", true, new Date());
		Representation r3 = insertRepresentation("c1", "jpg", "v3", "dp", true, new Date());
		Representation r4 = insertRepresentation("c1", "jpg", "v4", "dp1", true, new Date());

		List<Representation> foundRepresentations = solrDAO.search(RepresentationSearchParams.builder().
				setDataProvider("dp1").build(), 0, 10);
		assertSameContent(Arrays.asList(r1, r4), foundRepresentations);
	}


	@Test
	public void searchBySchemaAndProvider()
			throws Exception {
		Representation r1 = insertRepresentation("c1", "dc", "v1", "dp1", true, new Date());
		Representation r2 = insertRepresentation("c1", "dc", "v2", "dp", true, new Date());
		Representation r3 = insertRepresentation("c1", "jpg", "v3", "dp", true, new Date());
		Representation r4 = insertRepresentation("c1", "jpg", "v4", "dp1", true, new Date());

		List<Representation> foundRepresentations = solrDAO.
				search(RepresentationSearchParams.builder().setDataProvider("dp1").setSchema("dc").build(), 0, 10);
		assertSameContent(Arrays.asList(r1), foundRepresentations);
	}


	@Test
	public void searchByPersistent()
			throws Exception {
		Representation r1 = insertRepresentation("c1", "dc", "v1", "dp1", true, new Date());
		Representation r2 = insertRepresentation("c1", "dc", "v2", "dp", false, new Date());

		List<Representation> onlyPersistent = solrDAO.
				search(RepresentationSearchParams.builder().setSchema("dc").setPersistent(Boolean.TRUE).build(), 0, 10);
		assertSameContent(Arrays.asList(r1), onlyPersistent);
		List<Representation> onlyNotPersistent = solrDAO.
				search(RepresentationSearchParams.builder().setSchema("dc").setPersistent(Boolean.FALSE).build(), 0, 10);
		assertSameContent(Arrays.asList(r2), onlyNotPersistent);
		List<Representation> regardlessPersistence = solrDAO.
				search(RepresentationSearchParams.builder().setSchema("dc").build(), 0, 10);
		assertSameContent(Arrays.asList(r1, r2), regardlessPersistence);

	}


	@Test
	public void searchByDate()
			throws IOException, SolrServerException {
		Calendar c = GregorianCalendar.getInstance();
		c.set(2000, 05, 15, 15, 15);
		Representation r1 = insertRepresentation("c1", "dc", "v1", "dp1", true, c.getTime());
		c.set(2000, 05, 15, 15, 17);
		Representation r2 = insertRepresentation("c1", "dc", "v2", "dp1", true, c.getTime());
		c.set(2001, 05, 15, 15, 0);
		Representation r3 = insertRepresentation("c1", "dc", "v3", "dp1", true, c.getTime());
		c.set(2002, 05, 15, 15, 15);
		Representation r4 = insertRepresentation("c1", "dc", "v4", "dp1", true, c.getTime());
		List<Representation> foundRepresentations = solrDAO.
				search(RepresentationSearchParams.builder()
						.setFromDate(r2.getCreationDate())
						.setToDate(r3.getCreationDate())
						.build(), 0, 10);
		assertSameContent(foundRepresentations, Arrays.asList(r2, r3));
	}


	@Test
	public void shouldIterateThroughAllRepresentations()
			throws Exception {
		int count = 50;
		Set<Representation> generatedRepresentations = new HashSet<>(count, 1f);
		for (int i = 0; i < count; i++) {
			generatedRepresentations.
					add(insertRepresentation("id", "dc", UUID.randomUUID().toString(), "dp", true, new Date()));
		}
		RepresentationSearchParams searchParams = RepresentationSearchParams.builder().setSchema("dc").build();
		Set<Representation> foundRepresentations = new HashSet<>(count, 1f);
		boolean hasNext = true;
		int offset = 0;
		int pageLimit = 8;
		while (hasNext) {
			List<Representation> searchResults = solrDAO.search(searchParams, offset, pageLimit);;
			foundRepresentations.addAll(searchResults);
			hasNext = !searchResults.isEmpty();
			offset += pageLimit;
		}
		assertThat(foundRepresentations.size(), is(count));
		assertThat(foundRepresentations, is(generatedRepresentations));
	}


	private <T> void assertSameContent(Collection<? extends T> actual, Collection<? extends T> expected) {
		Set<T> actualSet = new HashSet<>(actual);
		Set<T> expectedSet = new HashSet<>(expected);
		assertThat(actualSet, is(expectedSet));
	}


	private Representation insertRepresentation(String cloudId, String schema, String version, String dataProvider, boolean persistent, Date date)
			throws IOException, SolrServerException {
		Representation rep = new Representation(cloudId, schema, version, null, null, dataProvider, new ArrayList<File>(), persistent, date);
		solrDAO.insertRepresentation(rep, null);
		return rep;
	}

}
