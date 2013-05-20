# Overview

Sweetp is a simple client for the sweetp server. In short, the sweetp server
gives you an ecosystem for your programming automation stuff. Logic for
automating software development is bundled in services. You can read more about
how to get more productive at
[sweet productivity](http://sweet-productivity.com).

# Features

* calls service urls
* passes parameters as key=value pairs
* output response as formattet text intead of raw json
* change server url with switch
* enable 'trust all' mode for self signed SSL certificates with switch
* auto updating config file to server iff the file was 'touched'
* project name is automatically send to server (which loads cached project config)

# Sweetp

This client is focused on helping you to issue commands to the server and read
the response easily. A call to sweetp server is a simple HTTP request and the
respons is JSON. Sweetp is a simple frontend to build the HTTP request and pack
your parameters into it. It works with every service and so sweetp is very
generic. Said this, if the response is not intend to seen by humans, you going
to see plain JSON.

Sweetp helps you also with project configuration files. You can initialize
a new project easily. Sweetp is also aware in which project you call a service
and send it to the server. Additionally your changed project configuration
get's send to the server if you "touched" it.

# "Manpage"

    sweetp -h

    usage: sweetp [options] servicename

    Examples:

    sweetp sayhello
    calls service sayhello: http://localhost:7777/services/myproject/sayhello

    sweept -Pname=foo sayhello
    calls service sayhello with name = foo:
    http://localhost:7777/services/myproject/sayhello?name=foo

    Options:
     -d,--debug                         is most verbose
     -h,--help                          prints this help text
     -i,--info                          is more verbose
     -init,--initialize <projectName>   the prjoect with a given name
     -P <parameter=value>               use value for a given query parameter
     -p,--project <projectName>         name to use, instead of reading local
                                        config
     -ta,--trustAll                     ssl certificates, including self
                                        signed ones
     -u,--url <arg>                     which is used, defaults to
                                        http://localhost:7777

    by Stefan Gojan
