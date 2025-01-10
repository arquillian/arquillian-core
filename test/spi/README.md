The tests in this module use a custom maven compiler configuration to simulate a class that exists on the server side but not on the client side to validate the behavior seen in issue
https://github.com/arquillian/arquillian-core/issues/641.

To be able to run these tests from within Intellij, one has to configure the 
"Settings | Build, Execution, Deployment | Build Tools | Maven | Runner" to have the "Delegate IDE build/run actions to Maven" box checked.

