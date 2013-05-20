package org.hoschi.sweetp.cli.exceptions

/**
 * @author Stefan Gojan
 */
class ProjectNotInitializedException extends RuntimeException {
	ProjectNotInitializedException(String fileName) {
		super("""File $fileName not found:
- initialize the project with 'sweetp -init name'
- or, provide a project name 'sweetp -p myproject'""")
	}
}
