/*
 * YARN Simplified API layer for services
 * Bringing a new service on YARN today is not a simple experience. The APIs of existing frameworks are either too low level (native YARN), require writing new code (for frameworks with programmatic APIs) or writing a complex spec (for declarative frameworks).  This simplified REST API can be used to create and manage the lifecycle of YARN services. In most cases, the application owner will not be forced to make any changes to their applications. This is primarily true if the application is packaged with containerization technologies like Docker.  This document describes the API specifications (aka. YarnFile) for deploying/managing containerized services on YARN. The same JSON spec can be used for both REST API and CLI to manage the services.
 *
 * OpenAPI spec version: 1.0.0
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package org.apache.cb.yarn.service.api;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-08-08T16:59:40.572+02:00")
public class Pair {
    private String name = "";
    private String value = "";

    public Pair(String name, String value) {
        setName(name);
        setValue(value);
    }

    private void setName(String name) {
        if (!isValidString(name)) return;

        this.name = name;
    }

    private void setValue(String value) {
        if (!isValidString(value)) return;

        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    private boolean isValidString(String arg) {
        if (arg == null) return false;
        if (arg.trim().isEmpty()) return false;

        return true;
    }
}
