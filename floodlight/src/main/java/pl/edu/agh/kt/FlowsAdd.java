package pl.edu.agh.kt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class FlowsAdd extends ServerResource {
	protected static Logger log = LoggerFactory.getLogger(LabRestServer.class);
	private static final ObjectMapper mapper;

	
	@Get("json")
	public String handleGet() throws JsonProcessingException {
        return null;
	}

	@Post("json")
	public String handlePost(String text) throws JsonProcessingException,
			IOException {
		log.info("SDN_PROJ:: handle POST in /flows/add");
		FlowData[] flowsEntries = deserialize(text);
        ArrayList<FlowData> flowsEntriesList = new ArrayList<>(Arrays.asList(flowsEntries));
        //SdnLabListener.updateIpToPortMapping(ipToPortList);
        System.out.println(flowsEntriesList.toArray().toString());
        FlowsDb.addFlowEntries(flowsEntriesList);
        FlowsDb.printFlowInfoAll();
		return serialize(flowsEntriesList);
	}

	static {
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	public static String serialize(List<FlowData> t) throws JsonProcessingException {
		return mapper.writeValueAsString(t);
	}

	public static FlowData[] deserialize(String text)
			throws IOException {
		return mapper.readValue(text, FlowData[].class);
	}
}