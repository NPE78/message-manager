package com.talanlabs.mm.supervision.utils;

import com.talanlabs.mm.engine.MMDictionary;
import com.talanlabs.mm.engine.SubDictionary;
import com.talanlabs.mm.engine.model.DefaultErrorType;
import com.talanlabs.mm.shared.model.domain.ErrorRecyclingKind;
import com.talanlabs.mm.supervision.model.DictionaryModelDto;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by NicolasP on 29/10/2015.
 */
public class DictionaryUtilsTest {

	@Test
	public void testDictionaryUtils() throws Exception {
        MMDictionary dictionary = new MMDictionary();
		dictionary.defineError(new DefaultErrorType("TEST", ErrorRecyclingKind.NOT_RECYCLABLE));

		SubDictionary sub = dictionary.addSubsetDictionary("sub");

		SubDictionary ter = sub.addSubsetDictionary("ter");
		ter.defineError(new DefaultErrorType("TEST2", ErrorRecyclingKind.AUTOMATIC, 120));

		DictionaryModelDto dictionaryDto = DictionaryUtils.getDictionaryDto(dictionary, null);
		Assert.assertEquals("MAIN", dictionaryDto.getDictionaryName());
		Assert.assertEquals("This is the root dictionary", dictionaryDto.getDescription());
		Assert.assertEquals(1, dictionaryDto.getErrorList().size());
		Assert.assertTrue(dictionaryDto.getErrorList().get(0).startsWith("TEST "));
		Assert.assertEquals(1, dictionaryDto.getSubDictionaryList().size());
		Assert.assertEquals("sub", dictionaryDto.getSubDictionaryList().get(0).getDictionaryName());
		Assert.assertEquals(0, dictionaryDto.getSubDictionaryList().get(0).getErrorList().size());
		Assert.assertEquals(1, dictionaryDto.getSubDictionaryList().get(0).getSubDictionaryList().size());
		Assert.assertEquals(1, dictionaryDto.getSubDictionaryList().get(0).getSubDictionaryList().get(0).getErrorList().size());
		Assert.assertTrue(dictionaryDto.getSubDictionaryList().get(0).getSubDictionaryList().get(0).getErrorList().get(0).startsWith("TEST2 "));

		dictionaryDto = DictionaryUtils.getDictionaryDto(dictionary, "sub");
		Assert.assertEquals("sub", dictionaryDto.getDictionaryName());
		Assert.assertEquals("This is a sub dictionary of MAIN", dictionaryDto.getDescription());
		Assert.assertEquals(0, dictionaryDto.getErrorList().size());
		Assert.assertEquals(1, dictionaryDto.getSubDictionaryList().size());
		Assert.assertEquals(1, dictionaryDto.getSubDictionaryList().get(0).getErrorList().size());
		Assert.assertTrue(dictionaryDto.getSubDictionaryList().get(0).getErrorList().get(0).startsWith("TEST2 "));

		dictionaryDto = DictionaryUtils.getDictionaryDto(dictionary, "sub.ter");
		Assert.assertEquals("ter", dictionaryDto.getDictionaryName());
		Assert.assertEquals("This is a sub dictionary of MAIN.sub", dictionaryDto.getDescription());
	}
}
