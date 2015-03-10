package org.springframework.xd.analytics.linguistics.langdetect;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Darimont
 */
public class LanguagePriorityParserTests {

	private LanguagePriorityParser languagePriorityParser;

	@Before
	public void setup() {

		languagePriorityParser = new LanguagePriorityParser();
	}

	@Test
	public void testLanguagePrioritiesTranslation_with_configured_translation() {

		Map<String, Double> languagePriorityMap = languagePriorityParser.parseToLanguagePriorityMap("en:0.1,de:0.2,fr:0.3");

		assertThat(languagePriorityMap.get("en"), is((Object) 0.1));
		assertThat(languagePriorityMap.get("de"), is((Object) 0.2));
		assertThat(languagePriorityMap.get("fr"), is((Object) 0.3));
	}

	@Test
	public void testLanguagePrioritiesTranslation_with_no_translation() {

		Map<String, Double> languagePriorityMap = languagePriorityParser.parseToLanguagePriorityMap(null);

		assertThat(languagePriorityMap.size(), is(equalTo(0)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLanguagePrioritiesTranslation_with_corrupt_translation() {

		Map<String, Double> languagePriorityMap = languagePriorityParser.parseToLanguagePriorityMap("aaaa:aaaa,bbb:");

		assertThat(languagePriorityMap.size(), is(equalTo(0)));
	}
}