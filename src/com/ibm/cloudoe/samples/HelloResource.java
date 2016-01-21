package com.ibm.cloudoe.samples;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.storage.object.*;
import  org.openstack4j.model.common.Identifier;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;

@Path("/hello")
public class HelloResource {

	@GET
	public String getInformation() {

		// 'VCAP_APPLICATION' is in JSON format, it contains useful information about a deployed application
		String envApp = System.getenv("VCAP_APPLICATION");

		// 'VCAP_SERVICES' contains all the credentials of services bound to this application.
		String envServices = System.getenv("VCAP_SERVICES");

		// LOAD VCAP STRING INTO JSON
		JSONParser parser = new JSONParser();

		try{
            Object obj = parser.parse(envServices);
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray vcapArray = (JSONArray) jsonObject.get("Object-Storage");
            JSONObject vcap = (JSONObject) vcapArray.get(0);
            JSONObject credentials = (JSONObject) vcap.get("credentials");
            String username = credentials.get("username").toString();
            String password = credentials.get("password").toString();
            String auth_url = credentials.get("auth_url").toString() + "/v3";
            String domain = credentials.get("domainName").toString();
            String project = credentials.get("projectName").toString();
            Identifier domainIdent = Identifier.byName(domain);
            Identifier projectIdent = Identifier.byName(project);


            OSClient os = OSFactory.builderV3()
                    .endpoint(auth_url)
                    .credentials(username, password)
                    .scopeToProject(projectIdent, domainIdent)
                    .authenticate();

            SwiftAccount account = os.objectStorage().account().get();
            os.objectStorage().containers().create("mySampleDevworksContainer");
            List<? extends SwiftContainer> containers = os.objectStorage().containers().list();
            SwiftContainer container = containers.get(0);


            return "Connected to object storage and created sample container:" + container.getName();

        }catch (Exception e){
            e.printStackTrace();
        }
		return "Hi World, have fun with Object Storage and Java";

	}
}