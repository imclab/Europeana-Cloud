package eu.europeana.cloud.client.uis.rest.console.commands;

import javax.naming.directory.InvalidAttributesException;

import eu.europeana.cloud.client.uis.rest.CloudException;
import eu.europeana.cloud.client.uis.rest.UISClient;
import eu.europeana.cloud.common.model.LocalId;

/**
 * Retrieval of record ids by provider console command
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class GetRecordIdsByProviderCommand extends Command {

	@Override
	public void execute(UISClient client,int threadNo,String... input) throws InvalidAttributesException {
		if(input.length<1){
			throw new InvalidAttributesException();
		}
		try {
			for (LocalId cId : client.getRecordIdsByProvider(input[0]).getResults()) {
				System.out.println(cId.toString());
			}
		} catch (CloudException e) {
			getLogger().error(e.getMessage());
		}

	}

}
