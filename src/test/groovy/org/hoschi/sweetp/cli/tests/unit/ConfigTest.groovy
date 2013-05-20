package org.hoschi.sweetp.cli.tests.unit

import org.gmock.WithGMock
import org.hoschi.sweetp.cli.Client
import org.hoschi.sweetp.cli.Config
import org.hoschi.sweetp.cli.exceptions.ProjectNotInitializedException
import org.junit.Before
import org.junit.Test

/**
 * @author Stefan Gojan
 */
@WithGMock
class ConfigTest {
	Config config

	@Before
	void setUp() {
		config = new Config(null)
	}

	@Test
	void initProject() {
		File parent = mock(File, constructor(Config.MAIN_DIR))
		parent.exists().returns(false)
		parent.mkdirs()

		File cli = mock(File, constructor(Config.CLI_FILE_NAME))
		cli.exists().returns(false)
		cli.createNewFile()
		cli.text.set('{"config":{"lastread":-1}}')

		File conf = mock(File, constructor(Config.CONFIG_FILE_NAME))
		conf.exists().returns(false)
		conf.createNewFile()
		conf.text.set('''{
    \"name\": \"foo\"
}''')

		play {
			config.initProject('foo')
		}
	}

	@Test
	void openCliFileOnce() {
		File cli = mock(File, constructor(Config.CLI_FILE_NAME))
		cli.exists().returns(true)

		play {
			config.openCliFile()
			config.openCliFile()
		}
	}

	@Test
	void openConfigFileOnce() {
		File conf = mock(File, constructor(Config.CONFIG_FILE_NAME))
		conf.exists().returns(true)

		play {
			config.openConfigFile()
			config.openConfigFile()
		}
	}

	@Test(expected = ProjectNotInitializedException)
	void testCheckLastReadWithNotExistingCliFile() {
		File cli = mock(File, constructor(Config.CLI_FILE_NAME))
		cli.exists().returns(false)

		play {
			config.checkLastRead()
		}
	}

	@Test(expected = ProjectNotInitializedException)
	void testCheckForUpdatesWithNotExistingConfigFile() {
		File cli = mock(File, constructor(Config.CLI_FILE_NAME))
		cli.exists().returns(true)
		cli.text.returns(null)

		File conf = mock(File, constructor(Config.CONFIG_FILE_NAME))
		conf.exists().returns(false)

		play {
			config.checkForUpdates()
		}
	}

	@Test(expected = IllegalArgumentException)
	void testCheckForUpdatesWithConfigFileWithNoName() {
		File cli = mock(File, constructor(Config.CLI_FILE_NAME))
		cli.exists().returns(true)
		cli.text.returns(null)

		File conf = mock(File, constructor(Config.CONFIG_FILE_NAME))
		conf.exists().returns(true)
		conf.lastModified.returns(0)
		conf.text.returns('{}')

		play {
			config.checkForUpdates()
		}
	}

	@Test
	void testCheckForNotUpdatedFile() {
		File cli = mock(File, constructor(Config.CLI_FILE_NAME))
		cli.exists().returns(true)
		cli.text.returns('{"config":{"lastread":1}}').stub()

		File conf = mock(File, constructor(Config.CONFIG_FILE_NAME))
		conf.exists().returns(true)
		conf.lastModified().returns(1)
		conf.text.returns('{"name":"foo"}')

		play {
			config.checkForUpdates()
		}
	}

	@Test
	void testCheckForUpdatedFile() {
		File current = mock(File, constructor('.'))
		current.canonicalPath.returns('/home/hoschi/foo')

		File cli = mock(File, constructor(Config.CLI_FILE_NAME))
		cli.exists().returns(true)
		cli.text.returns('{"config":{"lastread":0}}').stub()

		File conf = mock(File, constructor(Config.CONFIG_FILE_NAME))
		conf.exists().returns(true)
		conf.lastModified().returns(1)
		conf.text.returns('{"name":"foo"}').stub()

		cli.text.set('{\n    \"config\": {\n        \"lastread\": 1\n    }\n}')

		Client client = mock(Client)
		client.sendConfig(match {true})
		config.client = client

		play {
			config.checkForUpdates()
		}
	}
}
