# SelfRegister library

Mini-servlet library to add self register pages to your application.
Super thin configuration.

## Features

* email validation
* Timeout register validation
* User request and data via configuration
* Available userstores: jdbc, firebase

## Configuration

3 ways of personalization/configuration:

* Config/properties files
* Resource messages and template pages
* Action classes can be overwritten

### Config files

Init servlet [web.xml in example app](example/src/main/webapp/WEB-INF/web.xml)

    <servlet>
        <servlet-name>selfregister</servlet-name>
        <servlet-class>io.github.jdlopez.selfregister.SelfRegisterServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>selfregister</servlet-name>
        <url-pattern>/register/*</url-pattern>
    </servlet-mapping>
    
Configuration can be made first by servlet init parameters:

* *pathRegister* must be servlet's url-patter. Default "/register"
* *pathConfirm* confirmation page uri. Added to pathRegister. Default  "/confirm"
* *parameterEmail* name of email request parameter. Default "email"
* *expirationTimeMillis* Time of certification code expiration. Default 48 hours
* *configResourceName* Resource name (getResourceAsStream kind of) with full configuration. Default "/selfregister.properties"

All of this properties can be overwrited with same key values on configResource file.

See [ConfigStore](src/main/java/io/github/jdlopez/selfregister/ConfigStore.java) class for details. 

### Templates and message resources

Templates can be configured individually. Also with servlet init parameters.    

* *pageError* "/selfregister/error.jsp";
* *pageWelcome* "/selfregister/welcome.jsp";
* *pageConfirm* "/selfregister/confirm.jsp";
* *pageSuccess* "/selfregister/success.jsp";

Message strings used by these pages are located in resource boundle 
wich can also be overriden.
 
* *bundleName* Default value "io.github.jdlopez.selfregister.SelfRegisterServlet-messages"

### Action classes

Last but not least. UserStore class and EmailSender class can be overriden.
To do so you must provide full class names of replacement:

* *userStoreClass* User Store bean. Default value "InMemoryUserStore.class.getCanonicalName()"
* *emailSenderClass* Email Sender bean. Default value "StdoutMailSender.class.getCanonicalName()"

See full example [example web-app](example/README.md)
