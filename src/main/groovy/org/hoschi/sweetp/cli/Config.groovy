package org.hoschi.sweetp.cli

import groovy.json.JsonBuilder
import groovy.util.logging.Log4j
import net.sf.json.groovy.JsonSlurper
import org.hoschi.sweetp.cli.exceptions.ProjectNotInitializedException

/**
 * @author Stefan Gojan
 */
@Log4j
class Config {
	Client client
	File cli
	File config
	final static String MAIN_DIR = '.sweetp'
	final static String CLI_FILE_NAME = "$MAIN_DIR/cli.json"
	final static String CONFIG_FILE_NAME = "$MAIN_DIR/config.json"

	Config(Client client) {
		this.client = client
	}

	/**
	 * Open ".sweetp/cli.json" file.
	 */
	void openCliFile() {
		if (!cli) {
			cli = new File(CLI_FILE_NAME)
			if (!cli.exists()) {
				throw new ProjectNotInitializedException(CLI_FILE_NAME)
			}
		}
	}

	/**
	 * Open ".sweetp/config.json" file
	 */
	void openConfigFile() {
		if (!config) {
			config = new File(CONFIG_FILE_NAME)
			if (!config.exists()) {
				throw new ProjectNotInitializedException(CONFIG_FILE_NAME)
			}
		}
	}

	/**
	 * Check when config file was last read, or return -1 if the data was not
	 * saved yet in cli file.
	 *
	 * @return time or -1
	 */
	long checkLastRead() {
		JsonSlurper slurper = new JsonSlurper()

		long lastRead = -1
		def json = null

		openCliFile()
		if (cli.text) {
			json = slurper.parseText(cli.text)
		}
		if (json && json.config && json.config.lastread) {
			lastRead = json.config.lastread
		}

		lastRead
	}

	/**
	 * Save last modified time of config in cli file.
	 *
	 * @param lastModified time
	 */
	void saveLastModified(long lastModified) {
		openCliFile()
		def json = new JsonBuilder([
				config: [
						lastread: lastModified
				]
		])
		cli.text = json.toPrettyString()
	}

	/**
	 * Check if config was updated and if it has a name attribute. Update config
	 * in server if it is modification date is newer than saved one from cli
	 * file.
	 *
	 * @return name of project
	 */
	String checkForUpdates() {
		JsonSlurper slurper = new JsonSlurper()

		openConfigFile()
		def json = slurper.parseText(config.text)

		// assert name
		if (!json.name) {
			throw new IllegalArgumentException('config exists, but have no name inside')
		}

		// send file to server if it was modified
		long lastModified = config.lastModified()
		long lastRead = checkLastRead()
		if (lastModified > lastRead) {
			log.info 'reading new config file and send it to server'

			saveLastModified(lastModified)

			// add working dir
			json.dir = new File('.').canonicalPath
			JsonBuilder builder = new JsonBuilder(json)
			client.sendConfig(builder.toString())
		}

		json.name
	}

	/**
	 * Create a cli and config file. Add a name to config, because the name is
	 * used in many further actions.
	 *
	 * @param projectName to save into new config file
	 */
	void initProject(String projectName) {
		File parent = new File(MAIN_DIR)
		if (!parent.exists()) {
			parent.mkdirs()
		}

		File cli = new File(CLI_FILE_NAME)
		if (!cli.exists()) {
			cli.createNewFile()
		}
		cli.text = '{"config":{"lastread":-1}}'

		File config = new File(CONFIG_FILE_NAME)
		if (!config.exists()) {
			config.createNewFile()
		}
		JsonBuilder builder = new JsonBuilder([
				name: projectName
		])
		config.text = builder.toPrettyString()
	}
}
