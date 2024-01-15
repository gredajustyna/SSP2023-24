package pl.edu.agh.kt;

import pl.edu.agh.kt.IpToPort;
import java.io.IOException;
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

public class PortMappingRest extends ServerResource {
	protected static Logger log = LoggerFactory.getLogger(LabRestServer.class);
	private static final ObjectMapper mapper;

	
	@Get("json")
	public String handleGet() throws JsonProcessingException {
        return null;
	}

	@Post("json")
	public String handlePost(String text) throws JsonProcessingException,
			IOException {
		log.info("CINUS:: handlePost");
		IpToPort[] ipToPort = deserialize(text);
        List<IpToPort> ipToPortList = Arrays.asList(ipToPort);
        SdnLabListener.updateIpToPortMapping(ipToPortList);
		return serialize(ipToPortList);
	}

	static {
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	public static String serialize(List<IpToPort> t) throws JsonProcessingException {
		return mapper.writeValueAsString(t);
	}

	public static IpToPort[] deserialize(String text)
			throws IOException {
		return mapper.readValue(text, IpToPort[].class);
	}
}