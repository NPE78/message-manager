package com.talanlabs.mm.supervision.utils;

import com.talanlabs.mm.engine.MMDictionary;
import com.talanlabs.mm.engine.SubDictionary;
import com.talanlabs.mm.engine.exception.UnknownDictionaryException;
import com.talanlabs.mm.shared.model.IErrorType;
import com.talanlabs.mm.supervision.model.DictionaryModelDto;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by NicolasP on 06/04/2016.
 */
public class DictionaryUtils {


	private DictionaryUtils() {
	}

	public static DictionaryModelDto getDictionaryDto(MMDictionary dictionary, String subDictionary) throws UnknownDictionaryException {
		SubDictionary dico = dictionary;
		String description = "This is the root dictionary";
		String s = "MAIN";
		if (StringUtils.isNotBlank(subDictionary)) {
			dico = dictionary.getSubsetDictionary(subDictionary);
			s = s + "." + subDictionary;
			description = "This is a sub dictionary of " + StringUtils.substring(s, 0, s.lastIndexOf(".")); //$NON-NLS-2$
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
					String t = s + "." + r;
					newModel.setDescription("This is a sub dictionary of " + StringUtils.substring(t, 0, t.lastIndexOf(".")));
					currentModel.addDictionaryModelDto(newModel);
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
			currentModel.addError(v);
		}

		return root;
	}
}
