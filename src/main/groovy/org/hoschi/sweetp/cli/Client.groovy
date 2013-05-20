package org.hoschi.sweetp.cli

import groovy.util.logging.Log4j
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.conn.scheme.Scheme
import org.hoschi.sweetp.cli.exceptions.NoServiceFoundException

@Typed
@Log4j
class Client {
	RESTClient server

	Client(String url) {
		log.info "connected to $url"
		server = new RESTClient(url)
	}

	Client(RESTClient server) {
		this.server = server
	}

	/**
	 * Enable insecure trust.
	 */
	void trustAllSslCertificates() {
		server.client.connectionManager.schemeRegistry.register(
				new Scheme('https', TrustAllSslCertificatesFactory.instance, 443)
		)
	}

	/**
	 * Calls a non interactive service as client of sweetp server.
	 *
	 * @param name of service as url path
	 * @return output of service call
	 */
	@Typed(TypePolicy.DYNAMIC) // with "mixed" catch clause didn't work
	String call(String project, String name, Map query = [:]) {
		log.info "call service $name"
		try {
			def resp = server.get(
					path: "services/$project/$name",
					query: (query),
					headers: [Accept: 'application/json']
			)
			assert resp.status == 200
			log.info "got response with status 200, data is $resp.data"

			return resp.data.service
		} catch (HttpResponseException ex) {
            RuntimeException runtimeException
			if (ex.response.status == 404) {
				throw new NoServiceFoundException(name)
			} else if (ex.response.status == 500) {
                runtimeException = new RuntimeException("Error: $ex.response.data.service")
            } else {
				runtimeException = new RuntimeException("An error occured during reuqest to $name with status code $ex.response.status. Have a look at the server logs or the stack trace of this exception.")
			}
            runtimeException.setStackTrace(ex.stackTrace)
            throw runtimeException
		}
	}

	/**
	 * Send a config to update the server cache file.
	 *
	 * @param config to send
	 */
	@Typed(TypePolicy.MIXED)
	void sendConfig(String config) {
		log.info "send config to server with: $config"
		try {
			def resp = server.post(
					path: 'configs',
					body: config,
					requestContentType: groovyx.net.http.ContentType.JSON,
			)
			assert resp.status == 200
			log.info 'got response with status 200'
		} catch (HttpResponseException ex) {
			def runtimeException = new RuntimeException("An error occured with status code $ex.response.status. Have a look at the server logs or the stack trace of this exception.")
			runtimeException.setStackTrace(ex.stackTrace)
			throw runtimeException

		}
	}
}
