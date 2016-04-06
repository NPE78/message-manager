package com.synaptix.mm.supervision.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.synaptix.mm.engine.MMDictionary;
import com.synaptix.mm.engine.SubDictionary;
import com.synaptix.mm.engine.exception.UnknownDictionaryException;
import com.synaptix.mm.shared.model.IErrorType;
import com.synaptix.mm.supervision.model.DictionaryModelDto;

/**
 * Created by NicolasP on 06/04/2016.
 */
public class DictionaryUtils {

	@Inject
	private static MMDictionary dictionary;

	private DictionaryUtils() {
	}

	public static DictionaryModelDto getDictionaryDto(String subDictionary) throws UnknownDictionaryException {
		SubDictionary dico = dictionary;
		String description = "This is the root dictionary";
		if (StringUtils.isNotBlank(subDictionary)) {
			dico = dictionary.getSubsetDictionary(subDictionary);
			String t = "." + subDictionary;
			description = "This is a sub dictionary of MAIN" +  StringUtils.substring(t, 0, t.lastIndexOf("."));
		}

		Map<String, IErrorType> errorMap = dico.getErrorMap();
		DictionaryModelDto root = new DictionaryModelDto(dico.getDictionaryName());
		root.setDescription(description);
		Map<String, DictionaryModelDto> map = new HashMap<>();
		for (Map.Entry<String, IErrorType> entry : errorMap.entrySet()) {
			DictionaryModelDto currentModel = root;
			String[] dics = entry.getKey().split("\\."); //$NON-NLS-1$
			String r = "";
			for (int i = 0; i < dics.length - 1; i++) {
				String d = dics[i];
				r = (r.isEmpty() ? "" : r + ".") + d;
				DictionaryModelDto newModel = map.get(r);
				if (newModel == null) {
					newModel = new DictionaryModelDto(d);
					currentModel.getSubDictionaryList().add(newModel);
					map.put(r, newModel);
				}
				currentModel = newModel;
			}

			IErrorType value = entry.getValue();
			String v;
			if (value.getNextRecyclingDuration() != null) {
				v = String.format("%s (%s, %d)", value.getCode(), value.getRecyclingKind(), value.getNextRecyclingDuration());
			} else {
				v = String.format("%s (%s)", value.getCode(), value.getRecyclingKind());
			}
			currentModel.getErrorList().add(v);
		}

		return root;
	}
}
