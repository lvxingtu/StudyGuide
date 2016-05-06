package com.oskopek.studyguide.persistence;

import com.oskopek.studyguide.model.StudyPlan;
import com.oskopek.studyguide.model.courses.CourseRegistry;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * A simple and stable local integration test for {@link MFFHtmlScraper}.
 */
public class MFFHtmlScraperTest {

    private final String mffIoiInfoPath = "src/test/resources/com/oskopek/studyguide/persistence/ioi1516.html";
    private final String sisUrl = "file://" + Paths.get(".").toAbsolutePath()
            + "/src/test/resources/com/oskopek/studyguide/persistence/siscopy";
    private final String referenceFile
            = "src/test/resources/com/oskopek/studyguide/persistence/mff_bc_ioi_2015_2016.json";
    private MFFHtmlScraper scraper;

    @Before
    public void setUp() {
        scraper = new MFFHtmlScraper(sisUrl);
    }

    @Test
    public void testScrapeCourses() throws Exception {
        CourseRegistry registry = scraper.scrapeStudyPlan(Paths.get(mffIoiInfoPath)).getCourseRegistry();
        assertNotNull(registry);
        assertNotNull(registry.courseMapValues());
        assertEquals(70, registry.courseMapValues().size()); // 61 base from IOI + 4 required courses + random deps
        StudyPlan studyPlan = new JsonDataReaderWriter().readFrom(referenceFile);
        assertArrayEquals(studyPlan.getCourseRegistry().courseMapValues().toArray(),
                registry.courseMapValues().toArray());
    }

}
