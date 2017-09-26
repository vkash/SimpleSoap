# SimpleSoap
1C: Enterprise 8.x SOAP client

SoapCredentials credentials = new SoapCredentials.Builder(server)
	.timeout(120000)
	.auth("user", "pass")//basic auth
	.base("1c_base_name")
	.wsdl("ws1.1cws")
	.build();

SoapRequest request = new SoapRequest.Builder("soap_name_space", "soap_service_name", "soap_method_name")
	.param("param1", "data1")
	.param("param2", "data2")
	.build();

SoapWS soapWS = new SoapWS(credentials);
File response = soapWS.call(request);
