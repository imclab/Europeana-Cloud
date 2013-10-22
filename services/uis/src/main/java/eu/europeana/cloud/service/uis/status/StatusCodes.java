package eu.europeana.cloud.service.uis.status;

import javax.ws.rs.core.Response.Status;

import eu.europeana.cloud.common.response.StatusCode;

/**
 * Status Messages returned by all methods
 * 
 * @author Yorgos.Mamakis@kb.nl
 * @since Oct 22, 2013
 */
public enum StatusCodes {
	/**
	 * OK message - HTTP Code: 200
	 */
	OK {
		@Override
		public StatusCode getStatusCode(String... args) {
			return new StatusCode("OK", "Operation finished successfully", Status.OK);
		}
	},

	/**
	 * Unspecified Error - HTTP Code: 500
	 */
	GENERICERROR {
		@Override
		public StatusCode getStatusCode(String... args) {
			return new StatusCode("GENERICERROR", String.format(
					"Unspecified Error occured with message %s", args[0]),
					Status.INTERNAL_SERVER_ERROR);
		}
	},

	/**
	 * Database Connection Error - HTTP Code: 500
	 */
	DATABASECONNECTIONERROR {
		@Override
		public StatusCode getStatusCode(String... args) {
			return new StatusCode("DATABASECONNECTIONERROR", String.format(
					"The connection to the DB %s:%s/%s failed with error %s",
					args[0], args[1], args[2], args[3]),
					Status.INTERNAL_SERVER_ERROR);
		}
	},

	/**
	 * Record exists already in the database - HTTP code: 409
	 */
	RECORDEXISTS {
		@Override
		public StatusCode getStatusCode(String... args) {
			return new StatusCode(
					"RECORDEXISTS",
					String.format(
							"An identifier for provider id %s and record id %s already exists in the database",
							args[0], args[1]), Status.CONFLICT);
		}
	},

	/**
	 * The record does not exist in the database - HTTP code: 404
	 */
	RECORDDOESNOTEXIST {
		@Override
		public StatusCode getStatusCode(String... args) {
			return new StatusCode(
					"RECORDDOESNOTEXIST",
					String.format(
							"A global identifier for provider id %s and record id %s does not exist",
							args[0], args[1]), Status.NOT_FOUND);
		}
	},

	/**
	 * The supplied unique identifier does not exist - HTTP code: 404
	 */
	GLOBALIDDOESNOTEXIST {
		@Override
		public StatusCode getStatusCode(String... args) {
			return new StatusCode("GLOBALIDDOESNOTEXIST",
					String.format(
							"The supplied global identifier %s does not exist",
							args[0]), Status.NOT_FOUND);
		}
	},

	/**
	 * The provider id does not exist - HTTP code: 404
	 */
	PROVIDERDOESNOTEXIST {
		@Override
		public StatusCode getStatusCode(String... args) {
			return new StatusCode("PROVIDERDOESNOTEXIST", String.format(
					"The supplied provider identifier %s does not exist",
					args[0]), Status.NOT_FOUND);
		}
	},

	/**
	 * The record id does not exist - HTTP code: 404
	 */
	RECORDIDDOESNOTEXIST {
		@Override
		public StatusCode getStatusCode(String... args) {
			return new StatusCode("RECORDIDDOESNOTEXIST",
					String.format(
							"The supplied record identifier %s does not exist",
							args[0]), Status.NOT_FOUND);
		}

	},

	/**
	 * The requested record set for the provider id is empty - HTTP code: 404
	 */
	RECORDSETEMPTY {

		@Override
		public StatusCode getStatusCode(String... args) {
			return new StatusCode(
					"RECORDSETEMPTY",
					String.format(
							"The supplied provider %s does not have any records associated with it",
							args[0]), Status.NOT_FOUND);
		}
	},

	/**
	 * The combination of provider id/ record id has already been mapped to
	 * another unique identifier - HTTP code: 409
	 */
	IDHASBEENMAPPED {

		@Override
		public StatusCode getStatusCode(String... args) {
			return new StatusCode(
					"IDHASBEENMAPPED",
					String.format(
							"The supplied %s id for provider %s has already been assigned to the global identifier %s",
							args[0], args[1], args[2]), Status.CONFLICT);
		}
	};

	/**
	 * Return the a StatusCode object with the message or reply
	 * 
	 * @return http code
	 */
	public abstract StatusCode getStatusCode(String... args);
}