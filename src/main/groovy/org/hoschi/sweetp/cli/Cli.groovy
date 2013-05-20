package org.hoschi.sweetp.cli

import groovy.util.logging.Log4j
import org.apache.log4j.Level
import org.apache.log4j.Logger

/**
 * Main class for sweetp cli.
 */
@Typed
@Log4j
@SuppressWarnings('Println')
@SuppressWarnings('SystemErrPrint')
class Cli {
	Client client
	Config config
	CliBuilder builder

	static final String HEADER = '''
Examples:
sweetp sayhello
    calls service sayhello: http://localhost:7777/services/myproject/sayhello

sweept -Pname=foo sayhello
    calls service sayhello with name = foo: http://localhost:7777/services/myproject/sayhello?name=foo

Options:'''
	static final String FOOTER = '''
by Stefan Gojan

'''

	/**
	 * Set up cli switches and info text.
	 */
	@Typed(TypePolicy.DYNAMIC) Cli() {
		// specify cli options
		builder = new CliBuilder(
				usage: 'sweetp [options] servicename',
				header: HEADER,
				footer: FOOTER,
		)

		builder.identity {
			h(longOpt: 'help', 'prints this help text')
			i(longOpt: 'info', 'is more verbose')
			d(longOpt: 'debug', 'is most verbose')
			init(args: 1, argName: 'projectName', longOpt: 'initialize',
					'the prjoect with a given name')
			p(args: 1, argName: 'projectName', longOpt: 'project',
					'name to use, instead of reading local config')
			ta(longOpt: 'trustAll', 'ssl certificates, including self signed ones')
			u(args: 1, longOpt: 'url', 'which is used, defaults to http://localhost:7777')
			P(args: 2, valueSeparator: '=', argName: 'parameter=value', 'use value for a given query parameter')
		}
	}

	/**
	 * Parse command line arguments and do what to do.
	 *
	 * @param args
	 */
	@Typed(TypePolicy.DYNAMIC)
	@SuppressWarnings('CatchException')
	void action(String[] args) {
		assert config != null

		def options = builder.parse(args)
		assert options

		if (options.d) {
			Logger.rootLogger.level = Level.DEBUG
		} else if (options.i) {
			Logger.rootLogger.level = Level.INFO
		}

		try {
			if (options.h ||
					(options.arguments().isEmpty() && !options.init)) {
				builder.usage()
				return
			}


			Map params = [:]
			if (options.Ps) {
				List ps = options.Ps
				for (int i = 0; i < ps.size(); i += 2) {
					String key = ps[i].trim()
					String value = ps[i + 1].trim()
					if (params.containsKey(key)) {
						params."$key" << value
					} else {
						params."$key" = [value]
					}
				}

			}

			if (options.u) {
				client = new Client(options.u)
				config.client = client
			}

			if (options.ta) {
				client.trustAllSslCertificates()
			}

			if (options.init) {
				config.initProject(options.init)
				return
			}

			String projectName
			if (options.p) {
				projectName = options.p
			} else {
				projectName = config.checkForUpdates()
			}

			// call client with service name from args
			assert options.arguments().size() >= 1, 'too less arguments, last argument must be service name'
			println client.call(projectName, options.arguments().join('/'), params)
		} catch (Exception e) {
			// print full stacktrace for log level info and debug
			if (log.effectiveLevel.isGreaterOrEqual(Level.WARN)) {
				System.err.print e.message
			} else {
				e.printStackTrace(System.err)
			}
		}
	}

	/**
	 * Create instances for Cli, Client and Config. Spawn actions method to do
	 * some actions.
	 *
	 * @param args to parse and decide what to do
	 * @throws Exception if something goes wrong
	 */
	static void main(String[] args) throws Exception {
		def own = new Cli()
		own.client = new Client('http://localhost:7777');
		own.config = new Config(own.client)
		own.action(args)
	}
}
