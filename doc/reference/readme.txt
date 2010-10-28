To build the Arquillian docs, run the following command:

 mvn package

To view the complete HTML version, you have to run the package goal at least
once after executing clean. This step generates the documentation and combines
it with the supporting resources. After that, you can build the documentation
incrementally using the following command:

 mvn jdocbook:generate

The generated documentation gets published into this directory:

 target/docbook/publish
