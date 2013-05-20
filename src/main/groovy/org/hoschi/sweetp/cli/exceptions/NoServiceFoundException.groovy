package org.hoschi.sweetp.cli.exceptions

/**
 * Created by IntelliJ IDEA.
 * User: hoschi
 * Date: 10.06.11
 * Time: 20:19
 * To change this template use File | Settings | File Templates.
 */
class NoServiceFoundException extends RuntimeException {
	NoServiceFoundException(String target) {
		super("No service found for target: $target")
	}
}
