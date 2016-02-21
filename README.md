# crudlet
A simple, lean JAX-RS based framework to build CRUD REST-to-SQL web applications running on a Java web application server, e.g. as an AngularJS backend.

*Note: There is a port of the equivalent functionality for use on a Node.js server tech stack: [hapi-bookshelf-crud](https://github.com/codebulb/hapi-bookshelf-crud).*

## Table of contents
<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [Installation](#installation)
- [Ways to use it](#ways-to-use-it)
- [Why you should use it](#why-you-should-use-it)
- [Usage](#usage)
  - [Server: Setup](#server-setup)
    - [JAX-RS](#jax-rs)
    - [Database](#database)
    - [CORS](#cors)
  - [Server: Implementation](#server-implementation)
    - [Entity](#entity)
    - [Service](#service)
    - [Web service endpoint](#web-service-endpoint)
  - [AngularJS client: Setup](#angularjs-client-setup)
  - [AngularJS client: Implementation](#angularjs-client-implementation)
    - [Validation](#validation)
    - [Exceptions](#exceptions)
- [By example](#by-example)
- [Specification](#specification)
  - [REST service endpoints](#rest-service-endpoints)
    - [Validation errors](#validation-errors)
    - [Other errors](#other-errors)
  - [API](#api)
  - [Global hooks (overrides)](#global-hooks-overrides)
- [Project status and future plans](#project-status-and-future-plans)
- [Version history](#version-history)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Installation
Use [JitPack](https://jitpack.io/) to add its dependency to your Maven web application project:
```
<dependency>
    <groupId>com.github.codebulb</groupId>
    <artifactId>crudlet</artifactId>
    <version>0.1</version>
</dependency>
...
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
Replace the version by the tag / commit hash of your choice or `-SNAPSHOT` to get the newest SNAPSHOT.

Visit [JitPack’s docs](https://jitpack.io/docs/) for more information.

## Ways to use it
* Build your `@Entity` model and get production-ready REST CRUD operations in a few lines of code.
* Concentrate on building front-end logic (e.g. using AngularJS) and use Crudlet to ensure the database backend is “just there”, working as expected
* Study the project's open source code as an example of best-practices REST CRUD and build your own solution on top of it.

## Why you should use it
* Extremely small footprint (JAR <= 30KB), no dependencies other than plain Java EE 7.
* Human-readable documentation (here and in the [API docs](http://codebulb.github.io/pages/crudlet/doc/)).
* Free & Open source ([New BSD license](https://github.com/codebulb/crudlet/blob/master/LICENSE)).

## Usage
Note: The **complete source code of an example application** (server and client) is available [in a separate GitHub repository](https://github.com/codebulb/crudletdemo).

### Server: Setup
#### JAX-RS
You need to setup the JAX-RS Application servlet in the web.xml file as shown in the demo project:
```
<servlet>
    <servlet-name>javax.ws.rs.core.Application</servlet-name>
</servlet>
<servlet-mapping>
    <servlet-name>javax.ws.rs.core.Application</servlet-name>
    <url-pattern>/*</url-pattern>
</servlet-mapping>
```

#### Database
Define your database connection in the persistence.xml file. Any JDBC compliant connection is supported. In the demo project, we use a JTA data source the configuration of which is set up in the application server.

#### CORS
Crudlet by default allows you to handle CORS request without nasty errors as is usually desired in development / debug stage. The required request / response filters are implemented in the `CorsRequestFilter` and `CorsResponseFilter` class, respectively.

Set the `Options#CORS` boolean flag to false (e.g. in a `@Startup` `@Singleton` EJB bean) to disable CORS allow-all policy.

### Server: Implementation
Crudlet provides a simple, lean framework to build REST-to-SQL web applications based on common best practices. Having a basic CRUD implementation in place means that you can an any entity type:
* **Create (C)** new entities
* **Read (R)** persistent entities from the persistence storage
* **Update (U)** entities in the persistence storage
* **Delete (D)** entities from the persistence storage

Building your application around a CRUD centric approach brings a couple of advantages:
* The service interface is very simplistic, lean and self-documenting
* The business logic resides in the model rather than in the service interface which matches well an object-oriented language like Java
* Because the service interface stays the same for all entities, we can make excessive use of abstraction through inheritance and generics
* This architecture matches well a best practices compliant RESTful implementation where the four CRUD actions are really matched against HTTP verbs.

This best practices architecture is based on three central artifacts for which Crudlet provides an abstract generic base implementation:
* `CrudEntity`: the entity model
* `CrudService`: the persistence service
* `CrudResource`: the REST web service endpoint

In a CRUD application, the relation between these artifacts is 1 : 1 : 1; you will thus build a service and a controller for every entity. Thanks to the level of abstraction provided by Crudlet, this is a matter of about 30 lines of code:
* `CrudEntity` makes sure your entity implements an auto-ID generation strategy
* `CrudService` implements basic persistence storage access (through an EntityManager) for the four CRUD operations
* `CrudResource` implements a REST web service endpoint for editing all entities in the persistence storage including out-of-the-box support for returning I18N-ready model validation error messages.

#### Entity
Use either the `CrudIdentifiable` interface or the `CrudEntity` class to derive your entity model classes from. This is the only prerequisite to use them with a `CrudService` and a `CrudResource`.

The difference between the interface and the class is that the latter provides an auto-generated Long id field implementation out-of-the-box.

For instance, to create a `Customer` entity:
```
@Entity
public class Customer extends CrudEntity { 
    @NotNull
    @Pattern(regexp = "[A-Za-z ]*")
    private String name;
    private String address;
    private String city;
    ...
```
Use Bean Validation constraints to declaratively specify the model validation.

#### Service
In order to create a CRUD service for an entity type, implement `CrudService` for the entity and register it as a CDI bean in the container (depending on beans.xml bean-discovery-mode, explicit registration may not be necessary).

For instance, to create the service for the `Customer` entity:
```
public class CustomerService extends CrudService<Customer> {
    @Override
    @PersistenceContext
    protected void setEm(EntityManager em) {
        super.setEm(em);
    }
    
    @Override
    public Customer create() {
        return new Customer();
    }

    @Override
    public Class<Customer> getModelClass() {
        return Customer.class;
    }
}
```
* Within the `setEm(EntityManager)` method, simply call the super method. The important part is that you inject your `@PersistenceContext` in this method by annotation.

Of course, you are free to add additional methods to your `CrudService` implementation where reasonable.

#### Web service endpoint
Finally, create the REST web service endpoint by implementing `CrudResource` for the entity and register it as a `@Stateless` EJB bean in the container.

For instance, to create the web service endpoint for the `Customer` entity:
```
@Path("customers")
@Stateless
public class CustomerResource extends CrudResource<Customer> {
    @Inject
    private CustomerService service;
    
    @Override
    protected CrudService<Customer> getService() {
        return service;
    }
}
```
* The `@Path` defines the base path of the web service endpoint.
* Within the `getService()` method, return the concrete `CrudService` for the entity type in question which you should dependency-inject into the controller.

**That’s it.** Now you can use e.g. the [httpie](https://github.com/jkbrzt/httpie) command line tool to verify that you can execute RESTful CRUD operations on your entity running on the database.

Of course, you are free to add additional methods to your `CrudResource` implementation where reasonable.

Read on for an example client implementation based on AngularJS.

### AngularJS client: Setup
In this example, we use [Restangular](https://github.com/mgonto/restangular) as an abstraction layer to do RESTful HTTP requests which offers a far more sophisticated although more concise API than AngularJS’s built-in `$http` and `$resource`. It is set up as shown in the demo application’s main JavaScript file:
```
.config(function (RestangularProvider) {
  RestangularProvider.setBaseUrl('http://localhost:8080/CrudletDemo.server/');
  
  RestangularProvider.setRequestInterceptor(function(elem, operation) {
    // prevent "400 - bad request" error on DELETE
    // as in https://github.com/mgonto/restangular/issues/78#issuecomment-18687759
    if (operation === "remove") {
      return undefined;
    }
    return elem;
  });
})
```

You also potentially want to install and setup the [angular-translate](http://angular-translate.github.io/) module for I18N support:
```
.config(['$translateProvider', function ($translateProvider) {
  $translateProvider.translations('en', translations);
  $translateProvider.preferredLanguage('en');
  $translateProvider.useMissingTranslationHandlerLog();
  $translateProvider.useSanitizeValueStrategy('sanitize');
}])
```

### AngularJS client: Implementation
In the “controller” JavaScript file, we can use Restangular to access the RESTful web service endpoint of our Crudlet Customer service like so:
* Get a list of entities (GET /customers/): `Restangular.all("customers").getList().then(function(entities) {...})`
* Get a single entity (GET /customers/1): `Restangular.one("customers", $routeParams.id).get().then(function (entity) {...})`
* Save an entity (PUT /customers/1): `$scope.entity.save().then(function() {...})`
* ... (see [Restangular's documentation](https://github.com/mgonto/restangular) for more information)

#### Validation
An interesting aspect of Crudlet is its out-of-the-box support for localized validation error messages. If upon save, a validation error occurs, the server answers e.g. like this:
```
{
  "validationErrors": {
    "name": {
      "attributes": {
        "pattern": "/^[A-Za-z ]*$/",
        "value": "Name not allowed!!"
      },
      "constraintClassName": "string.regex.base",
      "invalidValue": "Name not allowed!!",
      "messageTemplate": "string.regex.base"
    }
  }
}
```

Using the angular-translate module of AngularJS we set up previously, we can show all localized validation messages like so:
```
<div class="alert alert-danger" ng-show="validationErrors != null">
  <ul>
    <li ng-repeat="(component, error) in validationErrors">
      {{'payment.' + component | translate}}: {{'error.' + error.messageTemplate | translate:error.attributes }}
    </li>
  </ul>
</div>
```
The `validationErrors.<property>.messageTemplate` part is the message template returned by the bean validation constraint. We can thus e.g. base the validation error localization on [Hibernate’s own validation messages](http://grepcode.com/file/repo1.maven.org/maven2/org.hibernate/hibernate-validator/5.1.3.Final/org/hibernate/validator/ValidationMessages.properties/):
```
var translations = {
  ...
  'error.string.regex.base': 'must match "{{pattern}}"',
  ...
};
```
(I preceded it with `error.` here.)

Because the error object returned by the server is a map, we can also use it to conditionally render special error styling, e.g. using Bootstrap’s error style class:
```
ng-class="{'has-error': errors.amount != null}"
```

#### Exceptions
Similar to validation errors, some runtime exceptions will also return a user-friendly error response message. For instance, let’s assume that a Customer has a list of Payments and you try to delete a Customer with a non-empty Payments list:
```
{
  "error": {
    "detailMessage": "delete from `customer` where `id` = '1' - ER_ROW_IS_REFERENCED_2: Cannot delete or update a parent row: a foreign key constraint fails (`restdemo`.`payment`, CONSTRAINT `payment_customer_id_foreign` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`))",
    "exception": "ER_ROW_IS_REFERENCED_2"
  }
}
```
Again, you can catch and display these in the AngularJS view:
```
<div class="alert alert-danger" ng-show="errorNotFound != null || error != null">
  <ul>
    <li ng-show="error != null">
      {{'error.' + error.exception | translate}}
    </li>
  </ul>
</div>
```
With appropriate localization:
```
var translations = {
  ...
  'error.ER_ROW_IS_REFERENCED_2': 'Cannot delete an object which is still referenced to by other objects.',
  ...
};
```
Because in a real-world production environment, exposing details of an exception may be a security issue, you can suppress this user-friendly exception detail output by setting the `Options#RETURN_EXCEPTION_BODY` boolean flag to false.

## By example
For a complete example, please take a look at the [**example application**](https://github.com/codebulb/crudletdemo). It also shows you how to easily implement a `CrudResource` for nested resources.

If you want to lean more about building RESTful web applications based on vanilla JAX-RS and Restangular, you may enjoy a [blog post I’ve written about it](http://www.codebulb.ch/2015/09/restful-software-requirements-specification-part-1.html); it features a complete example application as well.

## Specification
### REST service endpoints
Crudlet maps these HTTP requests to persistence storage operations:

* `GET /contextPath/model`: `service#findAll()`
  * Searches for all entities of the given type; or searches for all entities of the given type which match all the given query parameters if the global `Options#ALLOW_FILTERS` flag is set to `true`. Allowed filters are:
    * `=` String equals, e.g. GET `GET /contextPath/customers?city=Los%20Angeles`
    * `=>` Long greater than or equals, e.g. GET `GET /contextPath/customers/1/payments?amount=>100`
    * `=<` Long less than or equals, e.g. GET `GET /contextPath/customers/1/payments?amount=<100`
    * `=~` String SQL "LIKE", e.g. GET `GET /contextPath/customers?address=~%Street`
    * `Id=` Foreign key equals, e.g. GET `GET /contextPath/customers/1/payments?customerId=1` (this is rather used programmatically when implementing `CrudService` class to preconfigure nested service endpoints globally than by actual API clients)
  * returns HTTP 200 OK with list of entities
* `GET /contextPath/model/_count`: `service#countAll()`
  * Counts all entities of the given type; or counts all entities of the given type which match all the given query parameters if the global `Options#ALLOW_FILTERS` flag is set to `true`. Allowed filters are the same as for `GET /contextPath/model`.
  * returns HTTP 200 OK with the calculation output; or HTTP 403 FORBIDDEN if the global `Options#ALLOW_COUNT` flag is set to `false`.
* `GET /contextPath/model/:id`: `service#findById(id)`
  * Searches for the entity of the given type with the given id.
  * returns HTTP 200 OK with entity if found; or HTTP 404 NOT FOUND if entity is not found.
* `POST /contextPath/model` with entity: `service#save(entity)`
  * Saves the entity for the first time.
  * returns HTTP 200 OK with saved entity (as returned by the insert operation) and `Location` header with content “/contextPath/model/:id”; or HTTP 400 BAD REQUEST with error information on validation error / if entity's `id` field is not `null`.
* `PUT /contextPath/model/:id` with entity: `service#save(entity)`
  * Updates the existing entity.
  * returns HTTP 200 OK with updated entity (e.g. new id) and `Location` header with content “/contextPath/model/:id”; or HTTP 400 BAD REQUEST with error information on validation error / if entity's `id` field is not `null` nor matches the `:id` path parameter.
* `DELETE /contextPath/model`: `service#deleteAll(id)`
  * Deletes all entities of the given type; or deletes all entities of the given type which match all the given query parameters if the global `Options#ALLOW_FILTERS` flag is set to `true`. Allowed filters are the same as for `GET /contextPath/model`.
  * returns HTTP 204 NO CONTENT; or HTTP 403 FORBIDDEN if the global `Options#ALLOW_DELETE_ALL` flag is set to `false`.
* `DELETE /contextPath/model/:id`: `service#delete(id)`
  * Deletes the entity with the id provided or does nothing if no entity with the id provided exists.
  * returns HTTP 204 NO CONTENT.

These REST service endpoints are optimized for use with a [Restangular](https://github.com/mgonto/restangular) client.

Note: A JAX-RS based server implicitly allows optional trailing slashes (`/`) for these endpoints.

#### Validation errors
A validation error returns with HTTP 400 BAD REQUEST and the following error information (as far as it is available) in the body:
* `validationError`
  * (for every `constraintViolationException.constraintViolation`): [`violation.propertyPath.toString()`]; or, if the entire entity is erroneous (e.g. `null`): `.` (a single dot)
    * `constraintClassName`: `violation.constraintDescriptor.annotation.annotationType().name`
    * `messageTemplate`: `violation.messageTemplate`
    * `invalidValue`: `violation.invalidValue`
    * `attributes`: `violation.constraintDescriptor.attributes` without `groups`, `message`, `payload`.

#### Other errors
A non-validation error returns with HTTP 400 BAD REQUEST and the following error information (as far as it is available) in the body:
* (`throwable.cause` unwrapped until `SQLIntegrityConstraintViolationException`): `exception`
  * `exception`: `exception.class.name`
  * `detailMessage`: `exception.message`

### API
The API is documented in the project's [**JavaDoc**](http://codebulb.github.io/pages/crudlet/doc/).

### Global hooks (overrides)
You can e.g. use a `@Startup` `@Singleton` EJB bean to manipulate the following values on application startup to configure application behavior:
* `Options#CORS`: Disable the allow-all "preflight" CORS request filter as well as the allow-all CORS response filter.
* `Options#RETURN_EXCEPTION_BODY`: Disable user-friendly exception output.
* `Options#ALLOW_DELETE_ALL`: Disable "DELETE ALL" service endpoint.
* `Options#ALLOW_FILTERS`: Disable GET filter by query parameter functionality.
* `Options#ALLOW_COUNT`: Disable "GET COUNT" service endpoint.

## Project status and future plans
Crudlet is currently experimental. I’d like to make some stability updates before releasing a proper 1.0 version. It may still already be useful for evaluation purposes, or as a skeleton to build your own solution.

This is a private project I’ve started for my own pleasure and usage and to learn more about building (Ajax) REST APIs, and I have no plans for (commercial) support.

You can also find more information about this project on its [**accompanying blog post**](http://www.codebulb.ch/2016/01/crudlet-ready-to-use-restangular-to-sql-crud-with-jax-rs.html) and in the [**API docs**](http://codebulb.github.io/pages/crudlet/doc/).

## Version history
* [V. 0.2](https://github.com/codebulb/crudlet/issues?utf8=%E2%9C%93&q=milestone%3A0.2)
  * REST endpoints fixed / cleaned up up / enhanced: Support for DELETE ALL, COUNT, query parameter filters added.
  * Miscellaneous fixes / enhancements.
* V. 0.1
  * First release
