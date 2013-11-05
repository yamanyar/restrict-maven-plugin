With this plugin, you can restrict access from any package/class to target package/class/method inside the compiled code and the dependencies.

We use this plugin to be ensure that some restricted packages are not accessed by in-house developed components. This can not detect access via reflection however we do restrict reflection as well.

Please see https://github.com/yamanyar/restrict-maven-plugin/wiki/About-restrict-maven-plugin for more details.

java.lang.System
java.lang.System*.println()
