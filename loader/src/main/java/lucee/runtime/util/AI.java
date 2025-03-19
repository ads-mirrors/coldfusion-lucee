package lucee.runtime.util;

import java.util.List;

import lucee.runtime.ai.AIEngine;
import lucee.runtime.ai.Response;
import lucee.runtime.ai.Part;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

public interface AI {

	public void valdate(AIEngine aie, int connectTimeout, int socketTimeout) throws PageException;

	public List<String> getModelNames(AIEngine aie);

	public String findModelName(AIEngine aie, String name);

	public String getModelNamesAsStringList(AIEngine aie);

	public Struct getMetaData(AIEngine aie, boolean addModelsInfo, boolean addFilesInfo) throws PageException;

	public String extractStringAnswer(Response rsp);

	public List<Part> getAnswersFromAnswer(Response rsp);
}
