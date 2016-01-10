# crudlet
A simple, lean JAX-RS based framework to build CRUD REST-to-SQL web applications, e.g. as an AngularJS backend.

## Installation
Use [JitPack](https://jitpack.io/) to add its dependency to your Maven web application project:
```
<dependency>
    <groupId>com.github.codebulb</groupId>
    <artifactId>crudlet</artifactId>
    <version>0.1_RC-1</version>
</dependency>
```
Replace the version by the tag / commit hash of your choice or `-SNAPSHOT` to get the newest SNAPSHOT.

Not using Maven? You can [download the JAR directly from JitPack’s servers](https://jitpack.io/com/github/codebulb/crudlet/0.1_RC-1/crudlet-0.1_RC-1.jar).

Visit [JitPack’s docs](https://jitpack.io/docs/) for more information.

## Usage
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
Set the `CorsRequestFilter#ALLOW_OPTIONS` and `CorsResponseFilter# ALLOW_CORS` boolean flag to false (e.g. in a `@Startup` `@Singleton` EJB bean) to disable CORS allow-all policy.

### Server: Implementation
Crudlet provides a simple, lean framework to build (optionally RESTful) CRUD JSF applications based on common best practices. Having a basic CRUD implementation in place means that you can an any entity type:
* *Create (C)* new entities
* *Read (R)* persistent entities from the persistence storage
* *Update (U)* entities in the persistence storage
* *Delete (D)* entities from the persistence storage

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
* Within the setEm(EntityManager) method, simply call the super method. The important part is that you inject your `@PersistenceContext` in this method by annotation.

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

That’s it. Now you can use e.g. the `httpie` command line tool to verify that you can execute RESTful CRUD operations on your entity running on the database.

Of course, you are free to add additional methods to your `CrudResource` implementation where reasonable.

Read on for an example client implementation based on AngularJS.

### AngularJS client: Setup
In this example, we use Restangular as an abstraction layer to do RESTful HTTP requests which offers a far more sophisticated although more concise API than AngularJS’s built-in `$http` and `$resource`. It is set up as shown in the demo application’s main JavaScript file:
```
.config(function (RestangularProvider) {
	RestangularProvider.setBaseUrl('http://localhost:8080/CrudletDemo.server/');
})
```

You also potentially want to install and setup the angular-translate module for I18N support:
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

#### Validation
An interesting aspect of Crudlet is its out-of-the-box support for localized validation error messages. If upon save, a validation error occurs, the server answers e.g. like this:
```
{
    "errors": {
        "name": {
            "attributes": {
                "flags": "[Ljavax.validation.constraints.Pattern$Flag;@1f414540",
                "regexp": "[A-Za-z ]*"
            },
            "constraintClassName": "javax.validation.constraints.Pattern",
            "invalidValue": "Name not allowed!!",
            "messageTemplate": "javax.validation.constraints.Pattern.message"
        }
    }
}
```

Using the angular-translate module of AngularJS we set up previously, we can show all localized validation messages like so:
```
<div class="alert alert-danger" ng-show="errors != null">
	<ul>
		<li ng-repeat="(component, error) in errors">
			{{'payment.' + component | translate}}: {{'error.' + error.messageTemplate | translate:error.attributes }}
		</li>
	</ul>
</div>
```
The `error.<property>.messageTemplate` part is the message template returned by the bean validation constraint. We can thus e.g. base the validation error localization on Hibernate’s own validation messages:
```
var translations = {
    ...
	'error.javax.validation.constraints.Pattern.message': 'must match "{{regexp}}"',
	...
};
```
(I preceded it with `error.` here.)

Because the error object returned by the server is a map, we can also use it to conditionally render special error styling, e.g. using Bootstrap’s error style class:
```
ng-class="{'has-error': errors.amount != null}"
```

## By example
For a complete example, please take a look at the example application. It also shows you how to easily implement a `CrudResource` for nested resources.

If you want to lean more about building RESTful web applications based on vanilla JAX-RS and Restangular, you may enjoy a blog post I’ve written about it; it features a complete example application as well.

## Specification
Crudlet supports these HTTP to persistence storage operations:

* GET /contextPath/model: service#findAll()
  * Searches for all entities of the given type.
  * returns HTTP 200 OK with list of entities
* GET /contextPath/model/\:id: service#findById(id)
  * Searches for the entities of the given type with the given id.
  * returns HTTP 200 OK with entity if found; or HTTP 404 NOT FOUND if entity is not found.
* PUT /contextPath/model/ with entity or PUT /contextPath/model/:id with entity or POST /contextPath/model/ with entity or POST   * /contextPath/model/:id with entity: service#save(entity)
  * Saves the entity for the first time or updates the existing entity, based on the presence of an id on the entity.
  * returns HTTP 200 OK with updated entity (e.g. new id) and Link header with content “/contextPath/model/:id”; or HTTP 400 BAD REQUEST with error information on validation error
* DELETE /contextPath/model/:id or DELETE /contextPath/model/:id with entity: service#delete(id)
  * Deletes the entity with the id provided
  * returns HTTP 204 NO CONTENT.

## Project status and future plans
Crudlet is currently experimental. I’d like to make some stability updates before releasing a proper 1.0 version. It may still already be useful for evaluation purposes, or as a skeleton to build your own solution.

This is a private project I’ve started for my own pleasure and usage and to learn more about building (Ajax) REST APIs, and I have no plans for (commercial) support.

You may also find more information about this project on this accompanying blog post.
