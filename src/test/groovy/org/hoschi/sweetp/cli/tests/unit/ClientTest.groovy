package org.hoschi.sweetp.cli.tests.unit

import groovyx.net.http.RESTClient
import org.apache.http.conn.HttpHostConnectException
import org.gmock.WithGMock
import org.hoschi.sweetp.cli.Client
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@WithGMock
class ClientTest {
	Client client
	RESTClient server

	@Before
	void setUp() {
		server = mock(RESTClient)
		client = new Client(server)
	}

	@Test
	void callExistingService() {
		server.get(match {it.path == 'services/project/sayhello'}).returns([
				status: 200,
				data: [service: 'hello']
		])

		play {
			assert client.call('project', 'sayhello') == 'hello'
		}
	}

	@Test(expected = HttpHostConnectException)
	void callWrongUrlThrowsException() {
		client = new Client('http://localhost:77/')
		client.call('foo', 'bar')
	}

	@Test
	@Ignore
	@SuppressWarnings('EmptyMethod')
	void callNonExistingService() {
		// can't test this, because HttpResponseException is to heavy to mock
	}

	@Test
	void callServiceWithParameters() {
		server.get(match {it.query.name == 'hoschi'}).returns([
				status: 200,
				data: [service: 'hello hoschi']
		])

		play {
			assert client.call('project', 'sayhello', [name: 'hoschi']) == 'hello hoschi'
		}
	}

	@Test
	void testConfigSend() {
		def config = '{"name":"foo"}'

		server.post(match {it.body == '{"name":"foo"}'}).returns([
				status: 200,
				data: null
		])

		play {
			client.sendConfig(config)
		}
	}
}
