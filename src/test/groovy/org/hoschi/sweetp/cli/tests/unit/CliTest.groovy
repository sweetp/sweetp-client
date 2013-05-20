package org.hoschi.sweetp.cli.tests.unit

import org.gmock.WithGMock
import org.hoschi.sweetp.cli.Cli
import org.hoschi.sweetp.cli.Client
import org.hoschi.sweetp.cli.Config
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@WithGMock
class CliTest {
	Cli cli
	PrintStream out
	PrintStream err

	@Before
	void setUp() {
		out = new PrintStream(new ByteArrayOutputStream())
		err = new PrintStream(new ByteArrayOutputStream())
		System.out = out
		System.err = err
		cli = new Cli()
	}

	@Test
	void usageTextContainsExamplesAndHelpForOptions() {
		Config config = mock(Config, invokeConstructor(null))
		cli.config = config

		cli.action(['-h'] as String[])

		assert err.out.toString().isEmpty()
		['help', 'Options', 'Examples', 'service'].each {
			assert out.out.toString().contains(it)
		}

		cli.action([] as String[])

		assert err.out.toString().isEmpty()
		['help', 'Options', 'Examples', 'service'].each {
			assert out.out.toString().contains(it)
		}
	}

	@Test
	void initProjectWithName() {
		Config config = mock(Config, invokeConstructor(null))
		config.initProject('foo')
		cli.config = config

		play {
			cli.action(['-init', 'foo'] as String[])
		}
	}

	@Test
	void callServiceWithProjectName() {
		Client client = mock(Client, invokeConstructor(''))
		client.call('foo', 'sayhello', [:]).returns('hello')
		cli.client = client

		Config config = mock(Config, invokeConstructor(client))
		cli.config = config

		play {
			cli.action(['-p', 'foo', 'sayhello'] as String[])
			assert err.out.toString().isEmpty()
			assert out.out.toString() == 'hello\n'
		}
	}

	@Test
	void callServiceWithTheServiceName() {
		Client client = mock(Client, invokeConstructor(''))
		client.call('foo', 'sayhello', [:]).returns('hello')
		cli.client = client

		Config config = mock(Config, invokeConstructor(client))
		config.checkForUpdates().returns('foo')
		cli.config = config

		play {
			cli.action(['sayhello'] as String[])
			assert err.out.toString().isEmpty()
			assert out.out.toString() == 'hello\n'
		}
	}

	@Test
	void callServiceWithSpaceSeparatedName() {
		Client client = mock(Client, invokeConstructor(''))
		client.call('foo', 'say/hello', [:]).returns('hello')
		cli.client = client

		Config config = mock(Config, invokeConstructor(client))
		config.checkForUpdates().returns('foo')
		cli.config = config

		play {
			cli.action(['say', 'hello'] as String[])
			assert err.out.toString().isEmpty()
			assert out.out.toString() == 'hello\n'
		}
	}

	@Test
	void callServiceWithParameters() {
		Client client = mock(Client, invokeConstructor(''))
		client.call('foo', 'sayhello', [zonk: ['a'], name: ['foo']]).returns('hello')
		cli.client = client

		Config config = mock(Config, invokeConstructor(client))
		config.checkForUpdates().returns('foo')
		cli.config = config

		play {
			cli.action(['-P name=foo', '-Pzonk=a ', 'sayhello'] as String[])
			assert err.out.toString().isEmpty()
			assert out.out.toString() == 'hello\n'
		}
	}

	@Test
	void callServiceWithSameParameters() {
		Client client = mock(Client, invokeConstructor(''))
		client.call('foo', 'sayhello', [name: ['foo', 'a']]).returns('hello')
		cli.client = client

		Config config = mock(Config, invokeConstructor(client))
		config.checkForUpdates().returns('foo')
		cli.config = config

		play {
			cli.action(['-Pname=foo', '-Pname=a', 'sayhello'] as String[])
			assert err.out.toString().isEmpty()
			assert out.out.toString() == 'hello\n'
		}
	}

	@Test
	void provideOtherUrlAndCallService() {
		Client client = mock(Client, constructor('http://foo/bar/'))
		client.call('foo', 'sayhello', [:]).returns('hello')
		cli.client = client

		Config config = mock(Config, invokeConstructor(client))
		config.client.set(client)
		config.checkForUpdates().returns('foo')
		cli.config = config

		play {
			cli.action(['-u', 'http://foo/bar/', 'sayhello'] as String[])
			assert err.out.toString().isEmpty()
			assert out.out.toString() == 'hello\n'
		}
	}

	@Test
	@Ignore
	void trustAllSSLCertificatesDuringServiceCall() {
		Client client = mock(Client, constructor('https://foo/bar/'))

		// leads to IllegalAccessError, but don't know why
		client.trustAllSslCertificates()
		// leads to IllegalAccessError, but don't know why

		client.call('sayhello', [:]).returns('hello')
		cli.client = client

		play {
			cli.action(['-u', 'https://foo/bar/', '-ta', 'sayhello'] as String[])
			assert err.out.toString().isEmpty()
			assert out.out.toString() == 'hello\n'
		}
	}

	@Test
	void moreThenOneServiceNameGivesAnError() {
		Config config = mock(Config, invokeConstructor(null))
		config.checkForUpdates()
		cli.config = config
		try {
			cli.action(['say', 'hello'] as String[])
		} catch (AssertionError e) {
			assert e.message =~ /too much\/less arguments/
		}
	}

	@Test
	void noServiceNameGivesAnError() {
		Config config = mock(Config, invokeConstructor(null))
		config.checkForUpdates()
		cli.config = config
		try {
			cli.action([] as String[])
		} catch (AssertionError e) {
			assert e.message =~ /too much\/less arguments/
		}
	}

	@Test
	// this test is too fragile, because you can't say which port is free
	@Ignore
	void callToNonExistingServerShouldEndInAnHumanReadableErrorMessageWithoutStackTrace() {
		cli.client = new Client('http://localhost:1111/')
		Config config = mock(Config, invokeConstructor(cli.client))
		config.checkForUpdates().returns('foo')
		cli.config = config

		play {
			cli.action(['sayhello'] as String[])
		}

		assert out.out.toString() == ''
		assert err.out.toString().contains('refused')
		assert err.out.toString().contains('HttpHostConnectException') == false
		// part of the stacktrace
		assert err.out.toString().contains('org.apache.http') == false
		assert err.out.toString().contains('at java.net') == false
	}

	@Test
	void stackTraceOfAnErrorIsShownWithTheDebugSwitch() {
		cli.client = new Client('http://localhost:1111/')
		Config config = mock(Config, invokeConstructor(cli.client))
		config.checkForUpdates().returns('foo')
		cli.config = config

		play {
			cli.action(['--debug', 'sayhello'] as String[])
		}

		assert out.out.toString() == ''
		assert err.out.toString().contains('refused')
		assert err.out.toString().contains('HttpHostConnectException')
		// part of the stacktrace
		assert err.out.toString().contains('org.apache.http')
		assert err.out.toString().contains('at java.net')
	}
}

